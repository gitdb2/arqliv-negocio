package uy.edu.ort.arqliv.obligatorio.business.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.DepartureService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Departure;
import uy.edu.ort.arqliv.obligatorio.dominio.Ship;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IContainerDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IDepartureDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IShipDAO;

public class DepartureServiceImpl implements DepartureService {

	private final Logger log = LoggerFactory.getLogger(DepartureServiceImpl.class);
	
	private SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy");
	
	@Override
	public long store(String user, Departure departure, Long shipId, List<Long> containerList) throws CustomServiceException {
		return internalCreateUpdate(departure, shipId, containerList);
	}
	
	private synchronized long internalCreateUpdate(Departure departure,	Long shipId, List<Long> containerList) throws CustomServiceException { 

		try {
			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ContainerDao);
			List<Container> containers = new ArrayList<Container>();

			for (Long containerId : containerList) {
				Container container = containerDAO.findById(containerId);

				if (container == null) {
					throw new CustomServiceException("el contenedor con id= " 
							+ containerId + " no se encuentra en la DB. Se cancela el alta de partida");
				}

				if (containerDAO.isContainerInUse(containerId, departure.getDepartureDate())) {
					throw new CustomServiceException("el contenedor con id= "
							+ containerId
							+ " ya partió para la fecha ("+ sdfOut.format(departure.getDepartureDate()) + ")."
							+ " Se cancela el alta de partida");
				}

				containers.add(container);
			}
			
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ShipDao);
			Ship ship = shipDAO.findById(shipId);
			if (ship == null) {
				throw new CustomServiceException("el barco con id= "
						+ shipId + " no se encuentra en la DB. Se cancela el alta de partida");
			}
			
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);
			List<Arrival> arrivals = arrivalDAO.findArrivalByShipByDateByPort(ship.getId(), departure.getDepartureDate(), departure.getShipDestination());
			if (arrivals.isEmpty()) {
				throw new CustomServiceException("No hay arribos previos a la fecha " + sdfOut.format(departure.getDepartureDate()) 
						+ " para el barco de id " + ship.getId()
						+ " en el puerto " + departure.getShipDestination() + "."
						+ " Se cancela el alta de partida");
			}
			
			departure.setShip(ship);
			departure.setContainers(containers);

			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);

			return departureDAO.store(departure);
		} catch (CustomServiceException e) {
			log.error("error en alta de la Partida", e);
			throw e;
		} catch (Exception e) {
			log.error("error en alta de la Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}
	
	private long internalUpdate(Departure departure, Long shipId, List<Long> containerList) {
		return 0;
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
			log.error("error al buscar una Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

}
