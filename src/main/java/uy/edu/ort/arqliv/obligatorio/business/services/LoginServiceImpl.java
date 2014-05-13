package uy.edu.ort.arqliv.obligatorio.business.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uy.edu.ort.arqliv.obligatorio.common.LoginService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;

public class LoginServiceImpl implements LoginService{
	private final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);


	@Override
	public boolean login(String user, String password) throws CustomServiceException {
		
		log.info("user: "+ user + " pass: "+password );
		return "1".equals(user);
	}

}
