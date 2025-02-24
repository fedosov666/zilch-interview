package com.zilch.payment.adapter.inboud.api;

import com.zilch.payment.adapter.inboud.api.dto.InitializePaymentRequest;
import com.zilch.payment.adapter.inboud.api.dto.PaymentDetailsResponse;
import com.zilch.payment.application.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDetailsResponse initializePayment(@Valid @RequestBody InitializePaymentRequest initializePaymentRequest) {
        return PaymentDetailsResponse.fromPayment(
                paymentService.initializePayment(initializePaymentRequest.toFuturePaymentDetails())
        );
    }

    @GetMapping
    @RequestMapping("/{paymentId}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentDetailsResponse getPayment(@PathVariable String paymentId) {
        return PaymentDetailsResponse.fromPayment(paymentService.retrieveById(paymentId));
    }

}
