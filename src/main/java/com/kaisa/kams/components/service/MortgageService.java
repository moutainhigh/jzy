package com.kaisa.kams.components.service;

import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.BaseService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DownLoad;
import com.kaisa.kams.components.utils.PdfUtil;
import com.kaisa.kams.enums.ApprovalStatusType;
import com.kaisa.kams.enums.ChannelType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.MortgageDocumentType;
import com.kaisa.kams.enums.ProductTempType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.BaseModel;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.EquityHolder;
import com.kaisa.kams.models.House;
import com.kaisa.kams.models.Mortgage;
import com.kaisa.kams.models.MortgageHouse;
import com.kaisa.kams.models.MortgageOrder;
import com.kaisa.kams.models.ProductProcess;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.User;
import com.kaisa.kams.models.business.BusinessUser;

import com.kaisa.kams.models.flow.ApprovalResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@IocBean(fields = "dao")
public class MortgageService  extends BaseService<Mortgage> {

    @Inject
    private FlowConfigureService flowConfigureService;
    @Inject
    private FlowService flowService;
    @Inject
    private ProductService productService;
    @Inject
    private UserService userService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private BusinessUserService businessUserService;
    @Inject
    private ApprovalResultService approvalResultService;
    /**
     * 根据查询输入名称 查询菜单信息
     *
     * @param param
     * @return
     */
    public DataTables mortgageList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(),param.getLength());
        String businessName;
        String houseMortgageType;
        String mortgageCode;
        String approvalStatusType = null;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            businessName = keys.get("businessName");
            houseMortgageType = keys.get("houseMortgageType");
            mortgageCode =  keys.get("mortgageCode");
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(keys.get("approvalStatusType"))) {
                approvalStatusType = keys.get("approvalStatusType");
            }
        } else {
            return null;
        }
        String sqlStr = "select DISTINCT sm.* from sl_mortgage sm left join sl_mortgage_house smh on sm.id=smh.mortgageId where 1=1 and sm.createrId = @createrId ";
        String countSqlStr = "select count(DISTINCT sm.id) from sl_mortgage sm left join sl_mortgage_house smh on sm.id=smh.mortgageId where 1=1   and sm.createrId = @createrId ";

        if (org.apache.commons.lang3.StringUtils.isNotEmpty(businessName)) {
            sqlStr += " AND smh.houseId in (select seh.houseId from sl_equity_holder seh where seh.name like @name) ";
            countSqlStr += " AND smh.houseId in (select seh.houseId from sl_equity_holder seh where seh.name like @name) ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(houseMortgageType)) {
            sqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
            countSqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mortgageCode)) {
            sqlStr += " AND  sm.mortgageCode=@mortgageCode ";
            countSqlStr += " AND  sm.mortgageCode=@mortgageCode ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(approvalStatusType)) {
            sqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
            countSqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
        }
        sqlStr += " order by sm.updateTime desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("approvalStatusType", approvalStatusType);
        sql.setParam("mortgageCode", mortgageCode);
        sql.setParam("houseMortgageType", houseMortgageType);
        sql.setParam("name","%"+businessName+"%");
        sql.setParam("createrId",ShiroSession.getLoginUser().getId());
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(Mortgage.class));
        dao().execute(sql);
        List<Mortgage> list = sql.getList(Mortgage.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("approvalStatusType", approvalStatusType);
        countSql.setParam("mortgageCode", mortgageCode);
        countSql.setParam("houseMortgageType", houseMortgageType);
        countSql.setParam("name","%"+businessName+"%");
        countSql.setParam("createrId",ShiroSession.getLoginUser().getId());
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        for(Mortgage mortgage : list){
            setChannelInfo(mortgage);
        }
        return new DataTables(param.getDraw(),count,count,list);


    }

    private void setChannelInfo(Mortgage mortgage) {
        List<MortgageHouse> mortgageHouseList = queryMortgageHouseListByMortgageId(mortgage.getId());
        if(CollectionUtils.isNotEmpty(mortgageHouseList)) {
            String eh = "";
            List<EquityHolder> elist = queryEquityHolderByHouseId(mortgageHouseList.get(0).getHouseId());
            if (CollectionUtils.isNotEmpty(elist)) {
                for (EquityHolder equityHolder : elist) {
                    eh += equityHolder.getName() + "、";
                }
            }
            if (eh.length() > 0) eh = eh.substring(0, eh.length() - 1);
            mortgage.setEquityHolder(eh);
        }

        if (ChannelType.ZY.equals(mortgage.getChannel())) {
            BusinessUser businessUser = businessUserService.fetchById(mortgage.getBusinessId());
            mortgage.setBusinessSource(mortgage.getChannel().getDescription()+"|房贷-"+businessUser.getOrganize().getCode()+"-"+mortgage.getBusinessName());
            mortgage.setBusinessUser(businessUser);
            return;
        }
        if (ChannelType.QD.equals(mortgage.getChannel())) {
            mortgage.setBusinessSource(mortgage.getChannel().getDescription()+"|"+mortgage.getBusinessName());
            return;
        }
    }

    public String getNextFlowCode(){
        Cnd cnd = Cnd.where("1","=","1");
        cnd.desc("mortgageCode");
        Mortgage mortgage = dao().fetch(Mortgage.class,cnd);
        if(mortgage!=null){
            String flowCode = mortgage.getMortgageCode();
            String   maxCode =  flowCode.replaceAll("[A-Z]+[0]+","");
            maxCode =""+ (Integer.parseInt(maxCode)+1);
            return "FCDY000000".substring(0,10-maxCode.length())+maxCode;
        }else{
            return "FCDY000001";
        }
    }

    public Mortgage queryMortgageById(String id){
        return dao().fetch(Mortgage.class,id);
    }

    public List<MortgageHouse> queryMortgageHouseListByMortgageId(String mortgageId){
        Cnd cnd  = Cnd.where("mortgageId","=",mortgageId);
        cnd.asc("sortNo");
        return dao().query(MortgageHouse.class,cnd);
    }


    public House getHouseByMortgageHouseId(String houseId){
        return dao().fetch(House.class,houseId);
    }
    public List<House> queryHouseListByMortgageId(String mortgageId){
        List<House> houseList  = new ArrayList<>();
        queryMortgageHouseListByMortgageId(mortgageId).stream().forEach(mortgageHouse->{
            House house = getHouseByMortgageHouseId(mortgageHouse.getHouseId());
            if(house!=null){
                houseList.add(house);
            }
        });
        return houseList;
    }

    public List<EquityHolder> queryEquityHolderByMortgageId(String mortgageId){
        List<EquityHolder> equityHolderList  = new ArrayList<>();
        queryMortgageHouseListByMortgageId(mortgageId).stream().forEach(mortgageHouse->{
            List<EquityHolder> equityHolders =queryEquityHolderByHouseId(mortgageHouse.getHouseId());
            if(CollectionUtils.isNotEmpty(equityHolders)){
                equityHolderList.addAll(equityHolders);
            }
        });
        return equityHolderList;
    }

    public List<EquityHolder> queryEquityHolderByHouseId(String houseId){
        Cnd cnd  = Cnd.where("houseId","=",houseId);
        cnd.asc("sortNo");
        return dao().query(EquityHolder.class,cnd);
    }


    public Mortgage updateMortgage(Mortgage mortgage,String filter){
        if(filter==null)filter = "^(businessId|channel|businessName|houseMortgageType|loanSubjectId|status|approvalStatusType|approveStatus|approveStatusDesc|updateBy|updateTime)$";
        if(StringUtils.isEmpty(mortgage.getId())){
            mortgage.setMortgageCode(this.getNextFlowCode());
        }
        return persistence(mortgage,filter);
    }
    public MortgageHouse updateMortgageHouse(MortgageHouse mortgageHouse){
        return persistence(mortgageHouse,"^(internalEvaluationValue|sortNo|loanInterestRate|maximumLoanAmount|startBorrowingTime|endBorrowingTime|updateBy|updateTime)$");
    }
    public House updateHouse(House house){
        return persistence(house,"^(housePropertyNumber|address|area|updateBy|updateTime)$");
    }
    public EquityHolder updateEquityHolder(EquityHolder equityHolder){
        return persistence(equityHolder,"^(sortNo|name|homeAddress|certifType|certificateNo|holderIdentity|updateBy|updateTime)$");
    }
    public Mortgage update(Mortgage mortgage, NutMap result){
        result.put("ok",false);
        Trans.exec((Atom) () -> {
            List<FlowConfigure> flowConfigures =  flowConfigureService.queryFlowConfigureByFlowConfigureTypeAble(FlowConfigureType.MORTGAGE);
            if(CollectionUtils.isNotEmpty(flowConfigures)){
                mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                mortgage.setStatus(PublicStatus.ABLE);
                mortgage.updateOperator();
                //默认选中流程配置的第一个，这个不重要了
                mortgage.setProductId(flowConfigures.get(0).getProductId().split(",")[0]);
                Mortgage saveMortgage = updateMortgage(mortgage, "^(productId|businessId|channel|businessName|houseMortgageType|loanSubjectId|status|approvalStatusType|updateBy|updateTime)$");
                if(CollectionUtils.isNotEmpty(mortgage.getMortgageHouseList())){
                    List<EquityHolder> equityHolders  = new ArrayList<>();
                    List<House> houses = new ArrayList<>();
                    List<MortgageHouse> mortgageHouses = new ArrayList<>();
                    for(int i=0;i<mortgage.getMortgageHouseList().size();i++){
                        MortgageHouse mortgageHouse = mortgage.getMortgageHouseList().get(i);
                        mortgageHouse.setMortgageId(saveMortgage.getId());
                        mortgageHouse.setSortNo(i);
                        mortgageHouse.setStartBorrowingTime(DateUtil.getStringToDate(mortgageHouse.getBorrowingTime().split("~")[0]));
                        mortgageHouse.setEndBorrowingTime(DateUtil.getStringToDate(mortgageHouse.getBorrowingTime().split("~")[1]));
                        mortgageHouse.updateOperator();
                        House house = mortgageHouse.getHouse();
                        if(house!=null){
                            House saveHouse  =  updateHouse(house);
                            mortgageHouse.setHouseId(saveHouse.getId());
                            houses.add(saveHouse);
                            if(CollectionUtils.isNotEmpty(house.getEquityHolderList())){
                                for(int j=0;j<house.getEquityHolderList().size();j++){
                                    EquityHolder equityHolder = house.getEquityHolderList().get(j);
                                    equityHolder.setSortNo(j);
                                    equityHolder.setHouseId(saveHouse.getId());
                                    EquityHolder saveEquityHolder = updateEquityHolder(equityHolder);
                                    equityHolders.add(saveEquityHolder);
                                }
                            }
                        }
                        MortgageHouse saveMortgageHouse = updateMortgageHouse(mortgageHouse);
                        mortgageHouses.add(saveMortgageHouse);
                    }
                    removeDeletedBaseModel(this.queryEquityHolderByMortgageId(mortgage.getId()),equityHolders);
                    removeDeletedBaseModel(this.queryHouseListByMortgageId(mortgage.getId()),houses);
                    removeDeletedBaseModel(this.queryMortgageHouseListByMortgageId(mortgage.getId()),mortgageHouses);
                }
                result.put("ok",true);
            }else{
                result.put("ok",false);
                result.put("msg","‘"+FlowConfigureType.MORTGAGE.getDescription()+"’还未配置或启动，请先维护‘"+FlowConfigureType.MORTGAGE.getDescription()+"’");
            }
        });
        return mortgage;
    }
    public Mortgage getSimpleMortgageById(String id){

        Cnd cnd = Cnd.where("id","=",id).and("status","=",PublicStatus.ABLE);
        Mortgage mortgage  = this.fetch(cnd);
        return mortgage;
    }
    public int cancelMortgageById(String id){
        Mortgage mortgage = getSimpleMortgageById(id);
        mortgage.setApprovalStatusType(ApprovalStatusType.CANCEL);
        mortgage.updateOperator();
        return dao().update(mortgage);
    }
    public Mortgage getMortgageById(String id){
        Mortgage mortgage  = this.getSimpleMortgageById(id);
        mortgage.setBusinessUser(businessUserService.fetchById(mortgage.getBusinessId()));
        mortgage.setLoanSubject(loanSubjectService.fetch(mortgage.getLoanSubjectId()));
        setChannelInfo(mortgage);
        mortgage.setLoanSubjectName(loanSubjectService.getLoanSubjectName(mortgage.getLoanSubjectId()));
        List<MortgageHouse> mortgageHouseList = queryMortgageHouseListByMortgageId(id);
        if(CollectionUtils.isNotEmpty(mortgageHouseList)){
            for(MortgageHouse mortgageHouse : mortgageHouseList){
                House house =  getHouseByMortgageHouseId(mortgageHouse.getHouseId());
                if(house!=null){
                    List<EquityHolder> equityHolders =  queryEquityHolderByHouseId(mortgageHouse.getHouseId());
                    house.setEquityHolderList(equityHolders);
                }
                mortgageHouse.setHouse(house);
                mortgageHouse.splitTime();
                mortgage.addMortgageHouseList(mortgageHouse);
            }
        }
        return mortgage;
    }

    public MortgageOrder getMortgageOrderByMortgageId(String mortgageId,FlowConfigureType flowConfigureType){
        Cnd cnd = Cnd.where("mortgageId","=",mortgageId).and("flowConfigureType","=",flowConfigureType);
        return  dao().fetch(MortgageOrder.class,cnd);
    }

    /**
     * 通过loanId删除
     * @param mortgageId
     * @return
     */
    public boolean deleteByMortgageId(String mortgageId,FlowConfigureType flowConfigureType) {
        int flag = dao().clear(MortgageOrder.class,Cnd.where("mortgageId","=",mortgageId).and("flowConfigureType","=",flowConfigureType));
        return flag>0;
    }

    public MortgageOrder addMortgageOrder(MortgageOrder mortgageOrder){
        if(null==mortgageOrder){
            return null;
        }
        return dao().insert(mortgageOrder);
    }
    public NutMap startApprovalProcess(Mortgage mortgage,NutMap result ) {
        FlowConfigureType flowType = FlowConfigureType.MORTGAGE;
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

        Order order = flowService.startInstanceWithMortgage(mortgage, flowType);
        if (null == order) {
            mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
            result.put("ok", false);
            result.put("msg", "提交订单失败:初始化流程引擎失败！");
            return result;
        } else {
            mortgage.setApprovalStatusType(ApprovalStatusType.IN_APPROVAL);
        }
        mortgage.updateOperator();
        updateMortgage(mortgage,null);

        result.put("ok", true);
        result.put("msg", "提交订单成功");
        return result;
    }

    /**
     * 根据房产抵押编号查询审批状态
     */
    public String queryApprovalStatusType(String mortgageCode){
        Sql sql = Sqls.fetchString("select approvalStatusType from sl_mortgage where mortgageCode = @mortgageCode");
        sql.setParam("mortgageCode", mortgageCode);
        dao().execute(sql);
        return sql.getString();
    }
    /**
     * 根据房产抵押编号查询所有
     */
    public Mortgage queryAll(String mortgageCode){
        Cnd cnd = Cnd.where("mortgageCode","=",mortgageCode);
        Mortgage mortgage  = this.fetch(cnd);
        return mortgage;
    }

    public Mortgage getMortgageFilterByHouseId(String mortgageId ,String houseId ){
        Mortgage mortgage = this.getMortgageById(mortgageId);
        List<MortgageHouse> list  = new ArrayList<>();
        for(MortgageHouse mortgageHouse : mortgage.getMortgageHouseList()){
            if(mortgageHouse.getHouseId().equals(houseId)){
               list.add(mortgageHouse);
            }
        }
        mortgage.setMortgageHouseList(list);
        return mortgage;
    }
    public void documentDownload(String mortgageId ,String houseId ,MortgageDocumentType mortgageDocumentType, HttpServletResponse response)throws Exception{
        if(mortgageDocumentType.name().startsWith("NORMAL_")){
            DownLoad.fileDownload(response,mortgageDocumentType);
        }else if(mortgageDocumentType.equals(MortgageDocumentType.DYGZ_SQWTZMS)){//抵押广州 授权委托书证明
            Mortgage mortgage = getMortgageFilterByHouseId(mortgageId,houseId);
            String contextTemp = "\t\t\n"+
                    "${S16}U房地产交易中心：\n" +
                    "\t\t现授权${S8}U同志（身份证号：${S12}U），到贵单位办理抵押人" +
                    "$[${qyrs.name}U（${qyrs.getCertifTypeCN()}N：${qyrs.certificateNo}U）${END；}N]" +
                    "，房屋地址：${address}U房地产权证号：${zqh}U的抵押登记手续及领取房产证、他项权证，注销抵押、更正、换证、补正登记签名等有关事宜。\n" +
                    "\t\t委托期限自签发之日起3个月。\n" +
                    "\t\t\n" +
                    "\t\t\n" +
                    "\t\t\n" +
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t单位：${S6}N(盖章)${S8}N\n" +
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t法定代表人（签字或盖章）：${S5}N\n"+
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t${S2}N年${S2}N月${S2}N 日${S4}N";


            Map map = new HashMap();
            map.put("qyrs",mortgage.getMortgageHouseList().get(0).getHouse().getEquityHolderList());
            map.put("address",mortgage.getMortgageHouseList().get(0).getHouse().getAddress());
            map.put("zqh",mortgage.getMortgageHouseList().get(0).getHouse().getHousePropertyNumber());
            PdfUtil.generalPdf(response,map,"授权委托证明书",contextTemp,false);
        }else if(mortgageDocumentType.equals(MortgageDocumentType.HBDYSZ_WTS)){
            Mortgage mortgage = getMortgageFilterByHouseId(mortgageId,houseId);
            String contextTemp = "\t\t\n"+
                    "委托人：$[姓名 ${qyrs.name}U  性别 ${qyrs.S4}U  ${qyrs.getCertifTypeCN()}N ${qyrs.certificateNo}U${ENDNRS5}N]\n" +
                    "\t\t\t\t联系电话：${S8}U地址：${S10}U\n" +
                    "受托人：姓名 ${loanSubjectName}U  性别 ${S4}U  ${idType}N ${loanSubjectIdNumber}U\n" +
                    "\t\t\t\t联系电话：${S8}U地址：${S10}U\n" +
                    "\t\t受托人在代理期限（${S8}U年${S4}U月${S4}U日至${S8}U年${S4}U月${S4}U日）内代为办理深圳市${S12}U区${S12}U路${S12}U以下第（  ）、（  ）、（  ）、（  ）、（  ）项事宜：\n" +
                    "一、向深圳市${S12}U区地方税务分局或不动产登记机构申报纳税并签署纳税申报材料。\n" +
                    "二、缴费并领取《不动产权证书》、《不动产登记证明》、《房地产证》。\n" +
                    "三、向不动产登记机构申请安居房换证登记。\n" +
                    "四、申领被退回的申请材料，不包括撤回申请。\n" +
                    "五、向不动产登记机构申请办理转移登记手续（委托人为转让方或合同非买受人签署不适用）。\n" +
                    "六、其他事宜：${S24}U。\n" +
                    "受托人在其权限范围及代理期限内签署的一切有关文件，委托人均予以承认，受托人可独立办理，受托人无转委托权。委托人与受托人保证以上委托关系真实、合法、有效，否则愿意承担一切法律责任。\n"+
                    "委托人（签名）：                     \t\t\t\t\t\t\t\t\t\t\t\t\t\t受托人（签名）：                \n"+
                    "\t\t\t\t\t年           月           日\t\t\t\t\t\t\t\t\t\t\t\t\t\t                          年            月           日\n"+
                    "附： 《深圳经济特区房地产登记条例》 第十七条  申请房地产登记，申请人可以委托他人代理。由代理人办理申请登记的，应当向登记机构提交申请人的委托书。境外申请人的委托书应当按规定经过公证或者认证。";

            Map map = new HashMap();
            map.put("qyrs",mortgage.getMortgageHouseList().get(0).getHouse().getEquityHolderList());
            map.put("loanSubjectName",mortgage.getLoanSubject().getName());
            map.put("loanSubjectIdNumber",mortgage.getLoanSubject().getIdNumber());
            map.put("idType",mortgage.getLoanSubject().idType());
            map.put("zqh",mortgage.getMortgageHouseList().get(0).getHouse().getHousePropertyNumber());
            PdfUtil.generalPdf(response,map,"委托书",contextTemp,false);
        }else{
            Mortgage mortgage = getMortgageFilterByHouseId(mortgageId,houseId);
            PdfUtil.generalPdf(response, mortgageDocumentType, mortgage);
        }
    }


    public List<String> getMortgageIdByEquityHolderName(String name){
        Sql sql = Sqls.create("select DISTINCT sm.id from sl_mortgage sm left join sl_mortgage_house smh on sm.id=smh.mortgageId where smh.houseId in (select seh.houseId from sl_equity_holder seh where seh.name like @name)");
        sql.setParam("name", "%"+name+"%");
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    /**
     * 查询待审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalList(DataTableParam param) {
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String businessName;
        String houseMortgageType;
        String mortgageCode;
        String approvalStatusType = null;
        String approvalType;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            businessName = keys.get("businessName");
            houseMortgageType = keys.get("houseMortgageType");
            mortgageCode =  keys.get("mortgageCode");
            approvalType =  keys.get("approvalType");
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(keys.get("approvalStatusType"))) {
                approvalStatusType = keys.get("approvalStatusType");
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
        List<String> mortgageIds = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (WorkItem workItem:majorWorks){
            if(org.apache.commons.lang3.StringUtils.isEmpty(workItem.getTaskName())||workItem.getTaskKey().indexOf(approvalType)<0){
                continue;
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(workItem.getOrderId())) {
                orderIds.add(workItem.getOrderId());
            }
        }
        if (orderIds.size()>0) {
            mortgageIds = getMortgageIds(orderIds);
        }
        //获取到需要当前用户处理的列表
        if(null==mortgageIds||mortgageIds.size()==0){
            return new  DataTables(param.getDraw(),0,0,new ArrayList<>());
        }

        List<String> mortgageIds1 = getMortgageIdByEquityHolderName( businessName);
        mortgageIds.retainAll(mortgageIds1);

        StringBuffer ids = new StringBuffer();
        ids.append("(");
        for(int i=0;i<mortgageIds.size(); i++){
            ids.append("'"+mortgageIds.get(i)+"'");
            if(i!=mortgageIds.size()-1){
                ids.append(",");
            }
        }
        ids.append(")");
        String sqlStr = "SELECT sm.* "+
                " FROM sl_mortgage sm "+
                " LEFT JOIN (select  substring(variable, POSITION(\"\\\"mortgageId\\\":\" IN variable)+14, 36) mortgageId,display_Name  from wf_task) wt on sm.id=wt.mortgageId" +
                " WHERE 1=1"+
                " AND sm.approvalStatusType = 'IN_APPROVAL' "+
                "  AND sm.id IN "+ids.toString();


        String countSqlStr = "SELECT "+
                " COUNT(DISTINCT sm.id) AS 'number' "+
                " FROM sl_mortgage sm "+
                " WHERE 1=1"+
                " AND sm.approvalStatusType = 'IN_APPROVAL' "+
                " AND sm.id IN "+ids.toString();

        /*if (org.apache.commons.lang3.StringUtils.isNotEmpty(businessName)) {
            sqlStr += " AND  sm.businessName like @businessName ";
            countSqlStr += " AND  sm.businessName like @businessName ";
        }*/
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(houseMortgageType)) {
            sqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
            countSqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mortgageCode)) {
            sqlStr += " AND  sm.mortgageCode=@mortgageCode ";
            countSqlStr += " AND  sm.mortgageCode=@mortgageCode ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(approvalStatusType)) {
            sqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
            countSqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
        }

        sqlStr += " order by sm.updateTime desc";
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("approvalType",approvalType);
        sql.setParam("approvalStatusType", approvalStatusType);
        sql.setParam("mortgageCode", mortgageCode);
        sql.setParam("houseMortgageType", houseMortgageType);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(Mortgage.class));
        dao().execute(sql);
        List<Mortgage> list = sql.getList(Mortgage.class);
        countSql.setParam("approvalType",approvalType);
        countSql.setParam("approvalStatusType", approvalStatusType);
        countSql.setParam("mortgageCode", mortgageCode);
        countSql.setParam("houseMortgageType", houseMortgageType);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        if(null==list){
            list = new ArrayList<>();
        }
        for(Mortgage mortgage : list){
            setChannelInfo(mortgage);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }



    /**
     * 查询已完成审批列表
     * @param param
     * @return
     */
    public DataTables queryApprovalCompleteList(DataTableParam param) {
        String userId = ShiroSession.getLoginUser().getId();
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String businessName;
        String houseMortgageType;
        String mortgageCode;
        String approvalStatusType = null;
        String approvalType;
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            businessName = keys.get("businessName");
            houseMortgageType = keys.get("houseMortgageType");
            mortgageCode =  keys.get("mortgageCode");
            approvalType =  keys.get("approvalType");
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(keys.get("approvalStatusType"))) {
                approvalStatusType = keys.get("approvalStatusType");
            }
        } else {
            return new DataTables(param.getDraw(),0,0,new ArrayList());
        }

        List<String> mortgageIds = getMortgageIdByEquityHolderName(businessName);
        if (CollectionUtils.isEmpty(mortgageIds)) {
            return new DataTables(param.getDraw(),0,0,new ArrayList());
        }
        StringBuffer ids = new StringBuffer();
        ids.append("(");
        for(int i=0;i<mortgageIds.size(); i++){
            ids.append("'"+mortgageIds.get(i)+"'");
            if(i!=mortgageIds.size()-1){
                ids.append(",");
            }
        }
        ids.append(")");


        String sqlStr = "SELECT  sm.*   FROM  sl_mortgage sm " +
                "LEFT JOIN ( " +
                " SELECT " +
                "  substring( " +
                "   variable, " +
                "   POSITION( " +
                "    \"\\\"mortgageId\\\":\" IN variable " +
                "   ) + 14, " +
                "   36 " +
                "  ) mortgageId, " +
                "  display_Name " +
                " FROM " +
                "  wf_task " +
                ") wt ON sm.id = wt.mortgageId " +
                "WHERE " +
                " 1 = 1 " +
                "AND @userId IN ( " +
                " SELECT " +
                "  r.userId " +
                " FROM " +
                "  sl_approval_result r " +
                " WHERE " +
                "  r.loanId = sm.id " +
                " AND flowConfigureType = 'MORTGAGE' " +
                " AND r.approvalType = @approvalType " +
                ") " +
                "  AND sm.id IN "+ids.toString();
        String countSqlStr = "SELECT "+
                " count(sm.id) AS 'number' "+
                " FROM sl_mortgage sm " +
                " WHERE 1=1 "+
                " AND  @userId IN (SELECT r.userId FROM sl_approval_result r WHERE r.loanId = sm.id AND r.flowConfigureType ='MORTGAGE' AND r.approvalType =@approvalType)"+
                "  AND sm.id IN "+ids.toString();

       /* if (org.apache.commons.lang3.StringUtils.isNotEmpty(borrower)) {
            sqlStr += " AND  sm.borrower like @borrower ";
            countSqlStr += " AND  sm.borrower like @borrower ";
        }*/
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(houseMortgageType)) {
            sqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
            countSqlStr += " AND  sm.houseMortgageType=@houseMortgageType ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mortgageCode)) {
            sqlStr += " AND  sm.mortgageCode=@mortgageCode ";
            countSqlStr += " AND  sm.mortgageCode=@mortgageCode ";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(approvalStatusType)) {
            sqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
            countSqlStr += " AND  sm.approvalStatusType=@approvalStatusType ";
        }
        sqlStr += " order by sm.updateTime desc";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("approvalType",approvalType);
        sql.setParam("userId",userId);
        sql.setParam("approvalStatusType", approvalStatusType);
        sql.setParam("mortgageCode", mortgageCode);
        sql.setParam("houseMortgageType", houseMortgageType);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(Mortgage.class));
        dao().execute(sql);
        List<Mortgage> list = sql.getList(Mortgage.class);
        Sql countSql = Sqls.create(countSqlStr);
        countSql.setParam("approvalType",approvalType);
        countSql.setParam("userId",userId);
        countSql.setParam("approvalStatusType", approvalStatusType);
        countSql.setParam("mortgageCode", mortgageCode);
        countSql.setParam("houseMortgageType", houseMortgageType);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();
        for(Mortgage mortgage : list){
            setChannelInfo(mortgage);
        }
        return new DataTables(param.getDraw(),count,count,list);
    }

    /**
     * 获取loanId列表
     * @param orderIds
     * @return
     */
    public List<String> getMortgageIds(List<String> orderIds) {
        Sql sql = Sqls.create("SELECT mortgageId FROM sl_mortgage_order WHERE orderId in(@orderIds)");
        sql.setParam("orderIds", orderIds.toArray());
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    public void approvalDownload(String id ,HttpServletResponse response )throws Exception{
        Map<String,Object> map = new HashMap<String,Object>();
        Mortgage mortgage = this.getMortgageById(id);
        map.put("mortgage",mortgage);
        List<MortgageHouse> mortgageHouses = mortgage.getMortgageHouseList();
        map.put("mortgageHouseList",mortgageHouses);
        List<ApprovalResult>  approvalResults =  approvalResultService.query(id,null, FlowConfigureType.MORTGAGE);
        map.put("approvalResults",approvalResults);
        try{
            PdfUtil.generalTableTypePdf(response,map,"房产抵押审批单"+"-"+mortgage.getMortgageCode(),ProductTempType.DIYA,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
