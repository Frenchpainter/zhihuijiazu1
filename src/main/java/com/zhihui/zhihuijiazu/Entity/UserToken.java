package com.zhihui.zhihuijiazu.Entity;

import com.zhihui.zhihuijiazu.Common.util.TokenUtil;

public class UserToken {

    private String token;

    private Integer userId;

    private Long createtime;

    private Long expiretime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public Long getExpiretime() {
        return expiretime;
    }

    public void setExpiretime(Long expiretime) {
        this.expiretime = expiretime;
    }

    public static UserToken getInstance(Integer userId){
        UserToken userToken=new UserToken();
        userToken.setCreatetime(System.currentTimeMillis()/1000);
        userToken.setToken(TokenUtil.getToken(50));
        userToken.setUserId(userId);
        return userToken;
    }
}
