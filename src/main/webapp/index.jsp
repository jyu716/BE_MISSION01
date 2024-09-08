<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html><html lang="ko">
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>와이파이 정보 구하기</title>
</head>
<body>

<script>
    function getLocation(){
        if(navigator.geolocation){
            navigator.geolocation.getCurrentPosition(function(pos){
                document.getElementById("latitude").value = pos.coords.latitude;
                document.getElementById("longitude").value = pos.coords.longitude;
            })
        }else{
            alert("Geolocation은 이 브라우저에서 지원되지 않습니다.")
        }
    }
</script>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>
    function fetchWifiInfo() {
        var latitude = document.getElementById('latitude').value;
        var longitude = document.getElementById('longitude').value;

        if(latitude == 0 && longitude == 0){
            alert("내 위치를 가져와 주세요");
            return;
        }

        $.ajax({
            url: "/SearchData",
            type: "POST",
            data: {
                lat: latitude,
                lon: longitude
            },
            contentType: "application/x-www-form-urlencoded; charset=UTF-8",
            success: function(data){

                const table = document.getElementById('listBody');
                while (table.rows.length > 0) {
                    table.deleteRow(0); // 첫 번째 행부터 삭제
                }

                //받은 데이터 파싱 후 테이블 추가.

                if(data.length > 0) {
                    data.forEach(function (item) {
                        const newRow = table.insertRow();

                        function addCell(text) {
                            const newCell = newRow.insertCell(); // 새로운 셀 추가
                            newCell.innerText = text;            // 셀에 텍스트 추가
                        }

                        addCell((item.distance / 1000).toFixed(4) + " km");
                        addCell(item.X_SWIFI_MGR_NO);
                        addCell(item.X_SWIFI_WRDOFC);
                        addCell(item.X_SWIFI_MAIN_NM);
                        addCell(item.X_SWIFI_ADRES1);
                        addCell(item.X_SWIFI_ADRES2 || '-');
                        addCell(item.X_SWIFI_INSTL_FLOOR || '-');
                        addCell(item.X_SWIFI_INSTL_TY);
                        addCell(item.X_SWIFI_INSTL_MBY);
                        addCell(item.X_SWIFI_SVC_SE);
                        addCell(item.X_SWIFI_CMCWR);
                        addCell(item.X_SWIFI_CNSTC_YEAR);
                        addCell(item.X_SWIFI_INOUT_DOOR);
                        addCell(item.X_SWIFI_REMARS3 || '-');
                        addCell(item.LAT);
                        addCell(item.LNT);
                        addCell(item.WORK_DTTM);
                    });
                }else{
                    const newRow = table.insertRow();          // 새로운 행 추가
                    const newCell = newRow.insertCell();       // 새로운 셀 추가
                    newCell.colSpan = 17;                      // colspan을 17로 설정
                    newCell.innerText = "검색 결과가 없습니다.";  // 셀에 메시지 추가
                    newCell.style.textAlign = "center";
                }
            },
            error: function(xhr, status, error){

            }


        });

    }
</script>

<h1>와이파이 정보 구하기</h1>

    <jsp:include page="header.jsp"></jsp:include>

    <div class="form-container">
        LAT: <input type="text" id="latitude" value="0.0">
        LNT: <input type="text" id="longitude" value="0.0">
        <button onclick="getLocation()">내 위치 가져오기</button>

        <button onclick="fetchWifiInfo()">근처 WIFI 정보 받기</button>
    </div>
<br/>
<table id="tables">
<tr id="list">
            <th>거리(Km)</th>
            <th>관리번호</th>
            <th>자치구</th>
            <th>와이파이명</th>
            <th>도로명주소</th>
            <th>상세주소</th>
            <th>설치위치(층)</th>
            <th>설치유형</th>
            <th>설치기관</th>
            <th>서비스구분</th>
            <th>망종류</th>
            <th>설치년도</th>
            <th>실내외구분</th>
            <th>WIFI접속환경</th>
            <th>X좌표</th>
            <th>Y좌표</th>
            <th>작업일자</th>
        </tr>

    <tbody id="listBody">
    <tr>
        <td colspan="17" id="notice">위치 정보를 입력한 후에 조회해 주세요.</td>
    </tr>
    </tbody>
</table>
</body>
</html>