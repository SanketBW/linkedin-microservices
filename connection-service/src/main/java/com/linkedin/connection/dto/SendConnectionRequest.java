package com.linkedin.connection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendConnectionRequest {

    @NotNull(message = "Receiver ID is required")
    @Positive(message = "Receiver ID must be greater than zero")
    private Long receiverId;
}

