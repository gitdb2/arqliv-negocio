package uy.edu.ort.arqliv.obligatorio.business.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Singleton encargado de levantar el contexto spring.
 * Lee de archivo config.properties que xml levantar y 
 * si no se encuentra utiliza valores por defecto
 * @author rodrigo
 *
 */
@Deprecated
public class ContextSingleton {

	private final Logger log = LoggerFactory.getLogger(ContextSingleton.class);

	private static ContextSingleton instance = new ContextSingleton();
	private ApplicationContext ctx;

	private ContextSingleton() {

		Properties prop = new Properties();
		InputStream input = null;
		String[] beanArray = new String[] { "bean-remoting.xml",
				"bean-persistence.xml", "bean-aop.xml" };

		try {

			String filename = "config.properties";
			input = ContextSingleton.class.getClassLoader()
					.getResourceAsStream(filename);

			if (input == null) {
				log.error("Erro, no se encuentra " + filename
						+ ", usando valores por defecto");

			} else {

				// load a properties file from class path, inside static method
				prop.load(input);

				String beans = prop.getProperty("beans");
				if (beans == null) {
					log.error("Erro, no se encuentra " + filename
							+ ", usando valores por defecto");

				} else {
					beanArray = beans.split(",");
					log.info("beans configurados desde properties");
				}
			}

		} catch (IOException ex) {
			// ex.printStackTrace();
			log.error("IOException", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("IOException finally", e);
					// e.printStackTrace();
				}
			}
		}

		ctx = new ClassPathXmlApplicationContext(beanArray);
		
	}

	public static ContextSingleton getInstance() {
		return instance;
	}

	/**
	 * Metod dummy que se utiliza apra dar la idea de inicializacion y ser ejecutado por el main principal al inicio de la aplicacion.
	 */
	public void init() {
		log.info("ContextSingleton Inicializado");
	}

	/**
	 * Metodo para obtener desde el contexto String el bean con nombre beanName
	 * @param beanName
	 * @return
	 */
	public Object getBean(String beanName) {
		return ctx.getBean(beanName);
	}

}
