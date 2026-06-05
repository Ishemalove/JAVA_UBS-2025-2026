package rw.utility.billing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rw.utility.billing.repository.BlacklistedTokenRepository;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserDetailsService detailsService;
    private final BlacklistedTokenRepository blacklistedTokens;

    public JwtAuthFilter(JwtService jwtService, AppUserDetailsService detailsService, BlacklistedTokenRepository blacklistedTokens) {
        this.jwtService = jwtService;
        this.detailsService = detailsService;
        this.blacklistedTokens = blacklistedTokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (!blacklistedTokens.existsByToken(token)) {
                    String email = jwtService.subject(token);
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var details = detailsService.loadUserByUsername(email);
                        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
