package com.teriteri.backend;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.utils.OssUploadUtil;
import com.teriteri.backend.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

@SpringBootTest
class ApplicationTests {
    @Autowired
    DataSource dataSource;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private OssUploadUtil ossUploadUtil;

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

    @Test
    void redisGetMembers() {
        Set<Object> set = redisUtil.getMembers("video_status:0");
        System.out.println(set);
    }

    @Test
    void ossUploadImg() throws IOException {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", 3);
        Video video = videoMapper.selectOne(queryWrapper);
        String filePath = video.getCoverUrl();
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                "application/sql", fileInputStream);
        String url = ossUploadUtil.uploadImage(multipartFile, "cover");
        System.out.println(url);
    }

    @Test
    void ossCountFiles() {
        System.out.println(ossUploadUtil.countFiles("img/cover/1696"));
    }

    @Test
    void ossdeleteFiles() {
        ossUploadUtil.deleteFiles("img/cover/1696");
    }

    @Test
    void test() {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", 10);
        Video video = videoMapper.selectOne(queryWrapper);
        System.out.println(video);
    }
}