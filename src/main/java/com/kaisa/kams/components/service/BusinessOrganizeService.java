package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.enums.BusinessLine;
import com.kaisa.kams.enums.BusinessOrganizeType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessOrganize;

import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 组织服务层
 * Created by luoyj on 2016/12/08.
 */
@IocBean(fields="dao")
public class BusinessOrganizeService extends IdNameEntityService<BusinessOrganize>{
    private static final Log log = Logs.get();
    /**
     * 获取有组织数据
     * @return
     */
     public DataTables queryByParam(DataTableParam param){
         Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());

         String agencyId = "";
         String orgId = "";
         String businessLine = "";
         String organizeType = "";
         String date = "";
         if (null != param.getSearchKeys()) {
             Map<String,String> keys = param.getSearchKeys();
             agencyId = keys.get("agencyId");
             orgId = keys.get("orgId");
             businessLine = keys.get("businessLine");
             organizeType = keys.get("organizeType");
             date = keys.get("date");
         }
         List<BusinessOrganize> buList=new ArrayList<>();
         List<BusinessOrganize> list;
         DataTables dataTables;
         try{
             log.info("agencyId:"+agencyId+",orgId:"+orgId+",businessLine:"+businessLine+",organizeType:"+organizeType+",date:"+date);
             Cnd cnd;
             if(!Strings.isBlank(date)){
                 cnd=getQueryCnd(agencyId,orgId,businessLine,organizeType,getStartDateAndEndDate(date)[0].trim(),getStartDateAndEndDate(date)[1].trim());
                 list =dao().query(BusinessOrganize.class,cnd==null?Cnd.orderBy().asc("code"):cnd.asc("code"),pager);
             }else{
                 cnd=getQueryCnd(agencyId,orgId,businessLine,organizeType,"","");
                 list =dao().query(BusinessOrganize.class,cnd==null?Cnd.orderBy().asc("code"):cnd.asc("code"),pager);
             }
             if(list.size()>0){
                 //循环获取组织上级机构的信息
                 for (BusinessOrganize b:list) {
                     BusinessOrganize bu=dao().fetchLinks(dao().fetch(BusinessOrganize.class,b.getId()),"agency");
                     buList.add(bu);
                 }
             }
             dataTables=new DataTables(param.getDraw(),dao().count(BusinessOrganize.class),dao().count(BusinessOrganize.class,cnd),buList);
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
     * 模糊查询组织数据
     * @param search
     * @return
     */
    public List<BusinessOrganize>  queryBySearch(String search,String organizeType){
        List<BusinessOrganize> list=new ArrayList<BusinessOrganize>();
        List<BusinessOrganize> orList=null;
        Cnd cnd=null;
            if(!Strings.isBlank(search)){
                SqlExpressionGroup e1=Cnd.exps("name","like","%"+search+"%").or("code","like","%"+search+"%");
                if(!Strings.isBlank(organizeType)){
                    SqlExpressionGroup e2=Cnd.exps("organizeType","=",organizeType);
                    cnd=Cnd.where(e1).and(e2);
                }else {
                    cnd=Cnd.where(e1);
                }
                orList=dao().query(BusinessOrganize.class,cnd);
                if(orList.size()>0){
                    for (BusinessOrganize b:orList
                         ) {
                        BusinessOrganize bu=dao().fetchLinks(dao().fetch(BusinessOrganize.class,b.getId()),"agency");
                        list.add(bu);
                    }
                }
            }
        return list;
    }

    /**
     * 通过id查询某一条组织信息
     * @param id
     * @return
     */
    public BusinessOrganize fetchById(String id){
        return dao().fetchLinks(dao().fetchLinks(dao().fetch(BusinessOrganize.class,id),"parent"),"agency");
    }

    /**
     * 判断组织名是否重复
     * @param id
     * @param name
     * @param type
     * @return
     */
    public boolean eableName(String id,String name,String type){
        BusinessOrganize b=dao().fetch(BusinessOrganize.class,Cnd.where("name","=",name));
        if(b!=null){
            if (type.equals("add")) {
                return false;
            } else {
                if (!b.getId().equals(id)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 新增组织
     * @param businessOrganize
     * @return
     */
    public boolean add(BusinessOrganize businessOrganize){
        log.info("businessOrganize:"+businessOrganize.toString());
        //如果code重复自动更新
        BusinessOrganize b1=dao().fetch(BusinessOrganize.class,Cnd.where("code","=",businessOrganize.getCode()));
        if(b1!=null){
            // 自动更新code
            String code=(String) getOrgCode(businessOrganize.getOrganizeType().toString(),businessOrganize.getId(),businessOrganize.getBusinessLine().toString());
            businessOrganize.setCode(code);
        }
        User user= ShiroSession.getLoginUser();
        // 获取新增的记录的人员
        if(user!=null){
            businessOrganize.setCreateBy(String.valueOf(user.getName()));
        }
        businessOrganize.setCreateTime(new Date());
        BusinessOrganize businessOrganize1=dao().insert(businessOrganize);
        if (businessOrganize1 != null) {
            return true;
        }
        return false;
    }


    /**
     * 修改组织
     * @param businessOrganize
     * @return
     */
    public boolean update(BusinessOrganize businessOrganize){
        log.info("businessOrganize:"+businessOrganize.toString());
        User user= ShiroSession.getLoginUser();
        // 获取修改的记录的人员
        if(user!=null){
            businessOrganize.setUpdateBy(String.valueOf(user.getName()));
        }
        businessOrganize.setUpdateTime(new Date());
        boolean boo= Daos.ext(dao(), FieldFilter.locked(BusinessOrganize.class, "^id|code|parentId|agencyId|businessLine|path|agencyPath|createBy|createTime")).update(businessOrganize)>0;
       return boo;
    }

    /**
     * 获取业务条线
     * @return
     */
    public  List<NutMap>  getBusinessLine(){
        List<NutMap>  list=new ArrayList<NutMap>();
        try{
            for (BusinessLine e : BusinessLine.values()) {
                NutMap n=new NutMap();
                n.setv("name",e.getDescription());
                n.setv("value",e.toString());
                list.add(n);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    /**
     * 获取组织code
     * @param organizeType
     * @param orgId
     * @param lines
     * @return
     */
    public String getOrgCode(String organizeType,String orgId,String lines){
        DecimalFormat format = new DecimalFormat("000");
        String code="";
        List<BusinessOrganize> orgList;
        try{
            log.info("organizeType:"+organizeType+",orgId:"+orgId+",lines:"+lines);

            // 如果 organizeType 为AGENCY则是部门
            if(organizeType.equals(BusinessOrganizeType.AGENCY.toString())){
                // 查询部门或者组织是否有数据
                orgList = dao().query(BusinessOrganize.class,Cnd.where("businessLine","=",lines).and("organizeType","=",organizeType).desc("createTime"));
                String codeFirst=BusinessLine.getCode(lines);
                // 如果没有数据，按照生成规则生成code
                if (orgList.size()==0){
                    // 获取code第一位字母
                    code=codeFirst+"001";
                }else{
                    // 获取第一条数据，上面查询做了排序.
                    BusinessOrganize b=orgList.get(0);
                    int codeNum=Integer.valueOf(b.getCode().substring(1,b.getCode().length()))+1;
                    code=codeFirst+format.format(codeNum);
                }
            }
            else if(organizeType.equals(BusinessOrganizeType.ORGANIZE.toString())){
                orgList= dao().query(BusinessOrganize.class,Cnd.where("parentId","=",orgId).desc("createTime"));
                BusinessOrganize businessOrganize=dao().fetch(BusinessOrganize.class,orgId);
                if(orgList.size()==0){
                    code=businessOrganize.getCode()+"001";
                }else {
                    BusinessOrganize b=orgList.get(0);
                    int codeNum=Integer.valueOf(b.getCode().substring(4,b.getCode().length()))+1;
                    code=businessOrganize.getCode()+format.format(codeNum);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return code;
    }

    /**
     * 获取查询Cnd 对象
     * @param agencyId
     * @param orgId
     * @param businessLine
     * @param organizeType
     * @param startDate
     * @param endDate
     * @return
     */
        public Cnd getQueryCnd(String agencyId,String orgId,String businessLine,String organizeType,String startDate,String endDate){
            List<SqlExpressionGroup> listS=new ArrayList<>();
            Cnd cnd=null;
            SqlExpressionGroup e1;
            SqlExpressionGroup e2;
            SqlExpressionGroup e3;
            SqlExpressionGroup e4;
            SqlExpressionGroup e5;
                if(!Strings.isBlank(agencyId)){
                    e1=Cnd.exps("agencyId", "=", agencyId);
                    listS.add(e1);
                }
                if(!Strings.isBlank(orgId)){
                    e2=Cnd.exps("id", "=", orgId);
                    listS.add(e2);
                }
                if(!Strings.isBlank(businessLine)){
                    e3=Cnd.exps("businessLine", "=", businessLine);
                    listS.add(e3);
                }
                if(!Strings.isBlank(organizeType)){
                    e4=Cnd.exps("organizeType", "=", organizeType);
                    listS.add(e4);
                }
                if(!Strings.isBlank(startDate)&&!Strings.isBlank(endDate)){
                    e5=Cnd.exps("establishDate", ">=", startDate).and("establishDate", "<=", endDate);
                    listS.add(e5);
                }
            if(listS.size()>0){
                // 循环生成Cnd查询条件
                for (int i=0;i<listS.size();i++){
                    SqlExpressionGroup e=listS.get(i);
                    if(i==0){
                        cnd=Cnd.where(e);
                    }else if(i>0){
                        cnd=cnd.and(e);
                    }
                }
                return cnd;
            }
            return cnd;
        }

        public  String[] getStartDateAndEndDate(String date){
            return date.split("to");
        }

    public List<BusinessOrganize> listByManageId(String managerId){

        Cnd cnd = Cnd.where("managerId", "=", managerId).and("status", "=", PublicStatus.ABLE);
        List<BusinessOrganize> list =   dao().query(BusinessOrganize.class,cnd);
        return list;

    }

    /**
     * 通过code和条线查询某一条组织信息
     * @param code
     * @return
     */
    public BusinessOrganize fetchByCodeAndLine(String code,String lines){
        return dao().fetch(BusinessOrganize.class,Cnd.where("businessLine","=",lines).and("code","=",code));
    }
}


