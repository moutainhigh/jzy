package com.kaisa.kams.models;

import com.kaisa.kams.enums.FlowConfigureType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 流程引擎定义文件
 * Created by weid on 2017/3/24.
 */
@Table("wf_templet")
@Data
@NoArgsConstructor
public class WfTemplet extends BaseModel{

    /**
     * 产品Id
     */
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR,width = 2048)
    private String productId;


    /**
     * 流程类型
     */
    @Column("flowType")
    private FlowConfigureType flowType;

    /**
     * 模板内容
     */
    @Column("content")
    @ColDefine(type = ColType.TEXT)
    private String content;


}
