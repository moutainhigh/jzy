var  productTypeCascade = TwoCascade({parentSelect:$("#productType"),subSelect:$("#product")})
productTypeCascade.create();
 /**
  * 重置表单
  */
 function queryFormReset() {
     productTypeCascade.reset();
     $("#borrower").val("");
     $("#minAmount").val("");
     $("#maxAmount").val("");
     $("#status").val("");
     $(".js_search.close").each(function(){
         $(this).click();
     });
 }

/**
 * 查询列表
 */
function queryList() {

    var dtTable = $("#loanList").DataTable({
       serverSide: true,//服务端分页
       searching: false,//显示默认搜索框
       ordering: false,//开启排序
       bDestroy: true,
       "ajax": {
           "url": "/business_apply/query_finance_approval_complete_list",
           "data": function(d){
               var data = {};
               data.product = $('#product').val();
               data.productType = $('#productType').val();
               data.borrower= $("#borrower").val();
               data.minAmount= $("#minAmount").val();
               data.maxAmount= $("#maxAmount").val();
               data.status= $("#status").val();
               data.channelId= $("#channelId").val();
               var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
               return JSON.stringify(_d);
           },
           "type": "POST"
       },
       columns: [
           {data: 'code'},
           {data: "productTypeName"},
           {data: "productName"},
           {data: "saleName"},
           {data: "borrserName"},
           {data: null},
           {data: null},
           {data: null},
           {data: null},
           {data: null}
       ],
       columnDefs: [{
           targets:5,
           className:'right aligned',
           render: function (data, type, row, meta) {
               return accounting.formatMoney(data.amount,'',2,',','.');
           }
       },{
           targets:6,
           render: function (data, type, row, meta) {
               if("FIXED_DATE"==data.termType){
                   return "至"+data.term;
               }else{
                   if(data.termType!=undefined && enums.loanTermType[data.termType] != undefined){
                       return data.term + enums.loanTermType[data.termType];
                   }else{
                       return "--";
                   }

               }
           }
       }, {
           targets:7,
           render: function (data, type, row, meta) {
               return enums.loan_status[data.loanStatus];
           }
       },{
           targets:8,
           render: function (data, type, row, meta) {
               if("CANCEL" == row.loanStatus || "SAVE" == row.loanStatus){
                   return enums.loan_status[row.loanStatus];
               }
               if("APPROVEEND" == row.loanStatus || "LOANED" == row.loanStatus || "CLEARED" == row.loanStatus || "OVERDUE" == row.loanStatus || "APPROVEREJECT" == row.loanStatus){
                   return data.approveStatus;
               }else {
                   if(null != data.nextStatus){
                       return data.nextStatus+'-等待审批';
                   }
                   return data.approveStatus;
               }
           }
       }, {
           className: "single line",
           targets:9,
           render: function (data, type, row, meta) {
               return '<a target="_blank" href="/business_apply/detail?id=' + row.id + '&tab=riskControl&process=false" class="ui mini basic button"><i class="info circle icon"></i>查看</a>';
           }
       }
       ],
       "iDisplayLength": 20,
       "aLengthMenu": [
           [20],
           [20]
       ],
   });
}

queryList();

 function getManager(obj){
     $('#'+obj).search({
                           apiSettings: {
                               method: "post",
                               url: '/channel/list_channel_name' + '?channelName={query}&channelType=1'
                           },
                           fields: {
                               results: 'data',
                               title: 'name',
                               description: 'code'
                           },
                           onSelect: function (data) {
                               var $this = $('#'+obj);
                               $this.find('input').attr('readonly',true);
                               $this.find('.js_search').addClass('close');
                               $this.find('.js_search').css('pointer-events','auto');
                               $("#channelId").val(data.id);
                           },onResults: function(data) {}
                       });
 }
 getManager('managerChannelSearch');
 $(document).on('click','.js_search.close',function(){
     $(this).css('pointer-events','none');
     $(this).siblings('input').attr('readonly',false);
     $(this).siblings('input').val("");
     $(this).removeClass('close');
     $("#channelId").val("");
 });