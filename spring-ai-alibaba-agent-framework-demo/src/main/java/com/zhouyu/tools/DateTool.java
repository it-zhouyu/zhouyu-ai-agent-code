package com.zhouyu.tools;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Log4j2
public class DateTool implements BiFunction<DateTool.DateRequest, ToolContext, String> {
    @Override
    public String apply(DateRequest dateRequest, ToolContext toolContext) {
        log.info("获取{}的时间", dateRequest.cityName);
        return LocalDateTime.now().toString();
    }

    public record DateRequest(@ToolParam(description = "指定城市名称") String cityName) {}
}
