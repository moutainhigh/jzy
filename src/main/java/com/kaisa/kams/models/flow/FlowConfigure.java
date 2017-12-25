package com.kaisa.kams.models.flow;

import com.kaisa.kams.components.view.flow.FlowConfigureTypeVO;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@Table("sl_flow_configure")
@Data
@NoArgsConstructor
public class FlowConfigure  extends BaseModel {
    /**
     * 流程模板编号
     */
    @Column("flowCode")
    private String flowCode;

    @Column("flowType")
    private FlowConfigureType flowType;

    private FlowConfigureTypeVO flowConfigureTypeVO;
    /**
     * 业务流程名称
     */
    @Column("flowName")
    private String flowName;

    /**
     * 业务流程描述
     */
    @Column("flowDesc")
    @ColDefine(type = ColType.VARCHAR,width = 512)
    private String flowDesc;

    /**
     * 状态  是否生效
     */
    @Column("status")
    private PublicStatus status;

    /**
     * 产品ID
     */
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR,width = 2048)
    private String productId;

    /**
     * 渠道ID
     */
    @Column("channelId")
    @ColDefine(type = ColType.VARCHAR,width = 2048)
    private String channelId;

    private List<FlowConfigureRelation> flowConfigureRelations;

    public void addFlowConfigureRelation(FlowConfigureRelation flowConfigureRelation){
        if(flowConfigureRelations==null){
            flowConfigureRelations  = new ArrayList<FlowConfigureRelation>();
        }
        flowConfigureRelations.add(flowConfigureRelation);
    }


}
