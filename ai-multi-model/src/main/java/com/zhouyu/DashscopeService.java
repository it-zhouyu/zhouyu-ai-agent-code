package com.zhouyu;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
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
                        Map.of("image", imageUrl != null ? imageUrl : "data:image/png;base64," + encodeImageToBase64),
                        Map.of("text", prompt))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(List.of(userMessage))
                .build();

        MultiModalConversationResult result = conv.call(param);
        return JsonUtils.toJson(result);
    }

    public Flowable<MultiModalConversationResult> videoChat(String videoUrl, String localPath, String prompt, String model) throws NoApiKeyException, UploadFileException, IOException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(List.of(
                        Map.of("video", videoUrl != null ? videoUrl : "file://" + localPath, "fps", 2),
//                        Map.of("video", videoUrl != null ? videoUrl : "data:video/mp4;base64," + encodeVideoToBase64(localPath)),
                        Map.of("text", prompt))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(List.of(userMessage))
                .build();

//        MultiModalConversationResult result = conv.call(param);
//        return JsonUtils.toJson(result);
        return conv.streamCall(param);
    }

    public String audioChat(String videoUrl, String localPath, String model) throws NoApiKeyException, UploadFileException, IOException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(List.of(Map.of("audio", videoUrl != null ? videoUrl : "file://" + localPath))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(List.of(userMessage))
                .build();

        MultiModalConversationResult result = conv.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
//        return conv.streamCall(param);
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String encodeVideoToBase64(String videoPath) throws IOException {
        Path path = Paths.get(videoPath);
        byte[] videoBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(videoBytes);
    }

    public Path extractAudio(Path videoPath) {
        try {
            String videoFilePath = videoPath.toString();
            String audioFilePath = videoFilePath.substring(0, videoFilePath.lastIndexOf('.')) + ".mp3";

            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoFilePath, "-vn", audioFilePath);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg execution failed with exit code: " + exitCode);
            }

            return Path.of(audioFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting audio from video", e);
        }

    }

}