package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;
import com.sun.xml.internal.xsom.impl.util.SchemaTreeTraverser;
import org.nutz.dao.*;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 业务人员服务层
 * Created by luoyj on 2016/12/12.
 */
@IocBean(fields="dao")
public class BusinessUserService extends IdNameEntityService<BusinessUser>{
    private static final Log log = Logs.get();
    @Inject
    private UserService userService;




    /**
     * 根据查询类型和id 查询业务员人员
     * @param param
     * @return
     */
    public  DataTables queryParam(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

        String sId = "";
        String searchType = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            sId = keys.get("sId");
            searchType = keys.get("searchType");
        }
        List<BusinessUser> uList = new ArrayList<BusinessUser>();
        Cnd cnd = null;
        DataTables dataTables=null;
        try {
            log.info("sId:"+sId+",searchType:"+searchType);
            if (searchType.equals("agencyType")&&!Strings.isBlank(sId)) {
                cnd = Cnd.where("agencyId", "=", sId);
            }
            // 按照组织id查询业务员
            if (searchType.equals("orgType")&&!Strings.isBlank(sId)) {
                cnd = Cnd.where("organizeId", "=", sId);
            }
            // 按照人员id
            if (searchType.equals("userType")&&!Strings.isBlank(sId)) {
                cnd = Cnd.where("id", "=", sId);
            }
            //Pager pager= DataTablesUtil.getDataTableToPager(start,length);
            List<BusinessUser> userList=dao().query(BusinessUser.class,cnd==null?Cnd.orderBy().asc("createTime"):cnd.asc("createTime"),pager);
            for (BusinessUser b:userList) {
                BusinessOrganize org=dao().fetchLinks(dao().fetch(BusinessOrganize.class,b.getOrganizeId()),"agency");
                b.setOrganize(org);
                uList.add(b);
            }
            dataTables=new DataTables(param.getDraw(),dao().count(BusinessUser.class),dao().count(BusinessUser.class,cnd),uList);
            dataTables.setOk(true);
            dataTables.setMsg("成功");
        }catch (Exception e){
            log.error(e.getMessage());
            dataTables=new DataTables(0,0,0,null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;
    }


    /**
     * 模糊查询，根据code/name 查询业务业务人员
     * @param search
     * @return
     */
    public   List<BusinessUser> queryBySearch(String search){
        NutMap nutMap=new NutMap();
        List<BusinessUser> uList=null;
            if(!Strings.isBlank(search)) {
                Cnd cnd = Cnd.where("name", "like", "%" + search + "%").or("code", "like", "%" + search + "%");
                FieldMatcher fieldMatcher = new FieldMatcher();
                fieldMatcher.setActived("^id|name|code$");
                uList = dao().query(BusinessUser.class,cnd,null,fieldMatcher);
            }
        return uList;
    }


    /**
     * 新增业务员人员
     * @param b
     * @return
     */
    public boolean  add(BusinessUser b){

        log.info("businessUser:"+b.toString());

        // 判断业务员code是否重复
        BusinessUser b_user=dao().fetch(BusinessUser.class,Cnd.where("code","=",b.getCode()));
       if(b_user!=null){
           // 如果重复了，重新生成code
           b.setCode(getUserCode());
       }
        User user= ShiroSession.getLoginUser();
        // 获取新增的记录的人员
        if(user!=null){
            b.setCreateBy(String.valueOf(user.getName()));
        }
        b.setCreateTime(new Date());

        if(dao().insert(b)!=null){
            return true;
        }
        return false;
    }



    /**
     * 修改业务员人员
     * @param businessUser
     * @return
     */
    public  boolean update(BusinessUser businessUser){
        log.info("businessUser:"+businessUser.toString());
        User user= ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if(user!=null){
            user.setUpdateBy(user.getName());
        }
        user.setUpdateTime(new Date());
        return Daos.ext(dao(), FieldFilter.locked(BusinessUser.class, "^id|code|createBy|createTime|userId$")).update(businessUser)>0;
    }

    /**
     * 通过id查询业务员人员信息
     * @param id
     * @return
     */
    public BusinessUser fetchById(String id){
        NutMap nutMap=new NutMap();
        BusinessUser b_user=dao().fetch(BusinessUser.class,id);
            if (b_user!=null){
                 BusinessOrganize org=dao().fetchLinks(dao().fetch(BusinessOrganize.class,b_user.getOrganizeId()),"agency");
                b_user.setOrganize(org);
            }
        return b_user;
    }

    public  BusinessUser fetchByName(String name){
        BusinessUser b_user=dao().fetch(BusinessUser.class,Cnd.where("name","=",name));
        return b_user;
    }

    public  BusinessUser fetchByIdWithoutOrg(String id){
        BusinessUser b_user=dao().fetch(BusinessUser.class,id);
        return b_user;
    }

    public  BusinessUser fetchByMoblie(String mobile){
        BusinessUser b_user=dao().fetch(BusinessUser.class,Cnd.where("mobile","=",mobile));
        return b_user;
    }

    public  BusinessUser fetchByIdNumber(String idNumber){
        BusinessUser b_user=dao().fetch(BusinessUser.class,Cnd.where("idNumber","=",idNumber));
        return b_user;
    }


    /**
     * 获取业务职级信息
     * @return
     */
    public   List<NutMap> getPosition(){
        List<NutMap>  list=new ArrayList<NutMap>();
        try{
            for (Position p: Position.values()) {
                NutMap map=new NutMap();
                map.setv("name",p.getDescription());
                map.setv("value",p.toString());
                list.add(map);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
      return list;
    }

    /**
     * 获取业务员code
     * @return
     */
    public String getUserCode(){
        DecimalFormat format = new DecimalFormat("0000000");
        String code="";
        try {
            // 查询业务员人，按照新增记录倒叙排序
            List<BusinessUser> uList=dao().query(BusinessUser.class,Cnd.orderBy().desc("createTime"));
            if(uList.size()>0){
                String u_code=uList.get(0).getCode();
                code=format.format(Integer.valueOf(u_code)+1);
            }else {
                // 说明没有人员，第一个人员code为：0000001
                code="0000001";
            }

        }catch (Exception e){
            log.error(e.getMessage());

        }
        return code;
    }


    /**
     * 获取用户证件类型
     * @return
     */
    public  List<NutMap>  getUserCredentialsType(){
        List<NutMap>  list=new ArrayList<NutMap>();
        try{
            for (CredentialsType c: CredentialsType.values()) {
                NutMap map=new NutMap();
                map.setv("name",c.getDescription());
                map.setv("value",c.toString());
                list.add(map);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    public String getLoginName(String  userId){
        String loginName="";
        User user=userService.fetchLinksByIdValid(userId);
        if(user!=null){
            loginName=user.getLogin();
        }
        return loginName;
    }

    public List<String> list(String code){
        String sqlStr  = "select bu.id id from sl_business_user bu ,sl_business_organize bo " +
                " where bu.organizeId =  bo.id " +
                " AND bo.code like CONCAT('"+code+"','%')";

        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<String> list = new LinkedList<String>();
                while (rs.next()) {
                    list.add(rs.getString("id"));
                }
                return list;
            }
        });
        dao().execute(sql);
        List<String> list = sql.getList(String.class);
        return list;
    }

    public boolean updateUserId(BusinessUser businessUser) {
        return Daos.ext(dao(), FieldFilter.create(BusinessUser.class, "^userId$")).update(businessUser)>0;
    }

    public boolean updateMobileAndIdNumber(BusinessUser businessUser) {
        return Daos.ext(dao(), FieldFilter.create(BusinessUser.class, "^mobile|idNumber$",true)).
                update(BusinessUser.class,Chain.make("mobile",businessUser.getMobile()).add("idNumber",businessUser.getIdNumber()), Cnd.where("userId","=", businessUser.getUserId()))>0;
    }
}


