package com.codewithankita.aibugtracker.repository;
import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.BugStatus;
import com.codewithankita.aibugtracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BugRepository extends JpaRepository<Bug, UUID> {
    List<Bug> findByAssignedTo(User assignedTo);
    List<Bug> findByCreatedBy(User createdBy);
    List<Bug> findByStatus(BugStatus status);
}
