package com.hidrosense.project.waterquality.repository;

import com.hidrosense.project.waterquality.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
