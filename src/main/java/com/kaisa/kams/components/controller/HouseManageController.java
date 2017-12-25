package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.HouseManageService;
import com.kaisa.kams.components.utils.ApiPropsUtils;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.enums.MortgageType;
import com.kaisa.kams.enums.PropertyRightStatus;
import com.kaisa.kams.enums.StorageStatus;
import com.kaisa.kams.models.HouseManage;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liuwen01 on 2017/8/14.
 */
@IocBean
@At("/house_manage")
public class HouseManageController {

    @Inject
    private HouseManageService houseManageService;

    /**
     * 跳转到用印管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/assetManage/houseList.html")
    @RequiresPermissions("houseManage:view")
    public Context list() {
        Context ctx = Lang.context();
        ctx.set("propertyRightStatusList", PropertyRightStatus.values());
        ctx.set("storageStatusList", StorageStatus.values());

        return ctx;
    }


    @At("/house_manage_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("houseManage:view")
    public Object list(@Param("..")DataTableParam param) {

        return houseManageService.query(param);
    }

    /**
     * 修改房产
     */
    @At
    @POST
    @Ok("json")
    @RequiresPermissions("houseManage:update")
    public Object update(@Param("..") HouseManage houseManage) {

        NutMap result = new NutMap();
        if (null == houseManage) {
            result.put("ok", false);
            result.put("msg", "房产信息错误");
            return result;
        }

        boolean flag = false;
        houseManage.setUpdateTime(new Date());
        houseManage.setUpdateBy(ShiroSession.getLoginUser().getName());
        if(StringUtils.isNotEmpty(houseManage.getId()) && null !=houseManage.getGuaranteeResponsibility()){
            //修改数据
            houseManage.setPropertyRightStatus(PropertyRightStatus.SECURED);
            flag = houseManageService.update(houseManage);
            if (flag) {
                //sendMessage(houseManage.getBorrower(),houseManage.getLoanPrincipal(),houseManage.getCode(),"");
                result.put("ok", true);
                result.put("msg", "抵押成功");
            } else {
                result.put("ok", false);
                result.put("msg", "抵押失败");
            }
        }
        if(StringUtils.isNotEmpty(houseManage.getId()) && null != houseManage.getNoMortgageDate()){
            //修改数据
            houseManage.setPropertyRightStatus(PropertyRightStatus.SOLVED);
            flag = houseManageService.update(houseManage);
            if (flag) {
                //sendMessage(houseManage.getBorrower(),houseManage.getLoanPrincipal(),houseManage.getCode(),"");
                result.put("ok", true);
                result.put("msg", "解押成功");
            } else {
                result.put("ok", false);
                result.put("msg", "解押失败");
            }
        }
        if(StringUtils.isNotEmpty(houseManage.getId()) && null != houseManage.getInDate()){
            //修改数据
            houseManage.setStorageStatus(StorageStatus.IN);
            flag = houseManageService.update(houseManage);
            if (flag) {
                result.put("ok", true);
                result.put("msg", "入库成功");
            } else {
                result.put("ok", false);
                result.put("msg", "入库失败");
            }
        }
        if(StringUtils.isNotEmpty(houseManage.getId()) && null != houseManage.getOutDate()){
            //修改数据
            houseManage.setStorageStatus(StorageStatus.OUT);
            flag = houseManageService.update(houseManage);
            if (flag) {
                result.put("ok", true);
                result.put("msg", "出库成功");
            } else {
                result.put("ok", false);
                result.put("msg", "出库失败");
            }
        }

        return result;
    }


    /**
     * 初始化房产信息
     * @return
     */
    @At("/init_houseManage")
    @POST
    @Ok("json")
    @RequiresPermissions("houseManage:update")
    public Object initHouseManage(@Param("id") String id){
        NutMap result = new NutMap();
        HouseManage houseManage = houseManageService.fetchById(id);
        result.put("houseManage",houseManage);
        result.put("mortgageTypeList",MortgageType.values());
        return result;
    }

    /**
     * 查看房产信息
     * @return
     */
    @At("/fetch_houseManage")
    @POST
    @Ok("json")
    @RequiresPermissions("houseManage:update")
    public Object fetchHouseManage(@Param("id") String id){
        NutMap result = new NutMap();
        HouseManage houseManage = houseManageService.fetchById(id);
        result.put("houseManage",houseManage);
        return result;
    }

    @At("/houseManage_export")
    @Ok("void")
    @RequiresPermissions("houseManage:view")
    public void listExport(@Param("code")String code,
                           @Param("borrower")String borrower,
                           @Param("loanDate")String loanDate,
                           @Param("mortgageDate")String mortgageDate,
                           @Param("noMortgageDate")String noMortgageDate,
                           @Param("storageStatus")String storageStatus,
                           @Param("propertyRightStatus")String propertyRightStatus,
                           @Param("channelType")String channelType,
                           @Param("saleName")String saleName,
                           @Param("channelId")String channelId,
                           HttpServletResponse resp )throws Exception {

        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        param.setSearchKeys(map);
        param.getSearchKeys().put("code", code);
        param.getSearchKeys().put("borrower",borrower);
        param.getSearchKeys().put("loanDate",loanDate);
        param.getSearchKeys().put("mortgageDate",mortgageDate);
        param.getSearchKeys().put("noMortgageDate",noMortgageDate);
        param.getSearchKeys().put("storageStatus",storageStatus);
        param.getSearchKeys().put("propertyRightStatus",propertyRightStatus);
        param.getSearchKeys().put("channelType",channelType);
        param.getSearchKeys().put("saleName",saleName);
        param.getSearchKeys().put("channelId",channelId);

        List<HouseManage> cdList= houseManageService.query(param).getData();
        for(HouseManage cd :cdList){
            if(null != cd.getMortgageType()){
                cd.setMortgageTypeStr(getMortgageType(cd.getMortgageType().toString()));
            }
            if(null != cd.getPropertyRightStatus()) {
                cd.setPropertyRightStatusStr(getPropertyRightStatus(cd.getPropertyRightStatus().toString()));
            }
            if(null != cd.getStorageStatus()) {
                cd.setStorageStatusStr(getStorageStatus(cd.getStorageStatus().toString()));
            }
        }
        ExcelExportUtil.export(resp,cdList,"房产管理");
    }

    public String getPropertyRightStatus(String propertyRightStatus){
        if(StringUtils.isNotEmpty(propertyRightStatus)){
            for(PropertyRightStatus l : PropertyRightStatus.values()){
                if(propertyRightStatus.equals(l.toString())){
                    return l.getDescription();
                }
            }
        }
        return  null;
    }

    public String getMortgageType(String mortgageType){
        if(StringUtils.isNotEmpty(mortgageType)){
            for(MortgageType l : MortgageType.values()){
                if(mortgageType.equals(l.toString())){
                    return l.getDescription();
                }
            }
        }
        return  null;
    }

    public String getStorageStatus(String storageStatus){
        if(StringUtils.isNotEmpty(storageStatus)){
            for(StorageStatus l : StorageStatus.values()){
                if(storageStatus.equals(l.toString())){
                    return l.getDescription();
                }
            }
        }
        return  null;
    }

    private void sendMessage(String borrower,BigDecimal loanPrincipal,String code,String mobie){
        Map msgMap = new HashMap<String,String>();
        msgMap.put("name",borrower);
        msgMap.put("amount",loanPrincipal);
        msgMap.put("code",code);
        String resultStr = ApiRequestUtil.sendSmsByAPI(mobie.trim(),msgMap, ApiPropsUtils.getValueByKey("initial_password_tempId"));
    }
}
