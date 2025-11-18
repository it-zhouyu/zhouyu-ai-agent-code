package com.zhouyu;

import com.alibaba.fastjson2.JSONObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class OpenAITest {

    private static final String REACT_PROMPT_TEMPLATE = """
            你是一个强大的 AI 助手，通过思考和使用工具来解决用户的问题。
            
            你的任务是尽你所能回答以下问题。你可以使用以下工具：
            {tools}
           
            Question: {input}
            """;

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        HashMap<String, Method> tools = new HashMap<>();
        tools.put("writeFile", AgentTools.class.getMethod("writeFile", String.class));

        OpenAIClient apiClient = OpenAIOkHttpClient.builder()
                .apiKey(ModelConfig.API_KEY)
                .baseUrl(ModelConfig.BASE_URL)
                .build();



        String promptString = "将1到10中间的所有整数写到文件中";
        String prompt = REACT_PROMPT_TEMPLATE.replace("{tools}", ToolUtil.getToolDescription(AgentTools.class));
        prompt = prompt.replace("{input}", promptString);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(ModelConfig.LLM_NAME)
                .build();

        ChatCompletion chatCompletion = apiClient.chat().completions().create(params);
        String rawLlmOutput = chatCompletion.choices().get(0).message().content().get();
        System.out.println(rawLlmOutput);


        // 执行工具
        rawLlmOutput = rawLlmOutput.replace("```tool_code", "");
        rawLlmOutput = rawLlmOutput.replace("```", "");

        JSONObject jsonObject = JSONObject.parseObject(rawLlmOutput);
        String toolName = jsonObject.getString("toolName");
        String toolParams = jsonObject.getString("params");

        Method toolMethod = tools.get(toolName);
        Object result = toolMethod.invoke(new AgentTools(), toolParams);
        System.out.println(result);

    }
}
