package com.example.dailyplanner.specification;

import com.example.dailyplanner.model.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null
                        ? null
                        : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Task> hasStatusName(String statusName) {
        return (root, query, cb) ->
                statusName == null || statusName.isBlank()
                        ? null
                        : cb.equal(cb.upper(root.get("status").get("name")), statusName.toUpperCase());
    }

    public static Specification<Task> hasPriority(Integer priority) {
        return (root, query, cb) ->
                priority == null
                        ? null
                        : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> titleContains(String title) {
        return (root, query, cb) ->
                title == null || title.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Task> deadlineFrom(LocalDate deadlineFrom) {
        return (root, query, cb) ->
                deadlineFrom == null
                        ? null
                        : cb.greaterThanOrEqualTo(root.get("deadline"), deadlineFrom);
    }

    public static Specification<Task> deadlineTo(LocalDate deadlineTo) {
        return (root, query, cb) ->
                deadlineTo == null
                        ? null
                        : cb.lessThanOrEqualTo(root.get("deadline"), deadlineTo);
    }
}
