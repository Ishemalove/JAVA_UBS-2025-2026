package rw.utility.billing.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import rw.utility.billing.enums.AccountStatus;
import rw.utility.billing.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = users.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .disabled(user.getStatus() != AccountStatus.ACTIVE || !user.isEmailVerified())
                .authorities(user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList())
                .build();
    }
}
