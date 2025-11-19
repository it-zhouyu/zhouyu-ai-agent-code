package com.zhouyu.tools.impl;

import com.zhouyu.tools.BaseTool;
import com.zhouyu.tools.ToolResult;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class TavilySearchTool extends BaseTool {
    
    private final WebSearchEngine searchEngine;

    // https://www.tavily.com/

    public TavilySearchTool() {
        super("tavily_search", "Search the web for information using Tavily search engine");
        
        // Get API key from environment variable
        String apiKey = System.getenv("TAVILY_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("TAVILY_API_KEY environment variable is required");
        }
        
        this.searchEngine = TavilyWebSearchEngine.builder()
            .apiKey(apiKey)
            .build();
    }
    
    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Map<String, Object>> properties = new HashMap<>();
        
        properties.put("query", stringParam("The search query to execute"));
        properties.put("max_results", intParam("Maximum number of search results to return (default: 5, max: 20)"));
        
        return buildSchema(properties, List.of("query"));
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        try {
            String query = getString(parameters, "query");
            if (query == null || query.trim().isEmpty()) {
                return ToolResult.error("Query parameter is required");
            }
            
            // Execute search
            WebSearchResults results = searchEngine.search(query);
            
            // Format results
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("total_results", results.results().size());
            
            List<Map<String, Object>> searchResults = results.results().stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", result.title());
                    item.put("url", result.url());
                    item.put("snippet", result.snippet());
                    return item;
                })
                .toList();
            
            response.put("results", searchResults);
            
            return ToolResult.success(response);
            
        } catch (Exception e) {
            return ToolResult.error("Search failed: " + e.getMessage());
        }
    }
}