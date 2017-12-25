package com.kaisa.kams.components.controller.mobile;

import com.kaisa.kams.components.controller.base.BaseUploadController;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.MenuService;
import com.kaisa.kams.components.service.ProductInfoTmplService;
import com.kaisa.kams.components.service.ProductMediaAttachService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductInfoTmpl;
import com.kaisa.kams.models.ProductMediaAttach;
import com.kaisa.kams.models.User;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.ViewModel;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import java.util.List;
import java.util.Map;

/**
 * 移动端
 * Created by weid on 2016/12/24.
 */
@IocBean
@At("/m")
public class MobileController extends BaseUploadController {

    @Inject
    private UserService userService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private LoanService loanService;

    @Inject
    private ProductService productService;

    @Inject
    private MenuService menuService;

    @Inject
    private ProductMediaAttachService productMediaAttachService;

    @At("/to_login")
    @Ok("beetl:/h5/login.html")
    @Filters
    public Context toLogin(){
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 用户登录
     * @return
     */
    @At
    @Ok("redirect:${obj==true?'/m/index':'/m/to_login'}")
    @Filters
    public boolean login(@Param("::user.")User user){
        if(null==user){
            return false;
        }

        User currentUser = userService.fetch(user.getLogin(),user.getPassword());
        if (null==currentUser){
            return false;
        }

        if (!userService.loginByShiro(currentUser)) {
            return false;
        }

        //查找到用户菜单树
        List<Map> menus = menuService.drawMenu(currentUser);
        ShiroSession.setMenu(menus);
        return true;
    }

    /**
     * 跳转到\h5页面
     * @return
     */
    @At
    @Ok("beetl:/h5/index.html")
    public Context index() {
        Context ctx = Lang.context();
        return ctx;
    }

    /**
     * 跳转到\h5详情页
     * @return
     */
    @At("/detail")
    @Ok("re:beetl:/h5/detail.html")
    public String detail(@Param("id")String id,@Param("from")String from,ViewModel model) {
        //获取Loan
        Loan loan =  loanService.fetchById(id);
        if(null==loan){
            return null;
        }
        //获取到当前的产品
        Product product = productService.fetchEnableProductById(loan.getProductId());
        ProductInfoTmpl productInfoTmpl =  productInfoTmplService.fetchById(product.getInfoTmpId());
        model.setv("productInfoTmpl",productInfoTmpl);
        model.setv("from",from);
        ProductTempType loanTempType = productInfoTmpl.getProductTempType();
        if (ProductTempType.PIAOJU == loanTempType|| ProductTempType.YINPIAO == loanTempType) {
            return "beetl:/h5/piaojuDetail.html";
        }
        return null;
    }


    /**
     * 跳转到\h5业务审批列表
     * @return
     */
    @At("/business_approval_list")
    @Ok("beetl:/h5/business/index.html")
    public Context businessApprovalList() {
        return Lang.context();
    }

    /**
     * 跳转到\h5高管审批列表
     * @return
     */
    @At("/senior_approval_list")
    @Ok("beetl:/h5/senior/index.html")
    public void seniorApprovalList() {
    }

    /**
     * 跳转到\h5风控审批列表
     * @return
     */
    @At("/risk_approval_list")
    @Ok("beetl:/h5/risk/index.html")
    public Context riskApprovalList() {
        return Lang.context();
    }

    /**
     * 跳转到\h5风控审批列表
     * @return
     */
    @At("/finance_approval_list")
    @Ok("beetl:/h5/finance/index.html")
    public Context financeApprovalList() {
        return Lang.context();
    }

    /**
     * h5找回密码
     * @return
     */
    @At("/to_find_pw")
    @Ok("beetl:/h5/toFindPw.html")
    @Filters
    public Context toFindPw() {
        return Lang.context();
    }


    /**
     * 跳转到\h5s申请查询列表
     * @return
     */
    @At("/apply_query_list")
    @Ok("beetl:/h5/apply/list.html")
    public Context applyQueryList() {
        return Lang.context();
    }

    /**
     * 跳转到\h5查看详情页
     * @return
     */
    @At("/apply/detail")
    @Ok("re:beetl:/h5/apply/detail.html")
    public String toApplyDetail(@Param("id")String id,ViewModel model) {
        //获取Loan
        Loan loan =  loanService.fetchById(id);
        if(null==loan){
            return null;
        }
        //获取到当前的产品
        Product product = productService.fetchEnableProductById(loan.getProductId());
        ProductInfoTmpl productInfoTmpl =  productInfoTmplService.fetchById(product.getInfoTmpId());
        ProductTempType loanTempType = productInfoTmpl.getProductTempType();
        model.setv("productInfoTmpl",productInfoTmpl);
        if (ProductTempType.PIAOJU == loanTempType|| ProductTempType.YINPIAO == loanTempType) {
            return "beetl:/h5/apply/piaojuDetail.html";
        }
        return null;
    }


    /**
     * h5上传页面
     * @return
     */
    @At("/upload")
    @Ok("beetl:/h5/upload.html")
    public void toUpLoad(@Param("id")String id, ViewModel model) {
        if (StringUtils.isEmpty(id)) {
            return;
        }
        ProductMediaAttach attach = productMediaAttachService.fetchLinkById(id);
        if (null == attach) {
            return;
        }
        Loan loan = loanService.fetchById(attach.getLoanId());
        model.setv("loanCode",loan.getCode());
        model.setv("loanId",loan.getId());
        model.setv("attach",attach);
    }

    @At
    @GET
    @Ok("json")
    public Object fetchAliOssToken(@Param("dir") String dir) {
        return super.fetchAliOssToken(dir);
    }

    @At("/view_attach")
    @Ok("beetl:/h5/viewAttach.html")
    public void viewAttach(@Param("id")String id, ViewModel model) {
        if (StringUtils.isEmpty(id)) {
            return;
        }
        ProductMediaAttach attach = productMediaAttachService.fetchLinkById(id);
        if (null == attach) {
            return;
        }
        model.setv("attach",attach);
    }

}
