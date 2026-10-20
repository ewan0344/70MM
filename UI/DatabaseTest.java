
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection con = DatabaseManager.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT tmdb_id, title, release_year " +
                 "FROM movies_new LIMIT 5")) {

            System.out.println("Connected successfully!");
            System.out.println("First 5 movies:");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("tmdb_id") + " | " +
                    rs.getString("title") + " | " +
                    rs.getInt("release_year")
                );
            }

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
