import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class InnReservations {

    public static void main(String[] args) {
        try {
            InnReservations ir = new InnReservations();
            int demoNum = Integer.parseInt(args[0]);

            switch (demoNum) {
                case 1:
                    ir.R1();
                    break;
//                case 2: hp.demo2(); break;
//                case 3: hp.demo3(); break;
                case 4:
                    ir.R4();
                    break;
//                case 5: hp.demo5(); break;
            }

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        } catch (Exception e2) {
            System.err.println("Exception: " + e2.getMessage());
        }
    }

    private void R1() throws SQLException {

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            String sql = "SELECT * FROM yehlsing.lab7_rooms JOIN yehlaing.lab7_reservations;";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println(rs);
            }


        }
    }

    // Demo4 - Establish JDBC connection, execute DML query (UPDATE) using PreparedStatement / transaction
    private void R4() throws SQLException {

        try (Connection conn = DriverManager.getConnection(
                System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a reservation code: ");
            String code = scanner.nextLine();

            String updateSql = "DELETE FROM lnguy228.lab7_reservations WHERE CODE = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, Integer.parseInt(code));
                int rowCount = pstmt.executeUpdate();

                if(rowCount > 0)
                    System.out.printf("Reservation #%s has been canceled successfully.", code);
                else
                    System.out.printf("Reservation #%s can't be found. Please try again.",code);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            catch (NumberFormatException e) {
                System.out.printf("Invalid Reservation Code: %s. Please try again.", code);
                conn.rollback();
            }

        }
    }


    private void R5() throws SQLException {

        System.out.println("Detailed Reservation Information");
        try (Connection conn = DriverManager.getConnection(
                System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter a first name (or press Enter to leave the field blank): ");
            String firstname = scanner.nextLine();

            System.out.print("Enter a last name (or press Enter to leave the field blank): ");
            String lastname = scanner.nextLine();

            System.out.print("Enter a checkin date (or press Enter to leave the field blank): ");
            String checkin = scanner.nextLine();

            System.out.print("Enter a checkout date (or press Enter to leave the field blank): ");
            String checkout = scanner.nextLine();

            System.out.print("Enter a room code (or press Enter to leave the field blank): ");
            String room = scanner.nextLine();

            System.out.print("Enter a reservation code (or press Enter to leave the field blank): ");
            String code = scanner.nextLine();

            String updateSql = "DELETE FROM lnguy228.lab7_reservations WHERE CODE = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, Integer.parseInt(code));
                int rowCount = pstmt.executeUpdate();

                if(rowCount > 0)
                    System.out.printf("Reservation #%s has been canceled successfully.", code);
                else
                    System.out.printf("Reservation #%s can't be found. Please try again.",code);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            catch (NumberFormatException e) {
                System.out.printf("Invalid Reservation Code: %s. Please try again.", code);
                conn.rollback();
            }

        }
    }
}
