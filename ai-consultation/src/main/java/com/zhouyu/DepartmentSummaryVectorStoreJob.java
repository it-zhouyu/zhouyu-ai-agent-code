package com.zhouyu;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
@Log4j2
public class DepartmentSummaryVectorStoreJob {

    @Autowired
    private VectorStore vectorStore;

    private static final String DEPARTMENT_SUMMARY_INFO_DIR = "ai-consultation/department_summary_info";

    @PostConstruct
    public void init() {
        log.info("开始向量数据库初始化...");
        List<Document> documents = getDepartmentInfoDocuments();

        // 批量添加
        for (int i = 0; i < documents.size(); i += 5) {
            vectorStore.add(documents.subList(i, Math.min(i + 5, documents.size())));
        }
    }


    public List<Document> getDepartmentInfoDocuments() {
        ResourceLoader resourceLoader = new FileSystemResourceLoader();
        Resource resource = resourceLoader.getResource(DEPARTMENT_SUMMARY_INFO_DIR);

        List<Document> documents = new ArrayList<>();
        try {
            File[] resources = resource.getFile().listFiles();
            for (File file : resources) {

                String content = new String(Files.readAllBytes(file.toPath()));
                Document document = new Document(content);
                document.getMetadata().put("departmentName", file.getName().replace(".txt", ""));
                documents.add(document);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return documents;
    }
}
