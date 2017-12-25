package com.kaisa.kams.components.security;

import com.kaisa.kams.models.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.List;
import java.util.Map;


/**
 *  shiro中session的管理
 * Created by weid on 2016/11/23.
 */
public class ShiroSession {

    /**
     * 获取session
     * @return 当前用户的session
     */
    public static Session getSession(){
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        return session;
    }

    /**
     * 往session中添加数据
     * @param key 键
     * @param value 值
     */
    public static void setAttribute(Object key,Object value){
        Session session = getSession();
        if (session!=null){
            session.setAttribute(key,value);
        }
    }

    /**
     * 从session中获取值
     * @param key
     * @return
     */
    public static Object getAttribute(Object key){
        Session session = getSession();
        if (session==null){
            return null;
        }
        return session.getAttribute(key);
    }

    /**
     * 返回登录主体
     * @return
     */
    public static Subject getSubject(){
       return  SecurityUtils.getSubject();
    }


    /**
     *获取当前session用户
     * @return
     */
    public static User getLoginUser(){
        Object o = getSession().getAttribute("user");
        return o==null ? null : (User)o;
    }

    /**
     *设置当前session用户
     */
    public static void setLoginUser(User user){
         getSession().setAttribute("user",user);
    }

    /**
     * 设置菜单
     * @param menus
     */
    public static void setMenu(List<Map> menus){
        getSession().setAttribute("menus",menus);
    }

    /**
     * 获取菜单
     * @return
     */
    public static List<Map> getMenu(){
        Object o = getSession().getAttribute("menus");
        return o==null ? null : (List<Map>)o;
    }

}
