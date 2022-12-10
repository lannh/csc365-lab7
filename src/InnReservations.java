import java.sql.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDate;
import java.util.Date;

public class InnReservations {
    private long diff;

    public static void main(String[] args) {
        try {
            InnReservations ir = new InnReservations();
            int demoNum = Integer.parseInt(args[0]);

            switch (demoNum) {

                case 2:
                    ir.R2();
                    break;
                case 3:
                    ir.R3();
                    break;
                case 4:
                    ir.R4();
                    break;
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
        } finally {
            System.out.println("Exiting program...");
        }
    }

    private void R1() throws SQLException {

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct SQL statement
            String sql = "select roomname, popScore,nextAvail , MostRecentLength\n" +
                    "from\n" +
                    "(select rooms1.roomcode, ROUND(COALESCE(sum(stayTime), 0)/180 , 2)as popScore\n" +
                    "    from\n" +
                    "        yehlaing.lab7_rooms as rooms1 left join\n" +
                    "        (SELECT \n" +
                    "        roomcode, DATEDIFF(\n" +
                    "            IF(DATEDIFF(CURDATE(), checkout) >= 0, checkout, CURDATE()),\n" +
                    "            IF(DATEDIFF(CURDATE(), checkin) < 180, checkin, DATE_ADD(CURDATE(), INTERVAL -180 DAY)))as stayTime, \n" +
                    "        DATEDIFF(CURDATE(),checkout), checkin,checkout\n" +
                    "        from yehlaing.lab7_rooms\n" +
                    "            join yehlaing.lab7_reservations on roomcode= room\n" +
                    "        where (DATEDIFF(CURDATE(),checkout)< 180 and DATEDIFF(CURDATE(),checkout) >= 0 )\n" +
                    "        and   (DATEDIFF(CURDATE(),checkin) < 180 and DATEDIFF(CURDATE(),checkin) >= 0 ))as t1\n" +
                    "        on rooms1.roomcode = t1.roomcode\n" +
                    "    group by roomcode) as A\n" +
                    "join\n" +
                    "    (select B1.roomcode, \n" +
                    "        (SELECT\n" +
                    "        CASE\n" +
                    "            WHEN (NOT EXISTS \n" +
                    "                (SELECT *\n" +
                    "                FROM yehlaing.lab7_rooms join yehlaing.lab7_reservations on roomcode= room\n" +
                    "                WHERE roomcode = B1.roomcode and CURDATE() >= checkin and CURDATE() < checkout))\n" +
                    "            THEN\n" +
                    "                CURDATE()\n" +
                    "            ELSE \n" +
                    "            (select min(checkout)\n" +
                    "            from  yehlaing.lab7_reservations as RES_O\n" +
                    "            where RES_O.room = B1.roomcode and RES_O.checkout > CURDATE() \n" +
                    "                and RES_O.code NOT IN\n" +
                    "                    (select RES1.code\n" +
                    "                    from     yehlaing.lab7_reservations as RES1 \n" +
                    "                        join yehlaing.lab7_reservations as RES2 \n" +
                    "                        on RES1.room= RES2.room and RES1.checkout = RES2.checkin))\n" +
                    "        END) as nextAvail\n" +
                    "    from yehlaing.lab7_rooms as B1) as B\n" +
                    "on A.roomcode = B.roomcode\n" +
                    "join\n" +
                    "    (SELECT roomcode, roomname, checkout, DATEDIFF(checkout, checkin) as MostRecentLength\n" +
                    "    from yehlaing.lab7_rooms as rooms1 join yehlaing.lab7_reservations as res1 on roomcode= room\n" +
                    "    where checkout <CURDATE() and checkout = \n" +
                    "        (SELECT max(checkout) from\n" +
                    "            (SELECT roomcode, roomname, checkout, DATEDIFF(checkout, checkin) as roomdiff\n" +
                    "            from yehlaing.lab7_rooms join yehlaing.lab7_reservations on roomcode= room\n" +
                    "            where checkout <CURDATE()) as C1\n" +
                    "        where C1.roomcode = rooms1.roomcode\n" +
                    "        group by roomcode)\n" +
                    "    ) as C\n" +
                    "on A.roomcode = C.roomcode\n" +
                    "order by popScore desc";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String roomcode = rs.getString("roomname");
                    String popScore = rs.getString("popScore");
                    String nextAvail = rs.getString("nextAvail");
                    String mostRecentLength = rs.getString("MostRecentLength");


                    System.out.format("%-30s %-10s %-15s %-10s\n", roomcode, popScore, nextAvail, mostRecentLength);

                }
            }
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
                        } else {
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
                                } else {
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
                    values.add(firstName.toUpperCase());
                }
                if (!lastName.equals("")) {
                    params.add(" LastName = ?");
                    values.add(lastName.toUpperCase());
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
                    } else {
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


    // Demo4 - Establish JDBC connection, execute DML query (UPDATE) using PreparedStatement / transaction
    private void R4() throws SQLException {

        try (Connection conn = DriverManager.getConnection(
                System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a reservation code: ");
            String code = scanner.nextLine();

            String updateSql = "DELETE FROM yehlaing.lab7_reservations WHERE CODE = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, Integer.parseInt(code));
                int rowCount = pstmt.executeUpdate();

                if (rowCount > 0)
                    System.out.printf("Reservation #%s has been canceled successfully.", code);
                else
                    System.out.printf("Reservation #%s can't be found. Please try again.", code);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            } catch (NumberFormatException e) {
                System.out.printf("Invalid Reservation Code: %s. Please try again.", code);
                conn.rollback();
            }

        }
    }

    private String userInputR2(String field) {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        boolean validInput = false;
        while (!validInput) {
            String prompt = "Enter a " + field + " : ";

            if (field.equals("room code") || field.equals("desired bed type"))
                prompt = "Enter a " + field + " (or \"Any\" to indicate no preference): ";

            System.out.print(prompt);
            input = scanner.nextLine();

            validInput = verifyUserInputR2(input, field);
        }
        return input;
    }

    private boolean verifyUserInputR2(String input, String field) {
        if (input.isEmpty()) {
            System.out.println(field + " can't be empty. Please try again.");
            return false;
        }

        boolean checked = true;

        switch (field) {
            case "check-out date (format yyyy-mm-dd)":
            case "check-in date (format yyyy-mm-dd)":
                try {

                    LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date input: " + input + ". Correct date format: yyyy-mm-dd. Please try again.");
                    checked = false;
                }
                break;
            case "number of children":
            case "number of adults":
                try {
                    Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid " + field + " input: " + input + ". Please try again.");
                    checked = false;
                }
        }
        return checked;
    }


    private int findRoomsR2(Connection conn, List<String> res, List<String> resCheckin, int numOfPeople,
                            String checkin, String checkout,
                            String room, String bedType) throws SQLException {

        System.out.printf("\n %-10s %-10s %-30s %-7s %-12s %-7s %-12s %-14s [%s  ,   %s]\n",
                "Option #", "RoomCode", "RoomName", "Beds", "bedType", "maxOcc", "basePrice", "decor", "check-in", "check-out");

        /*------------------------------------------------------------------------------------------------------------------------------------*/
        //find exact matched results

        double exactMatchBasePrice = 0;
        String exactMatchDecor = "";

        int resFound = 0;

        String basesql =
                "SELECT * FROM yehlaing.lab7_rooms ";

        String exactMatchSQL = basesql;
        if (!room.equalsIgnoreCase("any"))
            exactMatchSQL += "WHERE RoomCode = ?";

        if (!bedType.equalsIgnoreCase("any")) {
            if (exactMatchSQL.endsWith("yehlaing.lab7_rooms "))
                exactMatchSQL += "WHERE bedType = ?";
            else
                exactMatchSQL += "AND bedType = ?";
        }

        //prepare and execute exactMatchSQL
        try (PreparedStatement pstmt = conn.prepareStatement(exactMatchSQL)) {
            //pstmt parameters
            int i = 1;
            if (!room.equalsIgnoreCase("any")) {
                pstmt.setString(i, room);
                i++;
            }
            if (!bedType.equalsIgnoreCase("any"))
                pstmt.setString(i, bedType);

            //execute exactmatchSQL
            try (ResultSet rs = pstmt.executeQuery()) {
                //looping through result set
                while (rs.next()) {
                    String roomCode = rs.getString("RoomCode");
                    int maxOcc = rs.getInt("maxOcc");

                    // skip the room if numOfPeole > max occupancy
                    if (maxOcc < numOfPeople)
                        continue;

                    //check if there is any reservation for this current room during desired checkin-checkout dates
                    String availableDateSQL =
                            "SELECT * FROM yehlaing.lab7_reservations " +
                                    "WHERE room = ? AND" +
                                    "(( ? < checkin AND ? > checkin) OR" +
                                    "( ? >= checkin AND ? <= checkout) OR" +
                                    "( ? < checkout AND ? > checkout))";

                    //prepare and execute availableDateSQL
                    try (PreparedStatement pstmtAvailableDateSQL = conn.prepareStatement(availableDateSQL)) {
                        pstmtAvailableDateSQL.setString(1, roomCode);
                        pstmtAvailableDateSQL.setString(2, checkin);
                        pstmtAvailableDateSQL.setString(3, checkout);
                        pstmtAvailableDateSQL.setString(4, checkin);
                        pstmtAvailableDateSQL.setString(5, checkout);
                        pstmtAvailableDateSQL.setString(6, checkin);
                        pstmtAvailableDateSQL.setString(7, checkout);
                        try (ResultSet availableDateRs = pstmtAvailableDateSQL.executeQuery()) {
                            //skip the room if there exist at least 1 reservation for this current room during the desired checkin-checkout date
                            if (availableDateRs.next())
                                continue;
                        }
                    }

                    //add roomcode to result
                    ++resFound;
                    res.add(roomCode);
                    resCheckin.add(checkin);

                    String roomName = rs.getString("RoomName");
                    int beds = rs.getInt("Beds");
                    String bedtype = rs.getString("bedType");
                    double baseprice = rs.getDouble("basePrice");
                    String decor = rs.getString("decor");

                    System.out.printf(" %-10d %-10s %-30s %-7d %-12s %-7d %-12.2f %-14s [%s, %s]\n",
                            resFound, roomCode, roomName, beds, bedtype, maxOcc, baseprice, decor, checkin, checkout);
                }
            }

        }

        /*------------------------------------------------------------------------------------------------------------------------------------*/
        //if there is no exact matched results found, find other room suggestion

        if (resFound == 0) {
            System.out.println("No exact matched rooms found. Suggested other options:");

            List<Object> params = new ArrayList<Object>();

            //find rooms that are available during the desired checkin-checkout date
            String sql1 = basesql +
                    "WHERE NOT EXISTS " +
                    "(SELECT * FROM yehlaing.lab7_reservations " +
                    "WHERE room = roomcode AND " +
                    "(( ? < checkin AND ? > checkin) OR " +
                    "( ? >= checkin AND ? <= checkout) OR " +
                    "( ? < checkout AND ? > checkout)))";
            params.add(checkin);
            params.add(checkout);
            params.add(checkin);
            params.add(checkout);
            params.add(checkin);
            params.add(checkout);

            //find room with similar bedtype
            String sql2 = basesql;
            if (!bedType.equalsIgnoreCase("any")) {
                params.add(bedType);
                sql2 += "WHERE bedType = ? ";
            }

            //find room with similar decor
            String sql3 = basesql;
            if (!exactMatchDecor.isEmpty()) {
                params.add(exactMatchDecor);
                sql3 += "WHERE decor = ? ";
            }

            //find room with similar basePrice
            String sql4 = basesql;
            if (Double.compare(exactMatchBasePrice, 0.0) != 0) {
                params.add(exactMatchBasePrice);
                sql4 += "WHERE basePrice = ? ";
            }

            // find room with the same roomcode
            String sql5 = basesql;
            if (!room.equalsIgnoreCase("any")) {
                params.add(room);
                sql5 += "WHERE roomcode = ? ";
            }

            // union all above sql statements
            String sql = sql1 + " UNION " + sql2 + " UNION ";
            if (!exactMatchDecor.isEmpty())
                sql += sql3 + " UNION ";
            if (Double.compare(exactMatchBasePrice, 0.0) != 0)
                sql += sql4 + " UNION ";
            sql += sql5 + " UNION " + basesql;

            //find how many days between checkin and checkout dates
            LocalDate checkinDate = LocalDate.parse(checkin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate checkoutDate = LocalDate.parse(checkout, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            diff = ChronoUnit.DAYS.between(checkinDate, checkoutDate);

            //prepare and execute the union sql
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int i = 1;
                for (Object p : params) {
                    pstmt.setObject(i++, p);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    //looping through the result set
                    while (resFound < 5 && rs.next()) {
                        String roomCode = rs.getString("RoomCode");
                        int maxOcc = rs.getInt("maxOcc");
                        String nextCheckin = checkin;

                        //skip the room if numofpeople > max occupancy of the room
                        if (maxOcc < numOfPeople)
                            continue;

                        //check if there is any reservation for this current room during desired checkin-checkout dates
                        String availableDateSQL =
                                "SELECT * FROM yehlaing.lab7_reservations " +
                                        "WHERE room = ? AND " +
                                        "(( ? < checkin AND ? > checkin) OR " +
                                        "( ? >= checkin AND ? <= checkout) OR " +
                                        "( ? < checkout AND ? > checkout))";

                        try (PreparedStatement pstmtAvailableDateSQL = conn.prepareStatement(availableDateSQL)) {
                            pstmtAvailableDateSQL.setString(1, roomCode);
                            pstmtAvailableDateSQL.setString(2, checkin);
                            pstmtAvailableDateSQL.setString(3, checkout);
                            pstmtAvailableDateSQL.setString(4, checkin);
                            pstmtAvailableDateSQL.setString(5, checkout);
                            pstmtAvailableDateSQL.setString(6, checkin);
                            pstmtAvailableDateSQL.setString(7, checkout);

                            try (ResultSet availableDateRs = pstmtAvailableDateSQL.executeQuery()) {
                                //if the room has been booked during the desired checkin-checkout dates, consider the next available date of the room
                                if (availableDateRs.next()) {
                                    String maxCheckoutSQL = "SELECT max(checkout) FROM yehlaing.lab7_reservations WHERE room = ?";
                                    try (PreparedStatement pstmtMaxCheckoutSQL = conn.prepareStatement(maxCheckoutSQL)) {
                                        pstmtMaxCheckoutSQL.setString(1, roomCode);

                                        try (ResultSet maxCheckoutRs = pstmtMaxCheckoutSQL.executeQuery()) {
                                            maxCheckoutRs.next();
                                            nextCheckin = maxCheckoutRs.getString(1);
                                        }
                                    }
                                }
                            }
                        }

                        //add the room to the result
                        ++resFound;
                        res.add(roomCode);
                        resCheckin.add(nextCheckin);

                        String roomName = rs.getString("RoomName");
                        int beds = rs.getInt("Beds");
                        String bedtype = rs.getString("bedType");
                        double baseprice = rs.getDouble("basePrice");
                        String decor = rs.getString("decor");

                        //assume the length of stays also the same as the original checkin-checkout
                        // => nextcheckout = nextcheckin + length of stays of the original checkin-checkout
                        LocalDate nextCheckout = LocalDate.parse(nextCheckin, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(diff);

                        System.out.printf(" %-10d %-10s %-30s %-7d %-12s %-7d %-12.2f %-14s [%s,  %s]\n",
                                resFound, roomCode, roomName, beds, bedtype, maxOcc, baseprice, decor, nextCheckin, nextCheckout);
                    }
                }


            }
        }

        return resFound;
    }

    private long getNumberOfWeekDays(LocalDate startDate, LocalDate endDate) {

        Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        final long weekDaysBetween = startDate.datesUntil(endDate)
                .filter(d -> !weekend.contains(d.getDayOfWeek()))
                .count();

        return weekDaysBetween;
    }

    private long getNumberOfWeekendDays(LocalDate startDate, LocalDate endDate) {

        Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        final long weekendDaysBetween = startDate.datesUntil(endDate)
                .filter(d -> weekend.contains(d.getDayOfWeek()))
                .count();

        return weekendDaysBetween;
    }

    private void bookingConfirmationR2(Connection conn, List<String> res, List<String> resCheckin,
                                       int opNum, int numOfChild, int numOfAdults,
                                       String oCheckin, String oCheckout,
                                       String firstname, String lastname) throws SQLException {

        String roomCode = res.get(opNum);
        String bedType, roomName;
        LocalDate checkin, checkout;
        double basePrice = 0.0;

        String sql =
                "SELECT * FROM yehlaing.lab7_rooms WHERE roomcode = ?";
        //prepare and execute sql to get the room information
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                roomName = rs.getString("RoomName");
                bedType = rs.getString("bedType");
                basePrice = rs.getDouble("basePrice");

                //get the checkin date from the checkin result list
                //re-calculate the checkout date. Notes: if checkin = original checkin, checkout = original checkout
                // assume the length of stays also the same as the original checkin-checkout
                //  => newcheckout = newcheckin + length of stays of the original checkin-checkout
                checkin = LocalDate.parse(resCheckin.get(opNum), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                checkout = checkin.plusDays(diff);
            }
        }

        System.out.println("Booking confirmation:");
        System.out.println("Firstname, Lastname: " + firstname + ", " + lastname);
        System.out.println("Number of adults: " + numOfAdults);
        System.out.println("Number of children: " + numOfChild);
        System.out.println("Room Information:");
        System.out.printf("\t\t%-10s %-30s %-12s [%s,  %s]\n",
                "RoomCode", "RoomName", "BedType", "checkin", "checkout");
        System.out.printf("\t\t%-10s %-30s %-12s [%s,  %s]\n",
                roomCode, roomName, bedType, checkin, checkout);
        //total cost = # of weekdays * baseprice + # of weekend days * basePrice * 110%
        double total = getNumberOfWeekDays(checkin, checkout) * basePrice +
                getNumberOfWeekendDays(checkin, checkout) * basePrice * 1.1;
        System.out.printf("Total cost of stay: %.2f\n", total);

        Scanner scanner = new Scanner(System.in);
        String formControl = "";

        while (!formControl.equalsIgnoreCase("yes") && !formControl.equalsIgnoreCase("no")) {
            System.out.println("\nType YES to confirm the booking or NO to cancel your request");
            formControl = scanner.nextLine();
        }

        if (formControl.equalsIgnoreCase("yes")) {
            String insertSql = "INSERT INTO yehlaing.lab7_reservations " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            conn.setAutoCommit(false);

            //prepare and execute insertSQL
            try (PreparedStatement pstmtInsertSQL = conn.prepareStatement(insertSql)) {
                //reservationCode = max(reservationCode) from the reservations table + 1
                long reservationCode = 0;
                //SQL to find the max reservation code
                String maxReservationCode = "SELECT max(code) FROM yehlaing.lab7_reservations";

                //execute maxReservationCode
                try (Statement maxReservationCodeStmt = conn.createStatement();
                     ResultSet rs = maxReservationCodeStmt.executeQuery(maxReservationCode)) {
                    rs.next();
                    reservationCode = rs.getInt(1) + 1;
                }

                //rate = total cost / # of nights
                double rate = total / diff;
                pstmtInsertSQL.setLong(1, reservationCode);
                pstmtInsertSQL.setString(2, roomCode);
                pstmtInsertSQL.setDate(3, java.sql.Date.valueOf(checkin));
                pstmtInsertSQL.setDate(4, java.sql.Date.valueOf(checkout));
                pstmtInsertSQL.setDouble(5, rate);
                pstmtInsertSQL.setString(6, lastname);
                pstmtInsertSQL.setString(7, firstname);
                pstmtInsertSQL.setInt(8, numOfAdults);
                pstmtInsertSQL.setInt(9, numOfChild);

                pstmtInsertSQL.executeUpdate();

                System.out.println("Your request has been submitted successfully. Your Reservation code is " + reservationCode);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        } else
            System.out.println("Your request has been cancelled successfully.");

    }

    private void R2() throws SQLException {

        try (Connection conn = DriverManager.getConnection(
                System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            while (true) {
                System.out.println("Room Reservation");

                System.out.println("Press Any key to continue or Q to quit the form");

                Scanner scanner = new Scanner(System.in);
                String formControl = scanner.nextLine();
                if (formControl.equalsIgnoreCase("q"))
                    break;

                System.out.println("Booking information: ");

                String firstname = userInputR2("firstname");
                String lastname = userInputR2("lastname");
                String room = userInputR2("room code");
                String bedType = userInputR2("desired bed type");
                String checkin = userInputR2("check-in date (format yyyy-mm-dd)");
                String checkout = userInputR2("check-out date (format yyyy-mm-dd)");
                int numOfChilds = Integer.parseInt(userInputR2("number of children"));
                int numOfAdults = Integer.parseInt(userInputR2("number of adults"));

                LocalDate checkinDate = LocalDate.parse(checkin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate checkoutDate = LocalDate.parse(checkout, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                diff = ChronoUnit.DAYS.between(checkinDate, checkoutDate);

                //find max(maxOcc) of all rooms
                String maxOccSql = "SELECT max(maxOcc) FROM yehlaing.lab7_rooms ";
                int maxOcc = 0;

                //execute maxOccSql
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(maxOccSql)) {

                    rs.next();
                    maxOcc = rs.getInt(1);
                }

                // if # of people > max(maxOcc), return to main menu
                if (numOfChilds + numOfAdults > maxOcc)
                    System.out.printf("No suitable rooms are available. Rooms with maximum occupancy can accommodate %d people.\n", maxOcc);
                else {
                    List<String> res = new ArrayList<>();
                    List<String> resCheckin = new ArrayList<>();

                    //find all possible rooms
                    int numOfOptions = findRoomsR2(conn, res, resCheckin, numOfAdults + numOfChilds, checkin, checkout, room, bedType);
                    int opNum = 0;

                    // let customer choose 1 room
                    formControl = "";
                    while (opNum == 0) {
                        System.out.println("\nEnter an option # associated with the room you wish to reserve, or type Quit to return to Main Menu");
                        formControl = scanner.nextLine();

                        if (formControl.equalsIgnoreCase("quit"))
                            break;

                        try {
                            opNum = Integer.parseInt(formControl);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid option #. Please try again.");
                        }

                        if (opNum > numOfOptions || opNum <= 0) {
                            opNum = 0;
                            System.out.println("Invalid option #. Please try again.");
                        }
                    }

                    //if the a room is chosen, confirm the booking
                    if (opNum != 0) {
                        bookingConfirmationR2(conn, res, resCheckin, opNum - 1, numOfChilds, numOfAdults, checkin, checkout, firstname, lastname);
                    }
                }

                System.out.println("Returning to main menu...");

                System.out.println("\n------------------------------------------------\n");
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
                while (rs.next()) {
                    String roomcode = rs.getString("roomname");
                    String popScore = rs.getString("popScore");
                    String nextAvail = rs.getString("nextAvail");
                    String mostRecentLength = rs.getString("MostRecentLength");

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
                }
            }
        }


    }
}

