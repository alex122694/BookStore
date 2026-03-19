package bookstore.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ServiceLogAspect {

	private static final Logger log = LoggerFactory.getLogger(ServiceLogAspect.class);

	@Pointcut("execution(* bookstore.service..*(..))")
	public void serviceLayer() {
	}

	private String getCurrentUsername() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();

			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				Object userId = request.getAttribute("userId");
				if (userId != null) {
					return "MemberID:" + userId.toString();
				}
				Object sessionUser = request.getSession().getAttribute("user");
				if (sessionUser != null) {
					return "SessionUser" + sessionUser.toString();
				}
			}
		} catch (Exception e) {

		}
		return "Guest";
	}

	private String formatArgs(Object[] args) {
		if (args == null || args.length == 0)
			return "[]";
		return Arrays.stream(args).map(arg -> {
			if (arg instanceof MultipartFile) {
				return "File" + ((MultipartFile) arg).getOriginalFilename();
			}
			return arg;
		}).toList().toString();
	}

	@Around("serviceLayer()")
	public Object logExecutionDetails(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();

		String username = getCurrentUsername();
		String className = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();
		String targetMethod = className + "." + methodName;
		Object[] args = joinPoint.getArgs();
		String argString = formatArgs(args);

		log.info("使用者：[{}] 呼叫方法:[{}] 參數:[{}]",username, targetMethod, argString);
		Object result;

		try {
			result = joinPoint.proceed();

			long executionTime = System.currentTimeMillis() - startTime;
			log.info("方法:[{}] 執行成功 | 耗時:{} ms | ", targetMethod, executionTime);
		} catch (Exception ex) {
			long executionTime = System.currentTimeMillis() - startTime;

			log.warn("方法:{} 執行失敗 | 耗時:{}ms | 異常訊息:{}", targetMethod, executionTime, ex.getMessage());

			throw ex;
		}
		return result;
	}

}
