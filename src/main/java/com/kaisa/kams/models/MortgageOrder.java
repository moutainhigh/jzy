package com.kaisa.kams.models;

import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 流程实例订单表
 * Created by weid on 2016/12/19.
 */
@Table("sl_mortgage_order")
@Data
@NoArgsConstructor
public class MortgageOrder extends BaseModel  {

    @Column("orderId")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String orderId;

    @Column("mortgageId")
    private String mortgageId;

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
