package com.zhouyu;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.reactivex.Flowable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class MultiModelController {


//    @Autowired
//    private ImageModel imageModel;
//
//    @Autowired
//    private ChatModel chatModel;

    @Autowired
    private DashScopeImageModel dashScopeImageModel;

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private DashscopeService dashscopeService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @GetMapping("/imageGeneration")
    public String imageGeneration() {
        // Spring AI调用智普生成图片
//        ImagePrompt imagePrompt = new ImagePrompt("生成一辆汽车", OpenAiImageOptions.builder()
////                                                                                .model("cogView-4")
//                                                                                .model("qwen-image")
//                                                                                .height(1024)
//                                                                                .width(1024).build());
//        ImageResponse imageResponse = imageModel.call(imagePrompt);
//        return imageResponse.getResult().getOutput().getUrl();

        // Spring AI Alibaba调用qwen-image生成图片，会报错
//        ImagePrompt imagePrompt = new ImagePrompt("生成一辆汽车", DashScopeImageOptions.builder()
//                .withModel("qwen-image")
//                .withHeight(1024)
//                .withWidth(1024)
//                .build());
//        ImageResponse imageResponse = dashScopeImageModel.call(imagePrompt);
//        return imageResponse.getResult().getOutput().getUrl();

        // Dashscope调用qwen-image生成图片
        String result = null;
        try {
            result = dashscopeService.imageGenerate("一辆汽车", "qwen-image", Map.of("size", "1328*1328"));
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @GetMapping("/imageChat")
    public String imageChat() {
        ClassPathResource imageResource = new ClassPathResource("/multimodal.test.png");

        UserMessage userMessage = UserMessage.builder()
                .text("图片里面有什么") // content
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource)) // media
                .build();

        // Spring AI调用智普图片理解
//        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
////                .model("glm-4.6v")
//                .model("qwen-vl-plus")
//                .build();
//
//        ChatResponse response = chatModel.call(new Prompt(userMessage, openAiChatOptions));
//        return response.getResult().getOutput().getText();

        // Spring AI Alibaba调用qwen-vl-plus图片理解，会报错
//        DashScopeChatOptions openAiChatOptions = DashScopeChatOptions.builder()
//                .withModel("qwen-vl-plus")
//                .build();
//        ChatResponse response = dashScopeChatModel.call(new Prompt(userMessage, openAiChatOptions));
//        return response.getResult().getOutput().getText();

        String result = null;
        try {
//            result = dashscopeService.imageChat("https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg","图片里有什么？", "qwen-vl-plus");
//            result = dashscopeService.imageChat(null,"/Users/dadudu/idea/java/vip/zhouyu-ai-agent-vip/ai-multi-model/src/main/resources/multimodal.test.png", "图片里有什么？", "qwen-vl-plus");
            result = dashscopeService.imageChat(null,"/Users/dadudu/idea/java/vip/zhouyu-ai-agent-vip/ai-multi-model/src/main/resources/multimodal.test.png", "图片里有什么？", "qwen-vl-plus");
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // 异步调用
    // https://help.aliyun.com/zh/model-studio/text-to-video-api-reference?spm=a2c4g.11186623.help-menu-2400256.d_2_3_3.4b0a11fc5PS841&scm=20140722.H_2865250._.OR_help-T_cn~zh-V_1#ecd1180f3c026

    @GetMapping(path = "/videoChat", produces = "text/html;charset=UTF-8")
    public Flowable<String> videoChat() {
        try {
            Flowable<MultiModalConversationResult> result = dashscopeService.videoChat(null,"/Users/dadudu/idea/java/vip/zhouyu-ai-agent-vip/ai-multi-model/src/main/resources/ToolCallLimitHook.mp4", "总结一下视频", "qwen3-vl-plus");
//            Flowable<MultiModalConversationResult> result = dashscopeService.videoChat("https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241115/cqqkru/1.mp4",null, "总结一下视频", "qwen3-vl-plus");

            Flowable<String> stringFlowable = result.map(multiModalConversationResult ->
                    multiModalConversationResult.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString()
            );
            return stringFlowable;
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping(path = "/audioChat", produces = "text/html;charset=UTF-8")
    public String audioChat() {
        try {

            String filePath = "/Users/dadudu/idea/java/vip/zhouyu-ai-agent-vip/ai-multi-model/src/main/resources/ToolCallLimitHook.mp4";
            Path audioPath = dashscopeService.extractAudio(Path.of(filePath));
            String result = dashscopeService.audioChat(null,audioPath.toAbsolutePath().toString(), "qwen3-asr-flash");

            String promptString = """
                你是一个专业的视频内容分析师。
                请根据以下由视频转录的文本，提供一份简洁、精确、易于阅读的内容摘要。
                你的摘要应该包含视频的核心观点和关键信息。

                请使用中文进行总结。

                转录文本如下:
                ---
                {transcript}
                ---
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptString);
            String prompt = promptTemplate.render(Map.of("transcript", result));

            ChatClient chatClient = chatClientBuilder.build();
            return chatClient.prompt(prompt).call().content();
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
