package com.zhouyu.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class RagTool {

    private VectorStore vectorStore;

    public RagTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(description = "内部知识库搜索工具")
    public List<Document> ragTool(@ToolParam(description = "用户原始的问题") String query) {
        return vectorStore.similaritySearch(query);
    }
}
