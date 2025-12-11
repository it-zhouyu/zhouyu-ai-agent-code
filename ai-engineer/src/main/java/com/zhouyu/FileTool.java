package com.zhouyu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具
 */
@Slf4j
public class FileTool {

    @Tool(description = "列出文件")
    public String listProjectFiles(@ToolParam(description = "要列出文件的目录路径，必须包含指定的项目工作目录") String path) {
        try {
            log.info("列出文件: {}", path);
            Path pathDirectory = Paths.get(path);
            if (!Files.exists(pathDirectory)) {
                return "目录不存在: " + path;
            }

            return Files.list(pathDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("列出文件失败: {}", path, e);
            return "列出文件失败: " + e.getMessage();
        }
    }

    @Tool(description = "读取文件")
    public String readFile(@ToolParam(description = "要读取文件的路径，必须包含指定的项目工作目录") String filePath) {
        try {
            log.info("读取文件: {}", filePath);
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readString(path);
            } else {
                log.warn("文件不存在: {}", filePath);
                return "文件不存在: " + filePath;
            }
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return "读取文件失败: " + e.getMessage();
        }
    }

    @Tool(description = "写入文件")
    public String writeFile(@ToolParam(description = "要写入文件的文件路径，必须包含指定的项目工作目录") String filePath, @ToolParam(description = "写入文件的内容") String content) {
        try {
            log.info("写入文件: {}", filePath);
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "文件写入成功: " + filePath;
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return "写入文件失败: " + e.getMessage();
        }
    }

//    @Tool(description = "创建目录")
//    public String mkdir(String dirPath) {
//        Path path = Paths.get(dirPath);
//        try {
//            log.info("创建目录: " + dirPath);
//            if (Files.exists(path)) {
//                return "目录已存在: " + dirPath;
//            }
//            Files.createDirectories(path);
//            return "目录创建成功: " + dirPath;
//        } catch (IOException e) {
//            log.error("创建目录失败: {}", dirPath, e);
//            return "创建目录失败: " + e.getMessage();
//        }
//    }
}
