package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 描述：费用减免的相关订单
 * 创建人：zhouchuang
 * 创建日期：2017-11-14:49
 */
@Table("sl_cost_exemption_order")
@Data
@NoArgsConstructor
public class CostExemptionOrder extends BaseOrder {
    @Column("costExemptionId")
    private String costExemptionId;
}
