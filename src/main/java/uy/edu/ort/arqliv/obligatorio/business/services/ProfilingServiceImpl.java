package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.ProfilingService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Pair;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IUsageAuditDAO;

public class ProfilingServiceImpl implements ProfilingService {

	private final Logger log = LoggerFactory.getLogger(ProfilingService.class);
	
	@Override
	public List<Pair<String, Double>> avgServiceTime(String user, Date forDate) throws CustomServiceException {
		try {
			IUsageAuditDAO usageAuditDAO = (IUsageAuditDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.UsageAuditDao);
			return usageAuditDAO.avgServiceTime(forDate);
		} catch (Exception e) {
			log.error("Error al obtener el promedio de tiempo de servicios", e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public List<Pair<String, Long>> minServiceTime(String user, Date forDate) throws CustomServiceException {
		try {
			IUsageAuditDAO usageAuditDAO = (IUsageAuditDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.UsageAuditDao);
			return usageAuditDAO.minServiceTime(forDate);
		} catch (Exception e) {
			log.error("Error al obtener el minimo tiempo de servicios", e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public List<Pair<String, Long>> maxServiceTime(String user, Date forDate) throws CustomServiceException {
		try {
			IUsageAuditDAO usageAuditDAO = (IUsageAuditDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.UsageAuditDao);
			return usageAuditDAO.maxServiceTime(forDate);
		} catch (Exception e) {
			log.error("Error al obtener el maximo tiempo de servicios", e);
			throw new CustomServiceException("", e);
		}
	}

}
