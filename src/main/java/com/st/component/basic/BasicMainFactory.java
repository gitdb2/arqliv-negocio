package com.st.component.basic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.st.component.Component;

public class BasicMainFactory {

	public static void main(String[] args) {
		BeanFactory ctx = new XmlBeanFactory(new ClassPathResource(
				"basic/application-context-basic.xml"));
		Component component = (Component) ctx.getBean("basicComponent");
		component.processData();

	}

}
