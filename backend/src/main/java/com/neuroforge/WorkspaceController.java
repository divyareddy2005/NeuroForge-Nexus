package com.neuroforge;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/workspaces")
public class WorkspaceController {
  private final WorkspaceRepository workspaces;
  private final MembershipRepository members;
  private final InvitationRepository invitations;
  private final WorkspaceAccess access;
  private final SecureRandom random=new SecureRandom();
  public WorkspaceController(WorkspaceRepository w,MembershipRepository m,InvitationRepository i,WorkspaceAccess a){workspaces=w;members=m;invitations=i;access=a;}
  public record WorkspaceView(Long id,String name,String role) {}
  public record CreateWorkspace(@NotBlank @Size(max=120) String name) {}
  public record InviteRequest(Membership.Role role) {}
  public record InviteResult(String code,String role,String expiresIn) {}
  public record JoinRequest(@NotBlank String code) {}
  public record MemberView(String username,String role) {}
  private String username(Jwt jwt){String n=jwt.getClaimAsString("preferred_username");return n==null?jwt.getSubject():n;}
  @GetMapping public List<WorkspaceView> mine(@AuthenticationPrincipal Jwt jwt){
    return members.findByUserId(jwt.getSubject()).stream().map(m->{
      var w=workspaces.findById(m.workspaceId).orElseThrow();return new WorkspaceView(w.id,w.name,m.role.name());
    }).toList();
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
  public WorkspaceView create(@Valid @RequestBody CreateWorkspace request,@AuthenticationPrincipal Jwt jwt){
    Workspace w=workspaces.save(new Workspace(request.name().trim(),jwt.getSubject()));
    members.save(new Membership(w.id,jwt.getSubject(),username(jwt),Membership.Role.ADMIN));
    return new WorkspaceView(w.id,w.name,"ADMIN");
  }
  @GetMapping("/{id}/members") public List<MemberView> listMembers(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){
    access.membership(id,jwt);
    return members.findByWorkspaceId(id).stream().map(m->new MemberView(m.username,m.role.name())).toList();
  }
  private String hash(String value){
    try {byte[] result=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(result);}
    catch(Exception ex){throw new IllegalStateException(ex);}
  }
  @PostMapping("/{id}/invites") @ResponseStatus(HttpStatus.CREATED)
  public InviteResult invite(@PathVariable Long id,@Valid @RequestBody InviteRequest request,@AuthenticationPrincipal Jwt jwt){
    access.require(id,jwt,Membership.Role.ADMIN);
    if(request.role()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Role is required");
    byte[] b=new byte[24];random.nextBytes(b);
    String code=Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    invitations.save(new Invitation(id,hash(code),request.role()));
    return new InviteResult(code,request.role().name(),"24 hours");
  }
  @PostMapping("/join") @Transactional
  public WorkspaceView join(@Valid @RequestBody JoinRequest request,@AuthenticationPrincipal Jwt jwt){
    Invitation invite=invitations.findByTokenHash(hash(request.code().trim()))
      .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid invitation"));
    // Row-locking is needed before using this for concurrent real-world registration.
    if(invite.used||invite.expiresAt.isBefore(java.time.Instant.now()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invitation expired or already used");
    if(members.findByWorkspaceIdAndUserId(invite.workspaceId,jwt.getSubject()).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT,"You already belong to this workspace");
    members.save(new Membership(invite.workspaceId,jwt.getSubject(),username(jwt),invite.role));
    invite.used=true;invitations.save(invite);
    Workspace w=workspaces.findById(invite.workspaceId).orElseThrow();
    return new WorkspaceView(w.id,w.name,invite.role.name());
  }
}
