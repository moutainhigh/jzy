
package com.kaisa.kams.components.utils.excelUtil;
import java.io.*;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.Region;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 该类实现了基于模板的导出
 * 如果要替换信息，需要传入一个Map，这个map中存储着要替换信息的值，在excel中通过#来开头
 * 要从哪一行那一列开始替换需要定义locationName.
 * 如果要设定相应的样式，可以在该行使用styles完成设定，此时所有此行都使用该样式
 * @author luoyj
 * @date 2017-02-27
 */
public class ExcelTemplate {

    /**
     * 数据结束标识
     */
    public static final String END = "end";
    /**
     * 默认样式标识
     */
    public static final String DEFAULT_STYLE = "defaultStyle";  //默认样式
    /**
     * 列样式标识
     */
    public static final String STYLE = "styles";  //样式


    private static ExcelTemplate et = new ExcelTemplate();
    private Workbook wb;
    private Sheet sheet;
    private int initColIndex;//数据的初始化列数 
    private int initRowIndex;//数据的初始化行数
    private int curColIndex;//当前列数
    private int curRowIndex;//当前行数
    private Row curRow;//当前行对象
    private int lastRowIndex;//最后一行的行数
    private CellStyle defaultStyle;//默认样式
    private float rowHeight;//默认行高
    //存储每一列所对应的样式
    private Map<Integer,CellStyle> styles = new HashMap<Integer,CellStyle>();

    private ExcelTemplate(){
        
    }
    public static ExcelTemplate getInstance(){
        return et;
    }
    
    /**
     * 从classpath路径下读取相应的模板文件
     * @param path
     * @return
     */
    public ExcelTemplate readTemplateByClasspath(String path){
        try {
            wb = WorkbookFactory.create(ExcelTemplate.class.getResourceAsStream(path));
            initTemplate();
        } catch (InvalidFormatException e) {
            throw new RuntimeException("读取模板格式有错，请检查");
        } catch (IOException e) {
            throw new RuntimeException("读取模板不存在，请检查");
        }
        return this;
    }
    
    /**
     * 从某个路径来读取模板
     * @param path
     * @return
     */
    public ExcelTemplate readTemplateByPath(String path){
        try {
            wb = WorkbookFactory.create(new File(path));
            initTemplate();
        } catch (InvalidFormatException e) {
            throw new RuntimeException("读取模板格式有错，请检查");
        } catch (IOException e) {
            throw new RuntimeException("读取模板不存在，请检查");
        }
        return this;
    }
    
    /**
     * 将文件写到相应的路径下
     * @param
     */
    public void writeToFile(String filePath){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            wb.write(fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("写入的文件不存在");
        } catch (IOException e) {
            throw new RuntimeException("写入数据失败"+e.getMessage());
        } finally{
            try {
                if(fos!=null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将文件通过web导出
     * @param fileName
     * @param request
     * @param response
     */
    public  void writeToWeb(String fileName, HttpServletRequest request, HttpServletResponse response){
        try {
            String finalFileName = URLEncoder.encode(fileName,"UTF8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename="+finalFileName+".xlsx");
            OutputStream ouputStream = response.getOutputStream();
            wb.write(ouputStream);
            ouputStream.flush();
            ouputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("写入数据失败"+e.getMessage());
        }
    }

    /**
     * 创建相应的元素，基于String类型
     * @param value
     */
    public void createCell(String value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue(value);
        curColIndex++;
    }
    public void createCell(int value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue((int)value);
        curColIndex++;
    }
    public void createCell(Date value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue(value);
        curColIndex++;
    }
    public void createCell(double value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue(value);
        curColIndex++;
    }
    public void createCell(boolean value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue(value);
        curColIndex++;
    }
    
    public void createCell(Calendar value) {
        Cell c = curRow.createCell(curColIndex);
        setCellStyle(c);
        c.setCellValue(value);
        curColIndex++;
    }
    
    /**
     * 设置某个元素的样式
     * @param c
     */
    private void setCellStyle(Cell c){
        if(styles.containsKey(curColIndex)) {
            CellStyle style=styles.get(curColIndex);
            style.setWrapText(true);
        	c.setCellStyle(style);
        } else {
            c.setCellStyle(defaultStyle);
        }
    }
    
    /**
     * 创建新行，在使用时只要添加完一行，需要调用该方法创建
     */
    public void createNewRow(){
        if(lastRowIndex > curRowIndex&&curRowIndex!=initRowIndex){
            sheet.shiftRows(curRowIndex, lastRowIndex, 1, true, true);//从当前行到最后一行往下移一行
            lastRowIndex ++;
        }
        curRow = sheet.createRow(curRowIndex);
        curRow.setHeightInPoints(rowHeight);
        curRowIndex++;
        curColIndex = initColIndex;
    }
    
    /**
     * 根据map替换相应的常量，通过Map中的值来替换#开头的值
     */
    public void replaceFinalData(Map<String, String> datas){
        if(datas==null) return;
        for(Row row : sheet){
            for(Cell c : row){
                if(c.getCellType()!=Cell.CELL_TYPE_STRING) continue;
                String str = c.getStringCellValue().trim();
                if(str.startsWith("#")){
                    if(datas.get(str.substring(1))==null||datas.get(str.substring(1)).equals("")){
                        c.setCellValue("");
                    }else{
                        c.setCellValue(datas.get(str.substring(1)));
                    }
                }

            }
        }
    }

    /**
     * 自动适应高度
     */
    public void setRowHeight() {
        for (Row row : sheet) {
            for (Cell cell : row) {
                calcAndSetRowHeigt(row,cell.getStringCellValue(),cell);
            }
        }
    }
    
    /**
     * 初始化模板
     */
    private void initTemplate(){
        sheet = wb.getSheetAt(0);
        initConfigData();
        lastRowIndex = sheet.getLastRowNum();
        curRow = sheet.createRow(curRowIndex);
    }

    /**
     *  通过位置名称获取行和列的位置
      * @param locationName
     */
   public void hqLocation(String locationName){
	   for(Row row : sheet){
		           for(Cell c : row){
		               if(c.getCellType()!=Cell.CELL_TYPE_STRING) continue;
		               String str = c.getStringCellValue().trim();
		               if(str.equals(locationName)){
		                   initColIndex = c.getColumnIndex();
		                   initRowIndex = row.getRowNum();
		                   curColIndex = initColIndex;
		                   curRowIndex = initRowIndex;
		                   defaultStyle = c.getCellStyle();
		                   rowHeight = row.getHeightInPoints();
		                   break;
		               }
		           }
		       }
       lastRowIndex = sheet.getLastRowNum();
   }

    /**
     * 初始化数据信息
     */
    private void initConfigData() {
        for(Row row : sheet){
            for(Cell c : row){
                if(c.getCellType()!=Cell.CELL_TYPE_STRING) continue;
                String str = c.getStringCellValue().trim();
                if(str.equals(END)){
                    initColIndex = c.getColumnIndex();
                    initRowIndex = row.getRowNum();
                    curColIndex = initColIndex;
                    curRowIndex = initRowIndex;
                    defaultStyle = c.getCellStyle();
                    rowHeight = row.getHeightInPoints();
                    initStyles();
                    break;
                }
            }
        }
        System.out.println(curRowIndex+"行--"+curColIndex+"列");
    }

    /**
     * 初始化样式信息
     */
    private void initStyles() {
        for(Row row : sheet){
            for(Cell c : row){
                if(c.getCellType()!=Cell.CELL_TYPE_STRING) continue;
                String str = c.getStringCellValue().trim();
                if(str.equals(DEFAULT_STYLE)){
                    defaultStyle = c.getCellStyle();
                }
                if(str.equals(STYLE)){
                    styles.put(c.getColumnIndex(), c.getCellStyle());
                }
            }
        }
    }

    /**
     * 根据行内容重新计算行高
     * @param sourceRow
     */
    public static void calcAndSetRowHeigt(Row sourceRow,String value,Cell sourceCell) {
        //原行高
        short height = sourceRow.getHeight();
        //计算后的行高
        double maxHeight = height;
            //单元格的内容
            String cellContent =value;
            //单元格的宽度
            int columnWidth = sourceCell.getSheet().getColumnWidth(sourceCell.getColumnIndex());
            CellStyle cellStyle = sourceCell.getCellStyle();
            Font font =  sourceCell.getSheet().getWorkbook().getFontAt(cellStyle.getFontIndex());
            //字体的高度
            short fontHeight = font.getFontHeight();
            //cell内容字符串总宽度
            double cellContentWidth = cellContent.getBytes().length * 2 * 256;
            //字符串需要的行数 不做四舍五入之类的操作
            double stringNeedsRows =(double)cellContentWidth / columnWidth;
            //小于一行补足一行
            if(stringNeedsRows < 1.0){
                stringNeedsRows = 1.0;
            }
            //需要的高度 (Math.floor(stringNeedsRows) - 1) * 40 为两行之间空白高度
            double stringNeedsHeight = (double)fontHeight * stringNeedsRows;
            if(stringNeedsHeight > maxHeight){
                maxHeight = stringNeedsHeight;
            }
        //超过原行高三倍 则为3倍 实际应用中可
        if(maxHeight/height > 5){
            maxHeight = 6 * height;
        }
        //最后取天花板防止高度不够
        maxHeight = Math.ceil(maxHeight);
        sourceRow.setHeight((short)maxHeight);
    }
}
