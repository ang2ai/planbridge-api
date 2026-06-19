package com.planbridge.api.repository;

import com.planbridge.api.entity.PbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PbUserRepository extends JpaRepository<PbUser, String> {
    Optional<PbUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
