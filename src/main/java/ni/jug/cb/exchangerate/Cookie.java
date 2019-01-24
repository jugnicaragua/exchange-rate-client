package ni.jug.cb.exchangerate;

import java.util.Objects;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class Cookie {

    private final String name;
    private final String value;

    public Cookie(String name, String value) {
        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Cookie{" + name + "=" + value + '}';
    }

}
