package com.neuroforge;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="project_tasks")
public class Task {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long workspaceId;
 @Column(nullable=false) public Long projectId;
 @Column(nullable=false,length=140) public String title;
 @Column(length=80) public String assignee="";
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) public Stage stage=Stage.TODO;
 public LocalDate dueDate;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected Task(){}
 public Task(Long workspaceId,Long projectId,String title,String assignee,LocalDate dueDate){
   this.workspaceId=workspaceId;this.projectId=projectId;this.title=title;this.assignee=assignee==null?"":assignee;this.dueDate=dueDate;
 }
 public enum Stage { TODO, IN_PROGRESS, DONE }
}
