package ni.jug.exchangerate.cb;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public final class ExecutionContext {

    private static final ExecutionContext INSTANCE = new ExecutionContext();

    private final ConcurrentMap<String, ConcurrentMap<String, String>> cookiesByBank;

    private ExecutionContext() {
        this.cookiesByBank = new ConcurrentHashMap<>();
    }

    private ConcurrentMap<String, String> initMapIfNecessary(String bank) {
        ConcurrentMap<String, String> cookies = cookiesByBank.get(bank);
        if (cookies == null) {
            cookies = new ConcurrentHashMap<>();
            cookiesByBank.putIfAbsent(bank, cookies);
        }
        return cookies;
    }

    public ExecutionContext addOrReplaceCookie(String bank, Cookie cookie) {
        Objects.requireNonNull(bank);
        Objects.requireNonNull(cookie);
        ConcurrentMap<String, String> cookies = initMapIfNecessary(bank);
        cookies.put(cookie.getName(), cookie.getValue());
        return this;
    }

    public ExecutionContext addOrReplaceCookies(String bank, Cookie... cookies) {
        if (!(cookies == null || cookies.length == 0)) {
            for (int i = 0; i < cookies.length; i++) {
                addOrReplaceCookie(bank, cookies[i]);
            }
        }
        return this;
    }

    public ExecutionContext addOrReplaceCookie(String bank, String name, String value) {
        Objects.requireNonNull(bank);
        Objects.requireNonNull(value);
        ConcurrentMap<String, String> cookies = initMapIfNecessary(bank);
        cookies.put(name, value);
        return this;
    }

    public ExecutionContext addOrReplaceCookie(String bank, Map<String, String> cookies) {
        if (!(cookies == null || cookies.isEmpty())) {
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                addOrReplaceCookie(bank, cookie.getKey(), cookie.getValue());
            }
        }
        return this;
    }

    public Map<String, String> cookies(String bank) {
        Objects.requireNonNull(bank);
        if (cookiesByBank.containsKey(bank)) {
            return cookiesByBank.get(bank);
        } else {
            return Collections.emptyMap();
        }
    }

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

}
