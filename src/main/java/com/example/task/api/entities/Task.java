package com.example.task.api.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Nullable
    private String description;
    @Enumerated(EnumType.STRING)
    private statusTypeEnum status;
    @Nullable
    private LocalDateTime DueDate;
    private String ownerId;
}
