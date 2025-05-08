package com.example.task.api;

import com.example.task.api.Exception.TaskNotFoundException;
import com.example.task.api.Exception.UnauthorizedTaskAccessException;
import com.example.task.api.config.SecurityUtils;
import com.example.task.api.entities.Task;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task getTaskById(Long taskId) {
        String userId = SecurityUtils.getCurrentUserId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));
        if (!task.getOwnerId().equals(userId)) {
            throw new UnauthorizedTaskAccessException("You are not allowed to access this task");
        }
        return task;
    }

    public Task createTask(Task task) {
        task.setOwnerId(SecurityUtils.getCurrentUserId());
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task updated) {
        Task existing = getTaskById(taskId);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setDueDate(updated.getDueDate());
        return taskRepository.save(existing);
    }

    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        taskRepository.delete(task);
    }

    public List<Task> getMyTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByOwnerId(SecurityUtils.getCurrentUserId(), pageable);
    }
}
