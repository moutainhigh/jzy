package com.kaisa.kams.components.service.flow;

import com.kaisa.kams.models.flow.FlowControlItem;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;

/**
 *
 * Created by weid on 2016/12/7.
 */
@IocBean(fields="dao")
public class FlowControlItemService extends IdNameEntityService<FlowControlItem> {
    private static final Log log = Logs.get();

    public void batchAdd(List<FlowControlItem> flowControlItemList) {
        flowControlItemList.stream().forEach(fci->{
            dao().insert(fci);
        });
    }

    /**
     * 根据流程Id查找
     * @param tmplId
     * @return
     */
    public List<FlowControlItem> queryByTmplId(String tmplId) {
       return dao().query(FlowControlItem.class, Cnd.where("tmplId","=",tmplId).asc("code"));
    }
    /**
     * 根据流程Id查找并且按数字大小排序
     * @param tmplId
     * @return
     */
    public List<FlowControlItem> queryByTmplIdOrderByCodeNum(String tmplId){
        List<FlowControlItem> flowControlItems  =  dao().query(FlowControlItem.class, Cnd.where("tmplId","=",tmplId));
        flowControlItems.sort((FlowControlItem item1, FlowControlItem item2) -> Integer.valueOf(item1.getCode().substring(1)).compareTo(Integer.valueOf(item2.getCode().substring(1))));
        return flowControlItems;
    }

    public FlowControlItem queryByTmplIdAndname(String tmplId,String name){
        return dao().fetch(FlowControlItem.class, Cnd.where("tmplId","=",tmplId).and("name","=",name));
    }

    /**
     * 通过 流程Id删除节点
     * @param tmplId
     */
    public void deleteByItemId(String tmplId) {
        dao().clear(FlowControlItem.class,Cnd.where("tmplId","=",tmplId));
    }
}
