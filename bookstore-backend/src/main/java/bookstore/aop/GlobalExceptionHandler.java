package bookstore.aop;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>>handleBusinessException(BusinessException ex) {
		log.warn("業務異常[代碼:{}]:{}", ex.getCode(), ex.getMessage());
		Map<String, Object> errorResponse = new HashMap<String, Object>();
		
		errorResponse.put("status", false);
		errorResponse.put("code", ex.getCode());
		errorResponse.put("message", ex.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		
	}

}
