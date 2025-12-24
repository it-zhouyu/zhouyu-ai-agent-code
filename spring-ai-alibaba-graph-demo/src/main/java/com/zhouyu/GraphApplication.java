package com.zhouyu;

import com.alibaba.cloud.ai.graph.observation.edge.GraphEdgeObservationHandler;
import com.alibaba.cloud.ai.graph.observation.node.GraphNodeObservationHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@SpringBootApplication
public class GraphApplication {

//    @Bean
//    public GraphEdgeObservationHandler graphEdgeObservationHandler(MeterRegistry meterRegistry) {
//        return new GraphEdgeObservationHandler(meterRegistry);
//    }
//
//    @Bean
//    public GraphNodeObservationHandler graphNodeObservationHandler(MeterRegistry meterRegistry) {
//        return new GraphNodeObservationHandler(meterRegistry);
//    }


    public static void main(String[] args) {
        SpringApplication.run(GraphApplication.class, args);
    }
}
