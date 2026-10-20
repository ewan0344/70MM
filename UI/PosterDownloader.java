
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;

public class PosterDownloader {

    public static void main(String[] args) {

        Path folder = Path.of("posters");

        try {
            Files.createDirectories(folder);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String sql = """
                SELECT tmdb_id, poster_path
                FROM movies_new
                WHERE poster_path IS NOT NULL
                  AND TRIM(poster_path) <> ''
                ORDER BY tmdb_id
                """;

            int downloaded = 0;
            int skipped = 0;
            int failed = 0;

            try (Connection con = DatabaseManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int id = rs.getInt("tmdb_id");
                    String posterPath = rs.getString("poster_path");

                    Path output = folder.resolve(id + ".jpg");

                    // Keep existing downloads; they don't need downloading again.
                    if (Files.exists(output)
                            && Files.size(output) > 0) {
                        skipped++;
                        continue;
                    }

                    try {
                        String url;

                        if (posterPath.startsWith("https://")
                                || posterPath.startsWith("http://")) {
                            url = posterPath;
                        } else {
                            if (!posterPath.startsWith("/")) {
                                posterPath = "/" + posterPath;
                            }

                            url = "https://image.tmdb.org/t/p/w342"
                                    + posterPath;
                        }

                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(Duration.ofSeconds(25))
                                .header("User-Agent", "70MM-Movie-App")
                                .GET()
                                .build();

                        HttpResponse<byte[]> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofByteArray()
                        );

                        String contentType = response.headers()
                                .firstValue("Content-Type")
                                .orElse("");

                        if (response.statusCode() == 200
                                && contentType.startsWith("image/")) {

                            Files.write(output, response.body());
                            downloaded++;

                            System.out.println(
                                    "Downloaded " + id + ".jpg"
                            );

                        } else {
                            failed++;
                            System.out.println(
                                    "Unavailable: movie ID " + id
                                    + " (HTTP " + response.statusCode()
                                    + ")"
                            );
                        }

                        // Avoid sending requests too quickly.
                        Thread.sleep(100);

                    } catch (Exception ex) {
                        failed++;
                        System.out.println(
                                "Failed movie ID " + id + ": "
                                + ex.getMessage()
                        );
                    }
                }
            }

            System.out.println("\nPoster download finished!");
            System.out.println("Downloaded: " + downloaded);
            System.out.println("Already existed: " + skipped);
            System.out.println("Failed/unavailable: " + failed);
            System.out.println("Folder: " + folder.toAbsolutePath());

        } catch (Exception e) {
            System.out.println(
                    "Poster downloader failed: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
