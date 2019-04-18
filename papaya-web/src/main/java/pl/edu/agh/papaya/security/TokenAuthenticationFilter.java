package pl.edu.agh.papaya.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
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
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.repository.UserRepository;

@Component
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<Pattern> UNSECURED_URLS = Arrays.asList(
            Pattern.compile("/login/?"),
            Pattern.compile("/h2(|/.*)"));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ServletContext servletContext;

    public TokenAuthenticationFilter() {
        super(req -> true);
        super.setAuthenticationManager(auth -> auth);
        super.setAuthenticationSuccessHandler((request, response, authentication) -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String contextPath = servletContext.getContextPath();
        String requestUri = request.getRequestURI();
        String relativeRequestUri = requestUri.startsWith(contextPath) ?
                requestUri.substring(contextPath.length()) :
                requestUri;

        return UNSECURED_URLS.stream()
                .map(p -> p.matcher(relativeRequestUri))
                .noneMatch(Matcher::matches);
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
        User user = userRepository.findByEmail(details.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid token, no user"));

        UserPrincipal principal = new UserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, token, details.getAuthorities());
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
