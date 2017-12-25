package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.service.IdNameEntityService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhouchuang on 2017/7/31.
 */
@IocBean(fields="dao")
public class ProductProfitService extends IdNameEntityService<ProductProfit> {

    @Inject
    ProductProfitService productProfitService;
    @Inject
    LoanService loanService;
    @Inject
    ChannelService channelService;
    @Inject
    BillLoanService billLoanService;
    @Inject
    ProductTypeService productTypeService;
    @Inject
    LoanRepayService loanRepayService;

    /**
     * 获取产品列表
     * @return
     */
    public DataTables queryListAll(DataTableParam paramData) {
        Pager pager= DataTablesUtil.getDataTableToPager(paramData.getStart(),paramData.getLength());
        String sqlStr = " SELECT " +
                " spt.name , " +
                " spt.productType ,"+
                " spt.guarantyType, " +
                " spt.businessType, " +
                " spt.`code`, " +
                " spt.`status`, " +
                " spt.id as  productTypeId ,"+
                " spp.id, " +
                " if( spp.id is not null,1,0) as operation " +
                " FROM " +
                " sl_product_type spt " +
                " LEFT JOIN sl_product_profit spp ON spt.id = spp.productTypeId " +
                " WHERE spt.status = 'ABLE'  and spt.productType not in ('PINGTAI','BAOLI') ";


        String countSqlStr = " SELECT " +
                " count(spt.id)   AS 'number' " +
                " FROM " +
                " sl_product_type spt " +
                " LEFT JOIN sl_product_profit spp ON spt.id = spp.productTypeId " +
                " WHERE spt. STATUS = 'ABLE'   and spt.productType not in ('PINGTAI','BAOLI') ";

        if (paramData.getSearchKeys()!=null&&StringUtils.isNotEmpty(paramData.getSearchKeys().get("code"))){
            sqlStr+= " AND spt.code = @code ";
            countSqlStr+=" AND spt.code = @code ";
        }
        if (paramData.getSearchKeys()!=null&&StringUtils.isNotEmpty(paramData.getSearchKeys().get("name"))){
            sqlStr+= " AND spt.name like concat('%',@name,'%') ";
            countSqlStr+=" AND spt.name like concat('%',@name,'%') ";
        }
        sqlStr += " order by spt.code ";

        Sql sql = Sqls.create(sqlStr);
        if(paramData.getSearchKeys()!=null){
            sql.setParam("code",paramData.getSearchKeys().get("code"));
            sql.setParam("name",paramData.getSearchKeys().get("name"));
        }

        Sql countSql = Sqls.create(countSqlStr);
        if(paramData.getSearchKeys()!=null){
            countSql.setParam("code",paramData.getSearchKeys().get("code"));
            countSql.setParam("name",paramData.getSearchKeys().get("name"));
        }

        sql.setPager(pager);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<ProductProfit> list = new LinkedList<ProductProfit>();
                while (rs.next()) {
                    ProductProfit productProfit=new ProductProfit();
                    productProfit.setProductTypeId(rs.getString("productTypeId"));
                    productProfit.setBusinessType(rs.getString("businessType"));
                    productProfit.setGuarantyType(rs.getString("guarantyType"));
                    productProfit.setCode(rs.getString("code"));
                    productProfit.setName(rs.getString("name"));
                    productProfit.setType(rs.getString("productType"));
                    productProfit.setOperation(rs.getInt("operation"));
                    productProfit.setId(rs.getString("id"));
                    list.add(productProfit);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<ProductProfit> list = sql.getList(ProductProfit.class);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(paramData.getDraw(),count,count,list);
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public ProductProfit fetchEnableProductProfitById(String id) {
        ProductProfit productProfit = dao().fetch(ProductProfit.class,Cnd.where("status","=", PublicStatus.ABLE).and("id","=",id));
        return productProfit;
    }

    /**
     * 通过产品ID查找
     * @param productTypeId
     * @return
     */
    public ProductProfit fetchEnableProductProfitByProductTypeId(String productTypeId) {
        ProductProfit productProfit = dao().fetch(ProductProfit.class,Cnd.where("status","=", PublicStatus.ABLE).and("productTypeId","=",productTypeId));
        return productProfit;
    }

    /**
     * 通过放款ID查找利润
     * @param loanId
     * @return
     */
    public LoanProfit fetchEnableLoanProfitByLoanId(String loanId) {
        LoanProfit loanProfit = dao().fetch(LoanProfit.class,Cnd.where("status","=", PublicStatus.ABLE).and("loanId","=",loanId));
        return loanProfit;
    }


    /**
     * 通过id 删除利润数据
     * @param id
     * @return
     */
    public int  deleteEnableLoanProfitByLoanId(String id) {
        return  dao().delete(LoanProfit.class,id);
    }


    /**
     * 通过放款ID查找BillLoan
     * @param loanId
     * @return
     */
    public BillLoan fetchEnableBillLoanByLoanId(String loanId) {
        BillLoan billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        return billLoan;
    }

    /**
     * 新增产品利润配置
     * @param productProfit
     * @return
     */
    public ProductProfit add(ProductProfit productProfit) {
        if (null==productProfit){
            return null;
        }
        productProfit.setStatus(PublicStatus.ABLE);
        productProfit.updateOperator();
        return  dao().insert(productProfit);
    }

    /**
     * 新增放款利润
     * @param loanProfit
     * @return
     */
    public LoanProfit addLoanProfit(LoanProfit loanProfit) {
        if (null==loanProfit){
            return null;
        }
        loanProfit.setStatus(PublicStatus.ABLE);
        loanProfit.updateOperator();
        return  dao().insert(loanProfit);
    }

    public BigDecimal getTnterest(Loan loan ){
        String loanId = loan.getId();
        //1.生成本金、利息、剩余本金还款计划
        LoanRepayPlan loanRepayPlan = LoanCalculator.calcuate(loan.getAmount(),
                loan.getTermType(),
                loan.getTerm(),
                loan.getRepayMethod(),
                loan.getLoanLimitType(),
                LoanLimitType.FIX_AMOUNT.equals(loan.getLoanLimitType())?loan.getInterestAmount():loan.getInterestRate(),
                loan.getRepayDateType(),
                loan.getLoanTime()!=null?loan.getLoanTime():new Date(),
                loan.getMinInterestAmount(),
                loan.getCalculateMethodAboutDay());
        BigDecimal interest = loanRepayPlan.getInterest();
        return interest;
    }
    /**
     * 修改产品利润配置
     * @param productProfit
     * @return
     */
    public boolean update(ProductProfit productProfit) {
        if (null==productProfit) {
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(Product.class, "^id|createBy|createTime$")).update(productProfit)>0;
    }


    //坏账拨备率
    private BigDecimal getBadAssetsReserve(Loan loan,ProductProfit productProfit,List<BigDecimal> commonlist,boolean needACPI){
        BigDecimal badAssetsReserve=BigDecimal.ZERO;
        if(needACPI){
            badAssetsReserve = commonlist.get(0).multiply(productProfit.getBadAssetsReserve());
        }else{
            badAssetsReserve =  loan.getAmount().multiply(productProfit.getBadAssetsReserve()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        }
        return badAssetsReserve;
    }
    //金服运营成本
    private BigDecimal getOperatingCost(Loan loan,ProductProfit productProfit,List<BigDecimal> commonlist,boolean needACPI){
        BigDecimal operatingCost = BigDecimal.ZERO;
        if(needACPI){
            operatingCost = commonlist.get(0).multiply(productProfit.getOperatingCostMonth());
        }else{
            if(loan.getTermType().equals(LoanTermType.DAYS)){
                operatingCost = commonlist.get(0).multiply(productProfit.getOperatingCostDay());
                //operatingCost =  loan.getAmount().multiply(new BigDecimal(loan.getTerm())).multiply(productProfit.getOperatingCostDay()).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
            }else if(loan.getTermType().equals(LoanTermType.MOTHS)){
                operatingCost = commonlist.get(0).multiply(productProfit.getOperatingCostMonth());
                //operatingCost =  loan.getAmount().multiply(new BigDecimal(loan.getTerm())).multiply(productProfit.getOperatingCostMonth()).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP);
            }else if(loan.getTermType().equals(LoanTermType.FIXED_DATE)){
                operatingCost = commonlist.get(0).multiply(productProfit.getOperatingCostDay());
            }
        }
        return operatingCost;
    }
    //资金成本计算方法
    private BigDecimal getCapitalCost(Loan loan,ProductProfit productProfit,List<BigDecimal> commonlist,boolean needACPI){

        BigDecimal common = BigDecimal.ZERO;
        BigDecimal capitalCost = BigDecimal.ZERO;
        if(needACPI){
            NutMap nutMap  = loanRepayService.generateLoanRepay(loan,loan.getLoanTime()!=null?loan.getLoanTime():new Date());
            List<LoanRepay> loanRepayList =  (List<LoanRepay> )nutMap.get("loanRepayList");
            common = common.add(loan.getAmount().multiply(new BigDecimal(3)).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP));
            for(int i=0;i<loanRepayList.size()/3;i++){
                LoanRepay loanRepay = loanRepayList.get((i+1)*3-1);
                common = common.add(loanRepay.getOutstanding().multiply(new BigDecimal(3)).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP));
            }
            capitalCost  = common.multiply(productProfit.getCapitalCostMonth());
        }else{
            if(loan.getTermType().equals(LoanTermType.DAYS)){
                common = loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
                capitalCost = common.multiply(productProfit.getCapitalCostDay());
                //capitalCost =  loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getCapitalCostDay());
            }else if(loan.getTermType().equals(LoanTermType.MOTHS)){
                common  = loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP);
                capitalCost = common.multiply(productProfit.getCapitalCostMonth());
                //capitalCost = loan.getAmount().multiply(new BigDecimal(loan.getTerm())).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getCapitalCostMonth());
            }else if(loan.getTermType().equals(LoanTermType.FIXED_DATE)){
                BigDecimal one = loan.getCalculateMethodAboutDay().equals(CalculateMethodAboutDay.CALCULATE_HEAD_AND_TAIL)?BigDecimal.ONE:BigDecimal.ZERO;
                BigDecimal term =  new BigDecimal(DateUtil.daysBetweenTowDate(loan.getLoanTime()!=null?loan.getLoanTime():new Date(),DateUtil.getStringToDate(loan.getTerm())));
                common = loan.getAmount().multiply(one.add(term)).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
                capitalCost = common.multiply(productProfit.getCapitalCostDay());
            }
        }
        commonlist.clear();
        commonlist.add(common);
        return capitalCost;
    }
    //首先判断是不是信用贷款
    public LoanProfit getCommonProfit( Loan loan , ProductProfit productProfit){

        ProductType productType = productTypeService.fetchById(loan.getProductTypeId());
        boolean isGerendai = "GERENDAI".equals(productType.getProductType());
        boolean isACPI = loan.getRepayMethod().equals(LoanRepayMethod.EQUAL_INSTALLMENT);

        LoanProfit loanProfit  =  new LoanProfit();
        BigDecimal interest = productProfitService.getTnterest(loan);
        BigDecimal common = BigDecimal.ZERO;
        List<BigDecimal> commonlist = new ArrayList<BigDecimal>();
        BigDecimal capitalCost  = getCapitalCost(loan,productProfit,commonlist,isGerendai&&isACPI);
        BigDecimal valueAddedTax = interest.divide(productProfit.getTotalTax(),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getValueAddedTax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal surtax = valueAddedTax.multiply(productProfit.getSurtax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())){
            channel = channelService.fetch(loan.getChannelId());
        }
        BigDecimal laborCost  =  channel!=null?(   "0".equals(channel.getChannelType())?productProfit.getLaborCostSelf():productProfit.getLaborCostChannel()   ):productProfit.getLaborCostSelf();
        BigDecimal administrativeExpenses = productProfit.getAdministrativeExpenses();
        //BigDecimal badAssetsReserve = loan.getAmount().multiply(productProfit.getBadAssetsReserve()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal badAssetsReserve = getBadAssetsReserve(loan,productProfit,commonlist,isGerendai&&isACPI);
        //BigDecimal operatingCost = BigDecimal.ZERO;
//        if(loan.getTermType().equals(LoanTermType.DAYS)){
//            operatingCost =  loan.getAmount().multiply(new BigDecimal(loan.getTerm())).multiply(productProfit.getOperatingCostDay()).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
//        }else if(loan.getTermType().equals(LoanTermType.MOTHS)){
//            operatingCost =  loan.getAmount().multiply(new BigDecimal(loan.getTerm())).multiply(productProfit.getOperatingCostMonth()).divide(new BigDecimal(1200),10,BigDecimal.ROUND_HALF_UP);
//        }else if(loan.getTermType().equals(LoanTermType.FIXED_DATE)){
//            operatingCost = common.multiply(productProfit.getOperatingCostDay());
//        }

        BigDecimal operatingCost = getOperatingCost(loan,productProfit,commonlist,isGerendai&&isACPI);
        // 净利润=利息收入（含税）-资金成本（含税）-增值税-营业税金及附加-人工费用-行政费用-资产坏账拨备-金服运营成本（赎楼，房贷，车押贷）。
        BigDecimal profit = interest.subtract(capitalCost).subtract(valueAddedTax).subtract(surtax).subtract(laborCost).subtract(administrativeExpenses).subtract(badAssetsReserve).subtract(operatingCost);
        loanProfit.setAdministrativeExpenses(administrativeExpenses);
        loanProfit.setBadAssetsReserve(badAssetsReserve);
        loanProfit.setCapitalCost(capitalCost);
        loanProfit.setInterestRevenue(interest);
        loanProfit.setLaborCost(laborCost);
        loanProfit.setOperatingCost(operatingCost);
        loanProfit.setSurtax(surtax);
        loanProfit.setProfit(profit);
        loanProfit.setValueAddedTax(valueAddedTax);
        loanProfit.setLoanId(loan.getId());

        LoanProfit saveLoanProfit =  productProfitService.addLoanProfit(loanProfit);
        return saveLoanProfit;
    }


    public LoanProfit getBillProfit(Loan loan ,ProductProfit productProfit){


        BillLoan billLoan = productProfitService.fetchEnableBillLoanByLoanId(loan.getId());
        List<BillLoanRepay>  billLoanRepays =  billLoanService.queryBillLoanRepay(loan.getId());
        LoanProfit loanProfit  =  new LoanProfit();
        BigDecimal interest = billLoan.getInterest();
        BigDecimal valueAddedTax = interest.divide(productProfit.getTotalTax(),10,BigDecimal.ROUND_HALF_UP).multiply(productProfit.getValueAddedTax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal surtax = valueAddedTax.multiply(productProfit.getSurtax()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        Channel channel = null;
        if(StringUtils.isNotEmpty(loan.getChannelId())){
            channel = channelService.fetch(loan.getChannelId());
        }
        BigDecimal laborCost  =  channel!=null?(   "0".equals(channel.getChannelType())?productProfit.getLaborCostSelf():productProfit.getLaborCostChannel()   ):productProfit.getLaborCostSelf();
        BigDecimal administrativeExpenses = productProfit.getAdministrativeExpenses();
        BigDecimal badAssetsReserve = (billLoan.getTotalAmount().subtract(interest)).multiply(productProfit.getBadAssetsReserve()).divide(new BigDecimal(100),10,BigDecimal.ROUND_HALF_UP);
        BigDecimal operatingCost  = new BigDecimal(0);
        BigDecimal capitalCost = new BigDecimal(0);
        for(BillLoanRepay billLoanRepay : billLoanRepays){
            BigDecimal common = billLoanRepay.getLoanRepay().getAmount().divide
                    (
                        (productProfit.getCapitalCostDay().multiply(new BigDecimal(billLoanRepay.getDisDays()))).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1)),10,BigDecimal.ROUND_HALF_UP
                    ).multiply(new BigDecimal(billLoanRepay.getDisDays())).divide(new BigDecimal(36500),10,BigDecimal.ROUND_HALF_UP);
            capitalCost = capitalCost.add(common.multiply(productProfit.getCapitalCostDay()));
            operatingCost = operatingCost.add(common.multiply(productProfit.getOperatingCostDay()));
        };
        BigDecimal brokerageFee  =  productProfit.getBrokerageFee().multiply(billLoan.getIntermediaryTotalFee()!=null?billLoan.getIntermediaryTotalFee():BigDecimal.ZERO);

        //净利润=利息收入（含税）-资金成本（含税）-增值税-营业税金及附加-人工费用-行政费用-资产坏账拨备-金服运营成本-居间费及税金（票据融资）
        BigDecimal profit = interest.subtract(capitalCost).subtract(valueAddedTax).subtract(surtax).subtract(laborCost).subtract(administrativeExpenses).subtract(badAssetsReserve).subtract(operatingCost).subtract(brokerageFee);

        loanProfit.setAdministrativeExpenses(administrativeExpenses);
        loanProfit.setBadAssetsReserve(badAssetsReserve);
        loanProfit.setCapitalCost(capitalCost);
        loanProfit.setInterestRevenue(interest);
        loanProfit.setLaborCost(laborCost);
        loanProfit.setOperatingCost(operatingCost);
        loanProfit.setSurtax(surtax);
        loanProfit.setProfit(profit);
        loanProfit.setValueAddedTax(valueAddedTax);
        loanProfit.setBrokerageFee(brokerageFee);
        loanProfit.setLoanId(loan.getId());

        LoanProfit saveLoanProfit =  productProfitService.addLoanProfit(loanProfit);
        return saveLoanProfit;
    }
}
