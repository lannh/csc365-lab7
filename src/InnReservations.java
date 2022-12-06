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
//                case 4: hp.demo4(); break;
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
            String sql = "SELECT * FROM yehlaing.lab7_rooms JOIN yehlaing.lab7_reservations;";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

            }


        }
    }
    private void R5() throws SQLException {

        System.out.println("FR5: Present a search prompt or form that allows the\n" +
                "user to enter any combination of the fields listed below\r\n");

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            Scanner scanner = new Scanner(System.in);

         //   String firstName, lastName, roomCode, reservationCode, checkIn, checkOut, year, month, day;
            String conditions = "";

            System.out.print("Enter a what you want to search for: ");

            // customer name
            System.out.println("Enter Firstname: ");
            String firstName = scanner.nextLine();
            System.out.println("Enter Lastname: ");
            String lastName = scanner.nextLine();

            // customer check in checkout date
            System.out.println("Enter CheckIn Date (YYYY-MM-DD): ");
            String checkIn = scanner.nextLine();
            System.out.println("Enter CheckOut Date (YYYY-MM-DD): ");
            String checkOut = scanner.nextLine();

            // customer room code
            System.out.println("Enter Room Code: ");
            String roomCode = scanner.nextLine();
            System.out.println("Enter Reservation Code: ");
            String reservationCode = scanner.nextLine();

//            System.out.format("Until what date will %s be available (YYYY-MM-DD)? ", flavor);
//            LocalDate availDt = LocalDate.parse(scanner.nextLine());
//
            String detailedSql = "SELECT * FROM lab7_reservations WHERE FirstName LIKE ? AND LastName LIKE ? AND " +
                    "CheckIn >= ? AND CheckOut <= ? AND Room LIKE ? AND Code LIKE ?";

            // Step 3: Start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(detailedSql)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, checkIn);
                pstmt.setString(4, checkOut);
                pstmt.setString(5, roomCode);
                pstmt.setString(6, reservationCode);

//                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
//                System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);

                // Step 6: Commit or rollback transaction
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }

        }
        // Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }

}

