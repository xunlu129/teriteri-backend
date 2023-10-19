package com.teriteri.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 暂时用不到，先注了
 */
@Configuration
public class RabbitMQConfig {

//    // 1.声明 direct 模式的交换机
//    /**
//     *投稿相关的交换机
//     */
//    @Bean
//    public DirectExchange directUploadExchange() {
//        return new DirectExchange("direct_upload_exchange", true, false);
//    }
//
//    // 2.声明队列
//    /**
//     * 视频投稿信息队列
//     */
//    @Bean
//    public Queue videoUploadQueue() {
//        return new Queue("videoUpload_direct_queue", true);
//    }
//
//    // 3.队列和交换机完成绑定关系
//    @Bean
//    public Binding videoUploadBinding() {
//        return BindingBuilder.bind(videoUploadQueue()).to(directUploadExchange()).with("videoUpload");
//    }

}
