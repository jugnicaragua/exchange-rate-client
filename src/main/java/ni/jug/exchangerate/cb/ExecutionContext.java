package ni.jug.exchangerate.cb;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 *
 * @author Armando Alaniz
 * @version 2.0
 * @since 1.0
 */
public final class ExecutionContext {

    private static final ExecutionContext INSTANCE = new ExecutionContext();

    private final ConcurrentMap<String, Set<Cookie>> cookiesByBank;

    private ExecutionContext() {
        this.cookiesByBank = new ConcurrentHashMap<>();
    }

    public ExecutionContext addOrReplaceCookie(String bank, Cookie cookie) {
        Objects.requireNonNull(bank);
        Objects.requireNonNull(cookie);
        Set<Cookie> cookies = cookiesByBank.get(bank);
        if (cookies == null) {
            cookies = new HashSet<>();
            cookiesByBank.putIfAbsent(bank, cookies);
        }
        synchronized (this) {
            cookies.add(cookie);
        }
        return this;
    }

    public ExecutionContext addOrReplaceCookie(String bank, String name, String value) {
        return addOrReplaceCookie(bank, new Cookie(name, value));
    }

    public ExecutionContext addOrReplaceBdfCookies(String bank, Cookie... cookies) {
        if (!(cookies == null || cookies.length == 0)) {
            for (int i = 0; i < cookies.length; i++) {
                addOrReplaceCookie(bank, cookies[i]);
            }
        }
        return this;
    }

    public Map<String, String> cookies(String bank) {
        Objects.requireNonNull(bank);
        return Collections.unmodifiableMap(cookiesByBank.getOrDefault(bank, Collections.emptySet())
                .stream()
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue)));
    }

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

}
