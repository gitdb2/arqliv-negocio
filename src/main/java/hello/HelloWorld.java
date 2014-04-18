package hello;

import org.joda.time.LocalTime;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class HelloWorld {
	
	
	static void beanFactoryAwareTest()
	{
	    Resource resource = new FileSystemResource("./src/main/resources/bean-lifecycle.xml");
	    BeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
	    BeanWithBeanFactoryAware beanFactoryAwareBean =
	        (BeanWithBeanFactoryAware)xmlBeanFactory.getBean("beanFactoryAwareBean");
	    BeanFactory beanFactory = beanFactoryAwareBean.getBeanFactory();
	    // Do something with this beanFactory object.
	}
	
	 public static void main(String[] args) {
			LocalTime currentTime = new LocalTime();
			System.out.println("The current local time is: " + currentTime);
			Greeter greeter = new Greeter();
			System.out.println(greeter.sayHello());
			
			beanFactoryAwareTest();
		}
}
