package com.st.postprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class AllBeansLister implements BeanFactoryPostProcessor {

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory factory) throws BeansException {

		System.out.println("The factory contains the followig beans:");
		String[] beanNames = factory.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; ++i)
			System.out.println(beanNames[i]);
	}
}
