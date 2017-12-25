package com.kaisa.kams.models;


import org.nutz.dao.entity.annotation.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by pengyueyang on 2016/11/24.
 * 放款主体账户
 */
@Table("sl_loan_subject_account")
@Data
@NoArgsConstructor
@PK("id")
public class LoanSubjectAccount{

    public String uuid(){
        return UUID.randomUUID().toString();
    }

    /**
     * 自增id
     */
    @Prev(els=@EL("uuid()"))
    private String id;

    /**
     * 放款主体Id
     */
    @Column("subjectId")
    private String subjectId;

    /**
     * 账户名
     */
    @Column("accountName")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String accountName;

    /**
     * 开户行
     */
    @Column("accountBank")
    @ColDefine(type = ColType.VARCHAR,width = 120)
    private String accountBank;

    /**
     * 账户
     */
    @Column("accountNo")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String accountNo;

    /**
     * 别名
     */
    @Column("alias")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String alias;

    @Column("position")
    @ColDefine(type = ColType.INT,width = 10)
    private int position;

}
