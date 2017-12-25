package com.kaisa.kams.components.controller.base;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.enums.FeeChargeType;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.ProductMediaItemType;
import com.kaisa.kams.models.*;

import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangqx on 2017/1/10.
 */
@IocBean
public class BusinessApplyBaseController {

    @Inject
    protected LoanService loanService;

    @Inject
    protected ProductService productService;

    @Inject
    protected ProductMediaAttachService productMediaAttachService;

    @Inject
    protected LoanRiskInfoService loanRiskInfoService;

    @Inject
    protected UserService userService;

    @Inject
    protected FlowService flowService;

    @Inject
    protected LoanOrderService loanOrderService;

    @Inject
    protected LoanBorrowerService loanBorrowerService;

    @Inject
    protected BorrowerAccountService borrowerAccountService;

    @Inject
    protected LoanFeeTempService loanFeeTempService;

    @Inject
    protected ProductInfoItemService productInfoItemService;

    @Inject
    protected ChannelService channelService;

    @Inject
    protected BillLoanService billLoanService;

    @Inject
    protected BankInfoService bankInfoService;

    @Inject
    protected HouseInfoService houseInfoService;

    @Inject
    protected BorrowerService borrowerService;


    /**
     * 查询需要录入的
     * @param loanId
     * @return
     */
    public Object queryRiskediaManifest(String loanId){
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        if(null==loan){
            result.put("ok",false);
            result.put("msg","当前单不存在");
            return result;
        }
        String productId = loan.getProductId();
        Product product = productService.fetchEnableProductById(productId);
        if(null==product){
            result.put("ok",false);
            result.put("msg","产品不存在");
            return result;
        }
        //查找到所有需要配置的资料
        List<ProductMediaAttach> productMediaAttachs = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.RISK);
        List<ProductMediaAttach> billMediaAttaches = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.BILL);
        //查找到填写的风控信息
        LoanRiskInfo loanRiskInfo = loanRiskInfoService.fetchByLoanId(loanId);

        Map data = new HashMap<>();
        data.put("loanRiskInfo",loanRiskInfo);
        data.put("productMediaAttachs",productMediaAttachs);
        data.put("billMediaAttaches",billMediaAttaches);
        data.put("flag",null!=loanRiskInfo);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data );
        return result;
    }

    public NutMap queryRiskediaManifestList(String loanId,String extensionId){
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        if(null==loan){
            result.put("ok",false);
            result.put("msg","当前单不存在");
            return result;
        }
        String productId = loan.getProductId();
        Product product = productService.fetchEnableProductById(productId);
        if(null==product){
            result.put("ok",false);
            result.put("msg","产品不存在");
            return result;
        }
        //查找到所有需要配置的资料
        List<ProductMediaAttach> productMediaAttachs = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.RISK);
        List<ProductMediaAttach> billMediaAttaches = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.BILL);
        //查找到填写的风控信息
        List<LoanRiskInfo> loanRiskInfos = loanRiskInfoService.listByLoanId(loanId);

        Map data = new HashMap<>();
        data.put("loanRiskInfo",loanRiskInfos);
        data.put("productMediaAttachs",productMediaAttachs);
        data.put("billMediaAttaches",billMediaAttaches);
        data.put("flag",loanRiskInfoService.fetchByExtensionId(extensionId)!=null);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data );
        return result;
    }

    /**
     * 查询待处理列表
     * @return
     */
    public DataTables queryApprovalList(int start,int length,int draw,String type){

        User user = userService.fetchLinksById(ShiroSession.getLoginUser().getId());
        List<Role> roles = user.getRoles();
        if(CollectionUtils.isEmpty(roles)){
            return new DataTables(draw,0,0,null);
        }

        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }

        //根据角色Id获取到当前需要处理的节点
        Page<WorkItem> majorPage = new Page<>(Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());
        List<WorkItem> majorWorks =  flowService.getEngine().query().getWorkItems(majorPage,queryFilter);

        //获取到所有的loanId
        List<String> loanIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
            if(StringUtils.isEmpty(workItem.getTaskName())||workItem.getTaskKey().indexOf(type)<0){
                continue;
            }
            if (StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }

        if (orderIds.size()>0) {
            loanIds = loanOrderService.getLoanIds(orderIds);
        }

        //获取到需要当前用户处理的列表
        List list = loanService.queryApprovalList(start,length,loanIds);
        int count = loanService.queryApprovalCount(loanIds);
        return new DataTables(draw,count,count,list);
    }


    /**
     * 通过ID查询基础信息
     * @return
     */
    public NutMap fetchBaseByLoanId(String id){
        NutMap result = new NutMap();
        User user = ShiroSession.getLoginUser();
        //获单信息
        Map loan =  loanService.fetchMapById(id);
        if(null==loan){
            result.put("ok",false);
            result.put("msg","业务申请单不存在");
            return result;
        }
        Loan newLoan = loanService.fetchById((String) loan.get("id"));
        if(null==newLoan){
            result.put("ok",false);
            result.put("msg","当前单不存在");
            return result;
        }
        Product product = productService.fetchEnableProductById(newLoan.getProductId());
        if(null==product){
            result.put("ok",false);
            result.put("msg","产品不存在");
            return result;
        }else {
            loan.put("productName",product.getName());
        }
        loan.put("channelName","--");
        loan.put("channelType","0");
        if (null!=loan.get("channelId")) {
            Channel channel=channelService.fetch((String) loan.get("channelId"));
            if(channel!=null){
                loan.put("channelName", channel.getName());
                loan.put("channelType",channel.getChannelType());
            }
        }
        if(ChannelUserType.CHANNEL_USER.equals(user.getType()) && StringUtils.isNotEmpty(newLoan.getChannelApplyId())){
            User channelUser = userService.fetch(newLoan.getChannelApplyId());
            if(null != channelUser){
                loan.put("applyName", channelUser.getName());
            }
        }
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",loan);
        return result;
    }


    /**
     * 通过ID查询申请单信息
     * @return
     */
    public NutMap fetchLoan(String id){
        NutMap result = new NutMap();

        //单信息
        Map loan =  loanService.fetchMapById(id);

        Loan l = loanService.fetchById(id);

        //查询借款人信息
        List<LoanBorrower> loanBorrowers = loanBorrowerService.queryByLoanId(id);

        //查询账号信息
        List<BorrowerAccount> borrowerAccounts = borrowerAccountService.queryFormatAccountsByLoanId(id);

        //查找资料信息
        List<ProductMediaAttach> productMediaAttaches = productMediaAttachService.queryDetailByLoanIdAndType(id, ProductMediaItemType.BUSINESS);

        //查询费用信息
        List<LoanFeeTemp> loanFeeTemps  = loanFeeTempService.queryByLoanIdAndLoanRepayMethod(id,l.getRepayMethod());

        //查找主借款人信息
        LoanBorrower loanBorrower = loanBorrowerService.fetchById(l.getMasterBorrowerId());

        //查询Borrower信息
        Borrower borrower=borrowerService.fetchById(loanBorrower.getBorrowerId());

        if(null!=loanFeeTemps&&loanFeeTemps.size()>0){
            for (int i=0;i<loanFeeTemps.size(); i++){
                LoanFeeTemp tmp = loanFeeTemps.get(i);
                if(null==tmp){
                    continue;
                }
                //如果是逾期罚息或者提前结清罚息都为0
                if (FeeType.OVERDUE_FEE == tmp.getFeeType() || FeeType.PREPAYMENT_FEE == tmp.getFeeType()) {
                    tmp.setFeeAmount(new BigDecimal(0));
                } else {
                    if (FeeChargeType.LOAN_AMOUNT_RATE == tmp.getChargeType()) {
                        Loan loanTmp = loanService.fetchById(id);
                        if (null != loanTmp.getAmount() && null != tmp.getFeeRate()) {
                            tmp.setFeeAmount(loanTmp.getAmount().multiply(tmp.getFeeRate()).divide(new BigDecimal(100)));
                        }
                    } else if (FeeChargeType.FIXED_AMOUNT == tmp.getChargeType() || FeeChargeType.LOAN_REQUEST_INPUT == tmp.getChargeType()) {

                    } else {
                        tmp.setFeeAmount(new BigDecimal(0));
                    }
                }
            }
        }

        loan.put("interestRate", DecimalFormatUtils.removeZeroFormat(loan.get("interestRate")));
        loan.put("interestAmount",DecimalFormatUtils.removeZeroFormat(loan.get("interestAmount")));
        Map data = new HashMap<>();
        data.put("loanBorrowers",loanBorrowers);
        data.put("borrower",borrower);
        data.put("borrowerAccounts",borrowerAccounts);
        data.put("productMediaAttaches",productMediaAttaches);
        data.put("loan",loan);
        data.put("loanFeeTemps",loanFeeTemps);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    /**
     * 查找业务信息
     * @param id
     * @return
     */
    public Object queryBusinessInfo(String id){
        NutMap result = new NutMap();
        List<ProductInfoItem> productInfoItems =  productInfoItemService.queryByLoanId(id);
        Map data = new HashMap<>();
        Boolean flag =false;
        int showFlag = 0;
        for (ProductInfoItem pit:productInfoItems){
            data.put(pit.getKeyName(), pit.getDataValue());
            if(("user").equals(pit.getKeyName())){
                flag = true;
                showFlag = 1;
            }
        }
        List<HouseInfo> houseList =houseInfoService.queryByLoanId(id);
        data.put("hasOldHouse",flag);
        data.put("showOldHouse",showFlag);
        data.put("bakAccountList",bankInfoService.queryFormatBankInfoByLoanId(id));
        data.put("houseList",houseList);
        if(CollectionUtils.isNotEmpty(houseList)){
            data.put("budgetAllPrice",houseList.get(0).getBudgetAllPrice());
        }
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    /**
     * 查找业务信息
     * @param id
     * @return
     */
    public Object queryBusinessInfoToMap(String id){
        NutMap result = new NutMap();
        List<ProductInfoItem> productInfoItems =  productInfoItemService.queryByLoanId(id);
        Map data = new HashMap<>();
        Boolean flag =false;
        int showFlag = 0;
        for (ProductInfoItem pit:productInfoItems){
            String maps = pit.getDataValue();
            if(StringUtils.isNotEmpty(maps)){
                if("json".equals(pit.getType())){
                    maps = maps.substring(1,maps.length()-1);
                    JSONArray jsonArray = JSONArray.fromObject(maps);//把String转换为json
                    List list = JSONArray.toList(jsonArray,HashMap.class);//这里的t是Class<T>
                    data.put(pit.getKeyName(),list);
                }else{
                    data.put(pit.getKeyName(),maps);
                }

            }
            if(("user").equals(pit.getKeyName())){
                flag = true;
                showFlag = 1;
            }
        }
        data.put("hasOldHouse",flag);
        data.put("showOldHouse",showFlag);
        data.put("bakAccountList",bankInfoService.queryFormatBankInfoByLoanId(id));
        data.put("houseList",houseInfoService.queryByLoanId(id));
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    public Object riskApproveInfoComplete(String loanId) {
        NutMap result = new NutMap();
        result.put("ok",true);

        LoanRiskInfo loanRiskInfo = loanRiskInfoService.fetchByLoanId(loanId);
        if (null == loanRiskInfo) {
            result.put("msg","请先填写并保存授信方案！");
            result.put("isCompleted",false);
            return result;

        }
        List<ProductMediaAttach> productMediaAttachs = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.RISK);
        for (ProductMediaAttach productMediaAttach : productMediaAttachs) {
            if (productMediaAttach.isRequired()&& CollectionUtils.isEmpty(productMediaAttach.getProductMediaAttachDetails())) {
                result.put("msg","请上传并保存必要的风控资料！");
                result.put("isCompleted",false);
                return result;
            }
        }
        result.put("isCompleted",true);
        return result;

    }
}
