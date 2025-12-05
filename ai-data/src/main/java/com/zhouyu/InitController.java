package com.zhouyu;

import com.zhouyu.dto.ColumnInfo;
import com.zhouyu.dto.TableInfo;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@RestController
public class InitController {

    @Autowired
    private VectorStore vectorStore;

    @GetMapping("/search")
    public List<Document> search(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    @GetMapping("/init")
    public List<TableInfo> init() {

        // AI Data自己有数据库，做数据分析时需要连接其他数据库
        String url = "jdbc:mysql://localhost:3306/zhouyu_db";
        String username = "root";
        String password = "Zhouyu123456...";
        DataSource dataSource = createDataSource(url, username, password);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 获取数据库名
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);

        // 获取所有表名
        List<String> tableNames = jdbcTemplate.queryForList("SHOW TABLES", String.class);

        // 获取所有表名及其备注
        String tableInfoSql = "SELECT table_name, table_comment FROM information_schema.TABLES WHERE table_schema = ?";
        List<TableInfo> tableInfoList = jdbcTemplate.query(tableInfoSql, new RowMapper<TableInfo>() {
            @Override
            public TableInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TableInfo(rs.getString("table_name"), rs.getString("table_comment"));
            }
        }, dbName);

        // 获取每个表的字段名称、字段类型、字段注释
        for (TableInfo tableInfo : tableInfoList) {
            String columnInfoSql = "SELECT column_name, column_type, column_comment FROM information_schema.COLUMNS WHERE table_schema = ? AND table_name = ?";
            List<ColumnInfo> columnInfoList = jdbcTemplate.query(columnInfoSql, new RowMapper<ColumnInfo>() {
                @Override
                public ColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new ColumnInfo(rs.getString("column_name"), rs.getString("column_type"), rs.getString("column_comment"));
                }
            }, dbName, tableInfo.getTableName());
            tableInfo.setColumnInfoList(columnInfoList);
        }

        // 每个字段对应一个Document
        List<Document> columnDocuments = new ArrayList<>();
        for (TableInfo tableInfo : tableInfoList) {
            List<ColumnInfo> columnInfoList = tableInfo.getColumnInfoList();
            for (ColumnInfo columnInfo : columnInfoList) {

                String columnName = columnInfo.getColumnName();
                String columnComment = columnInfo.getColumnComment();
                String columnType = columnInfo.getColumnType();

                String content = StringUtils.hasText(columnComment) ? columnComment : columnName;

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("columnName", columnName);
                metadata.put("columnType", columnType);
                metadata.put("columnComment", columnComment);
                metadata.put("tableName", tableInfo.getTableName());
                metadata.put("tableComment", tableInfo.getTableComment());
                metadata.put("vectorType", "column");

                Document document = new Document(content, metadata);
                columnDocuments.add(document);
            }
        }

        // 每个表对应一个Document
        List<Document> tableDocuments = new ArrayList<>();
        for (TableInfo tableInfo : tableInfoList) {
            String tableName = tableInfo.getTableName();
            String tableComment = tableInfo.getTableComment();
            List<ColumnInfo> columnInfoList = tableInfo.getColumnInfoList();

            String content = StringUtils.hasText(tableComment) ? tableComment : tableName;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("tableName", tableName);
            metadata.put("tableComment", tableComment);
            metadata.put("tableColumns", columnInfoList);
            metadata.put("vectorType", "table");

            Document document = new Document(content, metadata);
            tableDocuments.add(document);
        }

        List<Document> documents = new ArrayList<>();
        documents.addAll(columnDocuments);
        documents.addAll(tableDocuments);

//        vectorStore.add(documents);

        // 分批插入
        for (int i = 0; i < documents.size(); i += 5) {
            vectorStore.add(documents.subList(i, Math.min(i + 5, documents.size())));
        }

        return tableInfoList;
    }

    public DataSource createDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver") // 硬编码为 MySQL 驱动
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
