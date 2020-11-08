package ni.jug.exchangerate;

/**
 *
 * @author aalaniz
 * @version 1.0
 * @since 2.0
 */
public final class CommercialBank {

    private final String id;
    private final String description;
    private final String url;

    public CommercialBank(String id, String description, String url) {
        this.id = id;
        this.description = description;
        this.url = url;
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
        return "CommercialBank{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
