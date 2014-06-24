package uy.edu.ort.arqliv.obligatorio.business.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uy.edu.ort.arqliv.obligatorio.business.services.ArrivalServiceImpl.Changes;
import uy.edu.ort.arqliv.obligatorio.common.DepartureService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Departure;
import uy.edu.ort.arqliv.obligatorio.dominio.Ship;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IContainerDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IDepartureDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IShipDAO;
/**
 * 
 * @author mauricio
 *
 */
@Service("departureService")
public class DepartureServiceImpl implements DepartureService {

	private final Logger log = LoggerFactory.getLogger(DepartureServiceImpl.class);
	
	private SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy");
	
	@Autowired
	IDepartureDAO departureDAO ;
	
	@Autowired
	IContainerDAO containerDAO ;
	
	@Autowired
	IShipDAO shipDAO ;

	@Autowired
	IArrivalDAO arrivalDAO;
	
	@Override
	public long store(String user, Departure departure, Long shipId, List<Long> containerList, Long arrivalId) throws CustomServiceException {
		return internalCreateUpdate(departure, shipId, containerList, arrivalId, false);
	}
	
	/**
	 * asocia un departure con barco contenedores y arrival, y realiza los controles necesarios
	 * @param departure
	 * @param shipId
	 * @param containersIdList
	 * @param arrivalId
	 * @return
	 * @throws CustomServiceException
	 */
	private synchronized long internalCreateUpdate(Departure departure, 
			Long shipId, List<Long> containersIdList, 
			Long arrivalId, boolean isUpdate) throws CustomServiceException {
		try {
			//control existencia de arrival
			Arrival arrival = arrivalDAO.findById(arrivalId);
			if (arrival == null) {
				throw new CustomServiceException("el Arribo con id= "
						+ arrivalId + " no se encuentra en la DB. Se cancela el alta de partida");
			}
			
			//control del barco
			shipId = arrival.getShip().getId();
			Ship ship = shipDAO.findById(shipId);
			if (ship == null) {
				throw new CustomServiceException("el barco con id= "
						+ shipId + " no se encuentra en la DB. Se cancela el alta de partida");
			}
			
			//control de que ya no este asociado a un departure.
			boolean isDeparted;
			if (isUpdate) {
				isDeparted = departureDAO.isArrivalDepartedDifferentDeparture(arrivalId, departure.getId());
			} else {
				isDeparted = departureDAO.isArrivalDeparted(arrivalId);
			}
			 
			if (isDeparted) {
				throw new CustomServiceException("el Arribo con id= "
						+ arrivalId + " ya tiene asignada una partida. Se cancela el alta de partida");
			}
			
			//se limpia la lista de contenedores por si hay repetidos
			Set<Long> containerDepartureSet = new HashSet<>(containersIdList);
			containersIdList = new ArrayList<>(containerDepartureSet);
			
			//control que los contenendores existan.			
			List<Container> containersList = new ArrayList<Container>();
			double sumContainerCapacity = 0.0;
			for (Long containerId : containersIdList) {
				Container container = containerDAO.findById(containerId);

				if (container == null) {
					throw new CustomServiceException("el contenedor con id= " 
							+ containerId + " no se encuentra en la DB. Se cancela el alta de partida");
				}

				sumContainerCapacity += container.getCapacity();
				containersList.add(container);
			}
			
			if(sumContainerCapacity > ship.getCapacity()){
				throw new CustomServiceException("La capacidad del barco ("+ship.getCapacity()+") no es suficiente para los contenedores seleccionados ("+sumContainerCapacity+")");
			}
			
			//control de fecha
			int comparation  = compareDate(arrival.getArrivalDate(), departure.getDepartureDate());
			boolean mismoDia = comparation==0;
			boolean fechaOk  = comparation <=0;
			
			if(!fechaOk){
				throw new CustomServiceException("La fecha de partida no puede ser menor a la de arribo");
			}

			//si es el mismo dia se controla que los contenedores sean los mismos con los que arribo
			if(mismoDia){
				Set<Long> containerArrivedSet = new HashSet<>(arrival.getContainersIdList());
				
				boolean containerChanged =  !(containerArrivedSet.size() == containerDepartureSet.size()
									  && containerArrivedSet.containsAll(containerDepartureSet));
				if(containerChanged){
					throw new CustomServiceException("Los contenedores de partida deben ser mismos que los que arrivaron (fecha arribo =  fecha partida)");
				}
			}
			
			//chequeo de contenedores seleccionados para que no haya otra partida ese dia que los use.
			List<Departure> departuresUsignContainers = departureDAO.findDepartureUsingContainerListForDate(containersList, departure.getDepartureDate());
			if(departuresUsignContainers.size()> 0){
				throw new CustomServiceException("alguno de los contenedores ya estan en uso en otras partidas para ese dia");
			}
				
			for (Container container : containersList) {
				//control de que el contenedor haya arrivado y no partido en una fecha menor o igual
				if (!departureDAO.isContainerAvailableForDeparture(container.getId(), departure.getDepartureDate())) {
					throw new CustomServiceException("Al contenedor con id= "+ container.getId()
						+ " no se le puede encontrar un arribo anterir que no haya partido");
				}
			}
			
			//si el barco arribó, entonces hayq que controlar:
			// - Solo se puede crear una partida de un barco que haya arribado y no partido  (para la ultima fecha de arribo)
			// - si la fecha de arribo == partida controlar que los contenedores sean los mismos.
			// - si la fecha de partida > arribo los contenedores pueden cambiar
			// - un contenedor solo puede partir si arribó
			// - los contenedores que parten solo pueden estar en un sólo barco (una sola partida)
			
			departure.setShipTransportedWeightThatDay(sumContainerCapacity);
			departure.setShip(ship);
			departure.setArrival(arrival);
			departure.setContainers(containersList);
			
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
	public long update(String user, Departure newDeparture, Long shipId, List<Long> containerList, Long arrivalId) throws CustomServiceException {
		return internalCreateUpdate(newDeparture, shipId, containerList, arrivalId, true);//
		//return internalUpdate(newDeparture, shipId, containerList, arrivalId);
	}
	
	/**
	 * Compara fechas sin tener en cuenta hora
	 * @param date1
	 * @param date2
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private int compareDate(Date date1, Date date2) {
	    if (date1.getYear() == date2.getYear() &&
	        date1.getMonth() == date2.getMonth() &&
	        date1.getDate() == date2.getDate()) {
	      return 0 ;
	    } 
	    else if (date1.getYear() < date1.getYear() ||
	             (date1.getYear() == date2.getYear() &&
	              date1.getMonth() < date2.getMonth()) ||
	             (date1.getYear() == date2.getYear() &&
	              date1.getMonth() == date2.getMonth() &&
	              date1.getDate() < date2.getDate())) {
	      return -1 ;
	   }
	   else {
	     return 1 ;
	   }
	}
	

	private synchronized long internalUpdate(Departure newDeparture, Long shipId, List<Long> containerList, Long arrivalId) throws CustomServiceException {
		try {
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
				newDeparture.setShipTransportedWeightThatDay(sumCapacities(newContainers));
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
	
	/**
	 * Suma las capacidades de los contenedores, se usa en caso que cambie el barco y su capacidad
	 * @param containerList
	 * @return
	 */
	private Double sumCapacities(List<Container> containerList){
		double ret = 0.0;
		for (Container container : containerList) {
			ret += container.getCapacity();
		}
		return ret;
	}
	
	private void checkPreviousArrival(Departure newDeparture) throws CustomServiceException {
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
