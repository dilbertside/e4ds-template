package ch.rasc.e4ds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

@Configuration
@ComponentScan(basePackages = { "ch.ralscha.extdirectspring", "ch.rasc.e4ds" })
@PropertySource({ "version.properties" })
//@EnableAspectJAutoProxy
public class ComponentConfig {
	
	@Bean @Scope
	EventBus eventBus(){
		return new EventBus();
	}
}