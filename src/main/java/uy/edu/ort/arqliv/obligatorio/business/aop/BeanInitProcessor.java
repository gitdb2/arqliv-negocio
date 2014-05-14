package uy.edu.ort.arqliv.obligatorio.business.aop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class BeanInitProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("Se va a inicializar el bean " + beanName + " de la clase " + bean.getClass().getCanonicalName());
		return bean;
	}

}
