package com.neuroforge;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="projects")
public class Project {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=120) public String name;
 @Column(length=1000) public String description;
 @Column(nullable=false,length=32) public String status="PLANNED";
 @Column(nullable=false) public String createdBy;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 // Nullable only to retain projects from the previous single-company starter.
 // All new projects must have a workspaceId.
 @Column(name="workspace_id") public Long workspaceId;
 protected Project() {}
 public Project(Long workspaceId,String name,String description,String createdBy){
   this.workspaceId=workspaceId;this.name=name;this.description=description;this.createdBy=createdBy;
 }
}
