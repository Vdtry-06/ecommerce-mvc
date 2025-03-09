package vdtry06.springboot.ecommerce.cache;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String getFromCache(String key) {
        return cache.get(key);
    }

    public void saveToCache(String key, String value) {
        cache.put(key, value);
    }
}
