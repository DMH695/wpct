package com.example.wpct.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class WxMsgTemplateHasten {

    /**
     * 提醒
     */
    private Map<String, String> first;

    /*房屋号*/
    private Map<String, String> keyword1;

    /*缴费人*/
    private Map<String, String> keyword2;

    /*缴费类型*/
    private Map<String, String> keyword3;

    /*缴费状态*/
    private Map<String, String> keyword4;

    /*合计金额*/
    private Map<String, String> keyword5;

    /**
     * 提醒
     */
    private Map<String, String> remark;




    public HashMap<String, String> getFormat(String str) {
        return new HashMap<String, String>() {{
            put("value", str);
        }};
    }


    public void setFirst(String first) {
        this.first = getFormat(first);
    }

    public void setKeyword1(String keyword1) {
        this.keyword1 = getFormat(keyword1);
    }

    public void setKeyword2(String keyword2) {
        this.keyword2 = getFormat(keyword2);
    }

    public void setKeyword3(String keyword3) {
        this.keyword3 = getFormat(keyword3);
    }

    public void setKeyword4(String keyword4) {
        this.keyword4 = getFormat(keyword4);
    }

    public void setKeyword5(String keyword5) {
        this.keyword5 = getFormat(keyword5);
    }


    public void setRemark(String remark) {
        this.remark = getFormat(remark);
    }

}
