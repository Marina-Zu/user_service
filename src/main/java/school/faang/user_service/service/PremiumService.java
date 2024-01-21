package school.faang.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.client.PaymentServiceClient;
import school.faang.user_service.dto.payment.Currency;
import school.faang.user_service.dto.payment.PaymentRequest;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.dto.premium.PremiumEvent;
import school.faang.user_service.dto.premium.PremiumPeriod;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.mapper.PremiumMapper;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.publisher.PremiumEventPublisher;
import school.faang.user_service.repository.premium.PremiumRepository;
import school.faang.user_service.validator.PremiumValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PremiumService {
    private final PremiumRepository premiumRepository;
    private final PremiumMapper premiumMapper;
    private final PaymentServiceClient paymentServiceClient;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PremiumValidator premiumValidator;
    private final PremiumEventPublisher premiumEventPublisher;

    private static long paymentNumber;

    public PremiumDto buyPremium(long userId, PremiumPeriod premiumPeriod) {
        premiumValidator.validateUserId(userId);

        PaymentRequest paymentRequest = createPaymentRequest(premiumPeriod);

        PaymentResponse paymentResponse = paymentServiceClient.sendPayment(paymentRequest);

        premiumValidator.validateResponseStatus(paymentResponse);

        Premium premium = new Premium();
        premium.setUser(userMapper.toEntity(userService.getUser(userId)));
        premium.setStartDate(LocalDateTime.now());
        premium.setEndDate(LocalDateTime.now().plusDays(premiumPeriod.getDays()));

        premiumRepository.save(premium);
        premiumEventPublisher.publish(new PremiumEvent(userId, premium.getId(),premiumPeriod, LocalDateTime.now()));

        return premiumMapper.toDto(premium);
    }

    //Нужно сделать нормальную генерацию номера платежа
    public PaymentRequest createPaymentRequest(PremiumPeriod premiumPeriod) {
        return new PaymentRequest(++paymentNumber, BigDecimal.valueOf(premiumPeriod.getPrice()), Currency.USD);
    }
}
