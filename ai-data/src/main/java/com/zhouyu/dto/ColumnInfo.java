package com.zhouyu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String columnComment;
}
