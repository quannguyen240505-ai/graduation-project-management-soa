package com.graduation.auth.repository;
import com.graduation.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long>{ Optional<User> findByUserCode(String userCode); boolean existsByUserCode(String userCode); boolean existsByEmail(String email); }
