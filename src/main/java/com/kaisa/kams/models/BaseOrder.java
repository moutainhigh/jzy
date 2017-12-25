package com.kaisa.kams.models;

import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;

/**
 * Created by zhouchuang on 2017/10/31.
 */
@Data
@NoArgsConstructor
public class BaseOrder extends BaseModel {

    @Column("orderId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String orderId;

    @Column("currentNode")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String currentNode;

    @Column("flowControlType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FlowControlType flowControlType;

    @Column("flowConfigureType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FlowConfigureType flowConfigureType;
}
