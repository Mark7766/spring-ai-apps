package com.sandy.prototype.design;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeneratedDesignRepository extends JpaRepository<GeneratedDesign, Long> {
    Optional<GeneratedDesign> findByUuid(String uuid);
}