package com.zhouyu;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class AlarmRequest {

//    @ToolParam(description = "用中文中的年月日的格式，比如2025年3月31日")
//    @ToolParam(description = "时间")
    @JsonPropertyDescription("用中文中的年月日的格式，比如2025年3月31日")
    private String time;

    @ToolParam(description = "地址", required = false)
    private String address;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
