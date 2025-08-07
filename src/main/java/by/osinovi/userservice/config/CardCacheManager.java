package by.osinovi.userservice.config;

import by.osinovi.userservice.dto.card.CardResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardCacheManager {

    private static final String CACHE_PREFIX = "cards::";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, CardResponseDto> redisTemplate;

    public void cacheCard(String id, CardResponseDto card) {
        if (id != null && card != null) {
            String key = CACHE_PREFIX + id;
            redisTemplate.opsForValue().set(key, card, DEFAULT_TTL.getSeconds(), TimeUnit.SECONDS);
            log.debug("Cached card with id: {}", id);
        }
    }

    public CardResponseDto getCard(String id) {
        String key = CACHE_PREFIX + id;
        CardResponseDto cached = redisTemplate.opsForValue().get(key);
        log.debug("Cache hit for card id: {}, result: {}", id, cached != null);
        return cached;

    }

    public void evictCard(String id) {
        if (id != null) {
            redisTemplate.delete(CACHE_PREFIX + id);
            log.debug("Evicted card cache for id: {}", id);
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