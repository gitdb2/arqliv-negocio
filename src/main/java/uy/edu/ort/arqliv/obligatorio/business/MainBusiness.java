package uy.edu.ort.arqliv.obligatorio.business;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainBusiness {

	public static void main(String[] args) {
		new MainBusiness().run();

	}

	private void run() {
	
		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "bean-remoting.xml" });
		
	}

}
