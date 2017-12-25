package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.EndecryptUtils;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.components.utils.ApiPropsUtils;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.enums.UserType;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;

import com.kaisa.kams.models.business.BusinessUser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.dao.util.Daos;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.integration.shiro.SimpleShiroToken;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 用户服务层
 * Created by weid on 2016/11/17.
 */
@IocBean(fields = "dao")
public class UserService extends IdNameEntityService<User> {

    private static final Log log = Logs.get();

    @Inject
    private OrganizeService organizeService;

    @Inject
    private ConfigService configService;

    @Inject
    private BusinessUserService businessUserService;

    public String add(User user) {
        String msg = this.checkUser(user);
        if (StringUtils.isNotEmpty(msg)) {
            return msg;
        }

       /* Cnd cnd = Cnd.where("name", "=", user.getName()).and("status", "=", PublicStatus.ABLE);
        if (dao().fetch(User.class,cnd) != null) {
            return "用户姓名重复,新增失败.";
        }*/

        Cnd cnd = Cnd.where("login", "=", user.getLogin()).and("status", "=", PublicStatus.ABLE);
        if (dao().fetch(User.class, cnd) != null) {
            return "登录名重复,新增失败.";
        }

        cnd = Cnd.where("certificateNumber", "=", user.getCertificateNumber()).and("status", "=", PublicStatus.ABLE);
        if (dao().fetch(User.class, cnd) != null) {
            return "证件号码重复,新增失败.";
        }

        cnd = Cnd.where("mobile", "=", user.getMobile()).and("status", "=", PublicStatus.ABLE);
        if (dao().fetch(User.class,cnd) != null) {
           return "手机号码重复,新增失败.";
        }
        // 添加用户基本信息
        String sixRandoms = (int) ((Math.random() * 9 + 1) * 100000) + "";
        user.setPassword(EndecryptUtils.md5Encrypt(sixRandoms));
        user.setStatus(PublicStatus.ABLE);
        user.setCreateTime(new Date());
        user.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        User currentUser = dao().insert(user);
        if (currentUser != null) {
            //发送短信
            try {
//                String message = String.format("%s，您好！您的账号：%s，初始密码：%s，请及时登录系统修改初始密码。",user.getName(), user.getLogin(),sixRandoms);
//                configService.sendMessage(message, user.getMobile());
                Map msgMap = new HashMap<String,String>();
                msgMap.put("name",user.getName());
                msgMap.put("account",user.getLogin());
                msgMap.put("password",sixRandoms);
                String resultStr = ApiRequestUtil.sendSmsByAPI(user.getMobile().trim(),msgMap, ApiPropsUtils.getValueByKey("initial_password_tempId"));
                log.info("sendMessageForinitialPassword job result"+resultStr);
            } catch (Exception e) {
                log.error("failed to send sms for user add in UserService,exception={}", e);
            }
            // 添加角色用户信息表
            if (dao().insertRelation(currentUser, "roles") != null) {
                return "SUCCESS:"+currentUser.getId();
            }
        }
        return "新增用户失败.";
    }

    private String checkUser(User user) {
        if (user.isCompanyStatus() && null != user.getOutCompanyDate()) {

            if (isBeforeNow(user.getOutCompanyDate())) {
                return "在职人员的离司时间不能早于当前时间.";
            }

            if (inCompanyDateIsBeforeOutCompanyDate(user.getInCompanyDate(), user.getOutCompanyDate())) {
                return "在职人员的入司时间不能晚于离司时间.";
            }
        }

        if (!user.isCompanyStatus()) {
            if (null == user.getOutCompanyDate()) {
                return "离职人员的离司日期不能为空.";
            } else {
                if (inCompanyDateIsBeforeOutCompanyDate(user.getInCompanyDate(), user.getOutCompanyDate())) {
                    return "离职人员的入司时间不能晚于离司时间.";
                }
            }
        }
        return null;
    }

    private boolean isBeforeNow(Date appointDate) {
        LocalDate now = LocalDate.now();
        LocalDate date = appointDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return date.isBefore(now);
    }

    private boolean inCompanyDateIsBeforeOutCompanyDate(Date inCompanyDate,Date outCompanyDate) {
        LocalDate inDate = inCompanyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate  outDate = outCompanyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return inDate.isBefore(outDate);
    }

    /**
     * 根据用户名和密码查询
     *
     * @param login    登录名
     * @param password 未加密密码
     * @return
     */
    public User fetch(String login, String password) {
        String encryptPassword = EndecryptUtils.md5Encrypt(password);
        SqlExpressionGroup cndExpObject = Cnd.exps("login", "=", login).or("mobile", "=", login);
        Condition cnd = Cnd.where(cndExpObject).and("password","=",encryptPassword).and("status", "=", PublicStatus.ABLE);
        List<User> userList = dao().query(User.class, cnd);
        if (null != userList && userList.size() > 0) {
            return userList.get(0);
        }
        return null;
    }

    /**
     * 重置用户密码
     *
     * @param id
     * @param newPassword
     * @return
     */
    public boolean resetPassword(String id, String newPassword) {
        try {
            User user = dao().fetch(User.class, Cnd.where("id", "=", id).and("status", "=", PublicStatus.ABLE));
            user.setPassword(EndecryptUtils.md5Encrypt(newPassword));
            int result = dao().update(user);
            if (result > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error("fail to resetPw in UserService,exception={}" + e);
            return false;
        }
        return false;
    }

    /**
     * 通过条件统计user条数
     *
     * @param cnd
     * @return
     */
    public int countUser(Cnd cnd) {
        return dao().count(User.class, cnd);
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @return
     */
    public String update(User user) {

        User oldUser = dao().fetch(user);
        if (null == oldUser) {
            return "用户信息不存在,修改失败.";
        }

        String msg = this.checkUser(user);
        if (StringUtils.isNotEmpty(msg)) {
            return msg;
        }

        Cnd cnd = Cnd.where("login", "=", user.getLogin()).and("status", "=", PublicStatus.ABLE);
        User userLogin = dao().fetch(User.class, cnd);
        if (userLogin != null && !userLogin.getId().equals(user.getId())) {
            return "登录名重复,修改失败.";
        }

        cnd = Cnd.where("certificateNumber", "=", user.getCertificateNumber()).and("status", "=", PublicStatus.ABLE);
        User userCertificateNumber = dao().fetch(User.class, cnd);
        if (userCertificateNumber != null && !userCertificateNumber.getId().equals(user.getId())) {
            return "证件号码重复,修改失败.";
        }

        cnd = Cnd.where("mobile", "=", user.getMobile()).and("status", "=", PublicStatus.ABLE);
        User userMobile = dao().fetch(User.class, cnd);
        if (userMobile != null && !userMobile.getId().equals(user.getId())) {
            return "手机号码重复,修改失败.";
        }

        if (ShiroSession.getLoginUser() != null) {
            user.setUpdateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        }
        user.setUpdateTime(new Date());

        if (UserType.BUSINESS_USER.equals(oldUser.getUserType()) && isChangeMobileOrIdNumber(oldUser,user)) {
            BusinessUser businessUser = new BusinessUser();
            businessUser.setUserId(user.getId());
            businessUser.setMobile(user.getMobile());
            businessUser.setIdNumber(user.getCertificateNumber());
            businessUserService.updateMobileAndIdNumber(businessUser);
        }
        if(null != user.getType() && ChannelUserType.COMPANY_USER.equals(user.getType())){
            user.setProducts("");
            user.setChannels("");
        }
        int num = Daos.ext(dao(), FieldFilter.locked(User.class, "^id|organizeId|salt|userType$")).update(user);
        if (num > 0) {
            dao().clearLinks(user, "roles");
            User relationUser = dao().insertRelation(user, "roles");
            if (relationUser != null) {
                return "";
            }
        }
        return "修改用户信息失败.";
    }


    public boolean updateUserPassword(User user) {
        String sixRandoms = (int) ((Math.random() * 9 + 1) * 100000) + "";
        user.setPassword(EndecryptUtils.md5Encrypt(sixRandoms));
        if (ShiroSession.getLoginUser() != null) {
            user.setUpdateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        }
        user.setUpdateTime(new Date());
        boolean result = dao().update(user, "^(updateTime|updateBy|password)$")>0?true:false;
        if (result) {
            try {
//                String message = String.format("尊敬的用户，您的密码找回成功，新密码为：%s，您可以登录系统修改密码。",sixRandoms);
//                configService.sendMessage(message, user.getMobile());
                Map msgMap = new HashMap<String,String>();
                msgMap.put("password",sixRandoms);
                String resultStr = ApiRequestUtil.sendSmsByAPI(user.getMobile().trim(),msgMap, ApiPropsUtils.getValueByKey("forgot_password_tempId"));
                log.info("sendMessageForforgotPassword job result"+resultStr);
            } catch (Exception e) {
                log.error("failed to send sms for user update in UserService,exception={}", e);
            }
        }
        return result;
    }

    /**
     * 通过id查询角色信息（包含角色信息，可以查出无效）
     *
     * @param id
     * @return
     */
    public User fetchLinksById(String id) {
        return dao().fetchLinks(dao().fetch(User.class, id), "roles");
    }

    /**
     * 通过id查询角色信息
     *
     * @param id
     * @return
     */
    public User fetchById(String id) {
        return dao().fetch(User.class, id);
    }

    /**
     * 通过id查询角色信息（包含角色信息，有效数据）
     *
     * @param id
     * @return
     */
    public User fetchLinksByIdValid(String id) {
        return dao().fetchLinks(dao().fetch(User.class, Cnd.where("id", "=", id).and("status", "=", PublicStatus.ABLE)), "roles");
    }

    /**
     * 查询所有有效人员
     *
     * @return
     */
    public List<User> queryAllValid() {
        List<User> list = dao().query(User.class, Cnd.where("status", "=", PublicStatus.ABLE));
        return list;
    }

    /**
     * 获取拼接用户角色名称
     *
     * @param id
     * @return
     */
    public String getRoleName(String id) {
        StringBuffer roleName = new StringBuffer();
        User user = fetchLinksById(id);
        if (user != null && CollectionUtils.isNotEmpty(user.getRoles())) {
            List<Role> roles = user.getRoles();
            for (int i = 0; i < roles.size(); i++) {
                Role role =  roles.get(i);
                if (i + 1 < roles.size()) {
                    roleName.append(role.getMenus());
                    roleName.append("|");
                }
                if (i == roles.size() - 1) {
                    roleName.append(role.getMenus());
                }
            }
        }
        return roleName.toString();
    }

    /**
     * 通过查询条件查询用户信息
     * @return
     */
    public DataTables queryByParam(DataTableParam param) {
        List<User> listU = new ArrayList<>();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String role = "";
        String mobile = "";
        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            role = keys.get("role");
            mobile = keys.get("mobile");
            name = keys.get("name");
        }
        if (StringUtils.isNotEmpty(role)) {
            Sql sql = Sqls.create("SELECT * from sl_user t where EXISTS(select 1 from sl_roleuser a,sl_role b where a.role=b.id and a.`user`=t.id and b.name like '%" + role + "%') LIMIT " + param.getStart() + "," + param.getLength());
            Sql sqlCount = Sqls.create("SELECT * from sl_user t where EXISTS(select 1 from sl_roleuser a,sl_role b where a.role=b.id and a.`user`=t.id and b.name like '%" + role + "%')");
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao().getEntity(User.class));
            dao().execute(sql);
            List<User> list = sql.getList(User.class);
            if (list.size() > 0) {
                for (User u : list) {
                    u.setStatusDesc(u.getStatus().equals(PublicStatus.ABLE) ? "有效" : "无效");
                    u = dao().fetchLinks(u, "roles");
                }
            }
            sqlCount.setCallback(Sqls.callback.entities());
            sqlCount.setEntity(dao().getEntity(User.class));
            dao().execute(sqlCount);
            List<User> listCount = sqlCount.getList(User.class);
            return new DataTables(param.getDraw(), dao().count(User.class), listCount.size(), list);
        } else {
            Cnd cnd = Cnd.where("mobile", "like", "%" + mobile + "%").and("name", "like", "%" + name + "%");
            List<User> listUser = dao().query(User.class, cnd, pager);
            if (listUser.size() > 0) {
                for (User u : listUser) {
                    User user = dao().fetchLinks(u, "roles");
                    user.setStatusDesc(u.getStatus().equals(PublicStatus.ABLE) ? "有效" : "无效");
                    listU.add(user);
                }
            }
            return new DataTables(param.getDraw(), dao().count(User.class), dao().count(User.class, cnd), listU);
        }
    }

    /**
     * 根据login查询user
     *
     * @param login
     * @return
     */
    public User fetchByLogin(String login) {
        return dao().fetch(User.class, Cnd.where("login", "=", login).and("status", "=", PublicStatus.ABLE));
    }

    /**
     * 根据name查询user
     *
     * @param name
     * @return
     */
    public User fetchByName(String name) {
        return dao().fetch(User.class, Cnd.where("name", "=", name).and("status", "=", PublicStatus.ABLE));
    }

    /**
     * 根据账户或者手机号查询user
     *
     * @param login
     * @return
     */
    public User fetchByNameOrMobile(String login) {
        return dao().fetch(User.class, Cnd.where("login", "=", login).or("mobile", "=", login).and("status", "=", PublicStatus.ABLE));
    }

    /**
     * 按角色Id查询用户信息(批量)
     * @param id
     * @return
     */
    public List<Map> queryUserListByRoleId(String id) {
        Sql sql = Sqls.create("SELECT a.id,a.name FROM sl_user a left join sl_roleuser b on a.id=b.user $condition");
        Criteria cri = Cnd.cri();
        if (StringUtils.isNotEmpty(id)) {
            cri.where().and("b.role","=",id);
        }
        sql.setCondition(cri);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<>();
                while (rs.next()) {
                    Map tmp = new HashMap();
                    tmp.put("id", rs.getString("id"));
                    tmp.put("name", rs.getString("name"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        return sql.getList(Map.class);
    }

    public boolean loginByShiro(User user) {
        try{
            SecurityUtils.getSubject().login(new SimpleShiroToken(user.getId()));
            ShiroSession.setLoginUser(user);
        }catch (Exception e){
            e.printStackTrace();
            log.debug("Shiro验证失败，exception={}",e);
            return false;
        }
        return true;
    }

    public String updateUserMobileAndIdNumber(User user) {
        Cnd cnd = Cnd.where("certificateNumber", "=", user.getCertificateNumber()).and("status", "=", PublicStatus.ABLE);
        User userCertificateNumber = dao().fetch(User.class, cnd);
        if (userCertificateNumber != null && !userCertificateNumber.getId().equals(user.getId())) {
            return "证件号码重复,修改失败.";
        }

        cnd = Cnd.where("mobile", "=", user.getMobile()).and("status", "=", PublicStatus.ABLE);
        User userMobile = dao().fetch(User.class, cnd);
        if (userMobile != null && !userMobile.getId().equals(user.getId())) {
            return "手机号码重复,修改失败.";
        }
        return Daos.ext(dao(), FieldFilter.create(User.class, "^login|mobile|certificateNumber$",true)).update(user)>0?"":"数据库更新错,修改失败.";
    }

    private boolean isChangeMobileOrIdNumber(User oldUser, User user) {
        if (!oldUser.getMobile().equals(user.getMobile())) {
            return true;
        }
        if (!oldUser.getCertificateNumber().equals(user.getCertificateNumber())) {
            return true;
        }
        return false;
    }
}
