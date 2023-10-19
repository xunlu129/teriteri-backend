package com.teriteri.backend.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSSConfig {
    @Value("${oss.endpoint}")
    private String OSS_ENDPOINT;

    @Value("${oss.keyId}")
    private String ACCESS_KEY_ID;

    @Value("${oss.keySecret}")
    private String ACCESS_KEY_SECRET;

    @Value("${oss.idleTimeout}")
    private long IDLE_TIMEOUT;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        //连接空闲超时时间，超时则关闭
        conf.setIdleConnectionTime(IDLE_TIMEOUT);
        // 创建OSSClient实例
        return new OSSClientBuilder().build(OSS_ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET, conf);
    }
}
