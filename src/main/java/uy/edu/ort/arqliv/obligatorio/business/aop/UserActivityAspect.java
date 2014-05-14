package uy.edu.ort.arqliv.obligatorio.business.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class UserActivityAspect {

	private final Logger log = LoggerFactory.getLogger(UserActivityAspect.class);
	
	@Around("within(uy.edu.ort.arqliv.obligatorio.business.services.*)")
	public Object logUserAction(ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("The method " + joinPoint.getSignature().getName() 
				+ "() begins with " + Arrays.toString(joinPoint.getArgs())); 
		try { 
			Object result = joinPoint.proceed(); 
			log.info("The method " + joinPoint.getSignature().getName() 
					+ "() ends with " + result); 
			return result; 
		} catch (IllegalArgumentException e) { 
			log.error("Illegal argument " 
					+ Arrays.toString(joinPoint.getArgs()) + " in " 
					+ joinPoint.getSignature().getName() + "()"); 
			throw e; 
		}
	}

}
