package com.zhouyu.agent;

import com.zhouyu.model.Memory;
import com.zhouyu.model.Message;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public abstract class BaseAgent {

    protected final Memory memory;
    private final int maxStep;
    protected String systemPrompt;

    public BaseAgent(String systemPrompt) {
        this.memory = new Memory();
        this.maxStep = 10;
        this.systemPrompt = systemPrompt;

    }

    public String run(String prompt) {

        memory.addMessage(Message.systemMessage(systemPrompt));
        memory.addMessage(Message.userMessage(prompt));

        int currentStep = 0;
        StringBuilder allStepResult = new StringBuilder();

        // 达到最大步数就退出
        while (currentStep < maxStep) {

            StepResult stepResult = step();
            allStepResult.append(stepResult.output).append("/n");

            // 不继续了就退出
            if (!stepResult.isShouldContinue()) {
                break;
            }

            currentStep++;
        }

        return allStepResult.toString();
    }

    protected abstract StepResult step();

    @Data
    @Builder
    public static class StepResult {
        private final String output;  // 当前step的输出结果
        private final boolean shouldContinue; // 当前step执行完后是否还需要继续
    }
}
