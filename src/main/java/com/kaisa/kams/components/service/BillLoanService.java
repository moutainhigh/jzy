package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.BaseLoanService;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.components.view.loan.BillRepay;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunwanchao on 2017/2/27.
 */
@IocBean(fields = "dao")
public class BillLoanService extends BaseLoanService {

    @Inject
    private LoanService loanService;

    @Inject
    private FlowService flowService;

    @Inject
    private BillMediaAttachService billMediaAttachService;

    @Inject
    private ProductMediaAttachService productMediaAttachService;

    @Inject
    private MediaTemplateService mediaTemplateService;

    @Inject
    private ProductService productService;

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private ProductProfitService productProfitService;

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private LoanedResultService loanedResultService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private IntermediaryApplyService intermediaryApplyService;

    @Inject
    private EnterpriseService enterpriseService;

    private final static BigDecimal YEAR_DAYS = new BigDecimal(360);

    private final static BigDecimal ONE_HUNDRED = new BigDecimal(100);

    @Aop(TransAop.READ_COMMITTED)
    public NutMap addBorrower(String productId,
                              String saleId,
                              String loanSubjectId,
                              String masterBorrowerId,
                              String attachItemStr,
                              String loanId,
                              User user) {
        NutMap resultMap = new NutMap();
        Product prd = this.dao().fetch(Product.class, productId);
        if (null == prd) {
            return resultMap.setv("ok", false).setv("msg", "查找不到产品");
        }
        BusinessUser businessUser = this.dao().fetch(BusinessUser.class, saleId);
        if (businessUser == null) {
            return resultMap.setv("ok", false).setv("msg", "查找不到业务员");
        }
        Borrower borrower = this.dao().fetch(Borrower.class, masterBorrowerId);
        if (null == borrower) {
            return resultMap.setv("ok", false).setv("msg", "查找不到主借款人");
        }

        Loan loan = this.getLoan(loanId, prd.getCode(), saleId, businessUser, loanSubjectId, prd.getTypeId(), productId, user);
        BillLoan billLoan;
        if (StringUtils.isEmpty(loan.getId())) {
            billLoan = new BillLoan();
            billLoan.setLoan(loan);
            billLoan.setCreateBy(user.getName());
            billLoan.setCreateTime(new Date());
            billLoan.setUpdateBy(user.getName());
            billLoan.setUpdateTime(new Date());
            billLoan = this.dao().insertWith(billLoan, "loan");
            //更新影像模板loanId
            Product product = productService.fetchEnableProductById(productId);
            if(null != product){
                //新增所有的影像资料
                String billLoanId = billLoan.getLoanId();
                ProductMediaAttach productMediaAttachFirst;
                List<ProductMediaItem> productMediaItems = mediaTemplateService.queryByTmp(product.getMediaTmpId());
                for (ProductMediaItem productMediaItem : productMediaItems) {
                    if(!(ProductMediaItemType.BILL).equals(productMediaItem.getMediaItemType())){
                        productMediaAttachFirst = new ProductMediaAttach(productMediaItem);
                        productMediaAttachFirst.setCreateBy(user.getName());
                        productMediaAttachFirst.setUpdateBy(user.getName());
                        productMediaAttachFirst.setCreateTime(new Date());
                        productMediaAttachFirst.setUpdateTime(new Date());
                        productMediaAttachFirst.setLoanId(billLoanId);
                        productMediaAttachFirst = productMediaAttachService.add(productMediaAttachFirst);
                    }
                }
            }
            List<ProductMediaAttach> productMediaAttaches = Json.fromJsonAsList(ProductMediaAttach.class, attachItemStr);
            for (ProductMediaAttach productMediaAttach : productMediaAttaches) {
                productMediaAttach.setLoanId(billLoan.getLoanId());
                String mediaDetail = JSONArray.fromObject(productMediaAttach.getProductMediaAttachDetails()).toString();
                List<ProductMediaAttachDetail> mediaDetailList = productMediaAttach.getProductMediaAttachDetails();
                billMediaAttachService.addProductAndBillMediaAttach(productMediaAttach,mediaDetail,masterBorrowerId,productMediaAttach.getTmplId(),productId);

            }
            if (null == billLoan) {
                return resultMap.setv("ok", false).setv("msg", "保存失败");
            }
        } else {
            billLoan = this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loan.getId()));
            billLoan.setLoan(loan);
            billLoan.setUpdateBy(user.getName());
            billLoan.setUpdateTime(new Date());
            this.dao().updateWith(billLoan, "loan");
            //更新影像模板loanId
            List<ProductMediaAttach> productMediaAttaches = Json.fromJsonAsList(ProductMediaAttach.class, attachItemStr);
            List<ProductMediaAttach> oldProductMediaAttaches = productMediaAttachService.queryDetailByLoanIdAndType(billLoan.getLoanId(),ProductMediaItemType.BILL);
            boolean flag = billMediaAttachService.mediaAttachcompare(productMediaAttaches,oldProductMediaAttaches);
            if(!flag){
                    billMediaAttachService.deleteByLoanIdAndType(billLoan.getLoanId(),ProductMediaItemType.BILL);
            }
            for (ProductMediaAttach productMediaAttach : productMediaAttaches) {
                productMediaAttach.setLoanId(billLoan.getLoanId());
                String mediaDetail = JSONArray.fromObject(productMediaAttach.getProductMediaAttachDetails()).toString();
                List<ProductMediaAttachDetail> mediaDetailList = productMediaAttach.getProductMediaAttachDetails();
                billMediaAttachService.updateProductAndBillMediaAttach(productMediaAttach,mediaDetail,masterBorrowerId,productMediaAttach.getTmplId(),billLoan.getLoanId());

            }
        }

        this.dao().clear(LoanBorrower.class, Cnd.where("loanId", "=", billLoan.getLoanId()));
        LoanBorrower master = this.getLoanBorrower(loan.getId(), borrower, user);
        master = this.dao().insert(master);
        loan.setMasterBorrowerId(master.getId());
        this.dao().update(loan);
        return resultMap.setv("loan", loan).setv("ok", true);
    }

    private LoanBorrower getLoanBorrower(String loanId, Borrower borrower, User user) {
        LoanBorrower loanBorrower = new LoanBorrower(borrower);
        loanBorrower.setLoanId(loanId);
        loanBorrower.setStatus(PublicStatus.ABLE);
        loanBorrower.setMaster(true);
        loanBorrower.setCreateBy(user.getName());
        loanBorrower.setCreateTime(new Date());
        loanBorrower.setUpdateBy(user.getName());
        loanBorrower.setUpdateTime(new Date());
        return loanBorrower;
    }

    private Loan getLoan(String loanId, String prdCode, String saleId, BusinessUser businessUser, String loanSubjectId, String productTypeId, String productId, User user) {
        Loan l;
        if(StringUtils.isEmpty(loanId) || (l=this.dao().fetch(Loan.class,loanId))==null){
            l = new Loan();
            l.setCode(prdCode+loanService.fetchMaxCode(productId));
        }
        l.setSaleId(saleId);
        l.setSaleCode(businessUser.getCode());
        l.setSaleName(businessUser.getName());
        l.setLoanSubjectId(loanSubjectId);
        l.setStatus(PublicStatus.ABLE);
        l.setProductId(productId);
        l.setApplyId(user.getId());
        l.setProductTypeId(productTypeId);
        l.setCreateBy(user.getName());
        l.setCreateTime(new Date());
        l.setUpdateBy(user.getName());
        l.setUpdateTime(new Date());
        if(null == l.getSource()){
            if(null != user.getType() && ChannelUserType.CHANNEL_USER.equals(user.getType())){
                l.setLoanStatus(LoanStatus.CHANNELSAVE);
                l.setSource(SourceType.CHANNEL);
                l.setChannelApplyId(user.getId());
            }else {
                l.setLoanStatus(LoanStatus.SAVE);
                l.setSource(SourceType.BUSINESS);
            }
        }
        l.setMinInterestAmount(new BigDecimal(0));
        return l;
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap addBillLoan(BigDecimal totalAmount,
                              BigDecimal interest,
                              Date discountTime,
                              String accountName,
                              String accountBank,
                              String accountNo,
                              String loanId,
                              String loanSubjectId,
                              User user,
                              String intermediaryStr) {
        NutMap resultMap = new NutMap();
        if (StringUtils.isEmpty(loanId)) {
            return resultMap.setv("ok", false).setv("msg", "请先保存贴现人信息");
        }
        Date now = new Date();
        BillLoan billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        if(billLoan == null){
            return resultMap.setv("ok",false).setv("msg","请先保存贴现人信息");
        }
        if (StringUtils.isEmpty(intermediaryStr)) {
            billLoan.setIntermediaryId(null);
        }
        if (StringUtils.isEmpty(loanSubjectId) && user.getType().equals(ChannelUserType.COMPANY_USER)) {
            resultMap.put("ok", false);
            resultMap.put("msg", "放款主体不存在");
            return resultMap;
        }
        billLoan.setTotalAmount(totalAmount);
        billLoan.setInterest(interest);
        billLoan.setAccountName(accountName);
        billLoan.setAccountBank(accountBank);
        billLoan.setAccountNo(accountNo);
        billLoan.setUpdateBy(user.getName());
        billLoan.setUpdateTime(now);
        billLoan.setIntermediaryTotalFee(getBillTotalIntermediaryFee(loanId));
        if (StringUtils.isNotEmpty(intermediaryStr)) {
            List<Intermediary> intermediaryList = Json.fromJsonAsList(Intermediary.class,intermediaryStr);
            if(CollectionUtils.isNotEmpty(intermediaryList)){
                billLoan.setIntermediaryId(intermediaryList.get(0).getId());
                billLoan.setName(intermediaryList.get(0).getName());
                billLoan.setIdNumber(intermediaryList.get(0).getIdNumber());
                billLoan.setAccount(intermediaryList.get(0).getAccount());
                billLoan.setBank(intermediaryList.get(0).getBank());
                billLoan.setPhone(intermediaryList.get(0).getPhone());
                billLoan.setAddress(intermediaryList.get(0).getAddress());
            }

        }

//        for(Intermediary intermediary:intermediaryList){
//            Intermediary intermediaryTemp = new Intermediary();
//            intermediaryTemp.setId(intermediary.getId());
//            intermediaryTemp.setName(intermediary.getName());
//            intermediaryTemp.setIdNumber(intermediary.getIdNumber());
//            intermediaryTemp.setAddress(intermediary.getAddress());
//            intermediaryTemp.setAccount(intermediary.getAccount());
//            intermediaryTemp.setPhone(intermediary.getPhone());
//            intermediaryTemp.setBank(intermediary.getBank());
//            intermediaryTemp.setStatus(PublicStatus.ABLE);
//            intermediaryTemp.setCreateBy(ShiroSession.getLoginUser().getName());
//            intermediaryTemp.setUpdateBy(ShiroSession.getLoginUser().getName());
//            intermediaryTemp.setCreateTime(new Date());
//            intermediaryTemp.setUpdateTime(new Date());
//
//            Intermediary dbIntermediary = borrowerService.fetchByIdNumber(intermediary.getIdNumber());
//            if (null != dbIntermediary) {
//                intermediaryTemp.setId(dbIntermediary.getId());
//                borrowerService.updateIntermediary(intermediaryTemp);
//                billLoan.setIntermediaryId(dbIntermediary.getId());
//            } else {
//                Intermediary addIntermediary = borrowerService.add(intermediaryTemp);
//                billLoan.setIntermediaryId(addIntermediary.getId());
//            }
////
////            if(StringUtils.isNotEmpty(intermediary.getId())){
////                if(StringUtils.isNotEmpty(intermediary.getIdNumber()) && null == borrowerService.fetchByIdNumberAndId(intermediary.getIdNumber(),intermediary.getId())){
////                    borrowerService.update(intermediaryTemp);
////                }
////
////                if(StringUtils.isEmpty(billLoan.getIntermediaryId())){
////                    billLoan.setIntermediaryId(intermediaryTemp.getId());
////                }
////            }else {
////                if(StringUtils.isNotEmpty(intermediary.getIdNumber()) && null == borrowerService.fetchByIdNumber(intermediary.getIdNumber())){
////                    Intermediary addIntermediary =borrowerService.add(intermediaryTemp);
////                    billLoan.setIntermediaryId(addIntermediary.getId());
////                }
////            }
//        }

        Loan loan = billLoan.getLoan();
        loan.setAmount(DecimalUtils.sub(billLoan.getTotalAmount(),billLoan.getInterest()));
        loan.setUpdateBy(user.getName());
        loan.setUpdateTime(now);
        loan.setLoanSubjectId(loanSubjectId);
        this.dao().updateWith(billLoan, "loan");
        return new NutMap().setv("loan", loan).setv("ok", true);
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap addBillLoanRepay(String billLoanRepayStr, String loanId, User user) {
        NutMap resultMap = new NutMap();
        if (StringUtils.isEmpty(billLoanRepayStr)) {
            return resultMap.setv("ok", false).setv("msg", "票据信息不能为空");
        }
        if (StringUtils.isEmpty(loanId)) {
            return resultMap.setv("ok", false).setv("msg", "请先保存贴现人信息");
        }
        Date now = new Date();

        List<BillRepay> billRepayList = Json.fromJsonAsList(BillRepay.class, billLoanRepayStr);
        //Collections.sort(billRepayList, (o1, o2) -> o2.getDueDate().compareTo(o1.getDueDate()));
        Date term = getLastTerm(billRepayList);
        BillLoan billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class,Cnd.where("loanId","=",loanId)),"loan");
        billLoan.getLoan().setTermType(LoanTermType.FIXED_DATE);
        billLoan.getLoan().setTerm(TimeUtils.formatDate("yyyy-MM-dd",term));

        this.dao().clear(LoanRepay.class, Cnd.where("loanId", "=", loanId));
        this.dao().clear(BillLoanRepay.class, Cnd.where("loanId", "=", loanId));
        int period = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal interest = BigDecimal.ZERO;
        int postion = 0;
        Date disDate = null;
        for (BillRepay billRepay : billRepayList) {
            totalAmount = DecimalUtils.sum(totalAmount,billRepay.getAmount());
            interest = DecimalUtils.sum(interest,billRepay.getInterest());
            BillLoanRepay loanBillRepay = new BillLoanRepay();
            loanBillRepay.setLoanId(loanId);
            loanBillRepay.setBillNo(billRepay.getBillNo());
            loanBillRepay.setDrawTime(billRepay.getDrawTime());
            loanBillRepay.setPayer(billRepay.getPayer());
            loanBillRepay.setPayerId(billRepay.getPayerId());
            loanBillRepay.setPayerAccount(billRepay.getPayerAccount());
            loanBillRepay.setRiskRank(billRepay.getRiskRank());
            loanBillRepay.setPayee(billRepay.getPayee());
            loanBillRepay.setPayeeAccount(billRepay.getPayeeAccount());
            loanBillRepay.setPayeeBankName(billRepay.getPayeeBankName());
            loanBillRepay.setBankName(billRepay.getBankName());
            loanBillRepay.setBankAddress(billRepay.getBankAddress());
            loanBillRepay.setOverdueDays(billRepay.getOverdueDays());
            loanBillRepay.setCreateBy(user.getName());
            loanBillRepay.setCreateTime(now);
            loanBillRepay.setUpdateBy(user.getName());
            loanBillRepay.setUpdateTime(now);
            loanBillRepay.setDisDate(billRepay.getDisDate());
            loanBillRepay.setDisDays(billRepay.getDisDays());
            loanBillRepay.setActualDueDate(billRepay.getActualDueDate());
            loanBillRepay.setCostRate(billRepay.getCostRate());
            loanBillRepay.setIntermediaryFee(getBillIntermediaryFee(billRepay));
            loanBillRepay.setMinCost(billRepay.getMinCost());
            loanBillRepay.setDepositRate(billRepay.getDepositRate());
            loanBillRepay.setDepositFlag(billRepay.getDepositFlag());

            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loanId);
            loanRepay.setPeriod(period++);
            loanRepay.setAmount(billRepay.getAmount());
            loanRepay.setInterest(billRepay.getInterest());
            loanRepay.setDueDate(billRepay.getDueDate());
            loanRepay.setStatus(LoanRepayStatus.LOANED);
            loanRepay.setCreateBy(user.getName());
            loanRepay.setCreateTime(now);
            loanRepay.setUpdateBy(user.getName());
            loanRepay.setUpdateTime(now);
            loanBillRepay.setLoanRepay(loanRepay);

            loanBillRepay.setPosition(postion++);
            dao().insertWith(loanBillRepay, "loanRepay");
            disDate = billRepay.getDisDate();

        }
        billLoan.setTotalAmount(totalAmount);
        billLoan.setInterest(interest);
        billLoan.getLoan().setAmount(DecimalUtils.sub(totalAmount,billLoan.getInterest()));
        billLoan.setDiscountTime(disDate);
        this.dao().updateWith(billLoan,"loan");
        return resultMap.setv("loan",billLoan.getLoan()).setv("ok", true).setv("msg", "保存成功");
    }

    private Date getLastTerm(List<BillRepay> billRepayList) {
        if (CollectionUtils.isNotEmpty(billRepayList)) {
            Date term = billRepayList.get(0).getDueDate();
            for (BillRepay repay:billRepayList) {
                if (null != repay.getDueDate() && repay.getDueDate().compareTo(term)>0) {
                    term = repay.getDueDate();
                }
            }
            return term;
        }
        return null;
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap addBillLoan(String productId,
                              String saleId,
                              String loanSubjectId,
                              String masterBorrowerId,
                              String attachItemStr,
                              BigDecimal totalAmount,
                              BigDecimal interest,
                              Date discountTime,
                              String accountName,
                              String accountBank,
                              String accountNo,
                              String billLoanRepayStr,
                              String loanId,
                              User user,
                              String intermediaryStr) {
        FlowConfigureType flowType = FlowConfigureType.BORROW_APPLY;
        if (StringUtils.isNotEmpty(loanId)) {
            Loan loan = this.dao().fetch(Loan.class, loanId);
            if (loan != null) {
                productId = loan.getProductId();
            }
        }

        NutMap resultMap = addBorrower(productId, saleId, loanSubjectId, masterBorrowerId, attachItemStr, loanId, user);
        if ((Boolean) resultMap.get("ok") == false) {
            return resultMap;
        }
        Loan loan = (Loan) resultMap.get("loan");
        loanId = loan.getId();
        resultMap = this.addBillLoanRepay(billLoanRepayStr, loanId, user);
        if ((Boolean) resultMap.get("ok") == false) {
            return resultMap;
        }
        resultMap = this.addBillLoan(totalAmount,interest,discountTime, accountName, accountBank, accountNo, loanId,loanSubjectId, user, intermediaryStr);
        if ((Boolean) resultMap.get("ok") == false) {
            return resultMap;
        }
        if (!flowService.existSnakerFile(loan.getProductId(),flowType)) {
            return resultMap.setv("ok", false).setv("msg", "提交失败:流程引擎未部署！");
        }
        if (flowService.startInstance(loan.getProductId(), loanId,flowType) == null) {
            return resultMap.setv("ok", false).setv("msg", "提交失败:初始化流程引擎失败！");
        }
//        屏蔽例利润配置检测
//        ProductProfit productProfit = productProfitService.fetchEnableProductProfitByProductTypeId(loan.getProductTypeId());
//        ProductType productType = productTypeService.fetchById(loan.getProductTypeId());
//        String type = productType.getProductType();
//        if(productProfit==null&&(StringUtils.isNotEmpty(type)&&!"BAOLIPINGTAI".contains(type))){
//            resultMap.put("ok", false);
//            resultMap.put("msg", "没有对应产品利润参数配置");
//            return resultMap;
//        }

        loan = (Loan) resultMap.get("loan");
        //判断企业共享额度
        Product product = productService.fetchEnableProductById(loan.getProductId());
        if(null != user.getType() && ChannelUserType.COMPANY_USER.equals(user.getType())){
            if (null != product && product.getName().equals("商业承兑汇票")) {
                resultMap = getAmount(loanId);
                if ((Boolean) resultMap.get("ok") == false) {
                    return resultMap;
                }
            }
        }
        loan.setSubmitTime(new Date());
        if(null != loan.getSource() && SourceType.CHANNEL.equals(loan.getSource()) && loan.getLoanStatus().equals(LoanStatus.CHANNELSAVE)){
            loan.setLoanStatus(LoanStatus.SAVE);
        }else {
            loan.setLoanStatus(LoanStatus.SUBMIT);
        }
        loan.setUpdateBy(user.getName());
        loan.setUpdateTime(new Date());

        if (this.dao().update(loan) > 0) {
            //生成利润，如果以前提交过生成过利润，则删除，重新生成
            //不是保理平台才做利润计算处理
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
            return resultMap.setv("ok", true);
        }
        return resultMap.setv("ok", false).setv("msg", "提交失败");
    }

    public NutMap queryBillLoanInfo(String loanId) {
        NutMap resultMap = new NutMap();
        BillLoan billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        List<BillLoanRepay> billLoanRepayList = this.queryRepayOrderByPosition(loanId);
        List<ProductMediaAttach> productMediaAttachList =productMediaAttachService.queryDetailByLoanIdAndType(billLoan.getLoanId(),ProductMediaItemType.BILL);
        List<ProductMediaAttach> riskMediaAttachesList = productMediaAttachService.queryDetailByLoanIdAndType(loanId, ProductMediaItemType.RISK);
        LoanBorrower loanBorrower = this.dao().fetch(LoanBorrower.class, billLoan.getLoan().getMasterBorrowerId());
        Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
        //Intermediary intermediary = borrowerService.fetchIntermediaryById1(billLoan.getIntermediaryId());
        Intermediary intermediary = getIntermediary(billLoan);
        resultMap.put("intermediary", intermediary);
        resultMap.put("billLoan", billLoan);
        resultMap.put("billLoanRepayList", billLoanRepayList);
        resultMap.put("productMediaAttachList", productMediaAttachList);
        resultMap.put("riskMediaAttachesList", riskMediaAttachesList);
        resultMap.put("loanBorrower", borrower);
        return resultMap.setv("ok", true).setv("msg", "查询成功");
    }

    public NutMap queryBillLoanInfoForIntermediaryApply(String loanId,String intermediaryApplyId) {
        NutMap resultMap = new NutMap();
        BillLoan billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        List<BillLoanRepay> billLoanRepayList = this.queryRepayOrderByPosition(loanId);
        LoanBorrower loanBorrower = this.dao().fetch(LoanBorrower.class, billLoan.getLoan().getMasterBorrowerId());
        Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
        IntermediaryApply intermediaryApply = intermediaryApplyService.fetchById(intermediaryApplyId);
        resultMap.put("intermediaryApply", intermediaryApply);
        resultMap.put("billLoan", billLoan);
        resultMap.put("billLoanRepayList", billLoanRepayList);
        resultMap.put("loanBorrower", borrower);
        return resultMap.setv("ok", true).setv("msg", "查询成功");
    }

    public NutMap queryBillLoanRepayMap(String loanId) {
        return new NutMap().setv("loanRepayList", this.queryBillLoanRepay(loanId));
    }

    public NutMap queryBillLoanRepayById(String id){
        NutMap resultMap = new NutMap();
        BillLoanRepay billLoanRepay = this.dao().fetchLinks(this.dao().fetch(BillLoanRepay.class, id), "loanRepay");
        if (billLoanRepay == null) {
            return resultMap.setv("ok",false).setv("msg","还款计划不存在");
        }
        LoanRepay loanRepay = billLoanRepay.getLoanRepay();
        if(loanRepay == null){
            return resultMap.setv("ok",false).setv("msg","还款计划不存在");
        }

        List<LoanRepayRecord> loanRepayRecordList = this.dao().query(LoanRepayRecord.class,Cnd.where("repayId","=",loanRepay.getId()));
        Loan loan = dao().fetch(Loan.class,loanRepay.getLoanId());
        String borrowId = loan.getMasterBorrowerId();
        if(StringUtils.isNotEmpty(borrowId)){
            LoanBorrower loanBorrower = dao().fetch(LoanBorrower.class,borrowId);
            loanRepayRecordList.stream().forEach(r->r.setRepayments(loanBorrower.getName()));
        }

        return resultMap.setv("ok",true).setv("loanRepayRecordList",loanRepayRecordList);
    }

    @Aop(TransAop.READ_COMMITTED)
    public boolean preRepayment(String loanId){
        List<BillLoanRepay> canprelist  = queryCanPreRepayList(loanId);
        for(BillLoanRepay billLoanRepay : canprelist){
            //确认还请
            clearLoanRepayHandler(loanId,billLoanRepay.getId());
        }
        return true;
    }

    private void clearLoanRepayHandler(String loanId,String repayId){
        boolean flag = false;
        flag = clearLoanRepay(repayId);
        if(flag){
            Loan loan = loanService.fetchById(loanId);
            Product product = productService.fetchEnableProductById(loan.getProductId());
            if(null != product && product.getName().equals("商业承兑汇票")) {
                changeAmountForClear(repayId,loan);
            }
        }
    }

    public  List<BillLoanRepay> queryCanPreRepayList(String loanId){
        List<BillLoanRepay> list = queryBillLoanRepay(loanId);
        List<BillLoanRepay> canPreRepayList = new ArrayList<BillLoanRepay>();
        for(BillLoanRepay billLoanRepay : list){
            if(LoanRepayStatus.LOANED.equals(billLoanRepay.getLoanRepay().getStatus())){
                canPreRepayList.add(billLoanRepay);
            }
        }
        return canPreRepayList;
    }


    public List<BillLoanRepay> queryBillLoanRepay(String loanId) {
        List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "asc"));
        List<BillLoanRepay> billLoanRepayList = this.dao().query(BillLoanRepay.class, Cnd.where("loanId", "=", loanId));
        Map<String, BillLoanRepay> repayId2BillLoanRepayMap = billLoanRepayList.stream().collect(Collectors.toMap(b -> b.getRepayId(), b -> b));

        //order by period
        List<BillLoanRepay> resultList = new ArrayList<>();
        loanRepayList.stream().forEach(l -> {
            if (repayId2BillLoanRepayMap.containsKey(l.getId())) {
                BillLoanRepay billLoanRepay = repayId2BillLoanRepayMap.get(l.getId());
                billLoanRepay.setLoanRepay(l);
                resultList.add(billLoanRepay);
            }
        });
        return resultList;
    }

    public List<BillLoanRepay> queryRepayOrderByPosition(String loanId) {
        List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId));
        List<BillLoanRepay> billLoanRepayList = this.dao().query(BillLoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("position","asc"));
        billLoanRepayList.stream().forEach(repay -> {
            repay.setLoanRepay(getLoanRepayById(loanRepayList,repay.getRepayId()));
        });
        return billLoanRepayList;
    }

    private LoanRepay getLoanRepayById(List<LoanRepay> loanRepayList,String id){
        if (CollectionUtils.isNotEmpty(loanRepayList) && StringUtils.isNotEmpty(id)) {
            for(LoanRepay loanRepay: loanRepayList) {
                if (id.equals(loanRepay.getId())) {
                    return loanRepay;
                }
            }
        }
        return null;
    }

    public BigDecimal queryRepayAmount(String payerId) {
        BigDecimal total = BigDecimal.ZERO;
        List<BillLoanRepay> billLoanRepayList = this.dao().query(BillLoanRepay.class, Cnd.where("payerId", "=", payerId).orderBy("position","asc"));
        if(CollectionUtils.isNotEmpty(billLoanRepayList)){
            for (BillLoanRepay billLoanRepay : billLoanRepayList){
                Loan loan = loanService.fetchById(billLoanRepay.getLoanId());
                if(loan.getLoanStatus().equals(LoanStatus.LOANED)
                        || loan.getLoanStatus().equals(LoanStatus.SUBMIT)
                        || loan.getLoanStatus().equals(LoanStatus.APPROVEEND)
                        || loan.getLoanStatus().equals(LoanStatus.OVERDUE)){
                    List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class, Cnd.where("id", "=", billLoanRepay.getRepayId()).and("status","in",new LoanRepayStatus[]{LoanRepayStatus.LOANED,LoanRepayStatus.OVERDUE}));
                    if(CollectionUtils.isNotEmpty(loanRepayList)){
                        for (LoanRepay loanRepay : loanRepayList){
                            total = total.add(loanRepay.getAmount());
                        }
                    }
                }
            }
        }
        return total;
    }


    @Aop(TransAop.READ_COMMITTED)
    public NutMap loan(String loanId, String subjectId, String loanSubjectAccountId, Date loanTime) {
        User user = ShiroSession.getLoginUser();
        Loan loan = dao().fetch(Loan.class, loanId);
        Date lastUpdateTime = loan.getUpdateTime();
        Date now = new Date();
        loan.setUpdateBy(user.getLogin());
        loan.setUpdateTime(now);
        loan.setLoanTime(loanTime);
        loan.setActualAmount(loan.getAmount());
        loan.setLoanSubjectId(subjectId);
        loan.setLoanSubjectAccountId(loanSubjectAccountId);
        loan.setLoanStatus(LoanStatus.LOANED);

        Trans.exec((Atom) () -> {
            dao().update(loan);
            //还需要保存贷款相关节点到上贷款后操作节点里面，不要问为什么，需求就是这样
            loanedResultService.addLoanRecode(loan,lastUpdateTime);
            syncToChannel(loan.getChannelId());
        });
        NutMap result = loanRepayService.initLoanRecord(loanId,subjectId,loanSubjectAccountId,loanTime,user);
        if(null != result && null != result.get("loanRecordList")){
            dao().insert(result.get("loanRecordList"));
        }
        return new NutMap().setv("ok", true);
    }

    @Aop(TransAop.READ_COMMITTED)
    public boolean clearLoanRepay(String repayId) {
        BillLoanRepay billLoanRepay = dao().fetchLinks(dao().fetch(BillLoanRepay.class, repayId), "loanRepay");
        if (billLoanRepay == null) {
            return false;
        }
        LoanRepay loanRepay = billLoanRepay.getLoanRepay();
        if(loanRepay == null){
            return false;
        }
        Date now = new Date();
        insertLoanRepayRecord(loanRepay,now);
        updateLoanRepay(loanRepay,now);
        updateLoan(loanRepay,now);
        syncToLoanRepay(loanRepay.getId(),true);
        return true;
    }

    private void updateLoan(LoanRepay loanRepay,Date now) {
        User user = ShiroSession.getLoginUser();
        List<LoanRepay> loanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId","=",loanRepay.getLoanId()));
        boolean isOverdue = false;
        boolean isCleared = true;
        for(LoanRepay lr : loanRepayList){
            LoanRepayStatus loanRepayStatus = lr.getStatus();
            if(LoanRepayStatus.CLEARED.equals(loanRepayStatus) || LoanRepayStatus.AHEAD_CLEARED.equals(loanRepayStatus) || LoanRepayStatus.OVERDUE_CLEARED.equals(loanRepayStatus)){
               continue;
            }
            if(LoanRepayStatus.OVERDUE.equals(lr.getStatus())){
                isCleared = false;
                isOverdue = true;
                break;
            } else {
                isCleared = false;
            }
        }
        Loan loan = dao().fetch(Loan.class,loanRepay.getLoanId());
        LoanStatus loanStatus = loan.getLoanStatus();
        if(isOverdue){
            loan.setLoanStatus(LoanStatus.OVERDUE);
        } else {
            loan.setLoanStatus(LoanStatus.LOANED);
        }
        if(isCleared){
            loan.setLoanStatus(LoanStatus.CLEARED);
            loan.setClearDate(now);
            addLoanRecode(loan,loan.getLoanTime());
            addClearLoanRecode(loan.getId());
        }
        if(!loanStatus.equals(loan.getLoanStatus())){
            loan.setUpdateBy(user.getName());
            loan.setUpdateTime(now);
            dao().update(loan);
        }
    }

    private void updateLoanRepay(LoanRepay loanRepay,Date now) {
        User user = ShiroSession.getLoginUser();
        loanRepay.setStatus(getLoanRepayStatus(loanRepay));
        loanRepay.setUpdateBy(user.getName());
        loanRepay.setRepayDate(now);
        loanRepay.setUpdateTime(now);
        dao().update(loanRepay);
    }

    private LoanRepayStatus getLoanRepayStatus(LoanRepay loanRepay) {
        if (LoanRepayStatus.OVERDUE.equals(loanRepay.getStatus())) {
           return LoanRepayStatus.OVERDUE_CLEARED;
        }
        if (LoanRepayStatus.AHEAD_CLEARED.equals(loanRepay.getStatus())) {
            return LoanRepayStatus.AHEAD_CLEARED;
        }
        return LoanRepayStatus.CLEARED;
    }

    private void insertLoanRepayRecord(LoanRepay loanRepay,Date now) {
        User user = ShiroSession.getLoginUser();
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayId(loanRepay.getId());
        loanRepayRecord.setRepayAmount(loanRepay.getAmount());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getAmount());
        loanRepayRecord.setRepayDate(now);
        loanRepayRecord.setCreateBy(user.getName());
        loanRepayRecord.setCreateTime(now);
        loanRepayRecord.setUpdateTime(now);
        loanRepayRecord.setUpdateBy(user.getName());
        dao().insert(loanRepayRecord);
    }

    /**
     * 新增
     * @param billLoan
     * @return
     */
    public BillLoan add(BillLoan billLoan) {
        if(null==billLoan) {
            return null;
        }
        return dao().insert(billLoan);
    }

    /**
     * 新增
     * @param billLoanRepayList
     * @return
     */
    public List add(List<BillLoanRepay> billLoanRepayList) {
        if(null==billLoanRepayList) {
            return null;
        }
        return dao().insert(billLoanRepayList);
    }

    /**
     * 新增
     * @param loanRepayList
     * @return
     */
    public List addLoanRepay(List<LoanRepay> loanRepayList) {
        if(null==loanRepayList) {
            return null;
        }
        return dao().insert(loanRepayList);
    }

    /**
     * 通过loanid查询billloan
     * @param loanId
     * @return
     */
    public BillLoan fetchBillLoanByLoanId(String loanId){
        if(StringUtils.isNotEmpty(loanId)){
            BillLoan billLoan = this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId));
            return billLoan;
        }
        return null;
    }

    /**
     * 计算居间费
     * @return
     */
    public BigDecimal getBillIntermediaryFee(BillRepay billRepay){
        BigDecimal intermediaryFee = BigDecimal.ZERO;
        if (null == billRepay) {
            return intermediaryFee;
        }
        BigDecimal amount = billRepay.getAmount();
        BigDecimal costRate = billRepay.getCostRate();
        BigDecimal disDays = new BigDecimal(billRepay.getDisDays());
        BigDecimal interest = billRepay.getInterest();
        if (null == amount) {
            return intermediaryFee;
        }
        if (null == costRate) {
            return intermediaryFee;
        }
        if (null == interest) {
            return intermediaryFee;
        }

        intermediaryFee =  amount.multiply(costRate).multiply(disDays);

        intermediaryFee = intermediaryFee.divide(ONE_HUNDRED.multiply(YEAR_DAYS), MathContext.DECIMAL64);

        return interest.subtract(intermediaryFee);
    }

    /**
     * 计算总居间费
     * @return
     */
    public BigDecimal getBillTotalIntermediaryFee(String loanId){
        if (StringUtils.isEmpty(loanId)) {
            return BigDecimal.ZERO;
        }
        Cnd cnd = Cnd.where("loanId","=",loanId);
        BigDecimal totalAmount =  (BigDecimal) dao().func2(BillLoanRepay.class, "sum", "intermediaryFee",cnd);
        if (null != totalAmount) {
            return totalAmount;
        }
        return BigDecimal.ZERO;
    }


    /**
     * 更新代扣代缴税费、税后居间费
     * @param billLoan
     */
    public boolean updateFee(BillLoan billLoan) {
        if(null==billLoan ||  StringUtils.isEmpty(billLoan.getId())){
            return false;
        }
        User user= ShiroSession.getLoginUser();
        int flag = dao().update(BillLoan.class, Chain.make("withholdingTaxFee",billLoan.getWithHoldingTaxFee())
                .add("afterTaxIntermediaryFee",billLoan.getAfterTaxIntermediaryFee())
                .add("updateBy",user.getName()).add("updateTime",new Date())
                ,Cnd.where("loanId","=",billLoan.getLoanId()));
        return flag>0;
    }

    public Intermediary getIntermediary(BillLoan billLoan){
        if (null == billLoan) {
            return null;
        }
        Intermediary temp = new Intermediary();
        Intermediary resultIntermediary = borrowerService.fetchIntermediaryById(billLoan.getIntermediaryId());
        if(null != resultIntermediary){
            temp.setId(resultIntermediary.getId());
            temp.setName(billLoan.getName());
            temp.setIdNumber(billLoan.getIdNumber());
            temp.setBank(billLoan.getBank());
            temp.setAccount(billLoan.getAccount());
            temp.setAddress(billLoan.getAddress());
            temp.setPhone(billLoan.getPhone());
            temp.setContractFileUrls(resultIntermediary.getContractFileUrls());
            temp.setServiceContractFileUrls(resultIntermediary.getServiceContractFileUrls());
            temp.setStatus(resultIntermediary.getStatus());
            temp.setCreateBy(resultIntermediary.getCreateBy());
            temp.setCreateTime(resultIntermediary.getCreateTime());
            temp.setUpdateBy(resultIntermediary.getUpdateBy());
            temp.setUpdateTime(resultIntermediary.getUpdateTime());

        }
        return temp;
    }

    /**
     * 居间费放款回写居间人信息
     * @param billLoan
     */
    public boolean updateBillLoanForIntermediary (BillLoan billLoan) {
        Date now = new Date();
        User user = ShiroSession.getLoginUser();
        if(null==billLoan ||  StringUtils.isEmpty(billLoan.getId())){
            return false;
        }
        int flag = dao().update(BillLoan.class, Chain.make("phone",billLoan.getPhone())
                        .add("address",billLoan.getAddress())
                        .add("bank",billLoan.getBank())
                        .add("account",billLoan.getAccount())
                        .add("updateBy",user.getName()).add("updateTime",new Date())
                ,Cnd.where("loanId","=",billLoan.getLoanId()));
        return flag>0;
    }

    private NutMap getAmount(String loanId){
        NutMap result = new NutMap();
        int flag = 0;
        List<BillLoanRepay> billLoanRepayList = dao().query(BillLoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("position","asc"));
        for (BillLoanRepay billLoanRepay : billLoanRepayList){
            if(StringUtils.isNotEmpty(billLoanRepay.getRepayId())){
               Enterprise enterprise = enterpriseService.fetchById(billLoanRepay.getPayerId());
               LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(billLoanRepay.getRepayId());
                if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
                    result = compareAndUpdate(result, billLoanRepay, enterprise, loanRepay,null);
                    if ((Boolean) result.get("ok") == false) {
                        break;
                    }
                }else {
                    Enterprise creditEnterprise = enterpriseService.fetchById(enterprise.getCompanyId());
                    result = compareAndUpdate(result, billLoanRepay, creditEnterprise, loanRepay,enterprise);
                    if ((Boolean) result.get("ok") == false) {
                        break;
                    }
                }
                flag++;
            }
        }
        if(flag != billLoanRepayList.size()){
           for (int i = 0;i<flag;i++){
               if(StringUtils.isNotEmpty(billLoanRepayList.get(i).getRepayId())){
                   Enterprise enterprise = enterpriseService.fetchById(billLoanRepayList.get(i).getPayerId());
                   LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(billLoanRepayList.get(i).getRepayId());
                   if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
                       backAmount(enterprise, loanRepay,null);
                   }else {
                       Enterprise creditEnterprise = enterpriseService.fetchById(enterprise.getCompanyId());
                       backAmount(creditEnterprise, loanRepay,enterprise);
                   }
               }
           }

        }
        return  result;
    }

    private NutMap compareAndUpdate(NutMap result, BillLoanRepay billLoanRepay, Enterprise enterprise, LoanRepay loanRepay, Enterprise subEnterprise) {
        if(loanRepay.getAmount().compareTo(enterprise.getRemainderAmount()==null?BigDecimal.ZERO:enterprise.getRemainderAmount())>0){
            result.setv("ok", false).setv("msg", "第"+(billLoanRepay.getPosition()+1)+"张票的企业授信额度不足");
            return result;
        }else {
            result.setv("ok", true);
            if(null != subEnterprise && CompanyType.SUBCOMPANY.equals(subEnterprise.getType())){
                subEnterprise.setLibraryAmount((subEnterprise.getLibraryAmount()==null?BigDecimal.ZERO:subEnterprise.getLibraryAmount()).add(loanRepay.getAmount()));
            }
            if(null != enterprise.getCreditQuota()){
                //enterprise.setLibraryAmount(enterprise.getLibraryAmount().subtract(loanRepay.getAmount()));
                enterprise.setRemainderAmount(enterprise.getRemainderAmount().subtract(loanRepay.getAmount()));
            }

            if(null != subEnterprise){
                enterpriseService.update(subEnterprise);
            }
            enterpriseService.update(enterprise);
        }
        return  result;
    }

    private void backAmount(Enterprise enterprise, LoanRepay loanRepay, Enterprise subEnterprise) {
            if(null != subEnterprise && CompanyType.SUBCOMPANY.equals(subEnterprise.getType())){
                subEnterprise.setLibraryAmount((subEnterprise.getLibraryAmount()==null?BigDecimal.ZERO:subEnterprise.getLibraryAmount()).subtract(loanRepay.getAmount()));
            }
            if(null != enterprise.getCreditQuota()){
                enterprise.setRemainderAmount(enterprise.getRemainderAmount().add(loanRepay.getAmount()));
            }

            if(null != subEnterprise){
                enterpriseService.update(subEnterprise);
            }
            enterpriseService.update(enterprise);
    }

    public void changeAmount(String loanId){
        Loan loan = loanService.fetchById(loanId);
        List<BillLoanRepay> billLoanRepayList = dao().query(BillLoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("position","asc"));
        for (BillLoanRepay billLoanRepay : billLoanRepayList){
            if(StringUtils.isNotEmpty(billLoanRepay.getRepayId())){
                Enterprise enterprise = enterpriseService.fetchById(billLoanRepay.getPayerId());
                LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(billLoanRepay.getRepayId());
                if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
                    addAmount(loan, enterprise, loanRepay,null);
                }else {
                    Enterprise creditEnterprise = enterpriseService.fetchById(enterprise.getCompanyId());
                    addAmount(loan, creditEnterprise, loanRepay,enterprise);
                }
            }
        }
    }

    public void addAmount(Loan loan, Enterprise enterprise, LoanRepay loanRepay, Enterprise subEnterprise) {
        if(LoanStatus.CANCEL.equals(loan.getLoanStatus())
                || LoanStatus.LOANCANCEL.equals(loan.getLoanStatus())
                || LoanStatus.APPROVEREJECT.equals(loan.getLoanStatus())
                || (LoanStatus.SAVE.equals(loan.getLoanStatus()))) {
            setValue(enterprise, loanRepay, subEnterprise);
            enterpriseService.update(enterprise);
            if(null != subEnterprise){
                enterpriseService.update(subEnterprise);
            }

        }else {
            if(LoanRepayStatus.OVERDUE_CLEARED.equals(loanRepay.getStatus()) || LoanRepayStatus.CLEARED.equals(loanRepay.getStatus()) || LoanRepayStatus.AHEAD_CLEARED.equals(loanRepay.getStatus())){
                setValue(enterprise, loanRepay, subEnterprise);
                enterpriseService.update(enterprise);
                if(null != subEnterprise){
                    enterpriseService.update(subEnterprise);
                }
            }
        }
    }

    /**
     * 还清单独更新授信额度
     */
    public void changeAmountForClear(String repayId,Loan loan){
        BillLoanRepay billLoanRepay = dao().fetchLinks(dao().fetch(BillLoanRepay.class, repayId), "loanRepay");
        Enterprise enterprise = enterpriseService.fetchById(billLoanRepay.getPayerId());
        LoanRepay loanRepay = loanRepayService.getLoanRepayByRepayId(billLoanRepay.getRepayId());
        if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
            addAmount(loan, enterprise, loanRepay,null);
        }else {
            Enterprise creditEnterprise = enterpriseService.fetchById(enterprise.getCompanyId());
            addAmount(loan, creditEnterprise, loanRepay,enterprise);
        }
    }

    private void setValue(Enterprise enterprise, LoanRepay loanRepay, Enterprise subEnterprise) {
        if(null != subEnterprise && CompanyType.SUBCOMPANY.equals(subEnterprise.getType())) {
            subEnterprise.setLibraryAmount((subEnterprise.getLibraryAmount()==null?BigDecimal.ZERO:subEnterprise.getLibraryAmount()).subtract(loanRepay.getAmount()));
        }
        if(null != enterprise.getCreditQuota()){
            enterprise.setRemainderAmount(enterprise.getRemainderAmount().add(loanRepay.getAmount()));
        }
    }


}
