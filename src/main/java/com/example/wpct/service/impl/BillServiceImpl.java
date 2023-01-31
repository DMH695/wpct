package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.Bill;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.mapper.BillMapper;
import com.example.wpct.service.BillService;
import com.example.wpct.utils.ImageTools;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;

@Service
public class BillServiceImpl implements BillService {

    public static Page page;

    /**
     * 收款人
     */
    private static final String PAYEE = "曾雅娟";
    /**
     * 开票人
     */
    private static final String BILLER = "洪巧玲";



    @Autowired
    BillMapper billMapper;

    @Autowired
    @Lazy
    private HousingInformationServiceImpl housingInformationService;

    @Override
    public PageResult getAll(PageRequest pageRequest, String villageName, String buildName, String roomNum) {
        return PageUtil.getPageResult(getPage(pageRequest,villageName,buildName,roomNum),page);

    }

    @Override
    public void delete(int id) {
        billMapper.delete(id);
    }

    @SneakyThrows
    @Override
    public ResultBody getReceiptCertificate(int id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null){
            return ResultBody.fail("unknown id");
        }
        ClassPathResource classPathResource = new ClassPathResource("img/sj_template.png");
        InputStream inputStream = classPathResource.getInputStream();
        BufferedImage certificateImage = ImageIO.read(inputStream);
        inputStream.close();

        Graphics2D g = (Graphics2D) certificateImage.getGraphics();
        g.setBackground(Color.white);
        g.setColor(Color.black);
        Font font = new Font("宋体",Font.PLAIN,16);
        g.setFont(font);
        //抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fontMetrics = g.getFontMetrics();
        int fixH = fontMetrics.getHeight()/2;
        //开篇日期
        g.drawString(new Date(System.currentTimeMillis()).toString(),120,75+fixH);
        //收据号码

        //付款单位
        String paymentUnit;
        HousingInformationDto house = housingInformationService.query()
                .eq("village_name", bill.getVillageName())
                .eq("build_number", bill.getBuildName())
                .eq("house_no", bill.getRoomNum()).one();
        if (house == null){
            return ResultBody.fail("unknown house");
        }
        paymentUnit = String.format("%s-%s-%s    %s"
                ,house.getVillageName()
                ,house.getBuildNumber()
                ,house.getHouseNo()
                ,house.getName()
        );
        g.drawString(paymentUnit,95,120+fixH);

        if (bill.getType().equals("物业费")){
            String tmp = bill.getDetail();
            JSONObject detailJSON = JSONObject.parseObject(tmp);

        }else if (bill.getType().equals("公摊费")){

        }
        //金额
        g.drawString(bill.getPay().toString(),505,215+fixH);
        //价税合计
        g.drawString(digitCapital(bill.getPay()),130,405+fixH);
        //￥
        g.drawString(bill.getPay().toString(),490,405+fixH);
        //bottom
        g.drawString(PAYEE,75,630+fixH);
        g.drawString(BILLER,190,630+fixH);
        g.dispose();
        return ResultBody.ok(ImageTools.imgToBase64(certificateImage));
    }


    private PageInfo<?> getPage(PageRequest pageRequest,String villageName,String buildName,String roomNum) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(billMapper.getAll(villageName,buildName,roomNum));
    }

    public static String digitCapital(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        String head = n < 0 ? "负" : "";
        // 如果是负数取绝对值
        n = Math.abs(n);
        StringBuilder s = new StringBuilder();
        BigDecimal bigDecimal = new BigDecimal(Double.valueOf(n).toString());
        String nStr = bigDecimal.toString();
        // 小数部分
        String[] split = nStr.split("\\.");
        if (split.length > 1) {
            // 小数点为特殊符号，在分割时需进行转义
            String decimalStr = split[1];
            if (decimalStr.length() > 2) {
                decimalStr = decimalStr.substring(0, 2);
            }
            // 将小数部分转换为整数
            int integer = Integer.parseInt(decimalStr);
            StringBuilder p = new StringBuilder();
            for (int i = 0; i < decimalStr.length() && i < fraction.length; i++) {
                p.insert(0, digit[integer % 10] + fraction[decimalStr.length() - i - 1]);
                integer = integer / 10;
            }
            s.insert(0, p.toString().replaceAll("(零.)+", ""));
        }
        if (s.length() < 1) {
            s = new StringBuilder("整");
        }
        int integerPart = (int)Math.floor(n);
        // 整数部分
        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            StringBuilder p = new StringBuilder();
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p.insert(0, digit[integerPart % 10] + unit[1][j]);
                integerPart = integerPart / 10;
            }
            s.insert(0, p.toString().replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i]);
        }
        return head + s.toString().replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }

}
