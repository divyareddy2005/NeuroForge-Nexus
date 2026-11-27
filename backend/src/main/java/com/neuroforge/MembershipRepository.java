package com.neuroforge;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MembershipRepository extends JpaRepository<Membership,Long> {
  List<Membership> findByUserId(String userId);
  List<Membership> findByWorkspaceId(Long workspaceId);
  Optional<Membership> findByWorkspaceIdAndUserId(Long workspaceId,String userId);
}
