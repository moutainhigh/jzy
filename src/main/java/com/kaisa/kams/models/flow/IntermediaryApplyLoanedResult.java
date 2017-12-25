package com.kaisa.kams.models.flow;

import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

/**
 * 居间费审批结果
 * Created by lw on 2017/09/12.
 */
@Table("sl_intermediaryApply_loaned_result")
@Data
@NoArgsConstructor
public class IntermediaryApplyLoanedResult extends BaseModel {

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    /**
     * 节点名称
     */
    @Column("nodeName")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String nodeName;

    /**
     * 审批用户名称
     */
    @Column("userName")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String userName;

    /**
     * 审批用户Id
     */
    @Column("userId")
    private String userId;

    /**
     * 审批时间
     */
    @Column("approvalTime")
    @ColDefine(type = ColType.DATETIME)
    private Date approvalTime;

    /**
     * 节点编号
     */
    @Column("nodeCode")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String nodeCode;
    /**
     * 类型 D
     */
    @Column("approvalType")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String approvalType="D";


    /**
     * 开始时间
     */
    @Column("startTime")
    @ColDefine(type = ColType.DATETIME)
    private Date startTime;

    /**
     * 时效  天
     */
    @Column("duration")
    private long duration;


}
