package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by lw on 2017/8/30.
 * 居间费申请
 */
@Table("sl_intermediary_apply")
@Data
@NoArgsConstructor
public class IntermediaryApply extends BaseModel{

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    /**
     * 业务单号
     */
    @Comment("业务单号")
    @Column("businessCode")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String businessCode;

    /**
     * 放款申请编号
     */
    @Comment("放款申请编号")
    @Column("applyCode")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String applyCode;

    /**
     * 产品Id
     */
    @Comment("产品id")
    @Column("productId")
    private String productId;

    /**
     * 产品子类
     */
    @Comment("产品子类")
    @Column("productName")
    private String productName;

    /**
     * 主借款人id
     */
    @Comment("借款人id")
    @Column("masterBorrowerId")
    private String masterBorrowerId;

    /**
     * 借款人
     */
    @Comment("借款人")
    @Column("borrower")
    private String borrower;

    /**
     * 单状态
     */
    @Comment("单状态")
    @Column("loanStatus")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanStatus loanStatus;

    /**
     * 审批状态
     */
    @Comment("审批状态")
    @Column("approveStatus")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatus;

    /**
     * 审批状态描述
     */
    @Comment("审批状态描述")
    @Column("approveStatusDesc")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatusDesc;


    /**
     * 放款日期
     */
    @Comment("放款日期")
    @Column("loanTime")
    @ColDefine(type = ColType.DATETIME)
    private Date loanTime;

    /**
     * 提单日期
     */
    @Comment("提单日期")
    @Column("submitDate")
    @ColDefine(type = ColType.DATETIME)
    private Date submitDate;

    /**
     * 居间人id
     */
    @Comment("居间人id")
    @Column("intermediaryId")
    private String intermediaryId;


    /**
     * 居间费
     */
    @Comment("居间费")
    @Column("intermediaryFee")
    @ColDefine(type = ColType.FLOAT, width = 24,precision = 10)
    private BigDecimal intermediaryFee;


    /**
     * 居间人
     */
    @Comment("居间人")
    @Column("name")
    private String name;

    /**
     * 身份证号码
     */
    @Comment("身份证号码")
    @Column("idNumber")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String idNumber;

    /**
     * 手机号码
     */
    @Comment("手机号码")
    @Column("phone")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String phone;

    /**
     * 开户行
     */
    @Comment("开户行")
    @Column("bank")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String bank;

    /**
     * 账号
     */
    @Comment("账号")
    @Column("account")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String account;

    /**
     * 地址
     */
    @Comment("地址")
    @Column("address")
    @ColDefine(type=ColType.VARCHAR,width=256)
    private String address;

    /**
     * 支持多附件保存urls-居间服务协议
     */
    @Comment("居间服务协议")
    @Column("serviceContractFileUrls")
    @ColDefine(type = ColType.TEXT)
    private String serviceContractFileUrls;

    public String intermediaryFee(){
        if(intermediaryFee==null||intermediaryFee.doubleValue()==0D){
            return "- -";
        }else{
            BigDecimal setScale = intermediaryFee.setScale(2,BigDecimal.ROUND_HALF_UP);
            return setScale.toString();
        }
    }

    public String account(){
        return TextFormatUtils.formatAccount(this.account);
    }


}
