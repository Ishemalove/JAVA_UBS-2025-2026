package rw.utility.billing.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rw.utility.billing.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final ObjectMapper mapper;

    public PasswordChangeRequiredFilter(UserRepository users, ObjectMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String path = request.getRequestURI();
        if (auth != null && auth.isAuthenticated() && !isAllowedWhileChangingPassword(path)) {
            var user = users.findByEmail(auth.getName());
            if (user.isPresent() && user.get().isMustChangePassword()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getWriter(), Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 403,
                        "error", "Forbidden",
                        "message", "You must change your temporary password before accessing this endpoint."
                ));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isAllowedWhileChangingPassword(String path) {
        return path.startsWith("/api/auth/change-password")
                || path.startsWith("/api/auth/logout")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
