package org.example.mission_01;

import com.google.gson.Gson;
import org.example.mission_01.ROW;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/SearchData")
public class SearchData extends HttpServlet {

    // WIFIs.db의 경로는 C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin 톰캣안 bin 파일에 있음.
    String projectPath = System.getProperty("user.dir");
    String DBinfo = "jdbc:sqlite:"+projectPath+"/WIFIs.db";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("here is SearchData");

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String lat = req.getParameter("lat");
        String lon = req.getParameter("lon");

        System.out.println("lat:"+lat+", lon:"+lon);


        if (lat == null || lon == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Latitude and Longitude are required.\"}");
            return;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC 드라이버를 로드할 수 없습니다: " + e.getMessage());
        }

        insertHistory(lat, lon);

        // SQLite 쿼리: 검색시 내 주변에서 가까운 20개를 출력.
        String query = "SELECT * FROM (" +
                "SELECT *, " +
                "(6371000 * acos(cos(radians(?)) * cos(radians(LAT)) * cos(radians(LNT) - radians(?)) + sin(radians(?)) * sin(radians(LAT)))) AS distance " +
                "FROM wifi_info) " +
                "ORDER BY distance " +
                "LIMIT 20";


        try (Connection conn = DriverManager.getConnection(DBinfo);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // 위도와 경도 값을 쿼리에 바인딩
            pstmt.setString(1, lat);
            pstmt.setString(2, lon);
            pstmt.setString(3, lat);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ROW> wifiInfos = new ArrayList<>();

                while (rs.next()) {
                    ROW wifiInfo = new ROW();
                    wifiInfo.setDistance(rs.getDouble("distance"));
                    wifiInfo.setX_SWIFI_MGR_NO(rs.getString("X_SWIFI_MGR_NO"));
                    wifiInfo.setX_SWIFI_WRDOFC(rs.getString("X_SWIFI_WRDOFC"));
                    wifiInfo.setX_SWIFI_MAIN_NM(rs.getString("X_SWIFI_MAIN_NM"));
                    wifiInfo.setX_SWIFI_ADRES1(rs.getString("X_SWIFI_ADRES1"));
                    wifiInfo.setX_SWIFI_ADRES2(rs.getString("X_SWIFI_ADRES2"));
                    wifiInfo.setX_SWIFI_INSTL_FLOOR(rs.getString("X_SWIFI_INSTL_FLOOR"));
                    wifiInfo.setX_SWIFI_INSTL_TY(rs.getString("X_SWIFI_INSTL_TY"));
                    wifiInfo.setX_SWIFI_INSTL_MBY(rs.getString("X_SWIFI_INSTL_MBY"));
                    wifiInfo.setX_SWIFI_SVC_SE(rs.getString("X_SWIFI_SVC_SE"));
                    wifiInfo.setX_SWIFI_CMCWR(rs.getString("X_SWIFI_CMCWR"));
                    wifiInfo.setX_SWIFI_CNSTC_YEAR(rs.getString("X_SWIFI_CNSTC_YEAR"));
                    wifiInfo.setX_SWIFI_INOUT_DOOR(rs.getString("X_SWIFI_INOUT_DOOR"));
                    wifiInfo.setX_SWIFI_REMARS3(rs.getString("X_SWIFI_REMARS3"));
                    wifiInfo.setLAT(rs.getString("LAT"));
                    wifiInfo.setLNT(rs.getString("LNT"));
                    wifiInfo.setWORK_DTTM(rs.getString("WORK_DTTM"));

                    System.out.println("no:"+wifiInfo.getX_SWIFI_MGR_NO());
                    wifiInfos.add(wifiInfo);
                }
                System.out.println("Fetched " + wifiInfos.size() + " rows.");


                // 리스트를 JSON 형식으로 변환하여 클라이언트로 응답
                Gson gson = new Gson();
                String json = gson.toJson(wifiInfos);
                out.write(json);
                out.flush();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void insertHistory(String lat, String lon){
        System.out.println("insertHistory");

        try (Connection conn = DriverManager.getConnection(DBinfo)) {
            if (conn != null) {
                try (Statement statement = conn.createStatement()) {

                    String createTable = "CREATE TABLE IF NOT EXISTS myHistory (\n"
                            + " ID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " LAT TEXT,\n"
                            + " LON TEXT,\n"
                            + " DATE TEXT\n"
                            + ");";

                    statement.execute(createTable);
                    System.out.println("Table 'myHistory' is created.");


                    String insertSQL = "INSERT INTO myHistory (LAT, LON, DATE) VALUES (?, ?, ?);";
                    try ( PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        String formattedDate = now.format(formatter);

                        pstmt.setString(1, lat);
                        pstmt.setString(2, lon);
                        pstmt.setString(3, formattedDate);
                        pstmt.executeUpdate();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    String countSQL = "SELECT COUNT(*) FROM myHistory";
                    int rowCount = 0;
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(countSQL)) {

                        if (rs.next()) {
                            rowCount = rs.getInt(1);  // 첫 번째 열의 값을 가져옴
                        }
                        System.out.println("History cnt::"+rowCount);

                    } catch (SQLException e) {
                        System.out.println("Error counting rows: " + e.getMessage());
                    }

                } catch (SQLException e) {
                    System.out.println("Error creating table: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

    }
}
