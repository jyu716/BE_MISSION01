<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
   <link href="style.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="infoWrap">
<%
    String wifiInfoJson = (String) request.getAttribute("wifiInfoJson");
    if (wifiInfoJson != null && !wifiInfoJson.isEmpty()) {
%>
        <h1 id="InfoCount">
            <%= wifiInfoJson %>개의 WIFI 정보를 정상적으로 저장하였습니다.
        </h1>
<%
    } else {
        System.out.println("wifiInfo is null");
    }
%>

<a href="#" id="backHome" onclick="history.back()"> 홈으로 가기 </a>
</div>
</body>
</html>