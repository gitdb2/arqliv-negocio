package uy.edu.ort.arqliv.obligatorio.business.aop;

import java.util.Arrays;
import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.dominio.UsageAudit;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IUsageAuditDAO;

@Aspect
public class UsageAuditAspect {

	private final Logger log = LoggerFactory.getLogger(UsageAuditAspect.class);
	
	@Around("within(uy.edu.ort.arqliv.obligatorio.business.services.*)")
	public Object logUserAction(ProceedingJoinPoint joinPoint) throws Throwable {
		UsageAudit usageAudit = initializeUsageAudit(joinPoint);
		try {
			usageAudit.setActionStartTime(new Date());
			Object result = joinPoint.proceed();
			usageAudit.setActionEndTime(new Date());
			IUsageAuditDAO usageAuditDAO = (IUsageAuditDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.UsageAuditDao);
			usageAuditDAO.store(usageAudit);
			return result; 
		} catch (IllegalArgumentException e) { 
			log.error("Illegal argument " 
					+ Arrays.toString(joinPoint.getArgs()) + " e " 
					+ joinPoint.getSignature().getName() + "()", e); 
			throw e; 
		}
	}

	private UsageAudit initializeUsageAudit(ProceedingJoinPoint joinPoint) {
		UsageAudit usageAudit = new UsageAudit();
		try {
			usageAudit.setUser((String)joinPoint.getArgs()[0]);
			usageAudit.setService(joinPoint.getTarget().getClass().getSimpleName());
			usageAudit.setAction(joinPoint.getSignature().getName());
			usageAudit.setParameters(Arrays.toString(joinPoint.getArgs()));
		} catch (Exception e) {
			log.error("No se pudieron obtener todos los datos para Auditoria", e);
		}
		return usageAudit;
	}

}
