package com.zhihui.zhihuijiazu.Common.util;

import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

public class Result {
    private Integer RespCode;
    private Integer RespMsg;
    private Object DataList;
    private Map<String,Object> RetObject;

    public Result(Integer RespCode, Integer RespMsg) {
        this.RespCode = RespCode;
        this.RespMsg = RespMsg;
    }
    public static Result getErrorResult(Integer msg){
        Result result = new Result();
        result.setRespCode(1);
        result.setRespMsg(msg);
        return result;
    }
    public static Result getSuccessResult(Integer msg){
        Result result = new Result();
        result.setRespCode(0);
        result.setRespMsg(msg);
        return result;
    }

    public static Result getSuccessResultData(Integer msg,Object data,Map<String,Object> map){
        Result result = new Result();
        result.setRespCode(0);
        result.setRespMsg(msg);
        result.setDataList(data);
        result.setRetObject(map);
        return result;
    }

    public static Result getResult(Integer respCode,Integer respMsg){
        Result result = new Result();
        result.setRespCode(respCode);
        result.setRespMsg(respMsg);
        return result;
    }

    public static  Result getResultMsg(Integer respMsg){
        Result result = new Result();
        result.setRespMsg(respMsg);
        return result;
    }


    public Result() {
    }

    public Integer getRespCode() {
        return RespCode;
    }

    public void setRespCode(Integer respCode) {
        RespCode = respCode;
    }

    public Integer getRespMsg() {
        return RespMsg;
    }

    public void setRespMsg(Integer respMsg) {
        RespMsg = respMsg;
    }

    public Object getDataList() {
        return DataList;
    }

    public void setDataList(Object dataList) {
        DataList = dataList;
    }

    public void setRetObject(Map<String,Object> retObject) {
        RetObject = retObject;
    }

    public Map<String,Object> getRetObject() {
        return this.RetObject;
    }

}
