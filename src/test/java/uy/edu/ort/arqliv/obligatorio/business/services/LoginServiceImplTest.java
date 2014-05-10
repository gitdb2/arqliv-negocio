package uy.edu.ort.arqliv.obligatorio.business.services;

import static org.junit.Assert.*;

import org.junit.Test;

import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;

public class LoginServiceImplTest {

	@Test
	public void checkServiceSuccess() {
		try {
			assertTrue(new LoginServiceImpl().login("1", "1"));
		} catch (CustomServiceException e) {
			// TODO Auto-generated catch block
			
		}
	}

}
