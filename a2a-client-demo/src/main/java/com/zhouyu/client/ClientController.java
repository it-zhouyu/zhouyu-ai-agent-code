package com.zhouyu.client;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
@Log4j2
public class ClientController {

    @Autowired
    private A2aRemoteAgent a2aRemoteAgent;

    @Autowired
    private SequentialAgent sequentialAgent;

    @GetMapping("/hello")
    public Map<String, Object> hello(String input) {
        try {
//            Optional<OverAllState> overAllState = a2aRemoteAgent.invoke(input);
            Optional<OverAllState> overAllState = sequentialAgent.invoke(input);
            return overAllState.orElseThrow().data();
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

}
