package vdtry06.springboot.ecommerce.configuration;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;

import vdtry06.springboot.ecommerce.dto.request.IntrospectRequest;
import vdtry06.springboot.ecommerce.service.AuthenticationService;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    private final AuthenticationService authenticationService;

    NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // nhận token
    @Override
    public Jwt decode(String token) throws JwtException {

        // kiểm tra tính hợp lệ của jwt
        try {
            var response = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build());
            if (!response.isValid()) throw new JwtException("Token is invalid");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        // khởi tạo nimbusJWTdecoder nếu chưa tồn tại, giải mã token
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
