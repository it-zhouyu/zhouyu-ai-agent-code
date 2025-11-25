package com.zhouyu;

import com.alibaba.cloud.ai.parser.markdown.MarkdownDocumentParser;
import com.alibaba.cloud.ai.parser.markdown.config.MarkdownDocumentParserConfig;
import org.springframework.ai.document.Document;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class MarkdownTest {

    public static void main(String[] args) throws IOException {
        MarkdownDocumentParserConfig config = MarkdownDocumentParserConfig.builder()
                .withAdditionalMetadata("title", "zhouyu_title")
                .withIncludeCodeBlock(true)             // 代码块包含到知识点中，而不是单独的知识点
                .withIncludeBlockquote(true)            // 引用块包含到知识点中，而不是单独的知识点
                .withHorizontalRuleCreateDocument(true) // 根据分隔符切分知识点
                .build();
        MarkdownDocumentParser parser = new MarkdownDocumentParser(config);
        InputStream inputStream = new DefaultResourceLoader().getResource("classpath:/markdown-test.md").getInputStream();

        List<Document> documents = parser.parse(inputStream);
        for (Document document : documents) {
            System.out.println(document);
        }
    }
}
