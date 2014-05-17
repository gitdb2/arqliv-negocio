package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.ReportsService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;

public class ReportsServiceImpl implements ReportsService {

	private final Logger log = LoggerFactory.getLogger(ReportsServiceImpl.class);
	
	@Override
	public List<Arrival> arrivalsByMonth(String user, int month) throws CustomServiceException {
		try {
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);
			return arrivalDAO.arrivalsByMonth(month);
		} catch (Exception e) {
			log.error("Error al obtener el reporte de Arribos por Mes", e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public List<Arrival> arrivalsByMonthByShip(String user, int month, Long shipId) throws CustomServiceException {
		try {
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);
			return arrivalDAO.arrivalsByMonthByShip(month, shipId);
		} catch (Exception e) {
			log.error("Error al obtener el reporte de Arribos por Mes por Barco", e);
			throw new CustomServiceException("", e);
		}
	}

}
