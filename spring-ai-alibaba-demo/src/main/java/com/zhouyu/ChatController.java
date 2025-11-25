package com.zhouyu;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import com.alibaba.cloud.ai.toolcalling.time.GetTimeByZoneIdService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private BaiduSearchService baiduSearchService;

    @GetMapping("/chat")
    public String chat(String question) {
        return chatClient.prompt(question)
                .toolNames("getCityTimeFunction")
                .call()
                .content();
    }

    @GetMapping("/baidu")
    public BaiduSearchService.Response baidu(String question) {
        BaiduSearchService.Response result = baiduSearchService.apply(new BaiduSearchService.Request(question, 10));
        return result;
    }
}
