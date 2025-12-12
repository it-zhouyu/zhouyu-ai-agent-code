package com.zhouyu;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/imageGeneration")
    public String imageGeneration() {
//        ImagePrompt imagePrompt = new ImagePrompt("生成一辆汽车", OpenAiImageOptions.builder()
////                                                                                .model("cogView-4")
//                                                                                .model("qwen-image")
//                                                                                .height(1024)
//                                                                                .width(1024).build());
//        ImageResponse imageResponse = imageModel.call(imagePrompt);
//
//        return imageResponse.getResult().getOutput().getUrl();


        ImagePrompt imagePrompt = new ImagePrompt("生成一辆汽车", DashScopeImageOptions.builder()
                .withModel("qwen-image")
                .withHeight(1024)
                .withWidth(1024)
                .build());

        ImageResponse imageResponse = dashScopeImageModel.call(imagePrompt);
        return imageResponse.getResult().getOutput().getUrl();
    }

    @GetMapping("/imageChat")
    public String imageChat() {
        ClassPathResource imageResource = new ClassPathResource("/multimodal.test.png");

        UserMessage userMessage = UserMessage.builder()
                .text("图片里面有什么") // content
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource)) // media
                .build();

//        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
////                .model("glm-4.6v")
//                .model("qwen-vl-plus")
//                .build();
//
//        ChatResponse response = chatModel.call(new Prompt(userMessage, openAiChatOptions));
//        return response.getResult().getOutput().getText();

        DashScopeChatOptions openAiChatOptions = DashScopeChatOptions.builder()
                .withModel("qwen-vl-plus")
                .build();

        ChatResponse response = dashScopeChatModel.call(new Prompt(userMessage, openAiChatOptions));
        return response.getResult().getOutput().getText();
    }
}
