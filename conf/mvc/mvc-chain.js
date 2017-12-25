/**
 * 动作链
 * Created by weid on 2016/11/23.
 */

var chain={
    "default" : {
        "ps" : [
            "com.kaisa.kams.components.aop.LogTimeProcessor",
            "org.nutz.mvc.impl.processor.UpdateRequestAttributesProcessor",
            "org.nutz.mvc.impl.processor.EncodingProcessor",
            "org.nutz.mvc.impl.processor.ModuleProcessor",
            "org.nutz.mvc.impl.processor.ActionFiltersProcessor",
            "org.nutz.integration.shiro.NutShiroProcessor",
            "org.nutz.mvc.impl.processor.AdaptorProcessor",
            "org.nutz.mvc.impl.processor.MethodInvokeProcessor",
            "org.nutz.mvc.impl.processor.ViewProcessor"

        ],
        "error" : 'org.nutz.mvc.impl.processor.FailProcessor'
    }
};
