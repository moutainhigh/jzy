package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BorrowerAccountService;
import com.kaisa.kams.components.service.BorrowerService;
import com.kaisa.kams.components.service.LoanBorrowerService;
import com.kaisa.kams.models.Intermediary;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 居间人人信息维护
 * Created by lw on 2017/6/12.
 */
@IocBean
@At("/intermediary")
public class IntermediaryController {

    private static final Log log = Logs.get();

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private LoanBorrowerService loanBorrowerService;

    @Inject
    private BorrowerAccountService borrowerAccountService;


    /**
     * 跳转到居间人管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/intermediary/list.html")
    @RequiresPermissions("intermediary:view")
    public Context list() {
        Context ctx = Lang.context();
        return ctx;
    }


    @At("/intermediary_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object list(@Param("..")DataTableParam param) {
        return borrowerService.query(param);
    }



    /**
     * 新增居间人
     * @param intermediary
     * @return
     */
    @At("/add")
    @POST
    @Ok("json")
    public Object add(@Param("..")Intermediary intermediary){
        NutMap result = new NutMap();
        if(null==intermediary){
            result.put("ok",false);
            result.put("msg","居间人信息错误");
            return result;
        }
        Intermediary intermediaryTmp = borrowerService.fetchByIdNumberNoStatus(intermediary.getIdNumber());
        if(null!=intermediaryTmp){
            result.put("ok",false);
            result.put("msg","身份证号码已存在");
            return result;
        }
        intermediary.setCreateBy(ShiroSession.getLoginUser().getName());
        intermediary.setUpdateBy(ShiroSession.getLoginUser().getName());
        intermediary.setCreateTime(new Date());
        intermediary.setUpdateTime(new Date());
        Intermediary resultIntermediary = borrowerService.add(intermediary);
        if(null==resultIntermediary){
            result.put("ok",false);
            result.put("msg","保存失败");
            return result;
        }
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",resultIntermediary);
        return result;
    }

    /**
     * 修改
     * @param intermediary
     * @return
     */
    @At("/update")
    @POST
    @Ok("json")
    public Object update(@Param("..")Intermediary intermediary){
        NutMap result = new NutMap();
        if(null==intermediary){
            result.put("ok",false);
            result.put("msg","居间人信息错误");
            return result;
        }
        intermediary.setUpdateBy(ShiroSession.getLoginUser().getName());
        intermediary.setUpdateTime(new Date());
        Intermediary exitIntermediary = borrowerService.fetchByIdNumberAndIdNoStatus(intermediary.getIdNumber(),intermediary.getId());
        if(null!=exitIntermediary){
            result.put("ok",false);
            result.put("msg","身份证号码不能重复");
            return result;
        }
//        Intermediary exitIntermediaryName = borrowerService.fetchByNameAndId(intermediary.getName(),intermediary.getId());
//        if(null!=exitIntermediaryName){
//            result.put("ok",false);
//            result.put("msg","居间人姓名不能重复");
//            return result;
//        }
        boolean flag = borrowerService.updateIntermediary(intermediary);
        if(flag){
            Intermediary resultIntermediary = borrowerService.fetchIntermediaryById(intermediary.getId());
            result.put("ok",true);
            result.put("msg","修改成功");
            result.put("data",resultIntermediary);
            return result;
        }
        result.put("ok",false);
        result.put("msg","修改失败");
        return result;
    }

    /**
     * 查看证件号码是否存在
     * @param IdNumber
     * @return
     */
    @At("/exist_idNumber")
    @GET
    @Ok("json")
    public Object existIdNumber(@Param("idNumber")String IdNumber){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(IdNumber)){
            result.put("ok",false);
            result.put("msg","身份证号码不能为空");
            result.put("data",false);
            return result;
        }

        Intermediary intermediary = borrowerService.fetchByIdNumber(IdNumber);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary==null);
        return result;
    }

    /**
     * 查看证件号码是否存在
     * @param IdNumber
     * @return
     */
    @At("/exist_idNumber_edit")
    @GET
    @Ok("json")
    public Object existIdNumberForEdit(@Param("idNumber")String IdNumber,
                                       @Param("id")String id){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(IdNumber)){
            result.put("ok",false);
            result.put("msg","身份证号码不能为空");
            result.put("data",false);
            return result;
        }

        Intermediary intermediary = borrowerService.fetchByIdNumberAndId(IdNumber,id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary==null);
        return result;
    }

    /**
     * 查看姓名是否存在
     * @param name
     * @return
     */
    @At("/exist_name")
    @GET
    @Ok("json")
    public Object existName(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","居间人姓名不能为空");
            result.put("data",false);
            return result;
        }

        Intermediary intermediary = borrowerService.fetchByname(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary==null);
        return result;
    }

    /**
     * 查看姓名是否存在
     * @param name
     * @return
     */
    @At("/exist_name_edit")
    @GET
    @Ok("json")
    public Object existNameForEdit(@Param("name")String name,
                                   @Param("id")String id){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","居间人姓名不能为空");
            result.put("data",false);
            return result;
        }

        Intermediary intermediary = borrowerService.fetchByNameAndId(name,id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary==null);
        return result;
    }

    /**
     * 根据名称查询借款人
     * @param name
     * @return
     */
    @At("/query_by_name")
    @GET
    @Ok("json")
    public Object queryByName(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","居间人名称不能为空");
            return result;
        }

        List<Intermediary> intermediaryList = borrowerService.queryIntermediaryByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediaryList);
        return result;
    }

    /**
     * 根据id查询居间人
     * @param id
     * @return
     */
    @At("/fetch_by_id")
    @GET
    @Ok("json")
    public Object fetchById(@Param("id")String id){
        NutMap result = new NutMap();
        Intermediary intermediary = borrowerService.fetchIntermediaryById(id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary);
        return result;
    }

    /**
     * 根据id查询居间人 查询生效和失效数据
     * @param id
     * @return
     */
    @At("/fetch_by_Id")
    @GET
    @Ok("json")
    public Object fetchById1(@Param("id")String id){
        NutMap result = new NutMap();
        Intermediary intermediary = borrowerService.fetchIntermediaryById1(id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",intermediary);
        return result;
    }
}