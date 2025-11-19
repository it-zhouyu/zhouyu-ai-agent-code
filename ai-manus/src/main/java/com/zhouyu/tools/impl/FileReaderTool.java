package com.zhouyu.tools.impl;

import com.zhouyu.tools.BaseTool;
import com.zhouyu.tools.ToolResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class FileReaderTool extends BaseTool {
    
    public FileReaderTool() {
        super("read_file", "读取文件内容");
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return buildSchema(
            Map.of(
                "file_path", stringParam("要读取的文件路径")
            ),
            List.of("file_path")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        try {
            String filePath = getString(parameters, "file_path");
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return ToolResult.error("文件不存在：" + filePath);
            }

            String content = Files.readString(path);
            return ToolResult.success(content);
        } catch (IOException e) {
            return ToolResult.error("读取文件失败：" + e.getMessage());
        }
    }
}