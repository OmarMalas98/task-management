package com.example.task.api;

import com.example.task.api.entities.Task;
import com.example.task.api.entities.TaskDTO;
import com.example.task.api.entities.statusTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;  // Will be injected from Config

    @Autowired
    private ObjectMapper objectMapper;  // Spring Boot auto-configures this

    private final String userId = "mock-user-id";
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        Mockito.reset(taskService);

        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Desc");
        sampleTask.setDueDate(LocalDate.now().atStartOfDay());
        sampleTask.setStatus(statusTypeEnum.IN_PROGRESS);
        sampleTask.setOwnerId(userId);
    }

    private JwtRequestPostProcessor jwtWithUserId() {
        return jwt().jwt(builder -> builder.claim("sub", userId));
    }

    @Test
    void shouldReturnMyTasks() throws Exception {
        when(taskService.getMyTasks(0, 10)).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/api/tasks").with(jwtWithUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void shouldReturnSingleTask() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/tasks/1").with(jwtWithUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskDTO dto = new TaskDTO("New Task", "Description", statusTypeEnum.PENDING, LocalDate.now().atStartOfDay());

        Task created = new Task();
        created.setId(2L);
        created.setTitle(dto.getTitle());
        created.setDescription(dto.getDescription());
        created.setStatus(dto.getStatus());
        created.setDueDate(dto.getDueDate());
        created.setOwnerId(userId);

        when(taskService.createTask(any(Task.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .with(jwtWithUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskDTO dto = new TaskDTO("Updated", "Updated Desc", statusTypeEnum.PENDING, LocalDate.now().atStartOfDay());

        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle(dto.getTitle());
        updated.setDescription(dto.getDescription());
        updated.setStatus(dto.getStatus());
        updated.setDueDate(dto.getDueDate());
        updated.setOwnerId(userId);

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .with(jwtWithUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Mockito.doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1").with(jwtWithUserId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class Config {
        @Bean
        TaskService taskService() {
            return Mockito.mock(TaskService.class);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return Mockito.mock(JwtDecoder.class);
        }
    }


}