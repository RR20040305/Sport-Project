package ru.mirea.sport.error;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации данных", ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Запись не найдена", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleConflict(OptimisticLockingFailureException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Конфликт версий", "Запись была изменена другим пользователем. Пожалуйста, обновите данные.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleForbidden(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Доступ запрещен", "У вас недостаточно прав для этого действия");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка выполнения", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", "Обратитесь к администратору");
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}