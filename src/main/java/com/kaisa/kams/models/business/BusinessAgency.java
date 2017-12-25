package com.kaisa.kams.models.business;

import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.*;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务机构
 * Created by pengyueyang on 2016/12/05.
 */

@Data
@NoArgsConstructor
@Table("sl_business_agency")
public class BusinessAgency extends BaseModel{

    /**
     * 机构代码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=6)
    private String code;

    /**
     * 机构名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 机构级别 共为三级 1,2,3
     */
    @Column("level")
    @ColDefine(type = ColType.INT, width=4)
    private int level;

    /**
     * 上级组织Id
     */
    @Column("parentId")
    private String parentId;

    /**
     *上级组织实例对象
     */
    @One(target = BusinessAgency.class, field ="parentId")
    private BusinessAgency parent;

    /**
     * 上一级完整路径
     */
    @Column("path")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String path;

    /**
     * 机构地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String address;

    /**
     * 机构联系电话
     */
    @Column("telNumber")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String telNumber;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

}
