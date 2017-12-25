/**
 * Created by pengs on 2016/11/25.
 */
/**
 * 将表单的值序列化为对象
 * 此为semantic api方法  {serializeForm:true}的补丁
 *
 */
$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};
