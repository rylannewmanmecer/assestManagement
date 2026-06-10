package com.example.assestmanagement.repository; //  FIX

import com.example.assestmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String email);

    // 2. Validate password using PostgreSQL's native crypt verification
    @Query(value = "SELECT * FROM users WHERE email = :email AND password = crypt(:password, password)", nativeQuery = true)
    Optional<User> checkCredentials(@Param("email") String email, @Param("password") String password);
}