package pl.edu.agh.papaya.security;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private TokenRepository tokenRepository;

    public TokenAuthenticationFilter() {
        super(req -> true);
        super.setAuthenticationManager(auth -> auth);
        super.setAuthenticationSuccessHandler((request, response, authentication) -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        return !requestURI.startsWith("/rest/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String param = request.getHeader(HttpHeaders.AUTHORIZATION);

        String token = Optional.ofNullable(param)
                .flatMap(this::stripBearer)
                .map(String::trim)
                .orElseThrow(() -> new BadCredentialsException("Missing token"));

        return tokenRepository.getUserDetails(token)
                .map(det -> createAuthentication(token, det))
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));
    }

    private Authentication createAuthentication(String token, UserDetails details) {
        return new UsernamePasswordAuthenticationToken(details.getUsername(), token, details.getAuthorities());
    }

    private Optional<String> stripBearer(String auth) {
        if (auth.startsWith(BEARER_PREFIX)) {
            return Optional.of(auth.substring(BEARER_PREFIX.length()));
        }

        return Optional.empty();
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
