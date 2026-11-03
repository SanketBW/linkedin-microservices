package com.linkedin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse {
    private List<UserResponse> users;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
