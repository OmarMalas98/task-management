package com.example.task.api;

import com.example.task.api.Exception.TaskNotFoundException;
import com.example.task.api.Exception.UnauthorizedTaskAccessException;
import com.example.task.api.config.SecurityUtils;
import com.example.task.api.entities.Task;
import com.example.task.api.entities.statusTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final String mockUserId = "mock-user-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenOwnerMatches() {
        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);

            Task task = new Task();
            task.setId(1L);
            task.setOwnerId(mockUserId);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            Task result = taskService.getTaskById(1L);

            assertEquals(task, result);
        }
    }

    @Test
    void getTaskById_ShouldThrowTaskNotFoundException_WhenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);
            assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(999L));
        }
    }

    @Test
    void getTaskById_ShouldThrowUnauthorizedTaskAccessException_WhenOwnerMismatch() {
        Task task = new Task();
        task.setId(2L);
        task.setOwnerId("another-user");

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);
            assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.getTaskById(2L));
        }
    }

    @Test
    void createTask_ShouldAssignOwnerAndSave() {
        Task input = new Task();
        input.setTitle("New Task");

        Task saved = new Task();
        saved.setId(1L);
        saved.setTitle("New Task");
        saved.setOwnerId(mockUserId);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);
            Task result = taskService.createTask(input);

            assertEquals(mockUserId, result.getOwnerId());
            assertEquals("New Task", result.getTitle());
        }
    }

    @Test
    void updateTask_ShouldUpdateAndSaveTask() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setOwnerId(mockUserId);

        Task updated = new Task();
        updated.setTitle("Updated");
        updated.setDescription("Updated desc");
        updated.setStatus(statusTypeEnum.IN_PROGRESS);
        updated.setDueDate(LocalDate.now().atStartOfDay());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);
            Task result = taskService.updateTask(1L, updated);

            assertEquals("Updated", result.getTitle());
            assertEquals("Updated desc", result.getDescription());
            assertEquals(statusTypeEnum.IN_PROGRESS, result.getStatus());
        }
    }

    @Test
    void deleteTask_ShouldRemoveTask() {
        Task task = new Task();
        task.setId(1L);
        task.setOwnerId(mockUserId);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);

            assertDoesNotThrow(() -> taskService.deleteTask(1L));
            verify(taskRepository).delete(task);
        }
    }

    @Test
    void getMyTasks_ShouldReturnPaginatedTasks() {
        List<Task> tasks = List.of(new Task(), new Task());
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findByOwnerId(mockUserId, pageable)).thenReturn(tasks);

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);
            List<Task> result = taskService.getMyTasks(0, 10);

            assertEquals(2, result.size());
        }
    }
}
