package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * Created by sunwanchao on 2017/3/20.
 * 银行信息
 */
@Table("sl_bank_info")
@Data
@NoArgsConstructor
public class BankInfo extends BaseModel{
    /**
     * 业务申请Id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 开户银行名
     */
    @Column("bank")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String bank;

    /**
     * 账户名
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String name;

    /**
     * 账户
     */
    @Column("account")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String account;

    /**
     * 顺序位置
     */
    @Column("position")
    @ColDefine(type = ColType.INT,width = 10)
    private int position;
}
