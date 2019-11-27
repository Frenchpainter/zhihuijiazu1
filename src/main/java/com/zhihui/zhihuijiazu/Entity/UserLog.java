package com.zhihui.zhihuijiazu.Entity;

public class UserLog {

    private Integer id;

    //用户id
    private Integer userId;

    //登录时间
    private String logintime;

    //终端类型
    private Integer terminal;

    //操作类型
    private Integer type;

    //登录方式
    private Integer logintype;

    private String summary;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogintime() {
        return logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLogintype() {
        return logintype;
    }

    public void setLogintype(Integer logintype) {
        this.logintype = logintype;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public static UserLog userLogData(Integer userId,String logintime,Integer terminal,Integer type,Integer logintype){
        UserLog userLog=new UserLog();
        userLog.setUserId(userId);
        userLog.setLogintime(logintime);
        userLog.setTerminal(terminal);
        userLog.setLogintype(logintype);
        userLog.setType(type);
        return userLog;
    }
}
