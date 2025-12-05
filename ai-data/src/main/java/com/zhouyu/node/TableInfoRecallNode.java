package com.zhouyu.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson2.JSONObject;
import com.zhouyu.dto.ColumnInfo;
import com.zhouyu.dto.TableInfo;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class TableInfoRecallNode implements NodeAction {

    private VectorStore vectorStore;

    public TableInfoRecallNode(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String keywordsExtractResult = state.value("keywordsExtractResult", String.class).orElseThrow();
        Map<String, List<String>> keywordsExtractResultJSON = JSONObject.parseObject(keywordsExtractResult, Map.class);
        List<String> keywords = keywordsExtractResultJSON.get("keywords");

        // 匹配表
        List<Document> tableDocuments = new ArrayList<>();
        for (String keyword : keywords) {
            SearchRequest tableSearchRequest = SearchRequest.builder()
                    .query(keyword)
                    .topK(3)
                    .filterExpression("'vectorType' == 'table'")
                    .build();
            List<Document> documents = vectorStore.similaritySearch(tableSearchRequest);
            tableDocuments.addAll(documents);
        }

        // 去重
        List<Document> uniqueTableDocuments = tableDocuments.stream().filter(distinctByKey(Document::getId)).toList();

        List<TableInfo> tableInfoList = new ArrayList<>();

        // 遍历每个表，按关键字搜索匹配表中相关字段
        for (Document tableDocument : uniqueTableDocuments) {
            Map<String, Object> tableMetadata = tableDocument.getMetadata();
            String tableName = (String) tableMetadata.get("tableName");
            String tableComment = (String) tableMetadata.get("tableComment");

            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            tableInfo.setTableComment(tableComment);

            // 10个字段
            List<ColumnInfo> columnInfoList = new ArrayList<>();
            for (String keyword : keywords) {
                SearchRequest columnSearchRequest = SearchRequest.builder()
                        .query(keyword)
                        .topK(5)
                        .filterExpression("'vectorType' == 'column' and 'tableName' == '"+tableName+"'")
                        .build();
                List<Document> columnDocuments = vectorStore.similaritySearch(columnSearchRequest);
                for (Document columnDocument : columnDocuments) {
                    Map<String, Object> columnMetadata = columnDocument.getMetadata();
                    String columnName = (String) columnMetadata.get("columnName");
                    String columnType = (String) columnMetadata.get("columnType");
                    String columnComment = (String) columnMetadata.get("columnComment");
                    ColumnInfo columnInfo = new ColumnInfo(columnName, columnType, columnComment);
                    columnInfoList.add(columnInfo);
                }

            }
            tableInfo.setColumnInfoList(new ArrayList<>(new HashSet<>(columnInfoList)));

            tableInfoList.add(tableInfo);
        }

        return Map.of("tableInfoRecallResult", tableInfoList);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
