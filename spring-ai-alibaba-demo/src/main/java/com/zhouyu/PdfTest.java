package com.zhouyu;

import com.alibaba.cloud.ai.parser.apache.pdfbox.PagePdfDocumentParser;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class PdfTest {

    public static void main(String[] args) throws IOException {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfBottomTextLinesToDelete(3)   // 去掉页码
                        .build())
                .build();

        PagePdfDocumentParser parser = new PagePdfDocumentParser(config);

        List<Document> documents = parser.parse(new DefaultResourceLoader().getResource("classpath:/pdf-test.pdf").getInputStream());

        System.out.println("文档数量：" + documents.size());
        for (Document document : documents) {
            System.out.println(document);
        }
    }
}
