package vn.edu.student.security;

import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;
    public DatabaseUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));
        return User.withUsername(account.getUsername())
            .password(account.getPassword())
            .authorities("ROLE_" + account.getRole())
            .disabled(!account.isEnabled() || !"ACTIVE".equals(account.getStatus()))
            .build();
    }
}
