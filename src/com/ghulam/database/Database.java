package com.ghulam.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.scene.control.Alert;
import javafx.stage.Modality;

public class Database {
    private static Connection conn;
    private PreparedStatement pst = null;
    private ResultSet rs = null;
    private static Alert alert = null;
    
    // Consulta compatível com o catálogo do MySQL
    private final static String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND UPPER(TABLE_NAME)=?";

    public Database() {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro no Banco de Dados");
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
    }

    public final static Connection getConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Conecta ao schema 'library' usando o usuário root
            String url = "jdbc:mysql://localhost:3306/library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            conn = DriverManager.getConnection(url, "root", "root");
            return conn;
        } catch (Exception e) {
            if (alert == null) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
            }
            alert.setContentText("Falha ao conectar no MySQL: " + e.getMessage());
            alert.showAndWait();
        }
        return null;
    }

    public void initDatabase() {
        conn = getConnection();
        if (conn == null) {
            return;
        }
        checkUserTable();
        checkStudentTable();
        checkBookTable();
        checkBookIssueTable();
    }

    private void checkUserTable() {
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "USERDB");
            rs = pst.executeQuery();
            if (rs.next()) {
                return;
            } else {
                Statement st = conn.createStatement();
                st.execute("CREATE TABLE USERDB(USERNAME VARCHAR(20) PRIMARY KEY, PASSWORD VARCHAR(26))");
                st.execute("INSERT INTO USERDB VALUES('GHULAM', 'Ghulam')");
            }
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }
    }

    private void checkStudentTable() {
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "STUDENTDB");
            rs = pst.executeQuery();
            if (rs.next()) {
                return;
            } else {
                Statement st = conn.createStatement();
                st.execute("CREATE TABLE STUDENTDB ("
                        + "ID INT PRIMARY KEY, "
                        + "NAME VARCHAR(50), "
                        + "CLS VARCHAR(20), "
                        + "ROLL INT, "
                        + "MOBILE_NO VARCHAR(20), "
                        + "ADDRESS VARCHAR(100), "
                        + "ISSUEDBOOKS INT DEFAULT 0, "
                        + "DATE1 DATE)");
            }
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }
    }

    private void checkBookTable() {
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "BOOKDB");
            rs = pst.executeQuery();
            if (rs.next()) {
                return;
            } else {
                Statement st = conn.createStatement();
                st.execute("CREATE TABLE BOOKDB("
                        + "ID INT PRIMARY KEY, "
                        + "NAME VARCHAR(100), "
                        + "AUTHOR VARCHAR(60), "
                        + "DESCRIPTION VARCHAR(150), "
                        + "STOCK INT DEFAULT 0, "
                        + "DATE1 DATE)");
            }
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }
    }

    private void checkBookIssueTable() {
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "ISSUEBOOKDB");
            rs = pst.executeQuery();
            if (!rs.next()) {
                Statement st = conn.createStatement();
                st.execute("CREATE TABLE ISSUEBOOKDB("
                        + "ISSUE_ID INT PRIMARY KEY, "
                        + "BOOK_ID INT, "
                        + "STUDENT_ID INT, "
                        + "ISSUE_DATE DATE, "
                        + "RETURN_DATE DATE)");
            }
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "ISSUEBOOKDB_HISTORY");
            rs = pst.executeQuery();
            if (!rs.next()) {
                Statement st = conn.createStatement();
                st.execute("CREATE TABLE ISSUEBOOKDB_HISTORY("
                        + "ISSUE_ID INT, "
                        + "BOOK_ID INT, "
                        + "STUDENT_ID INT, "
                        + "ISSUE_DATE DATE, "
                        + "RETURN_DATE DATE)");
            }
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }

        try {
            Statement st = conn.createStatement();
            
            // Triggers adaptadas para o padrão MySQL
            st.execute("DROP TRIGGER IF EXISTS ISSUEBOOKDB_TRIGGER");
            st.execute("CREATE TRIGGER ISSUEBOOKDB_TRIGGER AFTER DELETE ON ISSUEBOOKDB FOR EACH ROW "
                    + "BEGIN "
                    + "  UPDATE STUDENTDB SET ISSUEDBOOKS = ISSUEDBOOKS - 1 WHERE ID = OLD.STUDENT_ID; "
                    + "  UPDATE BOOKDB SET STOCK = STOCK + 1 WHERE ID = OLD.BOOK_ID; "
                    + "  INSERT INTO ISSUEBOOKDB_HISTORY(ISSUE_ID, BOOK_ID, STUDENT_ID, ISSUE_DATE, RETURN_DATE) "
                    + "  VALUES (OLD.ISSUE_ID, OLD.BOOK_ID, OLD.STUDENT_ID, OLD.ISSUE_DATE, CURDATE()); "
                    + "END;");

            st.execute("DROP TRIGGER IF EXISTS TRIGGER_ISSUEDBOOKS_UPDATION");
            st.execute("CREATE TRIGGER TRIGGER_ISSUEDBOOKS_UPDATION BEFORE INSERT ON ISSUEBOOKDB FOR EACH ROW "
                    + "BEGIN "
                    + "  UPDATE STUDENTDB SET ISSUEDBOOKS = ISSUEDBOOKS + 1 WHERE ID = NEW.STUDENT_ID; "
                    + "  UPDATE BOOKDB SET STOCK = STOCK - 1 WHERE ID = NEW.BOOK_ID; "
                    + "END;");
        } catch (SQLException e) {
            alert.setContentText(e.toString());
            alert.show();
        }
    }
}