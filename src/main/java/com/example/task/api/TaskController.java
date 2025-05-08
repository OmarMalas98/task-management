package com.example.task.api;

import com.example.task.api.entities.Task;
import com.example.task.api.entities.TaskDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return service.getMyTasks(page,size);
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable Long id) {
        return service.getTaskById(id);
    }

    @PostMapping
    public Task create(@Valid @RequestBody TaskDTO taskDto) {
        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setStatus(taskDto.getStatus());
        task.setDescription(taskDto.getDescription());
        task.setDueDate(taskDto.getDueDate());
        return service.createTask(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @RequestBody TaskDTO taskDto) {
        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setStatus(taskDto.getStatus());
        task.setDescription(taskDto.getDescription());
        task.setDueDate(taskDto.getDueDate());
        return service.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
