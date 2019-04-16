package pl.edu.agh.papaya.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
class TokenRepository {

    private static final int TOKEN_CACHE_MAXIMUM_SIZE = 1000;
    private static final Duration TOKEN_CACHE_EXPIRATION_TIME = Duration.ofHours(2);

    private transient Cache<Object, UserDetails> tokenCache =
            CacheBuilder.newBuilder()
                    .maximumSize(TOKEN_CACHE_MAXIMUM_SIZE)
                    .expireAfterAccess(TOKEN_CACHE_EXPIRATION_TIME)
                    .build();

    Optional<UserDetails> getUserDetails(Object token) {
        return Optional.ofNullable(tokenCache.getIfPresent(token));
    }

    String newToken(UserDetails user) {
        String token = UUID.randomUUID().toString();
        tokenCache.put(token, user);
        return token;
    }
}
