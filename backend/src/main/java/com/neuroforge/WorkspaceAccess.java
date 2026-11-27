package com.neuroforge;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class WorkspaceAccess {
 private final MembershipRepository members;
 public WorkspaceAccess(MembershipRepository members){this.members=members;}
 public Membership membership(Long workspaceId,Jwt jwt){
   return members.findByWorkspaceIdAndUserId(workspaceId,jwt.getSubject())
      .orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Not a member of this workspace"));
 }
 public Membership require(Long workspaceId,Jwt jwt,Membership.Role... roles){
   Membership member=membership(workspaceId,jwt);
   for(var role:roles) if(member.role==role) return member;
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Your workspace role does not permit this action");
 }
}
