package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 风控录单填写信息
 * Created by weid on 2016/12/20.
 */
@Table("sl_loan_risk_info")
@Data
@NoArgsConstructor
public class LoanRiskInfo extends BaseModel {

    @Column("loanId")
    private String loanId;

    /**
     * 填单内容
     */
    @Column("content")
    @ColDefine(type = ColType.VARCHAR,width = 1000)
    private String content;


    //展期的ID，如果是业务单的授信，则么有这个ID
    @Column("extensionId")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String extensionId;

}
