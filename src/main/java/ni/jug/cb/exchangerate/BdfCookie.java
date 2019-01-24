package ni.jug.cb.exchangerate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class BdfCookie {

    private static BdfCookie instance;

    private final Map<String, String> cookies;

    public BdfCookie(Cookie cookie1, Cookie cookie2) {
        Objects.requireNonNull(cookie1);
        Objects.requireNonNull(cookie2);
        cookies = new HashMap<>(2);
        cookies.put(cookie1.getName(), cookie1.getValue());
        cookies.put(cookie2.getName(), cookie2.getValue());
    }

    public Map<String, String> cookies() {
        return Collections.unmodifiableMap(cookies);
    }

    public static void setInstance(Cookie cookie1, Cookie cookie2) {
        instance = new BdfCookie(cookie1, cookie2);
    }

    public static BdfCookie getInstance() {
        return instance;
    }

    public static Cookie createIncapsulaVisidCookie(String value) {
        return new Cookie("visid_incap_1796147", value);
    }

}
