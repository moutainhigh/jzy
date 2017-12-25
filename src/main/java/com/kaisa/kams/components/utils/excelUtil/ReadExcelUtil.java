
package com.kaisa.kams.components.utils.excelUtil;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.mvc.upload.TempFile;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author luoyj
 * @date 2017-04-19
 */
public class ReadExcelUtil {
    //总行数
    private int totalRows = 0;
    //总条数
    private int totalCells = 0;
    //错误信息接收器
    private String errorMsg;
    //构造方法
    public ReadExcelUtil(){}
    //获取总行数
    public int getTotalRows()  { return totalRows;}
    //获取总列数
    public int getTotalCells() {  return totalCells;}
    //获取错误信息
    public String getErrorInfo() { return errorMsg; }

    /**
     * 读EXCEL文件，获取信息集合
     * @param
     * @return
     */
    public List<ExcelChannelTmplView> getExcelInfo(TempFile mFile) {
        String fileName = mFile.getName();//获取文件名
        List<ExcelChannelTmplView> list=null;
        try {
//            if (!validateExcel(fileName)) {// 验证文件名是否合格
//                return null;
//            }
//            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
//            if (isExcel2007(fileName)) {
//                isExcel2003 = false;
//            }
            list = createExcel(mFile.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public void printEntity(TempFile mFile)throws Exception{
        Workbook rwb = null;
        rwb = new XSSFWorkbook(mFile.getInputStream());

        Sheet sheet=rwb.getSheetAt(0);//或者rwb.getSheet(0)
        String code="";
        this.totalRows = sheet.getPhysicalNumberOfRows();
        // 得到Excel的列数(前提是有行数)
        if (totalRows > 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        for(int k=0;k<totalCells;k++){
            String temp = "     /**\n" +
                    "     * ${name}\n" +
                    "     */\n" +
                    "    @Column(\"code\")\n" +
                    "    ${define}\n" +
                    "    private ${type} code;";
            Cell cell = sheet.getRow(0).getCell(k);
            Cell cellType = sheet.getRow(1).getCell(k);
            String type = "";
            String define = "";
            String name = cell.getStringCellValue();
            if(HSSFCell.CELL_TYPE_NUMERIC==cell.getCellType()||HSSFCell.CELL_TYPE_FORMULA==cell.getCellType()){
                if (HSSFDateUtil.isCellDateFormatted(cell)){
                    type = "Date";
                    define = "";
                }else{
                    type = "int";
                    define = "";
                }

            }else if(HSSFCell.CELL_TYPE_STRING==cell.getCellType()){
                type = "String";
                define = "@ColDefine(type = ColType.VARCHAR, width=50)";
            }
            System.out.println(temp.replace("${name}",name).replace("${type}",type).replace("${define}",define));
        }
    }

    //解析excel文档，并且保存为hashmap键值对形式存入arrayList
    public List<HashMap<String,Object>> getExcelInfoToMap(TempFile mFile)throws Exception {
        String fileName = mFile.getName();//获取文件名
        List<HashMap<String,Object>> datalist = new ArrayList<HashMap<String,Object>>();

        Workbook rwb = null;
        rwb = new XSSFWorkbook(mFile.getInputStream());

        Sheet sheet=rwb.getSheetAt(0);//或者rwb.getSheet(0)
        // 得到Excel的行数
        this.totalRows = sheet.getPhysicalNumberOfRows();
        // 得到Excel的列数(前提是有行数)
        if (totalRows > 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        System.out.println(" rows:"+totalRows+" cols:"+totalCells);
        List<String> titleList  = new ArrayList<String>();
        if(totalRows>0){
            for(int k=0;k<totalCells;k++){
                if(sheet.getRow(0).getCell(k)!=null){
                    titleList.add(sheet.getRow(0).getCell(k).getStringCellValue());
                }else{
                    titleList.add("col"+(k+1));
                }
            }
        }else{
            throw new Exception("上传文件内容为空，请不要开玩笑");
        }
        for (int i = 1; i < totalRows; i++) {
            HashMap<String,Object> datamap =  new HashMap<String,Object>();
            boolean flag  = false;
            for (int j = 0; j < totalCells; j++) {
                Object obj =  null;
                Cell cell = sheet.getRow(i).getCell(j);
                try{
                    obj = getValueFromCell(cell);
                }catch(Exception e){
                    e.printStackTrace();
                    throw new Exception((cell!=null?cell.getCellType():"")+"第"+(i+1)+"行，第'"+titleList.get(j)+"'列，值为'"+(cell!=null?cell.getStringCellValue():" ")+"'，异常信息："+e.getMessage());
                }
                if(obj!=null)flag = true;
                datamap.put(titleList.get(j),obj);
             //   System.out.print(getValueStringFromCell(sheet.getRow(i).getCell(j))+"\t");
            }
            if(flag)datalist.add(datamap);
           // System.out.println("row:"+i+" \t");
        }
        return datalist;
    }

    private Object getValueFromCell(Cell cell){
        Object obj = null;
        if(cell !=null){
            switch (cell.getCellType())
            {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA:
                {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell))
                    {
                        // 如果是Date类型则，取得该Cell的Date值
                        obj = cell.getDateCellValue();
                    }
                    // 如果是纯数字
                    else
                    {
                        // 取得当前Cell的数值
                       /* double num = new Double((double)cell.getNumericCellValue());
                        cellvalue = String.valueOf(myformat.format(num));*/
                        HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
                        String str = dataFormatter.formatCellValue(cell);
                        if(str.contains("%")){
                            str = str.replace("%","");
                            obj = new BigDecimal(str.replace(",","").trim()).divide(new BigDecimal(100),12,BigDecimal.ROUND_HALF_EVEN);
                            //obj = BigDecimal.valueOf(Double.parseDouble(str.replace(",","").trim())/100.0);
                        }else{
                            obj = BigDecimal.valueOf(Double.parseDouble(str.replace("*","").replace(",","").replace("¥","").trim()));
                        }
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    obj = cell.getStringCellValue().replaceAll("'", "''");
                    break;
                // 默认的Cell值
                default:
                   obj = null;
            }
        }else {
            obj =  null;
        }
        return obj;
    }

    private String getValueStringFromCell(Cell cell){
        // 判断当前Cell的Type
        String cellvalue = "";
        if (cell != null) {
            switch (cell.getCellType())
            {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA:
                {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell))
                    {
                        // 如果是Date类型则，取得该Cell的Date值
                        Date date = cell.getDateCellValue();
                        // 把Date转换成本地格式的字符串
                        cellvalue = cell.getDateCellValue().toLocaleString();
                    }
                    // 如果是纯数字
                    else
                    {
                        // 取得当前Cell的数值
                       /* double num = new Double((double)cell.getNumericCellValue());
                        cellvalue = String.valueOf(myformat.format(num));*/
                        HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
                        cellvalue = dataFormatter.formatCellValue(cell);

                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getStringCellValue().replaceAll("'", "''");
                    break;
                // 默认的Cell值
                default:
                    cellvalue = "找不到对应的类型";
            }
        }else {
            cellvalue = "";
        }
        return cellvalue;
    }

    /**
     * 根据excel里面的内容读取客户信息
     * @param is 输入流
     * @return
     * @throws IOException
     */
    public List<ExcelChannelTmplView> createExcel(InputStream is) {
        List<ExcelChannelTmplView> list=null;
        try{
            Workbook wb = null;
//            if (isExcel2003) {// 当excel是2003时,创建excel2003
//                wb = new HSSFWorkbook(is);
//            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(is);
//            }
            list= getAllByExcel(wb);// 读取Excel里面客户的信息
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
     * 查询指定目录中电子表格中所有的数据
     * @return
     */
    public  List<ExcelChannelTmplView> getAllByExcel(Workbook rwb){
        List<ExcelChannelTmplView> list=new ArrayList<ExcelChannelTmplView>();
        try {
            Sheet sheet=rwb.getSheetAt(0);//或者rwb.getSheet(0)
            // 得到Excel的行数
            this.totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            System.out.println(totalCells+" rows:"+totalRows);
            String code="";
            for (int i = 1; i < totalRows; i++) {
                for (int j = 0; j < totalCells; j++) {
                    //第一个是列数，第二个是行数
                    String id=sheet.getRow(i).getCell(j++).getStringCellValue();//默认最左边编号也算一列 所以这里得j++
                    String name=sheet.getRow(i).getCell(j++).getStringCellValue();
                    Cell r= sheet.getRow(i).getCell(j++);
                    if(null!=r){
                         code=r.getStringCellValue();
                    }else {
                         code="";
                    }
                    String manager=sheet.getRow(i).getCell(j++).getStringCellValue();
                    System.out.println("id:"+id+" name:"+name+" code:"+code+" manager:"+manager);
                    list.add(new ExcelChannelTmplView(id, name, code,manager));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
     * 验证EXCEL文件
     *
     * @param filePath
     * @return
     */
    public boolean validateExcel(String filePath) {
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
            errorMsg = "文件名不是excel格式";
            return false;
        }
        return true;
    }

    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    //@描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath)  {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }
    public static void main(String[] args) {
        ReadExcelUtil r=new ReadExcelUtil();
        //得到表格中所有的数据
       // List<ExcelChannelTmplView> listExcel=r.getAllByExcel("d://sl_channel.xlsx");
        /*//得到数据库表中所有的数据
		List<StuEntity> listDb=StuService.getAllByDb();*/
    }

}
