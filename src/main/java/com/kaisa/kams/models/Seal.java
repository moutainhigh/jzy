package com.kaisa.kams.models;

import com.kaisa.kams.enums.SealStatus;
import com.kaisa.kams.models.business.BusinessUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.Date;

/**
 * Created by liuwen01 on 2016/12/14.
 * 用印管理表
 */
@Table("sl_seal")
@Data
@NoArgsConstructor
public class Seal extends BaseModel{

    /**
     * 借款id
     */
    @Column("loanId")
    private String loanId;


    /**
     * 用印时间
     */
    @Column("useTime")
    @ColDefine(type = ColType.DATETIME)
    private Date useTime;

    /**
     * 是否用印
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private SealStatus status;

}
