package com.zhouyu;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
public class EmailTool {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${toEmail}")
    private String toEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Tool(description = "发送邮件")
    public void sendEmail(@ToolParam(description = "HTML格式的邮件内容") String reportContent) {
        System.out.println("邮件发送任务开始: " + toEmail);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "周报");
            helper.setText(reportContent, true);
            mailSender.send(mimeMessage);
            System.out.println("邮件发送任务执行成功: " + toEmail);
        } catch (MessagingException e) {
            System.out.println("邮件发送任务失败: " + e);
            throw new RuntimeException(e);
        }
    }
}
