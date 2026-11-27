package com.neuroforge;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="workspaces")
public class Workspace {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false,length=120) public String name;
  @Column(nullable=false) public String createdBy;
  @Column(nullable=false) public Instant createdAt=Instant.now();
  protected Workspace() {}
  public Workspace(String name,String creator){this.name=name;this.createdBy=creator;}
}
