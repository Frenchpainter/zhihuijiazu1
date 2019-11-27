package com.zhihui.zhihuijiazu.Common.util;

import javax.servlet.http.HttpServletRequest;

public class RequestJson {

    public static String parsePostJson(HttpServletRequest request){
        byte[] buf = new byte[request.getContentLength()];
        try{
            int len = request.getInputStream().read(buf);
            String str = new String(buf, 0, len, "utf-8");
            return str;
        }catch (Exception e){
            return null;
        }

    }

}
