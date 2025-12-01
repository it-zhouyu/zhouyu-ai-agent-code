package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
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
public class GraphController {

    @Autowired
    private CompiledGraph simpleStateGraph;

    @GetMapping("/simple")
    public Map<String, Object> simple(String subject) {
        Optional<OverAllState> overAllState = simpleStateGraph.invoke(Map.of("subject", subject));
        OverAllState state = overAllState.orElseThrow();
        return state.data();
    }
}
