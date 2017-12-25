package com.kaisa.kams.components.service.flow;

import com.kaisa.kams.components.service.base.BaseService;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.models.flow.FlowConfigureRelation;
import com.kaisa.kams.models.flow.FlowControlItem;
import com.kaisa.kams.models.flow.FlowControlTmpl;
import com.kaisa.kams.components.view.flow.FlowConfigureModuleVO;
import com.kaisa.kams.components.view.flow.FlowConfigureTypeVO;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.RoleService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.ParamData;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@IocBean(fields="dao")
public class FlowConfigureService extends BaseService<FlowConfigure> {
    private static final Log log = Logs.get();

    @Inject
    private RoleService roleService;

    @Inject
    private ProductService productService;

    /**
     * 分页模糊查询
     * @param param
     * @return
     */
    public DataTables queryFlowConfigurePage(ParamData param ) {
        Pager p= DataTablesUtil.getDataTableToPager(param.getStart(),param.getStart());
        List<FlowConfigure> list= dao().query(FlowConfigure.class,param.getCnd(),p);
        for(FlowConfigure flowConfigure : list){
            flowConfigure.setFlowConfigureTypeVO(FlowConfigureType.getModule(flowConfigure.getFlowType()));
        }
        return new DataTables(param.getDraw(),dao().count(FlowConfigure.class),dao().count(FlowConfigure.class,param.getCnd()),list);
    }

    public FlowConfigure getFlowConfigureById(String id){
        FlowConfigure flowConfigure  = this.fetch(id);
        List<FlowConfigureRelation> flowConfigureRelations = getFlowConfigureRelationByConfigId(id);
        if(CollectionUtils.isNotEmpty(flowConfigureRelations)){
            for(FlowConfigureRelation flowConfigureRelation : flowConfigureRelations){
                FlowControlTmpl flowControlTmpl =  getFlowControlTmplByTemplateId(flowConfigureRelation.getTemplateId());
                flowControlTmpl.setFlowControlItems(getFlowControlItemByTemplateId(flowConfigureRelation.getTemplateId()));
                flowConfigureRelation.setTemplate(flowControlTmpl);
                flowConfigure.addFlowConfigureRelation(flowConfigureRelation);
            }
        }
        return flowConfigure;
    }

    public FlowConfigure getSimpleFlowConfigureById(String id){
        return fetch(id);
    }

    public List<FlowConfigureRelation> getFlowConfigureRelationByConfigId(String configId){
        Cnd cnd = Cnd.where("configId","=",configId);
        cnd.asc("sortNo");
        List<FlowConfigureRelation> flowConfigureRelations =  dao().query(FlowConfigureRelation.class,cnd);
        return flowConfigureRelations;
    }

    public FlowControlTmpl getFlowControlTmplByTemplateId(String templateId){
        FlowControlTmpl flowControlTmpl =  dao().fetch(FlowControlTmpl.class,templateId);
        return flowControlTmpl;
    }
    public List<FlowControlTmpl> getFlowControlTmplListByTemplateId(String configId){
        List<FlowControlTmpl> flowControlTmpls = new ArrayList<FlowControlTmpl>();
        for(FlowConfigureRelation flowConfigureRelation : getFlowConfigureRelationByConfigId(configId)){
            FlowControlTmpl flowControlTmpl = this.getFlowControlTmplByTemplateId(flowConfigureRelation.getTemplateId());
            if(flowControlTmpl!=null)flowControlTmpls.add(flowControlTmpl);
        }
        return flowControlTmpls;
    }
    public List<FlowControlItem> getFlowControlItemByTemplateId(String tmplId){
        Cnd cnd = Cnd.where("tmplId","=",tmplId);
        //按数字大小排序实现，不能按字符串排序
        List<FlowControlItem> flowControlItems =  dao().query(FlowControlItem.class,cnd);
        flowControlItems.sort((FlowControlItem item1, FlowControlItem item2) -> Integer.valueOf(item1.getCode().substring(1)).compareTo(Integer.valueOf(item2.getCode().substring(1))));
        for(FlowControlItem flowControlItem :flowControlItems){
            Role role = roleService.fetch(flowControlItem.getOrganizeId());
            flowControlItem.setOrganizeName(role.getName());
        }
        return flowControlItems;
    }
    /**
     * 所有可用的流程类型
     * @return
     */
    public List<FlowConfigureTypeVO> queryFlowConfigureTypeList() {

        List list = new ArrayList();
        for (com.kaisa.kams.enums.FlowConfigureType flowConfigureType : com.kaisa.kams.enums.FlowConfigureType.values()) {
            FlowConfigureTypeVO configureType  = new FlowConfigureTypeVO();
            configureType.setName(flowConfigureType.name());
            configureType.setDesc(flowConfigureType.getDescription());
            configureType.setCode(flowConfigureType.getCode());
            list.add(configureType);
        }
        return list;
    }

    /**
     * 所有可用的模块类型
     * @return
     */
    public List<FlowConfigureModuleVO> queryFlowConfigureModuleList() {
        List list = new ArrayList();
        for (com.kaisa.kams.enums.FlowControlType flowControlType : com.kaisa.kams.enums.FlowControlType.values()) {
            if(flowControlType.name().startsWith("M_")){
                FlowConfigureModuleVO flowConfigureModuleVO = new FlowConfigureModuleVO();
                flowConfigureModuleVO.setName(flowControlType.name());
                flowConfigureModuleVO.setDesc(flowControlType.getDescription());
                flowConfigureModuleVO.setCode(flowControlType.getCode());
                list.add(flowConfigureModuleVO);
            }

        }
        return list;
    }

    public boolean checkIsExist(FlowConfigure flowConfigure){
        String productId   = StringUtils.isEmpty(flowConfigure.getProductId())? ApproveWarnMessageUtils.COMMON_PRODUCT_ID:flowConfigure.getProductId();
        return this.getFlowConfigureByFlowTypeAndProductId(flowConfigure.getFlowType().name(),productId)!=null;
    }
    public FlowConfigure getFlowConfigureByFlowypeAndProductIdMaybeNull(String flowConfigureType,String productId){
        FlowConfigure flowConfigure =  this.getFlowConfigureByFlowTypeAndProductId(flowConfigureType,productId);
        if(flowConfigure==null){
            flowConfigure = this.getFlowConfigureByFlowTypeAndProductId(flowConfigureType,ApproveWarnMessageUtils.COMMON_PRODUCT_ID);
        }
        return flowConfigure;
    }
    public FlowConfigure getFlowConfigureByFlowTypeAndProductId(String flowConfigureType,String productId){
        Cnd cnd = Cnd.where("flowType","=",flowConfigureType);
        cnd.and("productId","like","%"+productId+"%");
        return this.fetch(cnd);
    }
    public NutMap update(FlowConfigure flowConfigure,NutMap result){
        result.put("ok",false);
        String productCode = ApproveWarnMessageUtils.COMMON_PRODUCT_CODE;
        String productId   = StringUtils.isEmpty(flowConfigure.getProductId())?ApproveWarnMessageUtils.COMMON_PRODUCT_ID:flowConfigure.getProductId();
        Trans.exec((Atom) () -> {
            int index=1;
            flowConfigure.setStatus(PublicStatus.DISABLED);
            flowConfigure.setProductId(productId);
            flowConfigure.updateOperator();
            FlowConfigure saveFlowConfigure =  this.updateFlowConfigure(flowConfigure);

            if(CollectionUtils.isNotEmpty(flowConfigure.getFlowConfigureRelations())){
                List<FlowControlTmpl> flowControlTmpls = new ArrayList<FlowControlTmpl>();
                List<FlowConfigureRelation> flowConfigureRelations = new ArrayList<FlowConfigureRelation>();
                for(int i=0;i<flowConfigure.getFlowConfigureRelations().size();i++){
                    FlowConfigureRelation flowConfigureRelation = flowConfigure.getFlowConfigureRelations().get(i);

                    FlowControlTmpl flowControlTmpl = flowConfigureRelation.getTemplate();

                    flowControlTmpl.setName(flowConfigure.getFlowName());
                    flowControlTmpl.setDescription(flowConfigure.getFlowDesc());
                    flowControlTmpl.setStatus(PublicStatus.ABLE);
                    flowControlTmpl.setProductId(productId);
                    flowControlTmpl.setCode(flowConfigureRelation.getModuleType().getCode()+productCode);
                    flowControlTmpl.setType(flowConfigureRelation.getModuleType());
                    flowControlTmpl.updateOperator();
                    FlowControlTmpl saveflowControlTmpl = this.updateFlowControllerTmpl(flowControlTmpl);
                    flowControlTmpls.add(saveflowControlTmpl);

                    if(CollectionUtils.isNotEmpty(flowControlTmpl.getFlowControlItems())){
                        List<FlowControlItem> flowControlItems = new ArrayList<FlowControlItem>();
                        for(int j=0;j<flowControlTmpl.getFlowControlItems().size();j++){
                            FlowControlItem flowControlItem =  flowControlTmpl.getFlowControlItems().get(j);
                            flowControlItem.setCode(flowConfigureRelation.getModuleType().getCode()+(index++));
                            flowControlItem.setTmplId(saveflowControlTmpl.getId());
                            flowControlItem.updateOperator();
                            FlowControlItem saveFlowControlItem = this.updateFlowControllerItem(flowControlItem);
                            flowControlItems.add(saveFlowControlItem);
                        }
                        //清除被删除的item
                        removeDeletedBaseModel(this.getFlowControlItemByTemplateId(flowControlTmpl.getId()),flowControlItems);
                    }

                    flowConfigureRelation.setSortNo(i);
                    flowConfigureRelation.updateOperator();
                    flowConfigureRelation.setTemplateId(saveflowControlTmpl.getId());
                    flowConfigureRelation.setConfigId(saveFlowConfigure.getId());
                    FlowConfigureRelation saveflowConfigureRelation = this.updateFlowConfigureRelation(flowConfigureRelation);
                    flowConfigureRelations.add(saveflowConfigureRelation);
                }
                //清除被删除的tmpl
                removeDeletedBaseModel(this.getFlowControlTmplListByTemplateId(saveFlowConfigure.getId()),flowControlTmpls);
                //清除被删除的ralation
                removeDeletedBaseModel(this.getFlowConfigureRelationByConfigId(saveFlowConfigure.getId()),flowConfigureRelations);
            }
            result.put("ok",true);
        });
        return result;
    }

    public void transferHistoryFlowData(){
        String historyData = "RISK_CONTROL,FINANCE_CONTROL,BUSINESS_CONTROL";
        Cnd cnd  = Cnd.where("type","in",historyData.split(",")).and("status","=",PublicStatus.ABLE);
        cnd.desc("productId");
        List<FlowControlTmpl> flowControlTmpls  = dao().query(FlowControlTmpl.class,cnd);
        FlowConfigure flowConfigure = null;
        String lastProductId = "";
        int index=0;
        int itemIndex = 1;
        for(FlowControlTmpl flowControlTmpl : flowControlTmpls){
            if(StringUtils.isEmpty(flowControlTmpl.getProductId()))continue;
            Product product = productService.fetch(flowControlTmpl.getProductId());
            if(!flowControlTmpl.getProductId().equals(lastProductId)){
                flowConfigure =  new FlowConfigure();
                flowConfigure.setProductId(flowControlTmpl.getProductId());
                flowConfigure.setStatus(PublicStatus.DISABLED);
                flowConfigure.setFlowType(FlowConfigureType.BORROW_APPLY);
                flowConfigure.setFlowDesc(product.getName()+"-"+FlowConfigureType.BORROW_APPLY.getDescription());
                flowConfigure.setFlowName(flowConfigure.getFlowDesc());
                flowConfigure.updateOperator();
                this.updateFlowConfigure(flowConfigure);
                lastProductId = flowControlTmpl.getProductId();
            }
            FlowControlType flowControlType = null;
            if(FlowControlType.BUSINESS_CONTROL.equals(flowControlTmpl.getType())){
                flowControlType = FlowControlType.M_BUSINESS_CONTROL;
                index = 0;
            }else if(FlowControlType.RISK_CONTROL.equals(flowControlTmpl.getType())){
                flowControlType = FlowControlType.M_RISK_CONTROL;
                index = 1;
            }else if(FlowControlType.FINANCE_CONTROL.equals(flowControlTmpl.getType())){
                flowControlType = FlowControlType.M_FINANCE_CONTROL;
                index = 2;
            }

            //复制FlowControlTmpl
            FlowControlTmpl flowControlTmplnew = new FlowControlTmpl();
            flowControlTmplnew.setStatus(flowControlTmpl.getStatus());
            flowControlTmplnew.setCode(flowControlTmpl.getCode());
            flowControlTmplnew.setDescription(flowControlTmpl.getDescription());
            flowControlTmplnew.setName(flowControlTmpl.getName());
            flowControlTmplnew.setProductId(flowControlTmpl.getProductId());
            flowControlTmplnew.setProductTypeId(flowControlTmpl.getProductTypeId());
            flowControlTmplnew.setType(flowControlType);
            flowControlTmplnew.updateOperator();
            FlowControlTmpl flowControlTmplSave = updateFlowControllerTmpl(flowControlTmplnew);

            //生成FlowConfigureRelation
            FlowConfigureRelation flowConfigureRelation = new FlowConfigureRelation();
            flowConfigureRelation.setConfigId(flowConfigure.getId());
            flowConfigureRelation.setTemplateId(flowControlTmplSave.getId());
            flowConfigureRelation.setModuleType(flowControlType);
            flowConfigureRelation.setSortNo(index);
            flowConfigureRelation.updateOperator();
            updateFlowConfigureRelation(flowConfigureRelation);

            //禁用掉以前的FlowControlTmpl
            flowControlTmpl.setStatus(PublicStatus.DISABLED);
            flowControlTmpl.updateOperator();
            updateFlowControllerTmpl(flowControlTmpl);
            //复制FlowControlItem
            Cnd itemCnd = Cnd.where("tmplId","=",flowControlTmpl.getId());
            itemCnd.asc("code");
            List<FlowControlItem> flowControlItems = dao().query(FlowControlItem.class,itemCnd);
            for(FlowControlItem flowControlItem : flowControlItems){
                FlowControlItem flowControlItemNew = new FlowControlItem();
                flowControlItemNew.setApproveAmount(flowControlItem.getApproveAmount());
//                flowControlItemNew.setCode(flowControlItem.getCode());
                flowControlItemNew.setCode(flowControlType.getCode()+(itemIndex++));
                flowControlItemNew.setEnterprise(flowControlItem.isEnterprise());
                flowControlItemNew.setName(flowControlItem.getName());
                flowControlItemNew.setOrganizeId(flowControlItem.getOrganizeId());
                flowControlItemNew.setTmplId(flowControlTmplSave.getId());
                flowControlItemNew.updateOperator();
                updateFlowControllerItem(flowControlItemNew);
            }
        }
    }
    public String getNextFlowCode(){

        Cnd cnd = Cnd.where("1","=","1");
        cnd.desc("flowCode");
        FlowConfigure flowConfigure = dao().fetch(FlowConfigure.class,cnd);

        if(flowConfigure!=null){
            String flowCode = flowConfigure.getFlowCode();
            String   maxCode =  flowCode.replaceAll("LC[0]+","");
            maxCode =""+ (Integer.parseInt(maxCode)+1);
            return "LC0000000".substring(0,10-maxCode.length())+maxCode;
        }else{
            return "LC00000001";
        }
    }
    public FlowConfigure updateFlowConfigure(FlowConfigure flowConfigure){
        if(StringUtils.isNotEmpty(flowConfigure.getId())){
            dao().update(flowConfigure,"^(flowName|flowDesc|status|productId|channelId|updateBy|updateTime)$");
            return flowConfigure;
        }else{
            flowConfigure.setFlowCode(this.getNextFlowCode());
            return  dao().insert(flowConfigure);
        }
    }

    public FlowConfigureRelation updateFlowConfigureRelation(FlowConfigureRelation flowConfigureRelation){
        if(StringUtils.isNotEmpty(flowConfigureRelation.getId())){
            dao().update(flowConfigureRelation);
            return flowConfigureRelation;
        }else{
            return  dao().insert(flowConfigureRelation);
        }

    }

    public FlowControlTmpl updateFlowControllerTmpl(FlowControlTmpl flowControlTmpl){
        if(StringUtils.isNotEmpty(flowControlTmpl.getId())){
            dao().update(flowControlTmpl);
            return flowControlTmpl;
        }else{
            return  dao().insert(flowControlTmpl);
        }
    }

    public FlowControlItem updateFlowControllerItem(FlowControlItem flowControlItem){
        if(StringUtils.isNotEmpty(flowControlItem.getId())){
            dao().update(flowControlItem);
            return flowControlItem;
        }else{
            return  dao().insert(flowControlItem);
        }
    }
    public boolean disable(FlowConfigure flowConfigure){
        flowConfigure.setStatus(PublicStatus.DISABLED);
        return dao().update(flowConfigure,"^(status)$")>0;
    }

    public boolean able(FlowConfigure flowConfigure){
        flowConfigure.setStatus(PublicStatus.ABLE);
        return dao().update(flowConfigure,"^(status)$")>0;
    }

    /**
     * 通过Id查找
     * @param productId,flowConfigureType
     * @return
     */
    public ProductProcess fetchEnableProductProductById(String productId,FlowConfigureType flowConfigureType) {
        ProductProcess productProcess = dao().fetch(ProductProcess.class,Cnd.where("status","=",PublicStatus.ABLE).and("productId","=",productId).and("flowType","=",flowConfigureType.name()));
        return productProcess;
    }

    public List<FlowConfigure> queryFlowConfigureByFlowConfigureTypeAble(FlowConfigureType flowConfigureType){
        Cnd cnd = Cnd.where("flowType","=",flowConfigureType).and("status","=",PublicStatus.ABLE);
        return dao().query(FlowConfigure.class,cnd);
    }
    public List<FlowConfigure> queryFlowConfigureByFlowConfigureType(FlowConfigureType flowConfigureType){
        Cnd cnd = Cnd.where("flowType","=",flowConfigureType);
        return dao().query(FlowConfigure.class,cnd);
    }
    public String queryDisableProductIdByFlowConfigureType(FlowConfigureType flowConfigureType){
        List<FlowConfigure> list = queryFlowConfigureByFlowConfigureType(flowConfigureType);
        HashSet<String> productIds = new HashSet<String>();
        for(FlowConfigure flowConfigure : list){
            if(StringUtils.isNotEmpty(flowConfigure.getProductId())){
                if(ApproveWarnMessageUtils.COMMON_PRODUCT_ID.equals(flowConfigure.getProductId())){
                    productIds.clear();
                    productIds.add(flowConfigure.getProductId());
                    break;
                }else{
                    for( String productId : flowConfigure.getProductId().split(",")){
                        if(StringUtils.isNotEmpty(productId)){
                            productIds.add(productId);
                        }
                    }
                }
            }
        }
        return StringUtils.join(productIds.toArray(), ",");
    }
}
