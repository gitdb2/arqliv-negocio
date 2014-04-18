package com.st.component.di;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.st.component.Component;

public class DoubleMain {

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"basic/application-context-double.xml");
		Component component = (Component) ctx.getBean("doubleComponent");
		component.processData();

	}

}
