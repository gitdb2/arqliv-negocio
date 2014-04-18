package com.st.component.strategy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.st.component.Component;

public class StrategyMain {

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"strategy/application-context-strategy.xml");
		Component component = (Component) ctx.getBean("strategyComponent");

		component.processData();

	}

}