package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.controller.base.BusinessApplyBaseController;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.service.report.GerendaiReportService;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.components.view.report.ServiceQueryView;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.entity.Order;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sunwanchao on 2016/11/23.
 */
@IocBean
@At("/business_apply")
public class BusinessApplyController extends BusinessApplyBaseController {

    private static final Logger log = LoggerFactory.getLogger(BusinessApplyController.class);

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private BusinessUserService businessUserService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private MediaTemplateService mediaTemplateService;

    @Inject
    private ProductFeeService productFeeService;

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private GerendaiReportService gerendaiReportService;

    @Inject
    private FlowConfigureService flowConfigureService;

    @Inject
    private LoanMortgageService loanMortgageService;

    @Inject
    private MortgageService mortgageService;


    public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 跳转空白页面
     *
     * @return
     */
    @At("/check_image")
    @Ok("beetl:/common/check_image.html")
    public void detailList() {}

    @At
    @Ok("beetl:/businessApply/list.html")
    @RequiresPermissions("businessApply_query:view")
    public Context list() {
        return setStatusListInContext();
    }

    @At("/submit")
    @POST
    @Ok("json")
    public boolean submit(HttpServletRequest request) {
        return false;
    }

    /**
     * 查询产品申请列表
     */
    @At("/query_product")
    @Ok("beetl:/businessApply/queryProduct.html")
    @RequiresPermissions("businessApply_apply:view")
    public Context queryProduct() {
        Context ctx = Lang.context();
        List<Map> result = new ArrayList<>();

        //查询所有的productType
        List<ProductType> types = productTypeService.queryAbleAll();

        //循环所有的productType查询到所有的产品
        for (ProductType type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", type);

            //查询产品列表
            List<Product> prds = null == type ? null : productService.queryAbleByType(type.getId());
            item.put("prds", prds);
            result.add(item);
        }
        ctx.set("result", result);
        return ctx;
    }


    /**
     * 查询申请列表
     */
    @At("/query_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_query:view")
    public Object queryList(@Param("..")DataTableParam param) {
        return loanService.query(param);
    }

    @At("/list_all")
    @Ok("beetl:/businessApply/list_all.html")
    @RequiresPermissions("businessApply_query_all:view")
    public Context listAll() {
        return setStatusListInContext();
    }

    private Context setStatusListInContext() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanStatus.values());
        return ctx;
    }

    @At("/list_businessuser")
    @Ok("beetl:/businessApply/list_businessuser.html")
    @RequiresPermissions("businessApply_query_businessuser:view")
    public Context listBusinessUser() {
        return setStatusListInContext();
    }


    /**
     * 查询所有申请列表--针对业务人员-只会显示他能看到的部分
     */
    @At("/query_list_businessuser")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_query_businessuser:view")
    public Object queryListBusinessuser(@Param("..")DataTableParam param) {
        return loanService.queryAllBusinessUser(param);
    }

    @At("/query_list_businessuser_export")
    @Ok("void")
    @RequiresPermissions("businessApply_query_businessuser:view")
    public void listExport(@Param("apply")String apply,
                                 @Param("orgId")String orgId,
                                 @Param("borrower")String borrower,
                                 @Param("status")String status,
                                 @Param("channelId")String channelId,
                                 @Param("submitTime")String submitTime,
                                 @Param("product")String product,
                                 @Param("productType")String productType,
                                 @Param("repayDate")String repayDate,
                                 HttpServletResponse resp )throws Exception {

        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        param.setSearchKeys(map);
        param.getSearchKeys().put("apply", apply);
        param.getSearchKeys().put("orgId",orgId);
        param.getSearchKeys().put("borrower",borrower);
        param.getSearchKeys().put("channelId",channelId);
        param.getSearchKeys().put("status",status);
        param.getSearchKeys().put("submitTime",submitTime);
        param.getSearchKeys().put("product",product);
        param.getSearchKeys().put("productType",productType);
        param.getSearchKeys().put("repayDate",repayDate);

        List<ServiceQueryView> cdlist= loanService.queryAllBusinessUser(param).getData();
        for(ServiceQueryView cd :cdlist){

            if("CANCEL".equals(cd.getLoanStatus()) || "SAVE".equals(cd.getLoanStatus())){
                cd.setApproveStatus(gerendaiReportService.getStatus(cd.getLoanStatus()));
            }else if("APPROVEEND".equals(cd.getLoanStatus()) || "LOANED".equals(cd.getLoanStatus()) || "CLEARED".equals(cd.getLoanStatus())|| "OVERDUE".equals(cd.getLoanStatus())|| "APPROVEREJECT".equals(cd.getLoanStatus())){
                cd.setApproveStatus(cd.getApproveStatus());
            }else {
                if(null != cd.getNextStatus()){
                   cd.setApproveStatus(cd.getNextStatus()+"-等待审批");
                }
            }

            cd.setLoanStatus(gerendaiReportService.getStatus(cd.getLoanStatus()));
            cd.setTerm(gerendaiReportService.getTermType(cd.getTermType(),cd.getTerm()));

        }
        ExcelExportUtil.export(resp,cdlist,"业务单查询");
    }

    /**
     * 查询所有申请列表
     */
    @At("/query_list_all")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_query_all:view")
    public Object queryListAll(@Param("..")DataTableParam param) {
        return loanService.queryAllByChannel(param);
    }

    @At("/list_bill")
    @Ok("beetl:/businessApply/list_bill2.html")
    @RequiresPermissions("businessApply_query_bill:view")
    public void listBill() {}


    /**
     * 查询票据--业务人员
     */
    @At("/businessuser_bill")
    @Ok("beetl:/businessApply/businessuser_bill2.html")
    @RequiresPermissions("businessApply_query_bill:view")
    public void businessUserBill() {
    }

    /**
     * 查询票据列表
     */
    @At("/query_list_bill")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_query_bill:view")
    public Object queryListBill(@Param("..")DataTableParam param) {
        return loanService.queryBill(param);
    }

    /**
     * 查询票据列表--业务人员
     */
    @At("/query_businessuser_bill")
    @POST
    @Ok("json")
    @RequiresPermissions("businessApply_businessuser_bill:view")
    public Object businessUserListBill(@Param("start") int start,
                                       @Param("length") int length,
                                       @Param("draw") int draw,
                                       @Param("billCode") String billCode,
                                       @Param("payer") String payer,
                                       @Param("bankName") String bankName,
                                       @Param("discountTime") String discountTime,
                                       @Param("beginAmount") BigDecimal beginAmount,
                                       @Param("endAmount") BigDecimal endAmount,
                                       @Param("billType") String billType) {
        Date beginDate = TimeUtils.getQueryStartDateTime(discountTime);
        Date endDate = TimeUtils.getQueryEndDateTime(discountTime);
        return loanService.queryBillBusinessuser(start, length, draw, billCode, payer,bankName, beginDate, endDate,beginAmount,endAmount,billType);
    }

    /**
     * 跳转到业务申请页面
     */
    @At("/to_add")
    @Ok("re")
    //@RequiresPermissions("businessApply_apply:create")
    public String toAdd(@Param("productId") String productId,HttpServletRequest request) {


        //查询关联产品
        Product prd = productService.fetchEnableProductById(productId);

        //查询关联产品类型
        ProductType prdType = null == prd ? null : productTypeService.fetch(prd.getTypeId());

        //查询放款主体
        List<LoanSubject> loanSubjects = loanSubjectService.queryAble();

        //查询证件类型
        LoanerCertifType[] loanerCertifTypes = LoanerCertifType.values();


        request.setAttribute("loanerCertifTypes", loanerCertifTypes);
        request.setAttribute("prd", prd);
        request.setAttribute("prdType", prdType);
        request.setAttribute("loanSubjects", loanSubjects);
        request.setAttribute("user", ShiroSession.getLoginUser());

        ProductInfoTmpl productInfoTmpl = productInfoTmplService.fetchById(prd.getInfoTmpId());
        return "beetl:" + productInfoTmpl.getAddEntryUrl();
    }


    /**
     * 添加借款人信息（包括资产来源和放款主体两个附加信息）
     *
     * @param saleId         资产来源业务员Id
     * @param loanSubjectId  放款主题Id
     * @param borrowers      借款人信息json字符串
     * @param productId      产品Id
     * @param productTypeId  产品类型Id
     * @param masterBorrower 主借款人
     * @param borrowers      共同借款人JSON
     * @param accounts       账号JSON
     */
    @At("/add_borrowers")
    @POST
    @Ok("json")
    public Object addBorrowers(@Param("saleId") String saleId,
                               @Param("engagedSaleId") String engagedSaleId,
                               @Param("amount") BigDecimal amount,
                               @Param("loanSubjectId") String loanSubjectId,
                               @Param("productId") String productId,
                               @Param("productTypeId") String productTypeId,
                               @Param("masterBorrower") String masterBorrower,
                               @Param("borrowers") String borrowers,
                               @Param("accounts") String accounts,
                               @Param("step") String step,
                               @Param("channelId") String channelId, @Param("channelType") String channelType) {
        NutMap result = new NutMap();
        List<String>  channelTypeList=new ArrayList<String>();
        User user = ShiroSession.getLoginUser();
        //验证参数是否合法
        if (StringUtils.isEmpty(borrowers)) {
            result.put("ok", false);
            result.put("msg", "无借款人信息");
            return result;
        }

        //查找到产品
        Product prd = productService.fetchEnableProductById(productId);
        if (null == prd) {
            result.put("ok", false);
            result.put("msg", "查找不到产品");
            return result;
        }

        //查询主借款人
        Borrower borrower = borrowerService.fetchById(masterBorrower);
        if (null == borrower) {
            result.put("ok", false);
            result.put("msg", "查找不到主借款人");
            return result;
        }

        //查找业务员
        BusinessUser sale = businessUserService.fetchById(saleId);
        if (null == sale) {
            result.put("ok", false);
            result.put("msg", "查找不到业务员");
            return result;
        }
        //查找承揽业务员
        BusinessUser engagedSale = businessUserService.fetchById(engagedSaleId);
        if (null == engagedSale) {
            result.put("ok", false);
            result.put("msg", "查找不到承揽业务员");
            return result;
        }

        //获取产品业务单号
        String prdCode = prd.getCode();

        //订单后8位修正编号
        String fixCode = loanService.fetchMaxCode(productId);

        //生成业务单号
        String code = prdCode + fixCode;

        Loan l = new Loan();
        l.setCode(code);
        l.setAmount(amount);
        l.setSaleId(saleId);
        l.setSaleCode(sale.getCode());
        l.setSaleName(sale.getName());
        l.setEngagedSaleId(engagedSaleId);
        l.setEngagedSaleName(engagedSale.getName());
        l.setLoanSubjectId(loanSubjectId);
        l.setStatus(PublicStatus.ABLE);
        l.setProductId(productId);
        l.setApplyId(ShiroSession.getLoginUser().getId());
        l.setProductTypeId(productTypeId);
        l.setCreateBy(ShiroSession.getLoginUser().getName());
        l.setCreateTime(new Date());
        l.setUpdateBy(ShiroSession.getLoginUser().getName());
        l.setUpdateTime(new Date());
        l.setStep(step);
        if(null != user.getType() && ChannelUserType.CHANNEL_USER.equals(user.getType())){
            l.setLoanStatus(LoanStatus.CHANNELSAVE);
            l.setSource(SourceType.CHANNEL);
            l.setChannelApplyId(user.getId());
        }else {
            l.setLoanStatus(LoanStatus.SAVE);
            l.setSource(SourceType.BUSINESS);
        }
        l.setMinInterestAmount(new BigDecimal(0));
        l.setChannelId(channelId);
        //如果是赎楼，生成要件控制状态，默认为控制状态
        ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(productId);
        if(tmpl!=null){
            if(tmpl.getProductTempType().equals(ProductTempType.SHULOU)){
                l.setElementStatus("NOT_CONTROL");
            }
        }
        Loan resultLoan = loanService.add(l);

        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "保存失败");
            return result;
        }

        //保存主借款人
        LoanBorrower master = getLoanMasterBorrower(borrower, resultLoan);

        //修改主借款人信息
        Loan loan1 = new Loan();
        loan1.setId(resultLoan.getId());
        loan1.setMasterBorrowerId(master.getId());
        loanService.update(loan1);


        //添加借款人关联信息
        addLoanBorrowers(borrowers, resultLoan);

        //添加借款账号
        addAccounts(accounts, resultLoan);

        //新增所有的费用

        channelTypeList.add("ALL");
        channelTypeList.add(channelType);
        List<ProductFee> productFees = productFeeService.queryFeeByProductIdAndChannlType(productId,channelTypeList);

        for (ProductFee productFee : productFees) {
            LoanFeeTemp loanFeeTemp = new LoanFeeTemp(productFee);
            loanFeeTemp.setCreateBy(ShiroSession.getLoginUser().getName());
            loanFeeTemp.setCreateTime(new Date());
            loanFeeTemp.setUpdateTime(new Date());
            loanFeeTemp.setUpdateTime(new Date());
            loanFeeTemp.setLoanId(resultLoan.getId());
            loanFeeTempService.add(loanFeeTemp);
        }

        //新增所有的影像资料
        List<ProductMediaItem> productMediaItems = mediaTemplateService.queryByTmp(prd.getMediaTmpId());
        for (ProductMediaItem productMediaItem : productMediaItems) {
            ProductMediaAttach productMediaAttach = new ProductMediaAttach(productMediaItem);
            productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach.setCreateTime(new Date());
            productMediaAttach.setUpdateTime(new Date());
            productMediaAttach.setLoanId(resultLoan.getId());
            productMediaAttachService.add(productMediaAttach);
        }

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", resultLoan);
        return result;
    }

    private LoanBorrower getLoanMasterBorrower(Borrower borrower, Loan resultLoan) {
        LoanBorrower master = new LoanBorrower(borrower);
        master.setLoanId(resultLoan.getId());
        master.setStatus(PublicStatus.ABLE);
        master.setCreateBy(ShiroSession.getLoginUser().getName());
        master.setCreateTime(new Date());
        master.setUpdateBy(ShiroSession.getLoginUser().getName());
        master.setUpdateTime(new Date());
        master.setMaster(true);
        loanBorrowerService.add(master);
        return master;
    }

    private void addLoanBorrowers(@Param("borrowers") String borrowers, Loan resultLoan) {
        List<String> loanBorrowerList = Json.fromJsonAsList(String.class, borrowers);
        for (String id : loanBorrowerList) {
            //通过Id查找
            Borrower item = borrowerService.fetchById(id);
            LoanBorrower join = new LoanBorrower(item);
            join.setLoanId(resultLoan.getId());
            join.setStatus(PublicStatus.ABLE);
            join.setCreateBy(ShiroSession.getLoginUser().getName());
            join.setCreateTime(new Date());
            join.setUpdateBy(ShiroSession.getLoginUser().getName());
            join.setUpdateTime(new Date());
            join.setMaster(false);
            loanBorrowerService.add(join);
        }
    }

    private void addAccounts(@Param("accounts") String accounts, Loan resultLoan) {
        List<BorrowerAccount> borrowerAccountList = Json.fromJsonAsList(BorrowerAccount.class, accounts);
        int position = 0;
        for (BorrowerAccount account : borrowerAccountList) {
            account.setStatus(PublicStatus.ABLE);
            account.setCreateBy(ShiroSession.getLoginUser().getName());
            account.setCreateTime(new Date());
            account.setUpdateBy(ShiroSession.getLoginUser().getName());
            account.setUpdateTime(new Date());
            account.setLoanId(resultLoan.getId());
            account.setPosition(position++);
            borrowerAccountService.add(account);
        }
    }

    private int getAccount(String loadId,Channel channel){
        BigDecimal accountSubmit=new BigDecimal(0);
        BigDecimal cooperationAmount;
        BigDecimal residualAmount;
        List<BorrowerAccount> borrowerAccountList = borrowerAccountService.queryByLoanId(loadId);
        for (BorrowerAccount account : borrowerAccountList) {
            accountSubmit=accountSubmit.add(account.getAmount());
        }
        // 判断渠道资金是否充足
        if(channel.getDepositLimit()==null){
            cooperationAmount=new BigDecimal(0);
        }else {
            cooperationAmount=channel.getDepositLimit();
        }
        if(channel.getResidualAmount()==null){
            residualAmount=new BigDecimal(0);
        }else {
            residualAmount=channel.getResidualAmount();
        }
        return  cooperationAmount.subtract(residualAmount).compareTo(accountSubmit);
    }


    /**
     * 修改借款人信息（包括资产来源和放款主体两个附加信息）
     *
     * @param saleId         资产来源业务员Id
     * @param loanSubjectId  放款主题Id
     * @param borrowers      借款人信息json字符串
     * @param productId      产品Id
     * @param productTypeId  产品类型Id
     * @param masterBorrower 主借款人
     * @param borrowers      共同借款人JSON
     * @param accounts       账号JSON
     */
    @At("/update_borrowers")
    @POST
    @Ok("json")
    public Object updateBorrowers(@Param("id") String id,
                                  @Param("saleId") String saleId,
                                  @Param("engagedSaleId") String engagedSaleId,
                                  @Param("amount") BigDecimal amount,
                                  @Param("loanSubjectId") String loanSubjectId,
                                  @Param("productId") String productId,
                                  @Param("productTypeId") String productTypeId,
                                  @Param("masterBorrower") String masterBorrower,
                                  @Param("borrowers") String borrowers,
                                  @Param("accounts") String accounts,
                                  @Param("step") String step,
                                  @Param("channelId") String channelId,
                                  @Param("channelSearchName") String channelSearchName,@Param("channelType") String channelType) {
        NutMap result = new NutMap();
        User user = ShiroSession.getLoginUser();
        List<String>  channelTypeList=new ArrayList<String>();
        //验证参数是否合法
        if (StringUtils.isEmpty(borrowers)) {
            result.put("ok", false);
            result.put("msg", "无借款人信息");
            return result;
        }

        //查找到产品
        Product prd = productService.fetchEnableProductById(productId);
        if (null == prd) {
            result.put("ok", false);
            result.put("msg", "查找不到产品");
            return result;
        }

        //查询主借款人
        Borrower borrower = borrowerService.fetchById(masterBorrower);
        if (null == borrower) {
            result.put("ok", false);
            result.put("msg", "查找不到主借款人");
            return result;
        }

        //查找业务员
        BusinessUser sale = businessUserService.fetchById(saleId);
        if (null == sale) {
            result.put("ok", false);
            result.put("msg", "查找不到业务员");
            return result;
        }

        //查找承揽业务员
        BusinessUser engagedSale = businessUserService.fetchById(engagedSaleId);
        if (null == engagedSale) {
            result.put("ok", false);
            result.put("msg", "查找不到承揽业务员");
            return result;
        }
        String loanChannelId = getCheckedChannel(channelSearchName,channelId);
        Loan loan = new Loan();
        loan.setId(id);
        loan.setSaleId(saleId);
        loan.setEngagedSaleId(engagedSaleId);
        loan.setEngagedSaleName(engagedSale.getName());
        loan.setAmount(amount);
        loan.setStep(step);
        loan.setSaleCode(sale.getCode());
        loan.setSaleName(sale.getName());
        loan.setLoanSubjectId(loanSubjectId);
        loan.setUpdateBy(ShiroSession.getLoginUser().getName());
        loan.setUpdateTime(new Date());
        if(null != user.getType() && ChannelUserType.CHANNEL_USER.equals(user.getType())){
            loan.setLoanStatus(LoanStatus.CHANNELSAVE);
        }else {
            loan.setLoanStatus(LoanStatus.SAVE);
        }
        loan.setChannelId(loanChannelId);

        // l.setMasterBorrowerId(masterBorrower);
        boolean flag = loanService.update(loan);

        if (!flag) {
            result.put("ok", false);
            result.put("msg", "保存失败");
            return result;
        }

        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);

        //删除所有的借款人信息
        loanBorrowerService.deleteByLoanId(id);

        //保存主借款人
        LoanBorrower master = getLoanMasterBorrower(borrower, resultLoan);

        //修改主借款人信息
        Loan loan1 = new Loan();
        loan1.setId(resultLoan.getId());
        loan1.setMasterBorrowerId(master.getId());
        loanService.update(loan1);

        //添加借款人关联信息
        addLoanBorrowers(borrowers, resultLoan);

        borrowerAccountService.deleteByLoanId(id);
        //添加借款账号
        addAccounts(accounts, resultLoan);

        //删除费用临时表数据
       loanFeeTempService.deleteByLoanId(loan.getId());
        //修改费用临时表的数据
        //新增所有的费用

        channelTypeList.add("ALL");
        channelTypeList.add(channelType);
        List<ProductFee> productFees = productFeeService.queryFeeByProductIdAndChannlType(productId,channelTypeList);

        for (ProductFee productFee : productFees) {
            LoanFeeTemp loanFeeTemp = new LoanFeeTemp(productFee);
            loanFeeTemp.setCreateBy(ShiroSession.getLoginUser().getName());
            loanFeeTemp.setCreateTime(new Date());
            loanFeeTemp.setUpdateTime(new Date());
            loanFeeTemp.setLoanId(resultLoan.getId());
            loanFeeTempService.add(loanFeeTemp);
        }

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", resultLoan);
        return result;
    }

    /**
     * 复制单信息
     */
    @At("/new_loan")
    @POST
    @Ok("json")
    public Object newLoan(@Param("id") String id) {
        NutMap result = new NutMap();
        if (StringUtils.isEmpty (id)) {
            result.put("ok", false);
            result.put("msg", "原始单信息有问题");
        }
        User user = ShiroSession.getLoginUser();
        //获取Loan
        Loan oldloan = loanService.fetchById(id);
        Loan orgloan = loanService.fetchById(id);
        //查找到产品
        Product prd = productService.fetchEnableProductById(oldloan.getProductId());
        //查询产品模板
        ProductInfoTmpl productInfoTmpl = productInfoTmplService.fetchById(prd.getInfoTmpId());
        //获取产品业务单号
        String prdCode = prd.getCode();
        //订单后8位修正编号
        String fixCode = loanService.fetchMaxCode(oldloan.getProductId());
        //生成业务单号
        String code = prdCode + fixCode;
        Loan newloan = oldloan;
        newloan.setId(null);
        if(null != user.getType() && ChannelUserType.CHANNEL_USER.equals(user.getType())){
            newloan.setLoanStatus(LoanStatus.CHANNELSAVE);
            newloan.setSource(SourceType.CHANNEL);
        }else {
            newloan.setLoanStatus(LoanStatus.SAVE);
            newloan.setSource(SourceType.BUSINESS);
        }
        //newloan.setSubmitTime(null);
        newloan.setApproveStatus(null);
        newloan.setApproveStatusDesc(null);
        newloan.setCode(code);
        newloan = loanService.add(newloan);
        Loan finalNewLoan = newloan;
        if(null!= productInfoTmpl && ProductTempType.isBill(productInfoTmpl.getProductTempType())){
            NutMap obj = billLoanService.queryBillLoanInfo(orgloan.getId());
            BillLoan billLoan = (BillLoan) obj.get("billLoan");
            billLoan.setLoanId(finalNewLoan.getId());
            billLoanService.add(billLoan);
            List<BillLoanRepay> billLoanRepayList = obj.getList("billLoanRepayList",BillLoanRepay.class);
            //String jsonArray = Json.toJson(billLoanRepayList);
            //billLoanService.addBillLoanRepay(jsonArray.toString(),finalNewloan.getId(),ShiroSession.getLoginUser());
            List<LoanRepay> newloanRepayList = null;
            List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(orgloan.getId());
            if (CollectionUtils.isNotEmpty(loanRepayList)) {
                loanRepayList.forEach(loanRepay -> loanRepay.setLoanId(finalNewLoan.getId()));
                newloanRepayList = billLoanService.addLoanRepay(loanRepayList);
            }
            for (int j = 0; j < billLoanRepayList.size(); j++){
                BillLoanRepay blr = billLoanRepayList.get(j);
                if (CollectionUtils.isNotEmpty(newloanRepayList)) {
                    for (LoanRepay lr:newloanRepayList){
                        if(finalNewLoan.getId().equals(lr.getLoanId())){
                            billLoanRepayList.get(j).setRepayId(lr.getId());
                            billLoanRepayList.get(j).setLoanId(finalNewLoan.getId());
                        }
                    }
                }
            }
            billLoanService.add(billLoanRepayList);

        }else {
            if (null != newloan && null != newloan.getId()) {
                List<LoanBorrower> loanBorrowerList = loanBorrowerService.queryByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(loanBorrowerList)) {
                    loanBorrowerList.forEach(loanBorrower -> loanBorrower.setLoanId(finalNewLoan.getId()));
                    loanBorrowerService.add(loanBorrowerList);
                }
                List<BorrowerAccount> borrowerAccountList = borrowerAccountService.queryFormatAccountsByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(borrowerAccountList)) {
                    borrowerAccountList.forEach(borrowerAccount -> borrowerAccount.setLoanId(finalNewLoan.getId()));
                    borrowerAccountService.add(borrowerAccountList);
                }
                List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(loanFeeTempList)) {
                    loanFeeTempList.forEach(LoanFeeTemp -> LoanFeeTemp.setLoanId(finalNewLoan.getId()));
                    loanFeeTempService.add(loanFeeTempList);
                }
                List<BankInfo> bankInfoList = bankInfoService.queryByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(bankInfoList)) {
                    bankInfoList.forEach(bankInfo -> bankInfo.setLoanId(finalNewLoan.getId()));
                    bankInfoService.save(bankInfoList);
                }
                List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(houseInfoList)) {
                    houseInfoList.forEach(houseInfo -> houseInfo.setLoanId(finalNewLoan.getId()));
                    houseInfoService.save(houseInfoList);
                }
                List<ProductInfoItem> productInfoItemList = productInfoItemService.queryByLoanId(orgloan.getId());
                if (CollectionUtils.isNotEmpty(productInfoItemList)) {
                    productInfoItemList.forEach(ProductInfoItem -> ProductInfoItem.setLoanId(finalNewLoan.getId()));
                    productInfoItemService.add(productInfoItemList);
                }
            }
        }
        List<ProductMediaAttach> newProductMediaAttachList = null;
        List<ProductMediaAttach> productMediaAttachList = productMediaAttachService.queryDetailByLoanId(orgloan.getId());
        if (CollectionUtils.isNotEmpty(productMediaAttachList)) {
            productMediaAttachList.forEach(productMediaAttach -> productMediaAttach.setLoanId(finalNewLoan.getId()));
            newProductMediaAttachList = productMediaAttachService.add(productMediaAttachList);
        }
        for (int i = 0 ; i< productMediaAttachList.size();i++){
            ProductMediaAttach newPma = newProductMediaAttachList.get(i);
            if (CollectionUtils.isNotEmpty(productMediaAttachList.get(i).getProductMediaAttachDetails())) {
                for (ProductMediaAttachDetail pmad : productMediaAttachList.get(i).getProductMediaAttachDetails()){
                    productMediaAttachList.get(i).getProductMediaAttachDetails().forEach(productMediaAttachDetail -> productMediaAttachDetail.setProductMediaAttachId(newPma.getId()));
                }
                productMediaAttachService.addDetail(productMediaAttachList.get(i).getProductMediaAttachDetails());
            }
        }

        if (null!=newloan) {
            result.put("ok", true);
            result.put("msg", "修改成功");
            result.put("data", newloan);
        } else {
            result.put("ok", false);
            result.put("msg", "修改失败");
        }
        return result;
    }

    private String getCheckedChannel(String channelSearchName,String channelId) {
        if (StringUtils.isNotEmpty(channelSearchName) && StringUtils.isNotEmpty(channelId) ) {
            Channel channel = channelService.fetch(Cnd.where("id","=",channelId));
            if(null != channel && channelSearchName.equals(channel.getName())) {
                return channelId;
            }
        }
        return "";
    }


    /**
     * 跳转到详情页
     */
    @At
    @Ok("beetl:/businessApply/detail.html")
    public Context detail(@Param("id") String id,
                          @Param("tab") String tab) {
        Context ctx = Lang.context();
        ctx.set("business", false);
        ctx.set("risk", false);
        ctx.set("finance", false);
        ctx.set("loaned", false);
        ctx.set("push", false);
        ctx.set("extension", false);
        ctx.set("senior",false);
        ctx.set("flow",true);
        ctx.set("id", id);
        ctx.set("user", ShiroSession.getLoginUser());

        //获取Loan
        Loan loan = loanService.fetchById(id);

        if (null == loan) {
            return ctx;
        }

        //获取到当前的产品
        Product product = productService.fetchEnableProductById(loan.getProductId());

        if (null == product) {
            return ctx;
        }

        //查询要加载的模板
        ProductInfoTmpl productInfoTmpl = productInfoTmplService.fetchById(product.getInfoTmpId());
        ctx.set("productInfoTmpl", productInfoTmpl);

        if (StringUtils.isNotEmpty(tab)) {
            if (tab.equals("business")) {
                ctx.set("business", true);
            } else if (tab.equals("senior")) {
                ctx.set("business", true);
                ctx.set("finance", true);
                ctx.set("risk", true);
                ctx.set("senior",true);
            } else if (tab.equals("riskControl")) {
                ctx.set("business", true);
                ctx.set("risk", true);
            } else if (tab.equals("financial")) {
                ctx.set("business", true);
                ctx.set("risk", true);
                ctx.set("finance", true);
            } else if (tab.equals("postLoan")) {
                ctx.set("business", true);
                ctx.set("risk", true);
                ctx.set("finance", true);
                ctx.set("loaned", true);
                ctx.set("extension",true);
            } else if (tab.equals("pushes")) {
                ctx.set("business", true);
                ctx.set("finance", true);
                ctx.set("push", true);
            } else if(tab.equals("extension")){
                ctx.set("business",true);
                ctx.set("extension",true);
            } else if(tab.equals("slBusiness")) {
                ctx.set("loaned", true);
                ctx.set("business",true);
                ctx.set("extension", ProductTempType.hasExtension(productInfoTmpl.getProductTempType()));
            }
        }
        ctx.set("product",product);
        ctx.set("historyData","01".equals(loan.getHistoryData())?true:false);
        return ctx;
    }


    /**
     * 修改录单信息
     */
    @At("/update_loan")
    @POST
    @Ok("json")
    public Object updateLoan(@Param("..") Loan loan) {
        NutMap result = new NutMap();
        if (null == loan) {
            result.put("ok", false);
            result.put("msg", "录单信息有问题");
        }
        loan.setUpdateBy(ShiroSession.getLoginUser().getName());
        loan.setUpdateTime(new Date());
        boolean flag = loanService.update(loan);
        if (flag) {
            Loan loanReturn = loanService.fetchById(loan.getId());
            result.put("ok", true);
            result.put("msg", "修改成功");
            result.put("data", loanReturn);
        } else {
            result.put("ok", false);
            result.put("msg", "修改失败");
        }
        return result;
    }


    /**
     * 修改借款人信息
     */
    @At("/update_loan_borrower")
    @POST
    @Ok("json")
    public Object updateLoanBorrower(@Param("id") String id,
                                     @Param("masterBorrower") String masterBorrower,
                                     @Param("borrowers") String borrowers) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(borrowers)) {
            result.put("ok", false);
            result.put("msg", "无借款人信息");
            return result;
        }

        //查询主借款人
        Borrower borrower = borrowerService.fetchById(masterBorrower);
        if (null == borrower) {
            result.put("ok", false);
            result.put("msg", "查找不到主借款人");
            return result;
        }


        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);
        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        //删除所有的借款人信息
        loanBorrowerService.deleteByLoanId(id);

        //保存主借款人
        LoanBorrower master = getLoanMasterBorrower(borrower, resultLoan);

        //添加借款人关联信息
        addLoanBorrowers(borrowers, resultLoan);

        //查找到所有的借款人
        List<LoanBorrower> borrowersReturn = loanBorrowerService.queryByLoanId(id);

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", borrowersReturn);
        return result;
    }


    /**
     * 修改账户信息
     */
    @At("/update_loan_account")
    @POST
    @Ok("json")
    public Object updateLoanAccount(@Param("id") String id,
                                    @Param("accounts") String accounts) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(accounts)) {
            result.put("ok", false);
            result.put("msg", "无账号信息");
            return result;
        }

        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);
        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }


        borrowerAccountService.deleteByLoanId(id);
        //添加借款账号
        addAccounts(accounts, resultLoan);
        List<BorrowerAccount> borrowerAccountListReturn = borrowerAccountService.queryFormatAccountsByLoanId(id);

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", borrowerAccountListReturn);
        return result;
    }


    /**
     * 修改账户费用信息
     */
    @At("/update_loan_fee")
    @POST
    @Ok("json")
    public Object updateLoanFee(@Param("id") String id,
                                @Param("fees") String fees) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(fees)) {
            result.put("ok", false);
            result.put("msg", "无费用信息");
            return result;
        }


        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);

        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        //添加借款账号
        List<LoanFeeTemp> loanFeeTempList = Json.fromJsonAsList(LoanFeeTemp.class, fees);
        for (LoanFeeTemp lft : loanFeeTempList) {
            lft.setCreateBy(ShiroSession.getLoginUser().getName());
            lft.setUpdateBy(ShiroSession.getLoginUser().getName());
            lft.setUpdateTime(new Date());
            lft.setLoanId(resultLoan.getId());
            loanFeeTempService.update(lft);
        }
        List<LoanFeeTemp> loanFeeTempListReturn = loanFeeTempService.queryByLoanId(id);


        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", loanFeeTempListReturn);
        return result;
    }


    /**
     * 修改业务信息
     */
    @At("/update_loan_business")
    @POST
    @Ok("json")
    public Object updateLoanBusiness(@Param("id") String id,
                                     @Param("items") String items,
                                     @Param("step") String step,
                                     @Param("bakAccountListStr") String bakAccountListStr,
                                     @Param("houseAccountListStr") String houseAccountListStr) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(items)) {
            result.put("ok", false);
            result.put("msg", "无业务信息");
            return result;
        }


        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);

        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        productInfoItemService.deleteByLoanId(id);
        //添加借款账号
        List<ProductInfoItem> productInfoItems = Json.fromJsonAsList(ProductInfoItem.class, items);
        for (ProductInfoItem productInfoItem : productInfoItems) {
            productInfoItem.setCreateBy(ShiroSession.getLoginUser().getName());
            productInfoItem.setCreateTime(new Date());
            productInfoItem.setUpdateBy(ShiroSession.getLoginUser().getName());
            productInfoItem.setUpdateTime(new Date());
            productInfoItem.setLoanId(resultLoan.getId());
            productInfoItemService.add(productInfoItem);
        }
        if(StringUtils.isNotEmpty(bakAccountListStr)){
            List<BankInfo> bankInfoList = Json.fromJsonAsList(BankInfo.class,bakAccountListStr);
            int position = 0;
            for(BankInfo bankInfo : bankInfoList){
                bankInfo.setCreateTime(new Date());
                bankInfo.setCreateBy(ShiroSession.getLoginUser().getName());
                bankInfo.setUpdateTime(new Date());
                bankInfo.setUpdateBy(ShiroSession.getLoginUser().getName());
                bankInfo.setLoanId(resultLoan.getId());
                bankInfo.setPosition(position++);
            }

            bankInfoService.deleteByLoanId(resultLoan.getId());
            bankInfoService.save(bankInfoList);
        }

        if(StringUtils.isNotEmpty(houseAccountListStr)){
            List<HouseInfo> houseInfoList = Json.fromJsonAsList(HouseInfo.class,houseAccountListStr);
            int position = 0;
            for(HouseInfo houseInfo : houseInfoList){
                if(StringUtils.isNotEmpty(houseInfo.getOwerId())){
                    String [] idsArr = houseInfo.getOwerId().split(",");
                    List<String> idsList = Arrays.asList(idsArr);
                    StringBuffer ids = new StringBuffer();
                    for(int i=0;i<idsList.size(); i++){
                        ids.append("'"+idsList.get(i)+"'");
                        if(i!=idsList.size()-1){
                            ids.append(",");
                        }
                    }
                    List<Borrower> list = loanBorrowerService.queryPropertyOwnersByIds(ids.toString());
                    String resultStr = "";
                    for(Borrower loanBorrower : list){
                        resultStr =resultStr + loanBorrower.getName()+",";
                        houseInfo.setOwer(resultStr.substring(0,resultStr.length()-1));
                    }
                }
                houseInfo.setCreateTime(new Date());
                houseInfo.setCreateBy(ShiroSession.getLoginUser().getName());
                houseInfo.setUpdateTime(new Date());
                houseInfo.setUpdateBy(ShiroSession.getLoginUser().getName());
                houseInfo.setLoanId(resultLoan.getId());
                houseInfo.setPosition(position++);
            }

            houseInfoService.deleteByLoanId(resultLoan.getId());
            houseInfoService.save(houseInfoList);
        }

        List<ProductInfoItem> productInfoItemsReturn = productInfoItemService.queryByLoanId(id);

        resultLoan.setStep(step);
        loanService.update(resultLoan);

        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", productInfoItemsReturn);
        return result;
    }


    /**
     * 修改业务信息
     */
    @At("/update_loan_business_media")
    @POST
    @Ok("json")
    public Object updateLoanBusinessMedia(@Param("id") String id,
                                          @Param("medias") String medias,
                                          @Param("step") String step,
                                          @Param("loanSubjectId") String loanSubjectId) {
        NutMap result = new NutMap();
        User user = ShiroSession.getLoginUser();
        //验证参数是否合法
        if (StringUtils.isEmpty(medias)) {
            result.put("ok", false);
            result.put("msg", "无影像资料信息");
            return result;
        }
        if (StringUtils.isEmpty(loanSubjectId) && user.getType().equals(ChannelUserType.COMPANY_USER)) {
            result.put("ok", false);
            result.put("msg", "放款主体不存在");
            return result;
        }

        //根据Id查找
        Loan resultLoan = loanService.fetchById(id);

        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        //productMediaAttachService.deleteByLoanIdAndType(id, ProductMediaItemType.BUSINESS);
        //添加借款账号
        List<ProductMediaAttach> productMediaAttaches = Json.fromJsonAsList(ProductMediaAttach.class, medias);
        for (ProductMediaAttach productMediaAttach : productMediaAttaches) {
            String mediaDetail =JSONArray.fromObject(productMediaAttach.getProductMediaAttachDetails()).toString();
            List<ProductMediaAttachDetail> mediaDetailList = productMediaAttach.getProductMediaAttachDetails();
            productMediaAttachService.addProductMediaAttach(productMediaAttach,mediaDetail);
        }
        List<ProductMediaAttach> productMediaAttachesReturn = productMediaAttachService.queryDetailByLoanIdAndType(id, ProductMediaItemType.BUSINESS);

        resultLoan.setStep(step);
        resultLoan.setLoanSubjectId(loanSubjectId);
        loanService.update(resultLoan);
        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", productMediaAttachesReturn);
        return result;
    }

    private void addProductMediaAttach(Loan resultLoan, ProductMediaAttach productMediaAttach) {
        productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach.setCreateTime(new Date());
        productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach.setUpdateTime(new Date());
        productMediaAttach.setLoanId(resultLoan.getId());
        productMediaAttachService.add(productMediaAttach);
    }

    private void addProductMediaAttachDetail(Loan resultLoan, ProductMediaAttach productMediaAttach,String mediaDetail) {
        productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach.setCreateTime(new Date());
        productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach.setUpdateTime(new Date());
        productMediaAttach.setLoanId(resultLoan.getId());
        productMediaAttachService.addProductMediaAttach(productMediaAttach,mediaDetail);
    }


    /**
     * 提交业务信息
     */
    @At("/submit_loan")
    @POST
    @Ok("json")
    public Object submitLoan(@Param("id") String id,@Param("loanSubjectId") String loanSubjectId) {
        FlowConfigureType flowType = FlowConfigureType.BORROW_APPLY;
        NutMap result = new NutMap();
        User user = ShiroSession.getLoginUser();
        Loan loan = loanService.fetchById(id);
        if (null == loan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        if (loan.getLoanStatus() != LoanStatus.SAVE && loan.getLoanStatus() != LoanStatus.CHANNELSAVE) {
            result.put("ok", false);
            result.put("msg", "录单已提交");
            return result;
        }

        if (StringUtils.isEmpty(loanSubjectId) && user.getType().equals(ChannelUserType.COMPANY_USER)) {
            result.put("ok", false);
            result.put("msg", "放款主体不存在");
            return result;
        }

        FlowConfigure flowConfigure = flowConfigureService.getFlowConfigureByFlowypeAndProductIdMaybeNull(flowType.name(),loan.getProductId());
        //屏蔽利润计算
//        ProductProfit productProfit = productProfitService.fetchEnableProductProfitByProductTypeId(loan.getProductTypeId());
//        ProductType productType = productTypeService.fetchById(loan.getProductTypeId());
//        String type = productType.getProductType();
//        if(productProfit==null&&(StringUtils.isNotEmpty(type)&&!"BAOLIPINGTAI".contains(type))){
//            result.put("ok", false);
//            result.put("msg", "没有对应产品利润参数配置");
//            return result;
//        }

        // 校验渠道的剩余本金是否充足
        if(!Strings.isBlank(loan.getChannelId())){
            Channel channel=channelService.fetch(loan.getChannelId());
            if(channel!=null){
                if(channel.getChannelType().equals("1")){
                    // 获取借款本金
                    if(getAccount(loan.getId(),channel)==-1){
                        result.put("ok", false);
                        result.put("msg", "该渠道合作金额已不足，无法提交");
                        return result;
                    }
                }
            }
        }
        //TODO 录单完整性校验

        //查看产品的流程是否配置完全  以后流程都走新流程，不需要再看产品是否启动了流程。
        /*Product product = productService.fetchEnableProductById(loan.getProductId());
        if(null==product){
            result.put("ok", false);
            result.put("msg", "提交订单失败:产品尚未启用！");
            return result;
        }*/

        if(flowConfigure==null){
            result.put("ok", false);
            result.put("msg", "提交订单失败：该产品还未配置‘"+flowType.getDescription()+"’！");
            return result;
        }else if(PublicStatus.DISABLED.equals(flowConfigure.getStatus())){
            result.put("ok", false);
            result.put("msg", "提交订单失败：该产品对应的‘"+flowType.getDescription()+"’尚未启动！");
            return result;
        }else{
            ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(loan.getProductId(),flowType);
            if(productProcess==null){
                result.put("ok", false);
                result.put("msg", "提交订单失败：‘"+flowType.getDescription()+"’流程尚未关联该产品，请重新启动！");
                return result;
            }
        }
        //查看当前产品的流程文件是否存在
        boolean snakerExist = flowService.existSnakerFile(loan.getProductId(),flowType);

        if (!snakerExist) {
            result.put("ok", false);
            result.put("msg", "提交订单失败:流程引擎未部署！");
            return result;
        }
        Order order = flowService.startInstance(loan.getProductId(), id, flowType);
        if (null == order) {
            Loan l1 = new Loan();
            l1.setUpdateTime(new Date());
            l1.setId(id);
            if(null != user.getType() && ChannelUserType.CHANNEL_USER.equals(user.getType())){
                l1.setLoanStatus(LoanStatus.CHANNELSAVE);
            }else {
                l1.setLoanStatus(LoanStatus.SAVE);
            }
            l1.setSubmitTime(new Date());
            l1.setLoanSubjectId(loanSubjectId);
            loanService.update(l1);
            result.put("ok", false);
            result.put("msg", "提交订单失败:初始化流程引擎失败！");
            return result;
        } else {
            Loan l = new Loan();
            l.setUpdateBy(ShiroSession.getLoginUser().getName());
            l.setUpdateTime(new Date());
            l.setId(id);
            l.setSubmitTime(new Date());
            l.setLoanSubjectId(loanSubjectId);
            l.setApplyId(user.getId());
            if(null != loan.getSource() && SourceType.CHANNEL.equals(loan.getSource()) && loan.getLoanStatus().equals(LoanStatus.CHANNELSAVE)){
                l.setLoanStatus(LoanStatus.SAVE);
            }else {
                l.setLoanStatus(LoanStatus.SUBMIT);
            }
            boolean flag = loanService.update(l);

            //生成利润，如果以前提交过生成过利润，则删除，重新生成
            //如果是保理和平台则不做处理
            //屏蔽利润计算
//            if(!"BAOLIPINGTAI".contains(type)){
//                LoanProfit loanProfit   = productProfitService.fetchEnableLoanProfitByLoanId(loan.getId());
//                if(loanProfit!=null){
//                    productProfitService.deleteEnableLoanProfitByLoanId(loanProfit.getId());
//                }
//                if("PIAOJU".equals(type)){
//                    productProfitService.getBillProfit(loan,productProfit);
//                }else{
//                    productProfitService.getCommonProfit(loan,productProfit);
//                }
//            }



            if (flag) {
                result.put("ok", true);
                result.put("msg", "提交订单成功");
                return result;
            } else {
                result.put("ok", false);
                result.put("msg", "提交订单失败");
                return result;
            }

        }
    }

    /**
     * 查询待处理的业务申请列表
     */
    @At("/to_process_list")
    @GET
    @Ok("beetl:/businessApply/process_list.html")
    @RequiresPermissions("businessApply_apply:view")
    public Context toProcessList() {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 查询待处理的业务申请列表
     */
    @At("/query_process_list")
    @GET
    @Ok("json")
    @RequiresPermissions("businessApply_apply:view")
    public Object queryProcessList(@Param("start") int start,
                                   @Param("length") int length,
                                   @Param("draw") int draw) {
        String applyId = ShiroSession.getLoginUser().getId();
        return loanService.queryProcess(start, length, draw, applyId);
    }


    /**
     * 跳转到编辑页面
     */
    @At("/to_update")
    @GET
    @Ok("re")
    //@RequiresPermissions("businessApply_apply:update")
    public String toUpdate(@Param("id") String id, @Param(value = "flag", df = "1") int flag,HttpServletRequest request) {

        //查询到Loan的信息
        Loan loan = loanService.fetchById(id);

        //查找主借款人信息
        LoanBorrower loanBorrower = loanBorrowerService.fetchById(loan.getMasterBorrowerId());

        //查询Borrower信息
        Borrower borrower=borrowerService.fetchById(loanBorrower.getBorrowerId());

        //查询关联产品
        Product prd = productService.fetchEnableProductById(loan.getProductId());

        //查询关联产品类型
        ProductType prdType = null == prd ? null : productTypeService.fetch(prd.getTypeId());

        //查询产品模板
        ProductInfoTmpl productInfoTmpl = productInfoTmplService.fetchById(prd.getInfoTmpId());

        //查询放款主体
        List<LoanSubject> loanSubjects = loanSubjectService.queryAble();

        //查询证件类型
        LoanerCertifType[] loanerCertifTypes = LoanerCertifType.values();

        //查询借款人信息
        List<LoanBorrower> loanBorrowers = loanBorrowerService.queryCooperateByLoanId(id);

        //查询账户信息
        List<BorrowerAccount> borrowerAccounts = borrowerAccountService.queryFormatAccountsByLoanId(id);


        //查询业务信息
        NutMap businessMap = (NutMap) super.queryBusinessInfo(id);

        //查询影像资料信息
        List<ProductMediaAttach> productMediaAttaches = productMediaAttachService.queryDetailByLoanIdAndType(id, ProductMediaItemType.BUSINESS);
        loan.setInterestRate(DecimalFormatUtils.removeZeroFormat(loan.getInterestRate()));
        loan.setInterestAmount(DecimalFormatUtils.removeZeroFormat(loan.getInterestAmount()));
        request.setAttribute("loan", loan);
        request.setAttribute("loanerCertifTypes", loanerCertifTypes);
        request.setAttribute("prd", prd);
        request.setAttribute("prdType", prdType);
        request.setAttribute("productInfoTmpl", productInfoTmpl);
        request.setAttribute("loanSubjects", loanSubjects);
        request.setAttribute("loanBorrowers", loanBorrowers);
        request.setAttribute("borrowerAccounts", borrowerAccounts);
        request.setAttribute("borrower", borrower);
        request.setAttribute("productInfoItemData", businessMap.get("data"));
        request.setAttribute("productMediaAttaches", productMediaAttaches);
        request.setAttribute("loanBorrower", loanBorrower);
        request.setAttribute("flag", flag);
        request.setAttribute("user", ShiroSession.getLoginUser());
        if(!Strings.isBlank(loan.getChannelId())){
            Channel channel =channelService.fetch(loan.getChannelId());
            if(null!=channel){
                request.setAttribute("channelType",null !=channel.getChannelType()?channel.getChannelType():"");
                request.setAttribute("channelName",null !=channel.getName()?channel.getName():"");
                request.setAttribute("managerId",null !=channel.getManagerId()?channel.getManagerId():"");
            }
        }else {
            request.setAttribute("channelType","0");
            request.setAttribute("channelName","");
        }
        return "beetl:" + productInfoTmpl.getUpdateEntryUrl();

    }

    @At("/query_loanfee")
    @GET
    @Ok("json")
    public Object queryLoanFee(@Param("loanId") String loanId,
                               @Param("loanRepayMethod") String loanRepayMethodStr) {
        NutMap result = new NutMap();
        List data = new ArrayList<>();
        if (StringUtils.isEmpty(loanRepayMethodStr)) {
            result.put("ok", true);
            result.put("data", data);
            return result;
        }

        LoanRepayMethod loanRepayMethod = LoanRepayMethod.valueOf(loanRepayMethodStr);

        if (StringUtils.isEmpty(loanId)) {
            result.put("ok", false);
            result.put("data", data);
            result.put("msg", "录单不存在");
            return result;
        }

        if (null == loanRepayMethod) {
            result.put("ok", false);
            result.put("data", data);
            result.put("msg", "还款方式不能为空");
            return result;
        }

        //根据loanId和还款方式查询
        data = loanFeeTempService.queryByLoanIdAndLoanRepayMethod(loanId, loanRepayMethod);
        result.put("ok", true);
        result.put("data", data);
        result.put("msg", "查询成功");
        return result;
    }

    /**
     * 查询当前单所有权属人（主借款人与共同借款人）
     */
    @At("/query_propertyOwner")
    @Ok("json")
    public NutMap queryPropertyOwner(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        List<LoanBorrower> data = loanBorrowerService.queryPropertyOwnersById(loanId);
        result.put("ok",true);
        result.put("data",data);
        result.put("msg","查询成功");
        return result;
    }

    /**
     * 查询待处理的业务申请列表
     */
    @At("/to_approval_list")
    @GET
    @Ok("beetl:/businessApply/approval_list.html")
    public Context toApprovalList() {
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/bussiness_approval_list")
    @GET
    @Ok("beetl:/businessApply/approval_bussiness_list.html")
    @RequiresPermissions("businessApply_approval:view")
    public Context businessApprovalList(@Param("loanId") String loanId) {
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/risk_approval_list")
    @GET
    @Ok("beetl:/businessApply/approval_risk_list.html")
    @RequiresPermissions("businessApply_risk_approvalPending:view")
    public Context riskApprovalList(@Param("loanId") String loanId) {
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/finance_approval_list")
    @GET
    @Ok("beetl:/businessApply/approval_finance_list.html")
    @RequiresPermissions("businessApply:view")
    public Context financeApprovalList(@Param("loanId") String loanId) {
        Context ctx = Lang.context();
        return ctx;
    }


    /**
     * 修改风控信息影像资料
     */
    @At("/update_risk_media")
    @POST
    @Ok("json")
    public Object updateRiskMedia(@Param("loanId") String loanId,
                                  @Param("medias") String medias,
                                  @Param("content") String content,
                                  @Param("extensionId") String extensionId ) {
        NutMap result = new NutMap();

        //验证参数是否合法
        if (StringUtils.isEmpty(medias)) {
            result.put("ok", false);
            result.put("msg", "无影像资料信息");
            return result;
        }

        //验证参数是否合法
        if (StringUtils.isEmpty(content)) {
            result.put("ok", false);
            result.put("msg", "风控信息无数据");
            return result;
        }


        //根据Id查找
        Loan resultLoan = loanService.fetchById(loanId);

        if (null == resultLoan) {
            result.put("ok", false);
            result.put("msg", "录单信息不存在");
            return result;
        }

        //productMediaAttachService.deleteByLoanIdAndType(loanId, ProductMediaItemType.RISK);
        //添加影响资料
        List<ProductMediaAttach> productMediaAttaches = Json.fromJsonAsList(ProductMediaAttach.class, medias);
        for (ProductMediaAttach productMediaAttach : productMediaAttaches) {
            String mediaDetail =JSONArray.fromObject(productMediaAttach.getProductMediaAttachDetails()).toString();
            productMediaAttachService.updateProductMediaAttachByRisk(productMediaAttach,mediaDetail,loanId);
        }
        List<ProductMediaAttach> productMediaAttachesReturn = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.RISK);

        List<ProductMediaAttach> billMediaAttachesReturn = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.BILL);
        //修改风控信息内容

        //展期进来的
        LoanRiskInfo loanRiskInfo =null;
        if(StringUtils.isNotEmpty(extensionId)){
            loanRiskInfo = loanRiskInfoService.fetchByExtensionId(extensionId);
            if(loanRiskInfo!=null){
                loanRiskInfo.updateOperator();
                loanRiskInfo.setContent(content);
                loanRiskInfoService.update(loanRiskInfo);
            }else{
                loanRiskInfo  = new LoanRiskInfo();
                loanRiskInfo.setLoanId(loanId);
                loanRiskInfo.setContent(content);
                loanRiskInfo.setExtensionId(extensionId);
                loanRiskInfo.updateOperator();
                loanRiskInfoService.add(loanRiskInfo);
            }
        }else{  //业务进来的
            loanRiskInfo = loanRiskInfoService.fetchByLoanId(loanId);
            if (null != loanRiskInfo) {
                loanRiskInfo.setContent(content);
                loanRiskInfo.setUpdateBy(ShiroSession.getLoginUser().getName());
                loanRiskInfo.setUpdateTime(new Date());
                loanRiskInfoService.update(loanRiskInfo);
            } else {
                loanRiskInfo = new LoanRiskInfo();
                loanRiskInfo.setLoanId(loanId);
                loanRiskInfo.setContent(content);
                loanRiskInfo.setCreateBy(ShiroSession.getLoginUser().getName());
                loanRiskInfo.setCreateTime(new Date());
                loanRiskInfo.setUpdateBy(ShiroSession.getLoginUser().getName());
                loanRiskInfo.setUpdateTime(new Date());
                loanRiskInfoService.add(loanRiskInfo);
            }
        }


        Map data = new HashMap<>();
        data.put("loanRiskInfo", loanRiskInfo);
        data.put("productMediaAttachs", productMediaAttachesReturn);
        data.put("billMediaAttachs", billMediaAttachesReturn);
        data.put("flag", null != loanRiskInfo);
        result.put("ok", true);
        result.put("msg", "保存成功");
        result.put("data", data);
        return result;
    }


    @At("/to_approval_complete")
    @GET
    @Ok("beetl:/businessApply/approval_complete_list.html")
    @RequiresPermissions("businessApply_risk_approval:view")
    public Context toApprovalCompltete() {
        return setStatusListInContext();
    }

    @At("/query_apprpval_compltet_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_risk_approval:view")
    public Object queryApprovalCompleteList(@Param("..")DataTableParam param) {
        return loanService.queryApprovalCompleteList(param);
    }

    @At("/query_risk_media_manifest")
    @GET
    @Ok("json")
    public Object queryRiskediaManifest(@Param("loanId") String loanId) {
        return super.queryRiskediaManifest(loanId);
    }

    @At("/query_risk_media_manifest_list")
    @GET
    @Ok("json")
    public NutMap queryRiskediaManifestList(@Param("loanId") String loanId,@Param("extensionId") String extensionId) {
        return super.queryRiskediaManifestList(loanId,extensionId);
    }


    @At("/query_approval_list")
    @GET
    @Ok("json")
    public DataTables queryApprovalList(@Param("start") int start,
                                    @Param("length") int length,
                                    @Param("draw") int draw,
                                    @Param("type") String type) {
        return super.queryApprovalList(start, length, draw, type);
    }


    @At("/fetch_base")
    @GET
    @Ok("json")
    public NutMap fetchBaseByLoanId(@Param("id") String id) {
        return super.fetchBaseByLoanId(id);
    }

    @At("/fetch_loan")
    @GET
    @Ok("json")
    public NutMap fetchLoan(@Param("id") String id) {
        return super.fetchLoan(id);
    }


    @At("/query_business_info")
    @GET
    @Ok("json")
    public Object queryBusinessInfo(@Param("id") String id) {
        return super.queryBusinessInfo(id);
    }


    /**
     * 财务审批完成列表
     */
    @At("/to_finance_approval_complete")
    @GET
    @Ok("beetl:/businessApply/finance_approval_complete_list.html")
    @RequiresPermissions("finance_approval_query:view")
    public Context toFinanceApprovalCompltete() {
        return setStatusListInContext();
    }

    @At("/query_finance_approval_complete_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("finance_approval_query:view")
    public Object queryFinanceApprovalCompleteList(@Param("..")DataTableParam param) {
        return loanService.queryApprovalCompleteList(param);
    }

    /**
     * 取消申请单
     * @param id
     * @return
     */
    @At("/update_loanStatus")
    @POST
    @Ok("json")
    public Object updateLoanStatus(@Param("id") String id) {
        NutMap result = new NutMap();
        boolean flag = loanService.updateLoanStatus(id,LoanStatus.CANCEL);
        if (flag) {
            Loan loan = loanService.fetchById(id);
            Product product = productService.fetchEnableProductById(loan.getProductId());
            User user = ShiroSession.getLoginUser();
            if(ChannelUserType.COMPANY_USER.equals(user.getType()) && SourceType.BUSINESS.equals(loan.getSource())){
                if(null != product && ("商业承兑汇票").equals(product.getName())) {
                    billLoanService.changeAmount(id);
                }
            }

            result.put("ok", true);
            result.put("msg", "取消成功");
        } else {
            result.put("ok", false);
            result.put("msg", "取消失败");
        }
        return result;
    }

    @At("/extension")
    @GET
    @Ok("beetl:/businessApply/extension.html")
    @RequiresPermissions("business_extension:view")
    public void extend(){
    }

    /**
     * 查询当前单已关联的房产抵押
     * @param loanId
     * @return
     */
    @At("/query_loanMortgage")
    @Ok("json")
    public NutMap queryByLoanId(@Param("loanId") String loanId){
        NutMap result = new NutMap();
        List<LoanMortgage> mortgageCode = loanMortgageService.queryLoanMortgage(loanId);
        List<Mortgage> mortgages = new ArrayList<>();
        for (LoanMortgage loanMortgage: mortgageCode){
            Mortgage mortgage = mortgageService.queryAll(loanMortgage.getMortgageCode());
            mortgages.add(mortgage);
        }
        result.put("ok",true);
        result.put("data",mortgages);
        result.put("msg","查询成功");
        return result;
    }

    /**
     * 删除操作，取消当前单与房产抵押的关联，并再次查询出当前单已关联的抵押房产
     */
    @At("/deleteByMortgageCode")
    @Ok("json")
    public NutMap deleteByLoanId(@Param("loanId") String loanId,
                                 @Param("mortgageCode") String mortgageCode){
        NutMap result = new NutMap();
        loanMortgageService.deleteLoanMortgage(loanId,mortgageCode);
        //删除后再次查询出当前单已关联的抵押房产
        List<LoanMortgage> data = loanMortgageService.queryLoanMortgage(loanId);
        result.put("ok",true);
        result.put("data",data);
        result.put("msg","删除成功");
        return result;
    }

    /**
     * 关联当前单与房产抵押
     */
    @At("/add_loanMortgage")
    @Ok("json")
    public NutMap addLoanMortgage(@Param("loanId") String loanId,
                                  @Param("mortgageCode") String mortgageCode){
        NutMap result = new NutMap();
        String approvalStatusType = mortgageService.queryApprovalStatusType(mortgageCode);
        if (null == approvalStatusType){
            result.put("ok",false);
            result.put("msg","不存在该房产编号");
            return result;
        }
        if(approvalStatusType.equals("CANCEL") || approvalStatusType.equals("REJECT")){
            result.put("ok",false);
            result.put("msg","已拒绝、已取消的房产抵押编号不允许关联");
            return result;
        }
        List<LoanMortgage> repeat = loanMortgageService.queryMortgageCode(mortgageCode,loanId);
        if (repeat.size() >= 1){
            result.put("ok",false);
            result.put("msg","请不要在当前单重复关联");
            return result;
        }
        LoanMortgage loanMortgage = new LoanMortgage();
        loanMortgage.setLoanId(loanId);
        loanMortgage.setMortgageCode(mortgageCode);
        loanMortgageService.addLoanMortgage(loanMortgage);
        //插入之后再次查询已关联抵押房产的列表
        List<LoanMortgage> data = loanMortgageService.queryLoanMortgage(loanId);
        result.put("ok",true);
        result.put("data",data);
        result.put("msg","关联成功");
        return result;
    }

    /**
     * 高管审批完成列表
     */
    @At("/query_senior_executive_approval_complete_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("seniorExecutive:view")
    public Object querySeniorExecutiveApprovalCompleteList(@Param("..")DataTableParam param) {
        return loanService.queryApprovalCompleteList(param);
    }


    /**
     * 渠道查询待处理的业务申请列表
     */
    @At("/channel_process_list")
    @GET
    @Ok("beetl:/businessApply/channel/channel_process_list.html")
    @RequiresPermissions("businessApply_apply_channel:view")
    public Context channelProcessList() {
        Context ctx = Lang.context();
        ctx.set("status",LoanStatus.values());
        return ctx;
    }

    /**
     * 查询渠道待处理的业务申请列表
     */
    @At("/query_channel_process_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    //@RequiresPermissions("businessApply_apply_channel:view")
    public Object queryChannelProcessList(@Param("..")DataTableParam param) {
        return loanService.queryChannelProcess(param);
    }

    /**
     * 查询渠道已提单列表
     */
    @At("/query_channel_list")
    @POST
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("businessApply_apply_channel:view")
    public Object queryChannelList(@Param("..")DataTableParam param) {
        return loanService.query(param);
    }

    /**
     * 查询渠道产品申请列表
     */
    @At("/query_channel_product")
    @Ok("beetl:/businessApply/queryProduct.html")
    @RequiresPermissions("businessApply_apply_channel:view")
    public Context queryChannelProduct() {
        Context ctx = Lang.context();
        List<Map> result = new ArrayList<>();
        String userId = ShiroSession.getLoginUser().getId();
        User user = userService.fetchById(userId);
        List<Product> productList = null;
        HashSet<ProductType> productTypeHashSet = new HashSet<ProductType>();
        if(null != user && StringUtils.isNotEmpty(user.getProducts())){
            productList = productService.getProductsByIds(user.getProducts().trim());
            for (Product product : productList){
                ProductType productType = productTypeService.fetchById(product.getTypeId());
                productTypeHashSet.add(productType);
            }
        }
        for (ProductType type : productTypeHashSet){
            Map<String, Object> item = new HashMap<>();

            //查询产品列表
            List<Product> prds = null == type ? null : productService.queryAbleByTypeForChannel(type.getId());
            List<Product> temp = new ArrayList<>();
            for( Product p: prds){
                if(user.getProducts().trim().contains(p.getId())){
                    temp.add(p);
                    item.put("prds", temp);
                }
            }
            if(CollectionUtils.isNotEmpty(temp)){
                item.put("type", type);
                result.add(item);
            }

        }
        ctx.set("result", result);
        return ctx;
    }

    /**
     * 渠道退回和保存
     * @param id
     * @return
     */
    @At("/channel_back")
    @POST
    @Ok("json")
    public NutMap updateChannelLoanStatus(@Param("id") String id) {
        NutMap result = new NutMap();
        boolean flag = false;
        flag = loanService.updateLoanStatus(id,LoanStatus.CHANNELSAVE);
        if (flag) {
            result.put("ok", true);
            result.put("msg", "退回成功");
        } else {
            result.put("ok", false);
            result.put("msg", "退回失败");
        }
        return result;
    }

}
