package com.tmalllet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tmalllet.entity.Product;
import com.tmalllet.entity.ProductImage;
import com.tmalllet.entity.ProductStockLog;
import com.tmalllet.entity.Promotion;
import com.tmalllet.mapper.ProductMapper;
import com.tmalllet.mapper.ProductStockLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "products")
public class ProductService extends ServiceImpl<ProductMapper, Product> {
    Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    ProductMapper productMapper;

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    ReviewService reviewService;

    @Autowired
    ESService esService;

    @Autowired
    PromotionService promotionService;

    @Autowired
    ProductStockLogService productStockLogService;

    @Autowired
    ProductStockLogMapper productStockLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 本地缓存
    private Cache<String, Object> cache;

    /**
     * Bean 加载时初始化本地缓存
     */
    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }


    @Cacheable(key = "'products-cid-'+#p0")
    public List<Product> listByCategoryId(Integer cid) {
        return productMapper.selectAllByCategoryId(cid);
    }

    @Cacheable(key = "'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public PageInfo<Product> page(int start, int size, Integer cid) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> listByCategoryId(cid));
    }

    @CacheEvict(allEntries = true)
    public void add(Product product) throws IOException {
        productMapper.insert(product);
        esService.add(product);
    }

    @CacheEvict(allEntries = true)
    public void removeById(Integer id) throws Exception {
        super.removeById(id);
        esService.delete(id);
    }


    @CacheEvict(allEntries = true)
    public void update(Product product) throws Exception {
        super.updateById(product);
        esService.update(product);
    }

    @Cacheable(key = "'products-name-like-'+#p0+'-'+#p1")
    public List<Product> searchByName(String keyword, Integer start, Integer size) throws Exception {
        // return productMapper.selectAllByNameLikeLimit(keyword, limit);
        esService.init();
        return esService.searchByName(keyword, start, size);
    }

    /**
     * 获取商品数据，并填充上面的图片集合、销量、评价数量、头图
     *
     * @param pid
     * @return
     */
    public Product findProductById(Integer pid) {
        Product product = getById(pid);

        // 根据product_id，获取这个产品对应的 单个/详情 图片集合
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(pid);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(pid);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);
        // 设置产品的销量，评价数量
        setSaleAndReviewNumber(product);
        // 设置第一张产品图片
        setFirstProductImage(product);

        // 获取商品对应的秒杀活动
        Promotion promotion = promotionService.getByProductId(pid);
        return product;
    }

    /**
     * 加了两级缓存来获取产品信息
     *
     * @param pid
     * @return
     */
    public Product findProductInCache(Integer pid) {

        Product product = null;
        String key = "product:" + pid;

        // guava
        product = (Product) cache.getIfPresent(key);
        if (product != null) {
            return product;
        }

        // redis
        product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            cache.put(key, product);
            return product;
        }

        // mysql
        product = this.findProductById(pid);
        if (product != null) {
            cache.put(key, product);
            redisTemplate.opsForValue().set(key, product, 3, TimeUnit.MINUTES);
        }

        return product;
    }


    // 为产品设置销量和评论数据
    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product product : products)
            setSaleAndReviewNumber(product);
    }

    public void setSaleAndReviewNumber(Product product) {
        int saleCount = orderItemService.getSaleCount(product.getId());
        product.setSaleCount(saleCount);

        int reviewCount = reviewService.getCount(product.getId());
        product.setReviewCount(reviewCount);
    }

    public void setFirstProductImage(Product product) {
        List<ProductImage> singleImages = productImageService.listSingleProductImages(product.getId());
        if (!singleImages.isEmpty()) {
            product.setFirstProductImage(singleImages.get(0));
        } else {
            product.setFirstProductImage(new ProductImage());
        }
    }

    public void setFirstProductImages(List<Product> products) {
        for (Product product : products)
            setFirstProductImage(product);
    }

    /**
     * 生成库存流水
     *
     * @param pid
     * @param amount
     * @return
     */
    public ProductStockLog createProductStockLog(int pid, int amount) {
        if (pid <= 0 || amount <= 0) {
            return null;
        }

        ProductStockLog log = new ProductStockLog();
        log.setId(UUID.randomUUID().toString().replace("-", ""));
        log.setProductId(pid);
        log.setAmount(amount);
        log.setStatus(0);

        productStockLogMapper.insert(log);

        return log;
    }

    /**
     * 在 MySQL 里扣减库存
     * @param pid
     * @param amount
     */
    public boolean decreaseStock(int pid, int amount) throws Exception {
        if (pid <= 0 || amount <= 0) {
            throw new Exception("参数不合法！");
        }

        int rows = productMapper.decreaseStock(pid, amount);
        return rows > 0;
    }

    /**
     * 在 redis 缓存里扣减库存
     * 减去 amount 的数量
     *
     * @param pid
     * @param amount
     * @return
     */
    public boolean decreaseStockInCache(int pid, int amount) throws Exception {
        if (pid <= 0 || amount <= 0) {
            throw new Exception("参数不合法！");
        }

        String key = "product:stock:" + pid;
        long result = redisTemplate.opsForValue().decrement(key, amount);
        if (result < 0) {
            // 不能扣减，回补
            this.increaseStockInCache(pid, amount);
            logger.debug("回补库存完成 [" + pid + "]");
        } else if (result == 0) {
            // 售罄
            redisTemplate.opsForValue().set("product:stock:over:" + pid, 1);
            logger.debug("售罄标识完成 [" + pid + "]");
        }

        // 返回扣减库存是否成功
        return result >= 0;
    }

    private boolean increaseStockInCache(int pid, int amount) throws Exception {
        if (pid <= 0 || amount <= 0) {
            throw new Exception("参数不合法！");
        }

        String key = "product:stock:" + pid;
        redisTemplate.opsForValue().increment(pid, amount);
        return true;
    }

    /**
     * 更新库存流水状态
     * @param id
     * @param status
     */
    public void updateProductStockLogStatus(String id, int status) {
        ProductStockLog log = productStockLogMapper.selectById(id);
        log.setStatus(status);
        productStockLogMapper.updateById(log);
    }

    /**
     * 增加销量，加 amount
     * @param pid
     * @param amount
     */
    public void increaseSales(int pid, int amount) {
        if (pid <= 0 || amount <= 0) {
            return;
        }
        productMapper.increaseSales(pid, amount);
    }

    /**
     * 根据id获取
     * @param logId
     * @return
     */
    public ProductStockLog findProductStockLogById(String logId) {
        if (StringUtils.isEmpty(logId)) {
            return null;
        }
        return productStockLogMapper.selectById(logId);
    }

    /**
     * 获取活动商品
     * @return
     */
    public List<Product> findProductsOnPromotion() {
        List<Product> list = productMapper.selectAllInPromotion();
        return list.stream()
                .map((product -> {
                        Promotion promotion = promotionService.getByProductId(product.getId());
                        product.setPromotion(promotion);
                        return product;
                    }))
                // 要活动进行中的商品
                .filter((product -> product.getPromotion().getStatus() == 0))
                .collect(Collectors.toList());
    }
}




