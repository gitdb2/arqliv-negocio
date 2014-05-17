package uy.edu.ort.arqliv.obligatorio.business.services;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uy.edu.ort.arqliv.obligatorio.common.ArrivalService;
import uy.edu.ort.arqliv.obligatorio.common.exceptions.CustomServiceException;
import uy.edu.ort.arqliv.obligatorio.dominio.Arrival;


@ContextConfiguration
(
  {
   "classpath:bean-persistence.xml"
  }
)
@RunWith(SpringJUnit4ClassRunner.class)
public class ArrivalServiceImplTest {

	ArrivalService arrivalService =  new ArrivalServiceImpl();
	
	@Test
	public void TestUpdate(){
		
		try {
			Long shipId = 3L;
			Long arrivalId = 8L;
			
			List<Long> containerList = Arrays.asList(1L);//, 2L);
			
			Arrival arrival = arrivalService.find("rodrigo", arrivalId);
			
			Long res = arrivalService.update("rodrigo", arrival, shipId, containerList);
			
			
			arrival = arrivalService.find("rodrigo", res);
			
			System.out.println("modificó "+arrival);
		} catch (CustomServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
