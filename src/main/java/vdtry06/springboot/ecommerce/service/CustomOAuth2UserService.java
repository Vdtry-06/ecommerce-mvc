package vdtry06.springboot.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.OAuthProvider;
import vdtry06.springboot.ecommerce.entity.Role;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.repository.RoleRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider oAuthProvider = OAuthProvider.valueOf(provider.toUpperCase());

        return oAuthProvider == OAuthProvider.GITHUB
                ? processGithubUser(userRequest, oAuth2User)
                : processGoogleUser(userRequest, oAuth2User);
    }

    @Transactional
    protected OAuth2User processGithubUser(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String login = oAuth2User.getAttribute("login");
        if (login == null) {
            throw new OAuth2AuthenticationException("Cannot get login from GitHub OAuth2 provider");
        }

        String email = oAuth2User.getAttribute("email");
        if (email == null || !email.contains("@gmail.com")) {
            email = login + "@github.com";
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == null) {
                user.setProvider(OAuthProvider.GITHUB);
            }
        } else {
            String username = login;
            if (userRepository.existsByUsername(username)) {
                username = username + "-github";
            }

            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setProvider(OAuthProvider.GITHUB);
            user.setEnabled(true);

            HashSet<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName("USER");
            if (userRole != null) {
                roles.add(userRole);
            }
            user.setRoles(roles);
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("Error creating user from GitHub: " + e.getMessage());
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "id");
    }

    @Transactional
    protected OAuth2User processGoogleUser(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Cannot get email from Google OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmailWithRoles(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == null) {
                user.setProvider(OAuthProvider.GOOGLE);
            }
            user = userRepository.findById(user.getId()).orElse(user);
        } else {
            user = new User();
            user.setEmail(email);
            user.setUsername(email.substring(0, email.indexOf("@")));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setProvider(OAuthProvider.GOOGLE);
            user.setEnabled(true);
            user.setRoles(new HashSet<>());

            Role userRole = roleRepository.findByName("USER");
            if (userRole != null) {
                user.getRoles().add(userRole);
            }
        }

        if (email.equals("tunhoipro0306@gmail.com")) {
            Role adminRole = roleRepository.findByName("ADMIN");
            if (adminRole != null) {
                user.getRoles().add(adminRole);
            }
        }

        userRepository.save(user);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(), "email");
    }
}