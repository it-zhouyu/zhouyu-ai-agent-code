package com.zhouyu;

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
public class ReActAgent {


//    private static final String REACT_PROMPT_TEMPLATE = """
//            你是一个强大的 AI 助手，通过思考和使用工具来解决用户的问题。
//
//            你的任务是尽你所能回答以下问题。你可以使用以下工具：
//
//            {tools}
//
//            请严格遵循以下规则和格式：
//            1. 你的行动必须基于一个清晰的“Thought”过程。
//            2. 你必须按顺序使用 "Thought:", "Action:", "Action Input:"。
//            3. 在每次回复中，你只能生成 **一个** Thought/Action/Action Input 组合。
//            4. **绝对不要** 自己编造 "Observation:"。系统会在你执行动作后，将真实的结果作为 Observation 提供给你。
//            5. 当你拥有足够的信息来直接回答用户的问题时，请使用 "Final Answer:" 来输出最终答案。
//            6. 在每次回复中，"Thought:", "Action:", "Action Input:"和"Final Answer:"不能同时出现。
//
//            下面是你的思考和行动格式：
//            Thought: 我需要做什么来解决问题？下一步是什么？
//            Action: 我应该使用哪个工具？必须是 [{tool_names}] 中的一个。
//            Action Input: 我应该给这个工具提供什么输入？这必须是一个 JSON 对象。
//
//            --- 开始 ---
//
//            Question: {input}
//            {agent_scratchpad}
//            """;

    private static final String REACT_PROMPT_TEMPLATE = """
            
            ## 角色定义
            你是一个强大的 AI 助手，通过思考和使用工具来解决用户的问题。
            
            ## 任务
            你的任务是尽你所能回答以下问题。你可以使用以下工具：
            {tools}
            
            ## 规则
            - Action中只需要返回toolName
            - 千万不要自己生成**Observation: **相关的内容
            
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

    private OpenAIClient apiClient;

    public ReActAgent(OpenAIClient apiClient) {
        this.apiClient = apiClient;
    }

    public String run(String input) throws NoSuchMethodException {

        HashMap<String, Method> tools = new HashMap<>();
        tools.put("writeFile", AgentTools.class.getMethod("writeFile", String.class));


        // 记忆
        StringBuilder history = new StringBuilder();

        int i = 0;
        while (i < 10) {
            try {
                String prompt = buildPrompt(input, history.toString());

                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .addUserMessage(prompt)
                        .model(ModelConfig.LLM_NAME)
                        .build();

                ChatCompletion chatCompletion = apiClient.chat().completions().create(params);
                String rawLlmOutput = chatCompletion.choices().get(0).message().content().get();
                System.out.println("大模型原始输出：" + rawLlmOutput);

                ParsedOutput parsedOutput = parseLlmOutput(rawLlmOutput);

                if (parsedOutput.type.equals("final_answer")) {
                    return parsedOutput.answer;
                }

                String observation = executeTool(parsedOutput, tools);
                System.out.println("工具执行结果：" + observation);

                history.append("Reason: ").append(parsedOutput.reason).append("\n")
                        .append("Action: ").append(parsedOutput.action).append("\n")
                        .append("ActionInput: ").append(parsedOutput.actionInputStr).append("\n")
                        .append("Observation: ").append(observation).append("\n");
            } catch (Exception e) {
                i++;
            }
        }

        return "达到了循环最大次数";
    }

    private static String executeTool(ParsedOutput parsedOutput, HashMap<String, Method> tools) throws IllegalAccessException, InvocationTargetException {
        String toolName = parsedOutput.action;
        String toolParams = parsedOutput.actionInputStr;
        Method toolMethod = tools.get(toolName);
        Object observation = toolMethod.invoke(new AgentTools(), toolParams);
        return String.valueOf(observation);
    }

    private String buildPrompt(String input, String history) {
        String prompt = REACT_PROMPT_TEMPLATE.replace("{tools}", ToolUtil.getToolDescription(AgentTools.class));
        prompt = prompt.replace("{input}", input);
        prompt = prompt.replace("{history}", history);

        return prompt;
    }

    private ParsedOutput parseLlmOutput(String llmOutput) {
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


    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        OpenAIClient apiClient = OpenAIOkHttpClient.builder()
                .apiKey(ModelConfig.API_KEY)
                .baseUrl(ModelConfig.BASE_URL)
                .build();

        ReActAgent reActAgent = new ReActAgent(apiClient);
//        String promptString = "将1到10中间的所有整数写到文件中";
        String promptString = "请帮我用HTML、CSS、JS创建一个简单的贪吃蛇游戏，分成三个文件，分别是snake.html、snake.css、snake.js";

        String result = reActAgent.run(promptString);

        System.out.println(result);
    }
}
