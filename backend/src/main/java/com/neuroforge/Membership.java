package com.neuroforge;
import jakarta.persistence.*;
@Entity @Table(name="workspace_members",uniqueConstraints=@UniqueConstraint(columnNames={"workspace_id","user_id"}))
public class Membership {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(name="workspace_id",nullable=false) public Long workspaceId;
  @Column(name="user_id",nullable=false) public String userId;
  @Column(nullable=false,length=80) public String username;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) public Role role;
  protected Membership() {}
  public Membership(Long workspaceId,String userId,String username,Role role){this.workspaceId=workspaceId;this.userId=userId;this.username=username;this.role=role;}
  public enum Role { ADMIN, PROJECT_MANAGER, DEVELOPER, TESTER, DEVOPS_ENGINEER }
}
