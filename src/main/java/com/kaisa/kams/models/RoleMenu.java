package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.*;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色菜单权限表
 * Created by weid on 2016/11/17.
 */
@Table("sl_rolemenu")
@Data
@NoArgsConstructor
public class RoleMenu extends BaseModel{

    /**
     * 角色Id
     */
    @Column
    private String  roleId;

    /**
     * 角色
     */
    @One(target = Role.class, field ="roleId" )
    private Role role;

    /**
     * 菜单Id
     */
    @Column
    private String menuId;

    /**
     * 菜单id
     */
    @One(target = Menu.class, field ="menuId" )
    private Menu menu;

    /**
     * 权限
     */
    @Column("permission")
    @ColDefine(type = ColType.VARCHAR, width=4)
    private String permission;

    /**
     * 有效标志
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 按钮权限 按逗号隔开
     */
    @Column("buttonPermission")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    private String buttonPermission;


}
