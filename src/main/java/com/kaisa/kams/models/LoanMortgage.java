package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

/**
 * Created with IntelliJ IDEA
 * Created By dengly
 * Date: 2017/9/18
 * 业务抵押关联表
 */

@Table("sl_loan_mortgage")
@Data
@NoArgsConstructor
public class LoanMortgage {


    /**
     * 产品类型Id
     */
    @Column("loanId")
    @ColDefine(type= ColType.VARCHAR,width=50)
    private String loanId;

    /**
     * 房产抵押编号
     */
    @Column("mortgageCode")
    @ColDefine(type=ColType.VARCHAR,width=64)
    private String mortgageCode;
}
