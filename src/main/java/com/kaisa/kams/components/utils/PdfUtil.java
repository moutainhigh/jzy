package com.kaisa.kams.components.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.components.utils.pdfUtil.PdfTableUtil;
import com.kaisa.kams.enums.MortgageDocumentType;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.models.BaseModel;
import com.kaisa.kams.models.Mortgage;
import org.apache.commons.lang.*;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhouchuang on 2017/6/14.
 */
public class PdfUtil {
    public static BaseFont bfChinese = null;
    static {
        try {
            bfChinese =  BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Font titlefont=new Font(bfChinese,16,Font.BOLD);
    public static Font textfont=new Font(bfChinese,12,Font.NORMAL);
    public static Font underLineFont = new Font(bfChinese,12,Font.UNDERLINE);
    public static Font italicFont = new Font(bfChinese,12,Font.ITALIC);
    public static Font boldFont = new Font(bfChinese,12,Font.BOLD);

    public static String getStringFromRule(String key,Object obj)throws  Exception{
        if(key.matches("S\\d+")){
            int num = Integer.parseInt(key.substring(1));
            return "                                                                                                                                                  ".substring(0,num*4);
        }else{
            return ObjectUtil.getStringFromObject(key,obj);
        }
    }
    public static List<Paragraph> assembleParagraph(List<String> list , Object clearedProof)throws  Exception{
        List<Paragraph> paragraphs = new ArrayList<Paragraph>();
        Paragraph paragraph  = new Paragraph();
        paragraphs.add(paragraph);
        for(String str : list){
            if(str.startsWith("${")){
                Chunk chunk = new Chunk(getStringFromRule(str.substring(str.indexOf("${")+2,str.indexOf("}")),clearedProof),getFontByType(str.substring(str.length()-1,str.length())));
                paragraph.add(chunk);

            }else if(str.startsWith("$[")){
                String listname = str.substring(str.indexOf("${")+2,str.indexOf("}")).split("\\.")[0];
                List looplist = null;
                if( clearedProof instanceof  Map){
                    Map map = (Map)clearedProof;
                    looplist =(List) map.get(listname);
                }else{
                    Field loopField = clearedProof.getClass().getDeclaredField(listname);
                    loopField.setAccessible(true);
                    looplist =(List) loopField.get(clearedProof);
                }
                for(int i=0;i<looplist.size();i++){
                    Object map = looplist.get(i);
                    List<String> sublist = getGroup((list.get(0).equals(INDENT)?INDENT:"")+str.substring(str.indexOf("$[")+2,str.indexOf("]")));
                    for(int j =0;j<sublist.size();j++){
                        String subStr = sublist.get(j);
                        if(subStr.startsWith("${")) {
                           /* if(subStr.startsWith("${NR")){
                                paragraph  = new Paragraph();
                                paragraphs.add(paragraph);
                                if(subStr.contains("NRS")){
                                    Chunk chunk = new Chunk( getStringFromRule(subStr.substring(4,subStr.length()-2),map),getFontByType(subStr.substring(subStr.length()-2)));
                                    paragraph.add(chunk);
                                }
                            }*/
                            if(subStr.startsWith("${END")){
                                if((i==looplist.size()-1)&&(j==sublist.size()-1)){
                                    continue ;
                                }
                                if(subStr.contains("NR")){
                                    paragraph  = new Paragraph();
                                    paragraphs.add(paragraph);
                                    if(subStr.contains("NRS")){
                                        Chunk chunk = new Chunk( getStringFromRule(subStr.substring(7,subStr.length()-2),map),getFontByType(subStr.substring(subStr.length()-2)));
                                        paragraph.add(chunk);
                                    }
                                }else{
                                    Chunk chunk = new Chunk(subStr.substring(5,subStr.length()-2),textfont);
                                    paragraph.add(chunk);
                                }
                            }
                            else{
                                Chunk chunk = new Chunk(getStringFromRule(subStr.substring(subStr.indexOf("${") + 2, subStr.indexOf("}")).split("\\.")[1], map),getFontByType(subStr.substring(subStr.length()-1,subStr.length())));
                                paragraph.add(chunk);
                            }
                        }else{
                           /* if((i==looplist.size()-1)&&(j==sublist.size()-1)){
                                subStr = subStr.substring(0, subStr.length() - 1);
                            }*/
                            Chunk chunk = new Chunk(subStr,textfont);
                            paragraph.add(chunk);
                        }
                    }
                }
            }else{
                Chunk chunk = new Chunk(str,textfont);
                paragraph.add(chunk);

            }

        }
        return paragraphs;
    }
    public static Font getFontByType(String type){
        if("N".equals(type)){
            return textfont;
        }else if("B".equals(type)){
            return boldFont;
        }else if("I".equals(type)){
            return italicFont;
        }else if("U".equals(type)){
            return underLineFont;
        }else{
            return textfont;
        }
    }
    public static boolean isArrayFlag (int starts,int starto){
        if(Math.min(starts,starto)>0){
            return starts<starto;
        }else{
            return starts>0;
        }
    }
    public static List<String> getGroup(String linestr){
        List<String> list = new ArrayList<>();
        int starto = linestr.indexOf("${");
        int starts = linestr.indexOf("$[");
        int end = 0;
        int index = 0;
        boolean hasFlag = false;
        while(starto > -1||starts>-1){
            hasFlag = true;
            if(isArrayFlag(starts,starto)){//数组
                if(index<starts)
                    list.add(linestr.substring(index,starts));
                index = starts;
                end = linestr.indexOf("]",index)+1;
            }else{//对象
                if(index<starto)
                    list.add(linestr.substring(index,starto));
                index = starto;
                end = linestr.indexOf("}",index)+2;
            }
            list.add(linestr.substring(index,end));
            starto = linestr.indexOf("${",end);
            starts = linestr.indexOf("$[",end);
            index = end;
        }
        if(hasFlag==false){
            list.add(linestr);
        }else if(-1==Math.max(starto,starts)){
            if(index<linestr.length()-1)
                list.add(linestr.substring(index));
        }
        return list;
    }
    public static void generalPdf(HttpServletResponse response, MortgageDocumentType mortgageDocumentType, Object data)throws Exception{
        PdfReader reader = null;
        PdfStamper ps = null;
        OutputStream outputStream = null;
        try {
            response.setContentType("application/pdf");
            String fileName = new String(mortgageDocumentType.getDescription().getBytes("GB2312"), "ISO_8859_1");
            //response.addHeader("content-type", "application/shlnd.ms-excel;charset=utf-8");
            response.addHeader("content-disposition", "filename="+fileName+".pdf");
            outputStream = response.getOutputStream();
            reader = new PdfReader( "/data0/java/pdftemp" + File.separator + mortgageDocumentType.name() + ".pdf");
            ps = new PdfStamper(reader,outputStream);
            BaseFont bf = BaseFont.createFont("/data0/java/pdftemp" +"/simsun.ttc,1",
                    BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
            ArrayList<BaseFont> fontList = new ArrayList<BaseFont>();
            fontList.add(bf);
            fontList.add(bfChinese);
            AcroFields s = ps.getAcroFields();
            s.setSubstitutionFonts(fontList);
            fullValue(s,data,"");
            ps.setFormFlattening(true);
        }catch (Exception e){
            e.printStackTrace();
            throw new  Exception(e.getMessage());
        }finally {
            ps.close();
            reader.close();
        }

    }

    public static void fullValue(AcroFields acroFields , Object data ,String path){
        String currentPath  = new String(path);
        try{
            if(data instanceof BaseModel){
                Method[] methods = data.getClass().getDeclaredMethods();
                for(Method method : methods){
                    String key = currentPath+(StringUtils.isNotEmpty(currentPath)?".":"")+method.getName()+"()";
                    acroFields.getFields().forEach((n3,v3)->{
//                        if(n3.toString().contains(key)){
                        if(contains(key,n3.toString())){
                            try{
                                String old = acroFields.getField(n3.toString());
                                String flag =  StringUtils.isNotEmpty(old)?n3.replaceAll("[\\w\\.\\-_\\(\\)]","").trim().substring(0,1):"";
                                method.setAccessible(true);
                                if(",".equals(flag)||"，".equals(flag))
                                    acroFields.setField(n3.toString(),old+(StringUtils.isNotEmpty(old)?"、":"")+method.invoke(data,null));
                                else
                                    acroFields.setField(n3.toString(),old+(StringUtils.isNotEmpty(old)?flag:"")+method.invoke(data,null));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }

                Field[] fields = data.getClass().getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true);
                    if(field.get(data)!=null){
                        Object obj = field.get(data);
                        if(obj instanceof  List || obj instanceof Map || obj instanceof BaseModel ){
                            fullValue(acroFields,obj,currentPath+(StringUtils.isNotEmpty(currentPath)?".":"")+field.getName());
                        }else {
                            String key = currentPath+(StringUtils.isNotEmpty(currentPath)?".":"")+field.getName();
                            acroFields.getFields().forEach((n2,v2)->{
//                                if(n2.toString().contains(key)){
                                if(contains(key,n2.toString())){
                                    try{
                                        String old = acroFields.getField(n2.toString());
                                        String flag = StringUtils.isNotEmpty(old)?n2.replaceAll("[\\w\\.\\-_\\(\\)]","").trim().substring(0,1):"";
                                        if(",".equals(flag)||"，".equals(flag))
                                            acroFields.setField(n2.toString(),old+(StringUtils.isNotEmpty(old)?"、":"")+obj.toString());
                                        else
                                            acroFields.setField(n2.toString(),old+(StringUtils.isNotEmpty(old)?flag:"")+obj.toString());
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                            //acroFields.setField(currentPath+ (StringUtils.isNotEmpty(currentPath)?".":"")+field.getName(),field.get(data).toString());
                        }
                    }
                }
            }else if(data instanceof  List){
                if(data!=null){
                    List list  = (List)data;
                    for(int i=0;i<list.size();i++){
                        Object obj = list.get(i);
                        fullValue(acroFields,obj,currentPath+i);
                    }
                }
            }else if(data instanceof Map){
                if(data!=null){
                    Map map = (HashMap)data;
                    map.forEach((k,v)->{
                        if(v!=null){
                            if(v instanceof List || v instanceof Map|| v instanceof BaseModel){
                                fullValue(acroFields,v,currentPath+(StringUtils.isNotEmpty(currentPath)?".":"")+k);
                            }else{
                                String key = currentPath+(StringUtils.isNotEmpty(currentPath)?".":"")+k;
                                acroFields.getFields().forEach((n1,v1)->{
//                                    if(n1.toString().contains(key)){
                                    if(contains(key,n1.toString())){
                                        String old = acroFields.getField(n1.toString());
                                        try{
                                            String flag =  StringUtils.isNotEmpty(old)?n1.replaceAll("[\\w\\.\\-_\\(\\)]","").trim().substring(0,1):"";
                                            if(",".equals(flag)||"，".equals(flag))
                                                acroFields.setField(n1.toString(),old+(StringUtils.isNotEmpty(old)?"、":"")+v.toString());
                                            else
                                                acroFields.setField(n1.toString(),old+(StringUtils.isNotEmpty(old)?flag:"")+v.toString());
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static boolean contains(String key,String str){
        for(String s :str.split("[^\\w\\.\\-_\\(\\)]")){
           if(s.equals(key))return true;
        }
        return false;

    }

    public static void generalTableTypePdf(HttpServletResponse response, Object object,String title, ProductTempType productTempType, boolean needMark)throws Exception{
        Document document = new Document();
        try{
            setHeader(response, title, needMark, document);
            PdfPTable  table = PdfTableUtil.getPdfTable(productTempType,object);
            PdfPTable  flowTable =  PdfTableUtil.getPdfFlowTable(productTempType,object);
            document.add(table);
            document.add(flowTable);
        }catch(Exception e){
            e.printStackTrace();
        }
        document.close();
    }
    private static void setHeader(HttpServletResponse response, String titleName, boolean needMark, Document document) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        String fileName = new String(titleName.getBytes("GB2312"), "ISO_8859_1");
        //response.addHeader("content-type", "application/shlnd.ms-excel;charset=utf-8");
        response.addHeader("content-disposition", "filename="+fileName+".pdf");
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.addTitle(titleName);

        if(needMark){
            Image tImgCover = Image.getInstance(PdfUtil.class.getClassLoader().getResource("/").getPath()+"image"+ File.separator+"pdfbg.jpg");
            //Image tImgCover = Image.getInstance("D:/0911102509619kg4.jpg");
        /* 设置图片的位置 */
            tImgCover.setAbsolutePosition(0, 0);
         /* 设置图片的大小 */
            tImgCover.scaleAbsolute(595, 842);
            document.add(tImgCover);             //加载图片
        }
    }

    public static String INDENT  = "          ";
    public  static void generalPdf(HttpServletResponse response,Object object,String titleName,String contextTemp,boolean needMark)throws Exception{

        Document document = new Document();
        try{
            setHeader(response, titleName, needMark, document);
            Paragraph title = new Paragraph(titleName,titlefont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            String[] lineContext = contextTemp.split("\n");
            for(String linestr:lineContext){
                if(linestr.startsWith("\t\t\t\t\t\t")){
                    List<String> list = PdfUtil.getGroup(linestr);
                    List<Paragraph> rights  = PdfUtil.assembleParagraph(list,object);
                    for(Paragraph right : rights){
                        right.setLeading(25);
                        right.setAlignment(Element.ALIGN_RIGHT);
                        document.add(right);
                    }
                }else{
                    linestr  = linestr.replaceAll("\t\t",INDENT);
                    List<String> list = PdfUtil.getGroup(linestr);
                    List<Paragraph> texts = PdfUtil.assembleParagraph(list,object);
                    for(Paragraph text : texts){
                        text.setLeading(25);
                        document.add(text);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        document.close();
    }
}
