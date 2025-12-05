package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class DataController {

    @Autowired
    private CompiledGraph compiledGraph;

    @GetMapping("/stream")
    public Map<String, Object> stream(String input) {

        OverAllState state = compiledGraph.invoke(Map.of("input", input)).orElseThrow();
        return state.data();
    }
}
