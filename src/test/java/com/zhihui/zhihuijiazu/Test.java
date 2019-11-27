package com.zhihui.zhihuijiazu;

import java.util.Date;
import java.util.Random;

public class Test {

    public static String getCharAndNumr(int length) {

        Random random = new Random();

        StringBuffer valSb = new StringBuffer();

        String charStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int charLength = charStr.length();



        for (int i = 0; i < length; i++) {

            int index = random.nextInt(charLength);

            valSb.append(charStr.charAt(index));

        }

        return valSb.toString();

    }

    public static void main(String[] arg0){
        System.out.println();
    }

}
