package ch.rasc.e4ds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import ch.ralscha.extdirectspring.controller.BatchedMethodsExecutionPolicy;

@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig {
	// right now there is nothing here
	
	@Bean
	public ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean() {
	    ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
	    factory.setCorePoolSize(50);
	    factory.setMaxPoolSize(200);
	    factory.setQueueCapacity(5000);
	    return factory;
	}
	
}
