package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uy.edu.ort.arqliv.obligatorio.business.ContextSingleton;
import uy.edu.ort.arqliv.obligatorio.common.ContainerService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomInUseServiceException;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Container;
import uy.edu.ort.arqliv.obligatorio.persistencia.constants.PersistenceConstants;
import uy.edu.ort.arqliv.obligatorio.persistencia.dao.IContainerDAO;
/**
 * Implementacion del servicio para contenedores
 * @author rodrigo
 *
 */
public class ContainerServiceImpl implements ContainerService {
	private final Logger log = LoggerFactory.getLogger(ContainerServiceImpl.class);
	
	
	@Autowired
	IContainerDAO containerDAO;
	
	@Override
	public long store(String user, Container container) throws CustomServiceException {
		log.info("llego el login: "+user);
		log.info("llego el container: "+ container);
		try {
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(
//					PersistenceConstants.ContainerDao);

			return containerDAO.store(container);
		} catch (Exception e) {
			log.error("error al dar de alta un contenedor",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String user, long id) throws CustomServiceException {
		boolean ok =false;
		try {
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(PersistenceConstants.ContainerDao);
			ok =  containerDAO.delete(id);
		} catch (Exception e) {
			log.error("error al dar de baja un contenedor",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		if(!ok){
			throw new CustomInUseServiceException("No se puede borrar pues está en unso");
		}
	}



	@Override
	public List<Container> list(String user) throws CustomServiceException {
		try {
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(
//					PersistenceConstants.ContainerDao);
			
			return containerDAO.findAll();
		} catch (Exception e) {
			log.error("error al listar los contenedores",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
		
	}

	@Override
	public long update(String user, Container container) throws CustomServiceException {
		log.info("llego el login: "+user);
		log.info("llego el container: "+ container);
		
		try {
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(
//					PersistenceConstants.ContainerDao);
			Long ret = containerDAO.update(container);
			if(ret == null){
				throw new CustomServiceException("no se permite modificar la capacidad de un contenedor en uso");
			}
			return ret;
		}catch(CustomServiceException e){
			throw e;
		}
		catch (Exception e) {
			log.error("error al modificar un contenedor",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}

	@Override
	public Container find(String user, long id) throws CustomServiceException {
		try {
//			IContainerDAO containerDAO = (IContainerDAO) ContextSingleton.getInstance().getBean(
//					PersistenceConstants.ContainerDao);

			return containerDAO.findById(id);
		} catch (Exception e) {
			log.error("error al buscar un contenedor",e);
			throw new CustomServiceException(e.getMessage(), e);
		}
	}


}
