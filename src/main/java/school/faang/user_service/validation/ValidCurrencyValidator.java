package school.faang.user_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import school.faang.user_service.validation.annotation.ValidTariffPlanPayment;

import java.util.List;

public class ValidCurrencyValidator implements ConstraintValidator<ValidTariffPlanPayment, String> {

    public static final List<String> VALID_VALUES = List.of("USD", "EUR");

    @Override
    public void initialize(ValidTariffPlanPayment constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return VALID_VALUES.contains(value.toUpperCase());
    }
}
