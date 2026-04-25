package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
