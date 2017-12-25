package com.kaisa.kams.models.history;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.IdcardValidator;
import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.excelUtil.ExcelBaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhouchuang on 2017/5/8.
 */
@Table("sl_product_import_hongben")
@Data
@NoArgsConstructor
public class ProductImportHongben  extends ExcelBaseModel {
    /**
     * 产品名称
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="产品名称",NonNull=true)
    @Column("productName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String productName;
    /**
     * 业务员姓名
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="业务员姓名",NonNull=true)
    @Column("businessName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String businessName;
    /**
     * 放款主体
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="放款主体",NonNull=true)
    @Column("loanSubject")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanSubject;
    public void loanSubject(Object object){
        if(object.toString().equals("富昌")){
            loanSubject = "深圳市富昌小额贷款有限公司";
        }else if(object.toString().equals("平台募集")){
            loanSubject = "佳兆业金服";
        }else{
            loanSubject = object.toString();
        }
    }
    /**
     * 渠道名称
     */
    @ExcelAssistant(titleName="渠道名称",NonNull=true)
    @Column("channel")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String channel;
    public void channel(Object object){
        if(object!=null){
            if(object.toString().equals("富盈金融")){
                channel = "前海富盈";
            }else if(object.toString().equals("帮房宜")){
                channel = "帮房宜（房抵）";
            }else if(object.toString().equals("金葵晟")){
                channel = "金葵晟（房抵）";
            }else if(object.toString().equals("元通")){
                channel = "元通金融";
            }else{
                channel = object.toString();
            }
        }
    }
    /**
     * 借款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="借款人",NonNull=true)
    @Column("borrower")
    @ColDefine(type = ColType.VARCHAR, width=512)
    private String borrower;
    public  void borrower(Object object){
        borrower = object.toString();
        borrower = borrower.replaceAll(" ",",").replaceAll("，",",").replaceAll(",+",",");
    }
    /**
     * 身份证号
     */
    @ExcelAssistant(titleName="身份证号",NonNull=true)
    @Column("idNumber")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    private String idNumber;
    public  void idNumber(Object object){
        idNumber = object.toString();
        idNumber = idNumber.replaceAll(" ",",").replaceAll("，",",").replaceAll(",+",",");
    }
    /**
     * 收款人
     */
    @ExcelAssistant(titleName="收款人",NonNull=true)
    @Column("payee")
    @ColDefine(type = ColType.VARCHAR, width=512)
    private String payee;
    public  void payee(Object object){
        payee = object.toString();
        payee = payee.replaceAll(" ",",").replaceAll("，",",").replaceAll(",+",",");
    }
    /**
     * 借款本金
     */
    @ExcelAssistant(titleName="借款本金",NonNull=true)
    @Column("loanPrincipal")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal loanPrincipal;
    /**
     * 借款期限
     */
    @ExcelAssistant(titleName="借款期限",NonNull=true)
    @Column("loanExtensionon")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanExtensionon;

    /**
     * 借款期限（单位）
     */
    @ExcelAssistant(titleName="借款期限（单位）",NonNull=true)
    @Column("loanExtensiononUnit")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanExtensiononUnit;

    /**
     * 借款期限（数量）
     */
    @ExcelAssistant(titleName="借款期限（数量）",NonNull=true)
    @Column("loanExtensiononNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanExtensiononNum;

    /**
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式",NonNull=true)
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;

    /**
     * 借款利率
     */
    @ExcelAssistant(titleName="借款利率",NonNull=true)
    @Column("loanRate")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanRate;

    /**
     * 借款利率（单位）
     */
    @ExcelAssistant(titleName="借款利率（单位）")
    @Column("loanRateUnit")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String loanRateUnit;
    /**
     * 借款利率（数量）
     */
    @ExcelAssistant(titleName="借款利率（数量）")
    @Column("loanRateNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanRateNum;

    /**
     * 放款日
     */
    @ExcelAssistant(titleName="放款日",NonNull=true)
    @Column("loanDate")
    private Date loanDate;
    /**
     * 到期日
     */
    @ExcelAssistant(titleName="到期日",NonNull=true)
    @Column("expireDate")
    private Date expireDate;
    /**
     * 还款状态
     */
    @ExcelAssistant(titleName="还款状态",NonNull=true)
    @Column("repaymentStatus")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String repaymentStatus;
    /**
     * 结清日（字符串类型）
     */
    @ExcelAssistant(titleName="结清日")
    @Column("settleDateStr")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String settleDateStr;
    public void settleDateStr(Object obj){
        if(obj instanceof Date){
            settleDateStr = DateUtil.formatDateToString((Date)obj);
        }else{
            settleDateStr = obj.toString();
        }
    }
    /**
     * 结清日（时间类型）
     */
    @ExcelAssistant(titleName="结清日（时间）")
    @Column("settleDate")
    private Date settleDate;

    /**
     * 已还期数
     */
    @ExcelAssistant(titleName="已还期数")
    @Column("repaidPeriods")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal repaidPeriods;


    /**
     * 房屋所在地
     */
    @ExcelAssistant(titleName="房屋所在地",NonNull=true)
    @Column("houseAddress")
    @ColDefine(type = ColType.VARCHAR, width=512)
    private String houseAddress;
    /**
     * 产权证号
     */
    @ExcelAssistant(titleName="产权证号")
    @Column("propertyCertificate")
    @ColDefine(type = ColType.VARCHAR, width=512)
    private String propertyCertificate;
    public void propertyCertificate(Object object){
        if(object instanceof  BigDecimal){
            propertyCertificate  = ((BigDecimal)object).toPlainString();
        }else{
            propertyCertificate  = object.toString();
        }
    }
    /**
     * 已展期  00 默认，01已展期
     */
    @ExcelAssistant(titleName="已展期")
    @Column("extend")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String extend="00";


    /**
     * 费用名称
     */
    @ExcelAssistant(titleName="费用名称")
    @Column("costName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String costName="一次性服务费";
    /**
     * 费用金额
     */
    @ExcelAssistant(titleName="费用金额")
    @Column("costAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal costAmount=new BigDecimal("0");


    @Override
    public void dataConversion(){
        String units = "年,月,天";
        for(String unit : units.split(",")){
            if(loanExtensionon.contains(unit)){
                loanExtensiononUnit = unit;
                loanExtensiononNum = new BigDecimal(loanExtensionon.replaceAll("[\u4e00-\u9fa5]",""));
                break;
            }
        }
        for(String unit : units.split(",")){
            if(loanRate.contains(unit)){
                loanRateUnit = unit;
                String nums = loanRate.replaceAll("[\u4e00-\u9fa5%/]","");
                loanRateNum = new BigDecimal(Double.parseDouble(nums)/100.0);
            }
        }
        if(StringUtils.isNotEmpty(settleDateStr)){
            if(settleDateStr.contains("展期至")){
                settleDateStr = settleDateStr.replace("展期至","");
                settleDateStr = settleDateStr.replace(".","-");
                int  year  = expireDate.getYear()+1900;
                settleDate = DateUtil.getStringToDate(year+"-"+settleDateStr);
                if(settleDate.getTime()<expireDate.getTime()){
                    settleDate = DateUtil.getStringToDate((year+1)+"-"+settleDateStr);
                }
                extend = "01";
            }else{
                settleDate = DateUtil.getStringToDate(settleDateStr);
            }
        }
        //判断如果结清日大于到期日，则进一步判断是不是满足展期
        if(settleDate!=null){
            if(settleDate.getTime()>expireDate.getTime()){
                Date exDate = DateUtil.addMonth(expireDate,1);
                if(settleDate.getTime()>=exDate.getTime()){
                    this.extend = "01";
                }
            }
        }

    }

    public String toString(){
        return this.productName+this.businessName+this.loanSubject+this.channel+this.borrower+this.idNumber+this.payee+this.loanPrincipal+this.loanExtensionon+this.repaymentMethod+this.loanRate+this.loanDate+this.expireDate+this.repaymentStatus+this.settleDate+this.repaidPeriods+this.houseAddress+this.propertyCertificate;
    }

    public static void main(String[] args){
        String borrower  = "a,  b    c , d";
        borrower = borrower.replaceAll(" ",",").replaceAll("，",",").replaceAll(",+",",");
        System.out.println(borrower);
    }

    @Override
    public boolean validata(){
        boolean baseValidata =  super.validata();
        if(idNumber.split(",").length!=borrower.split(",").length){
            super.importMsg += "身份证与借款人数量不匹配，";
        }
        for(String subIdNumber : idNumber.split(",")){
            if(StringUtils.isNotEmpty(subIdNumber)&&!IdcardValidator.validateCard(subIdNumber)){
                super.importMsg += (subIdNumber+"身份证格式不正确，");
            }
        }
        if(!StringUtils.isEmpty(super.importMsg))super.importStatus="01";
        return !"01".equals(super.importMsg);
    }
}
