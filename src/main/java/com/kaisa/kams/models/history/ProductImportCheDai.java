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
@Table("sl_product_import_chedai")
@Data
@NoArgsConstructor
public class ProductImportCheDai extends ExcelBaseModel {
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
    public void loanSubject(Object object){
        if(object.toString().equals("富昌")){
            loanSubject = "深圳市富昌小额贷款有限公司";
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
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式")
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;
    /**
     * 借款利率
     */
    @ExcelAssistant(titleName="借款利率")
    @Column("borrowRate")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String borrowRate;

    /**
     * 借款利率（单位）
     */
    @ExcelAssistant(titleName="借款利率（单位）")
    @Column("borrowRateUnit")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String borrowRateUnit;
    /**
     * 借款利率（数量）
     */
    @ExcelAssistant(titleName="借款利率（数量）")
    @Column("borrowRateNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal borrowRateNum;


    /**
     * 放款日
     */
    @ExcelAssistant(titleName="放款日")
    @Column("loanDate")
    private Date loanDate;
    /**
     * 到期日
     */
    @ExcelAssistant(titleName="到期日")
    @Column("expireDate")
    private Date expireDate;
    /**
     * 还款状态
     */
    @ExcelAssistant(titleName="还款状态")
    @Column("repaymentStatus")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String repaymentStatus;

    /**
     * 已还期数
     */
    @ExcelAssistant(titleName="已还期数")
    @Column("repaymentNumber")
    @ColDefine(type = ColType.INT, width=10)
    private String repaymentNumber;

    /**
     * 车牌号码
     */
    @ExcelAssistant(titleName="车牌号码")
    @Column("carNumber")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String carNumber;

    /**
     * 车辆品牌
     */
    @ExcelAssistant(titleName="车辆品牌")
    @Column("carBrand")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String carBrand;

    /**
     * 车辆估值
     */
    @ExcelAssistant(titleName="车辆估值")
    @Column("carValuation")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal carValuation;

    /**
     * 备注
     */
    @ExcelAssistant(titleName="备注")
    @Column("remarks")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String remarks;


    public String toString(){
        return this.productName+this.businessName+this.loanSubject+this.channel+this.borrower+this.idNumber+this.payee+this.loanPrincipal+this.repaymentMethod+this.borrowRate+this.loanDate+this.expireDate+this.repaymentStatus+this.repaymentNumber+this.carNumber+this.carBrand+this.carValuation+this.remarks;
    }
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
        for(String unit : units.split(",")){
            if(borrowRate.contains(unit)){
                borrowRateUnit = unit;
                String nums = borrowRate.replaceAll("[\u4e00-\u9fa5%/]","");
                borrowRateNum = new BigDecimal(Double.parseDouble(nums)/100.0);
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
