package com.zhouyu.context;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class UserContext {
    private final String userId;

    public UserContext(String userId) { this.userId = userId; }
    public String getUserId() { return userId; }
}
