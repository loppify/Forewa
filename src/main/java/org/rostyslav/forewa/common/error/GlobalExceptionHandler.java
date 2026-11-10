package org.rostyslav.forewa.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountDoesNotExistException.class)
    public ProblemDetail handleAccountDoesNotExistException(AccountDoesNotExistException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Account does not exist");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", "ACCOUNT_DOES_NOT_EXIST");
        return problem;
    }

    @ExceptionHandler(IllegalTimeZoneException.class)
    public ProblemDetail handleIllegalTimeZoneException(IllegalTimeZoneException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Illegal time zone");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", "INVALID_TIMEZONE");
        return problem;
    }

    @ExceptionHandler(PathDoesNotExistException.class)
    public ProblemDetail handlePathDoesNotExistException(PathDoesNotExistException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Path does not exist");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", "PATH_DOES_NOT_EXIST");
        return problem;
    }
}
