package com.kaisa.kams.components.service;

import com.kaisa.kams.components.controller.base.BusinessApplyBaseController;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.PdfUtil;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.flow.ApprovalResult;
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
import org.nutz.mvc.annotation.Param;
import org.nutz.service.IdNameEntityService;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * 用印服务层
 * Created by lw on 2016/12/15.
 */
@IocBean(fields = "dao")
public class SealService extends IdNameEntityService<Seal> {

    @Inject
    private LoanService loanService;
    @Inject
    private BorrowerService borrowerService;
    @Inject
    private LoanBorrowerService loanBorrowerService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private LoanFeeTempService loanFeeTempService;
    @Inject
    private ProductService productService;
    @Inject
    private ProductInfoTmplService productInfoTmplService;
    @Inject
    private BusinessApplyBaseController businessApplyBaseController;
    @Inject
    private ApprovalResultService approvalResultService;
    @Inject
    private BorrowerAccountService borrowerAccountService;
    @Inject
    private HouseInfoService houseInfoService;
    @Inject
    private ChannelService channelService;
    @Inject
    private BillLoanService billLoanService;
    @Inject
    private ProductInfoItemService productInfoItemService;
    public DataTables query(int start,int length, int draw ,String bussinessNo, String borrower, String repayDate, String status){

        String sqlStr = "SELECT  " +
                "s.id AS 'id'," +
                "l.id AS 'loanId'," +
                "l.code AS 'code', " +
                "l.saleName AS 'saleName', " +
                "b.name AS 'borrserName', " +
                "l.submitTime AS 'loanDate'," +
                "l.approveStatusDesc AS 'loanStatus'," +
                "s.useTime AS 'userTime'," +
                "s.status AS 'status'," +
                "a.name AS 'agencyName'," +
                "spit.productTempType "+
                " FROM " +
                " sl_loan l LEFT JOIN sl_seal s ON l.id=s.loanId " +
                " LEFT JOIN sl_product sp on sp.id = l.productId " +
                " LEFT JOIN sl_product_info_tmpl spit on spit.id = sp.infoTmpId "+
                " LEFT JOIN sl_business_user u ON u.id=l.saleId " +
                " LEFT JOIN sl_loan_borrower b ON l.masterBorrowerId = b.id " +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                " WHERE " +
                " l.loanStatus in ('APPROVEEND','LOANED','CLEARED','OVERDUE') " ;


        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT l.id) AS 'number' "+
                " FROM " +
                " sl_loan l LEFT JOIN sl_seal s ON l.id=s.loanId " +
                " LEFT JOIN sl_business_user u ON u.id=l.saleId " +
                " LEFT JOIN sl_loan_borrower b ON l.masterBorrowerId = b.id " +
                " LEFT JOIN sl_business_organize o ON u.organizeId = o.id " +
                " LEFT JOIN sl_business_agency a ON o.agencyId = a.id " +
                " WHERE " +
                " l.loanStatus in ('APPROVEEND','LOANED','CLEARED','OVERDUE') " ;

        if (StringUtils.isNotEmpty(bussinessNo)){
            sqlStr+=" AND l.code=@bussinessNo ";
            countSqlStr+=" AND l.code=@bussinessNo ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  b.name like @borrower ";
            countSqlStr+=" AND  b.name like @borrower ";
        }

        if (StringUtils.isNotEmpty(status)){
            if(("UNUSED").equals(status)){
                sqlStr+=" AND  (s.status =@status  OR s.status is NULL) ";
                countSqlStr+=" AND  (s.status =@status  OR s.status is NULL) ";
            }else {
                sqlStr+=" AND  s.status=@status ";
                countSqlStr+=" AND  s.status=@status ";
            }

        }

        String beginDate = null;
        String endDate = null;
        if (StringUtils.isNotEmpty(repayDate)){
            String date[] = repayDate.split("to");

            if(null != date && date.length > 1){
                beginDate = date[0];
                endDate = date[1];
                sqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
                countSqlStr+=" AND l.submitTime>=@beginDate  AND l.submitTime<=@endDate ";
            }

        }
        sqlStr+=" GROUP BY l.code order by l.submitTime ASC " +
                " LIMIT @start,@length ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("bussinessNo",bussinessNo);
        sql.setParam("borrower","%"+borrower+"%");
        sql.setParam("status",status);
        sql.setParam("beginDate",beginDate+" 00:00:00");
        sql.setParam("endDate",endDate+"  23:59:59");
        sql.setParam("start",start);
        sql.setParam("length",length);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<Map> list = new LinkedList<Map>();
                while (rs.next()) {
                    Map tmp=new HashMap();
                    tmp.put("id",rs.getString("id"));
                    tmp.put("loanId",rs.getString("loanId"));
                    tmp.put("code",rs.getString("code"));
                    tmp.put("saleName",rs.getString("saleName"));
                    tmp.put("borrserName",rs.getString("borrserName"));
                    tmp.put("loanDate",rs.getString("loanDate"));
                    tmp.put("loanStatus",rs.getString("loanStatus"));
                    tmp.put("userTime",rs.getString("userTime"));
                    tmp.put("status",rs.getString("status"));
                    tmp.put("agencyName",rs.getString("agencyName"));
                    tmp.put("productTempType",rs.getString("productTempType"));
                    list.add(tmp);
                }
                return list;
            }
        });
        dao().execute(sql);
        List<Map> list = sql.getList(Map.class);

        countSql.setParam("bussinessNo",bussinessNo);
        countSql.setParam("borrower","%"+borrower+"%");
        countSql.setParam("status",status);
        countSql.setParam("beginDate",beginDate+" 00:00:00");
        countSql.setParam("endDate",endDate+"  23:59:59");
        countSql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                int result = 0;
                while (rs.next()) {
                    result = rs.getInt("number");
                }
                return result;
            }
        });
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(draw,count,count,list);

    }

    /**
     * 修改用印
     * @param seal
     * @return
     */
    public boolean update(Seal seal) {
        if (null==seal) {
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(Seal.class, "^id|loanId|createBy|createTime$")).update(seal)>0;
    }

    /**
     * 新增
     * @param seal
     * @return
     */
    public Seal add(Seal seal) {
        if(null==seal) {
            return null;
        }
        return dao().insert(seal);
    }

    public void documentDownload(String loanId ,HttpServletResponse response ){
        Map<String,Object> map = new HashMap<String,Object>();
        Loan loan = loanService.fetch(loanId);
        LoanBorrower loanBorrower = loanBorrowerService.fetch(loan.getMasterBorrowerId());
        Product product = productService.fetch(loan.getProductId());
        ProductInfoTmpl productInfoTmpl = productInfoTmplService.fetch(product.getInfoTmpId());
        ProductTempType productTempType = productInfoTmpl.getProductTempType();
        Map businessInfo  =   (Map)((NutMap)businessApplyBaseController.queryBusinessInfo(loanId)).get("data");
        if(businessInfo!=null){
            businessInfo.forEach((k,v)->{
                if("amount_scale".equals(k)){
                    Double num = Double.parseDouble(v.toString())*100;
                    BigDecimal b = new BigDecimal(num);
                    Double f1 = b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                    businessInfo.put(k,f1);
                }
            });
        }
        List<ApprovalResult>  approvalResults =  approvalResultService.query(loanId,null, FlowConfigureType.BORROW_APPLY);
        List<BorrowerAccount> borrowerAccounts = borrowerAccountService.queryFormatAccountsByLoanId(loanId);
        //查询费用信息
        map.put("loan",loan);
        map.put("borrower",borrowerService.fetch(loanBorrower.getBorrowerId()));
        map.put("loanBorrower",loanBorrower);
        map.put("loanFeeTemps",loanFeeTempService.queryByLoanIdAndLoanRepayMethodAndFeeType(loanId,loan.getRepayMethod()));
        map.put("businessInfo",businessInfo);
        map.put("approvalResults",approvalResults);
        map.put("borrowerAccounts",borrowerAccounts);
        map.put("houseList",houseInfoService.queryByLoanId(loanId));
        map.put("productName",product.getName());
        if(StringUtils.isNotEmpty(loan.getChannelId())){
            map.put("channel",channelService.fetch(loan.getChannelId()));
            map.put("channelsale",channelService.fetch(loan.getChannelId()).getName());
            map.put("loanType","渠道");
        }else{
            map.put("channelsale",loan.getSaleName());
            map.put("loanType","自营");
        }
        if(ProductTempType.isBill(productTempType)){
            map.put("billLoanRepayList", billLoanService.queryRepayOrderByPosition(loanId));
            map.put("billLoan",dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan"));
        }

        List<ProductInfoItem> productInfoItems =  productInfoItemService.queryByLoanId(loanId);
        Map data = new HashMap<>();
        for (ProductInfoItem pit:productInfoItems){
            data.put(pit.getKeyName(), pit.getDataValue());
        }
        map.put("baseInfo",data);
        try{
            PdfUtil.generalTableTypePdf(response,map,productInfoTmpl.getName()+"-"+product.getName()+"-"+loan.getCode(),productTempType,true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
