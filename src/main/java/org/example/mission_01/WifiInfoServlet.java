package org.example.mission_01;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.sql.*;

@WebServlet("/wifiInfo")
public class WifiInfoServlet extends HttpServlet {

    // WIFIs.db의 경로는 C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin 톰캣안 bin 파일에 있음.
    String projectPath = System.getProperty("user.dir");
    String DBinfo = "jdbc:sqlite:"+projectPath+"/WIFIs.db";

    Gson gson = new Gson();
    String key = "51636250646a797537396a66787462"; // 공공 api 키값
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        createTable();

        boolean check = getWifiInfo();

        int SaveCount = getRowCount();

        req.setAttribute("wifiInfoJson", "" + SaveCount);           // 포워드 시 데이터 전달
        req.getRequestDispatcher("/wifiInfo.jsp").forward(req, resp); //wifiInfo.jsp로 포워드

    }


    /**
     * Table생성
     */
    private void createTable() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC 드라이버를 로드할 수 없습니다: " + e.getMessage());
        }

        try (Connection conn = DriverManager.getConnection(DBinfo)) {
            if (conn != null) {
                try (Statement statement = conn.createStatement()) {
                    String dropTableSQL = "DROP TABLE IF EXISTS wifi_info;";
                    statement.execute(dropTableSQL);

                    String createTable = "CREATE TABLE IF NOT EXISTS wifi_info (\n"
                            + " X_SWIFI_MGR_NO TEXT PRIMARY KEY,\n"
                            + " X_SWIFI_WRDOFC TEXT,\n"
                            + " X_SWIFI_MAIN_NM TEXT,\n"
                            + " X_SWIFI_ADRES1 TEXT,\n"
                            + " X_SWIFI_ADRES2 TEXT,\n"
                            + " X_SWIFI_INSTL_FLOOR TEXT,\n"
                            + " X_SWIFI_INSTL_TY TEXT,\n"
                            + " X_SWIFI_INSTL_MBY TEXT,\n"
                            + " X_SWIFI_SVC_SE TEXT,\n"
                            + " X_SWIFI_CMCWR TEXT,\n"
                            + " X_SWIFI_CNSTC_YEAR TEXT,\n"
                            + " X_SWIFI_INOUT_DOOR TEXT,\n"
                            + " X_SWIFI_REMARS3 TEXT,\n"
                            + " LAT TEXT,\n"
                            + " LNT TEXT,\n"
                            + " WORK_DTTM TEXT\n"
                            + ");";

                    statement.execute(createTable);
                    System.out.println("Table 'wifi_info' is created.");

                } catch (SQLException e) {
                    System.out.println("Error creating table: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

    }


    /**
     * wifi_info 테이블에 저장된 데이터 개수 가져오기.
     *
     * @return
     */
    private int getRowCount() {
        String countSQL = "SELECT COUNT(*) FROM wifi_info";
        int rowCount = 0;

        try (Connection conn = DriverManager.getConnection(DBinfo);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            if (rs.next()) {
                rowCount = rs.getInt(1);  // 첫 번째 열의 값을 가져옴
            }

        } catch (SQLException e) {
            System.out.println("Error counting rows: " + e.getMessage());
        }

        return rowCount;
    }


    /**
     * 공공 api로 데이터 가져오기.
     *
     * @return
     * @throws IOException
     */
    private Boolean getWifiInfo() throws IOException {


        String insertSQL = "INSERT INTO wifi_info (X_SWIFI_MGR_NO, X_SWIFI_WRDOFC, X_SWIFI_MAIN_NM, X_SWIFI_ADRES1, X_SWIFI_ADRES2, "
                + "X_SWIFI_INSTL_FLOOR, X_SWIFI_INSTL_TY, X_SWIFI_INSTL_MBY, X_SWIFI_SVC_SE, X_SWIFI_CMCWR, "
                + "X_SWIFI_CNSTC_YEAR, X_SWIFI_INOUT_DOOR, X_SWIFI_REMARS3, LAT, LNT, WORK_DTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                + "ON CONFLICT (X_SWIFI_MGR_NO) DO NOTHING;";


        try (Connection conn = DriverManager.getConnection(DBinfo);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            int start = 1;
            int range = 1000;
            int total = 0;

            String url = "http://openapi.seoul.go.kr:8088/" + key + "/json/TbPublicWifiInfo/" + start + "/" + range + "/";

            Request.Builder builder = new Request.Builder().url(url).get();
            Request request = builder.build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {

                try (ResponseBody body = response.body()) {

                    if (body != null) {
                        TbPublicWifiInfoWrapper wrapper = gson.fromJson(body.string(), TbPublicWifiInfoWrapper.class);

                        for (ROW row : wrapper.getTbPublicWifiInfo().getRow()) {
                            insertInfo(pstmt, row);
                        }
                        pstmt.executeBatch();
                        pstmt.clearBatch();


                        total = wrapper.getTbPublicWifiInfo().getList_total_count();
                        System.out.println("total=" + total);

                    } else {
                        System.out.println("body is null");
                    }
                }
            } else {
                System.err.println("Error Occurred");
            }

            int i = 1;
            int end = 0;
            while (end < total) {
                start = (i * range) + 1;
                end = Math.min((i * range) + range, total);

                System.out.println("i=" + i + ", start=" + start + ", end=" + end);
                url = "http://openapi.seoul.go.kr:8088/" + key + "/json/TbPublicWifiInfo/" + start + "/" + end + "/";

                builder = new Request.Builder().url(url).get();
                request = builder.build();

                response = client.newCall(request).execute();
                if (response.isSuccessful()) {

                    ResponseBody body = response.body();

                    if (body != null) {
                        TbPublicWifiInfoWrapper wrapper = gson.fromJson(body.string(), TbPublicWifiInfoWrapper.class);

                        for (ROW row : wrapper.getTbPublicWifiInfo().getRow()) {
                            insertInfo(pstmt, row);
                        }
                        pstmt.executeBatch();
                        pstmt.clearBatch();


                    } else {
                        System.out.println("body is null");
                    }


                } else {
                    System.err.println("Error Occurred");
                    return false;
                }

                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;

    }


    /**
     * 공공 api로 가져온 데이터 DB에 저장.
     *
     * @param pstmt
     * @param row
     */
    private void insertInfo(PreparedStatement pstmt, ROW row) {

        try {
            pstmt.setString(1, row.getX_SWIFI_MGR_NO());
            pstmt.setString(2, row.getX_SWIFI_WRDOFC());
            pstmt.setString(3, row.getX_SWIFI_MAIN_NM());
            pstmt.setString(4, row.getX_SWIFI_ADRES1());
            pstmt.setString(5, row.getX_SWIFI_ADRES2());
            pstmt.setString(6, row.getX_SWIFI_INSTL_FLOOR());
            pstmt.setString(7, row.getX_SWIFI_INSTL_TY());
            pstmt.setString(8, row.getX_SWIFI_INSTL_MBY());
            pstmt.setString(9, row.getX_SWIFI_SVC_SE());
            pstmt.setString(10, row.getX_SWIFI_CMCWR());
            pstmt.setString(11, row.getX_SWIFI_CNSTC_YEAR());
            pstmt.setString(12, row.getX_SWIFI_INOUT_DOOR());
            pstmt.setString(13, row.getX_SWIFI_REMARS3());
            pstmt.setString(14, row.getLAT());
            pstmt.setString(15, row.getLNT());
            pstmt.setString(16, row.getWORK_DTTM());

            pstmt.addBatch();
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        }

    }

}
