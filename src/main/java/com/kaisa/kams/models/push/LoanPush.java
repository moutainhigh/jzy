package com.kaisa.kams.models.push;

import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.enums.push.LoanPushStatus;
import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lw on 2016/12/12.
 * Update by pengyueyang on 2017/11/2
 * 推单
 */
@Table("sl_loan_push")
@Data
@NoArgsConstructor
public class LoanPush extends BaseModel {

    @Comment("业务单id")
    @Column("loanId")
    private String loanId;

    @Comment("业务单code")
    @Column("loanCode")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String loanCode;

    @Comment("产品大类id")
    @Column("productTypeId")
    private String productTypeId;

    @Comment("产品大类名称")
    @Column("productTypeName")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String productTypeName;

    @Comment("产品id")
    @Column("productId")
    private String productId;

    @Comment("产品名称")
    @Column("productName")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String productName;

    @Comment("放款主体id")
    @Column("loanSubjectId")
    private String loanSubjectId;

    @Comment("loanSubjectName")
    @Column("loanSubjectName")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String loanSubjectName;

    @Comment("渠道名称")
    @Column("channelName")
    @ColDefine(type = ColType.VARCHAR, width = 60)
    private String channelName;

    @Comment("来源类型（自营0、渠道1）")
    @Column("sourceType")
    @ColDefine(type = ColType.CHAR, width = 1)
    private String sourceType;

    @Comment("业务条线")
    @Column("businessLine")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String businessLine;

    @Comment("组织代码")
    @Column("organizeCode")
    @ColDefine(type = ColType.VARCHAR, width = 10)
    private String organizeCode;

    @Comment("业务员姓名")
    @Column("saleName")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String saleName;

    @Comment("主借款人名称")
    @Column("masterBorrowerName")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String masterBorrowerName;

    @Comment("期限")
    @Column("term")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String term;

    @Comment("期限类型")
    @Column("termType")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private LoanTermType termType;

    @Comment("高管审批时间")
    @Column("leaderApprovedTime")
    @ColDefine(type = ColType.DATETIME)
    private Date leaderApprovedTime;

    @Comment("高管审批金额（申请金额）")
    @Column("amount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal amount;

    @Comment("状态")
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private LoanPushStatus status;

    @Comment("标的最大应结清时间")
    @Column("loanMaxDueDate")
    @ColDefine(type = ColType.DATE)
    private Date loanMaxDueDate;

    @Comment("标的最小应结清时间")
    @Column("loanMinDueDate")
    @ColDefine(type = ColType.DATE)
    private Date loanMinDueDate;

    @Comment("展期标签0未展期1展期")
    @Column("extensionLabel")
    @ColDefine(type = ColType.BOOLEAN)
    private Boolean extensionLabel;

    @Comment("产品模板类型")
    @Column("itemType")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private ItemType itemType;


}
