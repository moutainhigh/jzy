package com.kaisa.kams.models;

import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * Created by zhouchuang on 2017/9/1.
 */
@Table("sl_product_process")
@Data
@NoArgsConstructor
public class ProductProcess extends BaseModel{
    /**
     *产品ID
     */
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String productId;

    /**
     *流程ID
     */
    @Column("processId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String processId;

    /**
     *流程ID
     */
    @Column("flowType")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private FlowConfigureType flowType;

    /**
     * 状态  是否生效
     */
    @Column("status")
    private PublicStatus status;




}
