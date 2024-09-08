package org.example.mission_01;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    // WIFIs.db의 경로는 C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin 톰캣안 bin 파일에 있음.
    String projectPath = System.getProperty("user.dir");
    String DBinfo = "jdbc:sqlite:"+projectPath+"/WIFIs.db";
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            System.out.println(projectPath);
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC 드라이버를 로드할 수 없습니다: " + e.getMessage());
        }

        String query = "SELECT * FROM myHistory ORDER BY ID DESC";


        try (Connection conn = DriverManager.getConnection(DBinfo);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                List<MyHistoryData> myHistorys = new ArrayList<>();

                while (rs.next()) {
                    MyHistoryData myHistory = new MyHistoryData();
                    myHistory.setID(rs.getInt("ID"));
                    myHistory.setLAT(rs.getString("LAT"));
                    myHistory.setLON(rs.getString("LON"));
                    myHistory.setDATE(rs.getString("DATE"));
                    myHistorys.add(myHistory);
                }

                // 리스트를 JSON 형식으로 변환하여 클라이언트로 응답
                String json = gson.toJson(myHistorys);
                req.setAttribute("myHistorys", json);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }


        req.getRequestDispatcher("/history.jsp").forward(req, resp);
    }
}
