package com.kaisa.kams.models;

import com.kaisa.kams.enums.AddressType;
import com.kaisa.kams.enums.ApprovalStatusType;
import com.kaisa.kams.enums.LoanStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by lw on 2017/9/15.
 * 房产解押申请
 */
@Table("sl_house_noMortgage_apply")
@Data
@NoArgsConstructor
public class HouseNoMortgageApply extends BaseModel{

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    @Comment("关联的产品ID")
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String productId;

    /**
     * 业务单号
     */
    @Comment("业务单号")
    @Column("businessCode")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String businessCode;

    /**
     * 房产解押编号
     */
    @Comment("房产解押编号")
    @Column("applyCode")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String applyCode;


    /**
     * 业务来源
     */
    @Comment("业务来源")
    @Column("businessSource")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String businessSource;

    public String channel(){
        return this.businessSource.startsWith("渠道")?"渠道":"自营";
    }
    public String businessSource(){
        return this.businessSource.startsWith("渠道")?this.businessSource.replace("渠道|",""):this.businessSource.substring(this.businessSource.lastIndexOf("-")+1,this.businessSource.length());
    }

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
    private ApprovalStatusType loanStatus;

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
     * 提单日期
     */
    @Comment("提单日期")
    @Column("submitDate")
    @ColDefine(type = ColType.DATETIME)
    private Date submitDate;

    /**
     * 房产解押地类型
     */
    @Comment("房产解押地类型")
    @Column("addressType")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private AddressType addressType;

    /**
     * 抵押日期
     */
    @Comment("抵押日期")
    @Column("mortgageDate")
    @ColDefine(type = ColType.DATETIME)
    private Date mortgageDate ;


}
