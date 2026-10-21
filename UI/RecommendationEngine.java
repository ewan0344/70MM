import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecommendationEngine {

    private static final int RECOMMENDATION_COUNT = 5;
    private static final int CANDIDATE_LIMIT = 200;

    
    private static final Set<Integer> recentlyRecommended =
            new HashSet<>();

    public synchronized List<RecommendedMovie> recommend(String thought)
            throws SQLException {

        List<String> genres =
                MoodDetector.detectPreferredGenres(thought);

        List<RecommendedMovie> candidates = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT tmdb_id, title, release_year, genres, " +
            "director, poster_path FROM movies_new WHERE ("
        );

        for (int i = 0; i < genres.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("genres LIKE ?");
        }

        sql.append(") ORDER BY RAND() LIMIT ")
           .append(CANDIDATE_LIMIT);

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < genres.size(); i++) {
                statement.setString(i + 1, "%" + genres.get(i) + "%");
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("tmdb_id");

                    int yearValue = rs.getInt("release_year");
                    Integer year = rs.wasNull() ? null : yearValue;

                    candidates.add(new RecommendedMovie(
                        id,
                        rs.getString("title"),
                        year,
                        rs.getString("genres"),
                        rs.getString("director"),
                        rs.getString("poster_path")
                    ));
                }
            }
        }

       
        Collections.shuffle(candidates);

        List<RecommendedMovie> recommendations = new ArrayList<>();

        
        for (RecommendedMovie movie : candidates) {
            if (!recentlyRecommended.contains(movie.getId())) {
                recommendations.add(movie);

                if (recommendations.size() == RECOMMENDATION_COUNT) {
                    break;
                }
            }
        }

        
        if (recommendations.size() < RECOMMENDATION_COUNT) {
            for (RecommendedMovie movie : candidates) {
                if (!recommendations.contains(movie)) {
                    recommendations.add(movie);

                    if (recommendations.size()
                            == RECOMMENDATION_COUNT) {
                        break;
                    }
                }
            }
        }

        for (RecommendedMovie movie : recommendations) {
            recentlyRecommended.add(movie.getId());
        }

        
        if (recentlyRecommended.size() > 500) {
            recentlyRecommended.clear();

            for (RecommendedMovie movie : recommendations) {
                recentlyRecommended.add(movie.getId());
            }
        }

        return recommendations;
    }
}