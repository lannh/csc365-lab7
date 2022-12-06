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
                case 6:
                    ir.R6();
                    break;
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

    private void R6() throws SQLException {

        System.out.println("Revenue Overview\r\n");

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            // Step 2: Construct SQL statement
            String sql =
                "SELECT RoomCode, RoomName, "+
                        "round(COALESCE(R1.Revenue, 0),0) AS January, "+
                        "round(COALESCE(R2.Revenue, 0),0) AS February, "+
                        "round(COALESCE(R3.Revenue, 0),0) AS March, "+
                        "round(COALESCE(R4.Revenue, 0),0) AS April, "+
                        "round(COALESCE(R5.Revenue, 0),0) AS May, "+
                        "round(COALESCE(R6.Revenue, 0),0) AS June, "+
                        "round(COALESCE(R7.Revenue, 0),0) AS July, "+
                        "round(COALESCE(R8.Revenue, 0),0) AS August, "+
                        "round(COALESCE(R9.Revenue, 0),0) AS September, "+
                        "round(COALESCE(R10.Revenue, 0),0) AS October, "+
                        "round(COALESCE(R11.Revenue, 0),0) AS November, "+
                        "round(COALESCE(R12.Revenue, 0),0) AS December, "+
                        "round(COALESCE(R1.Revenue, 0) + COALESCE(R2.Revenue, 0) + COALESCE(R3.Revenue, 0) +  "+
                                "COALESCE(R4.Revenue, 0) + COALESCE(R5.Revenue, 0) + COALESCE(R6.Revenue, 0) +  "+
                                "COALESCE(R7.Revenue, 0) + COALESCE(R8.Revenue, 0) + COALESCE(R9.Revenue, 0) +  "+
                                "COALESCE(R10.Revenue, 0) + COALESCE(R11.Revenue, 0) + COALESCE(R12.Revenue, 0),0) AS TotalRevenue "+
                "FROM lnguy228.lab7_rooms AS R "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue "+
                                "FROM lnguy228.lab7_reservations "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=1 "+
                                "GROUP BY Month, room) AS R1 ON R1.room = R.roomcode "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue "+
                                "FROM lnguy228.lab7_reservations "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=2 "+
                                "GROUP BY Month, room) AS R2 ON R2.room = R.roomcode "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue "+
                                "FROM lnguy228.lab7_reservations "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=3 "+
                                "GROUP BY Month, room) AS R3 ON R3.room = R.roomcode "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue "+
                                "FROM lnguy228.lab7_reservations "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=4 "+
                                "GROUP BY Month, room) AS R4 ON R4.room = R.roomcode "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue "+
                                "FROM lnguy228.lab7_reservations "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=5 "+
                                "GROUP BY Month, room) AS R5 ON R5.room = R.roomcode "+
                "LEFT OUTER JOIN "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=6  "+
                                "GROUP BY Month, room) AS R6 ON R6.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=7 " +
                                "GROUP BY Month, room) AS R7 ON R7.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=8  "+
                                "GROUP BY Month, room) AS R8 ON R8.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=9  "+
                                "GROUP BY Month, room) AS R9 ON R9.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=10  "+
                                "GROUP BY Month, room) AS R10 ON R10.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=11  "+
                                "GROUP BY Month, room) AS R11 ON R11.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=12  "+
                                "GROUP BY Month, room) AS R12 ON R12.room = R.roomcode  "+
                "UNION  "+
                "SELECT  \" \" AS RoomCode, \"Totals\" AS RoomName, "+
                        "round(COALESCE(sum(R1.Revenue),0),0) AS January, "+
                        "round(COALESCE(sum(R2.Revenue),0),0) AS February, "+
                        "round(COALESCE(sum(R3.Revenue),0),0) AS March, "+
                        "round(COALESCE(sum(R4.Revenue),0),0) AS April, "+
                        "round(COALESCE(sum(R5.Revenue),0),0) AS May, "+
                        "round(COALESCE(sum(R6.Revenue),0),0) AS June, "+
                        "round(COALESCE(sum(R7.Revenue),0),0) AS July, "+
                        "round(COALESCE(sum(R8.Revenue),0),0) AS August, "+
                        "round(COALESCE(sum(R9.Revenue),0),0) AS September, "+
                        "round(COALESCE(sum(R10.Revenue),0),0) AS October, "+
                        "round(COALESCE(sum(R11.Revenue),0),0) AS November, "+
                        "round(COALESCE(sum(R12.Revenue),0),0) AS December, "+
                        "round(COALESCE(sum(R1.Revenue),0) + COALESCE(sum(R2.Revenue),0) + COALESCE(sum(R3.Revenue),0) + "+
                                "COALESCE(sum(R4.Revenue),0) + COALESCE(sum(R5.Revenue),0) + COALESCE(sum(R6.Revenue),0) + "+
                                "COALESCE(sum(R7.Revenue),0) + COALESCE(sum(R8.Revenue),0) + COALESCE(sum(R9.Revenue),0) + "+
                                "COALESCE(sum(R10.Revenue),0) + COALESCE(sum(R11.Revenue),0) + COALESCE(sum(R12.Revenue),0),0) AS TotalRevenue "+
                "FROM lnguy228.lab7_rooms AS R "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=1  "+
                                "GROUP BY Month, room) AS R1 ON R1.room = R.roomcode " +
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=2  "+
                                "GROUP BY Month, room) AS R2 ON R2.room = R.roomcode "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=3  "+
                                "GROUP BY Month, room) AS R3 ON R3.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=4  "+
                                "GROUP BY Month, room) AS R4 ON R4.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=5  "+
                                "GROUP BY Month, room) AS R5 ON R5.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=6  "+
                                "GROUP BY Month, room) AS R6 ON R6.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=7  "+
                                "GROUP BY Month, room) AS R7 ON R7.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=8  "+
                                "GROUP BY Month, room) AS R8 ON R8.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=9  "+
                                "GROUP BY Month, room) AS R9 ON R9.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=10  "+
                                "GROUP BY Month, room) AS R10 ON R10.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=11  "+
                                "GROUP BY Month, room) AS R11 ON R11.room = R.roomcode  "+
                "LEFT OUTER JOIN  "+
                        "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  "+
                                "FROM lnguy228.lab7_reservations  "+
                                "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=12  "+
                                "GROUP BY Month, room) AS R12 ON R12.room = R.roomcode ";

            // Step 3: (omitted in this example) Start transaction

            // Step 4: Send SQL statement to DBMS
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.printf(" %-10s %-30s %-12s %-12s %-9s %-9s %-9s %-9s %-9s %-9s %-12s %-12s %-12s %-12s %-12s\n",
                        "RoomCode","RoomName","January","February","March","April","May","June","July","August","September","October","November","December","TotalRevenue");
                // Step 5: Receive results
                while (rs.next()) {
                    String RoomCode = rs.getString("RoomCode");
                    String RoomName = rs.getString("RoomName");

                    int revenue1 = rs.getInt("January");
                    int revenue2 = rs.getInt("February");
                    int revenue3 = rs.getInt("March");
                    int revenue4 = rs.getInt("April");
                    int revenue5 = rs.getInt("May");
                    int revenue6 = rs.getInt("June");
                    int revenue7 = rs.getInt("July");
                    int revenue8 = rs.getInt("August");
                    int revenue9 = rs.getInt("September");
                    int revenue10 = rs.getInt("October");
                    int revenue11 = rs.getInt("November");
                    int revenue12 = rs.getInt("December");
                    int totalRevenue = rs.getInt("TotalRevenue");

                    System.out.printf(" %-10s %-30s %-12d %-12d %-9d %-9d %-9d %-9d %-9d %-9d %-12d %-12d %-12d %-12d %-12d\n",
                            RoomCode,RoomName,
                            revenue1,revenue2,revenue3,
                            revenue4,revenue5,revenue6,
                            revenue7,revenue8,revenue9,
                            revenue10,revenue11,revenue12,totalRevenue);
                }
            }
        }
    }
}
