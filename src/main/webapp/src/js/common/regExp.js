/**
 * Created by pengs on 2017/1/3.
 */
//验证是小数且必须为两位小数
//todo 这条以后作废，用canBeDecimal
$.fn.form.settings.rules.validateDecimal2 = function (value) {
    var reg = new RegExp("^\\d+(\\.\\d{1," + 2 + "})?$", "gim")
    return reg.test(value);
};

/**
 * 验证小数
 * @param {Object} n 最多位数，最小值为1
 * @param {Object} value 值
 */
$.fn.form.settings.rules.canBeDecimal = function (value, n) {
    var r = n ? n : 1;
    var reg = new RegExp("^\\d+(\\.\\d{1," + r + "})?$", "gim")
    return reg.test(value) || value =='';
}

/**
 * 验证小数点后前后位数
 * @param {Object} n='1,13'
 * @param {Object} value 值
 * b 整数部位位数
 * r 小数点后位数
 *
 * @param {Object} n='12'
 * b 校验是否为正整数,且为n位
 */
$.fn.form.settings.rules.newCanBeDecimal = function(value ,n){
    if(n.indexOf(',')>-1){
        var b = parseFloat(n.split(',')[0]);
        var r = parseFloat(n.split(',')[1]);
        var l = r > 1 ? r : 1;
        var reg = new RegExp("^\\d+(\\.\\d{1," + l + "})?$", "gim");
    }else{
        var b = parseFloat(n);
        var reg = new RegExp("^[1-9]*[1-9][0-9]*$","gim");
    }
    if(value.indexOf('.')>-1){
        var flag = value.split('.')[0].length <= b;
    }else{
        var flag = value.length <= b;
    }
    return (reg.test(value) && flag) || value == "";
}


//验证两个关联的输入框后者必须必前者的值要大 其中id为前者输入框的id
$.fn.form.settings.rules.greater = function (value, id) {
    return parseFloat(value) >= parseFloat($(("#" + id)).val());
};

//验证字符串只能为中文数字字母和*()
$.fn.form.settings.rules.validateNotSpecial = function (value) {
    return /[\u4e00-\u9fa5a-z0-9()*]+$/i.test(value) || value == "";
};
//验证字符串只能为中文数字字母和*()
$.fn.form.settings.rules.validateCharNum = function (value) {
    return /[a-z0-9()]+$/i.test(value) || value == "";
};

//验证数字和小数（2位小数）和数值范围
$.fn.form.settings.rules.validateNumFloat = function (value,args) {
    if(args && args.indexOf('-')>-1){
        minNum = parseFloat(args.split('-')[0]);
        maxNum = parseFloat(args.split('-')[1]);
        return (/^[0-9]+([.]{1}[0-9]{1,2})?$/.test(value) && (value >= minNum && value <= maxNum)) || value == "";
    }else{
        return (/^[0-9]+([.]{1}[0-9]{1,2})?$/.test(value) && (value >= 0.01 && value <= 99999999.99)) || value == "";
    }
};
//验证数字
$.fn.form.settings.rules.validateNum = function (value) {
    var Val = value.replace(/\s/g,'');
    return /^\d{1,99}$/.test(Val)|| value == "";
}
//验证不包含字符
$.fn.form.settings.rules.validateChar = function (value,args) {
    var result=/[_\~\!\@\#\$\%\^\&\*\(\)\-\_\+\=\[\]\{\}\|\\\;\:\'\"\,\.\/\<\>\?]+/.test(value);
    return !result;
};

//格式为z_chRange[40,10]40位字符，10位汉字;z_chRange[40],只允许中文，且限制字数为40
$.fn.form.settings.rules.z_chRange = function (value,args) {
    var z_ch =  /^[\u4E00-\u9FA5]+$/;
    var val_length = value.length;
    if(args.indexOf(',')>-1){
        var maxLength = args.split(',')[0];
        var maxChinese = args.split(',')[1];
        if(val_length <= maxLength){
            if(z_ch.test(value) && val_length <= maxChinese){
                return true;
            }else if(!z_ch.test(value) && val_length <= maxLength){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }else{
        return (z_ch.test(value) && val_length < args) || value == "";
    }
};
//validateLen[10]位数需小于10位
$.fn.form.settings.rules.validateLen = function (value,args) {
    var val_length = value.length;
    return (val_length < args);
};


//验证手机号码
$.fn.form.settings.rules.mobile = function (value) {
    return /^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$/.test(value) || value == "";
};

//验证数字必须在区间内
$.fn.form.settings.rules.between = function (value, args) {
    var min = parseFloat(args.split(",")[0]);
    var max = parseFloat(args.split(",")[1]);
    if (parseFloat(value) < min || parseFloat(value) > max) {
        return false;
    } else {
        return true;
    }
}

//身份证
$.fn.form.settings.rules.identityCodeValid = function (value) {
    var city = {
        11: "北京",
        12: "天津",
        13: "河北",
        14: "山西",
        15: "内蒙古",
        21: "辽宁",
        22: "吉林",
        23: "黑龙江 ",
        31: "上海",
        32: "江苏",
        33: "浙江",
        34: "安徽",
        35: "福建",
        36: "江西",
        37: "山东",
        41: "河南",
        42: "湖北 ",
        43: "湖南",
        44: "广东",
        45: "广西",
        46: "海南",
        50: "重庆",
        51: "四川",
        52: "贵州",
        53: "云南",
        54: "西藏 ",
        61: "陕西",
        62: "甘肃",
        63: "青海",
        64: "宁夏",
        65: "新疆",
        71: "台湾",
        81: "香港",
        82: "澳门",
        91: "国外 "
    };
    var tip = "";
    var pass = true;

    if (!value || !/^\d{6}(18|19|20)?\d{2}(0[1-9]|1[012])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i.test(value)) {
        tip = "身份证号格式错误";
        pass = false;
    } else if (!city[value.substr(0, 2)]) {
        tip = "地址编码错误";
        pass = false;
    } else {
        //18位身份证需要验证最后一位校验位
        if (value.length == 18) {
            value = value.split('');
            //∑(ai×Wi)(mod 11)
            //加权因子
            var factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
            //校验位
            var parity = [1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2];
            var sum = 0;
            var ai = 0;
            var wi = 0;
            for (var i = 0; i < 17; i++) {
                ai = value[i];
                wi = factor[i];
                sum += ai * wi;
            }
            var last = parity[sum % 11];
            if (parity[sum % 11] != value[17]) {
                tip = "校验位错误";
                pass = false;
            }
        }
    }
    // if (!pass) alert(tip);
    return pass || value == "";
}

//银行卡
$.fn.form.settings.rules.bankCard = function (value) {
    var Val = value.replace(/\s/g,'');
    return /^\d{6,30}$/.test(Val)|| value == "";
}

//验证两个关联的输入框后者必须必前者的值要小 其中id为前者输入框的id
$.fn.form.settings.rules.smaller = function (value, id) {
    return parseFloat(value) <= parseFloat($(("#" + id)).val());
};