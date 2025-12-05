package com.zhouyu.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class PromptUtil {
    public static String getPrompt(String promptName) throws IOException {
        ClassPathResource resource = new ClassPathResource("prompts/" + promptName + ".md");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
