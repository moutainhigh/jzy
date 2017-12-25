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
import com.kaisa.kams.components.service.ProductInfoTmplService;
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
import com.kaisa.kams.models.history.ProductImportHongben;

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
public class ProductImportHongbenService extends IdNameEntityService<ProductImportHongben> {
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
    @Inject
    private ProductInfoTmplService productInfoTmplService;

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
                ProductImportHongben productImport = null;
                try {
                    productImport = (ProductImportHongben) ObjectUtil.fromMap(map,ProductImportHongben.class);
                    productImport.dataConversion();
                } catch (Exception e) {
                    e.printStackTrace();
                    return  "导入数据失败"+e.getMessage()+" 错误的excel行为："+(i+2);
                }
                //判断MD5值是不是已经存在
                String md5 = MD5Util.getMD5Code(productImport.toString());
                ProductImportHongben exist = exsit(md5);
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

    private ProductImportHongben exsit(String md5){
        Cnd cnd = Cnd.where("md5", "=", md5);
        return dao().fetch(ProductImportHongben.class, cnd);
    }


    /**
     * 获取产品列表分页形式
     * @return
     */
    public DataTables queryPage(ParamData paramData) {
        Pager pager = DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        List<ProductImportHongben> list = dao().query(ProductImportHongben.class,paramData.getCnd(),pager);
        DataTables dataTables =  new DataTables(paramData.getDraw(),dao().count(ProductImportHongben.class),dao().count(ProductImportHongben.class, paramData.getCnd()),list);
        dataTables.setOk(true);
        dataTables.setMsg("成功");
        return  dataTables;
    }


    /**
     * 批量处理
     * @return
     */
    public Map<String,Integer> excludeByEntity(ParamData paramData){
        ProductInfoTmpl productInfoTmplHongben = productInfoTmplService.fetch(Cnd.where("name","=","红本").and("status","=", PublicStatus.ABLE));
        List<ProductImportHongben> list = dao().query(ProductImportHongben.class,paramData.getCnd());
        ProductType productType = productTypeService.fetchByName("红本抵押");
        Product productsz = productService.fetch(Cnd.where("name","=","红本抵押-深圳").and("status","=", PublicStatus.ABLE));
        Product productgz = productService.fetch(Cnd.where("name","=","红本抵押-广州").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();//Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        for(ProductImportHongben productImport : list){
            exclude(result,productInfoTmplHongben, productType, productsz, productgz,borrowerList, subjectList, businessUserList,channelList, productImport);
        }
        return result;
    }

    /**
     * 处理数据--赎楼
     * */
    public Map<String,Integer> excludeById(String id){
        ProductInfoTmpl productInfoTmplHongben = productInfoTmplService.fetch(Cnd.where("name","=","红本").and("status","=", PublicStatus.ABLE));
        ProductType productType = productTypeService.fetchByName("红本抵押");
        Product productsz = productService.fetch(Cnd.where("name","=","红本抵押-深圳").and("status","=", PublicStatus.ABLE));
        Product productgz = productService.fetch(Cnd.where("name","=","红本抵押-广州").and("status","=", PublicStatus.ABLE));
        List<Borrower> borrowerList =borrowerService.query(Cnd.where("status","=",PublicStatus.ABLE));
        List<LoanSubject> subjectList = loanSubjectService.queryAble();
        List<BusinessUser> businessUserList = businessUserService.query();//Cnd.where("status","=",PublicStatus.ABLE)  去掉状态，离职的也算
        List<Channel> channelList = channelService.listAble();
        Map<String,Integer> result = new HashMap<String,Integer>();
        result.put("failure",0);
        result.put("success",0);
        ProductImportHongben productImportHongben = dao().fetch(ProductImportHongben.class,id);
        exclude(result,productInfoTmplHongben, productType, productsz, productgz,borrowerList, subjectList, businessUserList,channelList, productImportHongben);
        return result;
    }

    private void exclude(Map<String,Integer> result ,ProductInfoTmpl  productInfoTmplHongben,ProductType productType,Product productsz,Product productgz,List<Borrower> borrowerList,List<LoanSubject> subjectList,List<BusinessUser> businessUserList,List<Channel> channelList,ProductImportHongben productImportHongben){
        //导入失败的，已经执行成功的都不能执行了   品台募集的暂时也不能导入
        if(productImportHongben.getImportStatus().equals("01")||productImportHongben.getExcludeStatus().equals("01")||"平台募集".equals(productImportHongben.getLoanSubject()))return;
        Trans.exec((Atom) () -> {
            //一开始应该吧执行错误信息设置为空
            productImportHongben.setExcludeMsg("");
            productImportHongben.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportHongben.setUpdateTime(new Date());
            dao().update(productImportHongben);
            Loan loan = new Loan();
            loan.setHistoryData("01");
            loan.setCreateBy(ShiroSession.getLoginUser().getName());
            loan.setCreateTime(new Date());
            loan.setStatus(PublicStatus.ABLE);
            //loan.setProductType(productType);
            loan.setProductTypeId(productType.getId());
            String prdCode ="";
            String fixCode= "";
            if("广州自营".equals(productImportHongben.getChannel())){
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
            //检查产品是否存在
            if(StringUtils.isEmpty(loan.getProductId())){
                productImportHongben.addExcludeMsg("找不到对应的产品");
            }
            //生成业务单号
            String code = prdCode + fixCode;
            loan.setCode(code);
            //遇到富昌小贷都得找深圳富昌小额贷款有限公司  放款账号默认为第一个
            subjectList.stream().forEach(loanSubject -> {
                if("富昌".equals(productImportHongben.getLoanSubject())){
                    if("深圳富昌小额贷款有限公司".equals(loanSubject.getName())){
                        loan.setLoanSubjectId(loanSubject.getId());
                        LoanSubjectAccount loanSubjectAccount = loanSubjectAccountService.fetch(Cnd.where("subjectId","=",loanSubject.getId()));
                        if(loanSubjectAccount!=null)
                            loan.setLoanSubjectAccountId(loanSubjectAccount.getId());
                        return;
                    }
                }else{
                    if(loanSubject.getName().equals(productImportHongben.getLoanSubject())){
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
                productImportHongben.addExcludeMsg("放款主体不存在");
            }
            loan.setAmount(productImportHongben.getLoanPrincipal());
            loan.setActualAmount(productImportHongben.getLoanPrincipal());
            loan.setRepayMethod(getRepayMethod(productImportHongben.getRepaymentMethod(),productImportHongben.getLoanExtensiononUnit()));
            if(loan.getRepayMethod()==null){
                productImportHongben.addExcludeMsg("还款方式不存在");
            }else{
                //一次性就是期末  先息后本就是期初
                if(loan.getRepayMethod().getCode().equals("3301")||loan.getRepayMethod().getCode().equals("3300")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_PRE);
                }else if(loan.getRepayMethod().getCode().equals("3303")){
                    loan.setRepayDateType(LoanRepayDateType.REPAY_SUF);
                }
            }


            //利息类型  这里没有填写结清利息，如果是按天则需要把年/365转天保存
            loan.setLoanLimitType(LoanLimitType.FIX_RATE);
            if(productImportHongben.getLoanExtensiononUnit().equals("天")){
                loan.setTermType(LoanTermType.DAYS);
                if("年".equals(productImportHongben.getLoanRateUnit())){
                    loan.setInterestRate(productImportHongben.getLoanRateNum().divide(new BigDecimal(3.65),10,BigDecimal.ROUND_HALF_EVEN));
                }
            }else if(productImportHongben.getLoanExtensiononUnit().equals("月")){
                loan.setTermType(LoanTermType.MOTHS);
                if("年".equals(productImportHongben.getLoanRateUnit())){
                    loan.setInterestRate(productImportHongben.getLoanRateNum().divide(new BigDecimal(0.12),10,BigDecimal.ROUND_HALF_EVEN));
                }else if("月".equals(productImportHongben.getLoanRateUnit())){
                    loan.setInterestRate(productImportHongben.getLoanRateNum().multiply(new BigDecimal(100)));
                }
            }

            loan.setApproveStatus("C4--01");
            loan.setApproveStatusDesc("财务出纳审批--同意");
            if("还款中".equals(productImportHongben.getRepaymentStatus())){
                if(productImportHongben.getExpireDate().getTime()<=new Date().getTime()&&"00".equals(productImportHongben.getExtend())){
                    loan.setLoanStatus(LoanStatus.CLEARED);
                }else{
                    loan.setLoanStatus(LoanStatus.LOANED);
                }
            }else if("已还清".equals(productImportHongben.getRepaymentStatus())){
                loan.setLoanStatus(LoanStatus.CLEARED);
            }
            //判断还款状态
            if(null==loan.getLoanStatus()){
                productImportHongben.addExcludeMsg("找不到对应还款状态");
            }
            businessUserList.stream().forEach(businessUser -> {
                if(businessUser.getName().equals(productImportHongben.getBusinessName())){
                    loan.setSaleId(businessUser.getId());
                    loan.setSaleCode(businessUser.getCode());
                    loan.setSaleName(businessUser.getName());
                    return ;
                }
            });
            //判断有没有业务员
            if(StringUtils.isEmpty(loan.getSaleId())){
                //productImportHongben.addExcludeMsg("找不到对应的业务员");
                loan.setActualBusinessName(productImportHongben.getBusinessName());
                businessUserList.stream().forEach(businessUser -> {
                    if("虚拟业务员".equals(businessUser.getName())){
                        loan.setSaleId(businessUser.getId());
                        loan.setSaleCode(businessUser.getCode());
                        loan.setSaleName(businessUser.getName());
                        return ;
                    }
                });
            }
            loan.setSubmitTime(productImportHongben.getLoanDate());
           // loan.setTerm(getDuration(productImportHongben).intValue()+"");  这里传入了具体时间，所以我们直接用具体的期限时间
            loan.setTerm(productImportHongben.getLoanExtensiononNum().intValue()+"");
            loan.setApplyId(ShiroSession.getLoginUser().getId());
            loan.setLoanTime(productImportHongben.getLoanDate());
            //人人聚财 算头不算尾 其他算头算尾
            if(productImportHongben.getChannel().equals("人人聚财")){
                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL);
            }else{
                loan.setCalculateMethodAboutDay(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL);
            }
            //如果是深圳自营，则归纳为自营，没有合作方，没有渠道ID，所以，如果不是深圳自营，则需要保存渠道ID
            if(!productImportHongben.getChannel().contains("自营")&&!productImportHongben.getChannel().equals("广州分公司")){
                channelList.stream().forEach(channel -> {
                    if(!"动产资本".equals(productImportHongben.getChannel())){
                            if(channel.getName().contains(productImportHongben.getChannel())){
                            loan.setChannelId(channel.getId());
                            return;
                        }
                    }else{
                        if(channel.getName().equals("自营二部-动产资本")){
                            loan.setChannelId(channel.getId());
                            return;
                        }
                    }
                });
                //只有不是深圳自营才需要判断渠道是否存在
                if(StringUtils.isEmpty(loan.getChannelId())){
                    productImportHongben.addExcludeMsg("渠道不存在");
                }
            }

            //保存loan后在保存借款人信息，然后拿到借款人id
            LoanBorrower saveLoanBorrower = null;
            List<LoanBorrower> loanBorrowers = new ArrayList<LoanBorrower>();
            String borrwoers = productImportHongben.getBorrower();
            String idNumbers = productImportHongben.getIdNumber();
            String remainborrwoers=productImportHongben.getBorrower();
            String remainidNumbers = productImportHongben.getIdNumber();
            for(int i=0;i<borrwoers.split(",").length;i++ ){
                String borrowerName  = borrwoers.split(",")[i];
                String idNumber = productImportHongben.getIdNumber().split(",")[i];
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
           /* //检查借款人是否存在 ,如果不存在则创建
            if(loanBorrower==null){

                *//*String borrwoers = productImportHongben.getBorrower();
                String idNumbers = productImportHongben.getIdNumber();*//*
                for(int i=0;i<remainborrwoers.split(",").length;i++ ){
                    String borrowerName  = remainborrwoers.split(",")[i];
                    String idNumber =remainidNumbers.split(",")[i];
                    if(StringUtils.isEmpty(borrowerName)||StringUtils.isEmpty(idNumber)){
                        continue;
                    }
                    //创建借款人
                    Borrower borrower = new Borrower();
                    borrower.setName(borrowerName);
                    borrower.setCertifNumber(idNumber);
                    borrower.setCertifType(LoanerCertifType.ID);
                    borrower.setStatus(PublicStatus.ABLE);
                    borrower.setCreateBy(ShiroSession.getLoginUser().getName());
                    borrower.setCreateTime(new Date());
                    Borrower borrowerSaved  = borrowerService.add(borrower);
                    borrowerList.add(borrowerSaved);

                    //创建借款人和贷款的关联表，这里去第一个借款人为主要借款人
                    loanBorrower =  createLoanBorrower(borrowerSaved,i==0);
                }
            }*/

            if(StringUtils.isEmpty(productImportHongben.getExcludeMsg())){//如果导出信息为空，则符合导出条件。则保存load 获取id复制给借款人，保存借款人，拿到借款人ID复制给loan，然后在更新loan，改变状态为已经执行成功
               // Loan saveLoan =  loanService.add(loan);
                Loan saveLoan = dao().insert(loan);
                for(LoanBorrower loanBorrower : loanBorrowers){
                    loanBorrower.setLoanId(saveLoan.getId());
                    saveLoanBorrower = loanBorrowerService.add(loanBorrower);
                }
                //借款人如果存在，则更新loan的借款人关联id
                saveLoan.setMasterBorrowerId(saveLoanBorrower.getId());
                saveLoan.setUpdateBy(ShiroSession.getLoginUser().getName());
                saveLoan.setUpdateTime(new Date());
                loanService.update(saveLoan);
                //更新完后创建费用模板  红本不需要费用 ，已经确认了，去掉
                /*ProductFee productFee = productFeeService.fetch(Cnd.where("productId","=",saveLoan.getProductId()).and("feeType","=",FeeType.PREPAYMENT_FEE_RATE));
                LoanFeeTemp loanFeeTemp = new LoanFeeTemp(productFee);
                loanFeeTemp.setCreateBy(ShiroSession.getLoginUser().getName());
                loanFeeTemp.setCreateTime(new Date());
                loanFeeTemp.setUpdateTime(new Date());
                loanFeeTemp.setUpdateTime(new Date());
                loanFeeTemp.setLoanId(saveLoan.getId());
                //前端输入，这里设置为费用值
                loanFeeTemp.setFeeAmount(productImportHongben.getCostAmount());
                loanFeeTempService.add(loanFeeTemp);*/
                //更新完了后，再创建loanRepay对象
                List<LoanRepay> loanRepayList = createLoanRepayList(saveLoan ,productImportHongben);

                loanRepayList.stream().forEach(loanRepay -> {
                    //保存还款计划
                    LoanRepay loanRepaySaved = dao().insert(loanRepay);
                    /*LoanFee loanFee = loanRepay.getLoanFeeList().get(0);
                    //保存还款计划费用
                    loanFee.setRepayId(loanRepaySaved.getId());
                    LoanFee loanFeeSaved =  dao().insert(loanFee);*/

                    //保存还款记录
                    if(loanRepay.getLoanRepayRecordList()!=null)for(LoanRepayRecord loanRepayRecord  : loanRepay.getLoanRepayRecordList()){
                        loanRepayRecord.setRepayId(loanRepaySaved.getId());
                        LoanRepayRecord loanRepayRecordSaved = dao().insert(loanRepayRecord);
                        //保存还款记录里面的费用记录
                       /* if(loanRepayRecord.getLoanFeeRecordList()!=null)for(LoanFeeRecord loanFeeRecord : loanRepayRecord.getLoanFeeRecordList()){
                            loanFeeRecord.setLoanFeeId(loanFeeSaved.getId());
                            loanFeeRecord.setRepayRecordId(loanRepayRecordSaved.getId());
                            loanFeeRecord.setRepayId(loanRepaySaved.getId());
                            dao().insert(loanFeeRecord);
                        }*/
                    }
                });

                //保存房产信息  {"keyName":"user","dataValue":""+productImportHongben.getBorrower().split(",")[0]+""},  借款人可能并不是房产证的权属人
                String dataValue = "[[{\"keyName\":\"address\",\"dataValue\":\"" + productImportHongben.getHouseAddress()+ "\"},{\"keyName\":\"house_code\",\"dataValue\":\"" + productImportHongben.getPropertyCertificate() + "\"}]]";
                ProductInfoItem productInfoItem  = new ProductInfoItem();
                productInfoItem.setKeyName("house");
                productInfoItem.setType("json");
                productInfoItem.setDataValue(dataValue);
                productInfoItem.setLoanId(saveLoan.getId());
                productInfoItem.setTmplId(productInfoTmplHongben.getId());
                dao().insert(productInfoItem);


                //设置为成功，并且关联好loanId以便查询
                productImportHongben.setExcludeStatus("01");
                productImportHongben.setLoanId(saveLoan.getId());
                result.put("success",result.get("success")+1);
            }else{//如果不为空，则改变状态为02失败
                productImportHongben.setExcludeStatus("02");
                result.put("failure",result.get("failure")+1);
            }
            productImportHongben.setUpdateBy(ShiroSession.getLoginUser().getId());
            productImportHongben.setUpdateTime(new Date());
            dao().update(productImportHongben);
        });
    }


    private BigDecimal getDuration(Date end, Date start, boolean addOne){
        return new BigDecimal( (int)(((end.getTime()-start.getTime()))/(1000*24*3600))+(addOne?1:0));
    }

    private BigDecimal getDuration(ProductImportHongben productImport){
        return  getDuration(productImport.getExpireDate(),productImport.getLoanDate(),productImport.getChannel().equals("人人聚财")?false:true);
    }

    //判断还款方式，如果找不到还款方式，则输出错误信息
    private LoanRepayMethod getRepayMethod(String repayMethod,String loanExtensiononUnit){
        if("一次性还款-期末收息".equals(repayMethod)){
            return LoanRepayMethod.BULLET_REPAYMENT;
        }else if("先息后本-期初收息".equals(repayMethod)){
            if("天".equals(loanExtensiononUnit)){
                return LoanRepayMethod.INTEREST_DAYS;
            }else if("月".equals(loanExtensiononUnit)) {
                return LoanRepayMethod.INTEREST_MONTHS;
            }

        }
        return null;
    }
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
    private List<LoanRepay> createLoanRepayList(Loan loan,ProductImportHongben productImport){
        ProductFee productFee = productFeeService.fetch(Cnd.where("productId","=",loan.getProductId()).and("feeType","=",FeeType.PREPAYMENT_FEE_RATE));
        if(productFee==null){
            productImport.addExcludeMsg("产品没有添加费用列，");
        }
        List<LoanRepay> loanRepayList = new ArrayList<LoanRepay>();
        if("先息后本-期初收息".equals(productImport.getRepaymentMethod())){
            //如果是按天，则分两期，如果是按月，则根据月份分为多期
            if(loan.getTermType().equals(LoanTermType.DAYS)){
                LoanRepay fisrtLoanPepay =  createLoanRepayFirst(loan ,productImport,1);
                //第一期肯定会有一条还款记录  还款日期为当天
                LoanRepayRecord loanRepayRecord  = getLoanRepayRecord(fisrtLoanPepay,productImport);
                loanRepayRecord.setRepayDate(productImport.getLoanDate());
                //LoanFee loanFee = getLoanFee(fisrtLoanPepay,productImport,productFee);红本不需要费用记录
                loanRepayList.add(fisrtLoanPepay);
            }else{
                for(int i=0;i<productImport.getLoanExtensiononNum().intValue();i++ ){
                    LoanRepay fisrtLoanPepay =  createLoanRepayFirst(loan ,productImport,i+1);
                    //第一期肯定会有一条还款记录  还款日期为当天 ，如果已经结清了，则肯定也会有还款记录  日期小于当天的也标记为已经还清
                    if(i==0||LoanStatus.CLEARED.equals(loan.getLoanStatus())||(fisrtLoanPepay.getDueDate().getTime()<new Date().getTime())){
                        LoanRepayRecord loanRepayRecord  = getLoanRepayRecord(fisrtLoanPepay,productImport);
                        //loanRepayRecord.setRepayDate(productImport.getLoanDate());
                        loanRepayRecord.setRepayDate(fisrtLoanPepay.getDueDate());
                        fisrtLoanPepay.setRepayDate(fisrtLoanPepay.getDueDate());
                        fisrtLoanPepay.setStatus(LoanRepayStatus.CLEARED);
                    }
                    //LoanFee loanFee = getLoanFee(fisrtLoanPepay,productImport,productFee);红本不需要费用记录
                    loanRepayList.add(fisrtLoanPepay);
                }
            }

            //最后一期  最后一期不管怎么样肯定是为没有剩余本金了的
            LoanRepay secondLoanRepay = createLoanRepaySecond(loan ,productImport,productImport.getLoanExtensiononNum().intValue()+1);
            //重新设置还款日期，按第一期还款日期+借款期限来算
            secondLoanRepay.setDueDate(DateUtil.addMonth(productImport.getLoanDate(),productImport.getLoanExtensiononNum().intValue()));
            secondLoanRepay.setRepayDate(null);
            secondLoanRepay.setOutstanding(new BigDecimal(0));
            //LoanFee loanFee1 = getLoanFee(secondLoanRepay,productImport,productFee);  红本不需要费用记录
            //判断是否已经还清，如果还清了生成还款记录，还款日期为结清日   还款计划也要把还款日期修改为结清日期
            if(LoanStatus.CLEARED.equals(loan.getLoanStatus())){
                secondLoanRepay.setRepayDate(productImport.getSettleDate());
                LoanRepayRecord loanRepayRecord1  = getLoanRepayRecord(secondLoanRepay,productImport);
                loanRepayRecord1.setRepayDate(productImport.getSettleDate());
            }
            loanRepayList.add(secondLoanRepay);
        }else if("一次性还款-期末收息".equals(productImport.getRepaymentMethod())){
            LoanRepay loanRepay = createLoanRepay(loan,productImport);
            loanRepay.setOutstanding(new BigDecimal(0));
            //LoanFee loanFee = getLoanFee(loanRepay,productImport,productFee);  红本不需要费用记录
            loanRepayList.add(loanRepay);
            //判断是否已经还清，如果还清了生成还款记录，还款日期为结清日 还款计划也要把还款日期修改为结清日期
            if(LoanStatus.CLEARED.equals(loan.getLoanStatus())){
                loanRepay.setRepayDate(productImport.getSettleDate());
                LoanRepayRecord loanRepayRecord =  getLoanRepayRecord(loanRepay,productImport);
            }

        }
        return loanRepayList;
    }
    //一次性还本付息 ,本金所有，利息所有，费用所有
    private LoanRepay createLoanRepay(Loan loan,ProductImportHongben productImport){

        LoanRepay loanRepay = new LoanRepay();
        loanRepay.setDueDate(productImport.getExpireDate());
        loanRepay.setAmount(productImport.getLoanPrincipal());
        loanRepay.setFeeAmount(productImport.getCostAmount());
        //loanRepay.setInterest(productImport.getLoanPrincipal().multiply(productImport.getDayRate()).multiply( getDuration(productImport)) );
       /* if(loan.getTermType().equals(LoanTermType.DAYS)){
            loanRepay.setInterest(productImport.getLoanPrincipal().multiply(productImport.getLoanExtensiononNum()).multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
        }else if(loan.getTermType().equals(LoanTermType.MOTHS)){
            loanRepay.setInterest(productImport.getLoanPrincipal().multiply(productImport.getLoanExtensiononNum()).multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
        }*/
        loanRepay.setInterest(productImport.getLoanPrincipal().multiply(productImport.getLoanExtensiononNum()).multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
        loanRepay.setLoanId(loan.getId());
        loanRepay.setPeriod(1);
        loanRepay.setRemark("一次性还本付息共一期：第一期");
        if("还款中".equals(productImport.getRepaymentStatus())){
            //结清日期还没有填写的，并且还款日期小于当前日期
            if(productImport.getSettleDate()==null&&productImport.getExpireDate().getTime()<=new Date().getTime()){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
            }else{
                loanRepay.setStatus(LoanRepayStatus.LOANED);
            }
            loanRepay.setOutstanding(productImport.getLoanPrincipal());
            loanRepay.setRepayAmount(new BigDecimal(0));
            loanRepay.setRepayFeeAmount(new BigDecimal(0));
            loanRepay.setRepayInterest(new BigDecimal(0));
        }else if("已还清".equals(productImport.getRepaymentStatus())){
            loanRepay.setStatus(LoanRepayStatus.CLEARED);
            loanRepay.setOutstanding(new BigDecimal(0));
            loanRepay.setRepayAmount(productImport.getLoanPrincipal());
            loanRepay.setRepayFeeAmount(productImport.getCostAmount());
            //没有输入已还多少利息，所以默认为还清了，等于应还利息。
            loanRepay.setRepayInterest(loanRepay.getInterest());
            loanRepay.setRepayDate(productImport.getLoanDate());

        }
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
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
    //先息后本第N期，本金为0，利息为所有，费用为0 ，还款到期时间为当天
    private LoanRepay createLoanRepayFirst(Loan loan,ProductImportHongben productImport ,int period){
        LoanRepay loanRepay = createLoanRepay(loan,productImport);
        //loanRepay.setDueDate(productImport.getLoanDate());
        loanRepay.setDueDate(DateUtil.addMonth(productImport.getLoanDate(),(period-1)));
        loanRepay.setPeriod(period);
        loanRepay.setRemark("先息后本共"+(productImport.getLoanExtensiononNum().intValue()+1)+"期：第"+getPeriodCN(period)+"期");
        loanRepay.setOutstanding(productImport.getLoanPrincipal());
        loanRepay.setFeeAmount(new BigDecimal(0));
        loanRepay.setAmount(new BigDecimal(0));
        loanRepay.setRepayFeeAmount(new BigDecimal(0));
        loanRepay.setRepayAmount(new BigDecimal(0));
        //先息后本，第一期利息肯定交了 ,如果没有填写结清利息，则以到期日-放款日*天利率来得出利息*本金，利息等于一次性还清的利息
        //loanRepay.setRepayInterest(productImport.getSettleInterest()!=null?productImport.getSettleInterest():getDuration(productImport.getExpireDate(),productImport.getLoanDate(),true).multiply(productImport.getDayRate().multiply(productImport.getLoanPrincipal())));
        //如果是按天则为总期限*利率 ，如果是按月来算则是利率*1
        if(loan.getTermType().equals(LoanTermType.DAYS)){
            loanRepay.setInterest(productImport.getLoanPrincipal().multiply(productImport.getLoanExtensiononNum()).multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            //如果是第一期，或者是已经结清，则利息为已还，否则为0
            if(period==1||LoanStatus.CLEARED.equals(loan.getLoanStatus())||(loanRepay.getDueDate().getTime()<new Date().getTime())){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
                loanRepay.setRepayInterest(productImport.getLoanPrincipal().multiply(loan.getInterestRate()).multiply(productImport.getLoanExtensiononNum()).divide(new BigDecimal(100)));
            }else{
                loanRepay.setRepayInterest(new BigDecimal(0));
            }

        }else{
            loanRepay.setInterest(productImport.getLoanPrincipal().multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            if(period==1||LoanStatus.CLEARED.equals(loan.getLoanStatus())||(loanRepay.getDueDate().getTime()<new Date().getTime())){
                loanRepay.setStatus(LoanRepayStatus.CLEARED);
                loanRepay.setRepayInterest(productImport.getLoanPrincipal().multiply(loan.getInterestRate()).divide(new BigDecimal(100)));
            }else {
                loanRepay.setRepayInterest(new BigDecimal(0));
            }
        }
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        return loanRepay;
    }
    //先息后本第二期，本金所有，费用所有，利息为0，还款时间为放款日的第二天
    private LoanRepay createLoanRepaySecond(Loan loan,ProductImportHongben productImport,int period){
        LoanRepay loanRepay = createLoanRepay(loan,productImport);
        loanRepay.setRepayDate(new Date(productImport.getLoanDate().getTime()+24*3600*1000));
        loanRepay.setPeriod(period);
        loanRepay.setRemark("先息后本共"+(productImport.getLoanExtensiononNum().intValue()+1)+"期：第"+getPeriodCN(period)+"期");
        loanRepay.setInterest(new BigDecimal(0));
        loanRepay.setRepayInterest(new BigDecimal(0));
        loanRepay.setTotalAmount(loanRepay.getAmount().add(loanRepay.getInterest()).add(loanRepay.getFeeAmount()));
        loanRepay.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()).add(loanRepay.getFeeAmount()));
        return loanRepay;
    }
    private LoanRepayRecord getLoanRepayRecord(LoanRepay loanRepay,ProductImportHongben productImport){
        LoanRepayRecord loanRepayRecord = new LoanRepayRecord();
        loanRepayRecord.setRepayAmount(loanRepay.getRepayAmount());
        loanRepayRecord.setRepayDate(productImport.getSettleDate());
        loanRepayRecord.setRepayInterest(loanRepay.getRepayInterest());
        loanRepayRecord.setRepayTotalAmount(loanRepay.getRepayAmount().add(loanRepay.getRepayInterest()));
        loanRepayRecord.setCreateBy(ShiroSession.getLoginUser().getName());
        loanRepayRecord.setCreateTime(new Date());
        loanRepay.addLoanRepayRecord(loanRepayRecord);
        //有还款记录就会有费用记录  红本不需要费用
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
    private LoanFee getLoanFee(LoanRepay loanRepay,ProductImportHongben productImport,ProductFee productFee){
        LoanFee loanFee = new LoanFee();
        loanFee.setFeeId(productFee.getId());
        loanFee.setLoanId(loanRepay.getLoanId());
        loanFee.setFeeName("一次性服务费");
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



}
