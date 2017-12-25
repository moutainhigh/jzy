package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;

/**
 *借款人账户信息表
 * Created by weid on 2016/12/12.
 */
@Table("sl_borrower_account")
@Data
@NoArgsConstructor
public class BorrowerAccount extends BaseModel{

    /**
     * 户名
     */
    @Column("name")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String name;

    /**
     * 开户行
     */
    @Column("bank")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String bank;

    /**
     * 收款账号
     */
    @Column("account")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String account;

    /**
     * 收款金额
     */
    @Column("amount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal amount;


    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private PublicStatus status;

    /**
     * 借款人Id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 顺序位置
     */
    @Column("position")
    @ColDefine(type = ColType.INT,width = 10)
    private int position;


    /**
     * 平台账户
     */
    @Column("platformAccount")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String platformAccount;

}
