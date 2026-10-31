package com.linkedin.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    @NotBlank(message = "Post content is required")
    @Size(max = 3000, message = "Post cannot exceed 3000 characters")
    private String content;
}

