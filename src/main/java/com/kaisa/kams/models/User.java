package com.kaisa.kams.models;

import com.kaisa.kams.enums.CertificateType;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.enums.PublicStatus;

import com.kaisa.kams.enums.UserType;
import org.nutz.dao.entity.annotation.*;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人员
 * Created by weid on 2016/11/9.
 */
@Table("sl_user")
@Data
@NoArgsConstructor
public class User extends BaseModel{

    /**
     * 姓名
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 岗位组织Id
     */
    @Column
    private String organizeId;

    /**
     * 岗位组织实例对象
     */
    @One(target = Organize.class, field = "organizeId")
    private Organize organize;

    /**
     * 角色
     */
    @ManyMany(target = Role.class, relation = "sl_roleuser", from = "user", to = "role")
    private List<Role> roles;

    /**
     * 登录ID
     */
    @Column("login")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String login;

    /**
     * 登录密码(已经加密过的)
     */
    @Column("password")
    @ColDefine(type = ColType.VARCHAR, width = 100)
    private String password;

    /**
     * 盐
     */
    @Column("salt")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String salt;

    /**
     * 有效标志
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private PublicStatus status;

    /**
     * 手机号
     */
    @Column("mobile")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String mobile;

    /**
     * 电子邮箱
     */
    @Column("email")
    @ColDefine(type = ColType.VARCHAR, width = 128)
    private String email;

    /**
     * 联系地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR, width = 256)
    private String address;

    /**
     * 性别
     */
    @Column("sex")
    @ColDefine(type = ColType.VARCHAR, width = 1)
    private String sex;

    /**
     * 在职状态  boolean类型 false表示离职 true表示在职 默认为true在职
     */
    @Column("companyStatus")
    private boolean companyStatus;

    /**
     * 出生日YYYY-MM-DD
     */
    @Column("birthDate")
    @ColDefine(type = ColType.DATE)
    private Date birthDate;

    /**
     * 入司日期YYYY-MM-DD
     */
    @Column("inCompanyDate")
    @ColDefine(type = ColType.DATE)
    private Date inCompanyDate;

    /**
     * 离司日期YYYY-MM-DD
     */
    @Column("outCompanyDate")
    @ColDefine(type = ColType.DATE)
    private Date outCompanyDate;

    /**
     * 证件类型
     */
    @Column("certificateType")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private CertificateType certificateType;

    /**
     * 证件号码
     */
    @Column("certificateNumber")
    @ColDefine(type = ColType.VARCHAR, width = 20)
    private String certificateNumber;

    /**
     * 用户角色名称
     */
    private String roleName;

    /**
     * 岗位名称
     */
    private String orgName;

    /**
     * 状态描述
     */
    private String statusDesc;
    /**
     * 用户类型
     */
    @Column("userType")
    private UserType userType;

    /**
     * 配置渠道
     */
    @Column("channels")
    @ColDefine(type = ColType.VARCHAR, width = 2048)
    private String channels;

    /**
     * 产品子类配置
     */
    @Column("products")
    @ColDefine(type = ColType.VARCHAR, width = 2048)
    private String products;

    /**
     * 人员类型
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private ChannelUserType type;

}

