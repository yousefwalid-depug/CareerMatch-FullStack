package com.careermatch.careermatch_backend.repository;
import com.careermatch.careermatch_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {}
