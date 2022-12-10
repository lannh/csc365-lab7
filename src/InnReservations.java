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
                case 1: ir.R1(); break;
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
                while(rs.next()){
                    String roomcode = rs.getString("roomname");
                    String popScore = rs.getString("popScore");
                    String nextAvail = rs.getString("nextAvail");
                    String mostRecentLength = rs.getString("MostRecentLength");


                    System.out.format("%-30s %-10s %-15s %-10s\n", roomcode, popScore, nextAvail, mostRecentLength);

                }
            }
            }
        }
}
