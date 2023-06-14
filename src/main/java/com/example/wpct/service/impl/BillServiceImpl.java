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
import lombok.Builder;
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
import java.util.ArrayList;
import java.util.List;

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
        return PageUtil.getPageResult(getPage(pageRequest, villageName, buildName, roomNum), page);

    }

    @Override
    public void delete(int id) {
        billMapper.delete(id);
    }

    @SneakyThrows
    @Override
    public ResultBody getReceiptCertificate(int id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            return ResultBody.fail("unknown id");
        }
        String imgPath = "";
        if (bill.getType().equals("物业费")){
            imgPath = "img/sj_property_template.png";
        }else if (bill.getType().equals("公摊费")){
            imgPath = "img/sj_shared_template.png";
        }
        ClassPathResource classPathResource = new ClassPathResource(imgPath);
        InputStream inputStream = classPathResource.getInputStream();
        BufferedImage certificateImage = ImageIO.read(inputStream);
        inputStream.close();

        Graphics2D g = (Graphics2D) certificateImage.getGraphics();
        g.setBackground(Color.white);
        g.setColor(Color.black);
        Font font = new Font("宋体", Font.PLAIN, 16);
        g.setFont(font);
        //抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fontMetrics = g.getFontMetrics();
        int fixH = fontMetrics.getHeight() / 2;
        //开篇日期
        g.drawString(new Date(System.currentTimeMillis()).toString(), 120, 75 + fixH);
        //收据号码

        //付款单位
        String paymentUnit;
        HousingInformationDto house = housingInformationService.query()
                .eq("village_name", bill.getVillageName())
                .eq("build_number", bill.getBuildName())
                .eq("house_no", bill.getRoomNum()).one();
        if (house == null) {
            return ResultBody.fail("unknown house");
        }
        paymentUnit = String.format("%s-%s-%s    %s"
                , house.getVillageName()
                , house.getBuildNumber()
                , house.getHouseNo()
                , house.getName()
        );
        g.drawString(paymentUnit, 95, 120 + fixH);
        List<DrawStringUnit> drawStringUnits = new ArrayList<>();
        if (bill.getType().equals("物业费")) {
            drawStringUnits = getPropertyStrings(bill,fixH);
        } else if (bill.getType().equals("公摊费")) {
            drawStringUnits = getSharedFeeStrings(bill,fixH);
        }
        drawAll(drawStringUnits,g);
        g.dispose();
        return ResultBody.ok(ImageTools.imgToBase64(certificateImage));
    }

    /**
     * 获取物业费凭证的打印数据
     *
     * @param bill 账单
     * @param fixH 修正高度
     * @return 打印数据
     */
    private List<DrawStringUnit> getPropertyStrings(Bill bill, int fixH) {
        JSONObject detail = JSONObject.parseObject(bill.getDetail());
        int[][] detailLocations = {{145, 205 + fixH}, {500, 205 + fixH}};
        String date = bill.getBeginDate() + "-" + bill.getEndDate();
        String[] amounts = {
                detail.getString("基础面积费用"),
                detail.getString("超出面积费用"),
                detail.getString("停车费"),
                detail.getString("其他费用"),
                detail.getString("应收应退租金"),
                detail.getString("应收应退物业费"),
                detail.getString("收回不符合条件疫情减免金额"),
                detail.getString("收回不符合条件租金"),
                detail.getString("优惠"),
        };
        int[][] allAmountLocations = {{125, 650 + fixH}, {495, 650 + fixH}};
        List<DrawStringUnit> res = new ArrayList<>();
        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] == null) {
                amounts[i] = "";
            }
            res.add(DrawStringUnit.builder().str(date).x(detailLocations[0][0]).y(detailLocations[0][1] + i * 50).build());
            res.add(DrawStringUnit.builder().str(amounts[i]).x(detailLocations[1][0]).y(detailLocations[1][1] + i * 50).build());
        }
        res.add(DrawStringUnit.builder().str(digitCapital(bill.getPay())).x(allAmountLocations[0][0]).y(allAmountLocations[0][1]).build());
        res.add(DrawStringUnit.builder().str(String.valueOf(bill.getPay())).x(allAmountLocations[1][0]).y(allAmountLocations[1][1]).build());
        int[][] human = {{70,875},{185,875}};
        res.add(DrawStringUnit.builder().str(PAYEE).x(human[0][0]).y(human[0][1]).build());
        res.add(DrawStringUnit.builder().str(BILLER).x(human[1][0]).y(human[1][1]).build());
        return res;
    }

    /**
     * 获取公摊费凭证的打印数据
     *
     * @param bill 账单
     * @param fixH 修正高度
     * @return 打印数据
     */
    private List<DrawStringUnit> getSharedFeeStrings(Bill bill, int fixH){
        JSONObject detail = JSONObject.parseObject(bill.getDetail());
        int[][] detailLocations = {{145, 205 + fixH}, {500, 205 + fixH}};
        String date = bill.getBeginDate() + "-" + bill.getEndDate();
        String[] amounts = {
                detail.getString("公摊水费"),
                detail.getString("公摊电费"),
                detail.getString("公摊电梯费"),
        };
        int[][] allAmountLocations = {{125, 400 + fixH}, {495, 400 + fixH}};
        List<DrawStringUnit> res = new ArrayList<>();
        for (int i = 0; i < amounts.length; i++) {
            res.add(DrawStringUnit.builder().str(date).x(detailLocations[0][0]).y(detailLocations[0][1] + i * 50).build());
            res.add(DrawStringUnit.builder().str(amounts[i]).x(detailLocations[1][0]).y(detailLocations[1][1] + i * 50).build());
        }
        res.add(DrawStringUnit.builder().str(digitCapital(bill.getPay())).x(allAmountLocations[0][0]).y(allAmountLocations[0][1]).build());
        res.add(DrawStringUnit.builder().str(String.valueOf(bill.getPay())).x(allAmountLocations[1][0]).y(allAmountLocations[1][1]).build());
        int[][] human = {{70,630},{185,630}};
        res.add(DrawStringUnit.builder().str(PAYEE).x(human[0][0]).y(human[0][1]).build());
        res.add(DrawStringUnit.builder().str(BILLER).x(human[1][0]).y(human[1][1]).build());
        return res;
    }

    private void drawAll(List<DrawStringUnit> drawStringUnits,Graphics2D g){
        for (DrawStringUnit drawStringUnit : drawStringUnits) {
            g.drawString(drawStringUnit.getStr(),drawStringUnit.getX(),drawStringUnit.getY());
        }
    }

    @Override
    public List<Bill> getByOpenid(String openid,String villageName,String buildName,String roomNum) {
        return billMapper.getOne(openid,villageName,buildName,roomNum);
    }

    @Override
    public List<Bill> getByWid(String out_trade_no) {
        return billMapper.getByWid(out_trade_no);
    }


    private PageInfo<?> getPage(PageRequest pageRequest, String villageName, String buildName, String roomNum) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(billMapper.getAll(villageName, buildName, roomNum));
    }

    /**
     * 数字转大写
     *
     * @param n 金额
     * @return 大写数字
     */
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
        int integerPart = (int) Math.floor(n);
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

    @Data
    @Builder
    private static class DrawStringUnit {
        private String str;
        private int x;
        private int y;
    }


}
