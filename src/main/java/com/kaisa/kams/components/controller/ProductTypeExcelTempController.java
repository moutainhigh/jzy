package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.excelUtil.ExcelTableView;
import com.kaisa.kams.components.utils.excelUtil.ExcelTempUtil;
import com.kaisa.kams.components.view.excel.ExcelLoanTempView;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.flow.ApprovalResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 产品类型excel模板处理
 * Created by luoyj on 2017/03/01
 */
@IocBean
@At("/product_type_excel_tmpl")
public class ProductTypeExcelTempController {

    @Inject
    private LoanService loanService;
    @Inject
    private LoanBorrowerService loanBorrowerService;
    @Inject
    private LoanFeeTempService loanFeeTempService;
    @Inject
    private BorrowerAccountService borrowerAccountService;
    @Inject
    private ProductInfoItemService productInfoItemService;
    @Inject
    private ApprovalResultService approvalResultService;
    @Inject
    private ProductService productService;
    @Inject
    private ChannelService channelService;
    @Inject
    private  BillLoanService billLoanService;
    @Inject
    private BankInfoService bankInfoService;
    @Inject
    private BorrowerService borrowerService;



    @At("/down_load_excel_tmpl")
    @Ok("void")
    public Context downloadExcelTemp(@Param("loadId") String loadId, HttpServletRequest request, HttpServletResponse response) {
        Context ctx = Lang.context();
        String excelTempUrl = "";
        String typeName = "";
        Loan loan = loanService.fetchById(loadId);
        if (loan != null) {
            // 获取下载模板地址
            Product product = productService.fetchLinksProductById(loan.getProductId());
            if (product != null) {
                excelTempUrl = product.getProductInfoTmpl().getExcelUrl();
                typeName = product.getProductInfoTmpl().getName();
            }
        }
        // 封装的模板值方法（包含静态值和动态值）
        ExcelLoanTempView view = geView(loan, typeName);
        // 调用生成模板方法
        if (view != null) {
            try {
                String fileName = "贷款审批单-" + loan.getCode();
                ExcelTempUtil.getInstance().exportObj2ExcelByTemplate(view.getMap(), excelTempUrl, fileName, view.getListMap(), ExcelTableView.class, request, response);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ctx;
    }

    /**
     * 模板数据封装
     *
     * @param loan
     * @return
     */
    public ExcelLoanTempView geView(Loan loan, String typeName) {
        String loadId = loan.getId();
        // 初始化
        ExcelLoanTempView view = new ExcelLoanTempView();
        Map<String, List<?>> dataMap = new HashMap<>();
        // 获取贷款审批单基本信息和产品基本信息
        Map mapLoan = loanService.fetchMapById(loadId);
        // 获取模板基本信息（模板公共信息）
        Map<String, String> mapStaticObject = getBasicData(mapLoan,loan);
        // 通过模板类型调用不模板的封装方法
        switch(typeName)
        {
            case "红本":case "赎楼":case "车贷":case "个人贷":case "人人车":case "赎楼平台放款":
                dataMap = getViewShulouHongbenData(loan,mapStaticObject,mapLoan,typeName);
                break;
            case "票据":case "银票":
                dataMap = getViewPiaojuData(loadId,mapStaticObject,typeName);
            break;
            case "保理":
                dataMap = getViewBaoLiData(loadId,mapStaticObject,mapLoan);
                break;
            default:
                break;
        }
        view.setListMap(dataMap);
        view.setMap(mapStaticObject);
        return view;
    }

    /**
     * 通用模板基本信息
     */
    public Map<String, String> getBasicData(Map loan, Loan lo){
        Map<String, String> mapStaticObject = new HashMap<>();
        // 基本信息
        mapStaticObject.put("code", loan.get("code").toString());
        mapStaticObject.put("productTypeName", loan.get("productTypeName").toString());
        mapStaticObject.put("saleName", loan.get("saleName").toString());
        mapStaticObject.put("applyName", loan.get("applyName").toString());
        // 基本信息-查询渠道
        if(lo!=null){
            if(!Strings.isBlank(lo.getChannelId())){
                String channelName=channelService.findChannelNameById(lo.getChannelId());
                mapStaticObject.put("channel_name", channelName);
            }else {
                mapStaticObject.put("channel_name", "");
            }
        }
        return mapStaticObject;
    }

    /**
     * 通用模板审批流信息获取
     */
    public  List<ExcelTableView> getApprovalData(String loadId){
        List<ExcelTableView> listExcelApproval = new ArrayList<>();
        //查看已审批节点
        List<ApprovalResult> approvalResults = approvalResultService.query(loadId, null,FlowConfigureType.BORROW_APPLY);
        //审批流程
        if (approvalResults != null && approvalResults.size() > 0) {
            for (ApprovalResult result : approvalResults) {
                String enterpriseStr=null;
                String dateString = DateUtil.formatDateTimeToString(result.getApprovalTime());
                if(result.isEnterprise()==true){
                    enterpriseStr="同意";
                }
                ExcelTableView view = new ExcelTableView();
                view.setItem1(result.getNodeName());
                view.setItem2(result.getApprovalCode().getDescription() + "           " + dateString);
                if(enterpriseStr!=null){
                    view.setItem3(enterpriseStr + "           " + dateString);

                }else {
                    view.setItem3("");
                }
                view.setItem4(result.getUserName()+" "+"|"+" "+result.getContent());
                listExcelApproval.add(view);
            }
        } else {
            ExcelTableView view = new ExcelTableView("", "", "", "");
            listExcelApproval.add(view);
        }
        return listExcelApproval;
    }


    /**
     * 封装赎楼,红本，个人贷和车贷数据
     */
    public Map<String, List<?>> getViewShulouHongbenData(Loan loan, Map<String, String> mapStaticObject, Map mapLoan, String typeName){
        Map<String, List<?>> dataMap = new HashMap<>();
        String loanId = loan.getId();

        // 获取模板审批流新（模板公共信息）
        List<ExcelTableView> listExcelApproval= getApprovalData(loanId);

        List<ExcelTableView> listExcel_JK = new ArrayList<>();

        List<ExcelTableView> listExcel_FEE = new ArrayList<>();

        List<ExcelTableView> listExcel_SK = new ArrayList<>();

        List<ExcelTableView> listExcel_FC = new ArrayList<>();

        List<ExcelTableView> listExcel_ZKGZ = new ArrayList<>();

        List<ExcelTableView> listExcel_HK=  new ArrayList<>();

        // 查询借款信息
        List<LoanBorrower> loanBorrowers = loanBorrowerService.queryByLoanId(loanId);
        // 查询产品费率信息
        List<LoanFeeTemp> loanFeeTemps = loanFeeTempService.queryByLoanIdAndLoanRepayMethod(loanId, loan.getRepayMethod());
        //查询账号信息
        List<BorrowerAccount> borrowerAccounts = borrowerAccountService.queryFormatAccountsByLoanId(loanId);
        //查询业务信息
        List<ProductInfoItem> productInfoItems = productInfoItemService.queryByLoanId(loanId);
        //查询回款账户
        List<BankInfo> bankInfo=bankInfoService.queryByLoanId(loanId);


        // 借款人信息
        if (loanBorrowers != null && loanBorrowers.size() > 0) {
            if (loanBorrowers.size() == 1 && loanBorrowers.get(0).isMaster()) {
                LoanBorrower borrower = loanBorrowers.get(0);
                mapStaticObject.put("borrserName", borrower.getName());
                mapStaticObject.put("certifNumber", borrower.getCertifType().getDescription() + "/" + borrower.getCertifNumber());
                mapStaticObject.put("phone", borrower.getPhone());
                mapStaticObject.put("home_address", borrower.getAddress());
                ExcelTableView viewTable1 = new ExcelTableView("","" ,"", "");
                listExcel_JK.add(viewTable1);
            } else {
                for (LoanBorrower l : loanBorrowers) {
                    // 封装主借款人信息
                    if (l.isMaster()) {
                        mapStaticObject.put("borrserName", l.getName());
                        mapStaticObject.put("certifNumber", l.getCertifType().getDescription() + "/" + l.getCertifNumber());
                        mapStaticObject.put("phone", l.getPhone());
                        mapStaticObject.put("home_address", l.getAddress());
                    } else {
                        ExcelTableView viewTable1 = new ExcelTableView("共同借款人", l.getName(),"证件号码", l.getCertifType().getDescription() + "/" + l.getCertifNumber());
                        ExcelTableView viewTable2 = new ExcelTableView("手机号码", l.getPhone(), "家庭住址", l.getAddress());
                        listExcel_JK.add(viewTable1);
                        listExcel_JK.add(viewTable2);
                    }
                }
            }
        } else {
            mapStaticObject.put("borrserName", "");
            mapStaticObject.put("certifNumber", "");
            mapStaticObject.put("phone", "");
            mapStaticObject.put("home_address", "");
            ExcelTableView viewTable1 = new ExcelTableView("共同借款人","", "证件号码", "");
            ExcelTableView viewTable2 = new ExcelTableView("手机号码","","家庭住址", "");
            listExcel_JK.add(viewTable1);
            listExcel_JK.add(viewTable2);
        }
        //收款账户信息
        if (borrowerAccounts != null && borrowerAccounts.size() > 0) {
            for (BorrowerAccount bo : borrowerAccounts) {
                if(!(typeName.equals("人人车") || typeName.equals("赎楼平台放款"))){
                    ExcelTableView viewTable1 = new ExcelTableView("户名", bo.getName(), "收款账户", bo.getAccount());
                    ExcelTableView viewTable2 = new ExcelTableView("开户行", bo.getBank(), "收款金额（元）", bo.getAmount().toString());
                    listExcel_SK.add(viewTable1);
                    listExcel_SK.add(viewTable2);
                }else{
                    ExcelTableView viewTable1 = new ExcelTableView("融资单位（人）名字", bo.getName(),"平台账户（注册手机号）", bo.getPlatformAccount());
                    ExcelTableView viewTable2 = new ExcelTableView("收款金额（元）", bo.getAmount().toString(),"","");
                    listExcel_SK.add(viewTable1);
                    listExcel_SK.add(viewTable2);
                }
            }
        } else {
            if(!(typeName.equals("人人车") || typeName.equals("赎楼平台放款"))){
                ExcelTableView viewTable1 = new ExcelTableView("户名", "", "收款账户", "");
                ExcelTableView viewTable2 = new ExcelTableView("开户行", "", "收款金额（元）", "");
                listExcel_SK.add(viewTable1);
                listExcel_SK.add(viewTable2);
            }else{
                ExcelTableView viewTable1 = new ExcelTableView("融资单位（人）名字", "","平台账户（注册手机号）", "");
                ExcelTableView viewTable2 = new ExcelTableView("收款金额（元）","","","");
                listExcel_SK.add(viewTable1);
                listExcel_SK.add(viewTable2);
            }
        }
        // 产品信息
        mapStaticObject.put("amount", mapLoan.get("amount").toString());
        mapStaticObject.put("term", mapLoan.get("term") + ""+LoanTermForRateType.getdescription(loan.getTermType().getCode()));
        mapStaticObject.put("repayMethod", loan.getRepayMethod().getDescription());
        mapStaticObject.put("interestRate", getInterestRate(loan));
        mapStaticObject.put("minInterestAmount", mapLoan.get("minInterestAmount").toString());
        mapStaticObject.put("actualAmount", mapLoan.get("actualAmount").toString());
        // 产品信息费用相关
        loanFeeTemps = getFeeAmount(loanFeeTemps, loanId);
        setInfo(listExcel_FEE, loanFeeTemps);

        //红本和赎楼数据封装不同
        if (typeName.equals("赎楼") || typeName.equals("赎楼平台放款")) {
            // 业务信息
            getBussinessInfoShuLou(productInfoItems, mapStaticObject,bankInfo,listExcel_HK);
            // map封装
            dataMap.put("datas_1", listExcelApproval);
            dataMap.put("datas_2", listExcel_FEE);
            dataMap.put("datas_3", listExcel_SK);
            dataMap.put("datas_4", listExcel_JK);
            dataMap.put("datas_5", listExcel_HK);
        }
        else if (typeName.equals("红本")) {
            //  业务信息
            getBusinessInfoHongBen(productInfoItems, listExcel_FC, listExcel_ZKGZ);
            // map封装
            dataMap.put("datas_1", listExcelApproval);
            dataMap.put("datas_2", listExcel_ZKGZ);
            dataMap.put("datas_3", listExcel_FC);
            dataMap.put("datas_4", listExcel_FEE);
            dataMap.put("datas_5", listExcel_SK);
            dataMap.put("datas_6", listExcel_JK);
        } else  if(typeName.equals("个人贷")){
            // 业务信息
            getBussinessInfo_cd_gr(productInfoItems, mapStaticObject);
           // map封装
            dataMap.put("datas_1", listExcelApproval);
            dataMap.put("datas_2", listExcel_FEE);
            dataMap.put("datas_3", listExcel_SK);
            dataMap.put("datas_4", listExcel_JK);

        }else  if(typeName.equals("车贷")||typeName.equals("人人车")){
            // 业务信息
            getBussinessInfo_cd_gr(productInfoItems, mapStaticObject);
            String car_value=mapStaticObject.get("car_value");
            if(!Strings.isBlank(car_value)){
                BigDecimal bd=new BigDecimal(car_value);
               String carValue= bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                mapStaticObject.put("car_value",carValue);
            }
            // map封装
            dataMap.put("datas_1", listExcelApproval);
            dataMap.put("datas_2", listExcel_FEE);
            dataMap.put("datas_3", listExcel_SK);
            dataMap.put("datas_4", listExcel_JK);
        }
        return dataMap;
    }

    private void setInfo(List<ExcelTableView> listExcelFee, List<LoanFeeTemp> loanFeeTemps) {
        if (loanFeeTemps != null && loanFeeTemps.size() > 0) {
            for (LoanFeeTemp fee : loanFeeTemps) {
                ExcelTableView viewTable = new ExcelTableView();
                if (fee.getFeeType() != null) {
                    viewTable.setItem1(fee.getFeeType().getDescription());
                 } else {
                    viewTable.setItem1("");
                }
                if (fee.getFeeCycle() != null) {
                    viewTable.setItem2(fee.getFeeCycle().getDescription());
                } else {
                    viewTable.setItem2("");
                }
                if (fee.getChargeNode() != null) {
                    viewTable.setItem3(fee.getChargeNode().getDescription());
                } else {
                    viewTable.setItem3("");
                }
                // 转换feeAmount
                BigDecimal amount = fee.getFeeAmount();
                if (fee.getFeeType().equals(FeeType.OVERDUE_FEE) || fee.getFeeType().equals(FeeType.PREPAYMENT_FEE)) {
                    viewTable.setItem4("--");
                } else {
                    if (amount != null) {
                        viewTable.setItem4(amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "元");
                    } else {
                        viewTable.setItem4("");
                    }
                }
                listExcelFee.add(viewTable);
            }
        } else {
            ExcelTableView viewTable = new ExcelTableView("", "", "","");
            listExcelFee.add(viewTable);
        }
    }

    public Map<String, List<?>> getViewBaoLiData(String loadId, Map<String, String> mapStaticObject, Map loan){
        Map<String, List<?>> dataMap = new HashMap<>();

        // 获取模板审批流新（模板公共信息）
        List<ExcelTableView> listExcelApproval= getApprovalData(loadId);
        List<ExcelTableView> listExcel_JK = new ArrayList<>();

        List<ExcelTableView> listExcel_FEE = new ArrayList<>();

        List<ExcelTableView> listExcel_SK = new ArrayList<>();


        Loan lo = loanService.fetchById(loadId);
        // 查询借款信息
        List<LoanBorrower> loanBorrowers = loanBorrowerService.queryByLoanId(loadId);
        // 查询产品费率信息
        List<LoanFeeTemp> loanFeeTemps = loanFeeTempService.queryByLoanIdAndLoanRepayMethod(loadId, lo.getRepayMethod());
        //查询账号信息
        List<BorrowerAccount> borrowerAccounts = borrowerAccountService.queryFormatAccountsByLoanId(loadId);
        //查询业务信息
        List<ProductInfoItem> productInfoItems = productInfoItemService.queryByLoanId(loadId);

        // 借款人信息
        if (loanBorrowers != null && loanBorrowers.size() > 0) {
            if (loanBorrowers.size() == 1 && loanBorrowers.get(0).isMaster()) {
                LoanBorrower borrower = loanBorrowers.get(0);
                Borrower borrower1=borrowerService.fetchById(borrower.getBorrowerId());
                mapStaticObject.put("borrserName", borrower.getName());
                mapStaticObject.put("certifNumber", borrower.getCertifType().getDescription() + "/" + borrower.getCertifNumber());
                mapStaticObject.put("legalPerson", borrower1.getLegalPerson());
                mapStaticObject.put("legalPersonCertifNumber", borrower1.getLegalPersonCertifNumber());
                ExcelTableView viemTale1 = new ExcelTableView("","" , "","");
                listExcel_JK.add(viemTale1);
            }
        } else {
            mapStaticObject.put("borrserName", "");
            mapStaticObject.put("certifNumber", "");
            mapStaticObject.put("legalPerson", "");
            mapStaticObject.put("legalPersonCertifNumber", "");
        }

        if (borrowerAccounts != null && borrowerAccounts.size() > 0) {
            for (BorrowerAccount bo : borrowerAccounts) {
                    ExcelTableView viemTale1 = new ExcelTableView("收款人", bo.getName(), "收款账户", bo.getAccount());
                    ExcelTableView viemTale2 = new ExcelTableView("开户行", bo.getBank(), "收款金额（元）", bo.getAmount().toString());
                    listExcel_SK.add(viemTale1);
                    listExcel_SK.add(viemTale2);
            }
        } else {
                ExcelTableView viemTale1 = new ExcelTableView("收款人", "", "收款账户", "");
                ExcelTableView viemTale2 = new ExcelTableView("开户行", "", "收款金额（元）", "");
                listExcel_SK.add(viemTale1);
                listExcel_SK.add(viemTale2);
        }
        // 业务数据封装
        getBussinessInfo_cd_gr(productInfoItems, mapStaticObject);

        // 产品信息
        mapStaticObject.put("amount", loan.get("amount").toString());
        mapStaticObject.put("term", loan.get("term") + ""+LoanTermForRateType.getdescription(lo.getTermType().getCode()));
        mapStaticObject.put("repayMethod", lo.getRepayMethod().getDescription());
        mapStaticObject.put("interestRate", getInterestRate(lo));
        // 获取应收账款金额
        BigDecimal re_amount =new BigDecimal(mapStaticObject.get("re_amount"));
        BigDecimal amount1=new BigDecimal(loan.get("amount").toString());
        mapStaticObject.put("financing_proportion",amount1.divide(re_amount,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).toString());
        mapStaticObject.put("overdueDays", String.valueOf(lo.getGrace())+"天");
        mapStaticObject.put("re_amount",re_amount.toString());

        // 产品信息费用相关
        loanFeeTemps = getFeeAmount(loanFeeTemps, loadId);
        setInfo(listExcel_FEE, loanFeeTemps);
        // map封装
        dataMap.put("datas_1", listExcelApproval);
        dataMap.put("datas_2", listExcel_FEE);
        dataMap.put("datas_3", listExcel_SK);
        return dataMap;
    }



    /**
     * 封装票据数据
     */
    public Map<String, List<?>> getViewPiaojuData(String loanId, Map<String, String> mapStaticObject, String typeName){

        Map<String, List<?>> dataMap = new HashMap<>();
        // 获取模板审批流新（模板公共信息）
        List<ExcelTableView> listExcelApproval= getApprovalData(loanId);

        List<ExcelTableView> listExcel_PJ= new ArrayList<>();
         //获取票据总信息信息
         NutMap nutMap= billLoanService.queryBillLoanInfo(loanId);
        //获取封装贴现人信息
        Borrower borrower= (Borrower) nutMap.get("loanBorrower");
        if(borrower!=null){
            mapStaticObject.put("name",borrower.getName());
            mapStaticObject.put("certifNumber",borrower.getCertifNumber());
        }else {
            mapStaticObject.put("name","");
            mapStaticObject.put("certifNumber","");
        }

        // 获取封装票据信息
        List<BillLoanRepay> billLoanRepayList= (List<BillLoanRepay>) nutMap.get("billLoanRepayList");
        //调用票据-商票/票据-银票的票据信息
        getPiaojuMessage(listExcel_PJ,billLoanRepayList,typeName);
        //获取封装付款信息人
        BillLoan billLoan= (BillLoan) nutMap.get("billLoan");
        if(billLoan!=null){
            mapStaticObject.put("totalAmount",billLoan.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapStaticObject.put("interest",billLoan.getInterest().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapStaticObject.put("amount",billLoan.getLoan().getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapStaticObject.put("discountTime",DateUtil.formatDateToString(billLoan.getDiscountTime()));
            mapStaticObject.put("accountName",billLoan.getAccountName());
            mapStaticObject.put("accountBank",billLoan.getAccountBank());
            mapStaticObject.put("accountNo",billLoan.getAccountNo());
        }else {
            mapStaticObject.put("totalAmount","");
            mapStaticObject.put("interest","");
            mapStaticObject.put("amount","");
            mapStaticObject.put("discountTime","");
            mapStaticObject.put("accountName","");
            mapStaticObject.put("accountBank","");
            mapStaticObject.put("accountNo","");
        }

        dataMap.put("datas_1", listExcelApproval);
        dataMap.put("datas_2", listExcel_PJ);
        return dataMap;
    }


    /**
     * 获取票据-商票/票据-银票的票据信息
     * @param listExcel_PJ
     * @param billLoanRepayList
     * @param typeName
     * @return
     */
    public    List<ExcelTableView>  getPiaojuMessage(List<ExcelTableView> listExcel_PJ, List<BillLoanRepay> billLoanRepayList, String typeName){

        ExcelTableView viewTale1;
        ExcelTableView viewTale2;
        ExcelTableView viewTale3;
        ExcelTableView viewTale4;
        ExcelTableView viewTale5;

        if(billLoanRepayList!=null&&billLoanRepayList.size()>0){
            for (BillLoanRepay billLoanRepay:billLoanRepayList){
                if(typeName.equals("票据")){
                     viewTale1 = new ExcelTableView("票号",billLoanRepay.getBillNo() ,"出票日期", DateUtil.formatDateToString(billLoanRepay.getDrawTime()));
                     viewTale2 = new ExcelTableView("付款人", billLoanRepay.getPayer(), "收款人", billLoanRepay.getPayee());
                     viewTale3 = new ExcelTableView("出票金额（元）",billLoanRepay.getLoanRepay().getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString() ,"贴现利息（元）", toInterestString(billLoanRepay.getLoanRepay().getInterest()));
                     viewTale4 = new ExcelTableView("到期日",DateUtil.formatDateToString(billLoanRepay.getLoanRepay().getDueDate()), "付款人开户行", billLoanRepay.getBankName());
                     viewTale5 = new ExcelTableView("调整天数(自然日)",String.valueOf(billLoanRepay.getOverdueDays()),"","");
                }else{
                     viewTale1 = new ExcelTableView("票号",billLoanRepay.getBillNo() ,"出票日期", DateUtil.formatDateToString(billLoanRepay.getDrawTime()));
                     viewTale2 = new ExcelTableView("出票人", billLoanRepay.getPayer(),"收款人", billLoanRepay.getPayee());
                     viewTale3 = new ExcelTableView("出票金额（元）",billLoanRepay.getLoanRepay().getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString() , "贴现利息（元）", toInterestString(billLoanRepay.getLoanRepay().getInterest()));
                     viewTale4 = new ExcelTableView("到期日",DateUtil.formatDateToString(billLoanRepay.getLoanRepay().getDueDate()), "付款行全称", billLoanRepay.getBankName());
                     viewTale5 = new ExcelTableView("调整天数(自然日)",String.valueOf(billLoanRepay.getOverdueDays()),"","");
                }
                listExcel_PJ.add(viewTale1);
                listExcel_PJ.add(viewTale2);
                listExcel_PJ.add(viewTale3);
                listExcel_PJ.add(viewTale4);
                listExcel_PJ.add(viewTale5);
            }

        }else {
            if(typeName.equals("票据")){
                viewTale1 = new ExcelTableView("票号","" , "出票日期","" );
                viewTale2 = new ExcelTableView("付款人", "" , "收款人", "" );
                viewTale3 = new ExcelTableView("出票金额（元）","" , "贴现利息（元）","" );
                viewTale4 = new ExcelTableView("到期日", "" , "付款人开户行", "" );
                viewTale5 = new ExcelTableView("调整天数(自然日)","","","" );
            }else {
                viewTale1 = new ExcelTableView("票号","" ,"出票日期","" );
                viewTale2 = new ExcelTableView("出票人", "" , "收款人", "" );
                viewTale3 = new ExcelTableView("出票金额（元）","" , "贴现利息（元）","" );
                viewTale4 = new ExcelTableView("到期日", "" ,"付款行全称", "" );
                viewTale5 = new ExcelTableView("调整天数(自然日)","","","" );
            }
            listExcel_PJ.add(viewTale1);
            listExcel_PJ.add(viewTale2);
            listExcel_PJ.add(viewTale3);
            listExcel_PJ.add(viewTale4);
            listExcel_PJ.add(viewTale5);
        }

        return listExcel_PJ;
    }

    private String toInterestString(BigDecimal interest) {
        if (null != interest) {
            return interest.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        return "";
    }

    /**
     * 转换获取feeAmont
     *
     * @param loanFeeTemps
     * @return
     */
    private List<LoanFeeTemp> getFeeAmount(List<LoanFeeTemp> loanFeeTemps, String loadId) {
        if (null != loanFeeTemps && loanFeeTemps.size() > 0) {
            for (int i = 0; i <loanFeeTemps.size(); i++) {
                LoanFeeTemp tmp = loanFeeTemps.get(i);
                if (null == tmp) {
                    continue;
                }
                //如果是逾期罚息或者提前结清罚息都为0
                if (FeeType.OVERDUE_FEE == tmp.getFeeType() || FeeType.PREPAYMENT_FEE == tmp.getFeeType()) {
                    tmp.setFeeAmount(new BigDecimal(0));
                } else {
                    if (FeeChargeType.LOAN_AMOUNT_RATE == tmp.getChargeType()) {
                        Loan loanTmp = loanService.fetchById(loadId);
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
        return loanFeeTemps;
    }

    /**
     * 转换获取借款利息
     *
     * @param loan
     * @return
     */
    private String getInterestRate(Loan loan) {
        String interestRate = "";
        String loanMethod = LoanTermForRateType.getdescription(loan.getTermType().getCode());
        if (loan.getLoanLimitType().equals(LoanLimitType.FIX_AMOUNT)) {
            BigDecimal amount = loan.getInterestAmount();
            interestRate = amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "元/" + loanMethod + "（金额计息）";
        } else if (loan.getLoanLimitType().equals(LoanLimitType.FIX_RATE)) {
            BigDecimal amount = loan.getInterestRate();
            interestRate = amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%/" + loanMethod + "（比例计息）";
        } else {
            interestRate = "--";
        }
        return interestRate;
    }

    /**
     * 赎楼业务信息数据封装
     *
     * @param productInfoItems
     * @param mapStaticObject
     */
    private void getBussinessInfoShuLou(List<ProductInfoItem> productInfoItems, Map<String, String> mapStaticObject, List<BankInfo> bankInfoList, List<ExcelTableView> listExcel_HK) {

        // 封装房产审批赎楼信息
        for (ProductInfoItem pit : productInfoItems) {
            if ("ransom_account_control".equals(pit.getKeyName())) {
                if (pit.getDataValue().equals("true")) {
                    mapStaticObject.put(pit.getKeyName(), "是");
                } else if (pit.getDataValue().equals("false")) {
                    mapStaticObject.put(pit.getKeyName(), "否");
                } else {
                    mapStaticObject.put(pit.getKeyName(), pit.getDataValue());
                }
            } else if ("bak_account_control".equals(pit.getKeyName())) {
                if (pit.getDataValue().equals("true")) {
                    mapStaticObject.put(pit.getKeyName(), "是");
                } else if (pit.getDataValue().equals("false")) {
                    mapStaticObject.put(pit.getKeyName(), "否");
                } else {
                    mapStaticObject.put(pit.getKeyName(), pit.getDataValue());
                }
            } else {
                mapStaticObject.put(pit.getKeyName(), pit.getDataValue());
            }
        }
        // 转换赎楼期限
        String ransom_anytime=mapStaticObject.get("ransom_anytime");
        if(ransom_anytime.equals("true")){
            mapStaticObject.put("ransom_time","随时");
        }
        // 封装回款信息
        if (bankInfoList != null && bankInfoList.size() > 0) {
            for(BankInfo bankInfo:bankInfoList){
                ExcelTableView viewTable1 = new ExcelTableView("回款账户开户行", bankInfo.getBank(),"回款账户户名", bankInfo.getName());
                ExcelTableView viewTable2 = new ExcelTableView("回款账户账号", bankInfo.getAccount(),"", "");
                listExcel_HK.add(viewTable1);
                listExcel_HK.add(viewTable2);
            }
        }
    }

    /**
     * 红本业务信息数据封装
     *
     * @param productInfoItems
     * @param listExcel_FC
     */
    private void getBusinessInfoHongBen(List<ProductInfoItem> productInfoItems, List<ExcelTableView> listExcel_FC, List<ExcelTableView> listExcel_ZKGZ) {

        Map<String, Object> mapPit = new HashMap<>();
        if (productInfoItems != null && productInfoItems.size() > 0) {
            // 房产信息封装
            for (ProductInfoItem pit : productInfoItems) {
                 if(!Strings.isBlank(pit.getDataValue())){
                     mapPit.put(pit.getKeyName(), pit.getDataValue());
                 }else {
                     mapPit.put(pit.getKeyName(), "");
                 }
            }
            // 房产信息封装
            String houseJson = mapPit.get("house").toString();
            // 解析json，封装数据
            JSONArray jsonArray = JSONArray.fromObject(houseJson);
            for (int i = 0; i <jsonArray.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                JSONArray jsonArray1 = JSONArray.fromObject(jsonArray.get(i));
                for (int j = 0; j < jsonArray1.size(); j++) {
                    JSONObject jsonObject = jsonArray1.getJSONObject(j);
                    if (null == jsonObject || null == jsonObject.get("keyName")) {
                        continue;
                    }
                    String key = jsonObject.get("keyName").toString();
                    if(null != jsonObject.get("dataValue")){
                        map.put(key, jsonObject.get("dataValue").toString());
                    }else {
                        map.put(key, "");
                    }
                }
                //处理map数据
                String relation = map.get("relation").toString().equals("6") ? map.get("relation_else").toString() : RelationType.getdescription(map.get("relation").toString());
                ExcelTableView viewTable1 = new ExcelTableView("房产证号", map.get("house_code").toString(),"权属人",getHouseOwner(map.get("user").toString()));
                ExcelTableView viewTable2 = new ExcelTableView("与借款人关系", relation, "房产名称", map.get("house_name").toString());
                ExcelTableView viewTable3 = new ExcelTableView("房产地址", map.get("address").toString(),"房产面积㎡", map.get("area").toString());
                ExcelTableView viewTable4 = new ExcelTableView("房产估值（万元）", map.get("price").toString(),"估值渠道", map.get("channel").toString());
                listExcel_FC.add(viewTable1);
                listExcel_FC.add(viewTable2);
                listExcel_FC.add(viewTable3);
                listExcel_FC.add(viewTable4);
            }
            // 借款人业务信息封装
            String people_Type = mapPit.get("people_type").toString();
            // 判断是个人还是企业
            if (people_Type.equals("1")) {
                // 企业
                ExcelTableView viewTable7 = new ExcelTableView("工作类型", "经营企业","公司名称", mapPit.get("company_name_com").toString());
                ExcelTableView viewTable8 = new ExcelTableView("注册资本", mapPit.get("register_funds").toString(),"成立时间", mapPit.get("set_time").toString());
                ExcelTableView viewTable9 = new ExcelTableView("法人代表", mapPit.get("legal_person").toString(),"年营业额", mapPit.get("year_amount").toString());
                ExcelTableView viewTable10 = new ExcelTableView("主营业务", mapPit.get("business").toString(), "", "");
                listExcel_ZKGZ.add(viewTable7);
                listExcel_ZKGZ.add(viewTable8);
                listExcel_ZKGZ.add(viewTable9);
                listExcel_ZKGZ.add(viewTable10);
            } else {
                // 个人
                ExcelTableView viemTale5 = new ExcelTableView("工作类型", "受薪人士", "公司名称", mapPit.get("company_name_self").toString());
                ExcelTableView viemTale6 = new ExcelTableView("月收入（元）", mapPit.get("income").toString(), "", "");
                listExcel_ZKGZ.add(viemTale5);
                listExcel_ZKGZ.add(viemTale6);
            }
        } else {
            // 房产信息
            ExcelTableView viewTable1 = new ExcelTableView("房产证号", "","权属人", "");
            ExcelTableView viewTable2 = new ExcelTableView("与借款人关系", "", "房产名称", "");
            ExcelTableView viewTable3 = new ExcelTableView("房产地址", "", "房产面积㎡", "");
            ExcelTableView viewTable4 = new ExcelTableView("房产估值（万元）", "", "估值渠道", "");
            listExcel_FC.add(viewTable1);
            listExcel_FC.add(viewTable2);
            listExcel_FC.add(viewTable3);
            listExcel_FC.add(viewTable4);
        }
    }

    private String getHouseOwner(String userIdStr) {
        StringBuffer result = new StringBuffer("");

        if (StringUtils.isEmpty(userIdStr)) {
           return result.toString();
        }
        Map object = (Map)Json.fromJson(userIdStr);
        if (null == object || null == object.get("val")) {
            return result.toString();
        }
        List<String>  list = (List)object.get("val");
        if (CollectionUtils.isEmpty(list)) {
            return result.toString();
        }
        List<Borrower> borrowers = loanBorrowerService.queryPropertyOwnersByIdList(list);
        if (CollectionUtils.isEmpty(borrowers)) {
            return result.toString();
        }
        for (Borrower borrower : borrowers) {
            result.append(borrower.getName());
            result.append(" ");
        }
        return result.toString();
    }

    /**
     * 车贷,个人贷和保里业务封装方法
     */
    public void getBussinessInfo_cd_gr(List<ProductInfoItem> productInfoItems, Map<String, String> mapStaticObject){
        // 车贷,个人贷和保里业务封装
        if(productInfoItems!=null){
            for (ProductInfoItem pit : productInfoItems) {
                mapStaticObject.put(pit.getKeyName(), pit.getDataValue());
            }
        }
    }
}
