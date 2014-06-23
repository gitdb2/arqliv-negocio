package uy.edu.ort.arqliv.obligatorio.business.aop;

import java.util.Arrays;
import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uy.edu.ort.arqliv.obligatorio.dominio.UsageAudit;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IUsageAuditDAO;

/**
 * Bean de aspectos para llevar a cabo el profiling y auditoria de metodos
 * Se ejecuta antes de invocar un metodo y luego de invocarlo.
 * @author mauricio
 *
 */
@Aspect
public class UsageAuditAspect {

	private final Logger log = LoggerFactory.getLogger(UsageAuditAspect.class);
	
	@Autowired
	IUsageAuditDAO usageAuditDAO;
	
	/**
	 * Metodo ejecutado parra loguear acciones del usuario
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("within(uy.edu.ort.arqliv.obligatorio.business.services.*)")
	public Object logUserAction(ProceedingJoinPoint joinPoint) throws Throwable {
		UsageAudit usageAudit = initializeUsageAudit(joinPoint);
		try {
			long startTime = System.nanoTime();
			usageAudit.setActionDate(new Date());
			Object result = joinPoint.proceed();
			long endTime = System.nanoTime();
			usageAudit.setActionNanoSeconds(endTime - startTime);
			usageAuditDAO.store(usageAudit);
			return result; 
		} catch (IllegalArgumentException e) { 
			log.error("Illegal argument " 
					+ Arrays.toString(joinPoint.getArgs()) + " e " 
					+ joinPoint.getSignature().getName() + "()", e); 
			throw e; 
		}
	}

	/**
	 * inicializa el objeto que guarda las stats
	 * seteando los parametros salvo la hora de fin
	 * que se setea depues que hace el return el metodo
	 * @param joinPoint
	 * @return
	 */
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
