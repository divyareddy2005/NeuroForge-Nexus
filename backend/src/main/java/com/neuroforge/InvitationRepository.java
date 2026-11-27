package com.neuroforge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvitationRepository extends JpaRepository<Invitation,Long> {
 Optional<Invitation> findByTokenHash(String tokenHash);
}
