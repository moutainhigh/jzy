package com.kaisa.kams.components.utils.pdfUtil;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.models.BaseModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/10/12.
 */
public class PdfTableUtil {
    private static BaseFont bfChinese = null;
    private static Font normalFont = null;
    private static Font titleFont1 = null;
    private static Font titleFont2 = null;
    private static Map<String,String> template = new HashMap<String,String>();
    private static final String TEMPLATE_FEE = "费用名称\t<BG>收费频率\t收费节点\t<BG>金额\n" +
            "[<NORMAL>${loanFeeTemps.feeType.getDescription()}\t${loanFeeTemps.feeCycle.getDescription()}\t<NORMAL>${loanFeeTemps.chargeNode.getDescription()}\t${loanFeeTemps.feeAmount()}元]\n";
    private static final String TEMPLATE_FLOW ="审批流程\t\t\t\n" +
            "流程节点名称\t<BG>审批结果\t用印审批\t<BG>审批人 | 审批意见\n" +
            "[<NORMAL>${approvalResults.nodeName}\t${approvalResults.approvalCode.getDescription()}           ${approvalResults.approvalTime()}\t<NORMAL>${approvalResults.getEnterpriseMsgForPDF()}\t ${approvalResults.userName} | ${approvalResults.content}]\n";
    private static final String TEMPLATE_FLOW_SIMPLE ="审批流程\t\t\t\n" +
            "流程节点名称\t<BG>审批结果\t<BG>审批人 | 审批意见\n" +
            "[<NORMAL>${approvalResults.nodeName}\t${approvalResults.approvalCode.getDescription()}           ${approvalResults.approvalTime()}\t<NORMAL>${approvalResults.userName} | ${approvalResults.content}]\n";
    private static final String TEMPLATE_BI= "业务单号\t${loan.code}\t\t\n" +
            "产品名称\t${productName}\t\t\n" +
            "类型\t${loanType}\t\t\n" +
            "渠道/业务员\t${channelsale}\t\t\n" +
            "填单人\t${loan.createBy}\t\t\n" ;
    private static final String TEMPLATE_BORROWER= "借款人信息\t\t\t\n"+
            "借款人\t${loanBorrower.name}\t证件号码\t${borrower.certifType.getDescription()}/${borrower.certifNumber}\n" +
            "手机号码\t${loanBorrower.phone}\t家庭住址\t${loanBorrower.address}\n" ;
    private static final String TEMPLATE_LOAN =  "产品信息\t\t\t\n" +
            "申请金额（元）\t${loan.amount}\t借款期限\t${loan.termType2()}\n" +
            "还款方式\t${loan.repayMethod.getDescription()}\t借款利息\t${loan.getPercentageInterestRate()}/${loan.termType1()}${loan.loanLimitType()}\n"+
            "最小利息金额（元）\t${loan.minInterestAmount}\t\t\n"  ;
    static {
        try {
            bfChinese =  BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            normalFont = new Font(bfChinese,12,Font.NORMAL);
            titleFont1 =  new Font(bfChinese,20,Font.BOLD);
            titleFont2 =  new Font(bfChinese,14,Font.BOLD);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        template.put(ProductTempType.BAOLI.name(),"业务审批单\n"+
                TEMPLATE_BI+
                "借款人信息\t\t\t\n" +
                "融资主体名称\t${loanBorrower.name}\t证件号码\t${borrower.certifType.getDescription()}/${borrower.certifNumber}\n" +
                "法人姓名\t${borrower.legalPerson}\t法人身份证\t${borrower.legalPersonCertifNumber}\n" +
                "产品信息\t\t\t\n" +
                "融资申请金额（元）\t${loan.amount}\t借款期限\t${loan.termType2()}\n" +
                "还款方式\t${loan.repayMethod.getDescription()}\t借款利息\t${loan.getPercentageInterestRate()}/${loan.termType1()}${loan.loanLimitType()}\n"+
                "融资比例%\t${businessInfo.amount_scale}%\t宽限期\t${loan.grace}天\n" +
                 TEMPLATE_FEE+
                "资产包信息\t\t\t\n" +
                "交易买方名称\t${businessInfo.name}\t交易买方营业执照\t${businessInfo.license}\n" +
                "交易买方法人代表\t${businessInfo.agent_name}\t应收账款金额（元）\t${businessInfo.re_amount}\n" +
                "应收账款合同编号\t${businessInfo.agree_number}\t\t\n" +
                "<BG>买方企业简介\t\t\t\n" +
                "<NORMAL>${businessInfo.qy_content}\t\t\t\n");
        template.put(ProductTempType.SHULOU.name(),"业务审批单\n" +
                TEMPLATE_BI.replace("${productTypeName}","赎楼贷")+
                 TEMPLATE_BORROWER+
                TEMPLATE_LOAN+
                TEMPLATE_FEE+
                "收款账户\t\t\t\n" +
                "[户名\t${borrowerAccounts.bank}\t收款账户\t${borrowerAccounts.name}\n" +
                "开户行\t${borrowerAccounts.account}\t收款金额\tt${borrowerAccounts.amount}]\n" +
                "业务信息\t\t\t\n" +
                "[<BG>房产信息\t\t\t\n" +
                "房产证号\t${houseList.code}\t权属人\t${houseList.ower}\n" +
                "房产面积㎡\t${houseList.area}\t房产地址\t${houseList.address}\n" +
                "房产估值\t${houseList.price}\t估值渠道\t${houseList.channel}]\n");
        template.put(ProductTempType.GERENDAI.name(),"业务审批单\n"+
                TEMPLATE_BI.replace("${productTypeName}","信用贷")+
                TEMPLATE_BORROWER+
                TEMPLATE_LOAN+
                TEMPLATE_FEE);
        template.put(ProductTempType.PIAOJU.name(),"业务审批单\n"+
                TEMPLATE_BI.replace("${productTypeName}","票据融资").replace("渠道/业务员\t${channelsale}\t\t\n","")+
                "贴现人信息\t\t\t\n" +
                "贴现人\t${loanBorrower.name}\t营业执照\t${borrower.certifNumber}\n" +
                "法人代表人\t${borrower.legalRepresentative}\t联系方式\t${borrower.legalRepresentativePhone}\n" +
                "联系人\t${borrower.linkman}\t联系方式\t${borrower.linkmanPhone}\n" +
                "住所\t${borrower.residence}\t\t\n" +
                "票据信息\t\t\t\n" +
                "[票号\t${billLoanRepayList.billNo}\t出票日期\t${billLoanRepayList.drawTime()}\n" +
                "付款人\t${billLoanRepayList.payer}\t收款人\t${billLoanRepayList.payee}\n" +
                "出票金额（元）\t${billLoanRepayList.loanRepay.amount}\t贴现利息（元）\t${billLoanRepayList.loanRepay.interest}\n" +
                "到期日\t${billLoanRepayList.loanRepay.dueDate()}\t付款人开户行\t${billLoanRepayList.bankName}\n" +
                "调整天数(自然日)\t${billLoanRepayList.overdueDays}\t\t]\n" +
                "付款信息\t\t\t\n" +
                "票面总金额（元）\t${billLoan.totalAmount}\t贴现利息（元）\t${billLoan.interest}\n" +
                "付款金额（元）\t${billLoan.loan.amount}\t贴现日期\t${billLoan.discountTime()}\n" +
                "户名\t${billLoan.accountName}\t开户行\t${billLoan.accountBank}\n" +
                "账号\t${billLoan.accountNo}\t\t\n");
        template.put(ProductTempType.YINPIAO.name(),template.get(ProductTempType.PIAOJU.name()));
        template.put(ProductTempType.HONGBEN.name(),"业务审批单\n"+
                TEMPLATE_BI.replace("${productTypeName}","房抵贷")+
                TEMPLATE_BORROWER+
                "收款账户\t\t\t\n" +
                "[户名\t${borrowerAccounts.name}\t收款账户\t${borrowerAccounts.account}\n" +
                "开户行\t${borrowerAccounts.bank}\t收款金额（元）\t${borrowerAccounts.amount}]\n" +
                TEMPLATE_LOAN+
                TEMPLATE_FEE+
                "\t\t\t\n" +
                "业务信息\t\t\t\n" +
                "<BG>房产信息\t\t\t\n" +
                "[房产证号\t${houseList.code}\t权属人\t${houseList.ower}\n" +
                "与借款人关系\t${houseList.relation()}\t房产名称\t${houseList.houseName}\n" +
                "房产地址\t${houseList.address}\t房产面积㎡\t${houseList.area}\n" +
                "房产估值（万元）\t${houseList.price}\t估值渠道\t${houseList.channel}]\n");
        template.put(ProductTempType.CHEDAI.name(),"业务审批单\n"+
                TEMPLATE_BI.replace("${productTypeName}","车押贷")+
                TEMPLATE_BORROWER+
                "产品信息\t\t\t\n" +
                "申请金额（元）\t${loan.amount}\t借款期限\t${loan.termType2()}\n" +
                "还款方式\t${loan.repayMethod.getDescription()}\t借款利息\t${loan.getPercentageInterestRate()}/${loan.termType1()}${loan.loanLimitType()}\n"+
                TEMPLATE_FEE+
                "业务信息\t\t\t\n" +
                "<BG>车贷信息\t\t\t\n" +
                "车牌号码\t${baseInfo.car_number}\t车辆品牌\t${baseInfo.car_type}\n" +
                "车辆估值（元）\t${baseInfo.car_value}\t\t\n");
        template.put(ProductTempType.JIEYA.name(),"房产解押审批单\n" +
                "房产解押编号\t${nomortgage.applyCode}\t\t\n" +
                "业务类型\t${nomortgage.addressType.getDescription()}\t\t\n" +
                "类型\t${nomortgage.channel()}\t\t\n" +
                "渠道/业务员\t${nomortgage.businessSource()}\t\t\n" +
                "填单人\t${nomortgage.createBy}\t\t\n" +
                "业务信息\t\t\t\n" +
                "业务单号\t${nomortgage.businessCode}\t\t\n" +
                "借款金额（元）\t${loanAmount}\t结清日期\t${clearDate}\n" +
                "房产信息\t\t\t\n" +
                "[房产证号\t${houseInfoList.code}\t房产地址\t${houseInfoList.address}\n" +
                "房产面积（㎡）\t${houseInfoList.area}\t\t\n" +
                "<权利人\t${houseInfoList.owerList.name}\t${houseInfoList.owerList.type}\t${houseInfoList.owerList.idNumber}>\n" +
                "抵押登记编号\t${houseInfoList.warrantNumber}\t\t]\n");
        template.put(ProductTempType.DIYA.name(),"房产抵押审批单\n" +
                "房产抵押编号\t${mortgage.mortgageCode}\t\t\n" +
                "业务类型\t${mortgage.houseMortgageType.getDescription()}房产抵押\t\t\n" +
                "类型\t${mortgage.channel.getDescription()}\t\t\n" +
                "渠道/业务员\t${mortgage.businessSource()}\t\t\n" +
                "填单人\t${mortgage.createBy}\t\t\n" +
                "房产信息\t\t\t\n" +
                "[房产证号\t${mortgageHouseList.house.housePropertyNumber}\t房产地址\t${mortgageHouseList.house.address}\n" +
                "房产面积（㎡）\t${mortgageHouseList.house.area}\t借款利息\t${mortgageHouseList.loanInterestRate()}%/月\n" +
                "最高借款金额（元）\t${mortgageHouseList.maximumLoanAmount}\t借款时间\t${mortgageHouseList.startBorrowingTime()} ~ ${mortgageHouseList.endBorrowingTime()}\n" +
                "<权利人\t${mortgageHouseList.house.equityHolderList.name}\t${mortgageHouseList.house.equityHolderList.getCertifTypeCN()}\t${mortgageHouseList.house.equityHolderList.certificateNo}>]\n");
        template.put(ProductTempType.BROKERAGEFEE.name(),"居间费付费审批单\n"+
                "业务单号\t${loan.code}\t\t\n" +
                "放款申请编号\t${intermediaryApply.applyCode}\t\t\n" +
                "产品子类\t${product.name}\t\t\n" +
                "填单人\t${loan.createBy}\t\t\n" +
                "居间人信息\t\t\t\n" +
                "居间人\t${intermediaryApply.name}\t证件号码\t身份证/${intermediaryApply.idNumber}\n" +
                "手机号码\t${intermediaryApply.phone}\t家庭住址\t${intermediaryApply.address}\n" +
                "居间总费用（元）\t${intermediaryApply.intermediaryFee()}\t代扣代缴税费（元）\t${billLoan.withHoldingTaxFee}\n" +
                "税后居间费（元）\t${billLoan.afterTaxIntermediaryFee}\t\t\n" +
                "收款账户\t\t\t\n" +
                "收款人\t${intermediaryApply.name}\t开户行\t${intermediaryApply.bank}\n" +
                "账号\t${intermediaryApply.account()}\t\t\n" +
                "业务信息\t\t\t\n" +
                "票面总金额（元）\t${billLoan.totalAmount}\t贴现利息（元）\t${billLoan.interest}\n" +
                "付款金额（元）\t${loan.amount}\t贴现日期\t${billLoan.discountTime()}\n");

    }

    private static String getTemplate(ProductTempType productTempType){
        String temp = template.get(productTempType.name());
        if(org.apache.commons.lang.StringUtils.isNotEmpty(temp)){
            return temp;
        }else{
            return productTempType.name()+"模板不需要用印\t\t\t";
        }

    }
    public static PdfPTable getPdfTable(ProductTempType productTempType,Object object )throws Exception{
        String temp =getTemplate(productTempType);
        PdfPTable table=new PdfPTable(4);
        table.setTotalWidth(new float[]{150,300,150,300});
        matchTemplateValue(temp,object,table);
        return table;
    }
    public static PdfPTable getPdfFlowTable(ProductTempType productTempType,Object object )throws Exception{
        PdfPTable table=null;
        String temp = null;
        if("BROKERAGEFEEDIYAJIEYA".contains(productTempType.name())){
            temp = TEMPLATE_FLOW_SIMPLE;
            table=new PdfPTable(3);
            table.setTotalWidth(new float[]{200,200,200});
        }else{
            temp = TEMPLATE_FLOW;
            table=new PdfPTable(4);
            table.setTotalWidth(new float[]{200,200,200,200});
        }
        matchTemplateValue(temp,object,table);
        return table;
    }
    private static void addCellToTable(PdfPTable table , PdfTableCell pdfTableCell){
        com.itextpdf.text.pdf.PdfPCell cell=mergeCol(pdfTableCell.getText(), getFont(pdfTableCell.getFontType()),pdfTableCell.getMergeCol() );
        if(pdfTableCell.isNeedBg()){
            cell.setBackgroundColor(new BaseColor(191,191,191));
        }
        table.addCell(cell);
    }
    private static String getShowInfomation(String text,Object object)throws Exception{
        while (text.contains("${")){
            String key = text.substring(text.indexOf("${")+2,text.indexOf("}"));
            String value = getStringByReflect(key,0,object);
            text = text.replace("${"+key+"}",value);
        }
        return text;
    }

    private static Object getObjectByReflect(String key,int currentKeyIndex,Object object)throws Exception{
        if(object==null)return "";
        Object obj = null;
        if(object instanceof Map){
            obj = ((Map)object).get(key.split("\\.")[currentKeyIndex]);
        }else{
            obj =  ObjectUtil.getValueFromObject(key.split("\\.")[currentKeyIndex],object);
        }
        if(obj!=null&&(obj instanceof BaseModel||obj.getClass().isEnum())||obj instanceof Map){
            return getObjectByReflect(key,++currentKeyIndex,obj);
        }else{
            return obj;
        }
    }
    private static String getStringByReflect(String key,int currentKeyIndex,Object object)throws Exception{
        Object obj = getObjectByReflect(key,currentKeyIndex,object);
        return obj!=null?obj.toString():"";
    }
    private static void matchLineValue(String line ,Object object,PdfPTable table)throws Exception{
        if(line.equals("\t\t\t")){
            return;
        }else if(line.contains("\t\t\t")){
            addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[0],object),4,FontType.TITLE2,false));
        }else if(line.contains("\t\t")){
            addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[0],object),1,FontType.NORMAL,true));
            addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[1],object),3,FontType.NORMAL,false));
        }else if(line.contains("\t")){
            addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[0],object),1,FontType.NORMAL,true));
            addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[1],object),1,FontType.NORMAL,false));
            if(line.split("\t").length>3){
                addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[2],object),1,FontType.NORMAL,true));
                addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[3],object),1,FontType.NORMAL,false));
            }else{
                addCellToTable(table,new PdfTableCell(getShowInfomation(line.split("\t")[2],object),1,FontType.NORMAL,false));
            }

        }else{
            addCellToTable(table,new PdfTableCell(getShowInfomation(line,object),4,FontType.TITLE1,false));
        }
    }
    private static void matchTemplateValue(String template,Object object,PdfPTable table )throws Exception{
        List<String> grouplist = new ArrayList<String>();
        for(String line : template.split("\n")){
            if(line.endsWith("]")){
                String subline = line.substring(line.indexOf("[")+1,line.length()-1);
                grouplist.add(subline);
                String listkey = subline.substring(subline.indexOf("${")+2,subline.indexOf("}")).split("\\.")[0];
                Object listobj   = getObjectByReflect(listkey,0,object);
                if(listobj!=null){
                    List list = (List)listobj;
                    for(Object obj : list){
                        for(String subline1: grouplist){
                            if(subline1.matches("^\\<.+\\>$")){//如果能匹配上<***>则为数组里面的数组，这么处理很low的，如果数组里面再嵌套数组就不行了。所以最好是做成通用解析数组迭代形式，不过也没必要做成很灵活的配置，只有这里会用的到，就不浪费时间了
                                String subkey = subline1.substring(subline1.indexOf("${")+2,subline1.indexOf("}"));
                                String sublistkey = subkey.substring(subkey.indexOf(".")+1,subkey.lastIndexOf("."));
                                Object sublistObj = getObjectByReflect(sublistkey,0,obj);
                                if(sublistObj!=null){
                                    List subList = (List)sublistObj;
                                    for(Object subobj : subList){
                                        String subline2 = subline1.substring(subline1.indexOf("<")+1,subline1.indexOf(">")).replace(listkey+"."+sublistkey+".","");
                                        matchLineValue(subline2,subobj,table);
                                    }
                                }
                            }else{
                                subline1 = subline1.replace(listkey+".","");
                                matchLineValue(subline1,obj,table);
                            }

                        }
                    }
                }
                grouplist.clear();
            }else if(line.startsWith("[")){
                String subline = line.substring(1,line.length());
                grouplist.add(subline);
            }else{
                if(grouplist.size()>0){
                    grouplist.add(line);
                }else{
                    matchLineValue(line,object,table);
                }
            }
        }
    }

    //合并行的静态函数
    private static PdfPCell mergeRow(String str, Font font, int i) {

        //创建单元格对象，将内容及字体传入
        PdfPCell cell=new PdfPCell(new Paragraph(str,font));
        //设置单元格内容居中
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //将该单元格所在列包括该单元格在内的i行单元格合并为一个单元格
        cell.setRowspan(i);

        return cell;
    }

    private static Font getFont(FontType fontType){
        if(fontType.equals(FontType.NORMAL)){
            return normalFont;
        }else if(fontType.equals(FontType.TITLE1)){
            return titleFont1;
        }else if(fontType.equals(FontType.TITLE2)){
            return titleFont2;
        }
        return normalFont;
    }

    //合并列的静态函数
    private static PdfPCell mergeCol(String str,Font font,int i) {

        PdfPCell cell=new PdfPCell(new Paragraph(str,font));
        cell.setMinimumHeight(25);
        if(font==normalFont){
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }else{
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //将该单元格所在行包括该单元格在内的i列单元格合并为一个单元格
        cell.setColspan(i);

        return cell;
    }

    //获取指定内容与字体的单元格
    private static PdfPCell getPDFCell(String string, Font font)
    {
        //创建单元格对象，将内容与字体放入段落中作为单元格内容
        PdfPCell cell=new PdfPCell(new Paragraph(string,font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //设置最小单元格高度
        cell.setMinimumHeight(25);
        return cell;
    }
}
