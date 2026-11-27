package com.neuroforge;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/workspaces/{workspaceId}/projects")
public class ProjectController {
 private final ProjectRepository projects; private final WorkspaceAccess access;
 public ProjectController(ProjectRepository projects,WorkspaceAccess access){this.projects=projects;this.access=access;}
 @GetMapping public List<Project> list(@PathVariable Long workspaceId,@AuthenticationPrincipal Jwt jwt){
   access.membership(workspaceId,jwt);return projects.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
 }
 public record NewProject(@NotBlank @Size(max=120) String name,@Size(max=1000) String description){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Project create(@PathVariable Long workspaceId,@Valid @RequestBody NewProject request,@AuthenticationPrincipal Jwt jwt){
   access.require(workspaceId,jwt,Membership.Role.ADMIN,Membership.Role.PROJECT_MANAGER);
   return projects.save(new Project(workspaceId,request.name().trim(),request.description(),jwt.getSubject()));
 }
}
