package com.zhouyu.tools;

import com.zhouyu.context.UserContext;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolEmitter;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.ToolSuspendException;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class WeatherService {
    @Tool(description = "获取指定城市的天气")
    public String getWeather(@ToolParam(name = "city", description = "城市名称") String city) {
        return city + " 的天气：晴天，25°C";
    }

    @Tool(description = "生成数据")
    public ToolResultBlock generate(@ToolParam(name = "count") int count, ToolEmitter emitter) {  // 自动注入，无需 @ToolParam
        for (int i = 0; i < count; i++) {
            emitter.emit(ToolResultBlock.text("进度 " + i));
        }
        return ToolResultBlock.text("完成");
    }

    @Tool(description = "查询用户数据")
    public String query(@ToolParam(name = "sql") String sql, UserContext ctx) {  // 自动注入，无需 @ToolParam
        return "用户 " + ctx.getUserId() + " 的数据";
    }
}
