package com.kaisa.kams.models.flow;

import com.kaisa.kams.models.BaseModel;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品流程配置项信息表
 */
@Table("sl_flow_control_item")
@Data
@NoArgsConstructor
public class FlowControlItem extends BaseModel {
    /**
     * 流程模板Id
     */
    @Column("tmplId")
    private String tmplId;

    /**
     * 流程名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 风控流程岗位Id
     * (暂时已为角色Id代替)
     */
    @Column("organizeId")
    private String organizeId;

    /**
     * 岗位角色名称
     */
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String organizeName;

    /**
     * 流程节点编码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String code;

    /**
     * 节点最低审批金额
     */
    @Column("approveAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal approveAmount = new BigDecimal(0);

    /**
     *是否参与用印
     */
    @Column("enterprise")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean enterprise;
    public String enterprise(){
        return enterprise?"true":"false";
    }
}
