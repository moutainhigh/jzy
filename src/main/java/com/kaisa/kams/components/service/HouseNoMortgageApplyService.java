package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.*;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.flow.ApprovalResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lw on 2017/9/15.
 */
@IocBean(fields = "dao")
public class HouseNoMortgageApplyService extends IdNameEntityService<HouseNoMortgageApply> {
    private static final Log log = Logs.get();

    @Inject
    private LoanService loanService;

    @Inject
    private FlowService flowService;

    @Inject
    private FlowConfigureService flowConfigureService;

    @Inject
    private LoanBorrowerService loanBorrowerService;

    @Inject
    private BorrowerService borrowerService;

    @Inject
    private HouseInfoService houseInfoService;

    @Inject
    private LoanRepayService loanRepayService;

    @Inject
    private UserService userService;

    @Inject
    private LoanOrderService loanOrderService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private LoanSubjectAccountService loanSubjectAccountService;

    @Inject
    private ApprovalResultService approvalResultService;


    /**
     * 新增
     * @param houseNoMortgageApply
     * @return
     */
    public HouseNoMortgageApply addHouseNoMortgageApply(HouseNoMortgageApply houseNoMortgageApply) {
        if (null==houseNoMortgageApply){
            return null;
        }
        return  dao().insert(houseNoMortgageApply);
    }

    /**
     * 通过loanid查询
     * @param loanId
     * @return
     */
    public HouseNoMortgageApply fetchByLoanId(String loanId){
        if(StringUtils.isNotEmpty(loanId)){
            HouseNoMortgageApply houseNoMortgageApply = this.dao().fetch(HouseNoMortgageApply.class, Cnd.where("loanId", "=", loanId));
            return houseNoMortgageApply;
        }
        return null;
    }

    /**
     * 通过code查询
     * @param code
     * @return
     */
    public HouseNoMortgageApply fetchByCode(String code){
        if(StringUtils.isNotEmpty(code)){
            HouseNoMortgageApply houseNoMortgageApply = this.dao().fetch(HouseNoMortgageApply.class, Cnd.where("businessCode", "=", code).and("loanStatus","in",new ApprovalStatusType[]{ApprovalStatusType.IN_EDIT,ApprovalStatusType.IN_APPROVAL,ApprovalStatusType.APPROVED}));
            return houseNoMortgageApply;
        }
        return null;
    }

    /**
     * 修改申请单
     * @param houseNoMortgageApply
     */
    public boolean updateHouseNoMortgageApply(HouseNoMortgageApply houseNoMortgageApply) {
        if(null==houseNoMortgageApply){
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(HouseNoMortgageApply.class, "^id|createBy|createTime|applyCode$")).update(houseNoMortgageApply)>0;
    }

    /**
     * 根据id更新状态
     * @param id
     * @param loanStatus
     * @return
     */
    public  boolean  updateLoanStatus(String id ,ApprovalStatusType loanStatus){
        User user= ShiroSession.getLoginUser();
        return dao().update(HouseNoMortgageApply.class, Chain.make("loanStatus",loanStatus).add("updateBy",user.getName()).add("updateTime",new Date()),Cnd.where("id","=",id))>0;
    }

    /**
     * 获取申请单后6位编号
     * @param
     * @return
     */
    public String fetchMaxCode() {
        String init = "FCJY";
        String maxCode = (String)dao().func2(HouseNoMortgageApply.class,"max","applyCode",Cnd.where("1","=","1"));
        if (null == maxCode) {
            return init+"000001";
        }else {
            String   result =  maxCode.substring(4,maxCode.length());
            maxCode =String.format("%06d",Integer.parseInt(result)+1);
            return init+maxCode;
        }
    }

    public HouseNoMortgageApply queryHouseNoMortgageApplyById(String id){
        return dao().fetch(HouseNoMortgageApply.class,Cnd.where("id","=",id));
    }

    public HouseNoMortgageApply fetchHouseNoMortgageApplyByLoanId(String loanId){
        return dao().fetch(HouseNoMortgageApply.class,Cnd.where("loanId","=",loanId));
    }

    public boolean deleteHouseNoMortgageApplyById(String id){
        return dao().delete(HouseNoMortgageApply.class,id)>0;
    }

    /**
     * 申请列表查询
     * @param param
     * @return
     */
    public DataTables houseNoMortgageApplyList(DataTableParam param){

        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String borrower;
        String addressType;
        String applyCode;
        String loanStatus = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            borrower = keys.get("borrower");
            addressType = keys.get("addressType");
            applyCode =  keys.get("applyCode");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }

        String sqlStr = "SELECT * " +
                " FROM sl_house_noMortgage_apply sha "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sha.loanId=wt.loanId   where  sha.createrId = @createrId " ;
        String countSqlStr = "SELECT  COUNT( sha.id ) AS 'number' " +
                "  FROM " +
                "  sl_house_noMortgage_apply sha " +
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sha.loanId=wt.loanId   where  sha.createrId = @createrId " ;

        if (StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  sha.borrower like @borrower ";
            countSqlStr += " AND  sha.borrower like @borrower ";
        }
        if (StringUtils.isNotEmpty(addressType)) {
            sqlStr += " AND  sha.addressType=@addressType ";
            countSqlStr += " AND  sha.addressType=@addressType ";
        }
        if (StringUtils.isNotEmpty(applyCode)) {
            sqlStr += " AND  sha.applyCode=@applyCode ";
            countSqlStr += " AND  sha.applyCode=@applyCode ";
        }
        if (StringUtils.isNotEmpty(loanStatus)) {
            sqlStr += " AND  sha.loanStatus=@loanStatus ";
            countSqlStr += " AND  sha.loanStatus=@loanStatus ";
        }

        sqlStr += " order by sha.submitDate desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("borrower", '%'+borrower+'%');
        sql.setParam("addressType", addressType);
        sql.setParam("applyCode", applyCode);
        sql.setParam("loanStatus", loanStatus);
        sql.setParam("createrId",ShiroSession.getLoginUser().getId());
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(HouseNoMortgageApply.class));
        dao().execute(sql);
        List<HouseNoMortgageApply> list = sql.getList(HouseNoMortgageApply.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setParam("borrower", '%'+borrower+'%');
        countSql.setParam("addressType", addressType);
        countSql.setParam("applyCode", applyCode);
        countSql.setParam("createrId",ShiroSession.getLoginUser().getId());
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);
    }

    /**
     * 查询待审批列表
     * @param param
     * @return
     */
    public Object queryApprovalList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String borrower;
        String addressType;
        String applyCode;
        String loanStatus = null;
        String type;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            borrower = keys.get("borrower");
            addressType = keys.get("addressType");
            applyCode =  keys.get("applyCode");
            type =  keys.get("type");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }

        User user = userService.fetchLinksById(ShiroSession.getLoginUser().getId());
        List<Role> roles = user.getRoles();

        if(null==roles||roles.isEmpty()){
            return new DataTables(param.getDraw(),0,0,null);
        }

        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }

        //根据角色Id获取到当前需要处理的节点
        Page<WorkItem> majorPage = new Page<WorkItem>(Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());

        List<WorkItem> majorWorks =  flowService.getEngine().query().getWorkItems(majorPage,queryFilter);
        //获取到所有的loanId
        List<String> loanIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
            if(StringUtils.isEmpty(workItem.getTaskName())||workItem.getTaskKey().indexOf(type)<0){
                continue;
            }
            if (StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }
        if (orderIds.size()>0) {
            loanIds = loanOrderService.getLoanIds(orderIds);
        }
        //获取到需要当前用户处理的列表
        if(null==loanIds||loanIds.size()==0){
            return new  DataTables(param.getDraw(),0,0,new ArrayList<>());
        }

        StringBuffer ids = new StringBuffer();
        ids.append("(");
        for(int i=0;i<loanIds.size(); i++){
            ids.append("'"+loanIds.get(i)+"'");
            if(i!=loanIds.size()-1){
                ids.append(",");
            }
        }
        ids.append(")");
        String sqlStr = "SELECT * "+
                " FROM sl_house_noMortgage_apply sia "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.id=wt.loanId" +
                " WHERE 1=1"+
                " AND sia.loanStatus = 'IN_APPROVAL' "+
                "  AND sia.id IN "+ids.toString();

        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT sia.id) AS 'number' "+
                " FROM sl_house_noMortgage_apply sia "+
                " WHERE 1=1"+
                " AND sia.loanStatus = 'IN_APPROVAL' "+
                " AND sia.id IN "+ids.toString();

        if (StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  sia.borrower like @borrower ";
            countSqlStr += " AND  sia.borrower like @borrower ";
        }
        if (StringUtils.isNotEmpty(addressType)) {
            sqlStr += " AND  sia.addressType=@addressType ";
            countSqlStr += " AND  sia.addressType=@addressType ";
        }
        if (StringUtils.isNotEmpty(applyCode)) {
            sqlStr += " AND  sia.applyCode=@applyCode ";
            countSqlStr += " AND  sia.applyCode=@applyCode ";
        }
        if (StringUtils.isNotEmpty(loanStatus)) {
            sqlStr += " AND  sia.loanStatus=@loanStatus ";
            countSqlStr += " AND  sia.loanStatus=@loanStatus ";
        }

        sqlStr+=" order by sia.submitDate desc ";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("loanIds",ids.toString());
        sql.setParam("borrower", '%'+borrower+'%');
        sql.setParam("addressType", addressType);
        sql.setParam("applyCode", applyCode);
        sql.setParam("loanStatus", loanStatus);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(HouseNoMortgageApply.class));
        dao().execute(sql);
        List<HouseNoMortgageApply> list = sql.getList(HouseNoMortgageApply.class);
        countSql.setParam("loanIds",ids.toString());
        countSql.setParam("borrower", '%'+borrower+'%');
        countSql.setParam("addressType", addressType);
        countSql.setParam("applyCode", applyCode);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        return new DataTables(param.getDraw(),count,count,list);
    }


    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public Object queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String borrower;
        String addressType;
        String applyCode;
        String loanStatus = null;
        String type;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            borrower = keys.get("borrower");
            addressType = keys.get("addressType");
            applyCode =  keys.get("applyCode");
            type =  keys.get("type");
            if(StringUtils.isNotEmpty(keys.get("loanStatus"))) {
                loanStatus = keys.get("loanStatus");
            }
        } else {
            return null;
        }

        String sqlStr = "SELECT * "+
                " FROM sl_house_noMortgage_apply sia "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"loanId\\\":\" IN variable)+10, 36) loanId,display_Name  from wf_task) wt on sia.id=wt.loanId" +
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = sia.id AND flowConfigureType ='DECOMPRESSION' AND r.approvalType =@type)";

        String countSqlStr = "SELECT "+
                " count(sia.id) AS 'number' "+
                " FROM sl_house_noMortgage_apply sia " +
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = sia.id AND r.flowConfigureType ='DECOMPRESSION' AND r.approvalType =@type)";

        if (StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  sia.borrower like @borrower ";
            countSqlStr += " AND  sia.borrower like @borrower ";
        }
        if (StringUtils.isNotEmpty(addressType)) {
            sqlStr += " AND  sia.addressType=@addressType ";
            countSqlStr += " AND  sia.addressType=@addressType ";
        }
        if (StringUtils.isNotEmpty(applyCode)) {
            sqlStr += " AND  sia.applyCode=@applyCode ";
            countSqlStr += " AND  sia.applyCode=@applyCode ";
        }
        if (StringUtils.isNotEmpty(loanStatus)) {
            sqlStr += " AND  sia.loanStatus=@loanStatus ";
            countSqlStr += " AND  sia.loanStatus=@loanStatus ";
        }

        sqlStr += " order by sia.submitDate desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("type",type);
        sql.setParam("userId",userId);
        sql.setParam("borrower", '%'+borrower+'%');
        sql.setParam("addressType", addressType);
        sql.setParam("applyCode", applyCode);
        sql.setParam("loanStatus", loanStatus);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(HouseNoMortgageApply.class));
        dao().execute(sql);
        List<HouseNoMortgageApply> list = sql.getList(HouseNoMortgageApply.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("type",type);
        countSql.setParam("userId",userId);
        countSql.setParam("borrower", '%'+borrower+'%');
        countSql.setParam("addressType", addressType);
        countSql.setParam("applyCode", applyCode);
        countSql.setParam("loanStatus", loanStatus);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        return new DataTables(param.getDraw(),count,count,list);
    }

    public NutMap startApprovalProcess(HouseNoMortgageApply mortgage,NutMap result ) {
        FlowConfigureType flowType = FlowConfigureType.DECOMPRESSION;
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(mortgage.getProductId(), flowType);
        if (productProcess == null) {
            result.put("ok", false);
            result.put("msg", "提交订单失败:产品尚未配置流程！");
            return result;
        }
        //查看当前产品的流程文件是否存在
        boolean snakerExist = flowService.existSnakerFile(mortgage.getProductId(), flowType);
        if (!snakerExist) {
            result.put("ok", false);
            result.put("msg", "提交订单失败:流程引擎未部署！");
            return result;
        }

        Order order = flowService.startInstanceWithNoMortgage(mortgage, flowType);
        if (null == order) {
            mortgage.setLoanStatus(ApprovalStatusType.IN_EDIT);
            result.put("ok", false);
            result.put("msg", "提交订单失败:初始化流程引擎失败！");
            return result;
        } else {
            mortgage.setLoanStatus(ApprovalStatusType.IN_APPROVAL);
        }
        mortgage.updateOperator();
        boolean a = updateHouseNoMortgageApply(mortgage);

        result.put("ok", true);
        result.put("msg", "提交订单成功");
        return result;
    }

    public void documentDownload(String houseId ,String loanId,MortgageDocumentType mortgageDocumentType,String applyId, HttpServletResponse response)throws Exception{

        if(MortgageDocumentType.ZXGZ_TXZM.equals(mortgageDocumentType)){
            ZXGZ_TXZM(houseId,loanId,applyId,response);
        }else if(MortgageDocumentType.ZXGZ_JQZM.equals(mortgageDocumentType)){
            ZXGZ_JQZM(houseId,loanId,applyId,response);
        }else if(MortgageDocumentType.ZXGZ_FRSQWTS.equals(mortgageDocumentType)){
            ZXGZ_FRSQWTS(houseId,loanId,applyId,response);
        }else if(mortgageDocumentType.name().contains("NORMAL")){
            DownLoad.fileDownload(response,mortgageDocumentType);
        }else {
            Map result = getBaseInfo(loanId,houseId,applyId);
            PdfUtil.generalPdf(response, mortgageDocumentType, result);
        }


    }

    public void ZXGZ_TXZM(String houseId,String loanId ,String applyId,HttpServletResponse response)throws Exception{

        Map result = getBaseInfo(loanId,houseId,applyId);

        String contextTemp = "\t\t\n"+
                "\t\t广州市房地产交易登记中心:\n"+
                "\t\t现有借款人$[${loanBorr.borrName}U（${loanBorr.certifType}N：${loanBorr.certifNumber}U）${END，}N]" +
                "于${S1}U${year}U${S1}U年${S1}U${month}U${S1}U月${S1}U${day}U${S1}U日已归还我司（抵押权人）贷款金额（人民币大写）${loanAmount}U，结清贷款，现申请办理抵押登记涂销手续，抵押人抵押资料如下：\n" +
                "\t\t1、抵押人：${ower}U\n" +
                "\t\t2、房产证号：${code}U\n" +
                "\t\t3、他项权证：${warrantNumber}U\n" +
                "\t\t4、抵押房产地址：${address}U\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t深圳市富昌小额贷款有限公司\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${S2}N年${S2}N月${S2}N 日";


        PdfUtil.generalPdf(response,result,"涂销证明",contextTemp,false);
    }

    public void ZXGZ_JQZM(String houseId,String loanId ,String applyId,HttpServletResponse response)throws Exception{

        Map result = getBaseInfo(loanId,houseId,applyId);

        String contextTemp = "\t\t\n"+
                "\t\t借款人：$[${loanBorr.borrName}U（${loanBorr.certifType}N：${loanBorr.certifNumber}U）${END，}N]" +
                "房产地址为：${address}U，他项权证号：${warrantNumber}U，您在本公司的个人贷款的本金及相关利息已于"+
                "${S1}U${year}U${S1}U年${S1}U${month}U${S1}U月${S1}U${day}U${S1}U日全部结清。\n" +
                "\t\t特此证明。\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t深圳市富昌小额贷款有限公司\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${S2}N年${S2}N月${S2}N 日";



        PdfUtil.generalPdf(response,result,"结清证明",contextTemp,false);
    }

    public void ZXGZ_FRSQWTS(String houseId,String loanId ,String applyId, HttpServletResponse response)throws Exception{

        Map result = getBaseInfo(loanId,houseId,applyId);

        String contextTemp = "\t\t\n"+
                "${S16}U房地产交易中心：\n" +
                "\t\t现授权${S8}U同志（身份证号：${S12}U），到贵单位办理抵押人" +
                "$[${loanBorr.borrName}U（${loanBorr.certifType}N：${loanBorr.certifNumber}U）${END；}N]" +
                "，房屋地址：${address}U房地产权证号：${warrantNumber}U的抵押登记手续及领取房产证、他项权证，注销抵押、更正、换证、补正登记签名等有关事宜。\n" +
                "\t\t委托期限自签发之日起3个月。\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t单位：${S6}N(盖章)${S8}N\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t法定代表人（签字或盖章）：${S5}N\n"+
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${S2}N年${S2}N月${S2}N 日${S4}N";



        PdfUtil.generalPdf(response,result,"授权委托证明书",contextTemp,false);
    }

    private Map getBaseInfo(String loanId,String houseId,String applyId) {
        Map result = new HashMap();
        List owerList = new ArrayList();
        List<Borrower> list = new ArrayList<>();
        Loan loan = loanService.fetchById(loanId);
        List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(loanId);
        HouseInfo house = houseInfoService.fetchById(houseId);
        List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(loan.getId());
        Date clearDate = loanRepayList.get(loanRepayList.size()-1).getRepayDate();
        LoanSubject loanSubject = loanSubjectService.fetch(loan.getLoanSubjectId());
        HouseNoMortgageApply houseNoMortgageApply = queryHouseNoMortgageApplyById(applyId);
        Date mortgageDate = houseNoMortgageApply.getMortgageDate();
        for(HouseInfo houseInfo : houseInfoList){
            if(StringUtils.isNotEmpty(houseInfo.getOwerId())){
                String [] idsArr = houseInfo.getOwerId().split(",");
                List<String> idsList = Arrays.asList(idsArr);
                StringBuffer ids = new StringBuffer();
                for(int i=0;i<idsList.size(); i++){
                    ids.append("'"+idsList.get(i)+"'");
                    if(i!=idsList.size()-1){
                        ids.append(",");
                    }
                }
                list = loanBorrowerService.queryPropertyOwnersByIds(ids.toString());
                for(Borrower loanBorrower : list){
                    Map<String,String> map  =new HashMap<String,String>();
                    map.put("borrName",loanBorrower.getName());
                    map.put("certifNumber",loanBorrower.getCertifNumber().toString());
                    map.put("certifType",loanBorrower.getCertifType().getDescription());
                    owerList.add(map);
                    houseInfo.setOwerStr(Json.toJson(owerList));
                }
            }
        }
        result.put("loanAmount",FDFFormatUtils.loanMoneyChinese(loan.getActualAmount()));
        if(null != mortgageDate){
            result.put("mortgageYear",FDFFormatUtils.getY(mortgageDate));
            result.put("mortgageMonth",FDFFormatUtils.getM(mortgageDate));
            result.put("mortgageDay",FDFFormatUtils.getD(mortgageDate));
        }
        result.put("year",FDFFormatUtils.getY(clearDate));
        result.put("month",FDFFormatUtils.getM(clearDate));
        result.put("day",FDFFormatUtils.getD(clearDate));
        result.put("loanBorr",owerList);
        result.put("ower",house.getOwer());
        result.put("code",house.getCode());
        result.put("area",house.getArea());
        result.put("warrantNumber",house.getWarrantNumber());
        result.put("address",house.getAddress());
        result.put("loanSubjectName",loanSubject.getName());
        result.put("loanSubjectTypeNumber",LoanSubjectType.ENTERPRISE.equals(loanSubject.getType())?"营业执照"+"："+loanSubject.getIdNumber():"身份证"+"："+loanSubject.getIdNumber());
        result.put("loanSubjectIdNumber",loanSubject.getIdNumber());
        result.put("loanSubjectCType",LoanSubjectType.ENTERPRISE.equals(loanSubject.getType())?"营业执照":"身份证");
        return result;
    }

    public List<HouseInfo> setHouseInfoStr(String loanId) {
        List<HouseInfo> houseInfoList = houseInfoService.queryByLoanId(loanId);
        for(HouseInfo houseInfo : houseInfoList){
            if(org.apache.commons.lang.StringUtils.isNotEmpty(houseInfo.getOwerId())){
                String [] idsArr = houseInfo.getOwerId().split(",");
                List<String> idsList = Arrays.asList(idsArr);
                List<Borrower> list = loanBorrowerService.queryPropertyOwnersByIdList(idsList);
                List<Map> owerStr = new ArrayList();
                for(Borrower loanBorrower : list){
                    Map map = new HashMap();
                    map.put("name",loanBorrower.getName());
                    map.put("idNumber",loanBorrower.getCertifNumber());
                    map.put("type",loanBorrower.getCertifType().getDescription());
                    owerStr.add(map);
                    houseInfo.setOwerStr(Json.toJson(owerStr));
                }
            }
        }
        return houseInfoList;
    }

    public void approvalDownload(String id ,HttpServletResponse response )throws Exception{
        Map<String,Object> map = new HashMap<String,Object>();
        HouseNoMortgageApply houseNoMortgageApply = this.fetch(id);
        Loan loan  = loanService.fetch(houseNoMortgageApply.getLoanId());
        List<LoanRepay> loanRepayList = loanRepayService.queryLoanRepayByLoanId(houseNoMortgageApply.getLoanId());
        map.put("nomortgage",houseNoMortgageApply);
        map.put("loanAmount",loan.getActualAmount());
        map.put("clearDate",DateUtil.formatDateToString(loanRepayList.get(loanRepayList.size()-1).getRepayDate()));
        List<HouseInfo> houseInfoList = setHouseInfoStr(loan.getId());
        map.put("houseInfoList",houseInfoList);
        if(CollectionUtils.isNotEmpty(houseInfoList)){
            for(HouseInfo houseInfo : houseInfoList){
                if(org.apache.commons.lang.StringUtils.isNotEmpty(houseInfo.getOwerId())){
                    String [] idsArr = houseInfo.getOwerId().split(",");
                    List<String> idsList = Arrays.asList(idsArr);
                    StringBuffer ids = new StringBuffer();
                    for(int i=0;i<idsList.size(); i++){
                        ids.append("'"+idsList.get(i)+"'");
                        if(i!=idsList.size()-1){
                            ids.append(",");
                        }
                    }
                    List<Borrower> list = loanBorrowerService.queryPropertyOwnersByIds(ids.toString());
                    List<Map> owerStr = new ArrayList();
                    for(Borrower loanBorrower : list){
                        Map map1 = new HashMap();
                        map1.put("name",loanBorrower.getName());
                        map1.put("idNumber",loanBorrower.getCertifNumber());
                        map1.put("type",loanBorrower.getCertifType().getDescription());
                        owerStr.add(map1);
                    }
                    houseInfo.setOwerList(owerStr);
                }

            }
        }
        List<ApprovalResult>  approvalResults =  approvalResultService.query(houseNoMortgageApply.getId(),null, FlowConfigureType.DECOMPRESSION);
        map.put("approvalResults",approvalResults);
        try{
            PdfUtil.generalTableTypePdf(response,map,"房产解押审批单"+"-"+houseNoMortgageApply.getApplyCode(),ProductTempType.JIEYA,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
