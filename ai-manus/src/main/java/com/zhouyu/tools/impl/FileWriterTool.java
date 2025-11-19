package com.zhouyu.tools.impl;

import com.zhouyu.tools.BaseTool;
import com.zhouyu.tools.ToolResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class FileWriterTool extends BaseTool {
    
    public FileWriterTool() {
        super("write_file", "写入内容到文件");
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return buildSchema(
            Map.of(
                "file_path", stringParam("要写入的文件路径"),
                "content", stringParam("要写入文件的内容"),
                "append", boolParam("追加到文件而不是覆盖（默认：false）")
            ),
            List.of("file_path", "content")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        try {
            String filePath = getString(parameters, "file_path");
            String content = getString(parameters, "content");
            boolean append = getBoolean(parameters, "append", false);

            if (filePath == null || filePath.trim().isEmpty()) {
                return ToolResult.error("文件路径为必填项");
            }

            if (content == null) {
                content = "";
            }

            Path path = Paths.get(filePath);

            // 如果父目录不存在则创建
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            if (append) {
                Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return ToolResult.success("成功写入文件：" + filePath);

        } catch (IOException e) {
            return ToolResult.error("写入文件失败：" + e.getMessage());
        }
    }
}