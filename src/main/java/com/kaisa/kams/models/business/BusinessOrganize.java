package com.kaisa.kams.models.business;

import com.kaisa.kams.enums.BusinessLine;
import com.kaisa.kams.enums.BusinessOrganizeType;
import com.kaisa.kams.enums.Position;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务组织
 * Created by pengyueyang on 2016/12/05.
 */
@Data
@NoArgsConstructor
@Table("sl_business_organize")
public class BusinessOrganize extends BaseModel{

    /**
     * BusinessOrganizeType 类型：部和组
     */
    @Column("organizeType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private BusinessOrganizeType organizeType;

    /**
     * 上级组织Id
     */
    @Column("parentId")
    private String parentId;

    /**
     * 上级组织实例对象
     */
    @One(target = BusinessOrganize.class, field ="parentId")
    private BusinessOrganize parent;

    /**
     * 所属机构Id
     */
    @Column("agencyId")
    private String agencyId;

    /**
     * 所属机构
     */
    @One(target = BusinessAgency.class, field ="agencyId" )
    private BusinessAgency agency;

    /**
     *  组织代码 部级组织代码格式为Z+3位数字 组级组织代码为上级组织代码+2位数字
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String code;

    /**
     * 组织名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 主管姓名
     */
    @Column("managerName")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String managerName;


    /**
     * 主管职级
     */
    @Column("managerPosition")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private Position managerPosition;

    /**
     * 联系地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String address;

    /**
     * 联系电话
     */
    @Column("telNumber")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String telNumber;

    /**
     * 建立时间
     */
    @Column("establishDate")
    @ColDefine(type = ColType.DATE)
    private Date establishDate;

    /**
     * 业务条线
     */
    @Column("businessLine")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private BusinessLine businessLine;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     *  撤销时间
     */
    @Column("revokeDate")
    @ColDefine(type = ColType.DATE)
    private Date revokeDate;

    /**
     * 组织别名
     */
    @Column("aliasName")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String aliasName;


    /**
     * 组织上一级完整路径
     */
    @Column("path")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String path;

    /**
     * 所属机构完整路径
     */
    @Column("agencyPath")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String agencyPath;

    /**
     * 主管Id
     */
    @Column("managerId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String managerId;
}
