package com.phyo.food_management_system.security;


import com.phyo.food_management_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DynamoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DynamoUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        var user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with the given email: " + email));

        return new CustomUserDetails(user);
    }
}
