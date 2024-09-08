<%@ page import="com.google.gson.Gson" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.mission_01.MyHistoryData" %>
<%@ page import="com.google.gson.reflect.TypeToken" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html><html lang="ko">
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>와이파이 정보 구하기</title>
    <script>
        function removeRow(id){
            alert("Remove ID:"+id);

            $.ajax({
                url: "/historys",
                type: "POST",
                data: {
                    id : id
                },
                contentType: "application/x-www-form-urlencoded; charset=UTF-8",
                success: function (data){

                },
                error: function (xhr, satus, error){

                }
            })

            $("#row"+id).remove()
        }
    </script>
</head>
<body>
<h1>위치 히스토리 목록</h1>

<jsp:include page="header.jsp"/>
<br/>
<table id="tables">
    <tr id="list">
        <th>ID</th>
        <th>X좌표</th>
        <th>Y좌표</th>
        <th>조회일자</th>
        <th>비고</th>
    </tr>
    <%
        Gson gson = new Gson();
        String myHistorys = (String) request.getAttribute("myHistorys");
        System.out.println(myHistorys);
        List<MyHistoryData> historyData = gson.fromJson(myHistorys, new TypeToken<List<MyHistoryData>>(){}.getType());

        for(MyHistoryData history : historyData) {
        %>
            <tr id="row<%= history.getID() %>">
                <th><%=history.getID()%></th>
                <th><%=history.getLAT()%></th>
                <th><%=history.getLON()%></th>
                <th><%=history.getDATE()%></th>
                <th>
                    <button onclick="removeRow(<%= history.getID() %>)">삭제</button>
                </th>
            </tr>
    <%
        }
    %>
</table>

</body>
</html>