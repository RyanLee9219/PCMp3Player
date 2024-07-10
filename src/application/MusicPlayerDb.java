package application;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MusicPlayerDb {
    private PreparedStatement pst;
    private ResultSet rs;
    private Connection conn;

    public MusicPlayerDb(Connection conn) {
        this.conn = conn;
    }

    public ObservableList<MusicData> loadData() {
        ObservableList<MusicData> tmpOv = FXCollections.observableArrayList();
        String query = "SELECT * FROM Music";
        try {
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            while (rs.next()) {
                String path = rs.getString("Path");
                File file = new File(path);
                MusicData data = new MusicData(file.getName(), path);
                tmpOv.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
              
            }
        }
        return tmpOv;
    }

    public void insertData(String path) {
        String query = "INSERT or REPLACE INTO Music (Path) VALUES (?)";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, path);
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
               
            }
        }
    }

    public void deleteData(String data) {
        String query = "DELETE FROM Music WHERE Path = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, data);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
             
            }
        }
    }
}
