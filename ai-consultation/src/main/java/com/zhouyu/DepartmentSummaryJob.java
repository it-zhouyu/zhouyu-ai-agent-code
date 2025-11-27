package com.zhouyu;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Component
@Log4j2
public class DepartmentSummaryJob {

    @Autowired
    private ChatClient.Builder builder;

    private static final String DEPARTMENT_INFO_DIR = "ai-consultation/department_info";
    private static final String DEPARTMENT_SUMMARY_INFO_DIR = "ai-consultation/department_summary_info";

    // few-shot 少样本提示 one-shot  zero-shot
    private static final String SYSTEM_PROMPT = """
            ## 角色
            你是一个AI问诊助手
            
            ## 任务
            请根据提供的科室介绍内容，总结出该科室主要诊治的疾病有哪些，只需要返回最精炼的总结即可
            
            ## 输出格式请参考以下格式
            湘雅三医院感染科主要诊治以下疾病：
            1. **各类肝病**：包括病毒性肝炎（甲、乙、丙、丁、戊型等）、酒精性肝病、药物性肝损伤、脂肪肝、自身免疫性肝病、遗传代谢性肝病、肝硬化、肝功能异常及肝衰竭等。
            2. **全身及各系统感染性疾病**：
               - **病毒感染**：如流感、新冠、肾综合征出血热、乙脑、水痘、麻疹等；
               - **细菌感染**：如败血症、感染性休克、多重耐药菌感染、伤寒、细菌性痢疾、肝/肾脓肿、结核、布鲁菌病等；
               - **寄生虫感染**：如血吸虫病、肺吸虫病、钩虫病、疟疾、阿米巴病、弓形虫病等；
               - **真菌感染**：如隐球菌病、念珠菌病、曲霉菌病、肺孢子菌病等；
               - **其他病原体感染**：如衣原体、立克次体感染等。
            3. **发热、腹泻、休克、颅内感染、多发脓肿、深部真菌感染等疑难重症感染的病因排查与治疗**。
            4. **特殊人群感染**：如器官移植后、肿瘤放化疗后、免疫功能低下或中性粒细胞减少患者的感染。
            5. **医院获得性感染**：如导管相关感染、手术部位感染、植入器械相关感染等。
            6. **法定传染病**：如霍乱、伤寒、痢疾、麻疹、百日咳、破伤风、钩体病、肾综合征出血热等。
            """;

    @PostConstruct
    public void init() {
        // 总结科室信息-->疾病、症状--->向量化--->向量数据库

        Path departmentSummaryInfoDir = Paths.get(DEPARTMENT_SUMMARY_INFO_DIR);

//        if (Files.exists(departmentSummaryInfoDir)) {
//            System.out.println("输出目录已存在: " + departmentSummaryInfoDir.toAbsolutePath());
//            return;
//        }
//
//        try {
//            Files.createDirectories(departmentSummaryInfoDir);
//            System.out.println("输出目录已创建: " + departmentSummaryInfoDir.toAbsolutePath());
//        } catch (IOException e) {
//            System.err.println("创建目录失败: " + e.getMessage());
//            throw new RuntimeException(e);
//        }

        ChatClient chatClient = builder.build();

        Path documentDir = Paths.get(DEPARTMENT_INFO_DIR);
        try (Stream<Path> documentList = Files.list(documentDir)) {
            documentList.forEach(path -> {

                File file = path.toFile();
                String fileName = file.getName();

                Path summaryFilePath = departmentSummaryInfoDir.resolve(fileName);

                // 如果文件已存在，则跳过
                if (Files.exists(summaryFilePath)) {
                    return;
                }
                try {
                    String fileContent = new String(Files.readAllBytes(path));

                    log.info("正在处理：" + fileName);

                    String summaryContent = chatClient.prompt()
                            .system(SYSTEM_PROMPT)
                            .user("科室介绍：" + fileContent)
                            .call()
                            .content();

                    Files.write(summaryFilePath, summaryContent.getBytes(StandardCharsets.UTF_8));

                } catch (Exception e) {
//                    // 有可能会出现处理不了的文件，比如：
//                    // 处理心血管内科.txt文件失败: HTTP 400 - {"request_id":"d7672fe9-d99d-401a-8319-6c625e68fe39","code":"DataInspectionFailed","message":"Input data may contain inappropriate content."}
                    log.error("处理" + fileName + "文件失败: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
