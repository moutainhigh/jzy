package com.kaisa.kams.components.flow;

import com.kaisa.kams.components.service.RoleService;
import com.kaisa.kams.components.service.UserService;
import org.nutz.ioc.loader.annotation.Inject;
import org.snaker.engine.impl.GeneralAccessStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by weid on 2016/12/2.
 */
public class CustomAccessStrategy extends GeneralAccessStrategy{

    @Inject
    private RoleService roleService;

    @Inject
    private UserService userService;

    protected List<String> ensureGroup(int roleId){
        List<String> group = new ArrayList<>();

        //根据操作角色获取到用户
        List<Map> users = userService.queryUserListByRoleId(roleId+"");
        for (Map user: users){
            group.add(null==user ? null :(String)user.get("id"));
        }
        return group;
    }
}
