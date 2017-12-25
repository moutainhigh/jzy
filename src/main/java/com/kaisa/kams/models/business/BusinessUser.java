package com.kaisa.kams.models.business;

import com.kaisa.kams.enums.CredentialsType;
import com.kaisa.kams.enums.Position;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.BaseModel;

import org.nutz.dao.entity.annotation.*;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务人员表
 * Created by pengyueyang on 2016/12/05.
 */
@Data
@NoArgsConstructor
@Table("sl_business_user")
public class BusinessUser extends BaseModel{

    /**
     * 业务员代码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String code;

    /**
     * 姓名
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 身份证
     */
    @Column("idNumber")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String idNumber;

    /**
     * 性别，0:女，1:男
     */
    @Column("sex")
    @ColDefine(type = ColType.INT, width=1)
    private int sex;

    /**
     * 出生日期
     */
    @Column("birthday")
    @ColDefine(type = ColType.DATE)
    private Date birthday;

    /**
     * 手机号
     */
    @Column("mobile")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String mobile;

    /**
     * 职级
     */
    @Column("position")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private Position position;

    /**
     * 所属组织Id
     */
    @Column("organizeId")
    private String organizeId;


    /**
     * 所属组织
     */
    @One(target = BusinessOrganize.class, field ="organizeId")
    private BusinessOrganize organize;


    /**
     * 入职日期
     */
    @Column("entryDate")
    @ColDefine(type = ColType.DATE)
    private Date entryDate;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 所属机构id
     */
    @Column("agencyId")
    private String agencyId;

    /**
     * 用户email
     */
    @Column("email")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String email;

    /**
     * 工作状态，0:在职，1:离职
     */
    @Column("workState")
    @ColDefine(type = ColType.INT, width=1)
    private int workState;

    /**
     * 详见CredentialsType 证件类型
     */
    @Column("credentialsType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private CredentialsType credentialsType;

    /**
     * 离职日期
     */
    @Column("quitDate")
    @ColDefine(type = ColType.DATE)
    private Date quitDate;

    /**
     * 联系地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR, width=254)
    private String address;

    /**
     * 关联用户表用户Id
     */
    @Column("userId")
    private String userId;
}
