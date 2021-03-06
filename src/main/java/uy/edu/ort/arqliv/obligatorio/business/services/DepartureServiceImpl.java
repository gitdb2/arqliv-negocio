package uy.edu.ort.arqliv.obligatorio.business.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.business.services.ArrivalServiceImpl.Changes;
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
		return internalCreate(departure, shipId, containerList);
	}
	
	private synchronized long internalCreate(Departure departure, Long shipId, List<Long> containerList) throws CustomServiceException { 
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ContainerDao);
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ShipDao);
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);
			
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
			
			Ship ship = shipDAO.findById(shipId);
			if (ship == null) {
				throw new CustomServiceException("el barco con id= "
						+ shipId + " no se encuentra en la DB. Se cancela el alta de partida");
			}
			
			List<Arrival> arrivals = arrivalDAO.findArrivalByShipByDateByPort(ship.getId(), departure.getDepartureDate(), departure.getShipDestination());
			if (arrivals.isEmpty()) {
				throw new CustomServiceException("No hay arribos previos a la fecha " + sdfOut.format(departure.getDepartureDate()) 
						+ " para el barco de id " + ship.getId()
						+ " en el puerto " + departure.getShipDestination() + "."
						+ " Se cancela el alta de partida");
			}
			
			departure.setShip(ship);
			departure.setContainers(containers);
			return departureDAO.store(departure);
		} catch (CustomServiceException e) {
			log.error("error en alta de la Partida", e);
			throw e;
		} catch (Exception e) {
			log.error("error en alta de la Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public long update(String user, Departure newDeparture, Long shipId, List<Long> containerList) throws CustomServiceException {
		return internalUpdate(newDeparture, shipId, containerList);
	}

	private synchronized long internalUpdate(Departure newDeparture, Long shipId, List<Long> containerList) throws CustomServiceException {
		try {
			IDepartureDAO departureDAO = (IDepartureDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.DepartureDao);
			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ContainerDao);
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ShipDao);
			
			Ship newShip = shipDAO.findById(shipId);
			if (newShip == null) {
				throw new CustomServiceException("El barco con id= " + shipId
						+ " no se encuentra en la DB. Se cancela modificacion de arribo");
			}
			
			Departure originalDeparture = departureDAO.findById(newDeparture.getId());

			List<Changes> changesList = findDifferences(newDeparture, originalDeparture, newShip, containerList);
			final boolean DATE_CHANGED = changesList.contains(Changes.DATE);
			final boolean CONT_CHANGED = changesList.contains(Changes.CONT);
			final boolean SHIP_CHANGED = changesList.contains(Changes.SHIP);
			final boolean DESC_CHANGED = changesList.contains(Changes.DESC);
			final boolean DEST_CHANGED = changesList.contains(Changes.ORIG);
			
			if(!DATE_CHANGED && !CONT_CHANGED && !SHIP_CHANGED && !DESC_CHANGED && !DEST_CHANGED){
				throw new CustomServiceException("No hay cambios para guardar en la DB, se cancela el Update");
			}
			
			if (SHIP_CHANGED) {
				newDeparture.setShip(newShip);
				checkPreviousArrival(newDeparture);
			}
			
			if (CONT_CHANGED) {
				List<Container> newContainers = getContainers(containerDAO, containerList);
				if(DATE_CHANGED){
					//cabio contenedor y fecha: no importa si cambio el barco MUST disponibilidad contenedor en la fecha
					//no hay que sacar al arribo de la lista
					checkAvailability(departureDAO, newDeparture, originalDeparture, newContainers, false);
				} else {
					//cambio contenedor: no importa si cambio el barco MUST check 
					//disponibilidad contenedor en la fecha pero sin tener en cuenta los que ya estan asignados al arribo
					checkAvailability(departureDAO, newDeparture, originalDeparture, newContainers, true);
				}
				newDeparture.setContainers(newContainers);
			}
			
			if (DATE_CHANGED) {
				checkAvailability(departureDAO, newDeparture, originalDeparture, originalDeparture.getContainers(), false);
			} else {
				//nada que rompa reglas de negocio cambia
			}
			
			return departureDAO.store(newDeparture);
		} catch (CustomServiceException e) {
			log.error("Error en modificacion de Partida", e);
			throw e;
		} catch (Exception e) {
			log.error("Error en modificacion de Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}
	
	private void checkPreviousArrival(Departure newDeparture) throws CustomServiceException {
		IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);
		List<Arrival> previousArrivals = arrivalDAO
				.findArrivalByShipByDateByPort(newDeparture.getShip().getId(),
						newDeparture.getDepartureDate(),
						newDeparture.getShipDestination());
		if (previousArrivals.isEmpty()) {
			throw new CustomServiceException("El Barco de id " + newDeparture.getShip().getId() 
					+ " no tiene arribos al puerto " + newDeparture.getShipDestination()
					+ " previos a la fecha " + sdfOut.format(newDeparture.getDepartureDate()) + "."
					+ " Se cancela la operacion");
		}
	}

	private boolean checkAvailability(IDepartureDAO departureDAO, Departure newDeparture, Departure origDeparture,
			List<Container> containersToCheck, boolean dontCheckAgainstSameArrivalItem) throws CustomServiceException {
		
		List<Departure> results = departureDAO.findDepartureUsingContainerListForDate(containersToCheck, newDeparture.getDepartureDate());
		
		if(dontCheckAgainstSameArrivalItem){
			//saco de la lista de departures que usan el contenedor en la fecha dada.
			results.remove(newDeparture);
		}
		
		if (results.size() > 0) {
			throw new CustomServiceException("Existe algun contenedor que ya partió para la fecha ("
					+ sdfOut.format(newDeparture.getDepartureDate()) + ")."
					+ " Se cancela la operacion");
		}
		
		return true;
	}
	
	private List<Container> getContainers(IContainerDAO containerDAO, List<Long> containerList) throws CustomServiceException {
		List<Container> containers = new ArrayList<Container>();
		for (Long containerId : containerList) {
			Container container = containerDAO.findById(containerId);
			if (container == null) {
				throw new CustomServiceException("el contenedor con id= "
						+ containerId
						+ " no se encuentra en la DB. Se cancela "
						+ "la operacion");
			}
			containers.add(container);
		}
		return containers;
	}
	
	/**
	 * Examina los parametros y busca que cambios hay en la actualizacion.
	 * y retorna una lista de enumerado Changes
	 * @param newDeparture
	 * @param originalDeparture
	 * @param ship
	 * @param newContainerList
	 * @return
	 */
	private List<Changes> findDifferences(Departure newDeparture,
			Departure originalDeparture, Ship ship, List<Long> newContainerList) {
		
		List<Changes> ret = new ArrayList<>();
		List<Long> originalContainers = generateContainerList(originalDeparture.getContainers());
		
		{//chequeo de contenedores, si hubo cambios en cantidad  o elementos
			boolean containerChanged = newContainerList.size() != originalContainers.size();
			if (containerChanged) {
				ret.add(Changes.CONT);
			} else {
				for (Long newIds : newContainerList) {
					if (!originalContainers.contains(newIds)) {
						containerChanged = true;
						break;
					}
				}
				if (containerChanged) {
					ret.add(Changes.CONT);
				}
			}
		}
		{//Chequeo de cambio de fecha
			if(!DateUtils.isSameDay(originalDeparture.getDepartureDate(), newDeparture.getDepartureDate())){
				ret.add(Changes.DATE);
			}
		}
		{//Chequeo de strings simples no requieren manejo especial por el manejador
			if(!newDeparture.getContainersDescriptions().equals(originalDeparture.getContainersDescriptions())){
				ret.add(Changes.DESC);
			}
			if(!newDeparture.getShipDestination().equals(originalDeparture.getShipDestination())){
				ret.add(Changes.DEST);
			}
		}
		{//Chequeo ship
			if(!ship.getId().equals(originalDeparture.getShip().getId())){
				ret.add(Changes.SHIP);
			}
		}
		return ret;
	}
	
	/**
	 * transforma una lista de contenedores en una lista de sus ids
	 * @param containers
	 * @return
	 */
	private List<Long> generateContainerList(List<Container> containers) {
		List<Long> ret = new ArrayList<>();
		if (containers != null) {
			for (Container container : containers) {
				ret.add(container.getId());
			}
		}
		return ret;
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
