package com.kaisa.kams.models.history;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.IdcardValidator;
import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import com.kaisa.kams.components.utils.excelUtil.ExcelBaseModel;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
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
 * Created by zhouchuang on 2017/4/26.
 */
@Table("sl_product_import_baoli")
@Data
@NoArgsConstructor
public class ProductImportBaoli extends ExcelBaseModel {

    private String productName="商业保理";
    private String subProductName="商业保理";
    private String channel ="自营";
    /**
     * 业务员
     */
    @ExcelAssistant(titleName="业务员",NonNull=true)
    @Column("businessName")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String businessName;
    /**
     * 融资主体名称
     */
    @ExcelAssistant(titleName="融资主体名称",NonNull=true)
    @Column("financingSubject")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String financingSubject;

    /**
     * 放款主体
     */
    @ExcelAssistant(titleName="放款主体",NonNull=true)
    @Column("loanSubject")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanSubject;

    /**
     * 营业执照编号
     */
    @ExcelAssistant(titleName="营业执照编号",NonNull=true)
    @Column("licenseNo")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String licenseNo;


    /**
     * 法人姓名
     */
    @ExcelAssistant(titleName="法人姓名",NonNull=true)
    @Column("corporation")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String corporation;

    /**
     * 法人身份证号
     */
    @ExcelAssistant(titleName="法人身份证号")
    @Column("corporationID")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String corporationID;

    /**
     * 收款人
     */
    @ExcelAssistant(titleName="收款人",NonNull=true)
    @Column("payee")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String payee;


    /**
     * 开户行（需输入具体的支行）
     */
    @ExcelAssistant(titleName="开户行（需输入具体的支行）")
    @Column("openingBank")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String openingBank;

    /**
     * 收款账号
     */
    @ExcelAssistant(titleName="收款账号")
    @Column("payeeAccount")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String payeeAccount;

    /**
     * 融资申请金额（万元）
     */
    @ExcelAssistant(titleName="融资申请金额（元）")
    @Column("applyAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal  applyAmount;


    /**
     * 期限单位
     */
    @ExcelAssistant(titleName="期限单位",NonNull=true)
    @Column("loanExtensiononUnit")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String loanExtensiononUnit;

    /**
     * 借款期限
     */
    @ExcelAssistant(titleName="借款期限",NonNull=true)
    @Column("loanExtensiononNum")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal loanExtensiononNum;
    public void loanExtensiononNum(Object object){
        this.loanExtensiononNum = new BigDecimal(object.toString().replaceAll("[\u4e00-\u9fa5]", ""));
    }


    /**
     * 借款利息
     */
    @ExcelAssistant(titleName="借款利息",NonNull=true)
    @Column("loanRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 16)
    private BigDecimal loanRate;

    /**
     * 放款时间
     */
    @ExcelAssistant(titleName="放款时间",NonNull=true)
    @Column("loanDate")
    private Date loanDate;
    public void loanDate(Object object){
        if(object instanceof String){
            String dates = object.toString();
            System.out.println(dates);
            String year = dates.split("/")[0];
            String month = dates.split("/")[1];
            String date = dates.split("/")[2];
            loanDate =  new Date(Integer.parseInt(year)-1900,Integer.parseInt(month)-1,Integer.parseInt(date));
        }else{
            loanDate = (Date)object;
        }

    }

    /**
     * 收款时间
     */
    @Column("expireDate")
    private Date expireDate;

    private Date  settleDate;
    /**
     * 还款方式
     */
    @ExcelAssistant(titleName="还款方式",NonNull=true)
    @Column("repaymentMethod")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String repaymentMethod;

    /**
     * 状态
     */
    @ExcelAssistant(titleName="状态",NonNull=true)
    @Column("repaymentStatus")
    @ColDefine(type = ColType.VARCHAR, width=16)
    private String repaymentStatus;



    /**
     * 交易买方名称
     */
    @ExcelAssistant(titleName="交易买方名称")
    @Column("tradingParty")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String tradingParty;


    /**
     * 应收账款金额（元）
     */
    @ExcelAssistant(titleName="应收账款金额（元）")
    @Column("receivableAmount")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal receivableAmount;


    /**
     * 应收账款合同编号
     */
    @ExcelAssistant(titleName="应收账款合同编号")
    @Column("receivablesContractNo")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String receivablesContractNo;



    public String toString(){
        return this.productName+this.subProductName+this.channel+this.businessName+this.financingSubject+this.loanSubject+this.licenseNo+this.corporation+this.corporationID+this.payee+this.openingBank+this.payeeAccount+this.applyAmount+this.loanExtensiononUnit+this.loanExtensiononNum+this.loanRate+this.loanDate+this.expireDate+this.settleDate+this.repaymentMethod+this.repaymentStatus+this.tradingParty+this.receivableAmount+this.receivablesContractNo;
    }


    @Override
    public boolean validata(){
        boolean baseValidata =  super.validata();
        if(!StringUtils.isEmpty(super.importMsg))super.importStatus="01";
        return !"01".equals(super.importMsg);
    }

    @Override
    public void dataConversion(){
        if(this.loanExtensiononUnit.equals("天")){
            this.expireDate = DateUtil.addDays(this.loanDate,this.loanExtensiononNum.intValue(),0,0);
        }else  if(this.loanExtensiononUnit.equals("月")){
            this.expireDate = DateUtil.addDays(this.loanDate,0,this.loanExtensiononNum.intValue(),0);
        }else  if(this.loanExtensiononUnit.equals("季")){
            this.expireDate = DateUtil.addDays(this.loanDate,0,this.loanExtensiononNum.intValue()*3,0);
        }
    }
}
