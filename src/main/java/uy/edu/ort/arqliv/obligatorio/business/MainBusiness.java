package uy.edu.ort.arqliv.obligatorio.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main principal sel servidor
 * @author rodrigo
 *
 */
@Deprecated
public class MainBusiness {

	private static final Logger log = LoggerFactory.getLogger(MainBusiness.class);
	public static void main(String[] args) {
		
		log.info("============ START ==============");
		new MainBusiness().run();
	}

	private void run() {
		ContextSingleton.getInstance().init();
		log.info("=================================");
		log.info("======= SERVER IS ONLINE ========");
		log.info("=================================");
	}

}
