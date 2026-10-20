
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {

        Path sqlFile = Path.of("database", "movies_new.sql");

        if (!Files.exists(sqlFile)) {
            System.out.println(
                "movies_new.sql not found: "
                + sqlFile.toAbsolutePath()
            );
            return;
        }

        try (Connection con = DatabaseManager.getConnection();
             Statement stmt = con.createStatement()) {

            // Create the table if it doesn't exist.
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movies_new (
                    tmdb_id INT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    release_year INT NULL,
                    duration INT NULL,
                    genres VARCHAR(500) NULL,
                    age_rating VARCHAR(30) NULL,
                    director VARCHAR(500) NULL,
                    production_house VARCHAR(500) NULL,
                    poster_path VARCHAR(255) NULL
                )
                """);

            // Check whether the table already contains movies.
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM movies_new")) {

                rs.next();

                if (rs.getLong(1) > 0) {
                    System.out.println(
                        "Movie database already populated. Skipping import."
                    );
                    return;
                }
            }

            System.out.println("Importing movie data...");

            StringBuilder sql = new StringBuilder();
            boolean readingInsert = false;

            // Read the SQL dump and execute only INSERT statements.
            try (var reader = Files.newBufferedReader(sqlFile)) {

                String line;

                while ((line = reader.readLine()) != null) {

                    String trimmed = line.trim();

                    if (!readingInsert) {

                        if (trimmed.startsWith(
                                "INSERT INTO `movies_new`")
                                || trimmed.startsWith(
                                "INSERT INTO movies_new")) {

                            readingInsert = true;
                            sql.append(line).append('\n');

                        } else {
                            continue;
                        }

                    } else {
                        sql.append(line).append('\n');
                    }

                    // Execute each complete INSERT statement.
                    if (trimmed.endsWith(";")) {

                        stmt.execute(sql.toString());

                        sql.setLength(0);
                        readingInsert = false;
                    }
                }
            }

            if (readingInsert) {
                throw new IllegalStateException(
                    "Incomplete INSERT statement in movies_new.sql"
                );
            }

            // Verify the imported movie count.
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM movies_new")) {

                rs.next();

                System.out.println(
                    "Import finished. Movies loaded: "
                    + rs.getLong(1)
                );
            }

        } catch (Exception e) {

            System.out.println(
                "Database setup failed: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
