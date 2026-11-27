package com.neuroforge;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="workspace_invitations")
public class Invitation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public Long workspaceId;
  @Column(nullable=false,unique=true,length=64) public String tokenHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) public Membership.Role role;
  @Column(nullable=false) public Instant expiresAt;
  public boolean used=false;
  protected Invitation() {}
  public Invitation(Long id,String hash,Membership.Role role){this.workspaceId=id;this.tokenHash=hash;this.role=role;this.expiresAt=Instant.now().plusSeconds(86400);}
}
