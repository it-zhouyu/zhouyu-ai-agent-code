package com.zhouyu.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class OrderDocumentParser {

    @Value("classpath:customer.txt")
    private Resource resource;
    public List<Document> parse() {
        try {
            TextReader textReader = new TextReader(resource);
            List<Document> documents = textReader.get();
            
            List<Document> parsedDocuments = new ArrayList<>();
            
            for (Document document : documents) {
                String content = document.getText();
                if (content != null) {
                    Map<String, String> thirdLevelHeadings = parseThirdLevelHeadings(content);
                    
                    for (Map.Entry<String, String> entry : thirdLevelHeadings.entrySet()) {
                        String title = entry.getKey();
                        String knowledgeContent = entry.getValue();
                        
                        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                        metadata.put("title", title);
                        metadata.put("level", "h3");
                        
                        Document parsedDoc = new Document(title + "\n" +knowledgeContent, metadata);
                        parsedDocuments.add(parsedDoc);
                    }
                }
            }
            
            return parsedDocuments;
        } catch (Exception e) {
            throw new RuntimeException("解析订单文档失败", e);
        }
    }
    
    private Map<String, String> parseThirdLevelHeadings(String content) {
        Map<String, String> headings = new HashMap<>();
        String[] lines = content.split("\n");
        
        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();
        
        for (String line : lines) {
            if (line.startsWith("### ")) {
                if (currentTitle != null) {
                    headings.put(currentTitle, currentContent.toString().trim());
                }
                currentTitle = line.substring(4).trim();
                currentContent = new StringBuilder();
            } else if (line.startsWith("## ") || line.startsWith("# ")) {
                if (currentTitle != null) {
                    headings.put(currentTitle, currentContent.toString().trim());
                    currentTitle = null;
                    currentContent = new StringBuilder();
                }
            } else if (currentTitle != null) {
                currentContent.append(line).append("\n");
            }
        }
        
        if (currentTitle != null) {
            headings.put(currentTitle, currentContent.toString().trim());
        }
        
        return headings;
    }
}
