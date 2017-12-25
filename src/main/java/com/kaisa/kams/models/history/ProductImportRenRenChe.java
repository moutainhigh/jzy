package com.kaisa.kams.models.history;

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
 * Created by luoyj on 2017/5/08.
 */
@Table("sl_product_import_renrenche")
@Data
@NoArgsConstructor
public class ProductImportRenRenChe extends ExcelBaseModel {
    /**
     * 产品子类名
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="产品子类名")
    @Column("productName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String productName;
    public void productName(Object object){
        if(object.toString().equals("人人车")){
            productName = "车押贷-人人车";
        }else{
            productName = object.toString();
        }
    }

    /**
     * 放款主体
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="放款主体")
    @Column("loanSubject")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanSubject;

    /**
     * 借款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="借款人",NonNull=true)
    @Column("borrower")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String borrower;
    /**
     * 证件号码
     */
    @ExcelAssistant(titleName="证件号码",NonNull=true)
    @Column("idNumber")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String idNumber;

    /**
     * 状态
     */
    @ExcelAssistant(titleName="状态")
    @Column("repaymentStatus")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String repaymentStatus;

    /**
     * 机构
     */
    @Column("agencyName")
    @ExcelAssistant(titleName="机构")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String agencyName;
    /**
     * 组织
     */
    @Column("organizeName")
    @ExcelAssistant(titleName="组织")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String organizeName;


    /**
     * 渠道
     */
    @ExcelAssistant(titleName="渠道",NonNull=true)
    @Column("channel")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String channel;

    /**
     * 业务来源
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="业务来源",NonNull=true)
    @Column("businessName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String businessName;

    /**
     * 借款金额（元）
     */
    @ExcelAssistant(titleName="借款金额（元）",NonNull=true)
    @Column("loanPrincipal")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal loanPrincipal;

    /**
     * 实际放款金额
     */
    @ExcelAssistant(titleName="实际放款金额",NonNull=true)
    @Column("actualAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal actualAmount;

    /**
     * 借款期限
     */
    @ExcelAssistant(titleName="借款期限",NonNull=true)
    @Column("loanTerm")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanTerm;
    /**
     * 借款期限（单位）
     */
    @ExcelAssistant(titleName="借款期限（单位）",NonNull=true)
    @Column("loanTermUnit")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanTermUnit;

    /**
     * 借款期限（数量）
     */
    @ExcelAssistant(titleName="借款期限（数量）",NonNull=true)
    @Column("loanTermNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanTermNum;
    /**
     * 借款利息
     */
    @ExcelAssistant(titleName="借款利息(%)")
    @Column("borrowRate")
    @ColDefine(type = ColType.FLOAT, width=24,precision = 12)
    private BigDecimal borrowRate;
    public void borrowRate(Object object){
        if(object instanceof  String){
            String valueStr = object.toString().replace("%","");
            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(valueStr.replace(",","").trim())/100.0);
            borrowRate = bigDecimal;
        }else{
            borrowRate = (BigDecimal)object;
        }
    }


    /**
     * 计息方式
     */
    @ExcelAssistant(titleName="计息方式")
    @Column("interestMode")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String interestMode;

    /**
     * 一次性服务费
     */
    @ExcelAssistant(titleName="一次性服务费")
    @Column("serviceCharge")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal serviceCharge;

    /**
     * 应收利息（元）
     */
    @ExcelAssistant(titleName="应收利息（元）",NonNull=true)
    @Column("receivableRate")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal receivableRate;


    /**
     * 应收总金额（元）
     */
    @ExcelAssistant(titleName="应收总金额（元）",NonNull=true)
    @Column("receivableActualAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal receivableActualAmount;


    /**
     * 实际放款日期
     */
    @ExcelAssistant(titleName="实际放款日期")
    @Column("loanDate")
    @ColDefine(type = ColType.DATE)
    private Date loanDate;
    /**
     * 应还日期
     */
    @ExcelAssistant(titleName="应还日期")
    @Column("expireDate")
    @ColDefine(type = ColType.DATE)
    private Date expireDate;

    /**
     * 实际结清日期
     */
    @ExcelAssistant(titleName="实际结清日期")
    @Column("repayDate")
    @ColDefine(type = ColType.DATE)
    private Date repayDate;

    /**
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式")
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;


//    public String toString(){
//        return this.productName+this.loanSubject+this.channel+this.borrower+this.idNumber+this.payee+this.loanPrincipal+this.repaymentMethod+this.borrowRate+this.loanDate+this.expireDate+this.repaymentStatus+this.repaymentNumber+this.carNumber+this.carBrand+this.carValuation+this.remarks;
//    }
    @Override
    public void dataConversion(){
        String units = "年,月,天";
        for(String unit : units.split(",")){
            if(loanTerm.contains(unit)){
                loanTermUnit = unit;
                loanTermNum = new BigDecimal(loanTerm.replaceAll("[\u4e00-\u9fa5]",""));
                break;
            }
        }
    }

    @Override
    public boolean validata(){
        boolean baseValidata =  super.validata();
        if(!IdcardValidator.validateCard(this.idNumber)){
            super.importMsg += "身份证格式不正确，";
        }
        if(!StringUtils.isEmpty(super.importMsg))super.importStatus="01";
        return !"01".equals(super.importMsg);
    }
}
