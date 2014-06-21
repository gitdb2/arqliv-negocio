package uy.edu.ort.arqliv.obligatorio.business.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uy.edu.ort.arqliv.obligatorio.common.ArrivalService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.common.utils.Duple;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Ship;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IContainerDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IShipDAO;

/**
 * Implementacion del servicio de arribos
 * @author rodrigo
 * 
 */
@Service("arrivalService")
public class ArrivalServiceImpl implements ArrivalService {
	
	private final Logger log = LoggerFactory.getLogger(ArrivalServiceImpl.class);

	@Autowired
	IContainerDAO containerDAO;
	@Autowired
	IShipDAO shipDAO;
	@Autowired
	IArrivalDAO arrivalDAO;
	
//	= (IContainerDAO) ContextSingleton
//			.getInstance().getBean(PersistenceConstants.ContainerDao);
//	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized long store(String user, Arrival arrival, Long shipId,
			List<Long> containerList) throws CustomServiceException {
		return internalCreateUpdate(arrival, shipId, containerList, Operation.CREATE);
	}

	/**
	 * Metodo generico que realiza el alta o la modificacion
	 * 
	 * @param arrival
	 * @param shipId
	 * @param containerList
	 * @param operation
	 * @return
	 * @throws CustomServiceException
	 */
	private synchronized long internalCreateUpdate(Arrival arrival,
			Long shipId, List<Long> containerList, Operation operation)
			throws CustomServiceException {

		log.info("llego el arrival: " + arrival);
		log.info("llego el ship: " + shipId);
		log.info("llego el containerList: " + containerList);

		if (operation == Operation.CREATE) {
			final String theOperation = (operation == Operation.CREATE ? "el alta"
					: "la modificacion");
			final String errorWhen = (operation == Operation.CREATE ? "al dar de alta"
					: "al modificar");

			try {
			
//				IArrivalDAO arrivalDAO = (IContainerDAO) ContextSingleton
//						.getInstance().getBean(PersistenceConstants.ContainerDao);
				
				List<Container> containers = new ArrayList<Container>();

				double sumContainerCapacity = 0.0;
				for (Long containerId : containerList) {
					Container container = containerDAO.findById(containerId);
					if (container == null) {
						throw new CustomServiceException(
								"el contenedor con id= "
										+ containerId
										+ " no se encuentra en la DB. Se cancela "
										+ theOperation + " de arribo");
					}

					if (containerDAO.isContainerInUse(containerId,
							arrival.getArrivalDate())) {
						SimpleDateFormat sdfOut = new SimpleDateFormat(
								"dd/MM/yyyy");
						throw new CustomServiceException(
								"el contenedor con id= "
										+ containerId
										+ " ya arribó para la fecha ("
										+ sdfOut.format(arrival
												.getArrivalDate()) + ")."
										+ " Se cancela " + theOperation
										+ " de arribo");
					}

					containers.add(container);
					sumContainerCapacity += container.getCapacity();
				}

//				IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance()
//						.getBean(PersistenceConstants.ShipDao);

				Ship ship = shipDAO.findById(shipId);
				if (ship == null) {
					throw new CustomServiceException("el barco con id= "
							+ shipId + " no se encuentra en la DB."
							+ " Se cancela " + theOperation + " de arribo");
				}

				double shipCapacity = ship.getCapacity();

				if (shipCapacity < sumContainerCapacity) {
					throw new CustomServiceException("el barco con id= "
							+ shipId + ", no tiene capacidad (capa="
							+ String.format("%10.2f", shipCapacity) + " Kg.) "
							+ " para soportar "
							+ String.format("%10.2f", sumContainerCapacity)
							+ " Kg. de los contenedores." + " Se cancela "
							+ theOperation + " de arribo");
				}

				arrival.setShipCapacityThatDay(shipCapacity);
				arrival.setShipTransportedWeightThatDay(sumContainerCapacity);
				arrival.setShip(ship);
				arrival.setContainers(containers);

				IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ArrivalDao);

				return arrivalDAO.store(arrival);
			} catch (CustomServiceException e) {
				log.error("error " + errorWhen + " el Arribo", e);
				throw e;
			} catch (Exception e) {
				log.error("error " + errorWhen + " el Arribo", e);
				throw new CustomServiceException(e.getMessage(), e);
			}
		} else {
			return internalUpdate(arrival, shipId, containerList);
		}
	}

	/**
	 * Metodo para modificacion de un arrival, su implementacion tiene contreoles que 
	 * no permiten que sea el mismo que para el alta.
	 * Refuerza las 3 reglas de negocios que son requerimiento
	 * @param newArrival
	 * @param shipId
	 * @param containerList
	 * @return
	 * @throws CustomServiceException
	 */
	private long internalUpdate(Arrival newArrival, Long shipId,
			List<Long> containerList) throws CustomServiceException {

		log.info("llego el arrival: " + newArrival);
		log.info("llego el ship: " + shipId);
		log.info("llego el containerList: " + containerList);

		final String theOperation = "la modificacion";
		final String errorWhen = "al modificar";

		try {

			
			
//			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance()
//					.getBean(PersistenceConstants.ShipDao);
//			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton
//					.getInstance().getBean(PersistenceConstants.ArrivalDao);
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton
//					.getInstance().getBean(PersistenceConstants.ContainerDao);

			Ship ship = shipDAO.findById(shipId);
			if (ship == null) {
				throw new CustomServiceException("el barco con id= " + shipId
						+ " no se encuentra en la DB." + " Se cancela "
						+ theOperation + " de arribo");
			}
			
			boolean algoritmoIN = false;
			
			Arrival originalArrival = arrivalDAO.findById(newArrival.getId());

			List<Changes> changesList = findDifferences(newArrival, originalArrival, ship, containerList);
			final boolean DATE_CHANGED = changesList.contains(Changes.DATE);
			final boolean CONT_CHANGED = changesList.contains(Changes.CONT);
			final boolean SHIP_CHANGED = changesList.contains(Changes.SHIP);
			final boolean DESC_CHANGED = changesList.contains(Changes.DESC);
			final boolean ORIG_CHANGED = changesList.contains(Changes.ORIG);
			
			log.info("\n-------->cambios :"+changesList);
			
			if(!DATE_CHANGED && !CONT_CHANGED && !SHIP_CHANGED && !DESC_CHANGED && !ORIG_CHANGED){
				throw new CustomServiceException("No hay cambios para guardar en la DB, se cancela el Update");
			}
			
			double shipCapacity = ship.getCapacity();
			if(SHIP_CHANGED){
				newArrival.setShipCapacityThatDay(shipCapacity);
				newArrival.setShip(ship);
			}
			
			if(CONT_CHANGED){
				Duple<Double, List<Container>> newContainersDuple = getCpacityAndContainers(containerDAO, containerList);
				double sumContainerCapacity = newContainersDuple.getFirst();
				List<Container> newContainers = newContainersDuple.getSecond();
				
				checkCapacity(shipId, shipCapacity, sumContainerCapacity);
				
				if(DATE_CHANGED){
					//cabio contenedor y fecha: no importa si cambio el barco MUST check capacidad sum(cont) < cap barco
					//AND disponibilidad contenedor en la fecha
				
					checAvailability(arrivalDAO, newArrival, originalArrival, 
							newContainers, false, algoritmoIN);//no hay que sacar al arribo de la lista
					
				}else{
					//cabio contenedor: no importa si cambio el barco MUST check capacidad sum(cont) < cap barco 
					//AND disponibilidad contenedor en la fecha pero sin tener en cuenta los que ya estan asignados al arribo
					checAvailability(arrivalDAO, newArrival, originalArrival, 
							newContainers, true, algoritmoIN);
				}
				newArrival.setContainers(newContainers);
				
			}else{
				if(SHIP_CHANGED){
					double sumContainerCapacity = sumCapacities(originalArrival.getContainers());
					checkCapacity(shipId, shipCapacity, sumContainerCapacity);
				}

				if(DATE_CHANGED){				
					checAvailability(arrivalDAO, newArrival, originalArrival, 
										originalArrival.getContainers(), false, algoritmoIN);
				}else{
					//nada que rompa reglas de negocio cambia
				}
			}

			

			return arrivalDAO.store(newArrival);
			
		} catch (CustomServiceException e) {
			log.error("error " + errorWhen + " el Arribo", e);
			throw e;
		} catch (Exception e) {
			log.error("error " + errorWhen + " el Arribo", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}
	
	/**
	 * Cheuqeo de capacidad entre barco y contenedores
	 * @param shipId
	 * @param shipCapacity
	 * @param sumContainerCapacity
	 * @throws CustomServiceException
	 */
	private void checkCapacity(Long shipId, double shipCapacity, double sumContainerCapacity) throws CustomServiceException{
		if (shipCapacity < sumContainerCapacity) {
		throw new CustomServiceException("el barco con id= " + shipId
				+ ", no tiene capacidad (capa="
				+ String.format("%10.2f", shipCapacity) + " Kg.) "
				+ " para soportar "
				+ String.format("%10.2f", sumContainerCapacity)
				+ " Kg. de los contenedores." + " Se cancela la operacion");
	}

	}
	
	/**
	 * Chequea que los contenedores este disponibles para determinada fecha
	 * Si se indica, se 
	 * @param arrivalDAO
	 * @param newArrival
	 * @param origArrival
	 * @param containersToCheck
	 * @param dontCheckAgainstArrivalItem
	 * @return
	 * @throws CustomServiceException
	 */
	private boolean checAvailability(IArrivalDAO arrivalDAO, Arrival newArrival, Arrival origArrival,
			List<Container> containersToCheck, boolean dontCheckAgainstSameArrivalItem, boolean usar1) throws CustomServiceException{
		
		
		
		boolean ret = false;
		if(usar1){
			
			List<Arrival> results =  arrivalDAO.findArrivalUsingContainerListForDate(containersToCheck, newArrival.getArrivalDate());
			if(dontCheckAgainstSameArrivalItem){
				results.remove(newArrival); //saco de la lista de arrivals que usan el contenedor en la fecha dada.
			}
			
			if (results.size() > 0) {
				SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy");
				throw new CustomServiceException("Existe algun contenedor que ya arribó para la fecha ("
						+ sdfOut.format(newArrival.getArrivalDate()) + ")."
						+ " Se cancela la operacion");
			}
			ret= true;
		}else{
			for (Container container : containersToCheck) {
				Long containerId = container.getId();
				
				List<Arrival> results =  arrivalDAO.findArrivalUsingContainerForDate(containerId, newArrival.getArrivalDate());
				
				if(dontCheckAgainstSameArrivalItem){
					results.remove(newArrival); //saco de la lista de arrivals que usan el contenedor en la fecha dada.
				}
				
				if (results.size() > 0) {
					SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy");
					throw new CustomServiceException("el contenedor con id= "
							+ containerId + " ya arribó para la fecha ("
							+ sdfOut.format(newArrival.getArrivalDate()) + ")."
							+ " Se cancela la operacion");
				}
			}
			ret= true;
		}
		
		return ret;
	}
	
	/**
	 * genera una lista de contenedores a partir de sus ids y retorna la capacidad acumulada de los contenedores
	 * @param containerDAO
	 * @param containerList
	 * @return
	 * @throws CustomServiceException
	 */
	private Duple<Double, List<Container>> getCpacityAndContainers(IContainerDAO containerDAO, List<Long> containerList) throws CustomServiceException{
		List<Container> containers =new ArrayList<Container>();
		Duple<Double, List<Container>> ret=  new Duple<>(0.0, containers);

		for (Long containerId : containerList) {
			Container container = containerDAO.findById(containerId);
			if (container == null) {
				throw new CustomServiceException("el contenedor con id= "
						+ containerId
						+ " no se encuentra en la DB. Se cancela "
						+ "la operacion");
			}
			containers.add(container);
			ret.setFirst( ret.getFirst() +  container.getCapacity());
		}
		
		return ret;
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

	/**
	 * Examina los parametros y busca que cambios hay en la actualizacion.
	 * y retorna una lista de enumerado Changes
	 * @param newArrival
	 * @param originalArrival
	 * @param ship
	 * @param newContainerList
	 * @return
	 */
	private List<Changes> findDifferences(Arrival newArrival,
			Arrival originalArrival, Ship ship, List<Long> newContainerList) {
		List<Changes> ret = new ArrayList<>();

		List<Long> originalContainers = generateContainerList(originalArrival
				.getContainers());

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
			if(!DateUtils.isSameDay(originalArrival.getArrivalDate(), newArrival.getArrivalDate())){
				ret.add(Changes.DATE);
			}
		}
		{//checkeo de strings simples no requieren manejo especial por el manejador
			if(!newArrival.getContainersDescriptions().equals(originalArrival.getContainersDescriptions())){
				ret.add(Changes.DESC);
			}
			if(!newArrival.getShipOrigin().equals(originalArrival.getShipOrigin())){
				ret.add(Changes.ORIG);
			}
		}
		
		{//checkqueo ship
			if(!ship.getId().equals(originalArrival.getShip().getId())){
				ret.add(Changes.SHIP);
			}
		}

		return ret;

	}

	@Override
	public void delete(String user, long id) throws CustomServiceException {
		boolean ok = false;
		try {
//			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton
//					.getInstance().getBean(PersistenceConstants.ArrivalDao);

			ok = arrivalDAO.delete(id);
		} catch (Exception e) {
			log.error("error al dar de baja un Arribo", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		if (!ok) {
			throw new CustomInUseServiceException("No se puede borrar pues está en uso");
		}
	}

	@Override
	public List<Arrival> list(String user) throws CustomServiceException {
		try {
//			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton
//					.getInstance().getBean(PersistenceConstants.ArrivalDao);
			List<Arrival> ret = new ArrayList<Arrival>(arrivalDAO.findAll());

			for (Arrival arrival : ret) {
				arrival.setContainers(new ArrayList<Container>(arrival
						.getContainers()));
			}

			return ret;
		} catch (Exception e) {
			log.error("error al listar los Arribos", e);
			throw new CustomServiceException(e.getMessage(), e);
		}

	}

	@Override
	public long update(String user, Arrival arrival, Long shipId,
			List<Long> containerList) throws CustomServiceException {
		log.info("llego el login: " + user);
		return internalCreateUpdate(arrival, shipId, containerList,
				Operation.MODIFY);
	}

	@Override
	public Arrival find(String user, long id) throws CustomServiceException {
		try {
//			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton
//					.getInstance().getBean(PersistenceConstants.ArrivalDao);

			Arrival ret = arrivalDAO.findById(id);
			if (ret != null) {
				ret.setContainers(new ArrayList<Container>(ret.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("error al buscar un Arribo", e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	enum Operation {
		CREATE, MODIFY
	}

	enum Changes {
		DATE, CONT, SHIP, DESC, ORIG, DEST
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
}
