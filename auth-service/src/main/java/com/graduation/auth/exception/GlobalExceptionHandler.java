package com.graduation.auth.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler{
 @ExceptionHandler(ApiException.class) ResponseEntity<ApiError> api(ApiException ex,HttpServletRequest req){ var b=new ApiError(Instant.now(),ex.getStatus().value(),ex.getStatus().getReasonPhrase(),ex.getMessage(),req.getRequestURI(),null); return ResponseEntity.status(ex.getStatus()).body(b);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,HttpServletRequest req){ Map<String,String> e=new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(x->e.putIfAbsent(x.getField(),x.getDefaultMessage())); return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Bad Request","Dữ liệu gửi lên không hợp lệ",req.getRequestURI(),e));}
}
