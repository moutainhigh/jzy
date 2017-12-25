package com.kaisa.kams.models;

import com.kaisa.kams.enums.OperationType;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.EL;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Prev;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sunwanchao on 2016/11/23.
 * 用户操作记录审记表
 */
@Table("sl_audit")
@Data
@NoArgsConstructor
@PK("id")
public class Audit {

    public String uuid(){
        return UUID.randomUUID().toString();
    }

    @Prev(els=@EL("uuid()"))
    private String id;

    /**
     * 操作人姓名
     */
    @Column("operator")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String operator;

    /**
     * 操作类型
     */
    @Column("operatType")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private OperationType operationType;

    /**
     * 职位
     */
    @Column("position")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String position;

    /**
     * 用户Id
     */
    @Column("userId")
    private String userId;

    /**
     * 描述
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR,width = 254)
    private String description;

    /**
     * 创建时间
     */
    @Column("createTime")
    @ColDefine(type = ColType.DATETIME)
    private Date createTime;

}
