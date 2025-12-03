package com.zhouyu;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.streaming.GraphFlux;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import reactor.core.publisher.Flux;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

public class ParallelStreamingExample {

    /**
     * 并行节点流式输出 - 每个节点保持独立的节点 ID
     */
    public static void parallelStreamingWithNodeIdPreservation() throws GraphStateException {

        // 定义状态策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
            keyStrategyMap.put("messages", new AppendStrategy());
            keyStrategyMap.put("parallel_results", new AppendStrategy());
            return keyStrategyMap;
        };


        List<String> node1Result = new ArrayList<>();
        List<String> node2Result = new ArrayList<>();

        // 并行节点 1 - 返回 GraphFlux 流式输出
        AsyncNodeAction node1 = state -> {
            // 创建流式数据
            Flux<String> stream1 = Flux.just("节点1-块1", "节点1-块2", "节点1-块3")
                    .delayElements(Duration.ofMillis(50));

            // 定义最终结果映射函数
            Function<String, String> mapResult1 = lastChunk -> {
                node1Result.add(lastChunk);
                return lastChunk;
            };

            // 定义块结果提取函数
            Function<String, String> chunkResult1 = chunk -> "123"+chunk;

            // 创建 GraphFlux，指定节点 ID 为 "parallel_node_1"
            GraphFlux<String> graphFlux1 = GraphFlux.of(
                    "parallel_node_1", // 节点 ID
                    "stream1", // 输出键
                    stream1, // 流式数据
                    mapResult1, // 最终结果映射
                    chunkResult1 // 块结果提取
            );

            return CompletableFuture.completedFuture(Map.of("stream1", graphFlux1));
        };

        // 并行节点 2 - 返回 GraphFlux 流式输出
        AsyncNodeAction node2 = state -> {
            // 创建流式数据（延迟时间不同，模拟不同的处理速度）
            Flux<String> stream2 = Flux.just("节点2-块1", "节点2-块2", "节点2-块3")
                    .delayElements(Duration.ofMillis(75));

            // 定义最终结果映射函数
            Function<String, String> mapResult2 = lastChunk -> {
                node2Result.add(lastChunk);
                return lastChunk;
            };

            // 定义块结果提取函数
            Function<String, String> chunkResult2 = chunk -> chunk;

            // 创建 GraphFlux，指定节点 ID 为 "parallel_node_2"
            GraphFlux<String> graphFlux2 = GraphFlux.of(
                    "parallel_node_2", // 节点 ID
                    "stream2", // 输出键
                    stream2, // 流式数据
                    mapResult2, // 最终结果映射
                    chunkResult2 // 块结果提取
            );

            return CompletableFuture.completedFuture(Map.of("stream2", graphFlux2));
        };

        // 合并节点 - 接收并行节点的结果
        AsyncNodeAction mergeNode = state -> {
            System.out.println(" 合并节点接收到状态: " + state.data());
            System.out.println(" 节点1结果: " + node1Result);
            System.out.println(" 节点2结果: " + node2Result);
            return CompletableFuture.completedFuture(
                    Map.of("messages", "所有并行节点已完成，结果已合并")
            );
        };

        // 构建图：两个并行节点从 START 开始，都汇聚到 merge 节点
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("node1", node1)
                .addNode("node2", node2)
                .addNode("merge", mergeNode)
                .addEdge(START, "node1") // 并行分支 1
                .addEdge(START, "node2") // 并行分支 2
                .addEdge("node1", "merge") // 汇聚到合并节点
                .addEdge("node2", "merge") // 汇聚到合并节点
                .addEdge("merge", END);

        // 编译图
        CompiledGraph graph = stateGraph.compile(CompileConfig.builder().build());

        // 创建配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId("parallel_streaming_thread")
                .build();

        // 跟踪每个节点产生的流式输出数量
        Map<String, Integer> nodeStreamCounts = new HashMap<>();
        AtomicInteger totalChunks = new AtomicInteger(0);

        System.out.println("开始并行流式输出... ");

        // 执行流式图并处理输出
        graph.stream(Map.of("input", "test"), config)
                .doOnNext(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        // 处理流式输出
                        String nodeId = streamingOutput.node();
                        String chunk = streamingOutput.chunk();

                        // 统计每个节点的流式输出
                        nodeStreamCounts.merge(nodeId, 1, Integer::sum);
                        totalChunks.incrementAndGet();

                        // 实时打印流式内容，显示节点 ID
                        System.out.println("[流式输出] 节点: " + nodeId + ", 内容: " + chunk);
                    } else {
                        // 处理普通节点输出
                        String nodeId = output.node();
                        Map<String, Object> state = output.state().data();
                        System.out.println(" [节点完成] " + nodeId + ", 状态: " + state);
                    }
                })
                .doOnComplete(() -> {
                    System.out.println(" === 并行流式输出完成 ===");
                    System.out.println("总流式块数: " + totalChunks.get());
                    System.out.println("各节点流式输出统计: " + nodeStreamCounts);
                })
                .doOnError(error -> {
                    System.err.println("流式输出错误: " + error.getMessage());
                    error.printStackTrace();
                })
                .blockLast(); // 阻塞等待流完成
    }

    public static void main(String[] args) throws GraphStateException {
        parallelStreamingWithNodeIdPreservation();
    }
}
