package org.example.mission_01;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/historys")
public class historys extends HttpServlet {

    // WIFIs.db의 경로는 C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin 톰캣안 bin 파일에 있음.
    String projectPath = System.getProperty("user.dir");
    String DBinfo = "jdbc:sqlite:"+projectPath+"/WIFIs.db";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("here is historys");

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String id = req.getParameter("id");
        System.out.println("historys id::" + id);

        // JDBC 드라이버 로드
        loadJDBCDriver();

        // ID로 기록 삭제
        deleteHistoryById(id);

        // 남은 기록 개수 확인 및 출력
        int rowCount = getHistoryCount();
        System.out.println("History count::" + rowCount);
    }

    // SQLite JDBC 드라이버 로드
    private void loadJDBCDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC 드라이버를 로드할 수 없습니다: " + e.getMessage());
        }
    }

    // 주어진 ID의 기록을 삭제하는 메서드
    private void deleteHistoryById(String id) {
        String deleteSQL = "DELETE FROM myHistory WHERE ID = ?";
        try (Connection conn = DriverManager.getConnection(DBinfo);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error deleting history: " + e.getMessage());
        }
    }

    // 기록의 총 개수를 반환하는 메서드
    private int getHistoryCount() {
        String countSQL = "SELECT COUNT(*) FROM myHistory";
        try (Connection conn = DriverManager.getConnection(DBinfo);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            if (rs.next()) {
                return rs.getInt(1);  // 첫 번째 열의 값을 가져옴
            }
        } catch (SQLException e) {
            System.out.println("Error counting rows: " + e.getMessage());
        }
        return 0;
    }
}
