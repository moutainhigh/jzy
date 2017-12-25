package com.kaisa.kams.models.flow;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.ApprovalCode;
import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.repo.org.objectweb.asm.Type;

import java.util.Date;

/**
 * 审批结果
 * Created by weid on 2016/12/15.
 */
@Table("sl_approval_result")
@Data
@NoArgsConstructor
public class ApprovalResult  extends BaseModel {

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    /**
     * 订单Id
     */
    @Column("orderId")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String orderId;

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
    public String approvalTime(){
        return DateUtil.formatDateTimeToString(this.approvalTime);
    }

    /**
     * 节点编号
     */
    @Column("nodeCode")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String nodeCode;

    /**
     * 审批结果信息
     */
    @Column("content")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    private String content;

    /**
     * 审批代码
     */
    @Column("approvalCode")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private ApprovalCode approvalCode;

    /**
     * 审批类型
     */
    @Column("approvalType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private ApprovalType approvalType;

    /**
     * 开始时间
     */
    @Column("startTime")
    @ColDefine(type = ColType.DATETIME)
    private Date startTime;

    /**
     * 时效
     */
    @Column("duration")
    private long duration;

    /**
     *是否同意用印
     */
    @Column("enterprise")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean enterprise;

    public String getEnterpriseMsgForPDF(){
        if(this.enterprise){
            return "同意"+ DateUtil.formatDateTimeToString(this.approvalTime);
        }
        return "";
    }

    /**
     *审批的流程类型
     */
    @Column("flowConfigureType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FlowConfigureType flowConfigureType;

    /**
     *是否同意居间费立项
     */
    @Column("intermediary")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean intermediary;

}
