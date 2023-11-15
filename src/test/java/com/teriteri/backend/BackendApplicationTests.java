package com.teriteri.backend;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.OssUtil;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
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
    private VideoStatsService videoStatsService;

    @Autowired
    private OssUtil ossUtil;

    @Test
    void contextLoads() throws SQLException {
        System.out.println(dataSource.getClass());
        //获得连接
        Connection connection =  dataSource.getConnection();
        System.out.println(connection);
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        System.out.println("druidDataSource 数据源最小空闲连接数：" + druidDataSource.getMinIdle());
        System.out.println("druidDataSource 数据源最大连接数：" + druidDataSource.getMaxActive());
        System.out.println("druidDataSource 数据源初始化连接数：" + druidDataSource.getInitialSize());
        System.out.println("druidDataSource 空闲连接最小生存时间：" + druidDataSource.getMinEvictableIdleTimeMillis());
        System.out.println("druidDataSource 定时检查空闲连接的间隔：" + druidDataSource.getTimeBetweenEvictionRunsMillis());
        //关闭连接
        connection.close();
    }

    @Test
    void redis() {
        for (int i = 1; i <= 9; i++) {
            redisUtil.delValue("user:" + i);
        }
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
        String url = ossUtil.uploadImage(multipartFile, "cover");
        System.out.println(url);
    }

    @Test
    void ossCountFiles() {
        System.out.println(ossUtil.countFiles("img/cover/1696"));
    }

    @Test
    void ossdeleteFiles() {
        ossUtil.deleteFiles("img/cover/1696");
    }

    @Test
    void test() {
        VideoStats videoStats = videoStatsService.getVideoStatsById(1);
        System.out.println(videoStats);
    }
}