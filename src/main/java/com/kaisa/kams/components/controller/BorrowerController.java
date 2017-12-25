package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BorrowerAccountService;
import com.kaisa.kams.components.service.BorrowerService;
import com.kaisa.kams.components.service.LoanBorrowerService;
import com.kaisa.kams.enums.DiscountType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;
import java.util.List;

/**
 * 借款人信息维护
 * Created by weid on 2016/12/13.
 */
@IocBean
@At("/borrower")
public class BorrowerController {

    private static final Log log = Logs.get();

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private LoanBorrowerService loanBorrowerService;

    @Inject
    private BorrowerAccountService borrowerAccountService;


    @At
    @Ok("beetl:/borrower/list.html")
    public Context list(){
        Context ctx = Lang.context();
        return ctx;
    }
    /**
     * 跳转到贴现企业管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/discountCompany/list.html")
    @RequiresPermissions("discountCompany:view")
    public Context discountCompanyList() {
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/discountCompany_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    public Object list(@Param("..")DataTableParam param) {
        return borrowerService.queryBorrower(param);
    }

    @At
    @Ok("beetl:/discountCompany/add.html")
    public Context add(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/discountCompany/edit.html")
    public Context edit(){
        Context ctx = Lang.context();
        return ctx;
    }
    @At
    @Ok("beetl:/discountCompany/view.html")
    public Context view(){
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 根据证件号码查询借款人
     * @param certifNumber
     * @return
     */
    @At("/fetch_by_certifNumber")
    @GET
    @Ok("json")
    public Object fetchByCertifNumber(@Param("certifNumber")String certifNumber,@Param("certifType")String certifType){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(certifNumber)){
            result.put("ok",false);
            result.put("msg","证件号码不能为空");
            return result;
        }
        if(!StringUtils.isEmpty(certifType)){
            Borrower borrower = borrowerService.fetchByCertifNumberAndcertifType(certifNumber,certifType);
            result.put("ok",true);
            result.put("msg","查询成功");
            result.put("data",borrower);
            return result;
        }
        Borrower borrower = borrowerService.fetchByCertifNumber(certifNumber);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrower);
        return result;
    }

    /**
     * 根据id查询借款人
     * @param id
     * @return
     */
    @At("/fetch_by_id")
    @GET
    @Ok("json")
    public Object fetchById(@Param("id")String id){
        NutMap result = new NutMap();
        Borrower borrower = borrowerService.fetchById(id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrower);
        return result;
    }

    /**
     * 根据id查询借款人
     * @param id
     * @return
     */
    @At("/fetch_by_Id")
    @GET
    @Ok("json")
    public Object fetchById1(@Param("id")String id){
        NutMap result = new NutMap();
        Borrower borrower = borrowerService.fetchById1(id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrower);
        return result;
    }


    /**
     * 新增借款人
     * @param borrower
     * @return
     */
    @At("/add")
    @POST
    @Ok("json")
    public Object add(@Param("..")Borrower borrower){
        NutMap result = new NutMap();
        if(null==borrower){
            result.put("ok",false);
            result.put("msg","借款人信息错误");
            return result;
        }
        Borrower borrowerTmp = borrowerService.fetchByCertifNumber(borrower.getCertifNumber());
        if(null!=borrowerTmp){
            result.put("ok",false);
            result.put("msg","证件号码已存在");
            return result;
        }

        borrower.setStatus(PublicStatus.ABLE);
        borrower.setCreateBy(ShiroSession.getLoginUser().getName());
        borrower.setUpdateBy(ShiroSession.getLoginUser().getName());
        borrower.setCreateTime(new Date());
        borrower.setUpdateTime(new Date());
        Borrower resultBorrower = borrowerService.add(borrower);
        if(null==resultBorrower){
            result.put("ok",false);
            result.put("msg","保存失败");
            return result;
        }
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",resultBorrower);
        return result;
    }

    /**
     * 新增贴现人/贴现企业
     * @param borrower
     * @return
     */
    @At("/add_discount")
    @POST
    @Ok("json")
    public Object addDiscount(@Param("..")Borrower borrower){
        NutMap result = new NutMap();
        if(null==borrower){
            result.put("ok",false);
            result.put("msg","借款人信息错误");
            return result;
        }
        Borrower borrowerTmp = borrowerService.fetchByCertifNumberNotatus(borrower.getCertifNumber());
        if(null!=borrowerTmp){
            result.put("ok",false);
            result.put("msg","证件号码已存在");
            return result;
        }
        borrower.setCreateBy(ShiroSession.getLoginUser().getName());
        borrower.setCreateTime(new Date());
        Borrower resultBorrower = borrowerService.add(borrower);
        if(null==resultBorrower){
            result.put("ok",false);
            result.put("msg","保存失败");
            return result;
        }
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",resultBorrower);
        return result;
    }


    /**
     * 修改
     * @param borrower
     * @return
     */
    @At("/update")
    @POST
    @Ok("json")
    public Object update(@Param("..")Borrower borrower){
        NutMap result = new NutMap();
        if(null==borrower){
            result.put("ok",false);
            result.put("msg","借款人信息错误");
            return result;
        }
        borrower.setUpdateBy(ShiroSession.getLoginUser().getName());
        borrower.setUpdateTime(new Date());
        Borrower borrowerTmp = borrowerService.fetchByCertifyNumberAndId(borrower.getCertifNumber(),borrower.getId());
        if(null!=borrowerTmp){
            result.put("ok",false);
            result.put("msg","证件号码已存在");
            return result;
        }
        boolean flag = borrowerService.update(borrower);
//        loanBorrowerService.update(borrower);
        if(flag){
            Borrower resultBorrower = borrowerService.fetchById(borrower.getId());
            result.put("ok",true);
            result.put("msg","修改成功");
            result.put("data",resultBorrower);
            return result;
        }
        result.put("ok",false);
        result.put("msg","修改失败");
        return result;
    }
    /**
     * 修改贴现人/贴现企业
     * @param borrower
     * @return
     */
    @At("/update_discount")
    @POST
    @Ok("json")
    public Object updateDiscount(@Param("..")Borrower borrower){
        NutMap result = new NutMap();
        if(null==borrower){
            result.put("ok",false);
            result.put("msg","借款人信息错误");
            return result;
        }
        borrower.setUpdateBy(ShiroSession.getLoginUser().getName());
        borrower.setUpdateTime(new Date());
        Borrower borrowerTmp = borrowerService.fetchByCertifyNumberAndId(borrower.getCertifNumber(),borrower.getId());
        if(null!=borrowerTmp){
            result.put("ok",false);
            result.put("msg","证件号码已存在");
            return result;
        }
        boolean flag = borrowerService.updateDiscount(borrower);
//        loanBorrowerService.update(borrower);
        if(flag){
            Borrower resultBorrower = borrowerService.fetchById1(borrower.getId());
            result.put("ok",true);
            result.put("msg","修改成功");
            result.put("data",resultBorrower);
            return result;
        }
        result.put("ok",false);
        result.put("msg","修改失败");
        return result;
    }

    /**
     * 查看证件号码是否存在
     * @param certifNumber
     * @return
     */
    @At("/exist")
    @GET
    @Ok("json")
    public Object exist(@Param("certifNumber")String certifNumber){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(certifNumber)){
            result.put("ok",false);
            result.put("msg","证件号码不能为空");
            result.put("data",false);
            return result;
        }

        Borrower borrower = borrowerService.fetchByCertifNumber(certifNumber);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrower==null);
        return result;
    }

    /**
     * 查看证件号码是否存在
     * @param certifNumber
     * @return
     */
    @At("/existForEdit")
    @GET
    @Ok("json")
    public Object existForEdit(@Param("certifNumber")String certifNumber,
                               @Param("id")String id){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(certifNumber)){
            result.put("ok",false);
            result.put("msg","证件号码不能为空");
            result.put("data",false);
            return result;
        }

        Borrower borrower = borrowerService.fetchByCertifNumberAndId(certifNumber,id);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrower==null);
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
            result.put("msg","借款人名称不能为空");
            return result;
        }

        List<Borrower> borrowerList = borrowerService.queryByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrowerList);
        return result;
    }

    /**
     * 根据名称和类型查询借款人
     * @param name
     * @return
     */
    @At("/query_by_name_type")
    @GET
    @Ok("json")
    public Object queryByNameAndType(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","借款人名称不能为空");
            return result;
        }

        List<Borrower> borrowerList = borrowerService.queryByNameAndType(name, DiscountType.DISCOUNT_COMPANY);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrowerList);
        return result;
    }


    /**
     * 根据名称查询借款人
     * @param certifNumber
     * @return
     */
    @At("/query_list_by_certifNumber")
    @Ok("json")
    public Object queryListByCertifNumber(@Param("certifNumber")String certifNumber){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(certifNumber)){
            result.put("ok",false);
            result.put("msg","证件号不能为空");
            return result;
        }

        List<Borrower> borrowerList = borrowerService.queryListByCertifNumber(certifNumber);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",borrowerList);
        return result;
    }
}
