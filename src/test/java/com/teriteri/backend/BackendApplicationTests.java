package com.teriteri.backend;

import com.alibaba.druid.pool.DruidDataSource;
import com.teriteri.backend.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class ApplicationTests {
    @Autowired
    DataSource dataSource;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    void contextLoads() throws SQLException {
        System.out.println(dataSource.getClass());
        //获得连接
        Connection connection =  dataSource.getConnection();
        System.out.println(connection);
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        System.out.println("druidDataSource 数据源最大连接数：" + druidDataSource.getMaxActive());
        System.out.println("druidDataSource 数据源初始化连接数：" + druidDataSource.getInitialSize());
        //关闭连接
        connection.close();
    }

    @Test
    void redis() {

        // redisTemplate 操作不同的数据类型
        redisTemplate.opsForValue().set("name", "xunlu");
        System.out.println(redisTemplate.opsForValue().get("name"));

        // 获取 redis 的连接对象
//        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
//        connection.flushDb();
//        connection.close();

    }

}