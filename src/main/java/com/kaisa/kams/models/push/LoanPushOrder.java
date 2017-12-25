package com.kaisa.kams.models.push;

import com.kaisa.kams.enums.LoanTermType;
import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.enums.push.LoanPushOrderStatus;
import com.kaisa.kams.enums.push.PushRepayMethodType;
import com.kaisa.kams.enums.push.PushTarget;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;
import org.nutz.dao.entity.annotation.Index;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.dao.entity.annotation.TableIndexes;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务推送单订单
 * Created by pengyueyang on 2017/11/2.
 */
@Table("sl_loan_push_order")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_CODE", fields = { "code" }) })
public class LoanPushOrder extends BaseModel{

    @Comment("业务推送单id")
    @Column("pushId")
    private String pushId;

    @Comment("业务单id")
    @Column("loanId")
    private String loanId;

    @Comment("资产单号")
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String code;

    @Comment("推送目标")
    @Column("pushTarget")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private PushTarget pushTarget;

    @Comment("产品大类")
    @Column("productTypeName")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String productTypeName;

    @Comment("放款主体名称")
    @Column("loanSubjectName")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String loanSubjectName;

    @Comment("平台借款人用户（手机号或者用户名）")
    @Column("platformBorrower")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String platformBorrower;

    @Comment("平台借款人姓名")
    @Column("platformBorrowerName")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String platformBorrowerName;

    @Comment("平台借款人id")
    @Column("platformBorrowerId")
    private String platformBorrowerId;

    @Comment("推单金额")
    @Column("amount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal amount;

    @Comment("还款方式")
    @Column("repayMethod")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private PushRepayMethodType repayMethod;

    @Comment("期限")
    @Column("term")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String term;

    @Comment("期限类型")
    @Column("termType")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private LoanTermType termType;

    @Comment("渠道资金利率")
    @Column("channelRate")
    @ColDefine(type = ColType.FLOAT, width = 24, precision = 8)
    private BigDecimal channelRate;

    @Comment("平台标的利率")
    @Column("platformLoanRate")
    @ColDefine(type = ColType.FLOAT, width = 24, precision = 8)
    private BigDecimal platformLoanRate;

    @Comment("项目类型")
    @Column("itemType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private ItemType itemType;

    @Comment("标的内容")
    @Column("content")
    @ColDefine(type = ColType.TEXT)
    private String content;

    @Comment("平台标的单号")
    @Column("platformLoanCode")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String platformLoanCode;

    @Comment("平台标的id")
    @Column("platformLoanId")
    private String platformLoanId;

    @Comment("推单时间")
    @Column("pushDateTime")
    @ColDefine(type = ColType.DATETIME)
    private Date pushDateTime;

    @Comment("状态")
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private LoanPushOrderStatus status;

    @Comment("平台标的总期数")
    @Column("platformLoanTotalPeriod")
    @ColDefine(type = ColType.INT)
    private Integer platformLoanTotalPeriod;

    @Comment("平台标的放款时间")
    @Column("platformLoanTime")
    @ColDefine(type = ColType.DATETIME)
    private Date platformLoanTime;

    @Comment("平台标的当前期数")
    @Column("platformLoanCurrentPeriod")
    @ColDefine(type = ColType.INT)
    private Integer platformLoanCurrentPeriod;

    @Comment("平台标的下期到期时间")
    @Column("platformLoanNextDueDate")
    @ColDefine(type = ColType.DATE)
    private Date platformLoanNextDueDate;

    @Comment("平台标的应到期时间")
    @Column("platformLoanDueDate")
    @ColDefine(type = ColType.DATE)
    private Date platformLoanDueDate;

    @Comment("平台标的结清时间")
    @Column("platformLoanClearedDate")
    @ColDefine(type = ColType.DATE)
    private Date platformLoanClearedDate;

    @Comment("最大投标人数")
    @Column("maxInvestor")
    @ColDefine(type = ColType.INT)
    private Integer maxInvestor;

}
