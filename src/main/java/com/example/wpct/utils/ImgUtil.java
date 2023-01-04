package com.example.wpct.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgUtil {
    public static String adjust(String content){
        String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*/>";
        Matcher matcher = Pattern.compile(regxpForImgTag).matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String contentTemp = matcher.group(1);
            System.out.println(contentTemp);
           /* contentTemp = replaceContentByPartten(contentTemp,
                    "src=\"([^\"]+)\"",
                    "src=\"http://localhost:8080/uploadFile/test.mp4\"");*/
            if (!contentTemp.contains("height")){
                contentTemp = replaceContentByPartten(contentTemp,
                        "contenteditable=\"false\"", "contenteditable=\"false\"" + " width=\"358\"" + " height=\"264\"");
            }else {
                contentTemp = replaceContentByPartten(contentTemp,
                        "width=\"([^\"]+)\"", "width=\"358\"");
                contentTemp = replaceContentByPartten(contentTemp,
                        "height=\"([^\"]+)\"", "height=\"264\"");
            }
            matcher.appendReplacement(sb, "<img "+ contentTemp + "/>");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    public static String replaceContentByPartten(String content,
                                                 String partten, String target) {
        Matcher matcher = Pattern.compile(partten, Pattern.CASE_INSENSITIVE).matcher(content);
        if (matcher.find()) {
            StringBuffer temp = new StringBuffer();
            matcher.appendReplacement(temp, target);
            matcher.appendTail(temp);
            return temp.toString();
        } else {
            return content;
        }
    }
}
