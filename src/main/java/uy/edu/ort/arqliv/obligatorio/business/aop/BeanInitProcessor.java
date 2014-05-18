package uy.edu.ort.arqliv.obligatorio.business.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class BeanInitProcessor implements BeanPostProcessor {
	
	private final Logger log = LoggerFactory.getLogger(BeanInitProcessor.class);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		log.info("Se va a inicializar el bean " + beanName + " de la clase " + bean.getClass().getCanonicalName());
		return bean;
	}

}
