package ni.jug.cb.exchangerate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class ExecutionContext {

    private static final ExecutionContext INSTANCE = new ExecutionContext();

    private final ConcurrentMap<String, String> bdfCookies;

    public ExecutionContext() {
        this.bdfCookies = new ConcurrentHashMap<>();
    }

    public ExecutionContext addBdfCookie(Cookie cookie) {
        bdfCookies.put(cookie.getName(), cookie.getValue());
        return this;
    }

    public ExecutionContext addBdfCookies(Cookie... cookies) {
        if (!(cookies == null || cookies.length == 0)) {
            for (Cookie cookie : cookies) {
                bdfCookies.put(cookie.getName(), cookie.getValue());
            }
        }
        return this;
    }

    public Map<String, String> bdfCookies() {
        return Collections.unmodifiableMap(bdfCookies);
    }

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

}
