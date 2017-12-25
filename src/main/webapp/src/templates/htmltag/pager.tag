

    <%
        var start = 1;
        var toURL = request.requestURL;
        var end = pager.pageNumber + 1;
        if (end > pager.pageCount) {
            end = pager.pageCount;
        }


    %>

    <tr>
        <form method="post" action="" name="pagerForm">
            <% for (param in parameter.map) {
              if (param.key != "pageNo" && param.value != null) { %>
            <input type="hidden" name="${param.key}" value="${param.value[0]}"/>
            <%
                  }
                }
            %>
            <input type="hidden" name="pageNo" value="1"/>
            <th id="pager_th">
              <div class="ui right floated pagination menu">
                  <%
                      if (pager.pageNumber == 1) {
                  %>
                  <a class="icon item disabled">
                    <i class="left chevron icon"></i>
                  </a>
                  <% } else {%>
                     <a class="icon item" href="JavaScript:turnOverPage(${pager.pageNumber - 1})">
                         <i class="left chevron icon"></i>
                     </a>
                  <% }
                      for (var i = start; i <= end; i++) {
                        if (pager.pageNumber == i) {

                  %>
                  <a class="item active">${i}</a>

                  <%} else {%>
                  <a class="item" href="javascript:turnOverPage(${i})">${i}</a>

                  <% }
                  }
                    if (pager.pageNumber == pager.pageCount) {
                  %>
                    <a class="icon item disabled">
                      <i class="right chevron icon"></i>
                    </a>
                  <% } else { %>
                    <a class="icon item" href="javascript:turnOverPage(${pager.pageNumber + 1})">
                      <i class="right chevron icon"></i>
                    </a>
                  <% } %>
            </div>
        </th>
    </tr>
</form>


<script language="javascript">

    $(document).ready(function () {
        var colsNum = calculateCols($("table:first"));
        $("#pager_th").attr("colspan", colsNum);
        $("#pager_th").show();
    });

    function calculateCols(table) {
        var trs = $(table).find("th");
        return trs.length - 1;
    }


    function turnOverPage(no) {
        var qForm = document.pagerForm;
        if (no >${pager.pageCount}) {
            no =${pager.pageCount};
        }
        if (no < 1) {
            no = 1;
        }
        qForm.pageNo.value = no;
        qForm.action = "${toURL}";
        qForm.submit();
    }
</script>

<style>
    #pager_th{
        display: none;
    }
</style>
