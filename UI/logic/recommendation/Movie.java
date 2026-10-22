package logic.recommendation;

public class Movie {

    private final int id;
    private final String title;
    private final Integer year;
    private final String genres;
    private final String director;
    private final String posterPath;

    public Movie(int id, String title, Integer year,
                 String genres, String director,
                 String posterPath) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.genres = genres;
        this.director = director;
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public String getGenres() {
        return genres;
    }

    public String getDirector() {
        return director;
    }

    public String getPosterPath() {
        return posterPath;
    }

    @Override
    public String toString() {
        String movieYear = year == null
                ? "Year unknown" : year.toString();

        return title + " (" + movieYear + ")";
    }
}