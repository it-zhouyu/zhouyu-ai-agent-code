package com.zhouyu;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class OpenAITest {

    public static void main(String[] args) {

        OpenAIClient apiClient = OpenAIOkHttpClient.builder()
                .apiKey(ModelConfig.API_KEY)
                .baseUrl(ModelConfig.BASE_URL)
                .build();

        String promptString = "你是谁？";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(promptString)
                .model(ModelConfig.LLM_NAME)
                .build();

        ChatCompletion chatCompletion = apiClient.chat().completions().create(params);
        String rawLlmOutput = chatCompletion.choices().get(0).message().content().get();

        System.out.println(rawLlmOutput);
    }
}
