package com.kaisa.kams.components.service;

import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.BaseLoanService;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.components.view.loan.Repayment;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.BusinessUser;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.entity.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunwanchao on 2016/12/15.
 */
@IocBean(fields="dao")
public class LoanRepayService extends BaseLoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanRepayService.class);

    @Inject
    private LoanFeeTempService loanFeeTempService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    protected LoanBorrowerService loanBorrowerService;

    @Inject
    protected BusinessUserService businessUserService;

    @Inject
    private BusinessOrganizeService businessOrganizeService;

    @Inject
    protected ProductService productService;

    @Inject
    private LoanService loanService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    protected ProductInfoItemService productInfoItemService;

    @Inject
    private  ProductTypeService productTypeService;

    @Inject
    private ApprovalResultService approvalResultService;

    @Inject
    private LoanedResultService loanedResultService;

    @Inject
    protected BorrowerAccountService borrowerAccountService;

    @Inject
    protected LoanSubjectAccountService loanSubjectAccountService;

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private ProductProfitService productProfitService;

    @Inject
    private FlowConfigureService flowConfigureService;

    @Inject
    protected FlowService flowService;

    @Inject
    protected IntermediaryApplyService intermediaryApplyService;

    @Inject
    protected HouseInfoService houseInfoService;

    @Inject
    protected HouseNoMortgageApplyService houseNoMortgageApplyService;
    @Inject
    private  BillLoanService billLoanService;

    @Inject
    private  ExtensionService extensionService;

    private final static BigDecimal TAXFEE1 = new BigDecimal(1.0486);

    private final static BigDecimal TAXFEE2 = new BigDecimal(0.0486);


    /**
     * 还款计划
     * @return
     */
    public NutMap generateLoanRepay(Loan loan,Date loanTime){
        User user = ShiroSession.getLoginUser();
        String loanId = loan.getId();
        //1.生成本金、利息、剩余本金还款计划
        LoanRepayPlan loanRepayPlan = LoanCalculator.calcuate(loan.getAmount(),
                loan.getTermType(),
                loan.getTerm(),
                loan.getRepayMethod(),
                loan.getLoanLimitType(),
                LoanLimitType.FIX_AMOUNT.equals(loan.getLoanLimitType())?loan.getInterestAmount():loan.getInterestRate(),
                loan.getRepayDateType(),
                loanTime,
                loan.getMinInterestAmount(),
                loan.getCalculateMethodAboutDay());

        //2.按期费用,收费节点为还款时收取，收费频率是按期
        List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId,FeeChargeNode.REPAY_NODE);
        filterLoanFeeTemp(loanFeeTempList,loan);
        List<LoanRepay> loanRepayList = new ArrayList<>();
        List<LoanFee> loanRepayFeeList = new ArrayList<>();
        Date now = new Date();
        loanRepayPlan.getRepayments().stream().forEach(r->{
            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loanId);
            loanRepay.setPeriod(r.getPeriod());
            loanRepay.setAmount(r.getPrincipal());
            loanRepay.setInterest(r.getInterest());
            loanRepay.setOutstanding(r.getOutstanding());
            loanRepay.setDueDate(r.getDueDate());
            loanRepay.setStatus(LoanRepayStatus.LOANED);
            BigDecimal feeAmount = BigDecimal.ZERO;
            feeAmount = createFee(loan, user, loanFeeTempList, loanRepayFeeList, r, loanRepay, feeAmount);
            loanRepay.setFeeAmount(feeAmount);
            loanRepay.setTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
            loanRepay.setCreateBy(user.getName());
            loanRepay.setCreateTime(now);
            loanRepay.setUpdateBy(user.getName());
            loanRepay.setUpdateTime(now);
            loanRepayList.add(loanRepay);
        });
        NutMap nutMap = new NutMap();
        nutMap.setv("loanRepayList",loanRepayList);
        nutMap.setv("loanRepayFeeList",loanRepayFeeList);
        return nutMap;
    }

    public BigDecimal createFee(Loan loan, User user, List<LoanFeeTemp> loanFeeTempList, List<LoanFee> loanRepayFeeList, Repayment r, LoanRepay loanRepay, BigDecimal feeAmount) {
        for(LoanFeeTemp feeTemp : loanFeeTempList){
            BigDecimal fee = BigDecimal.ZERO;
            FeeType feeType = feeTemp.getFeeType();
            if(!FeeType.OVERDUE_FEE.equals(feeType) && !FeeType.PREPAYMENT_FEE.equals(feeType)){
                fee = calcByChargeType(loan.getAmount(),r.getOutstanding(),BigDecimal.ZERO,BigDecimal.ZERO,feeTemp);
            }
            if(LoanRepayDateType.REPAY_PRE.equals(loan.getRepayDateType()) && loanRepay.getPeriod()==1){
                fee = BigDecimal.ZERO;
            }
            LoanFee loanFee = getLoanFee(loan.getId(),feeTemp.getId(),feeTemp.getFeeName(),loanRepay.getDueDate(), LoanRepayStatus.LOANED,fee,feeTemp.getChargeNode(),feeTemp.getFeeType(),user);
            loanFee.setPeriod(loanRepay.getPeriod());
            loanRepayFeeList.add(loanFee);
            feeAmount = feeAmount.add(fee);
        }
        return feeAmount;
    }

    @Aop(TransAop.READ_COMMITTED)
    public void saveLoanRepay(Loan loan,Date loanTime,User user){
        String loanId = loan.getId();
        NutMap nutMap = this.generateLoanRepay(loan,loanTime);
        List<LoanRepay> loanRepayList = (List<LoanRepay>)nutMap.get("loanRepayList");
        List<LoanFee> loanRepayFeeList = (List<LoanFee>)nutMap.get("loanRepayFeeList");
        //3.放款时收取，收费频率是一次性
        List<LoanFee> loanFeeList = this.queryLoanNodeFeeByLoanId(loanId,user);

        Trans.exec((Atom) () -> {
            dao().clear(LoanRepay.class, Cnd.where("loanId","=",loanId));
            List<LoanRepay> repayList = dao().insert(loanRepayList);
            Map<Integer,LoanRepay> period2Map = repayList.stream().collect(Collectors.toMap(l->l.getPeriod(),l->l));
            loanRepayFeeList.stream().forEach(l->{
                if(period2Map.containsKey(l.getPeriod())){
                    LoanRepay repay = period2Map.get(l.getPeriod());
                    if(repay!=null){
                        l.setRepayId(repay.getId());
                    }
                }
            });
            dao().clear(LoanFee.class, Cnd.where("loanId","=",loanId));
            loanFeeList.addAll(loanRepayFeeList);
            dao().insert(loanFeeList);
        });
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap loan(String loanId,String subjectId,String loanSubjectAccountId,String loanFeeInfo, Date loanTime) {
        User user = ShiroSession.getLoginUser();
        Loan loan = this.fetchLoanById(loanId);
        Date lastUpdateTime = loan.getUpdateTime();
        this.saveLoanRepay(loan,loanTime,user);
        Date now = new Date();
        List<LoanFee> loanFeeList = Json.fromJsonAsList(LoanFee.class,loanFeeInfo);
        Map<FeeType,LoanFee> type2LoanFeeMap = loanFeeList.stream().collect(Collectors.toMap(l->l.getFeeType(),l->l));
        List<LoanFee> loanFees = this.dao().query(LoanFee.class,Cnd.where("loanId","=",loanId).and("chargeNode","=",FeeChargeNode.LOAN_NODE.name()));
        loanFees.stream().forEach(l->{
            if(type2LoanFeeMap.containsKey(l.getFeeType())){
                LoanFee loanFee = type2LoanFeeMap.get(l.getFeeType());
                l.setRepayFeeAmount(loanFee.getRepayFeeAmount());
                l.setRepayDate(loanFee.getRepayDate());
                l.setUpdateTime(now);
                l.setUpdateBy(user.getName());
            }
        });
        loan.setUpdateBy(user.getLogin());
        loan.setUpdateTime(now);
        loan.setLoanTime(loanTime);
        if(loan.getActualAmount()==null){
            loan.setActualAmount(loan.getAmount());
        }
        loan.setLoanSubjectId(subjectId);
        loan.setLoanSubjectAccountId(loanSubjectAccountId);
        loan.setLoanStatus(LoanStatus.LOANED);

        //房抵贷生成房产管理
        this.saveHouseManage(loan,user);

//        Seal seal = new Seal();
//        seal.setLoanId(loanId);
//        seal.setUpdateTime(now);
//        seal.setUpdateBy(user.getName());
//        seal.setCreateTime(now);
//        seal.setCreateBy(user.getName());
//        seal.setStatus(SealStatus.UNUSED);
        Trans.exec((Atom) () -> {
            dao().update(loanFees);
            dao().update(loan);
            //还需要保存贷款相关节点到上贷款后操作节点里面，不要问为什么，需求就是这样
            loanedResultService.addLoanRecode(loan,lastUpdateTime);
            //dao().insert(seal);
            this.syncToChannel(loan.getChannelId());
            NutMap result = this.initLoanRecord(loanId,subjectId,loanSubjectAccountId,loanTime,user);
            if(null != result && null != result.get("loanRecordList")){
                dao().insert(result.get("loanRecordList"));
            }
        });
        return new NutMap().setv("ok",true);
    }


    /**
     *
     * @param loanAmount 贷款本金
     * @param outstanding 剩余本金
     * @param dueAmount 逾期本金
     * @param dueTotal 逾期本息
     * @param loanFeeTemp
     * @return
     */
    private BigDecimal calcByChargeType(BigDecimal loanAmount,BigDecimal outstanding,BigDecimal dueAmount,BigDecimal dueTotal, LoanFeeTemp loanFeeTemp) {
        if(loanFeeTemp == null){
            return BigDecimal.ZERO;
        }
        BigDecimal feeAmount;
        BigDecimal feeRate = loanFeeTemp.getFeeRate()==null?BigDecimal.ZERO:loanFeeTemp.getFeeRate();
        switch (loanFeeTemp.getChargeType()){
            case LOAN_AMOUNT_RATE:
                feeAmount = (loanAmount==null?BigDecimal.ZERO:loanAmount).multiply(feeRate).divide(new BigDecimal(100));
                break;
            case REMAIN_PRINCIPAL_RATE:
                feeAmount = (outstanding==null?BigDecimal.ZERO:outstanding).multiply(feeRate).divide(new BigDecimal(100));
                break;
            case OVERDUE_PRINCIPAL_RATE:
                feeAmount = (dueAmount==null?BigDecimal.ZERO:dueAmount).multiply(feeRate).divide(new BigDecimal(100));
                break;
            case OVERDUE_REPAYMENT_RATE:
                feeAmount = (dueTotal==null?BigDecimal.ZERO:dueTotal).multiply(feeRate).divide(new BigDecimal(100));
                break;
            case FIXED_AMOUNT:
                feeAmount = loanFeeTemp.getFeeAmount()==null?BigDecimal.ZERO:loanFeeTemp.getFeeAmount();
                break;
            case LOAN_REQUEST_INPUT:
                feeAmount = loanFeeTemp.getFeeAmount()==null?BigDecimal.ZERO:loanFeeTemp.getFeeAmount();
                break;
            default:
                feeAmount = BigDecimal.ZERO;
                break;
        }
        BigDecimal minFeeAmount = loanFeeTemp.getMinFeeAmount();
        BigDecimal maxFeeAmount = loanFeeTemp.getMaxFeeAmount();
        if(feeAmount.compareTo(minFeeAmount)<=0){
            feeAmount = minFeeAmount;
        }
        if(feeAmount.compareTo(maxFeeAmount)>=0){
            feeAmount = maxFeeAmount;
        }
        return feeAmount;
    }

    /**
     * 放款费用
     * @param loanId
     * @param user
     * @return
     */
    public List<LoanFee> queryLoanNodeFeeByLoanId(String loanId,User user){
        List<LoanFee> loanFees = this.dao().query(LoanFee.class,Cnd.where("loanId","=",loanId).and("chargeNode","=",FeeChargeNode.LOAN_NODE.name()).orderBy("feeType","asc"));
        if(loanFees!=null&&loanFees.size()>0){
            return loanFees;
        }
        Date now = new Date();
        Loan loan = this.dao().fetch(Loan.class,loanId);
        BigDecimal loanAmount = loan.getAmount();
        //放款时收取，收费频率是一次性
        List<LoanFeeTemp> feeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId,FeeChargeNode.LOAN_NODE);
        this.filterLoanFeeTemp(feeTempList,loan);
        List<LoanFee> loanFeeList = new ArrayList<>();
        feeTempList.stream().forEach(l->{
            loanFeeList.add(this.getLoanFee(loanId,l.getId(),l.getFeeName(),now,LoanRepayStatus.LOANED,this.calcByChargeType(loanAmount,loanAmount,BigDecimal.ZERO,BigDecimal.ZERO,l),FeeChargeNode.LOAN_NODE,l.getFeeType(),user));
        });
        return loanFeeList;
    }

    public void filterLoanFeeTemp(List<LoanFeeTemp> loanFeeTempList,Loan loan){
        Iterator<LoanFeeTemp> iterator = loanFeeTempList.iterator();
        while(iterator.hasNext()){
            LoanFeeTemp l = iterator.next();
            if(l.getRepayMethod()!=null && loan.getRepayMethod()!=null &&!LoanRepayMethod.ALL.equals(l.getRepayMethod())&&!l.getRepayMethod().equals(loan.getRepayMethod())){
                iterator.remove();
            }
        }
    }

    private LoanFee getLoanFee(String loanId,String feeId,String feeName,Date dueDate,LoanRepayStatus status,BigDecimal feeAmount,FeeChargeNode chargeNode,FeeType feeType,User user){
        LoanFee loanFee = new LoanFee();
        loanFee.setLoanId(loanId);
        loanFee.setFeeId(feeId);
        loanFee.setFeeName(feeName);
        loanFee.setDueDate(dueDate);
        loanFee.setStatus(status);
        loanFee.setFeeAmount(feeAmount.setScale(2, RoundingMode.HALF_EVEN));
        loanFee.setChargeNode(chargeNode);
        loanFee.setFeeType(feeType);
        loanFee.setCreateBy(user.getLogin());
        loanFee.setCreateTime(new Date());
        loanFee.setUpdateBy(user.getLogin());
        loanFee.setUpdateTime(new Date());
        return loanFee;
    }

    public Loan fetchLoanById(String loanId) {
        return this.dao().fetch(Loan.class,loanId);
    }

    public List<LoanRepay> queryLoanRepayByLoanId(String loanId){
        return this.dao().query(LoanRepay.class,Cnd.where("loanId","=",loanId).orderBy("period","asc"));
    }

    public List<LoanRecord> queryLoanRecordByLoanId(String loanId){
        return this.dao().query(LoanRecord.class,Cnd.where("loanId","=",loanId).orderBy("position","asc"));
    }


    @Aop(TransAop.READ_COMMITTED)
    public boolean preRepayment(String loanId)throws Exception{
        List<LoanRepay> canprelist  = queryCanPreRepayList(loanId);
        for(LoanRepay loanRepay : canprelist){
            NutMap nutMap = fullRepay(loanRepay.getId());
            LoanRepay fullRepay =  (LoanRepay)nutMap.get("loanRepay");
            List<LoanFee> loanFees  = (List<LoanFee>)nutMap.get("loanFeeList");
            String repayFeeInfotemp = "{\"feeType\":\"${feeType}\",\"repayFeeAmount\":\"${feeAmount}\"}";
            String repayFeeInfo = "[";
            for(LoanFee loanFee : loanFees){
                repayFeeInfo += (repayFeeInfotemp.replace("${feeType}",loanFee.getFeeType().name()).replace("${feeAmount}",loanFee.getFeeAmount().toString())+",");
            }
            if(repayFeeInfo.length()>1){
                repayFeeInfo = repayFeeInfo.substring(0,repayFeeInfo.length()-1);
            }
            repayFeeInfo +="]";
            //保存，如果都为0的话，不保存
            if(DecimalFormatUtils.isNotEmpty(fullRepay.getAmount())||DecimalFormatUtils.isNotEmpty(fullRepay.getInterest())||DecimalFormatUtils.isNotEmpty(fullRepay.getFeeAmount()))
                insertRepayRecord(loanRepay.getId(),new Date(),fullRepay.getAmount(),fullRepay.getInterest(),repayFeeInfo,"",ShiroSession.getLoginUser());
            //保存之前把状态都改为提前还款
            setAheadClearedStatus(loanRepay);
            //确认还请
            clearLoanRepayHandler(loanId,loanRepay.getId(),"提前还款处理");
        }
        return true;
    }

    private void setAheadClearedStatus(LoanRepay loanRepay){
        loanRepay.setStatus(LoanRepayStatus.AHEAD_CLEARED);
        dao().update(loanRepay,"^(status)$");
    }

    public void clearLoanRepayHandler(String loanId,String repayId,String remark){
        if (productInfoTmplService.isBill(loanId)) {
            boolean flag = false;
            flag = billLoanService.clearLoanRepay(repayId);
            if(flag){
                Loan loan = loanService.fetchById(loanId);
                Product product = productService.fetchEnableProductById(loan.getProductId());
                if(null != product && product.getName().equals("商业承兑汇票")) {
                    billLoanService.changeAmountForClear(repayId,loan);
                }
            }
        }
        clearLoanRepay(repayId,remark);
    }

    public  List<LoanRepay> queryCanPreRepayList(String loanId){
        List<LoanRepay> list = queryLoanRepayByLoanId(loanId);
        List<LoanRepay> canPreRepayList = new ArrayList<LoanRepay>();
        for(LoanRepay loanRepay : list){
            if(LoanRepayStatus.LOANED.equals(loanRepay.getStatus())){
                canPreRepayList.add(loanRepay);
            }
        }
        return canPreRepayList;
    }

    @Aop(TransAop.READ_COMMITTED)
    public Object insertRepayRecord(String repayId,
                              Date repayDate,
                              BigDecimal repayAmount,
                              BigDecimal repayInterest,
                              String repayFeeInfo,
                              String remark,
                              User user) {
        LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",repayId));
        if(loanRepay==null){
            return null;
        }
        BigDecimal repayTotalAmount = DecimalUtils.sum(repayAmount,repayInterest);
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayId(repayId);
        loanRepayRecord.setRepayAmount(repayAmount);
        loanRepayRecord.setRepayInterest(repayInterest);
        loanRepayRecord.setRepayTotalAmount(repayTotalAmount);
        loanRepayRecord.setRepayDate(repayDate);
        loanRepayRecord.setCreateBy(user.getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepayRecord.setUpdateTime(new Date());
        loanRepayRecord.setUpdateBy(user.getName());
        loanRepayRecord = this.dao().insert(loanRepayRecord);

        //List<LoanFee> feeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId).and("period","=",loanRepay.getPeriod()));
        List<LoanFee> feeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId));
        Loan loan = this.fetchLoanById(loanRepay.getLoanId());
        Map<FeeType,LoanFee> type2FeeMap = feeList.stream().collect(Collectors.toMap(l->l.getFeeType(),l->l));
        List<LoanFee> loanFeeList = Json.fromJsonAsList(LoanFee.class,repayFeeInfo);
        List<LoanFeeRecord> loanFeeRecordList = new ArrayList<>();
        for(LoanFee l:loanFeeList){
            LoanFeeRecord loanFeeRecord = new LoanFeeRecord();
            LoanFee loanFee = type2FeeMap.get(l.getFeeType());
            loanFeeRecord.setRepayId(repayId);
            loanFeeRecord.setRepayRecordId(loanRepayRecord.getId());
            loanFeeRecord.setLoanFeeId(loanFee.getId());
            loanFeeRecord.setRepayFeeAmount(l.getRepayFeeAmount());
            this.handleOverdueOrPrePayFee(loan,loanRepay,loanFee,repayDate);
            loanFeeRecord.setCreateBy(user.getName());
            loanFeeRecord.setCreateTime(new Date());
            loanFeeRecord.setUpdateBy(user.getName());
            loanFeeRecord.setUpdateTime(new Date());
            loanFeeRecordList.add(loanFeeRecord);
        }
        loanRepay.setRemark(remark);
        loanRepay.setUpdateBy(user.getName());
        loanRepay.setUpdateTime(new Date());
        dao().update(loanRepay);
        this.dao().insert(loanFeeRecordList);
        this.dao().update(feeList);
        this.dao().update(loan);
        this.syncToLoanRepay(repayId,false);
        return loanRepayRecord;
    }

    /**
     * 计算逾期或提前还款
     */
    private void handleOverdueOrPrePayFee(Loan loan,LoanRepay loanRepay,LoanFee loanFee, Date repayDate) {
        if(loanFee==null){
            return;
        }
        FeeType feeType = loanFee.getFeeType();
        if(FeeType.OVERDUE_FEE.equals(feeType)){
            List<LoanFeeTemp> loanFeeTempList = this.dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanRepay.getLoanId()).and("feeType","=",FeeType.OVERDUE_FEE));
            this.filterLoanFeeTemp(loanFeeTempList,loan);
            Map<String,LoanFeeTemp> id2FeeTempMap = loanFeeTempList.stream().collect(Collectors.toMap(l->l.getId(),l->l));
            //逾期罚息
            loanFee.setFeeAmount(DecimalUtils.sum(loanFee.getHistoryFeeAmount(),this.calcOverdueFee(loanRepay,loan,id2FeeTempMap.get(loanFee.getFeeId()),new Date())));
        }
//        else if(FeeType.PREPAYMENT_FEE.equals(feeType)){
//            //提前结清罚息
//            loanFee.setFeeAmount(this.calcPrePayFee(loanRepay,loan,id2FeeTempMap.get(loanFee.getFeeId()),repayDate));
//        }
    }

    /**
     * 足额还款，计算该期应还金额
     * @param repayId
     * @return
     */
    public NutMap fullRepay(String repayId) {
        LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",repayId));
        if(loanRepay==null){
            return null;
        }

        List<LoanFee> loanFeeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId));
        loanFeeList.stream().forEach(l->{
            l.setFeeAmount(DecimalUtils.sub(l.getFeeAmount(),l.getRepayFeeAmount()));
        });
        loanRepay.setAmount(DecimalUtils.sub(loanRepay.getAmount(),loanRepay.getRepayAmount()));
        loanRepay.setInterest(DecimalUtils.sub(loanRepay.getInterest(),loanRepay.getRepayInterest()));
        loanRepay.setTotalAmount(DecimalUtils.sub(loanRepay.getTotalAmount(),loanRepay.getRepayTotalAmount()));
        return new NutMap().setv("loanRepay",loanRepay).setv("loanFeeList",loanFeeList);
    }

    /**
     * 查询收款记录
     * @param repayId
     * @return
     */
    public NutMap queryRepayRecord(String repayId) {
        //查询本期还款计划
        LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",repayId));
        String loanId = null;
        if(loanRepay==null){
            OldLoanRepay oldLoanRepay = dao().fetch(OldLoanRepay.class,Cnd.where("repayId","=",repayId));
            if (oldLoanRepay == null) {
                NutMap resultMap = new NutMap();
                return resultMap.setv("loanRepayRecordList", new ArrayList<>());
            }else{
                loanId = oldLoanRepay.getLoanId();
            }
        }else{
            loanId  = loanRepay.getLoanId();
        }
        List<LoanRepayRecord> loanRepayRecordList = this.dao().query(LoanRepayRecord.class,Cnd.where("repayId","=",repayId).orderBy("createTime","asc"));
        List<LoanFee> loanFeeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId));
        loanFeeList.stream().forEach(l->{
            l.setFeeAmount(DecimalUtils.sub(l.getFeeAmount(),l.getRepayFeeAmount()));
        });
        Map<String,List<Map>> repayRecord2FeeRecordMap = this.queryRepayRecordId2FeeRecordMap(repayId,loanFeeList);
        List<Map> recordList = new ArrayList<>();
        int period=1;
        Loan loan = dao().fetch(Loan.class,loanId);
        LoanBorrower loanBorrower  =  dao().fetch(LoanBorrower.class,Cnd.where("id","=",loan.getMasterBorrowerId()));
        for(LoanRepayRecord record:loanRepayRecordList){
            Map<String,Object> tempMap = new LinkedHashMap<>();
            tempMap.put("period",period++);
            tempMap.put("repayId",repayId);
            tempMap.put("id",record.getId());
            tempMap.put("repayDate",record.getRepayDate());
            tempMap.put("repayAmount",record.getRepayAmount());
            tempMap.put("repayInterest",record.getRepayInterest());
            tempMap.put("repayments","系统".equals(record.getCreateBy())?"系统":loanBorrower.getName());
            if(repayRecord2FeeRecordMap.containsKey(record.getId())){
                tempMap.put("feeList",repayRecord2FeeRecordMap.get(record.getId()));
                fullFeelist((List<Map>)tempMap.get("feeList"),loanFeeList);
            }else{
                List<Map<String,Object>> tempMapList = new ArrayList<>();
                loanFeeList.stream().forEach(l->{
                    Map map = new HashedMap();
                    map.put("repayFeeAmount","0.00");
                    map.put("feeType",l.getFeeType());
                    tempMapList.add(map);
                });
                tempMap.put("feeList",tempMapList);
            }
            BigDecimal repayFeeAmount = this.getRepayFeeAmount(repayRecord2FeeRecordMap.get(record.getId()));
            tempMap.put("repayFeeAmount",repayFeeAmount);
            tempMap.put("repayTotalAmount",DecimalUtils.sumArr((BigDecimal) tempMap.get("repayAmount"),(BigDecimal)tempMap.get("repayInterest"),
                    (BigDecimal)tempMap.get("repayFeeAmount")));
            recordList.add(tempMap);
        }
        if (loanRepay != null) {
            loanRepay.setAmount(DecimalUtils.sub(loanRepay.getAmount(), loanRepay.getRepayAmount()));
            loanRepay.setInterest(DecimalUtils.sub(loanRepay.getInterest(), loanRepay.getRepayInterest()));
            loanRepay.setTotalAmount(DecimalUtils.sub(loanRepay.getTotalAmount(), loanRepay.getRepayTotalAmount()));
        }
        NutMap resultMap = new NutMap();
        resultMap.setv("loanRepay",loanRepay);
        resultMap.setv("loanRepayRecordList",recordList);
        resultMap.setv("loanFeeList",loanFeeList);
        return resultMap;
    }

    private void fullFeelist(List<Map> feelist,List<LoanFee> loanFees){
        if(feelist.size()<loanFees.size()){
            for(int i=0;i<loanFees.size();i++){
                while(feelist.size()<loanFees.size()){
                    if(feelist.size()==i){
                        Map map = new HashedMap();
                        map.put("repayFeeAmount","0.00");
                        map.put("feeType",loanFees.get(i).getFeeType());
                        feelist.add(i,map);
                    }else{
                        for(int j=i;j<feelist.size();j++){
                            String feeType  = (String)feelist.get(j).get("feeType");
                            if(!loanFees.get(i).getFeeType().name().equals( feeType)){
                                Map map = new HashedMap();
                                map.put("repayFeeAmount","0.00");
                                map.put("feeType",loanFees.get(i).getFeeType());
                                feelist.add(i,map);
                                break;
                            }
                        }
                    }

                }
            }
        }

    }

    /**
     * 提前结清费用计算
     * @param loanRepay
     * @param loan
     * @param loanFeeTemp
     * @param repayDate
     * @return
     */
    private BigDecimal calcPrePayFee(LoanRepay loanRepay,Loan loan,LoanFeeTemp loanFeeTemp,Date repayDate){
        Date dueDate = loanRepay.getDueDate();
        int dueDays = TimeUtils.daysBetween(dueDate,repayDate);
        if(dueDays>=0){
            return BigDecimal.ZERO;
        }
        return this.calcByChargeType(loan.getActualAmount(),loanRepay.getOutstanding(),loanRepay.getAmount(),DecimalUtils.sum(loanRepay.getAmount(),loanRepay.getInterest()),loanFeeTemp);
    }

    /**
     * 逾期费用计算
     * @param loanRepay
     */
    private BigDecimal calcOverdueFee(LoanRepay loanRepay,Loan loan,LoanFeeTemp loanFeeTemp,Date repayDate) {
        Date dueDate = loanRepay.getDueDate();
        int dueDays = TimeUtils.daysBetween(dueDate,repayDate);
        Product product = this.dao().fetch(Product.class,Cnd.where("id","=",loan.getProductId()));
        if(product!=null){
            dueDays = dueDays-product.getOverdueDays();
        }
        if(dueDays<=0){
            return BigDecimal.ZERO;
        }
        BigDecimal total = DecimalUtils.sum(loanRepay.getAmount(),loanRepay.getInterest());//应还本息
        BigDecimal dueFee = this.calcByChargeType(loan.getAmount(),loanRepay.getOutstanding(),loanRepay.getAmount(),total,loanFeeTemp);
        loan.setLoanStatus(LoanStatus.OVERDUE);
        loanRepay.setStatus(LoanRepayStatus.OVERDUE);
        return dueFee.multiply(new BigDecimal(dueDays));
    }

    private BigDecimal getRepayFeeAmount(List<Map> listMap) {
        if(listMap==null||listMap.isEmpty()){
            return BigDecimal.ZERO;
        }
        BigDecimal result=BigDecimal.ZERO;
        for(Map map:listMap){
            BigDecimal repayFeeAmount = map.get("repayFeeAmount")== null?BigDecimal.ZERO:new BigDecimal(map.get("repayFeeAmount").toString());
            result = result.add(repayFeeAmount);
        }
        return result;
    }

    /**
     * 查询费用记录表
     * @param repayId
     * @return
     */
    public  Map<String,List<Map>> queryRepayRecordId2FeeRecordMap(String repayId,List<LoanFee> loanFeeList){
        String sqlStr = "select " +
                "rr.repayId as repayId," +
                "rr.id as repayRecordId," +
                "lf.id as loanFeeId," +
                "lf.feeType as feeType," +
                "fr.repayFeeAmount as repayFeeAmount " +
                "from sl_loan_fee_record fr," +
                "sl_loan_repay_record rr," +
                "sl_loan_fee lf " +
                "where fr.repayRecordId=rr.id " +
                "and fr.loanFeeId=lf.id " +
                "and fr.repayId=rr.repayId " +
                "and rr.repayId=lf.repayId " +
                "and rr.repayId=@repayId";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("repayId",repayId);
        sql.setCallback((connection, rs, sql1) -> {
            List<Map<String,String>> list = new ArrayList<>();
            while (rs.next()) {
                Map tmp=new LinkedHashMap();
                tmp.put("repayId",rs.getString("repayId"));
                tmp.put("repayRecordId",rs.getString("repayRecordId"));
                tmp.put("loanFeeId",rs.getString("loanFeeId"));
                tmp.put("feeType",rs.getString("feeType"));
                tmp.put("repayFeeAmount",rs.getString("repayFeeAmount"));
                list.add(tmp);
            }
            return list;
        });
        this.dao().execute(sql);
        List<Map> feeListMap = sql.getList(Map.class);
        Map<String,List<Map>> feeType2Map = feeListMap.stream().collect(Collectors.groupingBy(l->(String)l.get("feeType")));
        //排序，保证取的数据和列头同步
        List<Map> feeList = new ArrayList<>();
        loanFeeList.stream().filter(loanFee -> feeType2Map.containsKey(loanFee.getFeeType().name())).forEach(loanFee -> {
            feeList.addAll(feeType2Map.get(loanFee.getFeeType().name()));
        });

        Map<String,List<Map>> resultMap = new LinkedHashMap<>();
        for(Map map:feeList){
            String repayRecordId = (String) map.get("repayRecordId");
            if(resultMap.containsKey(repayRecordId)){
                resultMap.get(repayRecordId).add(map);
            }else{
                List<Map> listMap = new ArrayList<>();
                listMap.add(map);
                resultMap.put(repayRecordId,listMap);
            }
        }
        return resultMap;
    }


    public boolean deleteByRecordId(String id) {
        LoanRepayRecord record = this.dao().fetch(LoanRepayRecord.class,Cnd.where("id","=",id));
        this.dao().clear(LoanRepayRecord.class,Cnd.where("id","=",id));
        this.dao().clear(LoanFeeRecord.class,Cnd.where("repayRecordId","=",id));
        if(record!=null&&StringUtils.isNotEmpty(record.getRepayId())){
            this.syncToLoanRepay(record.getRepayId(),false);
        }
        return true;
    }


    public List<LoanFee> calcFee(String repayId,Date repayDate) {
        LoanRepay loanRepay = this.dao().fetch(LoanRepay.class,Cnd.where("id","=",repayId));
        if(loanRepay==null || repayDate ==null){
            return null;
        }
        List<LoanFee> loanFeeList = this.dao().query(LoanFee.class,Cnd.where("repayId","=",repayId));
        Map<String,LoanFee> id2LoanFeeMap = loanFeeList.stream().collect(Collectors.toMap(l->l.getId(),l->l));
        List<LoanFeeRecord> loanFeeRecordList = this.dao().query(LoanFeeRecord.class,Cnd.where("repayId","=",repayId));
        loanFeeRecordList.stream().forEach(l->{
           if(id2LoanFeeMap.containsKey(l.getLoanFeeId())){
               LoanFee loanFee = id2LoanFeeMap.get(l.getLoanFeeId());
               if(!FeeType.OVERDUE_FEE.equals(loanFee.getFeeType()) && !FeeType.PREPAYMENT_FEE.equals(loanFee.getFeeType())){
                   loanFee.setFeeAmount(DecimalUtils.sub(loanFee.getFeeAmount(),l.getRepayFeeAmount()));
               }
           }
        });
        Loan loan = this.fetchLoanById(loanRepay.getLoanId());
        List<LoanFeeTemp> loanFeeTempList = this.dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loan.getId()));
        Map<String,LoanFeeTemp> id2LoanFeeTemp = loanFeeTempList.stream().collect(Collectors.toMap(l->l.getId(),l->l));
        for(LoanFee loanFee : loanFeeList){
            if(FeeType.OVERDUE_FEE.equals(loanFee.getFeeType())){
                loanFee.setFeeAmount(this.calcOverdueFee(loanRepay,loan,id2LoanFeeTemp.get(loanFee.getFeeId()),repayDate));
            }else if(FeeType.PREPAYMENT_FEE.equals(loanFee.getFeeType())){
                loanFee.setFeeAmount(this.calcPrePayFee(loanRepay,loan,id2LoanFeeTemp.get(loanFee.getFeeId()),repayDate));
            }
        }
        return loanFeeList;
    }

    //定时任务跑逾期标的
    @Aop(TransAop.READ_COMMITTED)
    public void handleOverdue(){
        logger.info("handle overdue loan begin.");
        List<Loan> loanList = this.dao().query(Loan.class, Cnd.where("status","=", PublicStatus.ABLE).and("loanStatus","in",new LoanStatus[]{LoanStatus.LOANED,LoanStatus.OVERDUE}));

        List<Loan> billLoanList = new ArrayList<>();
        List<Loan> notBillLoanList = new ArrayList<>();
        Date now = new Date();
        loanList.stream().forEach(l->{
            if(productInfoTmplService.isBill(l.getId())){
                billLoanList.add(l);
            }else{
                notBillLoanList.add(l);
            }
        });

        //票据
        billLoanList.stream().forEach(l->{
            List<BillLoanRepay> billLoanRepayList = billLoanService.queryBillLoanRepay(l.getId());
            billLoanRepayList.stream().forEach(b->{
                LoanRepay loanRepay = b.getLoanRepay();
                if(loanRepay!=null){
                    LoanRepayStatus loanRepayStatus = loanRepay.getStatus();
                    if(LoanRepayStatus.LOANED.equals(loanRepayStatus) || LoanRepayStatus.OVERDUE.equals(loanRepayStatus)){
                        int dueDays = TimeUtils.daysBetween(loanRepay.getDueDate(),now);
                        dueDays = dueDays - b.getOverdueDays();
                        if(dueDays>0){
                            logger.info("target loanId={},repayId={} is overdue,will be update status.",l.getId(),loanRepay.getId());
                            if(!LoanRepayStatus.OVERDUE.equals(loanRepay.getStatus())){
                                loanRepay.setStatus(LoanRepayStatus.OVERDUE);
                            }
                            loanRepay.setOverdueDays(dueDays);
                            this.dao().update(loanRepay);

                            if(!LoanStatus.OVERDUE.equals(l.getLoanStatus())){
                                l.setLoanStatus(LoanStatus.OVERDUE);
                                this.dao().update(l);
                            }
                        }
                    }
                }
            });
        });


        notBillLoanList.stream().forEach(l->{
            Product product = this.dao().fetch(Product.class,Cnd.where("id","=",l.getProductId()));
            List<LoanRepay> loanRepayList = this.dao().query(LoanRepay.class,Cnd.where("loanId","=",l.getId()).and("status","in",new LoanRepayStatus[]{LoanRepayStatus.LOANED,LoanRepayStatus.OVERDUE}));
            loanRepayList.stream().forEach(r->{
                int dueDays = TimeUtils.daysBetween(r.getDueDate(),now);
                if(product!=null){
                    dueDays = dueDays-product.getOverdueDays();
                }
                if(dueDays>0){
                    logger.info("target loanId={},repayId={} is overdue,will be update status.",l.getId(),r.getId());
                    if(!LoanRepayStatus.OVERDUE.equals(r.getStatus())){
                        r.setStatus(LoanRepayStatus.OVERDUE);

                    }
                    r.setOverdueDays(dueDays);
                    dao().update(r);
                    if(!LoanStatus.OVERDUE.equals(l.getLoanStatus())){
                        l.setLoanStatus(LoanStatus.OVERDUE);
                        this.dao().update(l);
                    }
                    LoanFee loanFee = this.dao().fetch(LoanFee.class,Cnd.where("loanId","=",l.getId()).and("repayId","=",r.getId()).and("feeType","=", FeeType.OVERDUE_FEE));
                    List<LoanFeeTemp> loanFeeTempList=null;
                    if(loanFee!=null){
                        //判断是否有展期
                        List<LoanExtension> loanExtensionList=this.dao().query(LoanExtension.class, Cnd.where("loanId","=",l.getId()).orderBy("position","desc"));
                        if(loanExtensionList.size()>0){
                            LoanExtension loanExtension=  loanExtensionList.get(0);
                            loanFeeTempList = this.dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",l.getId()).and("feeType","=",FeeType.OVERDUE_FEE).and("repayMethod","=",loanExtension.getRepayMethod()));
                        }else {
                            loanFeeTempList = this.dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",l.getId()).and("feeType","=",FeeType.OVERDUE_FEE));
                            this.filterLoanFeeTemp(loanFeeTempList,l);
                        }
                        LoanFeeTemp loanFeeTemp = null;
                        if(loanFeeTempList!=null&&loanFeeTempList.size()>0){
                            loanFeeTemp = loanFeeTempList.get(0);
                        }
                        BigDecimal total = DecimalUtils.sum(r.getAmount(),r.getInterest());//应还本息
                        BigDecimal dueFee = this.calcByChargeType(l.getAmount(),r.getOutstanding(),r.getAmount(),total,loanFeeTemp);
                        dueFee = dueFee.multiply(new BigDecimal(dueDays));
                        logger.info("update loanFee begin,overdue fee={}.",dueFee);
                        loanFee.setFeeAmount(DecimalUtils.sum(dueFee,loanFee.getHistoryFeeAmount()));
                        this.dao().update(loanFee);
                        this.syncToLoanRepay(r.getId(),false);
                        logger.info("update loanFee end,overdue fee={}.",dueFee);

                    }
                }
            });
        });
        logger.info("handle overdue loan end.");
    }

    public DataTables queryLoanRepayList(DataTableParam param) {
        String borrower = "";
        String repayTime = "";
        String productType = "";
        String product = "";
        LoanRepayStatus status = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            repayTime = keys.get("repayTime");
            borrower = keys.get("borrower");
            if(StringUtils.isNotEmpty(keys.get("status"))){
                status = status.valueOf(keys.get("status"));
            }
            productType = keys.get("productType");
            product = keys.get("product");
        }
        LoanRepayStatus[] statusArr = {LoanRepayStatus.LOANED, LoanRepayStatus.OVERDUE};
        LoanRepayStatus[] singleStatus = {status};
        LoanRepayStatus[] loanRepayStatus = status == null ? statusArr : singleStatus;
        Date beginDate = TimeUtils.getQueryStartDateTime(repayTime);
        Date endDate = TimeUtils.getQueryEndDateTime(repayTime);
        String sqlStr = "select IFNULL(l.id,'--') AS 'id',"+
                "IFNULL(l.`code`,'--') AS 'code',"+
                "IFNULL(pt.name,'--') AS 'productTypeName',"+
                "IFNULL(sp.name,'--') AS 'productName'," +
                "IFNULL(u.name,'--') AS 'saleName',"+
                "lb.name AS 'borrserName',"+
                "IFNULL(l.submitTime,'--') AS 'submitTime',"+
                "IFNULL(l.amount,'--') AS 'amount',"+
                "IFNULL(l.actualAmount,'--') AS 'actualAmount',"+
                "IFNULL(l.loanTime,'--') AS 'loanTime',"+
                "IFNULL(o.businessLine,'--') AS 'businessLine',"+
                "IFNULL(l.term,'--') AS 'term',"+
                "IFNULL(l.approveStatusDesc,'--') AS 'approveStatus',"+
                "IFNULL(o.name,'--') AS 'organizeName',"+
                "IFNULL(l.termType,'--') AS 'termType',"+
                "IFNULL(l.loanStatus,'--') AS 'loanStatus',"+
                "IFNULL(a.name,'--') AS 'agencyName',"+
                "IFNULL(t.productTempType,'--') AS 'productTempType',"+
                "IFNULL(lr.period,'--') AS 'period',"+
                "IF(blr.actualDueDate IS NULL , lr.dueDate,blr.actualDueDate) AS 'dueDate', " +
                "IFNULL(lr.totalAmount,'--') AS 'totalAmount',"+
                "IFNULL(lr.repayTotalAmount,'--') AS 'repayTotalAmount'," +
                "IFNULL(lr.status,'--') AS 'repayStatus'," +
                "IFNULL(lr.amount,'--') AS 'repayAmount', "+
                "IFNULL(lr.overdueDays,'--') AS 'overdueDays' "+
                "from sl_loan_repay lr "+
                "left join sl_bill_loan_repay blr on lr.id = blr.repayId " +
                "left join sl_loan l on lr.loanId=l.id "+
                "left join sl_business_user u on l.saleId=u.id " +
                "left join sl_product_type pt on l.productTypeId=pt.id "+
                "left join sl_business_organize o on u.organizeId=o.id "+
                "left join sl_business_agency a on o.agencyId = a.id "+
                "left join sl_product sp on l.productId=sp.id "+
                "left join sl_product_info_tmpl t on sp.infoTmpId=t.id "+
                "left join (select name,loanId from sl_loan_borrower where master=1) lb on lb.loanId=l.id "+
                "where lr.status in (@loanRepayStatus) "+
                "and l.loanStatus in ('LOANED','OVERDUE') "+
                "and l.status='ABLE' ";


        String countSqlStr =" SELECT  COUNT(DISTINCT lr.id) AS 'number'  "+
                "from sl_loan_repay lr "+
                "left join sl_bill_loan_repay blr on lr.id = blr.repayId " +
                "left join sl_loan l on lr.loanId=l.id "+
                "left join sl_business_user u on l.saleId=u.id " +
                "left join sl_product_type pt on l.productTypeId=pt.id "+
                "left join sl_business_organize o on u.organizeId=o.id "+
                "left join sl_business_agency a on o.agencyId = a.id "+
                "left join sl_product sp on l.productId=sp.id "+
                "left join sl_product_info_tmpl t on sp.infoTmpId=t.id "+
                "where lr.status in (@loanRepayStatus) "+
                "and l.loanStatus in ('LOANED','OVERDUE') "+
                "and l.status='ABLE' ";

        sqlStr += getQueryRepayDateSql(beginDate,endDate);
        countSqlStr += getQueryRepayDateSql(beginDate,endDate);

        if(StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
            countSqlStr+=" AND  l.id in (select loanId from sl_loan_borrower b where b.name like @borrower) ";
        }
        if (StringUtils.isNotEmpty(productType)){
            sqlStr+=" AND l.productTypeId=@productType ";
            countSqlStr += " AND  l.productTypeId=@productType ";
        }
        if (StringUtils.isNotEmpty(product)) {
            sqlStr += " AND  l.productId=@productId ";
            countSqlStr += " AND  l.productId=@productId ";
        }
        String[] loanRepayStatusArr = new String[loanRepayStatus.length];
        int i=0;
        for(LoanRepayStatus newStatus:loanRepayStatus){
            loanRepayStatusArr[i++]=newStatus.name();
        }
        sqlStr+=" order by dueDate asc limit @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("loanRepayStatus",loanRepayStatusArr);
        sql.setParam("borrower",'%'+borrower+'%');
        sql.setParam("beginDate",beginDate);
        sql.setParam("endDate",endDate);
        sql.setParam("start",param.getStart());
        sql.setParam("length",param.getLength());
        sql.setParam("productType", productType);
        sql.setParam("productId", product);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanRepayStatus",loanRepayStatusArr);
        countSql.setParam("borrower",'%'+borrower+'%');
        countSql.setParam("beginDate",beginDate);
        countSql.setParam("endDate",endDate);
        countSql.setParam("typeId",productType);
        countSql.setParam("productType", productType);
        countSql.setParam("productId", product);
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        if(count<1){
            return new DataTables(param.getDraw(),count,count,list);
        }
        String periodSqlStr = "select lr.loanId AS loanId,count(lr.id) AS total from sl_loan_repay lr group by lr.loanId";
        Sql periodSql = Sqls.create(periodSqlStr);

        periodSql.setCallback(Sqls.callback.maps());
        dao().execute(periodSql);
        List<Map> periodList = periodSql.getList(Map.class);
        Map<String,String> loanId2RepayTotalMap = new HashMap();
        periodList.stream().forEach(m->{
            loanId2RepayTotalMap.put((String)m.get("loanId"),(m.get("total")).toString());
        });
        list.stream().forEach(m->{
            String loanId = (String)m.get("id");
            String period = (String)m.get("period");
            BigDecimal totalAmount = new BigDecimal("--".equals(m.get("totalAmount"))?"0":(String)m.get("totalAmount"));
            BigDecimal repayTotalAmount = new BigDecimal("--".equals(m.get("repayTotalAmount"))?"0":(String)m.get("repayTotalAmount"));
            String productTempType = (String)m.get("productTempType");
            String repayAmount = (String)m.get("repayAmount");
            //应还金额
            m.put("repayAmount",ProductTempType.isBill(ProductTempType.valueOf(productTempType))?repayAmount:DecimalUtils.sub(totalAmount,repayTotalAmount));

            //还款期数
            m.put("periodStr",ProductTempType.isBill(ProductTempType.valueOf(productTempType))?"--":period+"/"+loanId2RepayTotalMap.get(loanId));
        });
        return new DataTables(param.getDraw(),count,count,list);
    }

    private String getQueryRepayDateSql(Date beginDate, Date endDate) {
        if (null != beginDate && null != endDate){
            return " AND IF(blr.overdueDays IS NULL , lr.dueDate,blr.actualDueDate)>=@beginDate  AND IF(blr.overdueDays IS NULL , lr.dueDate, blr.actualDueDate)<=@endDate ";
        }
        return "";
    }

    //@Async
    public void saveHouseManage(Loan loan,User user){
        String loanId = loan.getId();
        Date now = new Date();
        //房抵贷生成房产管理
        ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());
        if(null != tmpl && ProductTempType.HONGBEN.equals(tmpl.getProductTempType())) {

            //房產
            Map data = new HashMap<>();
            List<LoanRepay> loanRepayList = dao().query(LoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("period", "desc"));
            List<OldLoanRepay> oldLoanRepayList = this.dao().query(OldLoanRepay.class, Cnd.where("loanId", "=", loanId).orderBy("position,period", "desc"));
            List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(loanId);
            if(CollectionUtils.isNotEmpty(houseInfoList)){
            for (HouseInfo h : houseInfoList) {
                HouseManage houseManage = new HouseManage();
                if(CollectionUtils.isNotEmpty(loanRepayList)){
                    houseManage.setDueDate(oldLoanRepayList.get(0).getDueDate());
                    houseManage.setExtensionDueDate(loanRepayList.get(0).getDueDate());
                }
                houseManage.setBusinessCode(loan.getCode());
                houseManage.setChannelId(loan.getChannelId());
                houseManage.setLoanId(loanId);
                houseManage.setUpdateTime(now);
                houseManage.setUpdateBy(user.getName());
                houseManage.setCreateTime(now);
                houseManage.setCreateBy(user.getName());
                houseManage.setPropertyRightStatus(PropertyRightStatus.WAITSECURED);

                //查找主借款人信息
                List<LoanBorrower> loanBorrowerList = loanBorrowerService.queryByLoanId(loan.getId());
                if (CollectionUtils.isNotEmpty(loanBorrowerList)) {
                    for (LoanBorrower lb : loanBorrowerList) {
                        if (lb.isMaster() == true) {
                            houseManage.setBorrower(lb.getName());
                        }
                    }
                }
                //业务来源
                BusinessUser businessUser = businessUserService.fetchById(loan.getSaleId());
                if (null != businessUser) {
                    BusinessOrganize businessOrganize = businessOrganizeService.fetchById(businessUser.getOrganizeId());
                    if (null != businessOrganize) {
                        String re = loanService.getBusinessSource(loan.getChannelId(), businessOrganize.getBusinessLine().toString(), businessOrganize.getCode(), loan.getSaleName());
                        houseManage.setSaleName(re);
                    }
                }
                houseManage.setSaleMan(loan.getSaleName());
                Product prd = productService.fetchEnableProductById(loan.getProductId());
                houseManage.setProductName(prd.getName());
                LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
                houseManage.setLoanSubject(loanSubject.getName());
                //渠道类型
                Channel channel = null;
                if (StringUtils.isNotEmpty(loan.getChannelId())) {
                    channel = dao().fetch(Channel.class, loan.getChannelId());
                }
                if (null != channel && ("1").equals(channel.getChannelType())) {
                    houseManage.setChannelType("1");
                } else {
                    houseManage.setChannelType("0");
                }
                houseManage.setLoanPrincipal(loan.getAmount());
                houseManage.setLoanTerm(TransformUtil.getTermType(loan.getTermType(), loan.getTerm()));
                houseManage.setLoanTime(loan.getLoanTime());
                houseManage.setTermType(loan.getTermType().toString());
                houseManage.setCode(h.getCode());
                houseManage.setAddress(h.getAddress());
                houseManage.setArea(h.getArea());
                houseManage.setPrice(h.getPrice());
                houseManage.setOwer(h.getOwer());
                Trans.exec((Atom) () -> {
                    dao().insert(houseManage);
                });
            }
            }

        }

    }


    public void saveHouseManageHistory(){
        ProductType productType = productTypeService.fetchByName("房抵贷");
        List<Loan> loanList = this.dao().query(Loan.class, Cnd.where("status","=", PublicStatus.ABLE).and("productTypeId","=",productType.getId()).and("loanStatus","in",new LoanStatus[]{LoanStatus.LOANED,LoanStatus.OVERDUE,LoanStatus.CLEARED}));
        for(Loan loan:loanList){
            String loanId = loan.getId();
            Date now = new Date();
            //房抵贷生成房产管理
            ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());
            if(null != tmpl && ProductTempType.HONGBEN.equals(tmpl.getProductTempType())){

                //房產
                Map data = new HashMap<>();
                List<HouseInfo> houseInfoList = new ArrayList<>();
                List<ProductInfoItem> productInfoItems =  productInfoItemService.queryByLoanId(loan.getId());
                if(CollectionUtils.isNotEmpty(productInfoItems)){
                    for (ProductInfoItem pit:productInfoItems){
                        if(("house").equals(pit.getKeyName())){
                            String houseVal = pit.getDataValue();
                            JSONArray jsonArray = JSONArray.fromObject(houseVal);
                            List<Map<String,String>> mapListJson = (List)jsonArray;
                            for (int i = 0; i < mapListJson.size(); i++) {
                                JSONArray jsonArray2 = JSONArray.fromObject(mapListJson.get(i));
                                //System.out.println("KEY:"+jsonArray2.get(0)+"  --> +");
                                List houseList = (List)jsonArray2;
                                for (int j = 0; j < houseList.size(); j++){
                                    Map<String,String> obj = (Map<String, String>) houseList.get(j);
                                    if(("house_code").equals(obj.get("keyName"))){
                                        data.put("house_code",obj.get("dataValue"));
                                    }else if(("address").equals(obj.get("keyName"))){
                                        data.put("address",obj.get("dataValue"));
                                    }else if(("user").equals(obj.get("keyName"))){
                                        data.put("user",obj.get("dataValue"));
                                    }else if(("area").equals(obj.get("keyName"))){
                                        data.put("area",obj.get("dataValue"));
                                    }else if(("price").equals(obj.get("keyName"))){
                                        data.put("price",obj.get("dataValue"));
                                    }
                                    //System.out.println("KEY:"+obj.get("keyName")+"  -->  Value:"+obj.get("dataValue")+" ");
                                }
                                if((null!= data && data.size()>0)){
                                    HouseInfo h= new HouseInfo();
                                    if(null != data.get("house_code") && StringUtils.isNotEmpty(data.get("house_code").toString())){
                                        h.setCode(data.get("house_code").toString());
                                    }
                                    if(null != data.get("address") && StringUtils.isNotEmpty(data.get("address").toString())){
                                        h.setAddress(data.get("address").toString());
                                    }

                                    if(null != data.get("user") && StringUtils.isNotEmpty(data.get("user").toString())){
                                        h.setOwer(data.get("user").toString());
                                    }

                                    if(null != data.get("area") && StringUtils.isNotEmpty(data.get("area").toString())){
                                        h.setArea(data.get("area").toString());
                                    }

                                    if(null != data.get("price") && StringUtils.isNotEmpty(data.get("price").toString())){
                                        h.setPrice(data.get("price").toString());
                                    }
                                    houseInfoList.add(h);
                                }
                            }
                        }
                    }
                }

                for (HouseInfo h :houseInfoList){
                    HouseManage houseManage = new HouseManage();
                    houseManage.setBusinessCode(loan.getCode());
                    houseManage.setChannelId(loan.getChannelId());
                    houseManage.setLoanId(loanId);
                    houseManage.setUpdateTime(now);
                    houseManage.setUpdateBy("历史数据");
                    houseManage.setCreateTime(now);
                    houseManage.setCreateBy("历史数据");


                    //查找主借款人信息
                    List<LoanBorrower> loanBorrowerList = loanBorrowerService.queryByLoanId(loan.getId());
                    if(CollectionUtils.isNotEmpty(loanBorrowerList)){
                        for (LoanBorrower lb: loanBorrowerList){
                            if(lb.isMaster()==true){
                                houseManage.setBorrower(lb.getName());
                            }
                        }
                    }
                    //业务来源
                    BusinessUser businessUser = businessUserService.fetchById(loan.getSaleId());
                    if(null != businessUser){
                        BusinessOrganize businessOrganize = businessOrganizeService.fetchById(businessUser.getOrganizeId());
                        if(null != businessOrganize){
                            String re = loanService.getBusinessSource(loan.getChannelId(),businessOrganize.getBusinessLine().toString(),businessOrganize.getCode(),loan.getSaleName());
                            houseManage.setSaleName(re);
                        }
                    }
                    houseManage.setSaleMan(loan.getSaleName());
                    Product prd = productService.fetchEnableProductById(loan.getProductId());
                    houseManage.setProductName(prd.getName());
                    LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
                    houseManage.setLoanSubject(loanSubject.getName());
                    //渠道类型
                    Channel channel = null;
                    if(StringUtils.isNotEmpty(loan.getChannelId())){
                        channel = dao().fetch(Channel.class, loan.getChannelId());
                    }
                    if(null != channel && ("1").equals(channel.getChannelType())){
                        houseManage.setChannelType("1");
                    }else {
                        houseManage.setChannelType("0");
                    }
                    houseManage.setLoanPrincipal(loan.getAmount());
                    houseManage.setLoanTerm(TransformUtil.getTermType(loan.getTermType(),loan.getTerm()));
                    houseManage.setLoanTime(loan.getLoanTime());
                    houseManage.setTermType(loan.getTermType().toString());
                    houseManage.setCode(h.getCode());
                    houseManage.setAddress(h.getAddress());
                    houseManage.setArea(h.getArea());
                    houseManage.setPrice(h.getPrice());
                    houseManage.setOwer(h.getOwer());
                    if(loan.getLoanStatus().equals(LoanStatus.CLEARED)){
                        List<LoanRepay> loanRepayList = queryLoanRepayByLoanId(loan.getId());
                        houseManage.setPropertyRightStatus(PropertyRightStatus.SOLVED);
                        houseManage.setStorageStatus(StorageStatus.OUT);
                        houseManage.setMortgageDate(loan.getLoanTime());
                        houseManage.setInDate(loan.getLoanTime());
                        if(CollectionUtils.isNotEmpty(loanRepayList)){
                            houseManage.setNoMortgageDate(loanRepayList.get(loanRepayList.size()-1).getRepayDate());
                            houseManage.setOutDate(loanRepayList.get(loanRepayList.size()-1).getRepayDate());
                        }
                        houseManage.setMortgageType(MortgageType.GENMORTGAGE);
                        houseManage.setGuaranteeResponsibility(loan.getAmount());
                    }else if(loan.getLoanStatus().equals(LoanStatus.LOANED) || loan.getLoanStatus().equals(LoanStatus.OVERDUE)){
                        houseManage.setPropertyRightStatus(PropertyRightStatus.WAITSECURED);
                    }
                    Trans.exec((Atom) () -> {
                        dao().insert(houseManage);
                    });
                }

            }
        }


    }

    //放款记录
    public NutMap initLoanRecord(String loanId,String subjectId,String loanSubjectAccountId,Date loanTime,User user) {
        NutMap result = new NutMap();

        String initCode = "00";
        String billCode = null;
        int i = 0;
        int j = 0;
        Date now = new Date();

        Loan loan = loanService.fetchById(loanId);

        LoanSubject loanSubject = loanSubjectService.fetch(subjectId);

        LoanSubjectAccount account = loanSubjectAccountService.queryFormatAccountsById(loanSubjectAccountId);

        List<BorrowerAccount> borrowerAccountList = borrowerAccountService.queryFormatAccountsByLoanId(loanId);

        ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());

        List<LoanRecord> loanRecordList = new ArrayList<>();
        //除了票据类型放款记录
        if (CollectionUtils.isNotEmpty(borrowerAccountList)) {
            for (BorrowerAccount borrowerAccount : borrowerAccountList) {
                i++;
                LoanRecord loanRecord = new LoanRecord();
                loanRecord.setCreateBy(user.getName());
                loanRecord.setCreateTime(now);
                loanRecord.setUpdateBy(user.getName());
                loanRecord.setUpdateTime(now);
                String code = loan.getCode()+initCode+String.valueOf(i);
                loanRecord.setLoanCode(code);
                loanRecord.setAmountType(AmountType.LOAN_AMOUNT);
                loanRecord.setLoanAmount(loan.getActualAmount().multiply(borrowerAccount.getAmount()).divide(loan.getAmount(),2, BigDecimal.ROUND_HALF_EVEN));
                loanRecord.setLoanId(loanId);
                loanRecord.setLoanDate(loanTime);
                loanRecord.setLoanStatus(LoanStatus.LOANED);
                loanRecord.setPayAcount(account.getAlias());
                loanRecord.setLoanSubject(loanSubject.getName());
                loanRecord.setPayee(borrowerAccount.getName());
                loanRecord.setPayeeAcount(getAccount(borrowerAccount,tmpl.getProductTempType()));
                loanRecord.setPosition(borrowerAccount.getPosition());
                loanRecordList.add(loanRecord);
            }
        }
        //票据放款记录
        if (productInfoTmplService.isBill(loanId)) {
            BillLoan billLoan = this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId));
            LoanBorrower loanBorrower = this.dao().fetch(LoanBorrower.class, loan.getMasterBorrowerId());
            Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
                String code = loan.getCode()+initCode+String.valueOf(1);
                LoanRecord loanRecord = new LoanRecord();
                loanRecord.setCreateBy(user.getName());
                loanRecord.setCreateTime(now);
                loanRecord.setUpdateBy(user.getName());
                loanRecord.setUpdateTime(now);
                loanRecord.setLoanCode(code);
                loanRecord.setAmountType(AmountType.LOAN_AMOUNT);
                billCode = loan.getCode()+initCode+String.valueOf(2);
                loanRecord.setLoanAmount(loan.getActualAmount());
                loanRecord.setLoanId(loanId);
                loanRecord.setLoanDate(loanTime);
                loanRecord.setLoanStatus(LoanStatus.LOANED);
                loanRecord.setPayAcount(account.getAlias());
                loanRecord.setLoanSubject(loanSubject.getName());
                loanRecord.setPayee(borrower.getName());
                loanRecord.setPayeeAcount(borrower.getAccount());
                loanRecord.setPosition(0);
                loanRecordList.add(loanRecord);

            //如有居间人则生成居间费申请
            IntermediaryApply intermediaryApplyResult = null;
            IntermediaryApply intermediaryApply = new IntermediaryApply();

            if(null != billLoan.getIntermediaryId()){
                intermediaryApply.setLoanId(loanId);
                intermediaryApply.setBusinessCode(loan.getCode());
                intermediaryApply.setApplyCode(billCode);
                Product prd = productService.fetchEnableProductById(loan.getProductId());
                intermediaryApply.setProductName(prd.getName());
                intermediaryApply.setProductId(prd.getId());
                //查找主借款人信息
                List<LoanBorrower> loanBorrowerList = loanBorrowerService.queryByLoanId(loan.getId());
                if(CollectionUtils.isNotEmpty(loanBorrowerList)){
                    for (LoanBorrower lb: loanBorrowerList){
                        if(lb.isMaster()==true){
                            intermediaryApply.setBorrower(lb.getName());
                            intermediaryApply.setMasterBorrowerId(lb.getBorrowerId());
                        }
                    }
                }
                intermediaryApply.setSubmitDate(now);
                //intermediaryApply.setLoanTime(loanTime);
                intermediaryApply.setIntermediaryId(billLoan.getIntermediaryId());
                Intermediary resultIntermediary = billLoanService.getIntermediary(billLoan);
                intermediaryApply.setName(resultIntermediary.getName());
                intermediaryApply.setIntermediaryFee(billLoan.getIntermediaryTotalFee());
                intermediaryApply.setPhone(resultIntermediary.getPhone());
                intermediaryApply.setBank(resultIntermediary.getBank());
                intermediaryApply.setAddress(resultIntermediary.getAddress());
                intermediaryApply.setAccount(resultIntermediary.getAccount());
                intermediaryApply.setIdNumber(resultIntermediary.getIdNumber());
                intermediaryApply.setCreateTime(now);
                intermediaryApply.setCreateBy(user.getName());
                intermediaryApply.setLoanStatus(LoanStatus.SAVE);
                intermediaryApplyResult = intermediaryApplyService.addIntermediaryApply(intermediaryApply);
                Boolean feeFlag = updateFee(loanId);
            }
        }
        result.put("loanRecordList",loanRecordList);
        return result;
    }

    //放款记录历史记录
    @Aop(TransAop.READ_COMMITTED)
    public void saveLoanRecordHistory() {
        List<Loan> loanList = this.dao().query(Loan.class, Cnd.where("status","=", PublicStatus.ABLE).and("loanStatus","in",new LoanStatus[]{LoanStatus.LOANED,LoanStatus.OVERDUE,LoanStatus.CLEARED}));
        for (Loan loan: loanList){
            String loanId = loan.getId();
            String subjectId = loan.getLoanSubjectId();
            String loanSubjectAccountId = loan.getLoanSubjectAccountId();

            String initCode = "00";
            String billCode = null;
            int i = 0;
            int j = 0;
            Date now = new Date();
            LoanSubject loanSubject = null;
            LoanSubjectAccount account = null;
            if(StringUtils.isNotEmpty(subjectId)){
                loanSubject = loanSubjectService.fetch(subjectId);
            }
            if(StringUtils.isNotEmpty(loanSubjectAccountId)){
                account = loanSubjectAccountService.queryFormatAccountsById(loanSubjectAccountId);
            }

            List<BorrowerAccount> borrowerAccountList = borrowerAccountService.queryFormatAccountsByLoanId(loanId);

            List<LoanRecord> loanRecordList = new ArrayList<>();

            ProductInfoTmpl tmpl=productInfoTmplService.fetchByProductId(loan.getProductId());

            if (CollectionUtils.isNotEmpty(borrowerAccountList)) {
                for (BorrowerAccount borrowerAccount : borrowerAccountList) {
                    i++;
                    LoanRecord loanRecord = new LoanRecord();
                    loanRecord.setCreateBy("历史数据");
                    loanRecord.setCreateTime(now);
                    loanRecord.setUpdateBy("历史数据");
                    loanRecord.setUpdateTime(now);
                    String code = loan.getCode()+initCode+String.valueOf(i);
                    loanRecord.setLoanCode(code);
                    loanRecord.setAmountType(AmountType.LOAN_AMOUNT);
                    loanRecord.setLoanAmount(loan.getActualAmount().multiply(borrowerAccount.getAmount()).divide(loan.getAmount(),2, BigDecimal.ROUND_HALF_EVEN));
                    loanRecord.setLoanId(loanId);
                    loanRecord.setLoanDate(loan.getLoanTime());
                    loanRecord.setLoanStatus(loan.getLoanStatus());
                    loanRecord.setPayAcount(null==account?null:account.getAlias());
                    loanRecord.setLoanSubject(null==loanSubject?null:loanSubject.getName());
                    loanRecord.setPayee(borrowerAccount.getName());
                    loanRecord.setPayeeAcount(getAccount(borrowerAccount,tmpl.getProductTempType()));
                    loanRecord.setPosition(borrowerAccount.getPosition());
                    loanRecordList.add(loanRecord);
                }
            }
            //票据放款记录
            if (productInfoTmplService.isBill(loanId)) {
                BillLoan billLoan = this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId));
                LoanBorrower loanBorrower = this.dao().fetch(LoanBorrower.class, loan.getMasterBorrowerId());
                Borrower borrower = borrowerService.fetchById1(loanBorrower.getBorrowerId());
                String code = loan.getCode()+initCode+String.valueOf(1);
                LoanRecord loanRecord = new LoanRecord();
                loanRecord.setCreateBy("历史数据");
                loanRecord.setCreateTime(now);
                loanRecord.setUpdateBy("历史数据");
                loanRecord.setUpdateTime(now);
                loanRecord.setLoanCode(code);
                loanRecord.setAmountType(AmountType.LOAN_AMOUNT);
                loanRecord.setLoanAmount(loan.getActualAmount());
                loanRecord.setLoanId(loanId);
                loanRecord.setLoanDate(loan.getLoanTime());
                loanRecord.setLoanStatus(loan.getLoanStatus());
                loanRecord.setPayAcount(null==account?null:account.getAlias());
                loanRecord.setLoanSubject(null==loanSubject?null:loanSubject.getName());
                loanRecord.setPayee(borrower.getName());
                loanRecord.setPayeeAcount(borrower.getAccount());
                loanRecord.setPosition(0);
                loanRecordList.add(loanRecord);
                }

            Trans.exec((Atom) () -> {
                dao().insert(loanRecordList);
            });
        }
    }

    public NutMap submitApply(String loanId,String id,FlowConfigureType flowType) {
        //FlowConfigureType flowType = FlowConfigureType.BROKERAGE_FEE;
        NutMap result = new NutMap();
        Loan loan = loanService.fetchById(loanId);
        if (null == loan) {
            result.put("ok", false);
            result.put("msg", "申请单信息不存在");
            return result;
        }

        FlowConfigure flowConfigure = flowConfigureService.getFlowConfigureByFlowypeAndProductIdMaybeNull(flowType.name(), loan.getProductId());


        //查看产品的流程是否配置完全
        Product product = productService.fetchEnableProductById(loan.getProductId());

        if (null == product || flowConfigure == null || (flowConfigure != null && flowConfigure.getStatus().equals(PublicStatus.DISABLED))) {
            result.put("ok", false);
            result.put("msg", "提交失败:流程未启用！");
            return result;
        }

        //查看当前产品的流程文件是否存在
        boolean snakerExist = flowService.existSnakerFile(loan.getProductId(), flowType);

        if (!snakerExist) {
            result.put("ok", false);
            result.put("msg", "提交失败:流程引擎未部署！");
            return result;
        }
        if (FlowConfigureType.BROKERAGE_FEE.equals(flowType)) {
            IntermediaryApply intermediaryApply = new IntermediaryApply();
            intermediaryApply.setId(id);
            Order order = flowService.startInstanceForIntermediaryApply(loan.getProductId(), loanId, flowType);
            if (null == order) {
                intermediaryApply.setUpdateTime(new Date());
                intermediaryApply.setLoanId(loanId);
                intermediaryApply.setLoanStatus(LoanStatus.CANCEL);
                intermediaryApplyService.update(intermediaryApply);
                result.put("ok", false);
                result.put("msg", "提交失败:初始化流程引擎失败！");
                return result;
            } else {
                intermediaryApply.setUpdateBy(ShiroSession.getLoginUser().getName());
                intermediaryApply.setUpdateTime(new Date());
                intermediaryApply.setLoanId(loanId);
                intermediaryApply.setSubmitDate(new Date());
                intermediaryApply.setLoanStatus(LoanStatus.SUBMIT);
                boolean flag = intermediaryApplyService.update(intermediaryApply);

                if (flag) {
                    result.put("ok", true);
                    result.put("msg", "提交成功");
                    return result;
                } else {
                    result.put("ok", false);
                    result.put("msg", "提交失败");
                    return result;
                }

            }
        } else if (FlowConfigureType.DECOMPRESSION.equals(flowType)) {
            HouseNoMortgageApply houseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(id);
            Order order = flowService.startInstanceForIntermediaryApply(loan.getProductId(), loanId, flowType);
            if (null == order) {
                houseNoMortgageApply.setUpdateTime(new Date());
                houseNoMortgageApply.setLoanId(loanId);
                houseNoMortgageApply.setLoanStatus(ApprovalStatusType.CANCEL);
                houseNoMortgageApplyService.updateHouseNoMortgageApply(houseNoMortgageApply);
                result.put("ok", false);
                result.put("msg", "提交失败:初始化流程引擎失败！");
                return result;
            } else {
                houseNoMortgageApply.setUpdateBy(ShiroSession.getLoginUser().getName());
                houseNoMortgageApply.setUpdateTime(new Date());
                houseNoMortgageApply.setLoanId(loanId);
                houseNoMortgageApply.setSubmitDate(new Date());
                houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_APPROVAL);
                boolean flag = houseNoMortgageApplyService.updateHouseNoMortgageApply(houseNoMortgageApply);

                if (flag) {
                    result.put("ok", true);
                    result.put("msg", "提交成功");
                    return result;
                } else {
                    result.put("ok", false);
                    result.put("msg", "提交失败");
                    return result;
                }
            }

        }
        return result;
    }

    public String getAccount(BorrowerAccount account,ProductTempType type) {
        if (ProductTempType.RRC.equals(type) || ProductTempType.SHULOUPLAT.equals(type)) {
            return account.getPlatformAccount();
        }
        return account.getAccount();
    }

    //修改费用
    public boolean updateFee(String loanId){
        BillLoan billLoan = billLoanService.fetchBillLoanByLoanId(loanId);
        if(null != billLoan.getIntermediaryTotalFee()){
            billLoan.setWithHoldingTaxFee((billLoan.getIntermediaryTotalFee().divide(TAXFEE1,10,BigDecimal.ROUND_HALF_EVEN).multiply(TAXFEE2)).setScale(2,BigDecimal.ROUND_HALF_UP));
            billLoan.setAfterTaxIntermediaryFee((billLoan.getIntermediaryTotalFee().subtract(billLoan.getWithHoldingTaxFee())).setScale(2,BigDecimal.ROUND_HALF_UP));
        }
        Boolean feeFlag = billLoanService.updateFee(billLoan);
        return feeFlag;
    }

    //费用历史数据处理
    public void saveFee(){
        List<IntermediaryApply> intermediaryApplyList = this.dao().query(IntermediaryApply.class,Cnd.where("1","=","1"));
        for (IntermediaryApply intermediaryApply : intermediaryApplyList){
            updateFee(intermediaryApply.getLoanId());
        }
    }

    public Map fetchLoanRepayFeeByRepayId(String repayId){
        String sqlStr =  "SELECT " +
                "  (slr.interest-IFNULL(slr.repayInterest,0)) as interest , " +
                "  sum(case when slf.feeType='PREPAYMENT_FEE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end) as prepaymentFee, " +
                "  sum(case when slf.feeType='PREPAYMENT_FEE_RATE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end) as prepaymentFeeRate, " +
                "  sum(case when slf.feeType='OVERDUE_FEE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end) as overdueFee, " +
                "  sum(case when slf.feeType='MANAGE_FEE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end) as manageFee, " +
                "  sum(case when slf.feeType='GUARANTEE_FEE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end )as guaranteeFee, " +
                "  sum(case when slf.feeType='SERVICE_FEE' then slf.feeAmount-IFNULL(slf.repayFeeAmount,0) else 0 end )as serviceFee " +
                "FROM " +
                "   sl_loan_repay slr  " +
                "LEFT JOIN sl_loan_fee slf  ON slr.id = slf.repayId  " +
                "WHERE slr.id =@repayId " +
                "GROUP BY " +
                " slr.id";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("repayId",  repayId );
        sql.setCallback(Sqls.callback.maps());
        dao().execute(sql);
        List<Map> nutMaps = sql.getList(Map.class);
        return CollectionUtils.isNotEmpty(nutMaps)?nutMaps.get(0):null;
    }

    public LoanRepay getLoanRepayByRepayId(String repayId){
        return dao().fetch(LoanRepay.class,repayId);
    }

    public LoanFee getLoanFeeByRepayIdAndFeeType(String repayId,FeeType feeType){
        return dao().fetch(LoanFee.class,Cnd.where("repayId","=",repayId).and("feeType","=",feeType));
    }

    public void updateLoanFeeAmount(LoanFee loanFee){
        loanFee.updateOperator();
        dao().update(loanFee,"^(feeAmount|updateBy|updateTime)$");
    }
    public void updateLoanRepay(LoanRepay loanRepay)throws  KamsException{
        loanRepay.updateOperator();
        if(DecimalFormatUtils.compare(loanRepay.getInterest(),loanRepay.getRepayInterest())==-1)throw new KamsException("费用减免利息不应该大于应还利息");
        if(DecimalFormatUtils.compare(loanRepay.getFeeAmount(),loanRepay.getRepayFeeAmount())==-1)throw new KamsException("费用减免费用总额不应该大于应还费用总额");
        if(DecimalFormatUtils.compare(loanRepay.getTotalAmount(),loanRepay.getRepayTotalAmount())==-1)throw new KamsException("费用减免总额不应该大于应还总额");
        dao().update(loanRepay,"^(repayInterest|repayTotalAmount|repayFeeAmount|updateBy|updateTime)$");
    }
    public String validateLoanRepay(LoanRepay loanRepay){
        if(DecimalFormatUtils.compare(loanRepay.getInterest(),loanRepay.getRepayInterest())==-1)return  "费用减免利息、";
        if(DecimalFormatUtils.compare(loanRepay.getFeeAmount(),loanRepay.getRepayFeeAmount())==-1)return  "费用减免费用总额、";
        if(DecimalFormatUtils.compare(loanRepay.getTotalAmount(),loanRepay.getRepayTotalAmount())==-1)return  "费用减免总额、";
        return "";
    }
}

