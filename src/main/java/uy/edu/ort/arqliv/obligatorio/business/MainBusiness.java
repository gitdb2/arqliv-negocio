package uy.edu.ort.arqliv.obligatorio.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainBusiness {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		new MainBusiness().run();
	}

	private void run() {
		ContextSingleton.getInstance().init();
		log.info("=================================");
		log.info("======== SERVER ONLINE ==========");
		log.info("=================================");
	}

}
