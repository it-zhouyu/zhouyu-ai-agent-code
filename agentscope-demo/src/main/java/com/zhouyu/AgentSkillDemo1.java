package com.zhouyu;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.util.SkillUtil;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.coding.ShellCommandTool;

import java.util.Map;
import java.util.Set;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class AgentSkillDemo1 {

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
        AgentSkill dataSkill = createExplainCodeSkill();
        skillBox.registerSkill(dataSkill);


        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("你是一个有帮助的 AI 助手。")
                .model(chatModel)
                .skillBox(skillBox)
                .toolkit(toolkit)
                .build();

        Msg response = agent.call(Msg.builder()
                .textContent("解释一下以下代码: System.out.println(\"hello world\");")
                .build()).block();

        System.out.println(response.getTextContent());
    }


    private static AgentSkill createExplainCodeSkill() {
        String skillMd =
                """
                ---
                name: explain-code
                description: 当需要解释代码时，使用这个技能
                ---
                
                # Explain Code Skill
                
                ## Overview
                你是一个专业的代码解释助手，解释代码时，请始终包含以下内容：
                1. 将代码与日常生活中的事物进行比较
                2. 分步解释发生了什么
                
                ## 其他需求和规则
                1. 如果是Java代码则读取references/java.md来获取其他详细规则和需求
                2. 如果是Python代码则读取references/python.md来获取其他详细规则和需求
                """;

        Map<String, String> resources =
                Map.of(
                        "references/java.md",
                        """
                                ## Java代码详细规则和需求
                                1. 用Java程序员的习惯来解释代码
                                2. 使用ASCII艺术展示流程、结构或关系
                                3. 将代码解释结果，通过执行[scripts/pdf.py]脚本转为为pdf文件，用法为`python pdf.py 代码解释结果`
                            """,
                        "references/python.md",
                        """
                                ## Python代码详细规则和需求
                                用Python程序员的习惯来解释代码，用最简单的方式来解释代码。
                            """,
                        "scripts/pdf.py",
                        """
                                    #!/usr/bin/env python
                                    # -*- coding: utf-8 -*-
                                    ""\"
                                    PDF 生成工具
                                    使用方法: python pdf.py "pdf内容"
                                    ""\"
                                
                                    import sys
                                    from reportlab.pdfgen import canvas
                                    from reportlab.lib.pagesizes import A4
                                    from reportlab.pdfbase import pdfmetrics
                                    from reportlab.pdfbase.ttfonts import TTFont
                                
                                    def create_pdf(text_content, output_filename="output.pdf"):
                                        ""\"
                                        创建 PDF 文件
                                
                                        Args:
                                            text_content (str): 要写入 PDF 的文本内容
                                            output_filename (str): 输出文件名
                                        ""\"
                                        c = canvas.Canvas(output_filename, pagesize=A4)
                                        width, height = A4
                                
                                        # 设置中文字体
                                        try:
                                            pdfmetrics.registerFont(TTFont('SimSun', 'simsun.ttc'))
                                            c.setFont("SimSun", 12)
                                        except:
                                            c.setFont("Helvetica", 12)  # fallback 字体
                                
                                        # 处理文本换行
                                        lines = text_content.split('\\n')
                                
                                        y_position = height - 40  # 从页面顶部开始
                                        line_height = 15
                                
                                        for line in lines:
                                            if y_position < 40:  # 如果当前页空间不足，创建新页
                                                c.showPage()
                                                y_position = height - 40
                                
                                            c.drawString(40, y_position, line)
                                            y_position -= line_height
                                
                                        c.save()
                                        print(f"PDF 文件已生成: {output_filename}")
                                
                                    def main():
                                        if len(sys.argv) < 2:
                                            print("使用方法: python pdf.py \\"pdf内容\\"")
                                            sys.exit(1)
                                
                                        text_content = sys.argv[1]
                                        create_pdf(text_content)
                                
                                    if __name__ == "__main__":
                                        main()
                            """
                );

        return SkillUtil.createFrom(skillMd, resources);
    }
}
