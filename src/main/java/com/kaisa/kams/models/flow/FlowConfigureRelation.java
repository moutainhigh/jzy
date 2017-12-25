package com.kaisa.kams.models.flow;

import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.models.BaseModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@Table("sl_flow_configure_relation")
@Data
@NoArgsConstructor
public class FlowConfigureRelation extends BaseModel {

    /**
     * 流程ID
     */
    @Column("configId")
    private String configId;

    /**
     *FlowControllerTemp模型ID
     */
    @Column("templateId")
    private String templateId;

    /**
     * 模型
     */
    @One(target = FlowControlTmpl.class, field ="templateId" )
    private FlowControlTmpl template;


    @Column("moduleType")
    private FlowControlType moduleType;

    /**
     * 排序
     */
    @Column("sortNo")
    private int sortNo;


}
