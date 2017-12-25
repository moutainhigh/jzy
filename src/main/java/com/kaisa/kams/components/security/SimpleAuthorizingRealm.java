package com.kaisa.kams.components.security;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.integration.shiro.SimpleShiroToken;
import org.nutz.mvc.Mvcs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * shiro安全域
 */
public class SimpleAuthorizingRealm extends AuthorizingRealm {

    protected Dao dao; // ShiroFilter先于NutFilter初始化化,所以无法使用注入功能

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }
        String userId =  principals.getPrimaryPrincipal().toString();
        User user = dao().fetchLinks(dao().fetch(User.class, userId), "roles");
        if (user == null) {
            return null;
        }
        SimpleAuthorizationInfo auth = new SimpleAuthorizationInfo();
        if (user.getRoles() != null) {
            //dao().fetchLinks(user.getRoles(), null);
            Set<String> roleIds = new HashSet<>();
            for (Role role : user.getRoles()) {
                  auth.addRole(role.getName());
                  roleIds.add(role.getId());
//                auth.addRole(role.getName());
//                List<RoleMenu> rms = dao.query(RoleMenu.class, Cnd.where("roleId","=",role.getId()));
//                for (RoleMenu rm : rms){
//                    if(null==rm) continue;
//
//                    //获取到相应的菜单
//                    Menu menu = dao.fetch(Menu.class,rm.getMenuId());
//
//                    if(null==menu) continue;
//
//                    //添加权限
//                    auth.addObjectPermission(new WildcardPermission(menu.getAlias()+":"+BitPermissionResolver.getPermission(rm.getPermission())));
//                   // auth.addStringPermission("+"+menu.getAlias()+"+"+Integer.valueOf(rm.getPermission(),2));
//                }
            }
            List<HashMap> permissions = findPermissions(roleIds);
            if (CollectionUtils.isNotEmpty(permissions)) {
                for(HashMap permission:permissions) {
                    String bntPermission ="";
                    if(StringFormatUtils.isNotEmpty(permission.get("buttonPermission"))){
                        bntPermission =","+(String)permission.get("buttonPermission");
                    }
                    auth.addObjectPermission(new WildcardPermission(permission.get("alias")+":"+BitPermissionResolver.getPermission((String)permission.get("permission"))+bntPermission));
                }
            }
        }
        return auth;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        SimpleShiroToken upToken = (SimpleShiroToken) token;
        // upToken.getPrincipal() 的返回值就是SimpleShiroToken构造方法传入的值
        // 可以是int也可以是User类实例,或任何你希望的值,自行处理一下就好了
        User user = dao().fetch(User.class, upToken.getPrincipal().toString());
        if (user == null)  return null;
        return new SimpleAccount(user.getId(), user.getPassword(), getName());
    }

    /**
     * 覆盖父类的验证,直接pass. 在shiro内做验证的话, 出错了都不知道哪里错
     */
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
    }

    public SimpleAuthorizingRealm() {
        this(null, null);
    }

    public SimpleAuthorizingRealm(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
        setAuthenticationTokenClass(SimpleShiroToken.class); // 非常非常重要,与SecurityUtils.getSubject().login是对应关系!!!
    }

    public SimpleAuthorizingRealm(CacheManager cacheManager) {
        this(cacheManager, null);
    }

    public SimpleAuthorizingRealm(CredentialsMatcher matcher) {
        this(null, matcher);
    }

    public Dao dao() {
        if (dao == null) {
            dao = Mvcs.ctx().getDefaultIoc().get(Dao.class, "dao");
            return dao;
        }
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    public List<HashMap> findPermissions(Set<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return null;
        }
        Sql sql = Sqls.create("select rm.buttonPermission,rm.permission permission,m.alias alias from sl_rolemenu rm inner join sl_menu m on rm.menuId=m.id " +
                "where rm.roleId in(@roleIds)");
        sql.params().set("roleIds", roleIds.toArray());
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<HashMap> list = new ArrayList<HashMap>();
                while (rs.next()) {
                    if (StringUtils.isNotEmpty(rs.getString("alias"))&&StringUtils.isNotEmpty(rs.getString("permission"))) {
                        HashMap map = new HashMap<>();
                        map.put("alias", rs.getString("alias"));
                        map.put("permission", rs.getString("permission"));
                        map.put("buttonPermission",rs.getString("buttonPermission"));
                        list.add(map);
                    }
                }
                return list;
            }
        });
        dao().execute(sql);
        return sql.getList(HashMap.class);

    }

}