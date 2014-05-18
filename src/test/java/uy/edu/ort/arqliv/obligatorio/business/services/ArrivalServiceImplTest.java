package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uy.edu.ort.arqliv.obligatorio.common.ArrivalService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;

@ContextConfiguration({ "classpath:bean-persistence.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class ArrivalServiceImplTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	ArrivalService arrivalService = new ArrivalServiceImpl();

	@Test(expected = CustomServiceException.class)
	public void _1_SinCambios() throws CustomServiceException {
		Long shipId = 3L;
		Long arrivalId = 8L;

		List<Long> containerList = Arrays.asList(1L);// , 2L);

		Arrival arrival = arrivalService.find("rodrigo", arrivalId);

		Long res = arrivalService.update("rodrigo", arrival, shipId,
				containerList);

		arrival = arrivalService.find("rodrigo", res);

		System.out.println("modificó " + arrival);
	}

	@Test(expected = CustomServiceException.class)
	public void _2_SinCapacidad() throws CustomServiceException {
		Long shipId = 3L;
		Long arrivalId = 8L;

		List<Long> containerList = Arrays.asList(1L, 2L);

		Arrival arrival = arrivalService.find("rodrigo", arrivalId);

		Long res = arrivalService.update("rodrigo", arrival, shipId,
				containerList);

		arrival = arrivalService.find("rodrigo", res);

		System.out.println("modificó " + arrival);
	}

	@Test
	public void _3_OK() throws CustomServiceException {
		Long shipId = 3L;
		Long arrivalId = 8L;

		List<Long> containerList = Arrays.asList(2L);

		Arrival arrival = arrivalService.find("rodrigo", arrivalId);
		System.out.println("orig arruval " + arrival);
		
		Long res = arrivalService.update("rodrigo", arrival, shipId,
				containerList);

		arrival = arrivalService.find("rodrigo", res);
		Assert.assertTrue(arrival.getContainers().get(0).getId() == 2L);

		System.out.println("modificó " + arrival);

		containerList = Arrays.asList(1L);
		arrivalService.update("rodrigo", arrival, shipId, containerList);
		Assert.assertTrue(arrival.getContainers().get(0).getId() == 1L);

	}

	@Test
	public void _4_CambioFechaOK() throws CustomServiceException {
		Long shipId = 3L;
		Long arrivalId = 8L;

		List<Long> containerList = Arrays.asList(2L);

		Arrival arrival = arrivalService.find("rodrigo", arrivalId);

		Date origDate = arrival.getArrivalDate();

		arrival.setArrivalDate(new Date(112, 4, 17));

		Long res = arrivalService.update("rodrigo", arrival, shipId,
				containerList);

		arrival = arrivalService.find("rodrigo", res);
		Assert.assertTrue(arrival.getContainers().get(0).getId() == 2L);

		System.out.println("modificó " + arrival);

		arrival.setArrivalDate(origDate);
		containerList = Arrays.asList(1L);
		arrivalService.update("rodrigo", arrival, shipId, containerList);
		System.out.println("modificó " + arrival);
		Assert.assertTrue(arrival.getContainers().get(0).getId() == 1L);

	}

	@Test
	public void _5_CambioFechaFails() throws CustomServiceException {
		Long shipId = 3L;
		Long arrivalId = 8L;

		List<Long> containerList = Arrays.asList(2L);

		Arrival arrival = arrivalService.find("rodrigo", arrivalId);
		System.out.println("orig arruval " + arrival);
		
		Date origDate = arrival.getArrivalDate();

		arrival.setArrivalDate(new Date(114, 4, 17));

	    exception.expect(CustomServiceException.class);
		Long res = arrivalService.update("rodrigo", arrival, shipId, containerList);

		arrival = arrivalService.find("rodrigo", res);

		System.out.println("NO modificó " + arrival);

//		arrival.setArrivalDate(origDate);
//		containerList = Arrays.asList(1L);
//		arrivalService.update("rodrigo", arrival, shipId, containerList);
//		System.out.println("modificó " + arrival);
//		Assert.assertTrue(arrival.getContainers().get(0).getId() == 1L);

	}

}
