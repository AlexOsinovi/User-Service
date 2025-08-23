package by.osinovi.userservice.config;

import by.osinovi.userservice.dto.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCacheManager {

    private static final String CACHE_PREFIX = "users::";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, UserResponseDto> redisTemplate;

    public void cacheUser(String id, String email, UserResponseDto user) {
        if (id != null && user != null) {
            String idKey = CACHE_PREFIX + "id:" + id;
            String emailKey = CACHE_PREFIX + "email:" + email;
            redisTemplate.opsForValue().set(idKey, user, DEFAULT_TTL.getSeconds(), TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(emailKey, user, DEFAULT_TTL.getSeconds(), TimeUnit.SECONDS);
            log.debug("Cached user with id: {}, email: {}", id, email);
        }
    }

    public UserResponseDto getUserById(String id) {
        String key = CACHE_PREFIX + "id:" + id;
        UserResponseDto cached = redisTemplate.opsForValue().get(key);
        log.debug("Cache hit for user id: {}, result: {}", id, cached != null);
        return cached;
    }

    public UserResponseDto getUserByEmail(String email) {
        String key = CACHE_PREFIX + "email:" + email;
        UserResponseDto cached = redisTemplate.opsForValue().get(key);
            log.debug("Cache hit for user email: {}, result: {}", email, cached != null);
            return cached;
    }

    public void evictUser(String id, String email) {
        if (id != null) {
            redisTemplate.delete(CACHE_PREFIX + "id:" + id);
            log.debug("Evicted user cache for id: {}", id);
        }
        if (email != null) {
            redisTemplate.delete(CACHE_PREFIX + "email:" + email);
            log.debug("Evicted user cache for email: {}", email);
        }
    }

    public void clearAll() {
        var keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cleared all card cache entries");
        }
    }

}