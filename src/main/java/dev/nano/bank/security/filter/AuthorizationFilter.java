package dev.nano.bank.security.filter;

import dev.nano.bank.security.constant.SecurityConstant;
import dev.nano.bank.security.jwtProvider.JWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@Component
@AllArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private JWTProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase(SecurityConstant.OPTIONS_HTTP_METHOD)) {
            // check if request method is options (because opt it's send before any request
            // we should do nothing if req method is options
            response.setStatus(OK.value());
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstant.TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            // retrieve just the token by removing the bearer prefix
            String token = authorizationHeader.substring(SecurityConstant.TOKEN_PREFIX.length());
            String username = jwtProvider.getSubject(token);

            if (jwtProvider.isTokenValid(username, token)
                    && SecurityContextHolder.getContext().getAuthentication() == null)
            {
                List<GrantedAuthority> authorities = jwtProvider.getAuthorities(token);
                Authentication authentication = jwtProvider.getAuthentication(username, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
