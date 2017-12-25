package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.excelUtil.Condition;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.List;

/**
 * @description：费用免除审批类
 * @author：zhouchuang
 * @date：2017-11-14:48
 */
@Table("sl_cost_exemption")
@Data
@NoArgsConstructor
public class CostExemption extends BaseApproval{
    @Condition(condition = "LIKE",sql="%{}%")
    @Column("costExemptionCode")
    @Comment("费用免除编号")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String costExemptionCode;


    @Column("loanId")
    @Comment("业务单Id")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String loanId;


    @Column("业务员ID")
    @Comment("businessId")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String businessId;

    @Column("businessName")
    @Comment("业务员名字")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String businessName;


    public List<CostExemptionItem> costExemptionItemList;

    private String code;
    private String saleName;

}
