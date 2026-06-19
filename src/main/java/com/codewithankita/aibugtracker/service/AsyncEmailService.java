package com.codewithankita.aibugtracker.service;

import com.codewithankita.aibugtracker.Model.Bug;
import com.codewithankita.aibugtracker.Model.TestScript;
import com.codewithankita.aibugtracker.Model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {

    private final EmailService emailService;

    @Async("taskExecutor")
    public void sendBugCreatedEmail(Bug bug, User assignedTo) {
        emailService.sendBugCreatedEmail(bug, assignedTo);
    }

    @Async("taskExecutor")
    public void sendBugUpdatedEmail(Bug bug, User assignedTo, User createdBy) {
        emailService.sendBugUpdatedEmail(bug, assignedTo, createdBy);
    }

    @Async("taskExecutor")
    public void sendStatusChangedEmail(Bug bug, User changedBy, String oldStatus) {
        emailService.sendStatusChangedEmail(bug, changedBy, oldStatus);
    }

    @Async("taskExecutor")
    public void sendBugWithdrawnEmail(Bug bug, User assignedTo) {
        emailService.sendBugWithdrawnEmail(bug, assignedTo);
    }

    @Async("taskExecutor")
    public void sendTestResultEmail(Bug bug, TestScript testScript, User createdBy) {
        emailService.sendTestResultEmail(bug, testScript, createdBy);
    }
}
