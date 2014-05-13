package uy.edu.ort.arqliv.obligatorio.business;


public class MainBusiness {

	public static void main(String[] args) {
		
		
		new MainBusiness().run();
	}

	private void run() {
		ContextSingleton.getInstance().init();
//		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "bean-remoting.xml", "bean-persistence.xml" });
	}

}
