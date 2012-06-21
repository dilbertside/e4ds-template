import ch.ralscha.embeddedtc.EmbeddedTomcat;

public class StartTomcat {
	public static void main(final String[] args) throws Exception {
		EmbeddedTomcat.create().setContextPath("/e4")
				.addContextEnvironmentAndResourceFromFile("./src/main/config/tomcat.xml").startAndWait();
	}
}
