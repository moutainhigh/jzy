package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BillLoanService;
import com.kaisa.kams.components.service.EnterpriseService;
import com.kaisa.kams.enums.CompanyType;
import com.kaisa.kams.enums.CreditRating;
import com.kaisa.kams.models.Enterprise;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by liuwen01 on 2017/4/12.
 */
@IocBean
@At("/enterprise")
public class EnterpriseController {

    private static final Log log = Logs.get();

    @Inject
    private EnterpriseService enterpriseService;

    @Inject
    private BillLoanService billLoanService;

    /**
     * 跳转到用印管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/enterprise/list.html")
    @RequiresPermissions("enterprise:view")
    public Context list() {
        Context ctx = Lang.context();
        ctx.set("levelList", CreditRating.values());
        ctx.set("typeList", CompanyType.values());
        return ctx;
    }


    @At("/enterprise_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("enterprise:view")
    public Object list(@Param("..")DataTableParam param) {
        return enterpriseService.query(param);
    }

    /**
     * 编辑跳转方法
     * @param id
     * @return
     */
    @At("/edit_init")
    @Ok("json")
    @POST
    @RequiresPermissions("enterprise:update")
    public Object editInit(@Param("id") String id) {
        NutMap result = new NutMap();
        try {
            Enterprise enterprise = enterpriseService.fetchById(id);
            enterprise = setValues(enterprise);
            result.put("enterprise", enterprise);
            result.put("ok", true);
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "获取数据异常！");
        }
        return result;
    }

    /**
     * 修改企业
     */
    @At("/update_enterprise")
    @POST
    @Ok("json")
    @RequiresPermissions("enterprise:update")
    public Object update(@Param("..") Enterprise enterprise) {

        NutMap result = new NutMap();
        if (null == enterprise) {
            result.put("ok", false);
            result.put("msg", "企业信息错误");
            return result;
        }

        enterprise.setUpdateBy(ShiroSession.getLoginUser().getName());
        enterprise.setUpdateTime(new Date());
        Enterprise exitenterprise = enterpriseService.fetchByNameAndIdForExit(enterprise.getName(),enterprise.getId());
        if(null != exitenterprise){
            result.put("ok", false);
            result.put("msg", "企业名称不能重复");
            return result;
        }
        //修改数据
        boolean flag = enterpriseService.update(enterprise);


        if (flag) {
            result.put("ok", true);
            result.put("msg", "修改企业信息成功");
        } else {
            result.put("ok", false);
            result.put("msg", "修改企业信息失败");
        }
        return result;
    }

    /**
     * 添加企业
     */
    @At("/add_enterprise")
    @POST
    @Ok("json")
    @RequiresPermissions("enterprise:create")
    public Object add(@Param("..") Enterprise enterprise) {

        NutMap result = new NutMap();
        enterprise.setUpdateBy(ShiroSession.getLoginUser().getName());
        enterprise.setUpdateTime(new Date());
        enterprise.setCreateBy(ShiroSession.getLoginUser().getName());
        enterprise.setCreateTime(new Date());
        Enterprise exitEnterprise = enterpriseService.fetchByNameForExit(enterprise.getName());
        if(null != exitEnterprise){
            result.put("ok", false);
            result.put("msg", "企业名称不能重复");
            return result;
        }
        //新增数据
        enterprise = enterpriseService.add(enterprise);


        if (null != enterprise) {
            result.put("ok", true);
            result.put("msg", "添加企业信息成功");
        } else {
            result.put("ok", false);
            result.put("msg", "添加企业信息失败");
        }
        return result;
    }

    /**
     * 根据名称查询企业
     * @param name
     * @return
     */
    @At("/query_enterprise_by_name")
    @POST
    @Ok("json")
    public Object queryByName(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","付款人名称不能为空");
            return result;
        }

        List<Enterprise> enterpriseList = enterpriseService.fetchByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",enterpriseList);
        return result;
    }

    /**
     * 根据名称类型查询企业
     * @param name
     * @return
     */
    @At("/query_enterprise_by_name_type")
    @POST
    @Ok("json")
    public Object queryByNameAndType(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","授信公司不能为空");
            return result;
        }

        List<Enterprise> enterpriseList = enterpriseService.fetchByNameAndType(name,CompanyType.CREDITCOMPANY);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",enterpriseList);
        return result;
    }

    /**
     * 处理数据显示
     * @param enterprise
     * @return
     */
    public Enterprise setValues(Enterprise enterprise){
        List<Enterprise> enterpriseList;
        StringBuffer companyStr = new StringBuffer();
        BigDecimal libraryAmount = BigDecimal.ZERO;
        if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
            enterpriseList = enterpriseService.fetchByCompanyId(enterprise.getId());
            if(CollectionUtils.isNotEmpty(enterpriseList)){
                for (Enterprise e : enterpriseList){
                    companyStr.append(e.getName()+"，");
                    libraryAmount = libraryAmount.add(billLoanService.queryRepayAmount(e.getId()));
                }
                enterprise.setLibraryAmount(libraryAmount.add(billLoanService.queryRepayAmount(enterprise.getId())));
                if(null == enterprise.getCreditQuota()){
                    enterprise.setRemainderAmount(BigDecimal.ZERO);
                }else {
                    enterprise.setRemainderAmount(enterprise.getCreditQuota().subtract(enterprise.getLibraryAmount()));
                }
                enterprise.setCompanies(companyStr.substring(0,companyStr.length()-1));
            }else {
                if(null == enterprise.getCreditQuota()){
                    enterprise.setRemainderAmount(BigDecimal.ZERO);
                }else {
                    enterprise.setRemainderAmount(enterprise.getCreditQuota().subtract(billLoanService.queryRepayAmount(enterprise.getId())));
                }
                enterprise.setLibraryAmount(billLoanService.queryRepayAmount(enterprise.getId()));
            }
        }else {
            enterprise.setLibraryAmount(billLoanService.queryRepayAmount(enterprise.getId()));
        }
        return enterprise;
    }
}
