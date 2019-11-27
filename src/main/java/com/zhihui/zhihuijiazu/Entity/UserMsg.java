package com.zhihui.zhihuijiazu.Entity;

public class UserMsg {

    private Integer msgDetailId;

    private Integer msgType;

    private String msgTitle;

    private String msgDesc;

    private String msgTime;

    private String msgButton;

    private Integer actionType;

    private Integer actionId;


    public Integer getMsgDetailId() {
        return msgDetailId;
    }

    public void setMsgDetailId(Integer msgDetailId) {
        this.msgDetailId = msgDetailId;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public String getMsgDesc() {
        return msgDesc;
    }

    public void setMsgDesc(String msgDesc) {
        this.msgDesc = msgDesc;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getMsgButton() {
        return msgButton;
    }

    public void setMsgButton(String msgButton) {
        this.msgButton = msgButton;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }
}
