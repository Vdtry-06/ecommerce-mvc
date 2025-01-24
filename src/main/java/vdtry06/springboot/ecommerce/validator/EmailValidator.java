package vdtry06.springboot.ecommerce.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Validator để kiểm tra tính hợp lệ của email
public class EmailValidator implements ConstraintValidator<EmailConstraint, String> {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.(com|org|net)$";

    @Override
    public void initialize(EmailConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return false; // Trường hợp email null
        }

        // Kiểm tra email có thỏa mãn biểu thức chính quy không
        return Pattern.matches(EMAIL_REGEX, email);
    }
}
