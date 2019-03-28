package ni.jug.cb.exchangerate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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

    private ExecutionContext() {
        this.bdfCookies = new ConcurrentHashMap<>();
    }

    public ExecutionContext addOrReplaceBdfCookie(Cookie cookie) {
        Objects.requireNonNull(cookie);
        String old = bdfCookies.get(cookie.getName());
        if (old == null) {
            bdfCookies.putIfAbsent(cookie.getName(), cookie.getValue());
        } else {
            bdfCookies.replace(cookie.getName(), old, cookie.getValue());
        }
        return this;
    }

    public ExecutionContext addOrReplaceBdfCookies(Cookie... cookies) {
        if (!(cookies == null || cookies.length == 0)) {
            for (int i = 0; i < cookies.length; i++) {
                addOrReplaceBdfCookie(cookies[i]);
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
