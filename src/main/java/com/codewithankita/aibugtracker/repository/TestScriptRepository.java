package com.codewithankita.aibugtracker.repository;
import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.TestScriptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestScriptRepository extends JpaRepository<TestScript, UUID> {
    Optional<TestScript> findByBug(Bug bug);
    List<TestScript> findByStatus(TestScriptStatus status);
}