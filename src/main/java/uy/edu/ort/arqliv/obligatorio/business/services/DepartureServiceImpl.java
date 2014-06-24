package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uy.edu.ort.arqliv.obligatorio.common.DepartureService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Departure;
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
			
//			//control del barco
//			shipId = arrival.getShip().getId();
//			Ship ship = shipDAO.findById(shipId);
//			if (ship == null) {
//				throw new CustomServiceException("el barco con id= "
//						+ shipId + " no se encuentra en la DB. Se cancela el alta de partida");
//			}
			
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
			
			if(sumContainerCapacity >  arrival.getShip().getCapacity()){
				throw new CustomServiceException("La capacidad del barco ("+arrival.getShip().getCapacity()+") no es suficiente para los contenedores seleccionados ("+sumContainerCapacity+")");
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
			if (isUpdate) {
				if(departuresUsignContainers.size() > 1){
					throw new CustomServiceException("alguno de los contenedores ya estan en uso en otras partidas para ese dia");
				}	
			} else {
				if(departuresUsignContainers.size() > 0){
					throw new CustomServiceException("alguno de los contenedores ya estan en uso en otras partidas para ese dia");
				}
			}
			
			if (isUpdate) {
				for (Container container : containersList) {
					//control de que el contenedor haya arrivado y no partido en una fecha menor o igual
					//no considera el departure actual
					if (!departureDAO.isContainerAvailableForDepartureDifferentDeparture(container.getId(), departure.getDepartureDate(), departure.getId())) {
						throw new CustomServiceException("Al contenedor con id= "+ container.getId()
							+ " no se le puede encontrar un arribo anterir que no haya partido");
					}
				}
			} else {
				for (Container container : containersList) {
					//control de que el contenedor haya arrivado y no partido en una fecha menor o igual
					if (!departureDAO.isContainerAvailableForDeparture(container.getId(), departure.getDepartureDate())) {
						throw new CustomServiceException("Al contenedor con id= "+ container.getId()
							+ " no se le puede encontrar un arribo anterir que no haya partido");
					}
				}
			}
			
			//si el barco arribó, entonces hayq que controlar:
			// - Solo se puede crear una partida de un barco que haya arribado y no partido  (para la ultima fecha de arribo)
			// - si la fecha de arribo == partida controlar que los contenedores sean los mismos.
			// - si la fecha de partida > arribo los contenedores pueden cambiar
			// - un contenedor solo puede partir si arribó
			// - los contenedores que parten solo pueden estar en un sólo barco (una sola partida)
			
			departure.setShipTransportedWeightThatDay(sumContainerCapacity);
			departure.setShipCapacityThatDay(arrival.getShip().getCapacity());
			departure.setShip(arrival.getShip());
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
	private int compareDate(Date date1, Date date2) {
	   Calendar cal1 = GregorianCalendar.getInstance();
	   cal1.setTime(date1);
	   cal1.set(Calendar.SECOND, 0);
	   cal1.set(Calendar.MINUTE, 0);
	   cal1.set(Calendar.HOUR, 0);
	   
	   Calendar cal2 = GregorianCalendar.getInstance();
	   cal2.setTime(date2);
	   cal2.set(Calendar.SECOND, 0);
	   cal2.set(Calendar.MINUTE, 0);
	   cal2.set(Calendar.HOUR, 0);
	   
	   log.info("cal1="+ cal1.toString());
	   log.info("cal2="+ cal2.toString());
	   return cal1.compareTo(cal2);
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
				Set<Container> tmp = new HashSet<Container>();
				tmp.addAll(ret.getContainers());
				ret.setContainers(new ArrayList<Container>(tmp));
			}
			return ret;
		} catch (Exception e) {
			log.error("error al buscar una Partida", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

}
