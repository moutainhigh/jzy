package com.kaisa.kams.components.service.history;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BorrowerService;
import com.kaisa.kams.components.service.BusinessUserService;
import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.service.LoanBorrowerService;
import com.kaisa.kams.components.service.LoanFeeTempService;
import com.kaisa.kams.components.service.LoanRepayRecordService;
import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.LoanSubjectAccountService;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.ProductFeeService;
import com.kaisa.kams.components.service.ProductInfoItemService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.components.utils.excelUtil.ReadExcelUtil;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import com.kaisa.kams.models.history.ProductImportRenRenChe;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.upload.TempFile;
import org.nutz.service.IdNameEntityService;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.math.BigDecimal;
import java.util.*;

;

/**
 * Created by luoyj on 2017/5/08.
 */
@IocBean(fields="dao")
public class ProductImportRenRenCheService extends IdNameEntityService<ProductImportRenRenChe> {

    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private ProductService productService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private LoanBorrowerService loanBorrowerService;
    @Inject
    private BorrowerService borrowerService;
    @Inject
    private LoanService loanService;
    @Inject
    private LoanRepayService loanRepayService;
    @Inject
    private LoanRepayRecordService loanRepayRecordService;
    @Inject
    private BusinessUserService businessUserService;
    @Inject
    private ChannelService channelService;
    @Inject
    private ProductFeeService productFeeService;
    @Inject
    private LoanSubjectAccountService loanSubjectAccountService;
    @Inject
    private ProductInfoItemService productInfoItemService;
    @Inject
    private LoanFeeTempService loanFeeTempService;

    public String readExcelFile(TempFile mFile){
        String version = UUID.randomUUID().toString();
        String result ="";
        //创建处理EXCEL的类
        ReadExcelUtil readExcel=new ReadExcelUtil();
        //解析excel，获取上传的事件单

        List<HashMap<String,Object>> list = null;
        try {
            list = readExcel.getExcelInfoToMap(mFile);
        } catch (Exception e) {
            return e.getMessage();
        }
        //至此已经将excel中的数据转换到list里面了,接下来就可以操作list,可以进行保存到数据库

        if(!CollectionUtils.isEmpty(list)){
            for(int i=0;i<list.size();i++){
                HashMap<String,Object> map  =  list.get(i);
                ProductImportRenRenChe productImport = null;
                try {
                    productImport = (ProductImportRenRenChe) ObjectUtil.fromMap(map,ProductImportRenRenChe.class);
                    productImport.dataConversion();
                } catch (Exception e) {
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportRenRenChe exist = exsit(md5);
                if(exist==null){
                    productImport.setLineNum(i+2);
                    productImport.setVersion(version);
                    productImport.setMd5(md5);
                    productImport.setCreateTime(new Date());
                    productImport.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
                    productImport.validata();
                    dao().insert(productImport);
                }else{
                    if(version.equals(exist.getVersion())){
                        return  "导入数据失败，数据有重复，已存在的excel行为："+exist.getLineNum()+"，重复的excel行为："+(i+2);
                    }else{
                        //更新导入版本
                        exist.setVersion(version);
                        exist.setLineNum(i+2);
                        dao().update(exist);
                    }
                }
            };
            result="导入数据成功";
        }else {
            result="导入数据为空";
        }

        return result;
    }

    /**
     * 判断是否已经导入过了
     * @return
     */
    private ProductImportRenRenChe exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportRenRenChe.class, cnd);
    }

    /**
     * 获取产品列表
     * @return
     */
    public List<ProductImportRenRenChe> queryListAll(Cnd cnd) {
        return dao().query(ProductImportRenRenChe.class, cnd.orderBy().desc("createTime"));
    }


    /**
     * 获取产品列表分页形式
     * @return
     */
    public DataTables queryPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportRenRenChe> list = dao().query(ProductImportRenRenChe.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportRenRenChe.class),dao().count(ProductImportRenRenChe.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }

    /**
     * 处理-  人人车（单条）
     * @param id
     * @return
     */
    public Map<String,Integer> excludeRenRenCheById(String id){
        ProductType productType = productTypeService.fetchByName("平台放款");
        Product productyrrc = productService.fetch(Cnd.where("name","=","车押贷-人人车").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportRenRenChe productImportRenRenChe =dao().fetch(ProductImportRenRenChe.class,id);
        excludeRenRenChe(result, productType, productyrrc,borrowerList, subjectList, businessUserList,channelList,productImportRenRenChe);
        return result;
    }

    private void excludeRenRenChe(Map<String,Integer> result ,ProductType productType,Product productrrc,List<Borrower> borrowerList,List<LoanSubject> subjectList,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportRenRenChe productImportRenRenChe) {
        //导入失败的，已经执行成功的都不能执行了
        if (productImportRenRenChe.getImportStatus().equals("01") || productImportRenRenChe.getExcludeStatus().equals("01"))
            return;
        Trans.exec((Atom) () -> {
            //一开始应该把执行错误信息设置为空
            productImportRenRenChe.setExcludeMsg("");
            productImportRenRenChe.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportRenRenChe.setUpdateTime(new Date());
            dao().update(productImportRenRenChe);
            // 判断还款状态为已结清的数据，实际还款时间是否有值
            if("已结清".equals(productImportRenRenChe.getRepaymentStatus())&&productImportRenRenChe.getRepayDate()==null){
                productImportRenRenChe.addExcludeMsg("实际还款时间不能为空");
            }
            Loan loan = new Loan();
            loan.setHistoryData("01");
            loan.setCreateBy("历史数据导入");
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            //loan.setProductType(productType);
            loan.setProductTypeId(productType.getId());
            String prdCode = "";
            String fixCode = "";
            //产品id
            loan.setProductId(productrrc.getId());
            //获取产品业务单号
            prdCode = productrrc.getCode();
            //订单后8位修正编号
            fixCode = loanService.fetchMaxCode(productrrc.getId());
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //放款主体
            subjectList.stream().forEach(loanSubject -> {
                if (loanSubject.getName().equals(productImportRenRenChe.getLoanSubject())) {
                    loan.setLoanSubjectId(loanSubject.getId());
                    LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId", "=", loanSubject.getId()));
                    if (loanSubjectAccount != null)
                        loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                    return;
                }
            });
            //检查还款主体是否存在
            if (StringUtils.isEmpty(loan.getLoanSubjectId())) {
                productImportRenRenChe.addExcludeMsg("放款主体不存在");
            }
            loan.setAmount(productImportRenRenChe.getLoanPrincipal());
            loan.setActualAmount(productImportRenRenChe.getActualAmount());
            //还款方式
            loan.setRepayMethod(getRepayMethod(productImportRenRenChe.getRepaymentMethod(), productImportRenRenChe.getLoanTermUnit()));
            if (loan.getRepayMethod() == null) {
                productImportRenRenChe.addExcludeMsg("还款方式不存在");
            } else {
                //一次性就是期末  先息后本就是期初
                if (loan.getRepayMethod().getCode().equals("3300")) {
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                } else if (loan.getRepayMethod().getCode().equals("3303")) {
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }
            }
            //借款期限是否存在
            if (StringUtils.isNotEmpty(productImportRenRenChe.getLoanTerm())) {
                if (productImportRenRenChe.getLoanTermUnit().equals("天")) {
                    loan.setTermType(LoanTermType.DAYS);
                    loan.setTerm(productImportRenRenChe.getLoanTermNum().intValue() + "");
                    loan.setInterestRate(productImportRenRenChe.getBorrowRate().multiply(new BigDecimal(100)));
                }
            } else {
                productImportRenRenChe.addExcludeMsg("借款期限不存在");
            }
            loan.setLoanTime(productImportRenRenChe.getLoanDate());
            loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
            //利息类型
            loan.setLoanLimitType(LoanLimitType.FIX_RATE);
            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");
            if ("还款中".equals(productImportRenRenChe.getRepaymentStatus())) {
                loan.setLoanStatus(LoanStatus.LOANED);
            } else if ("结清".equals(productImportRenRenChe.getRepaymentStatus())) {
                loan.setLoanStatus(LoanStatus.CLEARED);
            }else if ("已结清".equals(productImportRenRenChe.getRepaymentStatus())) {
                loan.setLoanStatus(LoanStatus.CLEARED);
            }
            //判断还款状态
            if (null == loan.getLoanStatus()) {
                productImportRenRenChe.addExcludeMsg("找不到对应还款状态");
            }
            businessUserList.stream().forEach(businessUser -> {
                if (businessUser.getName().equals(productImportRenRenChe.getBusinessName())) {
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    loan.setSaleName(businessUser.getName());
                    return;
                }
            });
            //判断有没有业务员
            if (StringUtils.isEmpty(loan.getSaleId())) {
                //productImportCheDai.addExcludeMsg("找不到对应的业务员");
                loan.setActualBusinessName(productImportRenRenChe.getBusinessName());
                businessUserList.stream().forEach(businessUser -> {
                    if ("虚拟业务员".equals(businessUser.getName())) {
                        loan.setSaleId(businessUser.getId());
                        loan.setSaleCode(businessUser.getCode());
                        loan.setSaleName(businessUser.getName());
                        return;
                    }
                });
            }
            loan.setSubmitTime(productImportRenRenChe.getLoanDate());
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportRenRenChe.getLoanDate());
            //渠道Id设置
            channelList.stream().forEach(channel -> {
                if (channel.getName().contains(productImportRenRenChe.getChannel())) {
                    loan.setChannelId(channel.getId());
                    return;
                }
            });
            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            LoanBorrower loanBorrower = null;
            for (Borrower borrower : borrowerList) {
                if (borrower.getName().equals(productImportRenRenChe.getBorrower()) && borrower.getCertifNumber().equals(productImportRenRenChe.getIdNumber())) {
                    loanBorrower = createLoanBorrower(borrower);
                    break;
                }
            };
            //检查借款人是否存在 ,如果不存在则创建
            if (loanBorrower == null) {
                //创建借款人
                Borrower borrower = new Borrower();
                borrower.setName(productImportRenRenChe.getBorrower());
                borrower.setCertifNumber(productImportRenRenChe.getIdNumber());
                borrower.setCertifType(LoanerCertifType.ID);
                borrower.setStatus(PublicStatus.ABLE);
                borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                borrower.setCreateTime(new Date());
                Borrower borrowerSaved = borrowerService.add(borrower);
                borrowerList.add(borrowerSaved);
                //创建借款人和贷款的关联表
                loanBorrower = createLoanBorrower(borrowerSaved);
            }
            if (StringUtils.isEmpty(productImportRenRenChe.getExcludeMsg())) {//如果导出信息为空，则符合导出条件。则保存load 获取id复制给借款人，保存借款人，拿到借款人ID复制给loan，然后在更新loan，改变状态为已经执行成功
                Loan saveLoan = dao().insert(loan);
                //新增所有的费用
                List<ProductFee> productFees = productFeeService.queryFeeByProductId(productrrc.getId());
                for (ProductFee productFee : productFees) {
                    LoanFeeTemp loanFeeTemp = new LoanFeeTemp(productFee);
                    loanFeeTemp.setCreateBy(ShiroSession.getLoginUser().getName());
                    loanFeeTemp.setCreateTime(new Date());
                    loanFeeTemp.setUpdateTime(new Date());
                    loanFeeTemp.setUpdateTime(new Date());
                    loanFeeTemp.setLoanId(saveLoan.getId());
                    if(productFee.getFeeType().equals(FeeType.PREPAYMENT_FEE_RATE)){
                        loanFeeTemp.setFeeAmount(productImportRenRenChe.getServiceCharge());
                    }
                    loanFeeTempService.add(loanFeeTemp);
                }
                //关联借款人
                loanBorrower.setLoanId(saveLoan.getId());
                saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);

                //保存还款计划放款前收取的一次性服务费用
                ProductFee productFee = productFeeService.fetch(Cnd.where("productId","=",loan.getProductId()).and("feeType","=",FeeType.PREPAYMENT_FEE_RATE));
                LoanFee loanFee= getLoanFee(saveLoan.getId(),productImportRenRenChe,productFee);
                LoanFee loanFeeSaved =  dao().insert(loanFee);
                //更新完了后，再创建loanRepay对象
                List<LoanRepay> loanRepayList = createLoanRepayList(saveLoan, productImportRenRenChe);
                loanRepayList.stream().forEach(loanRepay -> {
                    //保存还款计划
                    LoanRepay loanRepaySaved = dao().insert(loanRepay);
                    if (LoanRepayStatus.CLEARED.equals(loanRepaySaved.getStatus())) {
                        LoanRepayRecord loanRepayRecord1 = getLoanRepayRecord(loanRepay);
                        loanRepayRecord1.setRepayDate(loanRepaySaved.getRepayDate());
                        // 通过比对期数，判断是否改变loanStatus
                        if (loanRepayList.size() == loanRepaySaved.getPeriod()) {
                            if (LoanStatus.LOANED.equals(saveLoan.getLoanStatus())) {
                                saveLoan.setLoanStatus(LoanStatus.CLEARED);
                                loanService.update(saveLoan);
                            }
                        }
                    }
                    //保存还款记录
                    if (loanRepay.getLoanRepayRecordList() != null)
                        for (LoanRepayRecord loanRepayRecord : loanRepay.getLoanRepayRecordList()) {
                            loanRepayRecord.setRepayId(loanRepaySaved.getId());
                            if(loanRepay.getStatus().equals(LoanRepayStatus.CLEARED)){
                                loanRepayRecord.setRepayDate(productImportRenRenChe.getRepayDate());
                            }
                            LoanRepayRecord loanRepayRecordSaved = dao().insert(loanRepayRecord);
                            //保存还款记录里面的费用记录
                            if (loanRepayRecord.getLoanFeeRecordList() != null)
                                for (LoanFeeRecord loanFeeRecord : loanRepayRecord.getLoanFeeRecordList()) {
                                    loanFeeRecord.setLoanFeeId(loanFeeSaved.getId());
                                    loanFeeRecord.setRepayRecordId(loanRepayRecordSaved.getId());
                                    loanFeeRecord.setRepayId(loanRepaySaved.getId());
                                    dao().insert(loanFeeRecord);
                                }
                        }
                });
                //设置为成功，并且关联好loanId以便查询
                productImportRenRenChe.setExcludeStatus("01");
                productImportRenRenChe.setLoanId(saveLoan.getId());
                result.put("success", result.get("success") + 1);
            } else {//如果不为空，则改变状态为02失败
                productImportRenRenChe.setExcludeStatus("02");
                result.put("failure", result.get("failure") + 1);
            }
            productImportRenRenChe.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportRenRenChe.setUpdateTime(new Date());
            dao().update(productImportRenRenChe);
        });
    }

    /**
     * 批量处理
     * @param paramData
     * @return
     */
    public Map<String,Integer> excludeRenRenCheByEntity(ParamData paramData){
        List<ProductImportRenRenChe> list = dao().query(ProductImportRenRenChe.class,paramData.getCnd());
        ProductType productType = productTypeService.fetchByName("平台放款");
        Product productyrrc = productService.fetch(Cnd.where("name","=","车押贷-人人车").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportRenRenChe productImportRenRenChe:list){
            excludeRenRenChe(result, productType, productyrrc,borrowerList, subjectList, businessUserList,channelList,productImportRenRenChe);
        }
        return result;
    }

    private LoanRepayMethod getRepayMethod(String repayMethod,String loanTermUnit){
        if("一次性还款-期末收息".equals(repayMethod) || "到期还本付息".equals(repayMethod)){
            return LoanRepayMethod.BULLET_REPAYMENT;
        }else if("先息后本-期末收息".equals(repayMethod)){
            if("天".equals(loanTermUnit)){
                return LoanRepayMethod.INTEREST_DAYS;
            }else if("月".equals(loanTermUnit)) {
                return LoanRepayMethod.INTEREST_MONTHS;
            }
        }else if("等额本息".equals(repayMethod)){
            return  LoanRepayMethod.EQUAL_INSTALLMENT;
        }else if("一次性还本付息".equals(repayMethod)){
            return  LoanRepayMethod.BULLET_REPAYMENT;
        }
        return null;
    }
    private LoanBorrower createLoanBorrower(Borrower borrower){
        LoanBorrower loanBorrower  = new LoanBorrower();
        loanBorrower.setName(borrower.getName());
        loanBorrower.setAddress(borrower.getAddress());
        loanBorrower.setBorrowerId(borrower.getId());
        loanBorrower.setCertifNumber(borrower.getCertifNumber());
        loanBorrower.setCertifType(borrower.getCertifType());
        loanBorrower.setMaster(Boolean.TRUE);
        loanBorrower.setPhone(borrower.getPhone());
        loanBorrower.setStatus(borrower.getStatus());
        loanBorrower.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        loanBorrower.setCreateTime(new Date());
        return loanBorrower;
    }

    private LoanRepayRecord getLoanRepayRecord(LoanRepay loanRepay){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayAmount(loanRepay.getAmount());
        loanRepayRecord.setRepayDate(loanRepay.getDueDate());
        loanRepayRecord.setRepayInterest(loanRepay.getInterest());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()));
        loanRepayRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepay.addLoanRepayRecord(loanRepayRecord);
        return loanRepayRecord;
    }

    private List<LoanRepay> createLoanRepayList(Loan loan,ProductImportRenRenChe productImportRenRenChe){

        LoanRepayPlan loanRepayPlan = LoanCalculator.calcuate(loan.getAmount(),
                loan.getTermType(),
                loan.getTerm(),
                loan.getRepayMethod(),
                loan.getLoanLimitType(),
                LoanLimitType.FIX_AMOUNT.equals(loan.getLoanLimitType())?loan.getInterestAmount():loan.getInterestRate(),
                loan.getRepayDateType(),
                loan.getLoanTime(),
                loan.getMinInterestAmount(),
                loan.getCalculateMethodAboutDay());
        //2.按期费用,收费节点为还款时收取，收费频率是按期
        List<LoanRepay> cacltLoanRepayList = new ArrayList<>();
        Date now = new Date();
        loanRepayPlan.getRepayments().stream().forEach(r->{
            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loan.getId());
            loanRepay.setPeriod(r.getPeriod());
            loanRepay.setAmount(r.getPrincipal());
            loanRepay.setInterest(r.getInterest());
            loanRepay.setOutstanding(r.getOutstanding());
            loanRepay.setDueDate(r.getDueDate());
            BigDecimal feeAmount = BigDecimal.ZERO;
            loanRepay.setFeeAmount(feeAmount);
            loanRepay.setTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
            loanRepay.setCreateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setCreateTime(now);
            loanRepay.setUpdateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setUpdateTime(now);
            if(r.getDueDate().getTime()<=new Date().getTime()){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
                loanRepay.setRepayDate(r.getDueDate());
                loanRepay.setRepayTotalAmount(loanRepay.getTotalAmount());
                loanRepay.setRepayAmount(loanRepay.getAmount());
                loanRepay.setRepayInterest(loanRepay.getInterest());
                loanRepay.setRepayFeeAmount(loanRepay.getFeeAmount());
            }else {
                loanRepay.setStatus(LoanRepayStatus.LOANED);
            }
            cacltLoanRepayList.add(loanRepay);
        });
        return cacltLoanRepayList;
    }
    private LoanFee getLoanFee(String loanId,ProductImportRenRenChe productImportRenRenChe,ProductFee productFee){
        LoanFee loanFee = new LoanFee();
        loanFee.setFeeId(productFee.getId());
        loanFee.setLoanId(loanId);
        loanFee.setFeeName(FeeType.PREPAYMENT_FEE_RATE.getDescription());
        loanFee.setPeriod(0);
        loanFee.setFeeAmount(productImportRenRenChe.getServiceCharge());
        loanFee.setRepayFeeAmount(productImportRenRenChe.getServiceCharge());
        loanFee.setRepayDate(productImportRenRenChe.getLoanDate());
        loanFee.setDueDate(productImportRenRenChe.getLoanDate());
        loanFee.setStatus(LoanRepayStatus.CLEARED);
        loanFee.setChargeNode(FeeChargeNode.LOAN_NODE);
        loanFee.setFeeType(FeeType.PREPAYMENT_FEE_RATE);
        loanFee.setCreateTime(new Date());
        loanFee.setCreateBy(ShiroSession.getLoginUser().getName());
        return loanFee;
    }
}
