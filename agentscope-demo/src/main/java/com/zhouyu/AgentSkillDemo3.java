package com.zhouyu;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.skill.util.JarSkillRepositoryAdapter;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.coding.ShellCommandTool;

import java.nio.file.Path;
import java.util.Set;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class AgentSkillDemo3 {

    public static void main(String[] args) {


        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .build();

        Toolkit toolkit = new Toolkit();
        SkillBox skillBox = new SkillBox(toolkit);

        // 如果要执行python代码，需要以下配置
        ShellCommandTool customShell = new ShellCommandTool(Set.of("python", "node", "npm"));
        skillBox.codeExecution()
                .withShell(customShell)
                .withRead()
                .withWrite()
                .enable();

        // 创建并注册一个技能
        AgentSkill explainCodeSkill = null;
        try (JarSkillRepositoryAdapter adapter = new JarSkillRepositoryAdapter("skills")) {
            explainCodeSkill = adapter.getSkill("explain-code");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        skillBox.registerSkill(explainCodeSkill);

        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("解释一下以下代码: System.out.println(\"hello world\");")
                .build()).block();

        System.out.println(response.getTextContent());
    }


}
