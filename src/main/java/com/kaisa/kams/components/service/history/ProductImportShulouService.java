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
import com.kaisa.kams.components.utils.MD5Util;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.components.utils.excelUtil.ReadExcelUtil;;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import com.kaisa.kams.models.history.ProductImportShulou;

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
 * Created by zhouchuang on 2017/4/26.
 */
@IocBean(fields="dao")
public class ProductImportShulouService extends IdNameEntityService<ProductImportShulou> {

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
                ProductImportShulou productImport = null;
                try {
                    productImport = (ProductImportShulou) ObjectUtil.fromMap(map,ProductImportShulou.class);
                } catch (Exception e) {
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportShulou exist = exsit(md5);
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
    /*private boolean exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        if (dao().fetch(ProductImportShulou.class, cnd) != null) {
            return true;
        }
        return false;
    }*/
    private ProductImportShulou exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportShulou.class, cnd);
    }




    /**
     * 获取产品列表
     * @return
     */
    public List<ProductImportShulou> queryListAll(Cnd cnd) {
        return dao().query(ProductImportShulou.class, cnd.orderBy().desc("createTime"));
    }


    /**
     * 获取产品列表分页形式
     * @return
     */
    public DataTables queryPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportShulou> list = dao().query(ProductImportShulou.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportShulou.class),dao().count(ProductImportShulou.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }

    /**
     * 批量处理
     * @return
     */
    public Map<String,Integer> excludeShulouByEntity(ParamData paramData){
        List<ProductImportShulou> list = dao().query(ProductImportShulou.class,paramData.getCnd());
        ProductType productType = productTypeService.fetchByName("赎楼贷");
        Product productsz = productService.fetch(Cnd.where("name","=","非交易性赎楼-深圳").and("status","=", PublicStatus.ABLE));
        Product productgz = productService.fetch(Cnd.where("name","=","非交易性赎楼-广州").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();//Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportShulou productImportShulou : list){
            excludeShulou(result, productType, productsz, productgz,borrowerList, subjectList, businessUserList,channelList, productImportShulou);
        }
        return result;
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
    private void excludeShulou(Map<String,Integer> result ,ProductType productType,Product productsz,Product productgz,List<Borrower> borrowerList,List<LoanSubject> subjectList,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportShulou productImportShulou){
        //导入失败的，已经执行成功的都不能执行了
        if(productImportShulou.getImportStatus().equals("01")||productImportShulou.getExcludeStatus().equals("01"))return;
        Trans.exec((Atom) () -> {
            //一开始应该吧执行错误信息设置为空

            productImportShulou.setExcludeMsg("");
            productImportShulou.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportShulou.setUpdateTime(new Date());
            dao().update(productImportShulou);
            Loan loan = new Loan();
            loan.setHistoryData("01");
            loan.setCreateBy("历史数据导入");
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            //loan.setProductType(productType);
            loan.setProductTypeId(productType.getId());
            String prdCode ="";
            String fixCode= "";
            if("广州分公司".equals(productImportShulou.getChannel())){
                loan.setProductId(productgz.getId());
                //loan.setProduct(productgz);
                //获取产品业务单号
                prdCode = productgz.getCode();
                //订单后8位修正编号
                fixCode = loanService.fetchMaxCode(productgz.getId());
            }else{
                loan.setProductId(productsz.getId());
                //loan.setProduct(productsz);
                //获取产品业务单号
                prdCode = productsz.getCode();
                //订单后8位修正编号
                fixCode = loanService.fetchMaxCode(productsz.getId());
            }
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //遇到富昌小贷都得找深圳富昌小额贷款有限公司  放款账号默认为第一个
            subjectList.stream().forEach(loanSubject -> {
                if("富昌小贷".equals(productImportShulou.getLoanSubject())){
                    if("深圳市富昌小额贷款有限公司".equals(loanSubject.getName())){
                        loan.setLoanSubjectId(loanSubject.getId());
                        LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
                        if(loanSubjectAccount!=null)
                            loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                        return;
                    }
                }else{
                    if(loanSubject.getName().equals(productImportShulou.getLoanSubject())){
                        loan.setLoanSubjectId(loanSubject.getId());
                        LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
                        if(loanSubjectAccount!=null)
                            loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                        return;
                    }
                }
            });
            //检查还款主体是否存在
            if(StringUtils.isEmpty(loan.getLoanSubjectId())){
                productImportShulou.addExcludeMsg("放款主体不存在");
            }
            loan.setAmount(productImportShulou.getLoanPrincipal());
            loan.setActualAmount(productImportShulou.getLoanPrincipal());
            loan.setRepayMethod(getRepayMethod(productImportShulou.getRepaymentMethod()));
            if(loan.getRepayMethod()==null){
                productImportShulou.addExcludeMsg("还款方式不存在");
            }else{
                //一次性就是期末  先息后本就是期初
                if(loan.getRepayMethod().getCode().equals("3301")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_PRE);
                }else if(loan.getRepayMethod().getCode().equals("3303")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }
            }


            //利息类型
            loan.setLoanLimitType(LoanLimitType.FIX_RATE);
            loan.setInterestRate(productImportShulou.getDayRate().multiply(new BigDecimal(100)));
            loan.setTermType(LoanTermType.DAYS);
            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");
            if("还款中".equals(productImportShulou.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.LOANED);
            }else if("结清".equals(productImportShulou.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.CLEARED);
            }
            //判断还款状态
            if(null==loan.getLoanStatus()){
                productImportShulou.addExcludeMsg("找不到对应还款状态");
            }
            businessUserList.stream().forEach(businessUser -> {
                if(businessUser.getName().equals(productImportShulou.getBusinessName())){
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    loan.setSaleName(businessUser.getName());
                    return ;
                }
            });
            //判断有没有业务员  没有的话指定为虚拟业务员
            if(StringUtils.isEmpty(loan.getSaleId())){
                loan.setActualBusinessName(productImportShulou.getBusinessName());
                businessUserList.stream().forEach(businessUser -> {
                    if("虚拟业务员".equals(businessUser.getName())){
                        loan.setSaleId(businessUser.getId());
                        loan.setSaleCode(businessUser.getCode());
                        loan.setSaleName(businessUser.getName());
                        return ;
                    }
                });
                //productImportShulou.addExcludeMsg("找不到对应的业务员");
            }
            loan.setSubmitTime(productImportShulou.getLoanDate());
            loan.setTerm(getDuration(productImportShulou).intValue()+"");
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportShulou.getLoanDate());
            //人人聚财 算头不算尾 其他算头算尾
            if(productImportShulou.getChannel().equals("人人聚财")){
                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
            }else{
                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL);
            }
            //如果是深圳自营，则归纳为自营，没有合作方，没有渠道ID，所以，如果不是深圳自营，则需要保存渠道ID
            if(!productImportShulou.getChannel().equals("深圳自营")&&!productImportShulou.getChannel().equals("广州分公司")){
                channelList.stream().forEach(channel -> {
                    if(channel.getName().contains(productImportShulou.getChannel())){
                        loan.setChannelId(channel.getId());
                        return;
                    }
                });
                //只有不是深圳自营才需要判断渠道是否存在
                if(StringUtils.isEmpty(loan.getChannelId())){
                    productImportShulou.addExcludeMsg("渠道不存在");
                }
            }


            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            LoanBorrower loanBorrower = null;
            for(Borrower borrower : borrowerList){
                if(borrower.getName().equals(productImportShulou.getBorrower())&&borrower.getCertifNumber().equals(productImportShulou.getIdNumber())){
                    loanBorrower =  createLoanBorrower(borrower);
                    break;
                }
            };

            //检查借款人是否存在 ,如果不存在则创建
            if(loanBorrower==null){

                //创建借款人
                Borrower borrower = new Borrower();
                borrower.setName(productImportShulou.getBorrower());
                borrower.setCertifNumber(productImportShulou.getIdNumber());
                borrower.setCertifType(LoanerCertifType.ID);
                borrower.setStatus(PublicStatus.ABLE);
                borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                borrower.setCreateTime(new Date());
                Borrower borrowerSaved  = borrowerService.add(borrower);
                borrowerList.add(borrowerSaved);

                //创建借款人和贷款的关联表
                loanBorrower =  createLoanBorrower(borrowerSaved);

            }

            if(StringUtils.isEmpty(productImportShulou.getExcludeMsg())){//如果导出信息为空，则符合导出条件。则保存load 获取id复制给借款人，保存借款人，拿到借款人ID复制给loan，然后在更新loan，改变状态为已经执行成功
               // Loan saveLoan =  loanService.add(loan);
                Loan saveLoan = dao().insert(loan);
                loanBorrower.setLoanId(saveLoan.getId());
                saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);
                //更新完后创建费用模板
                ProductFee productFee = productFeeService.fetch(Cnd.where("productId","=",saveLoan.getProductId()).and("feeType","=",FeeType.PREPAYMENT_FEE_RATE));
                LoanFeeTemp loanFeeTemp = new LoanFeeTemp(productFee);
                loanFeeTemp.setCreateBy(ShiroSession.getLoginUser().getName());
                loanFeeTemp.setCreateTime(new Date());
                loanFeeTemp.setUpdateTime(new Date());
                loanFeeTemp.setUpdateTime(new Date());
                loanFeeTemp.setLoanId(saveLoan.getId());
                //前端输入，这里设置为费用值
                loanFeeTemp.setFeeAmount(productImportShulou.getCostAmount());
                loanFeeTempService.add(loanFeeTemp);

                //更新完了后，再创建loanRepay对象
                List<LoanRepay> loanRepayList = createLoanRepayList(saveLoan ,productImportShulou);

                loanRepayList.stream().forEach(loanRepay -> {
                    //保存还款计划
                    LoanRepay loanRepaySaved = dao().insert(loanRepay);
                    LoanFee loanFee = loanRepay.getLoanFeeList().get(0);

                    //保存还款计划费用
                    loanFee.setRepayId(loanRepaySaved.getId());
                    LoanFee loanFeeSaved =  dao().insert(loanFee);

                    //保存还款记录
                    if(loanRepay.getLoanRepayRecordList()!=null)for(LoanRepayRecord loanRepayRecord  : loanRepay.getLoanRepayRecordList()){
                        loanRepayRecord.setRepayId(loanRepaySaved.getId());
                        LoanRepayRecord loanRepayRecordSaved = dao().insert(loanRepayRecord);
                        //保存还款记录里面的费用记录
                        if(loanRepayRecord.getLoanFeeRecordList()!=null)for(LoanFeeRecord loanFeeRecord : loanRepayRecord.getLoanFeeRecordList()){
                            loanFeeRecord.setLoanFeeId(loanFeeSaved.getId());
                            loanFeeRecord.setRepayRecordId(loanRepayRecordSaved.getId());
                            loanFeeRecord.setRepayId(loanRepaySaved.getId());
                            dao().insert(loanFeeRecord);
                        }
                    }
                });
                //设置为成功，并且关联好loanId以便查询
                productImportShulou.setExcludeStatus("01");
                productImportShulou.setLoanId(saveLoan.getId());
                result.put("success",result.get("success")+1);
            }else{//如果不为空，则改变状态为02失败
                productImportShulou.setExcludeStatus("02");
                result.put("failure",result.get("failure")+1);
            }
            productImportShulou.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportShulou.setUpdateTime(new Date());
            dao().update(productImportShulou);
        });
    }

    /**
     * 处理数据--赎楼
     * */
    public Map<String,Integer> excludeShulouById(String id){
        ProductType productType = productTypeService.fetchByName("赎楼贷");
        Product productsz = productService.fetch(Cnd.where("name","=","非交易性赎楼-深圳").and("status","=", PublicStatus.ABLE));
        Product productgz = productService.fetch(Cnd.where("name","=","非交易性赎楼-广州").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();//Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportShulou productImportShulou = dao().fetch(ProductImportShulou.class,id);
        excludeShulou(result, productType, productsz, productgz,borrowerList, subjectList, businessUserList,channelList, productImportShulou);
        return result;
    }

    private BigDecimal getDuration(Date end, Date start, boolean addOne){
        return new BigDecimal( (int)(((end.getTime()-start.getTime()))/(1000*24*3600))+(addOne?1:0));
    }
    private BigDecimal getDuration(ProductImportShulou productImportShulou){
        return  getDuration(productImportShulou.getExpireDate(),productImportShulou.getLoanDate(),productImportShulou.getChannel().equals("人人聚财")?false:true);
    }
    private List<LoanRepay> createLoanRepayList(Loan loan,ProductImportShulou productImportShulou){
        ProductFee productFee = productFeeService.fetch(Cnd.where("productId","=",loan.getProductId()).and("feeType","=",FeeType.PREPAYMENT_FEE_RATE));
        List<LoanRepay> loanRepayList = new ArrayList<LoanRepay>();
        if("先息后本".equals(productImportShulou.getRepaymentMethod())){
            LoanRepay fisrtLoanPepay =  createLoanRepayFirst(loan ,productImportShulou);

            //第一期肯定会有一条还款记录  还款日期为当天
            LoanRepayRecord loanRepayRecord  = getLoanRepayRecord(fisrtLoanPepay,productImportShulou);
            loanRepayRecord.setRepayDate(productImportShulou.getLoanDate());
            LoanFee loanFee = getLoanFee(fisrtLoanPepay,productImportShulou,productFee);

            //第二期
            LoanRepay secondLoanRepay = createLoanRepaySecond(loan ,productImportShulou);
            LoanFee loanFee1 = getLoanFee(secondLoanRepay,productImportShulou,productFee);
            //判断是否已经还清，如果还清了生成还款记录，还款日期为结清日   还款计划也要把还款日期修改为结清日期
            if("结清".equals(productImportShulou.getRepaymentStatus())){
                secondLoanRepay.setRepayDate(productImportShulou.getSettleDate());
                LoanRepayRecord loanRepayRecord1  = getLoanRepayRecord(secondLoanRepay,productImportShulou);
                loanRepayRecord1.setRepayDate(productImportShulou.getSettleDate());

            }
            loanRepayList.add(fisrtLoanPepay);
            loanRepayList.add(secondLoanRepay);
        }else if("一次性还本付息".equals(productImportShulou.getRepaymentMethod())){
            LoanRepay loanRepay = createLoanRepay(loan,productImportShulou);
            LoanFee loanFee = getLoanFee(loanRepay,productImportShulou,productFee);
            loanRepayList.add(loanRepay);
            //判断是否已经还清，如果还清了生成还款记录，还款日期为结清日 还款计划也要把还款日期修改为结清日期
            if("结清".equals(productImportShulou.getRepaymentStatus())){
                loanRepay.setRepayDate(productImportShulou.getSettleDate());
                LoanRepayRecord loanRepayRecord =  getLoanRepayRecord(loanRepay,productImportShulou);

            }

        }
        return loanRepayList;
    }
    private LoanRepayRecord getLoanRepayRecord(LoanRepay loanRepay,ProductImportShulou productImportShulou){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayAmount(loanRepay.getRepayAmount());
        loanRepayRecord.setRepayDate(productImportShulou.getSettleDate());
        loanRepayRecord.setRepayInterest(loanRepay.getRepayInterest());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()));
        loanRepayRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepay.addLoanRepayRecord(loanRepayRecord);
        //有还款记录就会有费用记录
        getLoanFeeRecord(loanRepay,loanRepayRecord);
        return loanRepayRecord;
    }
    private LoanFeeRecord getLoanFeeRecord(LoanRepay loanRepay,LoanRepayRecord loanRepayRecord){
        LoanFeeRecord loanFeeRecord = new LoanFeeRecord();
        loanFeeRecord.setFeeAmount(loanRepay.getFeeAmount());
        loanFeeRecord.setRepayFeeAmount(loanRepay.getRepayFeeAmount());
        loanFeeRecord.setCreateTime(new Date());
        loanFeeRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.addLoanFeeRecord(loanFeeRecord);
        return loanFeeRecord;
    }
    private LoanFee getLoanFee(LoanRepay loanRepay,ProductImportShulou productImportShulou,ProductFee productFee){
        LoanFee loanFee = new LoanFee();
        loanFee.setFeeId(productFee.getId());
        loanFee.setLoanId(loanRepay.getLoanId());
        loanFee.setFeeName(productImportShulou.getCostName());
        loanFee.setPeriod(loanRepay.getPeriod());
        loanFee.setFeeAmount(loanRepay.getFeeAmount());
        loanFee.setRepayFeeAmount(loanRepay.getRepayFeeAmount());
        loanFee.setRepayDate(loanRepay.getRepayDate());
        loanFee.setDueDate(loanRepay.getDueDate());
        loanFee.setStatus(loanRepay.getStatus());
        loanFee.setChargeNode(FeeChargeNode.REPAY_NODE);
        loanFee.setFeeType(FeeType.PREPAYMENT_FEE_RATE);
        loanFee.setCreateTime(new Date());
        loanFee.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepay.addLoanFee(loanFee);
        return loanFee;
    }
    //一次性还本付息 ,本金所有，利息所有，费用所有
    private LoanRepay createLoanRepay(Loan loan,ProductImportShulou productImportShulou){

        LoanRepay loanRepay = new LoanRepay();
        loanRepay.setDueDate(productImportShulou.getExpireDate());
        loanRepay.setAmount(productImportShulou.getLoanPrincipal());
        loanRepay.setFeeAmount(productImportShulou.getCostAmount());
        loanRepay.setInterest(productImportShulou.getLoanPrincipal().multiply(productImportShulou.getDayRate()).multiply( getDuration(productImportShulou)) );
        loanRepay.setLoanId(loan.getId());
        loanRepay.setPeriod(1);
        loanRepay.setRemark("一次性还本付息共一期：第一期");
        if("还款中".equals(productImportShulou.getRepaymentStatus())){
            loanRepay.setStatus(LoanRepayStatus.LOANED);
            loanRepay.setOutstanding(productImportShulou.getLoanPrincipal());
            loanRepay.setRepayAmount(new BigDecimal(0));
            loanRepay.setRepayFeeAmount(new BigDecimal(0));
            loanRepay.setRepayInterest(new BigDecimal(0));
        }else if("结清".equals(productImportShulou.getRepaymentStatus())){
            loanRepay.setStatus(LoanRepayStatus.CLEARED);
            loanRepay.setOutstanding(new BigDecimal(0));
            loanRepay.setRepayAmount(productImportShulou.getLoanPrincipal());
            loanRepay.setRepayFeeAmount(productImportShulou.getCostAmount());
            if(productImportShulou.getSettleInterest().doubleValue()!=0.0){
                loanRepay.setRepayInterest(productImportShulou.getSettleInterest());
            }else{
                loanRepay.setRepayInterest(loanRepay.getInterest());
            }
            loanRepay.setRepayDate(productImportShulou.getLoanDate());
        }
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepay.setCreateTime(new Date());
        return loanRepay;
    }
    //先息后本第一期，本金为0，利息为所有，费用为0 ，还款到期时间为当天
    private LoanRepay createLoanRepayFirst(Loan loan,ProductImportShulou productImportShulou ){
        LoanRepay loanRepay = createLoanRepay(loan,productImportShulou);
        loanRepay.setDueDate(productImportShulou.getLoanDate());
        loanRepay.setRemark("先息后本共两期：第一期");
        loanRepay.setOutstanding(productImportShulou.getLoanPrincipal());
        loanRepay.setFeeAmount(new BigDecimal(0));
        loanRepay.setAmount(new BigDecimal(0));
        loanRepay.setRepayFeeAmount(new BigDecimal(0));
        loanRepay.setRepayAmount(new BigDecimal(0));
        //先息后本，第一期利息肯定交了 ,如果没有填写结清利息，则以到期日-放款日*天利率来得出利息*本金
        loanRepay.setRepayInterest((productImportShulou.getSettleInterest()!=null&&productImportShulou.getSettleInterest().doubleValue()!=0.0)?productImportShulou.getSettleInterest():getDuration(productImportShulou.getExpireDate(),productImportShulou.getLoanDate(),true).multiply(productImportShulou.getDayRate().multiply(productImportShulou.getLoanPrincipal())));
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setStatus(LoanRepayStatus.CLEARED);
        return loanRepay;
    }
    //先息后本第二期，本金所有，费用所有，利息为0，还款时间为放款日的第二天
    private LoanRepay createLoanRepaySecond(Loan loan,ProductImportShulou productImportShulou ){
        LoanRepay loanRepay = createLoanRepay(loan,productImportShulou);
        loanRepay.setRepayDate(new Date(productImportShulou.getLoanDate().getTime()+24*3600*1000));
        loanRepay.setPeriod(2);
        loanRepay.setRemark("先息后本共两期：第二期");
        loanRepay.setInterest(new BigDecimal(0));
        loanRepay.setRepayInterest(new BigDecimal(0));
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        return loanRepay;
    }

    //判断还款方式，如果找不到还款方式，则输出错误信息
    private LoanRepayMethod getRepayMethod(String repayMethod){
        if("一次性还本付息".equals(repayMethod)){
            return LoanRepayMethod.BULLET_REPAYMENT;
        }else if("先息后本".equals(repayMethod)){
            return LoanRepayMethod.INTEREST_DAYS;
        }
        return null;
    }
}
