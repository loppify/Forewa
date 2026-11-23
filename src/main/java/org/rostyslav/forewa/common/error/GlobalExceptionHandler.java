package org.rostyslav.forewa.common.error;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalTimeZone.class)
    public ProblemDetail handleIllegalTimeZoneException(IllegalTimeZone e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Illegal time zone");
        problem.setDetail("The provided time zone is invalid.");
        problem.setProperty("code", "INVALID_TIMEZONE");
        return problem;
    }

    @ExceptionHandler(NodeTypeViolation.class)
    public ProblemDetail handleNodeTypeViolation(NodeTypeViolation e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Node type violation");
        problem.setDetail("The selected node type is not valid for this operation.");
        problem.setProperty("code", "INVALID_NODE_TYPE");
        return problem;
    }


    @ExceptionHandler(NodeHierarchyCycle.class)
    public ProblemDetail handleNodeHierarchyRecursion(NodeHierarchyCycle e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Node hierarchy recursion");
        problem.setDetail("The requested hierarchy would create a cycle.");
        problem.setProperty("code", "NODE_HIERARCHY_CYCLE");
        return problem;

    }

    @ExceptionHandler(BadRequest.class)
    public ProblemDetail handleInvalidRequest(BadRequest e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setDetail("The request contains invalid data.");
        problem.setProperty("code", "INVALID_REQUEST");
        return problem;
    }

    @ExceptionHandler(ActiveGoalAlreadyExists.class)
    public ProblemDetail handleActiveGoalAlreadyExists(ActiveGoalAlreadyExists e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Active goal already exists");
        problem.setDetail("An active goal already exists.");
        problem.setProperty("code", "ACTIVE_GOAL_ALREADY_EXISTS");
        return problem;
    }


    @ExceptionHandler(InvalidGoalTransition.class)
    public ProblemDetail handleInvalidGoalTransition(InvalidGoalTransition e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Invalid goal");
        problem.setDetail("The goal cannot be changed to the requested state.");
        problem.setProperty("code", "INVALID_GOAL_TRANSITION");
        return problem;
    }

    @ExceptionHandler(SkillNotTrainable.class)
    public ProblemDetail handleSkillNotTrainable(SkillNotTrainable e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Skill is not trainable");
        problem.setDetail("This skill cannot be trained.");
        problem.setProperty("code", "SKILL_NOT_TRAINABLE");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintException &&
                    "one_active_goal_per_skill_constraint".equals(constraintException.getConstraintName())) {
                ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                problem.setTitle("Active goal already exists");
                problem.setDetail("An active goal already exists.");
                problem.setProperty("code", "ACTIVE_GOAL_ALREADY_EXISTS");
                return problem;
            }
            cause = cause.getCause();
        }
        assert e != null;
        throw e;

    }

    @ExceptionHandler(InvalidSkillHierarchy.class)
    public ProblemDetail handleInvalidSkillHierarchy(InvalidSkillHierarchy e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Invalid skill hierarchy");
        problem.setDetail("The requested skill hierarchy is invalid.");
        problem.setProperty("code", "INVALID_SKILL_HIERARCHY");
        return problem;
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFound e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource not found");
        problem.setDetail("The requested resource could not be found.");
        problem.setProperty("code", "RESOURCE_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setDetail("The request contains invalid data.");
        problem.setProperty("code", "INVALID_REQUEST");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setDetail("The request contains invalid data.");
        problem.setProperty("code", "INVALID_REQUEST");
        return problem;
    }
}
