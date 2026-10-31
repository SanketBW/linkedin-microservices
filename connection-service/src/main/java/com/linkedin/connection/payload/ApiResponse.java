package com.linkedin.connection.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;
}


