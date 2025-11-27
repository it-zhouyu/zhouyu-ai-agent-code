package com.zhouyu;

import com.microsoft.playwright.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class DepartmentScraper {

    // 目标网站 URL
    private static final String BASE_URL = "https://www.xy3yy.com";
    private static final String START_URL = BASE_URL + "/ksjj/15380.html";

    // 用于定位元素的选择器 (Selector)
    // 这是最可能需要根据网站变动而修改的地方
    private static final String DEPT_LIST_SELECTOR = ".z_rw_l ul li a";
    private static final String CONTENT_SELECTOR = ".z_rw_r .content";

    // 输出目录
    private static final String OUTPUT_DIR = "ai-consultation/department_info";

    public static void main(String[] args) {

        try (Playwright playwright = Playwright.create()) {
            // 启动浏览器，建议使用 headless: false 进行调试，部署时改为 true
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            System.out.println("开始抓取科室列表: " + START_URL);
            page.navigate(START_URL);

            // 1. 创建用于存储的文件夹
            Path outputDir = Paths.get(OUTPUT_DIR);
            try {
                Files.createDirectories(outputDir);
                System.out.println("输出目录已创建: " + outputDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("创建目录失败: " + e.getMessage());
                return; // 无法创建目录，退出
            }


            // 2. 获取所有科室的名称和链接
            // 我们先把所有链接信息读入 Map，避免在遍历时点击链接导致 "Stale Element" 错误
            Map<String, String> departmentLinks = new HashMap<>();
            Locator links = page.locator(DEPT_LIST_SELECTOR);

            int count = links.count();
            System.out.println("发现 " + count + " 个科室链接。");

            for (int i = 0; i < count; i++) {
                Locator link = links.nth(i);
                String deptName = link.innerText().trim();
                String href = link.getAttribute("href");

                // 过滤掉非科室页面（比如当前页 "科室介绍"）
                if (href != null && !href.isEmpty() && !deptName.equals("科室介绍")) {
                    String fullUrl = BASE_URL + href;
                    departmentLinks.put(deptName, fullUrl);
                }
            }

            System.out.println("将抓取以下 " + departmentLinks.size() + " 个科室的详细信息...");

            // 3. 遍历 Map，访问每个科室页面并抓取内容
            for (Map.Entry<String, String> entry : departmentLinks.entrySet()) {
                String deptName = entry.getKey();
                String deptUrl = entry.getValue();

                try {
                    System.out.println("--- 开始抓取: " + deptName + " (" + deptUrl + ") ---");

                    // 导航到科室详情页
                    page.navigate(deptUrl);

                    // 等待内容区域加载
                    page.waitForSelector(CONTENT_SELECTOR);

                    // 定位并提取介绍内容
                    Locator contentArea = page.locator(CONTENT_SELECTOR);
                    String description = contentArea.innerText();

                    // 4. 清理文件名并保存到 TXT
                    // 替换文件名中不合法字符
                    String cleanFileName = deptName.replaceAll("[/\\\\?%*:|\"<>\\s]", "_") + ".txt";
                    Path filePath = outputDir.resolve(cleanFileName);

                    Files.write(filePath, description.getBytes(StandardCharsets.UTF_8));

                    System.out.println("✔ 已保存: " + filePath);

                } catch (Exception e) {
                    System.err.println("✘ 抓取失败: " + deptName + " | 错误: " + e.getMessage());
                }
            }

            // 5. 关闭浏览器
            browser.close();
            System.out.println("抓取完成。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
