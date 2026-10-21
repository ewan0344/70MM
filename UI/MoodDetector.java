import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoodDetector {

    public static List<String> detectPreferredGenres(String thought) {
        String text = thought == null
                ? ""
                : thought.toLowerCase(Locale.ROOT);

        List<String> genres = new ArrayList<>();

        if (containsAny(text, "sad", "unhappy", "heartbroken",
                "lonely", "depressed", "crying")) {
            addGenres(genres, "Comedy", "Family", "Animation",
                    "Adventure", "Romance");
        }

        if (containsAny(text, "stressed", "stress", "tired",
                "exhausted", "overwhelmed", "anxious")) {
            addGenres(genres, "Comedy", "Family", "Animation",
                    "Fantasy");
        }

        if (containsAny(text, "happy", "excited", "joyful",
                "great", "good mood")) {
            addGenres(genres, "Comedy", "Adventure", "Fantasy",
                    "Animation");
        }

        if (containsAny(text, "scared", "fear", "terrified",
                "spooky", "horror")) {
            addGenres(genres, "Horror", "Thriller", "Mystery");
        }

        if (containsAny(text, "adventurous", "adventure",
                "thrilling", "action", "energetic")) {
            addGenres(genres, "Adventure", "Action",
                    "Fantasy", "Science Fiction");
        }

        if (containsAny(text, "romantic", "love", "crush",
                "heartwarming")) {
            addGenres(genres, "Romance", "Comedy", "Drama");
        }

        if (containsAny(text, "curious", "thinking",
                "confused", "mysterious", "mind blown")) {
            addGenres(genres, "Mystery", "Science Fiction",
                    "Thriller", "Drama");
        }

        
        if (genres.isEmpty()) {
            addGenres(genres, "Comedy", "Adventure", "Drama",
                    "Fantasy", "Mystery", "Animation");
        }

        return genres;
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static void addGenres(List<String> genres,
                                  String... newGenres) {
        for (String genre : newGenres) {
            if (!genres.contains(genre)) {
                genres.add(genre);
            }
        }
    }
}