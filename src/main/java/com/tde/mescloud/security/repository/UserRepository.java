package com.tde.mescloud.security.repository;

import com.tde.mescloud.security.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    User findUserByUsername(String username);
    User findUserById(Long id);
}
