package com.st.component.list;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ListMain {

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"list/application-context-list.xml");
		// Component component = (Component) ctx.getBean("listComponent");
		// component.processData();

		String str = (String) ctx.getBean("myString");
		System.out.println(str);

		List list = (List) ctx.getBean("myArrayList");
		System.out.println(list.size());

	}

}
