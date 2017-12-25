package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BillLoanService;
import com.kaisa.kams.components.service.BorrowerAccountService;
import com.kaisa.kams.components.service.HouseInfoService;
import com.kaisa.kams.components.service.LoanBorrowerService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.ProductInfoItemService;
import com.kaisa.kams.components.service.ProductInfoTmplService;
import com.kaisa.kams.components.service.SealService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.components.utils.TransformUtil;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.BillLoan;
import com.kaisa.kams.models.BillLoanRepay;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.HouseInfo;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.ProductInfoItem;
import com.kaisa.kams.models.ProductInfoTmpl;
import com.kaisa.kams.models.Seal;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
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
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuwen01 on 2016/12/12.
 */
@IocBean
@At("/seal")
public class SealController {

    @Inject
    private SealService sealService;

    @Inject
    private LoanService loanService;

    @Inject
    private LoanBorrowerService loanBorrowerService;

    @Inject
    private HouseInfoService houseInfoService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private ProductInfoItemService productInfoItemService;

    @Inject
    private BorrowerAccountService borrowerAccountService;



    /**
     * 跳转到用印管理页面
     *
     * @return
     */
    @At
    @Ok("beetl:/seal/list.html")
    @RequiresPermissions("seal:view")
    public Context list() {
        Context ctx = Lang.context();
        ctx.set("sealStatusList", SealStatus.values());
        return ctx;
    }


    @At("/seal_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("seal:view")
    public Object list(@Param("..")DataTableParam param) {

        String businessNo = "";
        String borrower = "";
        String loanDate = "";
        String used = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            businessNo = keys.get("bussinessNo");
            borrower = keys.get("borrower");
            loanDate = keys.get("s_loanDate");
            used = keys.get("s_used");

        }
        //默认查询未用印
        if(StringUtils.isEmpty(used)){
            used = "UNUSED";
        }
        if(StringUtils.isNotEmpty(used) && (used).equals("ALL")){
            used = "";
        }
        return sealService.query(param.getStart(),param.getLength(), param.getDraw(),businessNo,borrower,loanDate,used);
    }

    /**
     * 修改用印
     */
    @At
    @POST
    @Ok("json")
    @RequiresPermissions("seal:update")
    public Object update(@Param("..") Seal seal,@Param("loanId") String loanId) {

        NutMap result = new NutMap();
        if (null == seal) {
            result.put("ok", false);
            result.put("msg", "用印信息错误");
            return result;
        }
        if(StringUtils.isNotEmpty(seal.getId())){
        Seal orgSeal = sealService.fetch(seal.getId());
            if(null != orgSeal && seal.getStatus().equals(orgSeal.getStatus())){
                result.put("ok", false);
                result.put("msg", "当前状态为拟用印，无法重复修改");
                return result;
            }
        }

        seal.setUpdateBy(ShiroSession.getLoginUser().getName());
        seal.setUpdateTime(new Date());
        if(SealStatus.USED.equals(seal.getStatus())){
            seal.setUseTime(new Date());
        }
        boolean flag = false;
        if(StringUtils.isNotEmpty(seal.getId())){
            //修改数据
            flag = sealService.update(seal);
        }else {
            //新增数据
            seal.setCreateBy(ShiroSession.getLoginUser().getName());
            seal.setCreateTime(new Date());
            seal.setLoanId(loanId);
            Seal s = sealService.add(seal);
            if(null != s){
                flag = true;
            }
        }




        if (flag) {
            result.put("ok", true);
            result.put("msg", "修改用印成功");
        } else {
            result.put("ok", false);
            result.put("msg", "修改用印失败");
        }
        return result;
    }

    /**
     * 初始化用印信息
     * @return
     */
    @At("/init_seal")
    @POST
    @Ok("json")
    @RequiresPermissions("seal:update")
    public Object initSeal(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        Map info = new HashMap<String,String>();
        Loan loan = loanService.fetchById(loanId);
        //查找主借款人信息
        List<LoanBorrower> loanBorrowerList = loanBorrowerService.queryByLoanId(loan.getId());
        List<LoanBorrower> masterLoanBorrowerList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(loanBorrowerList)){
            for (LoanBorrower lb: loanBorrowerList){
                if(lb.isMaster()==false){
                    masterLoanBorrowerList.add(lb);
                    info.put("loanBorrowerList",masterLoanBorrowerList);
                }else {
                    info.put("borrserName",lb.getName());
                    info.put("borrserIDType",lb.getCertifType());
                    info.put("borrserNameID",lb.getCertifNumber());
                }
            }
        }

        info.put("amount",loan.getAmount());
        info.put("loanTerm", TransformUtil.getTermType(loan.getTermType(),loan.getTerm()));
        info.put("calculateMethodAboutDay",loan.getCalculateMethodAboutDay());
        info.put("loanInterest",getInterestMode(loan.getLoanLimitType(),loan.getTermType(),loan.getInterestRate(),loan.getInterestAmount()));

        //房產
        Map data = new HashMap<>();
        List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(loan.getId());
        List<ProductInfoItem> productInfoItems =  productInfoItemService.queryByLoanId(loan.getId());
        if(CollectionUtils.isNotEmpty(productInfoItems) && CollectionUtils.isEmpty(houseInfoList)){
            for (ProductInfoItem pit:productInfoItems){
                if(("house").equals(pit.getKeyName())){
                    String houseVal = pit.getDataValue();
                    String starVal = houseVal.substring(1, houseVal.length());
                    JSONArray jsonArray = JSONArray.fromObject(houseVal);


                    List<Map<String,String>> mapListJson = (List)jsonArray;
                    for (int i = 0; i < mapListJson.size(); i++) {

                        JSONArray jsonArray2 = JSONArray.fromObject(mapListJson.get(i));
                        List houseList = jsonArray2;
                        for (int j = 0; j < houseList.size(); j++){
                            Map<String,String> obj = (Map<String, String>) houseList.get(j);
                            if(("house_code").equals(obj.get("keyName"))){
                                data.put("house_code",obj.get("dataValue"));
                            }else if(("address").equals(obj.get("keyName"))){
                                data.put("address",obj.get("dataValue"));
                            }
                        }
                        if((null!= data && data.size()>0)){
                            HouseInfo h= new HouseInfo();
                            h.setCode(data.get("house_code").toString());
                            h.setAddress(data.get("address").toString());
                            houseInfoList.add(h);
                        }
                    }


                }else {
                    if(("name").equals(pit.getKeyName())) {
                        info.put("buyerName", pit.getDataValue());
                    }if(("re_amount").equals(pit.getKeyName())) {
                        info.put("re_amount", pit.getDataValue());
                    }
                }
            }
        }


        info.put("houseInfoList",houseInfoList);
        //放款主體
        LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
        info.put("loanSubject",loanSubject.getName());
        ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());
        if(ProductTempType.isBill(tmpl.getProductTempType())){
            Map obj = billLoanService.queryBillLoanInfo(loanId);
            List<BillLoanRepay> billLoanRepayList = (List<BillLoanRepay>) obj.get("billLoanRepayList");
            BillLoan billLoan = (BillLoan) obj.get("billLoan");
            Borrower lb = (Borrower) obj.get("loanBorrower");
            info.put("discounter",lb.getName());
            info.put("businessNumber",lb.getCertifNumber());
            info.put("billTotalAmount",billLoan.getTotalAmount());
            info.put("billList",billLoanRepayList);
        }

        //收款账户
        info.put("bakAccountList",borrowerAccountService.queryFormatAccountsByLoanId(loan.getId()));
        //宽限期
        info.put("grace",loan.getGrace());
        info.put("productType",tmpl);
        result.put("info",info);
        return result;
    }

    public String getInterestMode(LoanLimitType loanLimitType, LoanTermType loanTermType, BigDecimal interestRate,BigDecimal interestAmount){

        if(LoanLimitType.FIX_AMOUNT.equals(loanLimitType)){
            String result;
            if(null != interestAmount){
                result = TextFormatUtils.formatBigDecimal(interestAmount);

            if(LoanTermType.DAYS.equals(loanTermType)){
                return result+"元/天(金额计息)";
            }if(LoanTermType.MOTHS.equals(loanTermType)){
                return result+"元/月(金额计息)";
            }if(LoanTermType.YEAS.equals(loanTermType)){
                return result+"元/年(金额计息)";
            }if(LoanTermType.FIXED_DATE.equals(loanTermType)){
                return result+"元/天(金额计息)";
            }
                if(LoanTermType.SEASONS.equals(loanTermType)){
                    return result+"元/季(金额计息)";
                }
            }

        }else if(LoanLimitType.FIX_RATE.equals(loanLimitType)){
            if(null != interestRate) {
                if (LoanTermType.DAYS.equals(loanTermType)) {
                    return interestRate.stripTrailingZeros() + "%/天(比例计息)";
                }
                if (LoanTermType.MOTHS.equals(loanTermType)) {
                    return interestRate.stripTrailingZeros() + "%/月(比例计息)";
                }
                if (LoanTermType.YEAS.equals(loanTermType)) {
                    return interestRate.stripTrailingZeros() + "%/年(比例计息)";
                }
                if (LoanTermType.FIXED_DATE.equals(loanTermType)) {
                    return interestRate.stripTrailingZeros() + "%/天(比例计息)";
                }if (LoanTermType.SEASONS.equals(loanTermType)) {
                    return interestRate.stripTrailingZeros() + "%/季(比例计息)";
                }
            }
        }
        return null;
    }

    @At("/document_download")
    @Ok("void")
    public void documentDownload(@Param("loanId")String loanId , HttpServletResponse response  ) {
        try{
            sealService.documentDownload(loanId,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
