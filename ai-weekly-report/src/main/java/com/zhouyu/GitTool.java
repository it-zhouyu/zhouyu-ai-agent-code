package com.zhouyu;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class GitTool {

    @Tool(description = "获取Git仓库的提交记录")
    public String getCommitLog(String projectPath) {
        try {
            return fetchWeeklyGitLog(projectPath);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchWeeklyGitLog(String projectPath) throws IOException, InterruptedException {

        // 计算一周前的日期
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        String sinceDate = oneWeekAgo.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 自动获取本地配置的 git 用户名，以实现 "只看自己"
        String authorName = getLocalGitUser(projectPath);

        System.out.println("正在获取从" + sinceDate + "开始的提交记录...");
        System.out.println("正在为作者" + authorName + "获取提交记录...");

        // 构建 git log 命令
        // 格式: [日期] | [作者] | [提交信息]
        ProcessBuilder processBuilder = new ProcessBuilder(
                "git",
                "log",
                "--since=" + sinceDate,
                "--author=" + authorName,    // --author 标志来筛选作者
                "--pretty=format:%ad | %an | %s", // %ad: 日期, %an: 作者, %s: 摘要
                "--date=short" // 日期格式 YYYY-MM-DD
        );

        // 在指定的项目目录中执行命令
        processBuilder.directory(new File(projectPath));
        processBuilder.redirectErrorStream(true); // 将错误流合并到标准输出流

        Process process = processBuilder.start();

        // 读取命令的输出
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // 如果 git 命令执行失败
            throw new IOException("Git log command failed with exit code " + exitCode + ". \nOutput: " + output);
        }

        System.out.println("本周提交记录为：\n" + output);

        return output;
    }

    private String getLocalGitUser(String projectPath) throws IOException, InterruptedException {

        // 开启一个进程
        ProcessBuilder processBuilder = new ProcessBuilder("git", "config", "user.name");
        processBuilder.directory(new File(projectPath));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String userName;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            // 读取命令输出并去除首尾空格
            userName = reader.lines().collect(Collectors.joining()).trim();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0 || userName.isBlank()) {
            // 如果命令执行失败或用户名为空，抛出明确的异常
            throw new IOException("无法获取本地 Git 用户名 (git config user.name)。" +
                    "请确保您在 '" + projectPath + "' 目录中配置了 Git 用户名。");
        }

        return userName;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GitTool gitTool = new GitTool();
        gitTool.fetchWeeklyGitLog("/Users/dadudu/idea/java/vip/zhouyu-ai-agent-vip");
    }
}
