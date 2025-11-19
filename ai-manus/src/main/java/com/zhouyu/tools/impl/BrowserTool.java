package com.zhouyu.tools.impl;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;
import com.zhouyu.tools.BaseTool;
import com.zhouyu.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class BrowserTool extends BaseTool {
    private static final Logger logger = LoggerFactory.getLogger(BrowserTool.class);
    private Browser browser;
    private BrowserContext context;
    private Page currentPage;

    public BrowserTool() {
        super("browser", "Navigate web pages, take screenshots, interact with elements, and extract content");
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return buildSchema(
                Map.of(
                        "action", enumParam("浏览器操作动作类型", List.of("navigate", "click", "type", "screenshot", "get_content", "scroll", "wait")),
                        "url", stringParam("导航至的URL（导航所需）"),
                        "selector", stringParam("元素的CSS选择器（点击、输入时必填）"),
                        "text", stringParam("待输入文本（键入操作必需）"),
                        "timeout", intParam("超时时间（毫秒）（默认值：30000）"),
                        "wait_for", stringParam("在继续之前等待元素/条件"),
                        "scroll_direction", enumParam("滚动方向", List.of("up", "down", "left", "right")),
                        "scroll_amount", intParam("滚动像素数（默认值：500）")
                ),
                List.of("action")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        try {
            String action = getString(parameters, "action");
            if (action == null) {
                return ToolResult.error("Action is required");
            }

            // Initialize browser if not already done
            if (browser == null) {
                initializeBrowser();
            }

            logger.info("正在执行的浏览器动作: " + action);

            switch (action.toLowerCase()) {
                case "navigate":
                    return handleNavigate(parameters);
                case "click":
                    return handleClick(parameters);
                case "type":
                    return handleType(parameters);
                case "screenshot":
                    return handleScreenshot(parameters);
                case "get_content":
                    return handleGetContent(parameters);
                case "scroll":
                    return handleScroll(parameters);
                case "wait":
                    return handleWait(parameters);
                default:
                    return ToolResult.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            logger.error("浏览器操作失败", e);
            return ToolResult.error("浏览器操作失败: " + e.getMessage());
        }
    }

    private void initializeBrowser() {
        try {
            Playwright playwright = Playwright.create();

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setTimeout(30000);

            browser = playwright.chromium().launch(launchOptions);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            context = browser.newContext(contextOptions);
            currentPage = context.newPage();

            logger.info("Browser initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize browser", e);
            throw new RuntimeException("Browser initialization failed", e);
        }
    }

    private ToolResult handleNavigate(Map<String, Object> parameters) {
        String url = getString(parameters, "url");
        if (url == null) {
            return ToolResult.error("URL is required for navigate action");
        }

        try {
            int timeout = getInteger(parameters, "timeout", 30000);

            Response response = currentPage.navigate(url, new Page.NavigateOptions().setTimeout(timeout));
            currentPage.waitForLoadState(LoadState.NETWORKIDLE);

            String title = currentPage.title();
            String finalUrl = currentPage.url();

            return ToolResult.success(String.format("Navigated to: %s\nTitle: %s\nFinal URL: %s\nStatus: %d",
                    url, title, finalUrl, response.status()));
        } catch (Exception e) {
            return ToolResult.error("Navigation failed: " + e.getMessage());
        }
    }

    private ToolResult handleClick(Map<String, Object> parameters) {
        String selector = getString(parameters, "selector");
        if (selector == null) {
            return ToolResult.error("Selector is required for click action");
        }

        try {
            int timeout = getInteger(parameters, "timeout", 30000);

            Locator element = currentPage.locator(selector);
            element.click(new Locator.ClickOptions().setTimeout(timeout));

            return ToolResult.success("Clicked element: " + selector);
        } catch (Exception e) {
            return ToolResult.error("Click failed: " + e.getMessage());
        }
    }

    private ToolResult handleType(Map<String, Object> parameters) {
        String selector = getString(parameters, "selector");
        String text = getString(parameters, "text");

        if (selector == null) {
            return ToolResult.error("Selector is required for type action");
        }
        if (text == null) {
            return ToolResult.error("Text is required for type action");
        }

        try {
            int timeout = getInteger(parameters, "timeout", 30000);

            Locator element = currentPage.locator(selector);
            element.clear();
            element.fill(text, new Locator.FillOptions().setTimeout(timeout));

            return ToolResult.success("Typed text into element: " + selector);
        } catch (Exception e) {
            return ToolResult.error("Type failed: " + e.getMessage());
        }
    }

    private ToolResult handleScreenshot(Map<String, Object> parameters) {
        try {
            byte[] screenshot = currentPage.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setType(ScreenshotType.PNG));

            String base64Screenshot = Base64.getEncoder().encodeToString(screenshot);

            return ToolResult.success("截图成功", base64Screenshot);
        } catch (Exception e) {
            return ToolResult.error("截图失败: " + e.getMessage());
        }
    }

    private ToolResult handleGetContent(Map<String, Object> parameters) {
        try {
            String title = currentPage.title();
            String url = currentPage.url();
            String textContent = currentPage.textContent("body");

            String result = String.format("Title: %s\nURL: %s\nContent:\n%s", title, url, textContent);
            return ToolResult.success(result);
        } catch (Exception e) {
            return ToolResult.error("Get content failed: " + e.getMessage());
        }
    }

    private ToolResult handleScroll(Map<String, Object> parameters) {
        try {
            String direction = getString(parameters, "scroll_direction", "down");
            int amount = getInteger(parameters, "scroll_amount", 500);

            switch (direction.toLowerCase()) {
                case "down":
                    currentPage.evaluate("window.scrollBy(0, " + amount + ")");
                    break;
                case "up":
                    currentPage.evaluate("window.scrollBy(0, -" + amount + ")");
                    break;
                case "left":
                    currentPage.evaluate("window.scrollBy(-" + amount + ", 0)");
                    break;
                case "right":
                    currentPage.evaluate("window.scrollBy(" + amount + ", 0)");
                    break;
                default:
                    return ToolResult.error("Invalid scroll direction: " + direction);
            }

            return ToolResult.success("Scrolled " + direction + " by " + amount + " pixels");
        } catch (Exception e) {
            return ToolResult.error("Scroll failed: " + e.getMessage());
        }
    }

    private ToolResult handleWait(Map<String, Object> parameters) {
        try {
            String waitFor = getString(parameters, "wait_for");
            int timeout = getInteger(parameters, "timeout", 30000);

            if (waitFor != null) {
                // Wait for specific element
                currentPage.waitForSelector(waitFor, new Page.WaitForSelectorOptions().setTimeout(timeout));
                return ToolResult.success("Waited for element: " + waitFor);
            } else {
                // Default wait for page load
                currentPage.waitForLoadState(LoadState.NETWORKIDLE);
                return ToolResult.success("Waited for page load");
            }
        } catch (Exception e) {
            return ToolResult.error("Wait failed: " + e.getMessage());
        }
    }

    public void cleanup() {
        try {
            if (currentPage != null) {
                currentPage.close();
            }
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
        } catch (Exception e) {
            logger.error("Error during browser cleanup", e);
        }
    }
}
