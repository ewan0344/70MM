public class RecommendedMovie {

    private int id;
    private String title;
    private Integer year;
    private String genres;
    private String director;
    private String posterPath;

    public RecommendedMovie(int id, String title, Integer year,
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
}