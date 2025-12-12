package com.zhouyu;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class DashscopeService {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    public String imageGenerate(String prompt, String model, Map<String, Object> parameters) throws NoApiKeyException, UploadFileException {

        MultiModalConversation conv = new MultiModalConversation();

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(List.of(Map.of("text", prompt)))
                .build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(List.of(userMessage))
                .parameters(parameters)
                .build();

        MultiModalConversationResult result = conv.call(param);
        return JsonUtils.toJson(result);
    }

    public String imageChat(String imageUrl, String localPath, String prompt, String model) throws NoApiKeyException, UploadFileException, IOException {
        String encodeImageToBase64 = encodeImageToBase64(localPath);

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(List.of(
//                        Map.of("image", imageUrl != null ? imageUrl : "file://"+localPath),
                        Map.of("image", imageUrl != null ? imageUrl : "data:image/png;base64,"+encodeImageToBase64),
                        Map.of("text", prompt))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(List.of(userMessage))
                .build();

        MultiModalConversationResult result = conv.call(param);
        return JsonUtils.toJson(result);
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}