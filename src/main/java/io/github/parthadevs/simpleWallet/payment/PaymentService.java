package io.github.parthadevs.simpleWallet.payment;

import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.payment.dto.CreatePaymentMethodRequest;
import io.github.parthadevs.simpleWallet.payment.dto.PaymentMethodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Page<PaymentMethodResponse> listPaymentMethods(boolean isActive, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("name").ascending());

        Page<PaymentMethod> page1 = paymentRepository.findByIsActive(isActive, pageable);

        List<PaymentMethodResponse> content = page1.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, page1.getTotalElements());
    }

    public PaymentMethodResponse getPaymentMethodById(Long id) {
        PaymentMethod method = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", "id", id));
        return mapToResponse(method);
    }

    public PaymentMethodResponse getPaymentMethodBySlug(String slug) {
        PaymentMethod method = paymentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", "slug", slug));
        return mapToResponse(method);
    }

    @Transactional
    public PaymentMethodResponse createPaymentMethod(CreatePaymentMethodRequest request) {
        if (paymentRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Payment method with slug '" + request.getSlug() + "' already exists.");
        }

        PaymentMethod method = PaymentMethod.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .type(request.getType())
                .instructions(request.getInstructions())
                .minimumAmount(request.getMinimumAmount())
                .maximumAmount(request.getMaximumAmount())
                .currency(request.getCurrency())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return mapToResponse(paymentRepository.save(method));
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, CreatePaymentMethodRequest request) {
        PaymentMethod method = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            method.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()
                && !request.getSlug().equals(method.getSlug())
                && paymentRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Payment method with slug '" + request.getSlug() + "' already exists.");
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            method.setSlug(request.getSlug());
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            method.setType(request.getType());
        }
        if (request.getInstructions() != null) {
            method.setInstructions(request.getInstructions());
        }
        if (request.getMinimumAmount() != null) {
            method.setMinimumAmount(request.getMinimumAmount());
        }
        if (request.getMaximumAmount() != null) {
            method.setMaximumAmount(request.getMaximumAmount());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            method.setCurrency(request.getCurrency());
        }
        if (request.getIsActive() != null) {
            method.setActive(request.getIsActive());
        }

        return mapToResponse(paymentRepository.save(method));
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment method", "id", id);
        }
        paymentRepository.deleteById(id);
    }

    private PaymentMethodResponse mapToResponse(PaymentMethod method) {
        return PaymentMethodResponse.builder()
                .id(method.getId())
                .name(method.getName())
                .slug(method.getSlug())
                .type(method.getType())
                .instructions(method.getInstructions())
                .minimumAmount(method.getMinimumAmount())
                .maximumAmount(method.getMaximumAmount())
                .currency(method.getCurrency())
                .isActive(method.isActive())
                .createdAt(method.getCreatedAt())
                .build();
    }
}
