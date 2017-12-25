package com.kaisa.kams.components.service;

import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 权限相关接口
 * Created by weid on 2016/11/23.
 */
@IocBean(name = "authorityService",fields="dao")
public class AuthorityService {

    private static final Log log = Logs.get();

    /**
     * 扫描RequiresPermissions和RequiresRoles注解
     * @param pkg 需要扫描的package
     */
    public void initFormPackage(String pkg){


    }

    /**
     * 检查最基础的权限,确保admin用户-admin角色-(用户增删改查-权限增删改查)这一基础权限设置
     * @param admin
     */
    void checkBasicRoles(User admin){

    }

    /**
     * 添加一个权限
     */
    public void addPermission(String permission){

    }

    /**
     * 添加一个角色
     */
    public Role addRole(String role){
       return null;
    }


}
