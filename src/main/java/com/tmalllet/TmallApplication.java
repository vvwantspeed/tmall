package com.tmalllet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan(basePackages = {"com.tmalllet.mapper"})
@EnableCaching
public class TmallApplication {
    static {
        // PortUtil.checkPort(6379,"Redis 服务端",true);
        // PortUtil.checkPort(9300,"ElasticSearch 服务端",true);
        // PortUtil.checkPort(5601,"Kibana 工具", true);
    }

    public static void main(String[] args) {
        SpringApplication.run(TmallApplication.class, args);
    }

}
