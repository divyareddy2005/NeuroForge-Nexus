package com.neuroforge;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
public class TaskController {
 private final TaskRepository tasks;private final ProjectRepository projects;private final WorkspaceAccess access;
 public TaskController(TaskRepository t,ProjectRepository p,WorkspaceAccess a){tasks=t;projects=p;access=a;}
 private Project project(Long w,Long p){
   Project item=projects.findById(p).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
   if(!w.equals(item.workspaceId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
   return item;
 }
 public record NewTask(@NotBlank @Size(max=140) String title,@Size(max=80) String assignee,LocalDate dueDate){}
 public record MoveTask(@NotNull Task.Stage stage){}
 @GetMapping public List<Task> list(@PathVariable Long workspaceId,@PathVariable Long projectId,@AuthenticationPrincipal Jwt jwt){
   access.membership(workspaceId,jwt);project(workspaceId,projectId);
   return tasks.findByWorkspaceIdAndProjectIdOrderByCreatedAtDesc(workspaceId,projectId);
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Task create(@PathVariable Long workspaceId,@PathVariable Long projectId,@Valid @RequestBody NewTask req,@AuthenticationPrincipal Jwt jwt){
   access.require(workspaceId,jwt,Membership.Role.ADMIN,Membership.Role.PROJECT_MANAGER);
   project(workspaceId,projectId);
   return tasks.save(new Task(workspaceId,projectId,req.title().trim(),req.assignee(),req.dueDate()));
 }
 @PatchMapping("/{taskId}/stage")
 public Task move(@PathVariable Long workspaceId,@PathVariable Long projectId,@PathVariable Long taskId,@Valid @RequestBody MoveTask req,@AuthenticationPrincipal Jwt jwt){
   access.require(workspaceId,jwt,Membership.Role.ADMIN,Membership.Role.PROJECT_MANAGER,Membership.Role.DEVELOPER,Membership.Role.TESTER);
   project(workspaceId,projectId);
   Task task=tasks.findById(taskId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
   if(!workspaceId.equals(task.workspaceId)||!projectId.equals(task.projectId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
   task.stage=req.stage();return tasks.save(task);
 }
}
