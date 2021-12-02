package com.tmalllet.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.tmalllet.entity.*;
import com.tmalllet.service.*;
import com.tmalllet.util.Result;
import com.tmalllet.util.comparator.*;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("front")
public class FrontRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderInfoService orderService;

    @Autowired
    PromotionService promotionService;

    // 限流器，1000个
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Autowired
    private RedisTemplate redisTemplate;

    // spring 提供的
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @GetMapping("home")
    public Object home() {
        // 查询所有分类
        List<Category> categories = categoryService.list();
        // 为这些分类填充产品集合
        categoryService.setProducts(categories);
        // 为这些分类填充推荐产品集合
        categoryService.setProductsByRow(categories);
        return categories;
    }

    // 注册
    @PostMapping("register")
    public Object register(@RequestBody User user) {
        // 把账号里的特殊符号进行转义
        String name = HtmlUtils.htmlEscape(user.getName());
        if (userService.isExist(name)) {
            String message = "用户名已被占用";
            return Result.fail(message);
        }

        user.setName(name);
        // 随机方式创建盐，并且加密算法采用 "md5", 进行 2 次加密。
        String password = user.getPassword();
        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        int times = 2;
        String algorithmName = "md5";
        String encodedPassword = new SimpleHash(algorithmName, password, salt, times).toString();

        user.setSalt(salt);
        user.setPassword(encodedPassword);

        userService.add(user);

        return Result.success();
    }

    @PostMapping("login")
    public Object login(@RequestBody User userParam, HttpSession session) {
        String name = userParam.getName();
        name = HtmlUtils.htmlEscape(name);

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, userParam.getPassword());
        try {
            subject.login(token);
            User user = userService.getOneByName(name);
            session.setAttribute("user", user);
            return Result.success();
        } catch (AuthenticationException e) {
            String message = "账号密码错误";
            return Result.fail(message);
        }
    }

    @GetMapping("checkLogin")
    public Object checkLogin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return Result.success();
        }
        return Result.fail("未登录");
    }

    @GetMapping("product/{pid}")
    public Object product(@PathVariable("pid") int pid) {
        // 根据 pid 获取Product 对象 product
        Product product = productService.findProductInCache(pid);


        // 获取产品的所有属性值
        List<PropertyValue> propertyValues = propertyValueService.list(pid);
        propertyValueService.setProperties(propertyValues);

        // 获取产品对应的所有的评价
        List<Review> reviews = reviewService.list(pid);
        // 为评价设置用户
        reviewService.setUsers(reviews);

        Map<String, Object> map = new HashMap<>();
        map.put("product", product);
        map.put("pvs", propertyValues);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    @GetMapping("category/{cid}")
    public Object category(@PathVariable int cid, String sort) {
        Category category = categoryService.getById(cid);
        // 为分类填充产品
        categoryService.setProduct(category);
        // 为产品填充销量和评价数据
        productService.setSaleAndReviewNumber(category.getProducts());

        // 如果sort==null，即不排序
        if (null != sort) {
            switch (sort) {
                case "review":
                    Collections.sort(category.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(category.getProducts(), new ProductDateComparator());
                    break;

                case "saleCount":
                    Collections.sort(category.getProducts(), new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(category.getProducts(), new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(category.getProducts(), new ProductAllComparator());
                    break;
            }
        }
        return category;
    }

    @PostMapping("search")
    public Object search(String keyword) throws Exception {
        if (null == keyword) {
            keyword = "";
        }
        // 根据keyword进行模糊查询，获取满足条件的前20个产品
        List<Product> products = productService.searchByName(keyword, 0, 20);
        // 为这些产品设置图片、销量、评价数量
        productService.setFirstProductImages(products);
        productService.setSaleAndReviewNumber(products);
        return products;
    }

    // 立即购买
    @GetMapping("buyone")
    public Object buyOne(int pid, int num, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        // 如果商品是秒杀商品
        Promotion promotion = promotionService.getByProductId(pid);
        // 0 代表秒杀活动进行中
        if (promotion != null && promotion.getStatus() != 0) {
            // 走秒杀逻辑
            return createForMiaoShao(pid, num, user, promotion);
        } else {
            // 否则，走普通下单逻辑
            return buyOneAndAddCart(pid, num, session);
        }
    }

    /**
     * 秒杀活动的下单逻辑
     * 限流
     * 这里，只要向 mq 传递半成品消息成功，就可以返回用户下单成功的消息了
     */
    private Object createForMiaoShao(int pid, int num, User user, Promotion promotion) {

        int promotionId = promotion.getId();

        // 抢令牌
        String promotionToken = promotionService.generateToken(user.getId(), pid, promotionId);
        if (StringUtils.isEmpty(promotionToken)) {
            // 没抢到令牌
            return Result.fail("下单失败！");
        }

        // 限制单机流量，一秒之内能申请到，就可以
        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            return Result.fail("服务器繁忙，请稍后再试！");
        }

        // 加入队列等待
        Future future = taskExecutor.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                // 异步创建订单
                // 创建流水、发送消息
//              orderService.createOrder(user.getId(), itemId, amount, promotionId);
                return orderService.createOrderAsync(user.getId(), pid, 1, promotionId);
            }
        });

        // 验证处理结果
        try {
            return future.get();
        } catch (Exception e) {
            return Result.fail("下单失败！");
        }

    }

    // 添加购物车
    @GetMapping("addCart")
    public Object addCart(int pid, int num, HttpSession session) {
        buyOneAndAddCart(pid, num, session);
        return Result.success();
    }

    private int buyOneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.getById(pid);
        User user = (User) session.getAttribute("user");
        // 如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。
        // 那么就应该在对应的OrderItem基础上，调整数量
        // 基于用户对象user，查询没有生成订单的订单项集合
        List<OrderItem> items = orderItemService.listUnBuyByUserId(user.getId());
        // 遍历，如果产品相同，追加数量
        for (OrderItem item : items) {
            if (item.getProductId().equals(product.getId())) {
                item.setNumber(item.getNumber() + num);
                orderItemService.updateById(item);
                // 获取这个订单项的 id
                return item.getId();
            }
        }

        // 如果不存在对应的OrderItem，新增一个订单项OrderItem
        OrderItem item = new OrderItem();
        item.setUserId(user.getId());
        item.setProductId(product.getId());
        item.setNumber(num);
        item.setPrice(product.getPromotePrice());
        orderItemService.add(item);
        // 返回当前订单项id
        return item.getId();
    }

    // 结算
    @GetMapping("buy")
    // http://127.0.0.1:9090/tmall/front/buy?oiid=1
    // 兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid
    public Object buy(String[] oiid, HttpSession session) {
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;
        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            // 根据传入的订单项id，获取订单项
            OrderItem item = orderItemService.getById(id);

            Product product = productService.getById(item.getProductId());
            // 为产品设置图片
            productService.setFirstProductImage(product);
            // 为订单项设置product
            item.setProduct(product);
            // 计算订单总金额
            total += item.getNumber() * item.getPrice();
            orderItems.add(item);
        }

        session.setAttribute("ois", orderItems);

        Map<String, Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);
        return Result.success(map);
    }

    // 购物车页面的数据
    @GetMapping("cart")
    public Object cart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<OrderItem> items = orderItemService.listUnBuyByUserId(user.getId());
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (null != product) {
                productService.setFirstProductImage(product);
            }
            item.setProduct(product);
        }
        return items;
    }

    // 根据user和pid，更改orderItem的数量num
    @GetMapping("changeOrderItem")
    public Object changeOrderItem(int pid, int num, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user) {
            return Result.fail("未登录");
        }

        List<OrderItem> items = orderItemService.listUnBuyByUserId(user.getId());
        for (OrderItem item : items) {
            if (item.getProductId() == pid) {
                item.setNumber(num);
                orderItemService.updateById(item);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("deleteOrderItem")
    public Object deleteOrderItem(HttpSession session, int oiid) {
        User user = (User) session.getAttribute("user");
        if (null == user) {
            return Result.fail("未登录");
        }

        orderItemService.removeById(oiid);
        return Result.success();
    }

    // “提交订单”按钮后
    @PostMapping("createOrder")
    public Object createOrder(@RequestBody OrderInfo order, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user) {
            return Result.fail("未登录");
        }

        // 生成订单号
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(user.getId());
        order.setStatus(OrderInfoService.waitPay);
        // 从session中获取订单项集合 ( 在结算功能的ForeRESTController.buy() ，订单项集合被放到了session中 )
        List<OrderItem> items = (List<OrderItem>) session.getAttribute("ois");

        // 把订单加入到数据库，并且遍历订单项集合，设置每个订单项的order，更新到数据库
        orderService.add(order, items);

        Map<String, Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", order.getTotalPrice());

        return Result.success(map);
    }

    // 修改订单状态为已支付
    @GetMapping("payed")
    public Object payed(int oid) {
        OrderInfo order = orderService.getById(oid);
        order.setStatus(OrderInfoService.waitDelivery);
        order.setPayDate(LocalDateTime.now());
        orderService.updateById(order);
        return order;
    }

    // 根据用户获取有效订单集合
    @GetMapping("bought")
    public Object bought(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (null == user) {
            return Result.fail("未登录");
        }
        // 获取所有订单
        List<OrderInfo> orders = orderService.listByUserIdWithoutDelete(user.getId());
        // 获取订单项
        for (OrderInfo order : orders) {
            orderService.setOrderItem(order);
            // 获取订单项对应的商品
            for (OrderItem item : order.getOrderItems()) {
                orderItemService.setProduct(item);
                // 为商品设置第一张商品详情图片
                productService.setFirstProductImage(item.getProduct());
            }
        }
        return orders;
    }

    // 根据单个订单id获取详情
    @GetMapping("confirmPay")
    public Object confirmPay(int oid) {
        OrderInfo order = orderService.getById(oid);
        // 为订单对象填充订单项
        orderService.setOrderItem(order);

        // 获取订单项对应的商品
        for (OrderItem item : order.getOrderItems()) {
            orderItemService.setProduct(item);
            // 为商品设置第一张商品详情图片
            productService.setFirstProductImage(item.getProduct());
        }

        return order;
    }

    // 更新订单状态为待评论，设置确认收货时间
    @GetMapping("orderConfirmed")
    public Object orderConfirmed(int oid) {
        OrderInfo order = orderService.getById(oid);
        order.setStatus(OrderInfoService.waitReview);
        order.setConfirmDate(LocalDateTime.now());
        orderService.updateById(order);
        return Result.success();
    }

    // 删除订单，更新订单状态为删除
    @PutMapping("deleteOrder")
    public Object deleteOrder(int oid) {
        OrderInfo order = orderService.getById(oid);
        order.setStatus(OrderInfoService.delete);
        orderService.updateById(order);
        return Result.success();
    }

    // 评论，返回产品、订单、评论
    @GetMapping("review")
    public Object review(int oid) {
        OrderInfo order = orderService.getById(oid);
        // 为订单对象填充订单项
        orderService.setOrderItem(order);

        // 获取第一个订单项
        OrderItem item = order.getOrderItems().get(0);
        // 为订单项设置商品
        orderItemService.setProduct(item);
        Product product = item.getProduct();
        // 为商品设置第一张商品详情图片
        productService.setFirstProductImage(product);

        // 获取这个产品的评价集合
        List<Review> reviews = reviewService.list(product.getId());
        // 为评论设置用户，匿名用户名
        for (Review review : reviews) {
            reviewService.setUser(review);
        }
        // 为产品设置评价数量和销量
        productService.setSaleAndReviewNumber(product);

        Map<String, Object> map = new HashMap<>();
        map.put("p", product);
        map.put("o", order);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    // 提交评价
    @PostMapping("doreview")
    public Object doreview(HttpSession session, int oid, int pid, String content) {
        // 更新订单状态为已评论
        OrderInfo order = orderService.getById(oid);
        order.setStatus(OrderInfoService.finish);
        orderService.updateById(order);

        // 转义传入的评价信息
        content = HtmlUtils.htmlEscape(content);

        // Product product = productService.getById(pid);
        User user = (User) session.getAttribute("user");

        Review review = new Review();
        review.setContent(content);
        review.setProductId(pid);
        review.setCreateDate(LocalDateTime.now());
        review.setUserId(user.getId());
        reviewService.add(review);

        return Result.success();
    }
}
