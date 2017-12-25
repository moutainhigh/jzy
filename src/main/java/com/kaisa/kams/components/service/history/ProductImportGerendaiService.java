package com.kaisa.kams.components.service.history;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BorrowerService;
import com.kaisa.kams.components.service.BusinessUserService;
import com.kaisa.kams.components.service.ChannelService;
import com.kaisa.kams.components.service.LoanBorrowerService;
import com.kaisa.kams.components.service.LoanRepayRecordService;
import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.LoanSubjectAccountService;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.ProductFeeService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.components.utils.excelUtil.ObjectUtil;
import com.kaisa.kams.components.utils.excelUtil.ReadExcelUtil;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.components.view.loan.NumberConstant;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.business.BusinessUser;
import com.kaisa.kams.models.history.ProductImportGerendai;
import com.kaisa.kams.models.history.ProductImportXinYongDai;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.upload.TempFile;
import org.nutz.service.IdNameEntityService;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.math.BigDecimal;
import java.util.*;

;import static com.kaisa.kams.components.view.loan.NumberConstant.MATH_CONTEXT;

/**
 * Created by lw on 2017/5/8.
 */
@IocBean(fields="dao")
public class ProductImportGerendaiService extends IdNameEntityService<ProductImportGerendai> {

    private static final BigDecimal RATE_FACTOR = new BigDecimal(NumberConstant.RATE_FACTOR);

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
                ProductImportGerendai productImport = null;
                try {
                    productImport = (ProductImportGerendai) ObjectUtil.fromMap(map,ProductImportGerendai.class);
                    productImport.dataConversion();
                } catch (Exception e) {
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                ProductImportGerendai productImportGerendai = new ProductImportGerendai();
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportGerendai exist = exsit(md5);
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

    //信用贷
    public String readXinyongExcelFile(TempFile mFile){
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
                ProductImportXinYongDai productImport = null;
                try {
                    productImport = (ProductImportXinYongDai) ObjectUtil.fromMap(map,ProductImportXinYongDai.class);
                    productImport.dataConversion();
                } catch (Exception e) {
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                ProductImportXinYongDai productImportXinYongDai = new ProductImportXinYongDai();
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportXinYongDai exist = xinyongdaiExsit(md5);
//                if(exist==null){
                    productImport.setLineNum(i+2);
                    productImport.setVersion(version);
                    productImport.setMd5(md5);
                    productImport.setCreateTime(new Date());
                    productImport.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
                    productImport.validata();
                    dao().insert(productImport);
//                }else{
//                    if(version.equals(exist.getVersion())){
//                        return  "导入数据失败，数据有重复，已存在的excel行为："+exist.getLineNum()+"，重复的excel行为："+(i+2);
//                    }else{
//                        //更新导入版本
//                        exist.setVersion(version);
//                        exist.setLineNum(i+2);
//                        dao().update(exist);
//                    }
//                }
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
    private ProductImportGerendai exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportGerendai.class, cnd);
    }

    //信用贷
    private ProductImportXinYongDai xinyongdaiExsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportXinYongDai.class, cnd);
    }




    /**
     * 获取产品列表
     * @return
     */
    public List<ProductImportGerendai> queryListAll(Cnd cnd) {
        return dao().query(ProductImportGerendai.class, cnd.orderBy().desc("createTime"));
    }

    /**
     * 获取产品列表-信用贷
     * @return
     */
    public List<ProductImportXinYongDai> queryXinyongdaiListAll(Cnd cnd) {
        return dao().query(ProductImportXinYongDai.class, cnd.orderBy().desc("createTime"));
    }


    /**
     * 获取产品列表分页形式
     * @return
     */
    public DataTables queryPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportGerendai> list = dao().query(ProductImportGerendai.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportGerendai.class),dao().count(ProductImportGerendai.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }

    /**
     * 获取产品列表分页形式-信用贷
     * @return
     */
    public DataTables queryXinyongdaiPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportXinYongDai> list = dao().query(ProductImportXinYongDai.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportXinYongDai.class),dao().count(ProductImportXinYongDai.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }

    /**
     * 批量处理
     * @return
     */
    public Map<String,Integer> excludeGerendaiByEntity(ParamData paramData){
        List<ProductImportGerendai> list = dao().query(ProductImportGerendai.class,paramData.getCnd());
        ProductType productType = productTypeService.fetchByName("信用贷");
        Product productygd = productService.fetch(Cnd.where("name","=","员工贷").and("status","=", PublicStatus.ABLE));
        Product productyzd = productService.fetch(Cnd.where("name","=","业主贷").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query(Cnd.where("1","=","1"));
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportGerendai productImportGerendai : list){
            excludeGerendai(result, productType, productygd, productyzd,borrowerList, subjectList, businessUserList,channelList, productImportGerendai);
        }
        return result;
    }

    /**
     * 批量处理-信用贷
     * @return
     */
    public Map<String,Integer> excludeXinyongdaiByEntity(ParamData paramData){
        List<ProductImportXinYongDai> list = dao().query(ProductImportXinYongDai.class,paramData.getCnd());
        ProductType productType = productTypeService.fetchByName("信用贷");
        Product productxsd = productService.fetch(Cnd.where("name","=","线上贷-渠道-达飞").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query(Cnd.where("1","=","1"));
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportXinYongDai productImportXinYongDai : list){
            excludeXinyongdai(result, productType, productxsd,borrowerList, subjectList, businessUserList,channelList, productImportXinYongDai);
        }
        return result;
    }

//    /**
//     * 批量处理信用贷
//     * @return
//     */
//    public Map<String,Integer> excludeXinyongdaiByEntity(ParamData paramData){
//        List<ProductImportGerendai> list = dao().query(ProductImportGerendai.class,paramData.getCnd());
//        ProductType productType = productTypeService.fetchByName("信用贷");
//        Product productxsd = productService.fetch(Cnd.where("name","=","线上贷-渠道-达飞").and("status","=", PublicStatus.ABLE));
//        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
//        List<LoanSubject> subjectList = loanSubjectService.queryAble();
//        List<BusinessUser> businessUserList = businessUserService.query(Cnd.where("1","=","1"));
//        List<Channel> channelList = channelService.listAble();
//        Map<String,Integer> result = new HashMap<String,Integer>();
//        result.put("failure",0);
//        result.put("success",0);
//        for(ProductImportXinYongDai productImportXinYongDai : list){
//            excludeXinyongdai(result, productType, productxsd,borrowerList, subjectList, businessUserList,channelList, productImportXinYongDai);
//        }
//        return result;
//    }

    private LoanBorrower createLoanBorrower(Borrower borrower,boolean isMaster){
        LoanBorrower loanBorrower  = new LoanBorrower();
        loanBorrower.setName(borrower.getName());
        loanBorrower.setAddress(borrower.getAddress());
        loanBorrower.setBorrowerId(borrower.getId());
        loanBorrower.setCertifNumber(borrower.getCertifNumber());
        loanBorrower.setCertifType(borrower.getCertifType());
        loanBorrower.setMaster(isMaster);
        loanBorrower.setPhone(borrower.getPhone());
        loanBorrower.setStatus(borrower.getStatus());
        loanBorrower.setCreateBy(String.valueOf(ShiroSession.getLoginUser().getId()));
        loanBorrower.setCreateTime(new Date());
        return loanBorrower;
    }
    private void excludeGerendai(Map<String,Integer> result ,ProductType productType,Product productygd,Product productyzd,List<Borrower> borrowerList,List<LoanSubject> subjectList,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportGerendai productImportGerendai){
        //导入失败的，已经执行成功的都不能执行了
        if(productImportGerendai.getImportStatus().equals("01")||productImportGerendai.getExcludeStatus().equals("01"))return;
        Trans.exec((Atom) () -> {
            //一开始应该吧执行错误信息设置为空
            productImportGerendai.setExcludeMsg("");
            productImportGerendai.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportGerendai.setUpdateTime(new Date());
            dao().update(productImportGerendai);
            Loan loan = new Loan();
            loan.setHistoryData("01");
            loan.setCreateBy(ShiroSession.getLoginUser().getName());
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            //loan.setProductType(productType);
            if(null != productType){
                loan.setProductTypeId(productType.getId());
            }else {
                productImportGerendai.addExcludeMsg("找不到信用贷产品大类");
            }

            String prdCode ="";
            String fixCode= "";
            if("员工贷".equals(productImportGerendai.getProductName())){
                if(null !=productygd){
                    loan.setProductId(productygd.getId());
                    //loan.setProduct(productgz);
                    //获取产品业务单号
                    prdCode = productygd.getCode();
                    //订单后8位修正编号
                    fixCode = loanService.fetchMaxCode(productygd.getId());
                }else {
                    productImportGerendai.addExcludeMsg("找不到对应员工贷的产品");
                }

            }else{
                if(null !=productyzd) {
                    loan.setProductId(productyzd.getId());
                    //loan.setProduct(productsz);
                    //获取产品业务单号
                    prdCode = productyzd.getCode();
                    //订单后8位修正编号
                    fixCode = loanService.fetchMaxCode(productyzd.getId());
                }else {
                    productImportGerendai.addExcludeMsg("找不到对应业主贷的产品");
                }
            }
            //检查产品是否存在
//            if(StringUtils.isEmpty(loan.getProductId())){
//                productImportGerendai.addExcludeMsg("找不到对应的产品");
//            }
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //遇到富昌小贷都得找深圳富昌小额贷款有限公司  放款账号默认为第一个
            subjectList.stream().forEach(loanSubject -> {
                if("富昌".equals(productImportGerendai.getLoanSubject())){
                    if("深圳市富昌小额贷款有限公司".equals(loanSubject.getName())){
                        loan.setLoanSubjectId(loanSubject.getId());
                        LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
                        if(loanSubjectAccount!=null)
                            loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                        return;
                    }
                }else{
                    if(loanSubject.getName().equals(productImportGerendai.getLoanSubject())){
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
                productImportGerendai.addExcludeMsg("放款主体不存在");
            }
            loan.setAmount(productImportGerendai.getLoanPrincipal());
            loan.setActualAmount(productImportGerendai.getLoanPrincipal());
            loan.setRepayMethod(getRepayMethod(productImportGerendai.getRepaymentMethod(),productImportGerendai.getLoanTermUnit()));
            if(loan.getRepayMethod()==null){
                productImportGerendai.addExcludeMsg("还款方式不存在");
            }else{
                //一次性就是期末  先息后本就是期初
                if(loan.getRepayMethod().getCode().equals("3300")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }else if(loan.getRepayMethod().getCode().equals("3303")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }
            }
            //借款期限是否存在
            if(StringUtils.isNotEmpty(productImportGerendai.getLoanTerm())){
                if(productImportGerendai.getLoanTermUnit().equals("天")){
                    loan.setTermType(LoanTermType.DAYS);
                    loan.setTerm(productImportGerendai.getLoanTermNum().intValue()+"");
                    if("年".equals(productImportGerendai.getBorrowRateUnit())) {
                        loan.setInterestRate(productImportGerendai.getBorrowRateNum().divide(new BigDecimal(3.65), 10, BigDecimal.ROUND_HALF_EVEN));
                    }

                }else if(productImportGerendai.getLoanTermUnit().equals("月")){
                    loan.setTermType(LoanTermType.MOTHS);
                    loan.setTerm(productImportGerendai.getLoanTermNum().intValue()+"");
                    loan.setInterestRate(productImportGerendai.getBorrowRateNum().divide(new BigDecimal(0.12), 10, BigDecimal.ROUND_HALF_EVEN));
                }


            }else{
                productImportGerendai.addExcludeMsg("借款期限不存在");
            }
            loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
            loan.setLoanTime(productImportGerendai.getStartDate());
            //利息类型
            loan.setLoanLimitType(LoanLimitType.FIX_RATE);
            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");
            if("还款中".equals(productImportGerendai.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.LOANED);
                if(productImportGerendai.getExpireDate().getTime()<=new Date().getTime()){
                    loan.setLoanStatus(LoanStatus.CLEARED);
                }
            }else if("已还清".equals(productImportGerendai.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.CLEARED);
            }
            //判断还款状态
            if(null==loan.getLoanStatus()){
                productImportGerendai.addExcludeMsg("找不到对应还款状态");
            }
            businessUserList.stream().forEach(businessUser -> {
                if(businessUser.getName().equals(productImportGerendai.getBusinessName().trim())){
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    loan.setSaleName(businessUser.getName());
                    return ;
                }
            });
            //判断有没有业务员
            if(StringUtils.isEmpty(loan.getSaleId())){
                //productImportGerendai.addExcludeMsg("找不到对应的业务员");
                loan.setActualBusinessName(productImportGerendai.getBusinessName());
                businessUserList.stream().forEach(businessUser -> {
                    if("虚拟业务员".equals(businessUser.getName())){
                        loan.setSaleId(businessUser.getId());
                        loan.setSaleCode(businessUser.getCode());
                        loan.setSaleName(businessUser.getName());
                        return ;
                    }
                });
            }
            loan.setSubmitTime(productImportGerendai.getStartDate());
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportGerendai.getStartDate());

            //人人聚财 算头不算尾 其他算头算尾
//            if(productImportShulou.getChannel().equals("人人聚财")){
//                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
//            }else{
//                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL);
//            }
            //默认自营渠道
//            channelList.stream().forEach(channel -> {
//                if(channel.getName().equals("自营")){
//                     loan.setChannelId(channel.getId());
//                     return;
//                }
//            });

            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            List<LoanBorrower> loanBorrowers = new ArrayList<LoanBorrower>();
            String borrwoers = productImportGerendai.getBorrower();
            String idNumbers = productImportGerendai.getIdNumber();
            String remainborrwoers=productImportGerendai.getBorrower();
            String remainidNumbers = productImportGerendai.getIdNumber();
            for(int i=0;i<borrwoers.split(",").length;i++ ){
                String borrowerName  = borrwoers.split(",")[i];
                String idNumber = productImportGerendai.getIdNumber().split(",")[i];
                if(StringUtils.isEmpty(borrowerName)||StringUtils.isEmpty(idNumber)){
                    continue;
                }
                boolean isNotExist = true;
                for(Borrower borrower : borrowerList){
                    if(borrower.getName().equals(borrowerName)&&borrower.getCertifNumber().equals(idNumber)){
                        remainborrwoers =  remainborrwoers.replace(borrowerName,"");
                        remainidNumbers =  remainidNumbers.replace(idNumber,"");
                        loanBorrowers.add( createLoanBorrower(borrower ,i==0));
                        isNotExist = false;
                        break;
                    }
                };

                if(isNotExist){
                    //创建借款人
                    Borrower borrower = new Borrower();
                    borrower.setName(borrowerName);
                    borrower.setCertifNumber(idNumber);
                    borrower.setCertifType(LoanerCertifType.ID);
                    borrower.setStatus(PublicStatus.ABLE);
                    borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                    borrower.setCreateTime(new Date());
                    Borrower borrowerSaved  = borrowerService.add(borrower);
                    //更新到借款人信息里面，避免重复创建
                    borrowerList.add(borrowerSaved);

                    //创建借款人和贷款的关联表，这里去第一个借款人为主要借款人
                    loanBorrowers.add(createLoanBorrower(borrowerSaved,i==0));
                }
            }

            if(StringUtils.isEmpty(productImportGerendai.getExcludeMsg())){ //如果导出信息为空，则符合导出条件。则保存load 获取id复制给借款人，保存借款人，拿到借款人ID复制给loan，然后在更新loan，改变状态为已经执行成功
                Loan saveLoan =  loanService.add(loan);
                for(LoanBorrower loanBorrower : loanBorrowers){
                    loanBorrower.setLoanId(saveLoan.getId());
                    saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                }
                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);
                //更新完了后，再创建loanRepay对象
                List<LoanRepay> loanRepayList = createLoanRepayList(saveLoan ,productImportGerendai);

                loanRepayList.stream().forEach(loanRepay -> {
                    //保存还款计划
                    LoanRepay loanRepaySaved = dao().insert(loanRepay);
                    //LoanFee loanFee = loanRepay.getLoanFeeList().get(0);
                    //保存还款计划费用
                    //loanFee.setRepayId(loanRepaySaved.getId());
                    //LoanFee loanFeeSaved =  dao().insert(loanFee);
                    if(LoanStatus.CLEARED.equals(loan.getLoanStatus()) ||(loanRepay.getDueDate().getTime()<=new Date().getTime())){
                        loanRepay.setRepayDate(productImportGerendai.getExpireDate());
                        LoanRepayRecord loanRepayRecord1  = getLoanRepayRecord(loanRepay,productImportGerendai);
                        loanRepayRecord1.setRepayDate(loanRepay.getDueDate());
                    }

                    //保存还款记录
                    if(loanRepay.getLoanRepayRecordList()!=null)for(LoanRepayRecord loanRepayRecord  : loanRepay.getLoanRepayRecordList()){
                        loanRepayRecord.setRepayId(loanRepaySaved.getId());
                        LoanRepayRecord loanRepayRecordSaved = dao().insert(loanRepayRecord);
                        //保存还款记录里面的费用记录
                        if(loanRepayRecord.getLoanFeeRecordList()!=null)for(LoanFeeRecord loanFeeRecord : loanRepayRecord.getLoanFeeRecordList()){
                            //loanFeeRecord.setLoanFeeId(loanFeeSaved.getId());
                            loanFeeRecord.setRepayRecordId(loanRepayRecordSaved.getId());
                            loanFeeRecord.setRepayId(loanRepaySaved.getId());
                            dao().insert(loanFeeRecord);
                        }
                    }
                });
                //设置为成功，并且关联好loanId以便查询
                productImportGerendai.setExcludeStatus("01");
                productImportGerendai.setLoanId(saveLoan.getId());
                result.put("success",result.get("success")+1);
            }else{//如果不为空，则改变状态为02失败
                productImportGerendai.setExcludeStatus("02");
                result.put("failure",result.get("failure")+1);
            }
            productImportGerendai.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportGerendai.setUpdateTime(new Date());
            dao().update(productImportGerendai);
        });
    }
    //信用贷
    private void excludeXinyongdai(Map<String,Integer> result ,ProductType productType,Product productxsd,List<Borrower> borrowerList,List<LoanSubject> subjectList,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportXinYongDai productImportXinYongDai){
        //导入失败的，已经执行成功的都不能执行了
        if(productImportXinYongDai.getImportStatus().equals("01")||productImportXinYongDai.getExcludeStatus().equals("01"))return;
        Trans.exec((Atom) () -> {
            //一开始应该吧执行错误信息设置为空
            productImportXinYongDai.setExcludeMsg("");
            productImportXinYongDai.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportXinYongDai.setUpdateTime(new Date());
            dao().update(productImportXinYongDai);
            Loan loan = new Loan();
            loan.setHistoryData("01");
            loan.setCreateBy(ShiroSession.getLoginUser().getName());
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            //loan.setProductType(productType);
            if(null != productType){
                loan.setProductTypeId(productType.getId());
            }else {
                productImportXinYongDai.addExcludeMsg("找不到信用贷产品大类");
            }

            String prdCode ="";
            String fixCode= "";
            if("消费贷".equals(productImportXinYongDai.getProductName())){
                if(null !=productxsd){
                    loan.setProductId(productxsd.getId());
                    //loan.setProduct(productgz);
                    //获取产品业务单号
                    prdCode = productxsd.getCode();
                    //订单后8位修正编号
                    fixCode = loanService.fetchMaxCode(productxsd.getId());
                }else {
                    productImportXinYongDai.addExcludeMsg("找不到对应信用贷的产品");
                }

            }
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //遇到富昌小贷都得找深圳富昌小额贷款有限公司  放款账号默认为第一个
            subjectList.stream().forEach(loanSubject -> {
                if("佳兆业金服".equals(productImportXinYongDai.getLoanSubject())){
                    if("佳兆业金服".equals(loanSubject.getName())){
                        loan.setLoanSubjectId(loanSubject.getId());
                        LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
                        if(loanSubjectAccount!=null)
                            loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                        return;
                    }
                }else{
                    if(loanSubject.getName().equals(productImportXinYongDai.getLoanSubject())){
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
                productImportXinYongDai.addExcludeMsg("放款主体不存在");
            }
            loan.setAmount(productImportXinYongDai.getLoanPrincipal());
            loan.setActualAmount(productImportXinYongDai.getLoanPrincipal());
            loan.setRepayMethod(getXinyongRepayMethod(productImportXinYongDai.getRepaymentMethod()));
            if(loan.getRepayMethod()==null){
                productImportXinYongDai.addExcludeMsg("还款方式不存在");
            }else{
                //一次性就是期末  先息后本就是期初
                if(loan.getRepayMethod().getCode().equals("3300")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }else if(loan.getRepayMethod().getCode().equals("3303")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }
            }
            //借款期限是否存在
            if(StringUtils.isNotEmpty(productImportXinYongDai.getLoanTerm().toString())){
               if(productImportXinYongDai.getLoanTermUnit().equals("月")){
                    loan.setTermType(LoanTermType.MOTHS);
                    loan.setTerm(productImportXinYongDai.getLoanTermNum().intValue()+"");
                    loan.setInterestRate(productImportXinYongDai.getBorrowRateNum().multiply(new BigDecimal(100)));
                }


            }else{
                productImportXinYongDai.addExcludeMsg("借款期限不存在");
            }
            loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
            loan.setLoanTime(productImportXinYongDai.getStartDate());
            //利息类型
            loan.setLoanLimitType(LoanLimitType.FIX_RATE);
            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");
             if("结清".equals(productImportXinYongDai.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.CLEARED);
            }
            //判断还款状态
            if(null==loan.getLoanStatus()){
                productImportXinYongDai.addExcludeMsg("找不到对应还款状态");
            }
            businessUserList.stream().forEach(businessUser -> {
                if(businessUser.getName().equals(productImportXinYongDai.getBusinessName().trim())){
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    loan.setSaleName(businessUser.getName());
                    return ;
                }
            });
            //判断有没有业务员
            if(StringUtils.isEmpty(loan.getSaleId())){
                //productImportGerendai.addExcludeMsg("找不到对应的业务员");
                loan.setActualBusinessName(productImportXinYongDai.getBusinessName());
                businessUserList.stream().forEach(businessUser -> {
                    if("虚拟业务员".equals(businessUser.getName())){
                        loan.setSaleId(businessUser.getId());
                        loan.setSaleCode(businessUser.getCode());
                        loan.setSaleName(businessUser.getName());
                        return ;
                    }
                });
            }
            loan.setSubmitTime(productImportXinYongDai.getStartDate());
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportXinYongDai.getStartDate());

            //人人聚财 算头不算尾 其他算头算尾
//            if(productImportShulou.getChannel().equals("人人聚财")){
//                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
//            }else{
//                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL);
//            }
            //默认自营渠道
            channelList.stream().forEach(channel -> {
                if(channel.getName().equals("达飞金控")){
                     loan.setChannelId(channel.getId());
                     return;
                }
            });

            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            List<LoanBorrower> loanBorrowers = new ArrayList<LoanBorrower>();
            String borrwoers = productImportXinYongDai.getBorrower();
            String idNumbers = productImportXinYongDai.getIdNumber();
            String remainborrwoers=productImportXinYongDai.getBorrower();
            String remainidNumbers = productImportXinYongDai.getIdNumber();
            for(int i=0;i<borrwoers.split(",").length;i++ ){
                String borrowerName  = borrwoers.split(",")[i];
                String idNumber = productImportXinYongDai.getIdNumber().split(",")[i];
                if(StringUtils.isEmpty(borrowerName)||StringUtils.isEmpty(idNumber)){
                    continue;
                }
                boolean isNotExist = true;
                for(Borrower borrower : borrowerList){
                    if(borrower.getName().equals(borrowerName)&&borrower.getCertifNumber().equals(idNumber)){
                        remainborrwoers =  remainborrwoers.replace(borrowerName,"");
                        remainidNumbers =  remainidNumbers.replace(idNumber,"");
                        loanBorrowers.add( createLoanBorrower(borrower ,i==0));
                        isNotExist = false;
                        break;
                    }
                };

                if(isNotExist){
                    //创建借款人
                    Borrower borrower = new Borrower();
                    borrower.setName(borrowerName);
                    borrower.setCertifNumber(idNumber);
                    borrower.setCertifType(LoanerCertifType.ID);
                    borrower.setStatus(PublicStatus.ABLE);
                    borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                    borrower.setCreateTime(new Date());
                    Borrower borrowerSaved  = borrowerService.add(borrower);
                    //更新到借款人信息里面，避免重复创建
                    borrowerList.add(borrowerSaved);

                    //创建借款人和贷款的关联表，这里去第一个借款人为主要借款人
                    loanBorrowers.add(createLoanBorrower(borrowerSaved,i==0));
                }
            }

            if(StringUtils.isEmpty(productImportXinYongDai.getExcludeMsg())){ //如果导出信息为空，则符合导出条件。则保存load 获取id复制给借款人，保存借款人，拿到借款人ID复制给loan，然后在更新loan，改变状态为已经执行成功
                Loan saveLoan =  loanService.add(loan);
                for(LoanBorrower loanBorrower : loanBorrowers){
                    loanBorrower.setLoanId(saveLoan.getId());
                    saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                }
                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);
                //更新完了后，再创建loanRepay对象
                List<LoanRepay> loanRepayList = createLoanRepayListForXYD(saveLoan ,productImportXinYongDai);

                loanRepayList.stream().forEach(loanRepay -> {
                    //保存还款计划
                    LoanRepay loanRepaySaved = dao().insert(loanRepay);
                    //LoanFee loanFee = loanRepay.getLoanFeeList().get(0);
                    //保存还款计划费用
                    //loanFee.setRepayId(loanRepaySaved.getId());
                    //LoanFee loanFeeSaved =  dao().insert(loanFee);
                    if(LoanStatus.CLEARED.equals(loan.getLoanStatus()) ||(loanRepay.getDueDate().getTime()<=new Date().getTime())){
                        //loanRepay.setRepayDate(productImportXinYongDai.getExpireDate());
                        LoanRepayRecord loanRepayRecord1  = getLoanRepayRecordForXYD(loanRepay,productImportXinYongDai);
                        loanRepayRecord1.setRepayDate(loanRepay.getDueDate());
                    }

                    //保存还款记录
                    if(loanRepay.getLoanRepayRecordList()!=null)for(LoanRepayRecord loanRepayRecord  : loanRepay.getLoanRepayRecordList()){
                        loanRepayRecord.setRepayId(loanRepaySaved.getId());
                        LoanRepayRecord loanRepayRecordSaved = dao().insert(loanRepayRecord);
                        //保存还款记录里面的费用记录
                        if(loanRepayRecord.getLoanFeeRecordList()!=null)for(LoanFeeRecord loanFeeRecord : loanRepayRecord.getLoanFeeRecordList()){
                            //loanFeeRecord.setLoanFeeId(loanFeeSaved.getId());
                            loanFeeRecord.setRepayRecordId(loanRepayRecordSaved.getId());
                            loanFeeRecord.setRepayId(loanRepaySaved.getId());
                            dao().insert(loanFeeRecord);
                        }
                    }
                });
                //设置为成功，并且关联好loanId以便查询
                productImportXinYongDai.setExcludeStatus("01");
                productImportXinYongDai.setLoanId(saveLoan.getId());
                result.put("success",result.get("success")+1);
            }else{//如果不为空，则改变状态为02失败
                productImportXinYongDai.setExcludeStatus("02");
                result.put("failure",result.get("failure")+1);
            }
            productImportXinYongDai.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportXinYongDai.setUpdateTime(new Date());
            dao().update(productImportXinYongDai);
        });
    }

    /**
     * 处理数据--赎楼
     * */
    public Map<String,Integer> excludeGerendaiById(String id){
        ProductType productType = productTypeService.fetchByName("信用贷");
        Product productygd = productService.fetch(Cnd.where("name","=","员工贷").and("status","=", PublicStatus.ABLE));
        Product productyzd = productService.fetch(Cnd.where("name","=","业主贷").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query(Cnd.where("1","=","1"));
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportGerendai productImportGerendai = dao().fetch(ProductImportGerendai.class,id);
        excludeGerendai(result, productType, productygd, productyzd,borrowerList, subjectList, businessUserList,channelList, productImportGerendai);
        return result;
    }

    /**
     * 处理数据--信用贷
     * */
    public Map<String,Integer> excludeXinyongdaiById(String id){
        ProductType productType = productTypeService.fetchByName("信用贷");
        Product productxsd = productService.fetch(Cnd.where("name","=","线上贷-渠道-达飞").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query(Cnd.where("1","=","1"));
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportXinYongDai productImportXinYongDai = dao().fetch(ProductImportXinYongDai.class,id);
        excludeXinyongdai(result, productType, productxsd,borrowerList, subjectList, businessUserList,channelList, productImportXinYongDai);
        return result;
    }

    private BigDecimal getDuration(Date end, Date start){
        return new BigDecimal( (int)(((end.getTime()-start.getTime()))/(1000*24*3600)));
    }
    private BigDecimal getDuration(ProductImportGerendai productImportGerendai){
        return  getDuration(productImportGerendai.getExpireDate(),productImportGerendai.getStartDate());
    }
    private List<LoanRepay> createLoanRepayList(Loan loan,ProductImportGerendai productImportGerendai){

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
        //List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId,FeeChargeNode.REPAY_NODE);
        //this.filterLoanFeeTemp(loanFeeTempList,loan);
        List<LoanRepay> cacltLoanRepayList = new ArrayList<>();
        //List<LoanFee> loanRepayFeeList = new ArrayList<>();
        Date now = new Date();
        //List<LoanRepay> finalLoanRepayList = loanRepayList;
        loanRepayPlan.getRepayments().stream().forEach(r->{
            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loan.getId());
            loanRepay.setPeriod(r.getPeriod());
            loanRepay.setAmount(r.getPrincipal());
            loanRepay.setInterest(r.getInterest());
            loanRepay.setOutstanding(r.getOutstanding());
            loanRepay.setDueDate(r.getDueDate());
            loanRepay.setRepayDate(r.getDueDate());
            BigDecimal feeAmount = BigDecimal.ZERO;
            loanRepay.setFeeAmount(feeAmount);
            loanRepay.setTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
            loanRepay.setCreateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setCreateTime(now);
            loanRepay.setUpdateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setUpdateTime(now);
            if(LoanStatus.CLEARED.equals(loan.getLoanStatus()) || (r.getDueDate().getTime()<=new Date().getTime())){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
                loanRepay.setRepayInterest(r.getInterest());
                loanRepay.setRepayAmount(r.getPrincipal());
                loanRepay.setRepayFeeAmount(feeAmount);
                loanRepay.setRepayTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
                if(null != productImportGerendai.getDescription() && productImportGerendai.getDescription().contains("展期") && r.getDueDate().equals(productImportGerendai.getExpireDate())){
                    loanRepay.setStatus(LoanRepayStatus.LOANED);
                }
            }else {
                loanRepay.setStatus(LoanRepayStatus.LOANED);
            }

            cacltLoanRepayList.add(loanRepay);
        });
        return cacltLoanRepayList;
    }

    //信用贷
    private List<LoanRepay> createLoanRepayListForXYD(Loan loan,ProductImportXinYongDai productImportXinYongDai){

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
        //List<LoanFeeTemp> loanFeeTempList = loanFeeTempService.queryByLoanIdAndChargeNode(loanId,FeeChargeNode.REPAY_NODE);
        //this.filterLoanFeeTemp(loanFeeTempList,loan);
        List<LoanRepay> cacltLoanRepayList = new ArrayList<>();
        //List<LoanFee> loanRepayFeeList = new ArrayList<>();
        Date now = new Date();
        //List<LoanRepay> finalLoanRepayList = loanRepayList;
        loanRepayPlan.getRepayments().stream().forEach(r->{
            LoanRepay loanRepay = new LoanRepay();
            loanRepay.setLoanId(loan.getId());
            loanRepay.setPeriod(r.getPeriod());
            loanRepay.setAmount(r.getPrincipal());
            loanRepay.setRepayAmount(r.getPrincipal());
            loanRepay.setInterest(r.getInterest());
            loanRepay.setRepayInterest(r.getInterest());
            loanRepay.setOutstanding(r.getOutstanding());
            loanRepay.setDueDate(r.getDueDate());
            loanRepay.setRepayDate(r.getDueDate());
            BigDecimal feeAmount = BigDecimal.ZERO;
            loanRepay.setFeeAmount(feeAmount);
            loanRepay.setTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
            loanRepay.setRepayTotalAmount(DecimalUtils.sumArr(r.getPrincipal(),r.getInterest(),loanRepay.getFeeAmount()));
            loanRepay.setCreateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setCreateTime(now);
            loanRepay.setUpdateBy(ShiroSession.getLoginUser().getName());
            loanRepay.setUpdateTime(now);
            if(LoanStatus.CLEARED.equals(loan.getLoanStatus()) || (r.getDueDate().getTime()<=new Date().getTime())){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
            }else {
                loanRepay.setStatus(LoanRepayStatus.LOANED);
            }

            cacltLoanRepayList.add(loanRepay);
        });
        return cacltLoanRepayList;
    }

    private LoanRepayRecord getLoanRepayRecord(LoanRepay loanRepay,ProductImportGerendai productImportGerendai){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayAmount(loanRepay.getAmount());
        loanRepayRecord.setRepayDate(loanRepay.getDueDate());
        loanRepayRecord.setRepayInterest(loanRepay.getInterest());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()));
        loanRepayRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepay.addLoanRepayRecord(loanRepayRecord);
        //有还款记录就会有费用记录
        //getLoanFeeRecord(loanRepay,loanRepayRecord);
        return loanRepayRecord;
    }

    private LoanRepayRecord getLoanRepayRecordForXYD(LoanRepay loanRepay,ProductImportXinYongDai productImportXinYongDai){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayAmount(loanRepay.getAmount());
        loanRepayRecord.setRepayDate(loanRepay.getDueDate());
        loanRepayRecord.setRepayInterest(loanRepay.getInterest());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()));
        loanRepayRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepay.addLoanRepayRecord(loanRepayRecord);
        //有还款记录就会有费用记录
        //getLoanFeeRecord(loanRepay,loanRepayRecord);
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
    private LoanFee getLoanFee(LoanRepay loanRepay,ProductImportGerendai productImportGerendai,ProductFee productFee){
        LoanFee loanFee = new LoanFee();
        loanFee.setFeeId(productFee.getId());
        loanFee.setLoanId(loanRepay.getLoanId());
        //loanFee.setFeeName(productImportGerendai.getCostName());
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
    private LoanRepay createLoanRepay(Loan loan,ProductImportGerendai productImportGerendai){

        LoanRepay loanRepay = new LoanRepay();
        loanRepay.setRepayDate(productImportGerendai.getStartDate());
        loanRepay.setDueDate(productImportGerendai.getExpireDate());
        loanRepay.setAmount(productImportGerendai.getLoanPrincipal());
        loanRepay.setFeeAmount(productImportGerendai.getCostAmount());
        if(productImportGerendai.getLoanTerm().contains("天")){
            loanRepay.setInterest(productImportGerendai.getLoanPrincipal().multiply(new BigDecimal(productImportGerendai.getBorrowRate().substring(0,productImportGerendai.getBorrowRate().length()-3)).divide(new BigDecimal(365*100),MATH_CONTEXT)).multiply( getDuration(productImportGerendai)) );
        }else if(productImportGerendai.getLoanTerm().contains("月")){
            loanRepay.setInterest(productImportGerendai.getLoanPrincipal().multiply(new BigDecimal(productImportGerendai.getBorrowRate().substring(0,productImportGerendai.getBorrowRate().length()-3)).divide(new BigDecimal(12*100),MATH_CONTEXT)).multiply( new BigDecimal(productImportGerendai.getLoanTerm().substring(0,productImportGerendai.getLoanTerm().length()-2))) );
        }

        loanRepay.setLoanId(loan.getId());
        loanRepay.setPeriod(1);
        loanRepay.setRemark("一次性还本付息共一期：第一期");
        if("还款中".equals(productImportGerendai.getRepaymentStatus())){
            loanRepay.setStatus(LoanRepayStatus.LOANED);
            loanRepay.setOutstanding(productImportGerendai.getLoanPrincipal());
            loanRepay.setRepayAmount(new BigDecimal(0));
            loanRepay.setRepayFeeAmount(new BigDecimal(0));
            loanRepay.setRepayInterest(new BigDecimal(0));
        }else if("已还清".equals(productImportGerendai.getRepaymentStatus())){
            loanRepay.setStatus(LoanRepayStatus.CLEARED);
            loanRepay.setOutstanding(new BigDecimal(0));
            loanRepay.setRepayAmount(productImportGerendai.getLoanPrincipal());
            loanRepay.setRepayFeeAmount(new BigDecimal(0));
            loanRepay.setRepayInterest(new BigDecimal(0));

        }
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount()==null?new BigDecimal(0):loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()==null?new BigDecimal(0):loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()==null?new BigDecimal(0):loanRepay.getFeeAmount()));
        loanRepay.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepay.setCreateTime(new Date());
        return loanRepay;
    }

    private String periodCN = "一二三四五六七八九十";
    private String getPeriodCN(int period){
        String perCN = "";
        int a  = period/10;
        int b  = period%10;
        if(a>0){
            perCN  += periodCN.charAt(a-1)+"十";
        }
        if(b>0){
            perCN += periodCN.charAt(b-1);
        }
        return perCN;
    }
    //先息后本第一期，本金为0，利息为所有，费用为0 ，还款到期时间为当天
    private LoanRepay createLoanRepayFirst(Loan loan,ProductImportGerendai productImportGerendai ,int period){


        LoanRepay loanRepay = createLoanRepay(loan,productImportGerendai);
        //loanRepay.setDueDate(productImport.getLoanDate());
        loanRepay.setDueDate(DateUtil.addMonth(productImportGerendai.getStartDate(),(period-1)));
        loanRepay.setPeriod(period);
        loanRepay.setRemark("先息后本共"+(productImportGerendai.getLoanTermNum().intValue()+1)+"期：第"+getPeriodCN(period)+"期");
        loanRepay.setOutstanding(productImportGerendai.getLoanPrincipal());
        loanRepay.setFeeAmount(new BigDecimal(0));
        loanRepay.setAmount(new BigDecimal(0));
        loanRepay.setRepayFeeAmount(new BigDecimal(0));
        loanRepay.setRepayAmount(new BigDecimal(0));
        //先息后本，第一期利息肯定交了 ,如果没有填写结清利息，则以到期日-放款日*天利率来得出利息*本金，利息等于一次性还清的利息
        //loanRepay.setRepayInterest(productImport.getSettleInterest()!=null?productImport.getSettleInterest():getDuration(productImport.getExpireDate(),productImport.getLoanDate(),true).multiply(productImport.getDayRate().multiply(productImport.getLoanPrincipal())));
        //如果是按天则为总期限*利率 ，如果是按月来算则是利率*1
        if(loan.getTermType().equals(LoanTermType.DAYS)){
            loanRepay.setInterest(productImportGerendai.getLoanPrincipal().multiply(productImportGerendai.getLoanTermNum()).multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            //如果是第一期，或者是已经结清，则利息为已还，否则为0
            if(period==1||LoanStatus.CLEARED.equals(loan.getLoanStatus())){
                loanRepay.setRepayInterest(productImportGerendai.getLoanPrincipal().multiply(loan.getInterestRate()).multiply(productImportGerendai.getLoanTermNum()).divide(new BigDecimal(100)));
            }else{
                loanRepay.setRepayInterest(new BigDecimal(0));
            }

        }else{
            loanRepay.setInterest(productImportGerendai.getLoanPrincipal().multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            if(period==1||LoanStatus.CLEARED.equals(loan.getLoanStatus())){
                loanRepay.setRepayInterest(productImportGerendai.getLoanPrincipal().multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            }else {
                loanRepay.setRepayInterest(new BigDecimal(0));
            }
        }
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        return loanRepay;
    }
    //先息后本第二期，本金所有，费用所有，利息为0，还款时间为放款日的第二天
    private LoanRepay createLoanRepaySecond(Loan loan,ProductImportGerendai productImportGerendai ,int period){
        LoanRepay loanRepay = createLoanRepay(loan,productImportGerendai);
        loanRepay.setRepayDate(new Date(productImportGerendai.getStartDate().getTime()+24*3600*1000));
        loanRepay.setPeriod(period);
        loanRepay.setRemark("先息后本共"+(productImportGerendai.getLoanTermNum().intValue()+1)+"期：第"+getPeriodCN(period)+"期");
        loanRepay.setInterest(new BigDecimal(0));
        loanRepay.setRepayInterest(new BigDecimal(0));
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));

        return loanRepay;
    }

    //判断还款方式，如果找不到还款方式，则输出错误信息
    private LoanRepayMethod getRepayMethod(String repayMethod){
       if("一次性还款-期末收息".equals(repayMethod)){
           return LoanRepayMethod.BULLET_REPAYMENT;
       }else if("先息后本-期末收息".equals(repayMethod)){
            return LoanRepayMethod.INTEREST_MONTHS;
       }else if("到期还本付息".equals(repayMethod)){
           return LoanRepayMethod.BULLET_REPAYMENT;
       }else if("等额本息".equals(repayMethod)){
           return  LoanRepayMethod.EQUAL_INSTALLMENT;
       }
       return null;
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
        }
        return null;
    }

    private LoanRepayMethod getXinyongRepayMethod(String repayMethod){
        if("一次性还本付息".equals(repayMethod)){
            return LoanRepayMethod.BULLET_REPAYMENT;
        }else if("先息后本-期末收息".equals(repayMethod)){
            return LoanRepayMethod.INTEREST_MONTHS;
        }else if("到期还本付息".equals(repayMethod)){
            return LoanRepayMethod.BULLET_REPAYMENT;
        }else if("等额本息".equals(repayMethod)){
            return  LoanRepayMethod.EQUAL_INSTALLMENT;
        }
        return null;
    }


    public NutMap deleteXYDById(String id){
        NutMap nutMap=new NutMap();
        int num=dao().delete(ProductImportXinYongDai.class,id);
        if(num>0){
            return nutMap.setv("ok",true).setv("msg","删除成功.");
        }
        return nutMap.setv("ok",false).setv("msg","删除失败.");
    }
}
