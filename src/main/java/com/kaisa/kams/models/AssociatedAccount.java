package com.kaisa.kams.models;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhouchuang on 2017/8/14.
 */
@Table("sl_oa_associated_account")
@Data
@NoArgsConstructor
public class AssociatedAccount extends BaseModel{
    /**
     * 资产账号
     */
    @Column("kamsUserAccount")
    @ColDefine(type = ColType.VARCHAR,width = 48)
    private String kamsUserAccount;

    /**
     * 资产里面的ID
     */
    @Column("kamsUserId")
    @ColDefine(type = ColType.VARCHAR,width = 48)
    private String kamsUserId;

    /**
     * 资产系统里面人员姓名
     */
    @Column("kamsUserName")
    @ColDefine(type = ColType.VARCHAR,width = 48)
    private String kamsUserName;

    /**
     * OA账号
     */
    @Column("oaUserAccount")
    @ColDefine(type = ColType.VARCHAR,width = 48)
    private String oaUserAccount;


}
