package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.ShipService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Ship;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IShipDAO;
/**
 * 
 * @author rodrigo
 *
 */
public class ShipServiceImpl implements ShipService {
	private final Logger log = LoggerFactory.getLogger(ShipServiceImpl.class);

	@Override
	public long store(String user, Ship ship) throws CustomServiceException {
		log.info("llego el login: "+user);
		log.info("llego el ship: "+ ship);
		try {
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);

			return shipDAO.store(ship);
		} catch (Exception e) {
			log.error("error al dar de alta un ship",e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public void delete(String user, long shipId) throws CustomServiceException {
		boolean ok =false;
		try {
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);

			ok =  shipDAO.delete(shipId);
		} catch (Exception e) {
			log.error("error al dar de baja un ship",e);
			throw new CustomServiceException("", e);
		}
		
		if(!ok){
			throw new CustomInUseServiceException("No se puede borrar pues está en unso");
		}
	}



	@Override
	public List<Ship> list(String user) throws CustomServiceException {
		try {
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);

			return shipDAO.findAll();
		} catch (Exception e) {
			log.error("error al listar los ships",e);
			throw new CustomServiceException("", e);
		}
		
	}

	@Override
	public long update(String user, Ship ship) throws CustomServiceException {
		log.info("llego el login: "+user);
		log.info("llego el ship: "+ ship);
		try {
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);

			return shipDAO.store(ship);
		} catch (Exception e) {
			log.error("error al modificar un ship",e);
			throw new CustomServiceException("", e);
		}
	}

	@Override
	public Ship find(String user, long shipId) throws CustomServiceException {
		try {
			IShipDAO shipDAO = (IShipDAO) ContextSingleton.getInstance().getBean(
					PersistenceConstants.ShipDao);

			return shipDAO.findById(shipId);
		} catch (Exception e) {
			log.error("error al buscar un ship",e);
			throw new CustomServiceException("", e);
		}
	}

}
