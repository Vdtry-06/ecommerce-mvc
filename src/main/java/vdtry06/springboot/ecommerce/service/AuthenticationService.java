package vdtry06.springboot.ecommerce.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import vdtry06.springboot.ecommerce.entity.InvalidatedToken;
import vdtry06.springboot.ecommerce.repository.InvalidatedTokenRepository;
import vdtry06.springboot.ecommerce.dto.request.IntrospectRequest;
import vdtry06.springboot.ecommerce.entity.Role;
import vdtry06.springboot.ecommerce.dto.request.LoginUserRequest;
import vdtry06.springboot.ecommerce.dto.request.LogoutRequest;
import vdtry06.springboot.ecommerce.dto.request.RefreshRequest;
import vdtry06.springboot.ecommerce.dto.response.AuthenticationResponse;
import vdtry06.springboot.ecommerce.dto.response.IntrospectResponse;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    CartService cartService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        boolean isValid = true;
        Long expiryTime = null;

        try {
            SignedJWT signedJWT = verifyToken(token, false);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime != null) {
                long remainingTime = expirationTime.getTime() - System.currentTimeMillis();
                expiryTime = remainingTime > 0 ? remainingTime : 0;
            }

        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
            log.error("Token verification failed: {}", e.getMessage());
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .expiryTime(expiryTime)
                .build();
    }

    public void logout(Long userId, LogoutRequest request) throws ParseException, JOSEException {
        cartService.syncCartToDatabase(userId);
        SecurityContextHolder.clearContext();
        try {
            var signToken = verifyToken(request.getToken(), true);

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(signToken.getJWTClaimsSet().getJWTID())
                    .expiryTime(signToken.getJWTClaimsSet().getExpirationTime().getTime())
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(signedJWT.getJWTClaimsSet().getJWTID())
                .expiryTime(signedJWT.getJWTClaimsSet().getExpirationTime().getTime())
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        var token = generateToken(user);
        long remainingTime = signedJWT.getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();

        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(remainingTime)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        long expiryTime = (isRefresh)
                ? signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli()
                : signedJWT.getJWTClaimsSet().getExpirationTime().getTime();

        var verified = signedJWT.verify(verifier); // true | false: neu token khong bi thay doi true
        if(!(verified && new Date(expiryTime).after(new Date()))) {
            log.error("Token verification failed: invalid signature or expired token");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            log.info("Token has been invalidated");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }

    public AuthenticationResponse login(LoginUserRequest request) throws ParseException {
        log.info("SignKey: {}", SIGNER_KEY);

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        var token = generateToken(user);

        long remainingTime = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();

        String nameRole = user.getRole() != null ? user.getRole().getName() : "USER";

        return AuthenticationResponse.builder()
                .token(token)
                .nameRole(nameRole)
                .expiryTime(remainingTime)
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("vdtry06.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        Role role = user.getRole();
        if (role != null) {
            stringJoiner.add("ROLE_" + role.getName());
            if (!CollectionUtils.isEmpty(role.getPermissions())) {
                role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }
        return stringJoiner.toString();
    }
}