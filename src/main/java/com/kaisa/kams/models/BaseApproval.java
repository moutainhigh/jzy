package com.kaisa.kams.models;

import com.kaisa.kams.enums.ApprovalStatusType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Comment;

import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/10/31.
 */
@Data
@NoArgsConstructor
public class BaseApproval extends BaseModel {
    @Comment("关联的产品ID")
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String productId;

    @Comment("审批状态")
    @Column("approveStatus")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatus;

    @Comment("审批状态描述")
    @Column("approveStatusDesc")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatusDesc;

    @Comment("审批单状态")
    @Column("approvalStatusType")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private ApprovalStatusType approvalStatusType;

    /**
     * 展期金额
     */
    @Comment("展期金额")
    @Column("amount")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal amount;
}
