package com.kaisa.kams.components.utils.excelUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zhouchuang on 2017/5/20.
 */
public class WriteExcelUtil {
    private static String split = "@#@";
    public static XSSFWorkbook createExcel(List list, String sheetName)throws Exception{


        XSSFWorkbook workbook =  new XSSFWorkbook() ;

        //时间类型
        XSSFCellStyle dateStyle = workbook.createCellStyle();
        XSSFDataFormat format= workbook.createDataFormat();
        dateStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));

        Sheet sheet = workbook.createSheet(sheetName);
        XSSFCellStyle rightAlignStyle = getCellRightAlign(workbook);
        XSSFCellStyle rightAlignPrecision = getCellRightAlignPrecision(workbook);
        if(list.size()>0){
            setColumnWidth(sheet,list.get(0).getClass());
            String[] keys = getKeys(list.get(0).getClass());
            int rows = createTitle(sheet,list.get(0).getClass(),getTitleStyle(workbook));
            Map<String,MergeModel> columnName  = new HashMap<>();
            for(int i=0;i<list.size();i++){
                Object obj =  list.get(i);
                Row row = sheet.createRow(i+rows);
                for(int j=0;j<keys.length;j++){
                    String key = keys[j];
                    Field field = obj.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    Cell cell = row.createCell(j);
                    Object value  = field.get(obj);
                    ExcelAssistant  excelAssistant = field.getAnnotation(ExcelAssistant.class);
                    if(value!=null){

                        if(value instanceof  String){
                            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                            cell.setCellValue(value.toString());
                        }else if(value instanceof Date){
                            cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                            cell.setCellStyle(dateStyle);
                            cell.setCellValue((Date)value);
                            Calendar calendar  = Calendar.getInstance();
                            calendar.setTime((Date)value);
                            cell.setCellValue(calendar);
                        }else if(value instanceof BigDecimal){
                            int precision = 2;
                            boolean showPercentile = false;
                            if(excelAssistant!=null){
                                precision = excelAssistant.precision();
                                showPercentile = excelAssistant.showPercentile();
                            }
                            if(showPercentile){
                                cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                cell.setCellStyle(rightAlignPrecision);
                                cell.setCellValue(((BigDecimal)value).doubleValue());
                            }else{
                                cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                                cell.setCellStyle(WriteExcelUtil.getNumberStyle(workbook,precision));
                                cell.setCellValue(WriteExcelUtil.precision(((BigDecimal) value).doubleValue(),precision));
                            }
                        }else if(value instanceof Integer ){
                            cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(value.toString()));
                            cell.setCellStyle(rightAlignStyle);
                        }else {
                            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                            cell.setCellValue(value.toString());
                        }

                    }else{
                        cell.setCellValue("--");
                    }

                    if(excelAssistant.mergeColumn()){
                        //如果又不是合并又为空则退出循环
                        if(StringUtils.isEmpty(excelAssistant.mergeRelationColumn())&&value==null)continue;
                        //第一列
                        String refName =  StringUtils.isNotEmpty(excelAssistant.mergeRelationColumn())?excelAssistant.mergeRelationColumn():field.getName();
                        String columnkey = field.getName();
                        if(columnName.get(columnkey)==null){
                            MergeModel mergeModel = getMergeModel(value,refName,obj);
                            columnName.put(columnkey,mergeModel);
                        }else if(!columnName.get(columnkey).getRefValue().equals(columnName.get(columnkey).getRefField().get(obj).toString())){
                            String val = columnName.get(columnkey).getValue();
                            if(StringUtils.isNotEmpty(val)) {
                                int rowsNum = columnName.get(columnkey).getCross();
                                CellRangeAddress region = new CellRangeAddress(i-rowsNum+rows,i+rows-1, j, j);
                                sheet.addMergedRegion(region);
                            }
                            MergeModel mergeModel = getMergeModel(value,refName,obj);
                            columnName.put(columnkey,mergeModel);
                        }else{
                            //sheet.getRow(row.getLastCellNum()).getCell(j).setCellValue("");
                            sheet.getRow(i+rows).getCell(j).setCellValue("");
                            columnName.get(columnkey).add();
                        }
                        if(i==(list.size()-1)){
                            String val = columnName.get(columnkey).getValue();
                            if(StringUtils.isNotEmpty(val)) {
                                int rowsNum = columnName.get(columnkey).getCross();
                                CellRangeAddress region = new CellRangeAddress(i-rowsNum+rows+1,i+rows, j, j);
                                sheet.addMergedRegion(region);
                            }
                        }

                       /* if(columnName.get(field.getName())==null){
                            columnName.put(field.getName(),value.toString()+split+1);
                        }else if(!columnName.get(field.getName()).startsWith(value.toString()+split)) {//新的列值了，需要合并，合并完了需要初始化新的列值
                            String val = columnName.get(field.getName());
                            if(StringUtils.isNotEmpty(val)) {
                                int rowsNum = Integer.parseInt(val.split(split)[1]);
                                CellRangeAddress region = new CellRangeAddress(i-rowsNum+rows,i+rows-1, j, j);
                                sheet.addMergedRegion(region);
                            }
                            columnName.put(field.getName(),value.toString()+split+1);
                        }else{//重复的列值，+1  ，上列内容设置为空
                            sheet.getRow(row.getLastCellNum()).getCell(j).setCellValue("");
                            columnName.put(field.getName(),value.toString()+split+(Integer.parseInt(columnName.get(field.getName()).split(split)[1])+1));
                        }

                        //数据处理完了需要把最后的结尾弄干净
                        if(i==(list.size()-1)){
                            String val = columnName.get(field.getName());
                            if(StringUtils.isNotEmpty(val)) {
                                int rowsNum = Integer.parseInt(val.split(split)[1]);
                                CellRangeAddress region = new CellRangeAddress(i-rowsNum+rows+1,i+rows, j, j);
                                sheet.addMergedRegion(region);
                            }
                        }*/

                    }
                }
            }
        }
        return workbook;
    }
    private static  MergeModel getMergeModel(Object value ,String refName,Object obj)throws Exception{
        MergeModel mergeModel = new MergeModel();
        mergeModel.setValue(value!=null?value.toString():"--");
        mergeModel.add();
        Field refField = obj.getClass().getDeclaredField(refName);
        refField.setAccessible(true);
        mergeModel.setRefField(refField);
        mergeModel.setRefValue(refField.get(obj).toString());
        return mergeModel;
    }
    private static XSSFCellStyle getTitleStyle( XSSFWorkbook workbook ){
        XSSFFont font1 = workbook.createFont();
        font1.setFontHeightInPoints((short) 10);
        //设置字体
        font1.setFontName("微软雅黑");
        //设置加粗
        font1.setBold(true);
        //设置字体颜色
        font1.setColor(IndexedColors.BLACK.getIndex());
        //font1.setColor(HSSFColor.YELLOW.index);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font1);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());// 设置背景色
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);


        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框




        return style;
    }

    public static String[]  getKeys(Class clazz){
        String keys = "";
        for(Field field : clazz.getDeclaredFields()){
            if(field.getAnnotation(ExcelAssistant.class)!=null){
                keys += field.getName()+",";
            }
        }
        return keys.substring(0,keys.length()-1).split(",");
    }
    public static int  createTitle(Sheet sheet,Class clazz,XSSFCellStyle style){
        if(isMerge(clazz)){
            int mergeCount  =  0;
            int lastConut =  0;
            CellRangeAddress region =null;
            String mergeTitle = "";
            Row row = sheet.createRow(0);
            row.setHeight((short) (400));
            for(Field field : clazz.getDeclaredFields()){
                if(field.getAnnotation(ExcelAssistant.class)!=null){
                    ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
                    if(StringUtils.isNotEmpty(mergeTitle)&&!excelAssistant.mergeTitleName().equals(mergeTitle)){
                        if(mergeCount==1){
                            region = new CellRangeAddress(0,1, (short)lastConut, (short)(lastConut));
                        }else{
                            region = new CellRangeAddress(0, 0, (short)lastConut, (short) (lastConut+mergeCount-1));
                        }
                        sheet.addMergedRegion(region);
                        Cell cell = row.createCell(lastConut);
                        cell.setCellValue(mergeTitle);
                        cell.setCellStyle(style);
                        lastConut = lastConut+mergeCount;
                        mergeCount=0;
                    }
                    mergeCount++;
                    mergeTitle = excelAssistant.mergeTitleName();
                }
            }
            if(mergeCount==1){
                region = new CellRangeAddress(0,1, (short)lastConut, (short)(lastConut));
            }else{
                region = new CellRangeAddress(0, 0, (short)lastConut, (short) (lastConut+mergeCount-1));
            }
            sheet.addMergedRegion(region);
            Cell cell1 = row.createCell(lastConut);
            cell1.setCellValue(mergeTitle);
            cell1.setCellStyle(style);
            int i = 0;
            Row row1 = sheet.createRow(1);
            row.setHeight((short) (400));
            for(Field field : clazz.getDeclaredFields()){
                if(field.getAnnotation(ExcelAssistant.class)!=null){
                    ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
                    Cell cell = row1.createCell(i++);
                    cell.setCellStyle(style);
                    if(!excelAssistant.titleName().equals(excelAssistant.mergeTitleName())){
                        cell.setCellValue(excelAssistant.titleName());
                    }else{
                        cell.setCellValue("");
                    }
                }
            }
            return 2;
        }else{
            int i = 0;
            Row row = sheet.createRow(0);
            row.setHeight((short) (400));
            for(Field field : clazz.getDeclaredFields()){
                if(field.getAnnotation(ExcelAssistant.class)!=null){
                    ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
                    Cell cell = row.createCell(i++);
                    cell.setCellValue(excelAssistant.titleName());
                    cell.setCellStyle(style);
                }
            }
            return 1;
        }

    }
    private static  boolean isMerge(Class clazz){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getAnnotation(ExcelAssistant.class)!=null){
                ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
                if(StringUtils.isNotEmpty(excelAssistant.mergeTitleName()))return true;
            }
        }
        return false;
    }

    private static void setColumnWidth(Sheet sheet,Class clazz){
        int i =0;
        for(Field field : clazz.getDeclaredFields()){
            if(field.getAnnotation(ExcelAssistant.class)!=null){
                int width = ((ExcelAssistant)field.getAnnotation(ExcelAssistant.class)).width();
                sheet.setColumnWidth(i++, width);
            }
        }
    }

    public static XSSFCellStyle getCellRightAlign(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        return style;
    }

    public static XSSFCellStyle getNumberStyle(XSSFWorkbook workbook,int precision){
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0."+"00000000".substring(0,precision))); //小数点后保留两位，可以写contentStyle.setDataFormat(df.getFormat("#,#0.00"));
        return style;
    }
    public static XSSFCellStyle getCellRightAlignPrecision(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));
        return style;
    }

    public static Double precision(Double value ,int precision){
        java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#."+"00000000".substring(0,precision));
        return  Double.parseDouble(df.format(value));
    }
}
