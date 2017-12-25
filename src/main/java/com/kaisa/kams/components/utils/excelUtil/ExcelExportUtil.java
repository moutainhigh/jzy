package com.kaisa.kams.components.utils.excelUtil;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouchuang on 2017/5/26.
 */
public class ExcelExportUtil {
    public static void export(HttpServletResponse resp, List list,String fileName){
        OutputStream out = null;
        try {
            String excelName = new String(fileName.getBytes("GB2312"), "ISO_8859_1");
            resp.addHeader("content-type", "application/shlnd.ms-excel;charset=utf-8");
            resp.addHeader("content-disposition", "attachment; filename=" + excelName + ".xlsx");
            XSSFWorkbook workbook = WriteExcelUtil.createExcel(list, fileName);
            out = resp.getOutputStream();
            workbook.write(out);
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != out) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
