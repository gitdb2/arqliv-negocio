package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.DepartureService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Departure;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IDepartureDAO;

public class DepartureServiceImpl implements DepartureService {

	private final Logger log = LoggerFactory.getLogger(DepartureServiceImpl.class);
	
	@Override
	public long store(String user, Departure departure, Long shipId, List<Long> containerList) throws CustomServiceException {
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			return departureDAO.store(departure);
		} catch (Exception e) {
			log.error("Error al dar de alta una Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}
	
	@Override
	public long update(String user, Departure departure, Long shipId, List<Long> containerList) throws CustomServiceException {
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			return departureDAO.update(departure);
		} catch (Exception e) {
			log.error("Error al actualizar una Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String user, long id) throws CustomServiceException {
		boolean ok = false;
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			ok = departureDAO.delete(id);
		} catch (Exception e) {
			log.error("Error al dar de baja una Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		if (!ok) {
			throw new CustomInUseServiceException("No se puede borrar pues está en uso");
		}

	}

	@Override
	public List<Departure> list(String user) throws CustomServiceException {
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			List<Departure> ret = new ArrayList<Departure>(departureDAO.findAll());
			for (Departure departure : ret) {
				departure.setContainers(new ArrayList<Container>(departure.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("Error al listar las Partidas", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public Departure find(String user, long id) throws CustomServiceException {
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			Departure ret = departureDAO.findById(id);
			if (ret != null) {
				ret.setContainers(new ArrayList<Container>(ret.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("error al buscar un Arribo", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

}
