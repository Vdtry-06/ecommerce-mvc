package vdtry06.springboot.ecommerce.config.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String provider = "";
        if (authentication instanceof OAuth2AuthenticationToken) {
            provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        }

        response.sendRedirect("http://localhost:3000/login?oauth2=true&provider=" + provider);
    }
}