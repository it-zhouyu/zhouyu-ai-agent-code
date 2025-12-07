package com.zhouyu.tools;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class StoreTool implements BiFunction<StoreTool.WeatherRequest, ToolContext, String> {
    @Override
    public String apply(StoreTool.WeatherRequest weatherRequest, ToolContext toolContext) {
        RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get("_AGENT_CONFIG_");
        Store store = runnableConfig.store();
        Optional<StoreItem> storeItem = store.getItem(List.of("user_info"), "user_002");
        storeItem.ifPresent(item -> {
            System.out.println("item: " + item);
        });

        OverAllState overAllState = (OverAllState) toolContext.getContext().get("_AGENT_STATE_");
        Optional<List> messages = overAllState.value("messages", List.class);
        System.out.println(messages.get());

        return "天晴";
    }

    public record WeatherRequest(@ToolParam(description = "指定城市名称") String cityName) {
    }
}
