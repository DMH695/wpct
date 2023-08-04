package com.example.wpct.config;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.HashMap;
import java.util.Map;

public class CustomSheetWriteHandler implements SheetWriteHandler {



    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        //定义一个map key是需要添加下拉框的列的index value是下拉框数据
        Map<Integer, String[]> mapDropDown = new HashMap<>(3);
        //设置单位身份 值写死
        String[] relation = {"户主","租户"};
        mapDropDown.put(8,relation);

        //获取工作簿
        Sheet sheet = writeSheetHolder.getSheet();
        ///开始设置下拉框
        DataValidationHelper helper = sheet.getDataValidationHelper();
        //设置下拉框
        for (Map.Entry<Integer, String[]> entry : mapDropDown.entrySet()) {
            /*起始行、终止行、起始列、终止列  起始行为1即表示表头不设置**/
            CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, entry.getKey(), entry.getKey());
            /*设置下拉框数据**/
            DataValidationConstraint constraint = helper.createExplicitListConstraint(entry.getValue());
            DataValidation dataValidation = helper.createValidation(constraint, addressList);
            sheet.addValidationData(dataValidation);
        }
    }
}
