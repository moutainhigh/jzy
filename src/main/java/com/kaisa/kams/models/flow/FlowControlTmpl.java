package com.kaisa.kams.models.flow;

import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.enums.PublicStatus;

import com.kaisa.kams.models.BaseModel;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品流程配置模板表
 */
@Table("sl_flow_control_tmpl")
@Data
@NoArgsConstructor
public class FlowControlTmpl extends BaseModel {
    /**
     * 产品类型Id
     */
    @Column("productTypeId")
    private String productTypeId;

    /**
     * 产品Id
     */
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR,width = 2048)
    private String productId;

    /**
     * 流程名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 流程说明
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR,width = 300)
    private String description;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;


    /**
     * 流程类型
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private FlowControlType type;

    /**
     * 流程编码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String code;

    private List<FlowControlItem> flowControlItems;
}
