package com.zhouyu.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Log4j2
public class ZhouyuModelInterceptor extends ModelInterceptor {

    @Override
    public String getName() {
        return "zhouyuModelInterceptor";
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {

        log.info("zhouyuModelInterceptor请求: {}", request);

        // 检查输入
        if (check(request.getMessages())) {
            return ModelResponse.of(new AssistantMessage("输入有不适当的内容"));
        }



        // 执行调用
        ModelResponse response = handler.call(request);


        log.info("zhouyuModelInterceptor响应: {}", response);

        if (check(response.getChatResponse().getResult().getOutput())) {
            return ModelResponse.of(new AssistantMessage("输出有不适当的内容"));
        }
        return response;
    }

    private boolean check(Message message) {
        if (message.getText().contains("死亡")) {
            return true;
        }
        return false;
    }

    private boolean check(List<Message> messages) {
        for (Message message : messages) {
            if (message.getText().contains("死亡")) {
                return true;
            }
        }
        return false;
    }


}
