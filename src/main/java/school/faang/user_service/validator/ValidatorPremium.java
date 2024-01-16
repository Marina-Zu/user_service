package school.faang.user_service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.premium.PremiumRepository;

@Service
@RequiredArgsConstructor
public class ValidatorPremium {
    private final PremiumRepository premiumRepository;

    public void validateUserId(long userId) {
        if (premiumRepository.existsByUserId(userId)) {
            throw new DataValidationException("Пользователь уже имеет премиум подписку");
        }
    }

    public void validateResponseStatus(PaymentResponse paymentResponse) {
        if (paymentResponse.getStatus() == PaymentStatus.FAILURE) {
            throw new DataValidationException("Ошибка платежа");
        }
    }

}