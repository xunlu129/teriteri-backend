package com.teriteri.backend.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 程序启动前创建分片目录
 */
@Component
public class StartupRunner implements CommandLineRunner {

    @Value("${directory.chunk}")
    private String chunkDirectory;

    @Override
    public void run(String... args) throws Exception {
        File chunkDir = new File(chunkDirectory);
        if (!chunkDir.exists()) {
            boolean created = chunkDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + chunkDirectory);
            }
        }
    }
}