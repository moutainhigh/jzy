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
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.MD5Util;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.components.utils.excelUtil.ReadExcelUtil;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import com.kaisa.kams.models.history.ProductImportPiaoju;

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

/**
 * Created by zhouchuang on 2017/5/8.
 */
@IocBean(fields="dao")
public class ProductImportPiaojuService extends IdNameEntityService<ProductImportPiaoju> {
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
    private LoanFeeTempService loanFeeTempService;

    public void printEntity(TempFile mFile){
        ReadExcelUtil readExcel=new ReadExcelUtil();
        try {
            readExcel.printEntity(mFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
            return e.getMessage();
        }
        //至此已经将excel中的数据转换到list里面了,接下来就可以操作list,可以进行保存到数据库

        if(!CollectionUtils.isEmpty(list)){
            for(int i=0;i<list.size();i++){
                HashMap<String,Object> map  =  list.get(i);
                ProductImportPiaoju productImport = null;
                try {
                    productImport = (ProductImportPiaoju) ObjectUtil.fromMap(map,ProductImportPiaoju.class);
                    //productImport.setAdjDays(productImport.getAdjustDays().intValue());
                    productImport.dataConversion();
                } catch (Exception e) {
                    e.printStackTrace();
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportPiaoju exist = exsit(md5);
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

    private ProductImportPiaoju exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportPiaoju.class, cnd);
    }


    /**
     * 获取产品列表分页形式
     * @return
     */
    public DataTables queryPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportPiaoju> list = dao().query(ProductImportPiaoju.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportPiaoju.class),dao().count(ProductImportPiaoju.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }


    /**
     * 批量处理
     * @return
     */
    public Map<String,Integer> excludeByEntity(ParamData paramData){
        List<ProductImportPiaoju> list = dao().query(ProductImportPiaoju.class,paramData.getCnd());
        /*ProductType productTypesp = productTypeService.fetchByName("票据-商票");
        if(productTypesp==null)productTypesp=productTypeService.fetchByName("商业承兑汇票");
        ProductType productTypeyp = productTypeService.fetchByName("票据-银票");
        if(productTypeyp==null)productTypeyp=productTypeService.fetchByName("银行承兑汇票");
        Product productsp = productService.fetch(Cnd.where("name","=","票据-商票").and("status","=", PublicStatus.ABLE));
        if(productsp==null)productsp=productService.fetch(Cnd.where("name","=","商业承兑汇票").and("status","=", PublicStatus.ABLE));
        Product productyp = productService.fetch(Cnd.where("name","=","票据-银票").and("status","=", PublicStatus.ABLE));
        if(productyp==null)productyp=productService.fetch(Cnd.where("name","=","银行承兑汇票").and("status","=", PublicStatus.ABLE));*/
        ProductType productTypesp = productTypeService.fetchByName("票据融资");
        ProductType productTypeyp = productTypeService.fetchByName("票据融资");
        Product  productsp=productService.fetch(Cnd.where("name","=","商业承兑汇票").and("status","=", PublicStatus.ABLE));
        Product  productyp=productService.fetch(Cnd.where("name","=","银行承兑汇票").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        LoanSubject loanSubject = loanSubjectService.fetch(Cnd.where("name","=","深圳深信资产管理有限公司").and("status","=",PublicStatus.ABLE));
        LoanSubjectAccount loanSubjectAccount = null;
        if(null != loanSubject){
            loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
        }
        List<BusinessUser> businessUserList = businessUserService.query();////Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportPiaoju productImport : list){
            exclude(result, productTypesp,productTypeyp, productsp, productyp,borrowerList, loanSubject,loanSubjectAccount, businessUserList,channelList, productImport);
        }
        return result;
    }

    /**
     * 处理数据--赎楼
     * */
    public Map<String,Integer> excludeById(String id){
        /*ProductType productTypesp = productTypeService.fetchByName("票据-商票");
        if(productTypesp==null)productTypesp=productTypeService.fetchByName("商业承兑汇票");
        ProductType productTypeyp = productTypeService.fetchByName("票据-银票");
        if(productTypeyp==null)productTypeyp=productTypeService.fetchByName("银行承兑汇票");
        Product productsp = productService.fetch(Cnd.where("name","=","票据-商票").and("status","=", PublicStatus.ABLE));
        if(productsp==null)productsp=productService.fetch(Cnd.where("name","=","商业承兑汇票").and("status","=", PublicStatus.ABLE));
        Product productyp = productService.fetch(Cnd.where("name","=","票据-银票").and("status","=", PublicStatus.ABLE));
        if(productyp==null)productyp=productService.fetch(Cnd.where("name","=","银行承兑汇票").and("status","=", PublicStatus.ABLE));*/

        ProductType productTypesp = productTypeService.fetchByName("票据融资");
        ProductType productTypeyp = productTypeService.fetchByName("票据融资");
        Product  productsp=productService.fetch(Cnd.where("name","=","商业承兑汇票").and("status","=", PublicStatus.ABLE));
        Product  productyp=productService.fetch(Cnd.where("name","=","银行承兑汇票").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        LoanSubject loanSubject = loanSubjectService.fetch(Cnd.where("name","=","深圳深信资产管理有限公司").and("status","=",PublicStatus.ABLE));
        LoanSubjectAccount loanSubjectAccount = null;
        if(null != loanSubject){
            loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
        }
        List<BusinessUser> businessUserList = businessUserService.query();//Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportPiaoju ProductImportPiaoju = dao().fetch(ProductImportPiaoju.class,id);
        exclude(result, productTypesp,productTypeyp, productsp, productyp,borrowerList, loanSubject,loanSubjectAccount, businessUserList,channelList, ProductImportPiaoju);
        return result;
    }

    private void exclude(Map<String,Integer> result ,ProductType productTypesp,ProductType productTypeyp,Product productsp,Product productyp,List<Borrower> borrowerList,LoanSubject loanSubject,LoanSubjectAccount loanSubjectAccount,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportPiaoju productImportPiaoju){
        //导入失败的，已经执行成功的都不能执行了
        if(productImportPiaoju.getImportStatus().equals("01")||productImportPiaoju.getExcludeStatus().equals("01"))return;
        Trans.exec((Atom) () -> {
            //一开始应该吧执行错误信息设置为空
            productImportPiaoju.setExcludeMsg("");
            productImportPiaoju.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportPiaoju.setUpdateTime(new Date());
            dao().update(productImportPiaoju);


            Loan loan = new Loan();
            BillLoan billLoan  = new BillLoan();
            billLoan.setLoan(loan);
            //billLoan.setAccountBank(productImportPiaoju.getPayingBank());
            billLoan.setDiscountTime(productImportPiaoju.getDiscountDate());
            billLoan.setInterest(productImportPiaoju.getDiscountInterest());
            billLoan.setTotalAmount(productImportPiaoju.getParValue());
            //billLoan.setAccountName(productImportPiaoju.getDrawer());


            loan.setHistoryData("01");
            loan.setCreateBy(ShiroSession.getLoginUser().getName());
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            String prdCode ="";
            String fixCode= "";
            if("商票".equals(productImportPiaoju.getType())){
                loan.setProductTypeId(productTypesp.getId());
                loan.setProductId(productsp.getId());
                //loan.setProduct(productgz);
                //获取产品业务单号
                prdCode = productsp.getCode();
                //订单后8位修正编号
                fixCode = loanService.fetchMaxCode(productsp.getId());
            }else if("银票".equals(productImportPiaoju.getType())){
                loan.setProductTypeId(productTypeyp.getId());
                loan.setProductId(productyp.getId());
                //获取产品业务单号
                prdCode = productyp.getCode();
                //订单后8位修正编号
                fixCode = loanService.fetchMaxCode(productyp.getId());
            }
            //检查产品是否存在
            if(StringUtils.isEmpty(loan.getProductId())){
                productImportPiaoju.addExcludeMsg("找不到对应的产品");
            }
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //所有的放款主体都是  深圳深信商业保理有限公司
            if(null != loanSubject){
                loan.setLoanSubjectId(loanSubject.getId());
            }

            if(loanSubjectAccount!=null)
                loan.setLoanSubjectAccountId(loanSubjectAccount.getId());

            //检查还款主体是否存在
            if(StringUtils.isEmpty(loan.getLoanSubjectId())){
                productImportPiaoju.addExcludeMsg("放款主体'深圳深信商业保理有限公司'不存在");
            }
            loan.setAmount(productImportPiaoju.getParValue().subtract(productImportPiaoju.getDiscountInterest()));

            loan.setTermType(LoanTermType.FIXED_DATE);

            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");

            //如果到期日在当前日期之后，则肯定是还款中，反正则为已还清。不管，就是以还清。
            if(productImportPiaoju.getExpireDate().getTime()>new Date().getTime()){
                loan.setLoanStatus(LoanStatus.LOANED);
            }else{
                loan.setLoanStatus(LoanStatus.CLEARED);
            }


            loan.setSubmitTime(productImportPiaoju.getDiscountDate());
            loan.setTerm(DateUtil.formatDateToString(productImportPiaoju.getExpireDate()));
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportPiaoju.getDiscountDate());
            loan.setActualAmount(productImportPiaoju.getParValue().subtract(productImportPiaoju.getDiscountInterest()));

            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            LoanBorrower loanBorrower = null;
            for(Borrower borrower : borrowerList){
                if(borrower.getName().equals(productImportPiaoju.getDiscountProposer())){
                    loanBorrower =  createLoanBorrower(borrower);
                    break;
                }
            };

            //没有业务员会差不到loan的信息，这里默认给她一个业务员 后续讨论
           /* for(BusinessUser businessUser : businessUserList){
                if("潘妙芙".equals(businessUser.getName())){
                    loan.setSaleName(businessUser.getName());
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    break;
                }
            }
*/
            //检查借款人是否存在 ,如果不存在则创建
            if(loanBorrower==null){
                //创建借款人
                Borrower borrower = new Borrower();
                borrower.setName(productImportPiaoju.getDiscountProposer());
                borrower.setCertifType(LoanerCertifType.ID);
                borrower.setStatus(PublicStatus.ABLE);
                borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                borrower.setCreateTime(new Date());
                Borrower borrowerSaved  = borrowerService.add(borrower);
                borrowerList.add(borrowerSaved);

                //创建借款人和贷款的关联表
                loanBorrower =  createLoanBorrower(borrowerSaved);
            }

            if(StringUtils.isEmpty(productImportPiaoju.getExcludeMsg())){
                //保存loan获取id 设置借款人保存，设置billloan的loanid保存
                Loan saveLoan = dao().insert(loan);
                loan.setId(saveLoan.getId());
                billLoan.setLoanId(saveLoan.getId());
                loanBorrower.setLoanId(saveLoan.getId());
                saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                dao().insert(billLoan);

                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);

                //更新完了后，再创建loanRepay对象
                BillLoanRepay billLoanRepay = createBillLoanRepay(billLoan ,productImportPiaoju);
                //保存票据信息
                BillLoanRepay billLoanRepaySaved = dao().insert(billLoanRepay);
                //保存票据的还款计划
                if(billLoanRepay.getLoanRepay()!=null){
                    LoanRepay loanRepaySaved = dao().insert(billLoanRepay.getLoanRepay());
                    billLoanRepaySaved.setRepayId(loanRepaySaved.getId());
                    billLoanRepaySaved.setUpdateBy(ShiroSession.getLoginUser().getName());
                    billLoanRepaySaved.setUpdateTime(new Date());
                    dao().update(billLoanRepaySaved);
                    //如果有还款记录，则保存还款记录
                    if(CollectionUtils.isNotEmpty(billLoanRepay.getLoanRepay().getLoanRepayRecordList())){
                        for(LoanRepayRecord loanRepayRecord : billLoanRepay.getLoanRepay().getLoanRepayRecordList()){
                            loanRepayRecord.setRepayId(loanRepaySaved.getId());
                            dao().insert(loanRepayRecord);
                        }
                    }
                }
                //设置为成功，并且关联好loanId以便查询
                productImportPiaoju.setExcludeStatus("01");
                productImportPiaoju.setLoanId(saveLoan.getId());
                result.put("success",result.get("success")+1);
            }else{//如果不为空，则改变状态为02失败
                productImportPiaoju.setExcludeStatus("02");
                result.put("failure",result.get("failure")+1);
            }
            productImportPiaoju.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportPiaoju.setUpdateTime(new Date());
            dao().update(productImportPiaoju);
        });
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
        loanBorrower.setLegalRepresentative(borrower.getLegalRepresentative());
        loanBorrower.setLegalRepresentativePhone(borrower.getLegalRepresentativePhone());
        loanBorrower.setLinkman(borrower.getLinkman());
        loanBorrower.setLinkmanPhone(borrower.getLinkmanPhone());
        loanBorrower.setResidence(borrower.getResidence());
        return loanBorrower;
    }

    private BillLoanRepay createBillLoanRepay(BillLoan billLoan,ProductImportPiaoju productImport){
        //分为还款中和已结清  已结结清是有还款记录的
        BillLoanRepay billLoanRepay = new BillLoanRepay();
        LoanRepay loanRepay = new LoanRepay();
        billLoanRepay.setBankAddress("");
        billLoanRepay.setBankName(productImport.getPayingBank());
        billLoanRepay.setBillNo(productImport.getBillNo());
        billLoanRepay.setDrawTime(productImport.getBillingDate());
        billLoanRepay.setLoanId(billLoan.getLoanId());
        billLoanRepay.setLoanRepay(loanRepay);
        //调整天数都为0
        billLoanRepay.setOverdueDays(productImport.getAdjDays().intValue());
        billLoanRepay.setPayee(productImport.getPayee());
        billLoanRepay.setPayer(productImport.getDrawer());
        billLoanRepay.setRiskRank(RiskRank.A);
        billLoanRepay.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        billLoanRepay.setCreateTime(new Date());

        billLoanRepay.setDisDays(productImport.getDiscountDays().intValue());
        billLoanRepay.setCostRate(productImport.getPrice().multiply(new BigDecimal(100)));
        billLoanRepay.setPayerAccount(productImport.getPayAcount());
        billLoanRepay.setDisDate(productImport.getDiscountDate());
        billLoanRepay.setActualDueDate(DateUtil.getDateAfter(productImport.getDiscountDate(),productImport.getDiscountDays().intValue()));

        loanRepay.setDueDate(productImport.getExpireDate());
        loanRepay.setInterest(productImport.getDiscountInterest());
        loanRepay.setRepayDate(productImport.getExpireDate());
        //没有利息可收，归还全部本金
        loanRepay.setAmount(productImport.getParValue());
        loanRepay.setRepayAmount(new BigDecimal(0));
        loanRepay.setRepayTotalAmount(new BigDecimal(0));
        loanRepay.setTotalAmount(productImport.getParValue());
        loanRepay.setStatus(LoanRepayStatus.LOANED);
        loanRepay.setPeriod(1);
        loanRepay.setRemark("票据还款共一期：第一期");
        loanRepay.setLoanId(billLoan.getLoanId());
        //loanRepay.setInterest(productImport.getDiscountInterest());
        //loanRepay.setRepayInterest(productImport.getDiscountInterest());
        loanRepay.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        loanRepay.setCreateTime(new Date());

        if(billLoan.getLoan().getLoanStatus().equals(LoanStatus.CLEARED)){
            billLoanRepay.getLoanRepay().setStatus(LoanRepayStatus.CLEARED);
            billLoanRepay.getLoanRepay().setRepayAmount(productImport.getParValue());
            billLoanRepay.getLoanRepay().setRepayTotalAmount(productImport.getParValue());

            LoanRepayRecord loanRepayRecord  = new LoanRepayRecord();
            loanRepayRecord.setRepayDate(billLoanRepay.getLoanRepay().getRepayDate());
            loanRepayRecord.setRepayTotalAmount(billLoanRepay.getLoanRepay().getRepayTotalAmount());
            loanRepayRecord.setRepayAmount(billLoanRepay.getLoanRepay().getRepayAmount());
            loanRepayRecord.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
            loanRepayRecord.setCreateTime(new Date());
            billLoanRepay.getLoanRepay().addLoanRepayRecord(loanRepayRecord);
        }
        return billLoanRepay;
    }

}
