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
//                case 1: ir.R1(); break;
//                case 2: ir.R2(); break;
                case 3:
                    ir.R3();
                    break;
//                case 4: ir.R4(); break;
                case 5:
                    ir.R5();
                    break;
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

    private void R3() throws SQLException {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            Scanner scanner = new Scanner(System.in);

            while (true) {

                System.out.println("Modify an existing reservation");

                System.out.print("Enter the code for the reservation that you want to make changes to: ");
                String code = scanner.nextLine();

                String checkCodeSql = "SELECT * FROM yehlaing.test_reservations WHERE CODE = " + code;

                String room = "";
                String oldCheckIn = "";
                String oldCheckOut = "";
                conn.setAutoCommit(false);

                // Checks if reservation exists
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkCodeSql)) {

                    if (rs.next()) {
                        room = rs.getString("Room");
                        oldCheckIn = rs.getString("CheckIn");
                        oldCheckOut = rs.getString("CheckOut");

                        System.out.println("-----Reservation found-----");
                        System.out.format("|%-10s|%-10s|%-15s|%-15s|%-10s|%-10s|%-10s|%-10s|%-10s|\n", "Code", "Room", "CheckIn", "CheckOut", "Rate", "LastName", "FirstName", "Adults", "Kids");
                        System.out.format("|%-10s|%-10s|%-15s|%-15s|%-10s|%-10s|%-10s|%-10s|%-10s|\n", rs.getString("Code"), rs.getString("Room"), rs.getString("CheckIn"), rs.getString("CheckOut"), rs.getString("Rate"), rs.getString("LastName"), rs.getString("FirstName"), rs.getString("Adults"), rs.getString("Kids"));

                    } else {
                        System.out.println("Reservation does not exist");
                        System.out.print("Would you like to try again? (y/n)");
                        if (scanner.nextLine().equals("n")) {
                            break;
                        }
                        else {
                            continue;
                        }
                    }

                }

                System.out.println("Enter the new values for any of the fields below or press enter to leave field blank");
                // customer name
                System.out.print("Enter Firstname: ");
                String firstName = scanner.nextLine();
                System.out.print("Enter Lastname: ");
                String lastName = scanner.nextLine();
                // customer check in checkout date
                System.out.print("Enter the begin date (YYYY-MM-DD): ");
                String checkIn = scanner.nextLine();
                System.out.print("Enter the end date (YYYY-MM-DD): ");
                String checkOut = scanner.nextLine();
                System.out.print("Enter the number of children: ");
                String children = scanner.nextLine();
                System.out.print("Enter the number of adults: ");
                String adult = scanner.nextLine();


                // Checks if new date overlaps with existing reservations
                if (!checkIn.equals("") || !checkOut.equals("")) {
                    if (!checkIn.equals("")) {
                        oldCheckIn = checkIn;

                    }
                    if (!checkOut.equals("")) {
                        oldCheckOut = checkOut;
                    }
                    System.out.println(oldCheckIn);
                    System.out.println(oldCheckOut);

                    String checkDateSql = "SELECT * FROM yehlaing.test_reservations WHERE CODE <> ? AND Room = ? AND CheckIn <= ? AND CheckOut > ?";
                    System.out.println(checkDateSql);

                    try (PreparedStatement pstmt = conn.prepareStatement(checkDateSql)) {
                        pstmt.setString(1, code);
                        pstmt.setString(2, room);
                        pstmt.setString(3, oldCheckOut);
                        pstmt.setString(4, oldCheckIn);


                        try (ResultSet rs = pstmt.executeQuery()) {

                            if (rs.next()) {
                                System.out.println("Date overlapping with existing reservation");
                                if (scanner.nextLine().equals("n")) {
                                    break;
                                }
                                else {
                                    continue;
                                }
                            } else if (!rs.next()) {
                                System.out.println("No overlapping dates found");
                            }

                        }
                    } catch (SQLException e) {
                        System.out.println(e);
                        conn.rollback();
                    }
                }
                String updateSql = "UPDATE yehlaing.test_reservations SET";
                List<String> params = new ArrayList<String>();
                List<String> values = new ArrayList<String>();

                if (!firstName.equals("")) {
                    params.add(" FirstName = ?");
                    values.add(firstName.toUpperCase() );
                }
                if (!lastName.equals("")) {
                    params.add(" LastName = ?");
                    values.add(lastName.toUpperCase() );
                }
                if (!checkIn.equals("")) {
                    params.add(" CheckIn = ?");
                    values.add(checkIn);
                }
                if (!checkOut.equals("")) {
                    params.add(" CheckOut = ?");
                    values.add(checkOut);
                }
                if (!children.equals("")) {
                    params.add(" Kids = ?");
                    values.add(children);
                }
                if (!adult.equals("")) {
                    params.add(" Adults = ?");
                    values.add(adult);
                }

                if (params.isEmpty()) {
                    System.out.println("No fields entered");
                    System.out.print("Would you like to try again? (y/n)");
                    if (scanner.nextLine().equals("n")) {
                        break;
                    }
                    else {
                        continue;
                    }
                }

                for (int i = 0; i < params.size(); i++) {
                    updateSql += params.get(i);
                    if (i != params.size() - 1) {
                        updateSql += " ,";
                    }
                }
                updateSql += " WHERE Code = ?";

                System.out.println(updateSql);

                // Updates reservation
                try (PreparedStatement pstmt2 = conn.prepareStatement(updateSql)) {
                    if (!values.isEmpty()) {
                        int i = 0;
                        for (; i < values.size(); i++) {
                            pstmt2.setString(i + 1, (values.get(i)));
                        }
                        pstmt2.setString(i + 1, code);
                    }
                    int rowCount = pstmt2.executeUpdate();
                    System.out.println("-----Updated " + rowCount + " record(s)-----");

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.out.println(e);
                }
                System.out.println("");
                System.out.print("Would you like to try again? (y/n)");
                if (scanner.nextLine().equals("n")) {
                    break;
                }
            }

        }

    }

    private void R5() throws SQLException {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            Scanner scanner = new Scanner(System.in);

            List<String> params = new ArrayList<String>();
            List<String> values = new ArrayList<String>();

            while (true) {
                System.out.println("Search for matching reservations");
                System.out.println("Enter any combination of fields listed below or press enter to leave field blank");
                System.out.println();
                // customer name
                System.out.print("Enter Firstname: ");
                String firstName = scanner.nextLine();
                System.out.print("Enter Lastname: ");
                String lastName = scanner.nextLine();
                System.out.print("Enter a date range (YYYY-MM-DD,YYYY-MM-DD): ");
                String dates = scanner.nextLine();
                System.out.print("Enter Room Code: ");
                String roomCode = scanner.nextLine();
                System.out.print("Enter Reservation Code: ");
                String reservationCode = scanner.nextLine();

                String detailedSql = "SELECT * FROM yehlaing.lab7_reservations JOIN yehlaing.lab7_rooms ON Room = RoomCode";

                // Step 3: Start transaction
                conn.setAutoCommit(false);
                if (!firstName.equals("") || "any".equalsIgnoreCase(firstName)) {
                    params.add(" FirstName LIKE ?");
                    values.add(firstName + "%");
                }
                if (!lastName.equals("") || "any".equalsIgnoreCase(lastName)) {
                    params.add(" LastName LIKE ?");
                    values.add(lastName + "%");
                }
                if (!dates.equals("") || "any".equalsIgnoreCase(lastName)) {
                    String[] date_array = dates.split(",");
                    params.add(" CheckOut >= ? and CheckIn <= ? ");
                    values.add(date_array[0]);
                    values.add(date_array[1]);
                }
                if (!roomCode.equals("") || "any".equalsIgnoreCase(roomCode)) {
                    params.add(" Room LIKE ?");
                    values.add(roomCode + "%");
                }
                if (!reservationCode.equals("") || "any".equalsIgnoreCase(lastName)) {
                    params.add(" CODE LIKE ?");
                    values.add(reservationCode + "%");
                }
                if (!params.isEmpty()) {
                    detailedSql += " WHERE";
                    for (int i = 0; i < params.size(); i++) {
                        detailedSql += params.get(i);
                        if (i != params.size() - 1) {
                            detailedSql += " AND";
                        }
                    }
                }
                System.out.println(detailedSql);


                try (PreparedStatement pstmt = conn.prepareStatement(detailedSql)) {
                    if (!values.isEmpty()) {
                        for (int i = 0; i < values.size(); i++) {
                            pstmt.setString(i + 1, (values.get(i)));
                        }
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        System.out.println("Matching reservations:");
                        System.out.format("|%-10s|%-10s|%-25s|%-15s|%-15s|%-10s|%-10s|%-10s|%-10s|%-10s|\n", "Code", "Room", "RoomName", "CheckIn", "CheckOut", "Rate", "LastName", "FirstName", "Adults", "Kids");
                        int matchCount = 0;
                        while (rs.next()) {
                            System.out.format("|%-10s|%-10s|%-25s|%-15s|%-15s|%-10s|%-10s|%-10s|%-10s|%-10s|\n", rs.getString("Code"), rs.getString("Room"), rs.getString("RoomName"), rs.getString("CheckIn"), rs.getString("CheckOut"), rs.getString("Rate"), rs.getString("LastName"), rs.getString("FirstName"), rs.getString("Adults"), rs.getString("Kids"));
                            matchCount++;
                        }
                        System.out.format("----------------------%nFound %d match%s %n", matchCount, matchCount == 1 ? "" : "es");
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                }
                System.out.println("");
                System.out.print("Would you like to try again? (y/n)");
                if (scanner.nextLine().equals("n")) {
                    break;
                }
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
                    "SELECT RoomCode, RoomName, " +
                            "round(COALESCE(R1.Revenue, 0),0) AS January, " +
                            "round(COALESCE(R2.Revenue, 0),0) AS February, " +
                            "round(COALESCE(R3.Revenue, 0),0) AS March, " +
                            "round(COALESCE(R4.Revenue, 0),0) AS April, " +
                            "round(COALESCE(R5.Revenue, 0),0) AS May, " +
                            "round(COALESCE(R6.Revenue, 0),0) AS June, " +
                            "round(COALESCE(R7.Revenue, 0),0) AS July, " +
                            "round(COALESCE(R8.Revenue, 0),0) AS August, " +
                            "round(COALESCE(R9.Revenue, 0),0) AS September, " +
                            "round(COALESCE(R10.Revenue, 0),0) AS October, " +
                            "round(COALESCE(R11.Revenue, 0),0) AS November, " +
                            "round(COALESCE(R12.Revenue, 0),0) AS December, " +
                            "round(COALESCE(R1.Revenue, 0) + COALESCE(R2.Revenue, 0) + COALESCE(R3.Revenue, 0) +  " +
                            "COALESCE(R4.Revenue, 0) + COALESCE(R5.Revenue, 0) + COALESCE(R6.Revenue, 0) +  " +
                            "COALESCE(R7.Revenue, 0) + COALESCE(R8.Revenue, 0) + COALESCE(R9.Revenue, 0) +  " +
                            "COALESCE(R10.Revenue, 0) + COALESCE(R11.Revenue, 0) + COALESCE(R12.Revenue, 0),0) AS TotalRevenue " +
                            "FROM yehlaing.lab7_rooms AS R " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue " +
                            "FROM yehlaing.lab7_reservations " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=1 " +
                            "GROUP BY Month, room) AS R1 ON R1.room = R.roomcode " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue " +
                            "FROM yehlaing.lab7_reservations " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=2 " +
                            "GROUP BY Month, room) AS R2 ON R2.room = R.roomcode " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue " +
                            "FROM yehlaing.lab7_reservations " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=3 " +
                            "GROUP BY Month, room) AS R3 ON R3.room = R.roomcode " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue " +
                            "FROM yehlaing.lab7_reservations " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=4 " +
                            "GROUP BY Month, room) AS R4 ON R4.room = R.roomcode " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue " +
                            "FROM yehlaing.lab7_reservations " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=5 " +
                            "GROUP BY Month, room) AS R5 ON R5.room = R.roomcode " +
                            "LEFT OUTER JOIN " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=6  " +
                            "GROUP BY Month, room) AS R6 ON R6.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=7 " +
                            "GROUP BY Month, room) AS R7 ON R7.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=8  " +
                            "GROUP BY Month, room) AS R8 ON R8.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=9  " +
                            "GROUP BY Month, room) AS R9 ON R9.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=10  " +
                            "GROUP BY Month, room) AS R10 ON R10.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=11  " +
                            "GROUP BY Month, room) AS R11 ON R11.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=12  " +
                            "GROUP BY Month, room) AS R12 ON R12.room = R.roomcode  " +
                            "UNION  " +
                            "SELECT  \" \" AS RoomCode, \"Totals\" AS RoomName, " +
                            "round(COALESCE(sum(R1.Revenue),0),0) AS January, " +
                            "round(COALESCE(sum(R2.Revenue),0),0) AS February, " +
                            "round(COALESCE(sum(R3.Revenue),0),0) AS March, " +
                            "round(COALESCE(sum(R4.Revenue),0),0) AS April, " +
                            "round(COALESCE(sum(R5.Revenue),0),0) AS May, " +
                            "round(COALESCE(sum(R6.Revenue),0),0) AS June, " +
                            "round(COALESCE(sum(R7.Revenue),0),0) AS July, " +
                            "round(COALESCE(sum(R8.Revenue),0),0) AS August, " +
                            "round(COALESCE(sum(R9.Revenue),0),0) AS September, " +
                            "round(COALESCE(sum(R10.Revenue),0),0) AS October, " +
                            "round(COALESCE(sum(R11.Revenue),0),0) AS November, " +
                            "round(COALESCE(sum(R12.Revenue),0),0) AS December, " +
                            "round(COALESCE(sum(R1.Revenue),0) + COALESCE(sum(R2.Revenue),0) + COALESCE(sum(R3.Revenue),0) + " +
                            "COALESCE(sum(R4.Revenue),0) + COALESCE(sum(R5.Revenue),0) + COALESCE(sum(R6.Revenue),0) + " +
                            "COALESCE(sum(R7.Revenue),0) + COALESCE(sum(R8.Revenue),0) + COALESCE(sum(R9.Revenue),0) + " +
                            "COALESCE(sum(R10.Revenue),0) + COALESCE(sum(R11.Revenue),0) + COALESCE(sum(R12.Revenue),0),0) AS TotalRevenue " +
                            "FROM yehlaing.lab7_rooms AS R " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=1  " +
                            "GROUP BY Month, room) AS R1 ON R1.room = R.roomcode " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=2  " +
                            "GROUP BY Month, room) AS R2 ON R2.room = R.roomcode " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=3  " +
                            "GROUP BY Month, room) AS R3 ON R3.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=4  " +
                            "GROUP BY Month, room) AS R4 ON R4.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=5  " +
                            "GROUP BY Month, room) AS R5 ON R5.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=6  " +
                            "GROUP BY Month, room) AS R6 ON R6.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=7  " +
                            "GROUP BY Month, room) AS R7 ON R7.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=8  " +
                            "GROUP BY Month, room) AS R8 ON R8.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=9  " +
                            "GROUP BY Month, room) AS R9 ON R9.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=10  " +
                            "GROUP BY Month, room) AS R10 ON R10.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=11  " +
                            "GROUP BY Month, room) AS R11 ON R11.room = R.roomcode  " +
                            "LEFT OUTER JOIN  " +
                            "(SELECT MONTH(checkout) AS Month, room, sum(DATEDIFF(checkout,checkin)*rate) AS Revenue  " +
                            "FROM yehlaing.lab7_reservations  " +
                            "WHERE YEAR(CURRENT_DATE) = YEAR(checkout) AND MONTH(checkout)=12  " +
                            "GROUP BY Month, room) AS R12 ON R12.room = R.roomcode ";

            // Step 3: (omitted in this example) Start transaction

            // Step 4: Send SQL statement to DBMS
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.printf(" %-10s %-30s %-12s %-12s %-9s %-9s %-9s %-9s %-9s %-9s %-12s %-12s %-12s %-12s %-12s\n",
                        "RoomCode", "RoomName", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "TotalRevenue");
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
                            RoomCode, RoomName,
                            revenue1, revenue2, revenue3,
                            revenue4, revenue5, revenue6,
                            revenue7, revenue8, revenue9,
                            revenue10, revenue11, revenue12, totalRevenue);
                }
            } catch (SQLException e) {
                System.out.print(e);
            }
        }
    }


}

