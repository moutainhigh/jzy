package com.kaisa.kams.models;

import com.kaisa.kams.enums.OrganizeType;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.*;

import java.io.PipedReader;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组织
 * Created by weid on 2016/11/17.
 */
@Table("sl_organize")
@Data
@NoArgsConstructor
public class Organize extends BaseModel{

    /**
     * 组织名称
     */
    @Name
    @Column
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 上级组织Id
     */
    @Column
    private String parentId;

    /**
     *上级组织实例对象
     */
    @One(target = Organize.class, field ="id" )
    private Organize parent;

    /**
     * 组织下对应的人员
     */
    @Many(target = User.class, field ="id" )
    private List<User> users;

    /**
     * organizeType
     */
    @Column("organizeType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private OrganizeType organizeType;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 上一级完整路径
     */
    @Column("path")
    @ColDefine(type = ColType.VARCHAR, width=254)
    private String path;

    /**
     *  组织或者机构代码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String code;

    /**
     * 机构名称
     */
    @Column("agencyName")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String agencyName;

    /**
     * 机构地址
     */
    @Column("agencyAddress")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String agencyAddress;

    /**
     * 机构联系电话
     */
    @Column("agencyTel")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String agencyTel;

    /**
     * 组织类型code
     */
    private String organizeCode;


    public Organize(String parentId, String name,OrganizeType organizeType,PublicStatus status,String path){
        this.parentId=parentId;
        this.name=name;
        this.organizeType=organizeType;
        this.status=status;
        this.path=path;
    }
}
