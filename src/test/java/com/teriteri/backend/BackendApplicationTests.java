package com.teriteri.backend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.OssUtil;
import com.teriteri.backend.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest(properties = {"spring.profiles.active=test"})
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
    private UserMapper userMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private ESUtil esUtil;

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
        List<RedisUtil.ZObjScore> list = redisUtil.zReverangeWithScores("search_word", 0L, -1L);
        int count = list.size();
        double total = 0;
        for (RedisUtil.ZObjScore o : list) {
            System.out.println(o.getMember() + " " + o.getScore());
            total += o.getScore();
        }
        BigDecimal bt = new BigDecimal(total);
        total = bt.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.println("count " + count + " total " + total);
        for (RedisUtil.ZObjScore o : list) {
            BigDecimal b = new BigDecimal((o.getScore() / total) * count);
            double score = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            o.setScore(score);
            System.out.println(o.getMember() + " " + o.getScore());
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


    // 测试 ElasticSearch
    // 创建索引
    @Test
    void createIndex() throws IOException {
        InputStream input1 = this.getClass().getResourceAsStream("/static/esindex/video.json");
        InputStream input2 = this.getClass().getResourceAsStream("/static/esindex/user.json");
        InputStream input3 = this.getClass().getResourceAsStream("/static/esindex/search_word.json");
        CreateIndexRequest req1 = CreateIndexRequest.of(b -> b.index("video").withJson(input1));
        CreateIndexRequest req2 = CreateIndexRequest.of(b -> b.index("user").withJson(input2));
        CreateIndexRequest req3 = CreateIndexRequest.of(b -> b.index("search_word").withJson(input3));
        CreateIndexResponse resp1 = client.indices().create(req1);
        CreateIndexResponse resp2 = client.indices().create(req2);
        CreateIndexResponse resp3 = client.indices().create(req3);
        System.out.println(resp1);
        System.out.println(resp2);
        System.out.println(resp3);
    }


    // 批量添加文档
    @Test
    void bulkAddDocVideo() throws IOException {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 3);
        List<Video> videos = videoMapper.selectList(queryWrapper);
        List<BulkOperation> bulkOperationList = new ArrayList<>();
        for (Video video : videos) {
            ESVideo esVideo = new ESVideo(video.getVid(), video.getUid(), video.getTitle(), video.getMcId(), video.getScId(), video.getTags(), video.getStatus());
            bulkOperationList.add(BulkOperation.of(o -> o.index(i -> i.document(esVideo).id(esVideo.getVid().toString()))));
        }
        BulkResponse bulkResponse = client.bulk(b -> b.index("video").operations(bulkOperationList));
        System.out.println(bulkResponse);
    }

    @Test
    void bulkAddDocUser() throws IOException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", 2);
        List<User> users = userMapper.selectList(queryWrapper);
        List<BulkOperation> bulkOperationList = new ArrayList<>();
        for (User user : users) {
            ESUser esUser = new ESUser(user.getUid(), user.getNickname());
            bulkOperationList.add(BulkOperation.of(o -> o.index(i -> i.document(esUser).id(esUser.getUid().toString()))));
        }
        BulkResponse bulkResponse = client.bulk(b -> b.index("user").operations(bulkOperationList));
        System.out.println(bulkResponse);
    }

    @Test
    void updateDocUser() throws IOException {
        User user = new User();
        user.setUid(13);
        user.setNickname("迷鹿");
        esUtil.updateUser(user);
    }

    // 删除文档
    @Test
    void deleteDoc() throws IOException {
        System.out.println(client.delete(d -> d.index("test2").id("3")));
    }

    // 查询文档数量
    @Test
    void searchCount() throws IOException {

        System.out.println(esUtil.getVideoCount("原神", true));
    }

    // 模糊匹配，分页查询
    @Test
    void searchMatch() throws IOException {
        List<Integer> list = esUtil.searchVideosByKeyword("原神", 1, 30, true);
        for (Integer i : list) {
            System.out.print(i + " ");
        }
    }


    /**
     * 格式化包含特殊字符的字符串
     */
    String formatString(String input) {
        // 使用正则表达式替换特殊字符，并保留一个空格符
        String formattedString = input.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fffぁ-んァ-ヶ]+", " ");
        // 去除首尾空格
        formattedString = formattedString.trim();
        return formattedString;
    }

    /**
     * 计算格式化后的中文和字母数量
     */
    int countChineseAndLetters(String formattedString) {
        // 去除数字和空格，计算剩余字符中中文和字母的数量
        String filteredString = formattedString.replaceAll("[0-9\\s]+", "");
        return filteredString.length();
    }

    @Test
    void testFormatString() {
        String formattedString = formatString("【我推的孩子】主题曲 YOASOBI「アイドル」 《偶像》 官方MV");
        System.out.println(formattedString);
        System.out.println(countChineseAndLetters(formattedString));
    }
}