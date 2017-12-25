package com.kaisa.kams.models.history;

import com.kaisa.kams.components.utils.IdcardValidator;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelBaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhouchuang on 2017/4/26.
 */
@Table("sl_product_import_shulou")
@Data
@NoArgsConstructor
public class ProductImportShulou extends ExcelBaseModel {
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
                channel = "帮房宜（赎楼）";
            }else if(object.toString().equals("金葵晟")){
                channel = "金葵晟（赎楼）";
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
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式",NonNull=true)
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;
    /**
     * 天利率
     */
    @ExcelAssistant(titleName="天利率",NonNull=true)
    @Column("dayRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 12)
    private BigDecimal dayRate;
    public void  dayRate(Object object){
        if(object instanceof  String){
            String valueStr = object.toString().replace("%","");
            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(valueStr.replace(",","").trim())/100.0);
            dayRate = bigDecimal;
        }else{
            dayRate = (BigDecimal)object;
        }
    }
    /**
     * 费用名称
     */
    @ExcelAssistant(titleName="费用名称",NonNull=true)
    @Column("costName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String costName;
    /**
     * 费用金额
     */
    @ExcelAssistant(titleName="费用金额")
    @Column("costAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal costAmount;
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
     * 结清日
     */
    @ExcelAssistant(titleName="结清日")
    @Column("settleDate")
    private Date settleDate;
    /**
     * 结清利息
     */
    @ExcelAssistant(titleName="结清利息")
    @Column("settleInterest")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal settleInterest;




    public String toString(){
        return this.productName+this.businessName+this.loanSubject+this.channel+this.borrower+this.idNumber+this.payee+this.loanPrincipal+this.repaymentMethod+this.dayRate+this.costName+this.costAmount+this.loanDate+this.expireDate+this.repaymentStatus+this.settleDate+this.settleInterest;
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
