package com.kaisa.kams.components.service.flow;

import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.models.flow.FlowConfigureRelation;
import com.kaisa.kams.models.flow.FlowControlItem;
import com.kaisa.kams.models.flow.FlowControlTmpl;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.service.WfTempletSevice;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.nutz.dao.Cnd;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 流程
 * Created by weid on 2016/12/7.
 */
@IocBean(fields="dao")
public class FlowControlTmplService extends IdNameEntityService<FlowControlTmpl> {
    private static final Log log = Logs.get();

    @Inject
    private ProductService productService;

    @Inject
    private FlowControlTmplService flowControlTmplService;

    @Inject
    private FlowControlItemService flowControlItemService;

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private WfTempletSevice wfTempletSevice;

    @Inject
    private FlowConfigureService flowConfigureService;

    private Element lastElement;


    public FlowControlTmpl add(FlowControlTmpl flowControlTmpl) {
        if (null==flowControlTmpl){
            return null;
        }
        return dao().insert(flowControlTmpl);
    }

    /**
     * 分页模糊查询
     * @param start
     * @param length
     * @param code
     * @param name
     * @param productType
     * @param product
     * @return
     */
    public DataTables query(int start, int length, int draw,String code, String name, String productType, String product,FlowControlType type) {
        Pager p= DataTablesUtil.getDataTableToPager(start,length);
        Cnd cnd = Cnd.where("type","=",type);
        if (!StringUtils.isEmpty(code)){
            cnd.and("code","like","%"+code+"%");
        }
        if (!StringUtils.isEmpty(name)){
            cnd.and("name","like","%"+name+"%");
        }
        if (!StringUtils.isEmpty(product)){
            cnd.and("productId","=",product);
        }
        if (!StringUtils.isEmpty(productType)){
            cnd.and("productTypeId","=",productType);
        }
        List<FlowControlTmpl> list= dao().query(FlowControlTmpl.class,cnd,p);
        return new DataTables(draw,dao().count(FlowControlTmpl.class),dao().count(FlowControlTmpl.class,cnd),list);
    }

    /**
     * 根据Id查找
     * @param id
     * @return
     */
    public FlowControlTmpl fetchById(String id) {
          return dao().fetch(FlowControlTmpl.class,id);
    }


    /**
     * 修改数据
     * @param flowControlTmpl
     * @return
     */
    public boolean update(FlowControlTmpl flowControlTmpl) {
        if (null==flowControlTmpl){
            return false;
        }
        int flag = dao().update(flowControlTmpl,"^(name|description|status|updateBy|updateTime)$");
        return flag>0;
    }


    /**
     * 根据产品Id和类型获取到审批流程
     * @param productId
     * @param type
     */
    public FlowControlTmpl fetchByProductIdAndType(String productId, FlowControlType type) {
        return dao().fetch(FlowControlTmpl.class,Cnd.where("productId","=",productId).and("type","=",type).and("status","=",PublicStatus.ABLE));
    }

    /**
     * 根据产品Id和类型获取到审批流程
     * @param productId
     * @param type
     */
    public FlowControlTmpl fetchByProductIdsAndType(String productId, FlowControlType type,FlowConfigureType flowConfigureType) {
        //如果不为空，则新流程生效，去新流程里面的内容,此处应该跟版本走的，如果在流程开启后提交的审批流程，然后在流程关闭后也应该查询到对应的流程模板，不应该影响已生成流程的正常审批
        Cnd cnd = Cnd.where("productId","like","%" + productId + "%").and("type","=",type).and("status","=",PublicStatus.ABLE);
        FlowControlTmpl flowControlTmpl = dao().fetch(FlowControlTmpl.class,cnd);
        if(flowControlTmpl==null){
            Cnd cnd1= Cnd.where("productId","=",ApproveWarnMessageUtils.COMMON_PRODUCT_ID).and("type","=",type).and("status","=",PublicStatus.ABLE);
            flowControlTmpl = dao().fetch(FlowControlTmpl.class,cnd1);
        }
        return flowControlTmpl;
    }


    /**
     * 获取财务流程
     * @return
     */
    public FlowControlTmpl fetchFinance() {
        return dao().fetch(FlowControlTmpl.class,Cnd.where("type","=",FlowControlType.FINANCE_CONTROL).and("status","=",PublicStatus.ABLE));
    }

    public WfTemplet newConverSnakerXml(String productId,String flowConfigureType){

        //根据产品Id获取到产品
        Product product = productService.fetchEnableProductById(productId);
        if (null==product){
            return null;
        }


        /*Cnd cnd = Cnd.where("flowType","=",flowConfigureType);
        cnd.and("productId","like","%"+product.getId()+"%");
        FlowConfigure flowConfigure = flowConfigureService.fetch(cnd);
        if(flowConfigure==null){
            flowConfigure = flowConfigureService.getFlowConfigureByFlowTypeAndProductId(flowConfigureType, ApproveWarnMessageUtils.COMMON_PRODUCT_ID);
            if(flowConfigure==null)return null;
        }*/

        FlowConfigure flowConfigure =flowConfigureService.getFlowConfigureByFlowypeAndProductIdMaybeNull(flowConfigureType,productId);
        if(flowConfigure==null){
            return null;
        }
//        //根节点
        Element process = new Element("process").setAttribute("displayName",product.getName()+"审批流程").setAttribute("name",product.getCode());
        Document doc = new Document(process);
        Element pre = null;
        //开始节点
        Element start = new Element("start").setAttribute("displayName","开始节点").setAttribute("name","start");
        Element startTran = new Element("transition").setAttribute("name","startTransition");
        start.addContent(startTran);
        process.addContent(start);
        pre = start;


        List<FlowConfigureRelation> flowConfigureRelations  = flowConfigureService.getFlowConfigureRelationByConfigId(flowConfigure.getId());
        if(CollectionUtils.isNotEmpty(flowConfigureRelations)){
            for(int i=0;i<flowConfigureRelations.size();i++){
                FlowConfigureRelation flowConfigureRelation  = flowConfigureRelations.get(i);
                FlowControlTmpl flowControlTmpl  = flowConfigureService.getFlowControlTmplByTemplateId(flowConfigureRelation.getTemplateId());
                String status = (i==(flowConfigureRelations.size()-1)?"E":(i==0?"S":"M"));
                pre = getNewDecisionElement(process, pre, flowControlTmpl,status,product);



            }
        }


        //结束节点
        Element end = new Element("end").setAttribute("displayName","结束节点").setAttribute("name","end");
        process.addContent(end);
        pre.getChild("transition").setAttribute("to","end");

        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");// 设置xml文件的字符为UTF-8，解决中文问题
        XMLOutputter xmlout = new XMLOutputter(format);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        String content = "";
        try {
            xmlout.output(doc, bo);
            content = new String(bo.toByteArray(),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //存储到数据库
        WfTemplet result = null;
        //查看数据库是否存在
        WfTemplet wff =  wfTempletSevice.getByProductIdAndType(product.getId(),flowConfigure.getFlowType().name());
        if(null==wff){
            WfTemplet wfTemplet = new WfTemplet();
            wfTemplet.setProductId(product.getId());
            wfTemplet.setFlowType(flowConfigure.getFlowType());
            wfTemplet.setContent(content);
            wfTemplet.setCreateBy(ShiroSession.getLoginUser().getName());
            wfTemplet.setUpdateBy(ShiroSession.getLoginUser().getName());
            wfTemplet.setCreateTime(new Date());
            wfTemplet.setUpdateTime(new Date());
            result = wfTempletSevice.add(wfTemplet);
        }else{
            wff.setContent(content);
            wff.setUpdateBy(ShiroSession.getLoginUser().getName());
            wff.setUpdateTime(new Date());
            result = wfTempletSevice.update(wff);
        }
        return result;


    }

    /**
     * 将制定的流程转换成snaker的xml文件
     * @param productId
     * @return
     */
    public WfTemplet converSnakerXml(String productId){



        //根据产品Id获取到产品
        Product product = productService.fetchEnableProductById(productId);
        if (null==product){
            return null;
        }


//        //根节点
        Element process = new Element("process").setAttribute("displayName",product.getName()+"审批流程").setAttribute("name",product.getCode());
        Document doc = new Document(process);
        Element pre = null;

        //开始节点
        Element start = new Element("start").setAttribute("displayName","开始节点").setAttribute("name","start");
        Element startTran = new Element("transition").setAttribute("name","startTransition");
        start.addContent(startTran);
        process.addContent(start);
        pre = start;


        //查找产品的业务流程
        FlowControlTmpl business = flowControlTmplService.fetchByProductIdAndType(product.getId(),FlowControlType.BUSINESS_CONTROL);
        if (business!=null){
            pre = getElement(process, pre, business);
        }
        //查找产品的风控流程
        FlowControlTmpl risk = flowControlTmplService.fetchByProductIdAndType(product.getId(),FlowControlType.RISK_CONTROL);
        if(risk!=null){
            pre = getDecisionElement(process, pre, risk,"R",product);
        }
        //查找财务流程
        FlowControlTmpl finance = flowControlTmplService.fetchByProductIdAndType(product.getId(),FlowControlType.FINANCE_CONTROL);
        if(finance!=null){
            pre = getDecisionElement(process, pre, finance,"C",product);
        }
        //结束节点
        Element end = new Element("end").setAttribute("displayName","结束节点").setAttribute("name","end");
        process.addContent(end);
        pre.getChild("transition").setAttribute("to","end");

        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");// 设置xml文件的字符为UTF-8，解决中文问题
        XMLOutputter xmlout = new XMLOutputter(format);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        String content = "";
        try {
            xmlout.output(doc, bo);
             content = new String(bo.toByteArray(),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }




        //存储到数据库
        WfTemplet result = null;
        //查看数据库是否存在
        WfTemplet wff =  wfTempletSevice.getByProductId(product.getId());
        if(null==wff){
            WfTemplet wfTemplet = new WfTemplet();
            wfTemplet.setProductId(product.getId());
            wfTemplet.setContent(content);
            wfTemplet.setCreateBy(ShiroSession.getLoginUser().getName());
            wfTemplet.setUpdateBy(ShiroSession.getLoginUser().getName());
            wfTemplet.setCreateTime(new Date());
            wfTemplet.setUpdateTime(new Date());
            result = wfTempletSevice.add(wfTemplet);
        }else{
            wff.setContent(content);
            wff.setUpdateBy(ShiroSession.getLoginUser().getName());
            wff.setUpdateTime(new Date());
            result = wfTempletSevice.update(wff);
        }
        return result;
    }


    /**
     * 获取流程节点元素
     * @param process
     * @param pre
     * @param flowControlTmpl
     * @return
     */
    private Element getDecisionElement(Element process, Element pre, FlowControlTmpl flowControlTmpl,String type, Product product) {
        Element result =  null;
        if (null!=flowControlTmpl){
            List<FlowControlItem> businessItems = flowControlItemService.queryByTmplId(flowControlTmpl.getId());

            //所有的task节点的列表
            List<Element> elements = new ArrayList<>();


            for (FlowControlItem item:businessItems){
                    Element bussinessElement = new Element("task").setAttribute("assignee",item.getOrganizeId()+"").setAttribute("displayName",item.getName()).setAttribute("name",item.getCode()+"").setAttribute("layout",item.getApproveAmount().toString()+"").setAttribute("taskType","Major");
                Element bussinessTran = new Element("transition").setAttribute("name",item.getCode()+"Transition");
                bussinessElement.addContent(bussinessTran);
                elements.add(bussinessElement);

            }

            //重新整理列表，为列表添加decision节点
            List<Element> elementsAll = new ArrayList<>();
            Element preTans = null;
            for(int i=0;i<elements.size(); i++){
                Element cur = elements.get(i);

                 //1.先添加一个decision节点
                Element decisionElements = new Element("decision").setAttribute("displayName","分支选择").setAttribute("name",cur.getAttribute("name").getValue()+"decision");
                Element decisionTran1 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransition").setAttribute("to",cur.getAttribute("name").getValue());
                decisionElements.addContent(decisionTran1);

                if(null!=preTans){
                    preTans.setAttribute("to",cur.getAttribute("name").getValue()+"decision");
                }

                if(i<elements.size()-1){
                    Element decisionTran2 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransitionNext");
                    decisionElements.addContent(decisionTran2);
                    preTans = decisionTran2;
                }else{

                    Element decisionTran2 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransitionNext");

                    if(!"R".equals(type)){
                        decisionTran2.setAttribute("to","end");
                    }else{
                        FlowControlTmpl finance = flowControlTmplService.fetchByProductIdAndType(product.getId(),FlowControlType.FINANCE_CONTROL);
                        if(finance!=null){
                            lastElement = decisionTran2;
                        }else {
                            decisionTran2.setAttribute("to","end");
                        }
                    }
                    decisionElements.addContent(decisionTran2);
                }

                //为decision节点添加expr表达式根据金额选择节点执行顺序
                decisionElements.setAttribute("expr","${amount > "+cur.getAttributeValue("layout")+" ? '"+cur.getAttribute("name").getValue()+"DecisionTransition'"+" : '"+cur.getAttribute("name").getValue()+"DecisionTransitionNext'}");


                elementsAll.add(decisionElements);

                 //2.添加task节点
                elementsAll.add(cur);

                 //3.建立联系关系
                if(i>0){
                    elements.get(i-1).getChild("transition").setAttribute("to",cur.getAttribute("name").getValue()+"decision");
                }



            }

            //添加到文档流
            for(int i=0; i<elementsAll.size(); i++){
                process.addContent(elementsAll.get(i));
            }

            //为第一个节点添加指向
            Element first = elementsAll.get(0);
            pre.getChild("transition").setAttribute("to",first.getAttribute("name").getValue());

           if("C".equals(type)){
               if(lastElement!=null){
                   lastElement.setAttribute("to",first.getAttributeValue("name"));
               }
           }



            result = elementsAll.get(elementsAll.size()-1);
        }
        return result;
    }



    private Element getNewDecisionElement(Element process, Element pre, FlowControlTmpl flowControlTmpl,String status,Product product) {
        Element result =  null;
        if (null!=flowControlTmpl){
            List<FlowControlItem> businessItems = flowControlItemService.queryByTmplIdOrderByCodeNum(flowControlTmpl.getId());

            //所有的task节点的列表
            List<Element> elements = new ArrayList<>();


            for (FlowControlItem item:businessItems){
                Element bussinessElement = new Element("task").setAttribute("assignee",item.getOrganizeId()+"").setAttribute("displayName",item.getName()).setAttribute("name",item.getCode()+"").setAttribute("layout",item.getApproveAmount().toString()+"").setAttribute("enterprise",item.enterprise()).setAttribute("taskType","Major");
                Element bussinessTran = new Element("transition").setAttribute("name",item.getCode()+"Transition");
                bussinessElement.addContent(bussinessTran);
                elements.add(bussinessElement);

            }

            //重新整理列表，为列表添加decision节点
            List<Element> elementsAll = new ArrayList<>();
            Element preTans = null;
            for(int i=0;i<elements.size(); i++){
                Element cur = elements.get(i);

                //1.先添加一个decision节点
                Element decisionElements = new Element("decision").setAttribute("displayName","分支选择").setAttribute("name",cur.getAttribute("name").getValue()+"decision");
                Element decisionTran1 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransition").setAttribute("to",cur.getAttribute("name").getValue());
                decisionElements.addContent(decisionTran1);

                if(null!=preTans){
                    preTans.setAttribute("to",cur.getAttribute("name").getValue()+"decision");
                }
                Element decisionTran2 =null;
                if(i<elements.size()-1){
                    decisionTran2 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransitionNext");
                    decisionElements.addContent(decisionTran2);
                    preTans = decisionTran2;
                }else{

                    decisionTran2 = new Element("transition").setAttribute("name",cur.getAttribute("name").getValue()+"DecisionTransitionNext");
                    //如果是最后一个则end ，否则不是
                    if("E".equals(status)){
                        decisionTran2.setAttribute("to","end");
                    }
                    decisionElements.addContent(decisionTran2);
                }

                //为decision节点添加expr表达式根据金额选择节点执行顺序
                decisionElements.setAttribute("expr","${amount > "+cur.getAttributeValue("layout")+" ? '"+cur.getAttribute("name").getValue()+"DecisionTransition'"+" : '"+cur.getAttribute("name").getValue()+"DecisionTransitionNext'}");


                elementsAll.add(decisionElements);

                //2.添加task节点
                elementsAll.add(cur);

                //3.建立联系关系
                if(i>0){
                    elements.get(i-1).getChild("transition").setAttribute("to",cur.getAttribute("name").getValue()+"decision");
                }else{
                    if(!"S".equals(status)){
                        if(lastElement!=null){
                            lastElement.setAttribute("to",decisionElements.getAttributeValue("name"));
                        }
                    }
                }
                lastElement = decisionTran2;
            }
            //添加到文档流
            for(int i=0; i<elementsAll.size(); i++){
                process.addContent(elementsAll.get(i));
            }

            //为第一个节点添加指向
            Element first = elementsAll.get(0);
            pre.getChild("transition").setAttribute("to",first.getAttribute("name").getValue());


            result = elementsAll.get(elementsAll.size()-1);
        }
        return result;
    }

    /**
     * 获取流程节点元素
     * @param process
     * @param pre
     * @param flowControlTmpl
     * @return
     */
    private Element getElement(Element process, Element pre, FlowControlTmpl flowControlTmpl) {
        if (null!=flowControlTmpl){
            List<FlowControlItem> businessItems = flowControlItemService.queryByTmplId(flowControlTmpl.getId());
            for (FlowControlItem item:businessItems){
                Element bussinessElement = new Element("task").setAttribute("assignee",item.getOrganizeId()+"").setAttribute("displayName",item.getName()).setAttribute("name",item.getCode()+"").setAttribute("taskType","Major");
                Element bussinessTran = new Element("transition").setAttribute("name",item.getCode()+"Transition");
                bussinessElement.addContent(bussinessTran);
                process.addContent(bussinessElement);
                pre.getChild("transition").setAttribute("to",item.getCode()+"");
                pre = bussinessElement;
            }
        }
        return pre;
    }


    /**
     * 根据产品Id查找
     * @param productId
     * @return
     */
    public List<FlowControlTmpl> fetchByProdyctId(String productId) {
        return dao().query(FlowControlTmpl.class,Cnd.where("status","=", PublicStatus.ABLE).and("productId","=",productId));
    }


    /**
     * 检测产品流程是否配置完成
     * @param productId
     * @return
     */
    public boolean checkComplete(String productId){
        List<FlowControlTmpl> flowControlTmpls =  flowControlTmplService.fetchByProdyctId(productId);
        if(null==flowControlTmpls||flowControlTmpls.size()<1){
            return false;
        }else{
            return true;
        }
    }


    /**
     * 通过产品Id和产品类型Id查询
     * @param productId
     * @param type
     * @return
     */
    public List<FlowControlTmpl> query(String productId, FlowControlType type) {
        return dao().query(FlowControlTmpl.class,Cnd.where("type","=",type).and("productId","=",productId));
    }



    /**
     * 复制产品流程
     * @param curProductId 需要复制的产品的Id
     * @param productId  复制到的产品的Id
     * @param productTypeId 复制到的产品类型的Id
     */
    public boolean copyFlowControl(String curProductId,String productId,String productTypeId){

        //判断参数
        if(StringUtils.isEmpty(curProductId)||StringUtils.isEmpty(productId)||StringUtils.isEmpty(productTypeId)){
            return false;
        }

        //查找产品
        Product curProudct = productService.fetchEnableProductById(curProductId);
        if(null==curProudct){
            return false;
        }
        //复制到的产品
        Product product = productService.fetchEnableProductById(productId);
        if(null==product){
            return false;
        }

        ProductType productType = productTypeService.fetchById(productTypeId);
        if(null==productType){
            return false;
        }
        //根据产品Id查找到当前的所有的流程和流程子项
        List<FlowControlTmpl> flowCrotrolTmpls =  flowControlTmplService.queryByProductId(curProductId);
        if(null==flowCrotrolTmpls||flowCrotrolTmpls.size()<1||flowCrotrolTmpls.size()>2){
             return false;
        }

        //先删除掉该产品的相关节点
        List<FlowControlTmpl> tmpls =  flowControlTmplService.queryByProductId(productId);
        if(null!=tmpls&&tmpls.size()>0){
            tmpls.stream().forEach(t->{
                flowControlItemService.deleteByItemId(t.getId());
                flowControlTmplService.deleteById(t.getId());
            });
        }



        for (FlowControlTmpl tmpl: flowCrotrolTmpls){
            tmpl.setName(tmpl.getName()+"-"+product.getName());
            tmpl.setProductId(productId);
            tmpl.setProductTypeId(productTypeId);
            tmpl.setCreateBy(ShiroSession.getLoginUser().getName());
            tmpl.setUpdateBy(ShiroSession.getLoginUser().getName());
            tmpl.setCreateTime(new Date());
            tmpl.setUpdateTime(new Date());
            tmpl.setCode(tmpl.getType().getCode()+(product==null?"":product.getCode()));

            List<FlowControlItem> flowControlItems = flowControlItemService.queryByTmplId(tmpl.getId());
            FlowControlTmpl flowControlTmpl = flowControlTmplService.add(tmpl);
            for (FlowControlItem flowControlItem:flowControlItems){
                flowControlItem.setTmplId(flowControlTmpl.getId());
                flowControlItem.setCreateBy(ShiroSession.getLoginUser().getName());
                flowControlItem.setUpdateBy(ShiroSession.getLoginUser().getName());
                flowControlItem.setCreateTime(new Date());
                flowControlItem.setUpdateTime(new Date());
            }
            flowControlItemService.batchAdd(flowControlItems);
        }
        return true;
    }

    /**
     * 通过Id删除
     * @param id
     */
    public boolean deleteById(String id) {
        if(StringUtils.isEmpty(id)){
            return false;
        }
        return dao().clear(FlowControlTmpl.class,Cnd.where("id","=",id))>0;
    }

    /**
     * 根据产品Id获取到相关流程
     * @param productId
     * @return
     */
    public List<FlowControlTmpl> queryByProductId(String productId) {
         return dao().query(FlowControlTmpl.class,Cnd.where("productId","=",productId).and("status","=",PublicStatus.ABLE));
    }
}
