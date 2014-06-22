package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uy.edu.ort.arqliv.obligatorio.common.ReportsService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;
/**
 * Implementa el servicio de reportes
 * @author mauricio
 *
 */
@Service("reportsService")
public class ReportsServiceImpl implements ReportsService {

	private final Logger log = LoggerFactory.getLogger(ReportsServiceImpl.class);
	
	
	@Autowired
	IArrivalDAO arrivalDAO;
	
	@Override
	public List<Arrival> arrivalsByMonth(String user, int month) throws CustomServiceException {
		try {
			List<Arrival> ret = arrivalDAO.arrivalsByMonth(month);
			for (Arrival arrival : ret) {
				arrival.setContainers(new ArrayList<Container>(arrival.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("Error al obtener el reporte de Arribos por Mes", e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public List<Arrival> arrivalsByMonthByShip(String user, int month, Long shipId) throws CustomServiceException {
		try {
			List<Arrival> ret = arrivalDAO.arrivalsByMonthByShip(month, shipId);
			for (Arrival arrival : ret) {
				arrival.setContainers(new ArrayList<Container>(arrival.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("Error al obtener el reporte de Arribos por Mes por Barco", e);
			throw new CustomServiceException("", e);
		}
	}

}
