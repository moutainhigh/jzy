/**
 * Created by wangqx on 2017/6/13.
 */
var TwoCascade = (function (window) {

    var cascadeData;
    var options;

    var TwoCascade = function (option) {
        return new TwoCascade.fn.init(option);
    };

    TwoCascade.fn = TwoCascade.prototype = {
        constructor: TwoCascade,
        init: function (option) {
            options = option;
            this.url = "product type cascade";
            this.parentSelect = option.parentSelect;
            this.subSelect = option.subSelect;
            initOption(this.parentSelect);
            initOption(this.subSelect);
            this.create = function () {
                this.build();
            },
            this.reset = function() {
                resetParentSelectOptions();
            }
        },
        build: function () {
            var url = this.url;
            $(document).api({
                on: "now",
                method: "post",
                action: url,
                onSuccess: function (data) {
                    cascadeData = data;
                    setParentSelectOptions();
                    bindParentSelectEvent();
                }
            });
        },



    };

    var initOptionName = "全部";
    var initOptionValue = "";

    var initOption = function(select) {
        if (select) {
            select.empty();
            addOption(select,initOptionName,initOptionValue);
        }
    };

    var addOption = function (select, optionName, optionValue) {
        select.append("<option value='"+optionValue+"'>"+optionName+"</option>");
    };


    var resetParentSelectOptions = function() {
        initOption(options.parentSelect);
        initOption(options.subSelect);
        setParentSelectOptions();
    };

    var setParentSelectOptions = function() {
        for (var i=0;i<cascadeData.length;i++) {
            addOption(options.parentSelect,cascadeData[i].productTypeName,cascadeData[i].productTypeId);
        }
    };

    var bindParentSelectEvent = function(){
        options.parentSelect.on("change", function(){
            var parentId = $(this).children('option:selected').val();
            setSubSelectOptions(parentId);
        });
    };

    var setSubSelectOptions = function(parentId) {
        initOption(options.subSelect);
        if ("" != parentId) {
            var subOptions = getSubOptions(parentId);
            for (var i = 0; i < subOptions.length; i++) {
                addOption(options.subSelect, subOptions[i].productName, subOptions[i].productId);
            }
        }
    };

    var getSubOptions = function(parentId) {
        for (var i=0;i<cascadeData.length;i++) {
            if (parentId == cascadeData[i].productTypeId) {
                return cascadeData[i].products;
            }
        }
    };

    TwoCascade.fn.init.prototype = TwoCascade.fn;

    return TwoCascade;
})();
