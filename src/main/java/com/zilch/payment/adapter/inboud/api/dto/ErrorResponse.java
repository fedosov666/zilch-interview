package com.zilch.payment.adapter.inboud.api.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        List<String> errors
) {}
