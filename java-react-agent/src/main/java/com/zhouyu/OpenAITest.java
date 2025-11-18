package com.zhouyu;

import com.alibaba.fastjson2.JSONObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class OpenAITest {

    private static final String REACT_PROMPT_TEMPLATE = """
            
            ## 角色定义
            你是一个强大的 AI 助手，通过思考和使用工具来解决用户的问题。
            
            ## 任务
            你的任务是尽你所能回答以下问题。你可以使用以下工具：
            {tools}
           
            ## 规则
            - Action中只需要返回toolName
           
            ## 输出格式
            Reason: 你思考的过程
            Action: 你的下一步动作，你想要执行的工具是哪个，必须是{tools}中的一个
            ActionInput: 你要调用的工具的输入参数是什么
            ...
            FinalAnswer: 表示最终的答案，只需要最后输出就可以了
            
            
            ## 用户需求
            Question: {input}
            
            ## 历史聊天记录
            {history}
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

        ParsedOutput parsedOutput = parseLlmOutput(rawLlmOutput);
        System.out.println(parsedOutput);


        // 执行工具
        String toolName = parsedOutput.action;
        String toolParams = parsedOutput.actionInputStr;

        Method toolMethod = tools.get(toolName);
        Object observation = toolMethod.invoke(new AgentTools(), toolParams);
        System.out.println(observation);




        StringBuilder history = new StringBuilder();
        history.append("Reason: ").append(parsedOutput.reason).append("\n")
                .append("Action: ").append(parsedOutput.action).append("\n")
                .append("ActionInput: ").append(parsedOutput.actionInputStr).append("\n")
                .append("Observation: ").append(observation).append("\n");

        String prompt1 = REACT_PROMPT_TEMPLATE.replace("{tools}", ToolUtil.getToolDescription(AgentTools.class));
        prompt1 = prompt1.replace("{input}", promptString);
        prompt1 = prompt1.replace("{history}", history.toString());

        ChatCompletionCreateParams params1 = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt1)
                .model(ModelConfig.LLM_NAME)
                .build();

        ChatCompletion chatCompletion1 = apiClient.chat().completions().create(params1);
        String rawLlmOutput1 = chatCompletion1.choices().get(0).message().content().get();
        System.out.println(rawLlmOutput1);
    }

    private static ParsedOutput parseLlmOutput(String llmOutput) {
        if (llmOutput.contains("FinalAnswer: ")) {
            return new ParsedOutput("final_answer", llmOutput.split("FinalAnswer: ")[1].strip(), null, null, null, null);
        }

        Pattern actionPattern = Pattern.compile("Reason:(.*?)Action:(.*?)ActionInput:(.*)", Pattern.DOTALL);
        Matcher matcher = actionPattern.matcher(llmOutput);

        if (matcher.find()) {
            String reason = matcher.group(1).trim();
            String action = matcher.group(2).trim();
            String actionInputStr = matcher.group(3).trim();

            if (actionInputStr.startsWith("```json")) {
                actionInputStr = actionInputStr.substring(7);
            }
            if (actionInputStr.endsWith("```")) {
                actionInputStr = actionInputStr.substring(0, actionInputStr.length() - 3);
            }
            actionInputStr = actionInputStr.trim();

            return new ParsedOutput("action", null, reason, action, actionInputStr, null);
        }

        return new ParsedOutput("error", null, null, null, null, String.format("解析LLM输出失败: '%s'", llmOutput));
    }

    private record ParsedOutput(
            String type, String answer, String reason, String action, String actionInputStr, String message
    ) {
    }
}
