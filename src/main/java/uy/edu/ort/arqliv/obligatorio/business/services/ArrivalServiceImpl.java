package uy.edu.ort.arqliv.obligatorio.business.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.ArrivalService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.dominio.Ship;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IArrivalDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IContainerDAO;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IShipDAO;
/**
 * 
 * @author rodrigo
 *
 */
public class ArrivalServiceImpl implements ArrivalService {
	private final Logger log = LoggerFactory.getLogger(ArrivalServiceImpl.class);
	
		
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public synchronized long store(String user, Arrival arrival, Long shipId, List<Long> containerList) throws CustomServiceException {
		log.info("llego el login: "+user);
		return internalCreateUpdate(arrival, shipId, containerList, Operation.CREATE);
	}

	/**
	 * Metodo generico que realiza el alta o la modificacion

	 * @param arrival
	 * @param shipId
	 * @param containerList
	 * @param operation
	 * @return
	 * @throws CustomServiceException
	 */
	private synchronized long internalCreateUpdate( Arrival arrival,
			Long shipId, List<Long> containerList, Operation operation)
			throws CustomServiceException {
	
		log.info("llego el arrival: "+ arrival);
		log.info("llego el ship: "+ shipId);
		log.info("llego el containerList: "+ containerList);
		
		final String  theOperation 	= (operation== Operation.CREATE? "el alta" : "la modificacion");
		final String  errorWhen 	= (operation== Operation.CREATE? "al dar de alta" : "al modificar");
			
		try {
			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ContainerDao);
			List<Container> containers = new ArrayList<Container>();
						
			double sumContainerCapacity = 0.0;
			for (Long containerId : containerList) {
				Container container = containerDAO.findById(containerId);
				if(container == null){
					throw new CustomServiceException("el contenedor con id= "+containerId 
							+ " no se encuentra en la DB. Se cancela "
							+theOperation+" de arribo");
				}
				
				if(containerDAO.isContainerInUse(containerId, arrival.getArrivalDate())){
					SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy");
					throw new CustomServiceException("el contenedor con id= "+containerId 
							+ " ya arribó para la fecha ("+ sdfOut.format(arrival.getArrivalDate())+")."
							+ " Se cancela "+theOperation+" de arribo");
				}
				
				containers.add(container);
				sumContainerCapacity += container.getCapacity();
			}
			
			
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);
			
			Ship ship = shipDAO.findById(shipId);
			if(ship == null){
				throw new CustomServiceException("el barco con id= "+shipId + " no se encuentra en la DB."
						+ " Se cancela "+theOperation+" de arribo");
			}
			
			double shipCapacity = ship.getCapacity();
			
			if(shipCapacity < sumContainerCapacity){
				throw new CustomServiceException("el barco con id= "+shipId 
						+ ", no tiene capacidad (capa="+String.format("%10.2f", shipCapacity)+" Kg.) "
						+ " para soportar "+String.format("%10.2f", sumContainerCapacity)+" Kg. de los contenedores."
						+ " Se cancela "+theOperation+" de arribo");
			}
			
			arrival.setShipCapacityThatDay(shipCapacity);
			arrival.setShip(ship);
			arrival.setContainers(containers);
			
			
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ArrivalDao);

			return arrivalDAO.store(arrival);
		}
		catch (CustomServiceException e){
			log.error("error "+errorWhen+" el Arribo",e);
			throw e;
		}
		catch (Exception e) {
			log.error("error "+errorWhen+" el Arribo",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String user, long id) throws CustomServiceException {
		boolean ok =false;
		try {
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ArrivalDao);

			ok =  arrivalDAO.delete(id);
		} catch (Exception e) {
			log.error("error al dar de baja un Arribo",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		
		if(!ok){
			throw new CustomInUseServiceException("No se puede borrar pues está en uso");
		}
	}



	@Override
	public List<Arrival> list(String user) throws CustomServiceException {
		try {
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ArrivalDao);
			List<Arrival> ret =  new ArrayList<Arrival>(arrivalDAO.findAll());
			
			for (Arrival arrival : ret) {
				arrival.setContainers(new ArrayList<Container>(arrival.getContainers()));
			}
			
			return ret;
		} catch (Exception e) {
			log.error("error al listar los Arribos",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		
	}

	@Override
	public long update(String user, Arrival arrival, Long shipId, List<Long> containerList) throws CustomServiceException {
		log.info("llego el login: "+user);
		return internalCreateUpdate( arrival, shipId, containerList, Operation.MODIFY);
	}

	@Override
	public Arrival find(String user, long id) throws CustomServiceException {
		try {
			IArrivalDAO arrivalDAO = (IArrivalDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ArrivalDao);

			Arrival ret=  arrivalDAO.findById(id);
			if(ret != null){
				ret.setContainers(new ArrayList<Container>(ret.getContainers()));
			}
			return ret;
		} catch (Exception e) {
			log.error("error al buscar un Arribo",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	enum Operation{
		CREATE, MODIFY
	}

}
