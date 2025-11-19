package com.zhouyu.agent;

import com.zhouyu.model.OpenAIClient;
import com.zhouyu.tools.ToolCollection;
import com.zhouyu.tools.impl.FileReaderTool;
import com.zhouyu.tools.impl.FileWriterTool;
import com.zhouyu.tools.impl.SandboxTool;
import com.zhouyu.tools.impl.TavilySearchTool;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class ManusAgent extends ToolCallAgent {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ManusAgent.class);

    private final static String SYSTEM_PROMPT = """
            ## 角色定义
            你是Manus，一个多功能的AI代理，能够使用可用的工具处理各种任务。
            
            ## 规则
            - 工作目录：{workspace}
            - Sandbox里面不使用工作目录
            - 利用Sandbox执行代码时，直接把代码内容传给Sandbox，而不是把代码脚本文件传给Sandbox
            - 一次只能执行一个工具
        
            """;

    public ManusAgent(OpenAIClient openAIClient) {
        super(openAIClient, null, null);

        ToolCollection toolCollection = new ToolCollection();
        toolCollection.addTool(new FileWriterTool());
        toolCollection.addTool(new FileReaderTool());
        toolCollection.addTool(new SandboxTool());
        toolCollection.addTool(new TavilySearchTool());
        this.toolCollection = toolCollection;

        // 如果工作区目录不存在则创建
        Path rootPath = getProjectRoot();
        Path workspaceRoot = rootPath.resolve("workspace");
        try {
            Files.createDirectories(workspaceRoot);
        } catch (IOException e) {
            logger.warn("创建工作区目录失败：{}", e.getMessage());
        }

        this.systemPrompt = SYSTEM_PROMPT.replace("{workspace}", workspaceRoot.toString());
    }

    private Path getProjectRoot() {
        // 通过查找pom.xml来找到项目根目录
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        // 回退到当前目录
        return Paths.get("").toAbsolutePath();
    }

}
