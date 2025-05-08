package com.example.task.api.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    @NotBlank(message = "Title is required")
    private String Title;
    private String Description;
    @NotNull(message = "Status is required")
    private statusTypeEnum Status;
    private LocalDateTime DueDate;

}
