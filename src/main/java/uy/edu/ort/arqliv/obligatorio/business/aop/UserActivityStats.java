package uy.edu.ort.arqliv.obligatorio.business.aop;

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserActivityStats implements MethodInterceptor {

	private final Logger log = LoggerFactory.getLogger(UserActivityStats.class);
	
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		log.info("Method name : " + methodInvocation.getMethod().getName());
		log.info("Method arguments : " + Arrays.toString(methodInvocation.getArguments()));
		try {
			// proceed to original method call
			Object result = methodInvocation.proceed();
			// same with AfterReturningAdvice
			log.info("HijackAroundMethod : Before after hijacked!");
			return result;
		} catch (IllegalArgumentException e) {
			// same with ThrowsAdvice
			log.info("HijackAroundMethod : Throw exception hijacked!");
			throw e;
		}
	}

}
