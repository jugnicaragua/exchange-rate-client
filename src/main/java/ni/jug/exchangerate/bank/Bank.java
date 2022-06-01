package ni.jug.exchangerate.bank;

/**
 * @author aalaniz
 */
public final class Bank {

    private final String id;
    private final String description;
    private final String url;

    public Bank(String id, String description, String url) {
        this.id = id;
        this.description = description;
        this.url = url;
    }

    public Bank(BankScraper scraper) {
        this(scraper.bank(), scraper.description(), scraper.url());
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
