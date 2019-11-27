package com.zhihui.zhihuijiazu.Common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    public static boolean isNull(String string){
        if(string!=null && !"".equals(string)){
            return false;
        }else{
            return true;
        }
    }

    public static boolean isEquals(String str1,String str2){
        if(str1.equals(str2)){
            return true;
        }
        return false;
    }

    public static String newDate(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
