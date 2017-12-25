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
 * Created by lw on 2017/5/8.
 */
@Table("sl_product_import_xinyongdai")
@Data
@NoArgsConstructor
public class ProductImportXinYongDai extends ExcelBaseModel {
    /**
     * 产品名称
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="产品名称")
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
    @ExcelAssistant(titleName="放款主体")
    @Column("loanSubject")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanSubject;
    /**
     * 渠道名称
     */
    @ExcelAssistant(titleName="渠道名称")
    @Column("channel")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String channel;
    /**
     * 借款人
     */
    @Condition(condition = "LIKE",sql="%{}%")
    @ExcelAssistant(titleName="借款人",NonNull=true)
    @Column("borrower")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String borrower;
    /**
     * 身份证号
     */
    @ExcelAssistant(titleName="身份证号",NonNull=true)
    @Column("idNumber")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String idNumber;
    /**
     * 收款人
     */
    @ExcelAssistant(titleName="收款人",NonNull=true)
    @Column("payee")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String payee;
    /**
     * 融资金额
     */
    @ExcelAssistant(titleName="融资金额",NonNull=true)
    @Column("loanPrincipal")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal loanPrincipal;
    /**
     * 借款期限
     */
    @ExcelAssistant(titleName="借款期限（月）",NonNull=true)
    @Column("loanTerm")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanTerm;
    /**
     * 借款期限（单位）
     */
    @ExcelAssistant(titleName="借款期限（单位）",NonNull=true)
    @Column("loanTermUnit")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanTermUnit="月";

    /**
     * 借款期限（数量）
     */
    @ExcelAssistant(titleName="借款期限（数量）",NonNull=true)
    @Column("loanTermNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanTermNum;
    /**
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式")
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;
    /**
     * 借款利率
     */
    @ExcelAssistant(titleName="借款利率（月）")
    @Column("borrowRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal borrowRate;
    /**
     * 借款利率（单位）
     */
    @ExcelAssistant(titleName="借款利率（单位）")
    @Column("borrowRateUnit")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String borrowRateUnit="月";
    /**
     * 借款利率（数量）
     */
    @ExcelAssistant(titleName="借款利率（数量）")
    @Column("borrowRateNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal borrowRateNum;
    /**
     * 起始日
     */
    @ExcelAssistant(titleName="起始日")
    @Column("startDate")
    private Date startDate;

    /**
     * 还款状态
     */
    @ExcelAssistant(titleName="还款状态")
    @Column("repaymentStatus")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String repaymentStatus;





    public String toString(){
        return this.productName+this.businessName+this.loanSubject+this.borrower+this.idNumber+this.payee+this.loanPrincipal+this.repaymentMethod+this.borrowRate+this.startDate+this.repaymentStatus;
    }

    @Override
    public void dataConversion(){
//        String units = "年,月,天";
//        for(String unit : units.split(",")){
//            if(loanTerm.contains(unit)){
//                loanTermUnit = unit;
//                loanTermNum = new BigDecimal(loanTerm.replaceAll("[\u4e00-\u9fa5]",""));
//                break;
//            }
//        }
//        for(String unit : units.split(",")){
//            if(borrowRate.contains(unit)){
//                borrowRateUnit = unit;
//                String nums = borrowRate.replaceAll("[\u4e00-\u9fa5%/]","");
//                borrowRateNum = new BigDecimal(Double.parseDouble(nums)/100.0);
//            }
//        }
        loanTermNum = loanTerm;
        borrowRateNum = borrowRate;
        if(("平台募集").equals(loanSubject)){
            loanSubject="佳兆业金服";
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
