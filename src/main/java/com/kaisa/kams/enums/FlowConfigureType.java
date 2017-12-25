package com.kaisa.kams.enums;

import com.kaisa.kams.components.view.flow.FlowConfigureTypeVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by zhouchuang on 2017/8/29.
 */
@AllArgsConstructor
@Getter
public enum FlowConfigureType {

    BORROW_APPLY("BA","借款申请流程"),
    COST_WAIVER("CW","费用免除流程"),
    BROKERAGE_FEE("BF","居间费付款流程"),
    MORTGAGE("DY","房产抵押流程"),
    DECOMPRESSION("JY","房产解押流程"),
    EXTENSION("ZQ","业务展期流程"),
    LOAN_PUSH("LP","推单流程");

    private String code;
    private String description;

    public static FlowConfigureTypeVO getModule(FlowConfigureType flowConfigureType){
        FlowConfigureTypeVO flowConfigureTypeVO  =new FlowConfigureTypeVO();
        flowConfigureTypeVO.setCode(flowConfigureType.getCode());
        flowConfigureTypeVO.setDesc(flowConfigureType.getDescription());
        flowConfigureTypeVO.setName(flowConfigureType.name());
        return flowConfigureTypeVO;
    }
}
