package ch.rasc.e4ds.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import ch.ralscha.extdirectspring.controller.BatchedMethodsExecutionPolicy;
import ch.ralscha.extdirectspring.util.JsonHandler;
import ch.rasc.e4ds.web.AppLocaleResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableMap;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private Environment environment;

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/").setCachePeriod(31556926);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		//registry.addViewController("/").setViewName("index");
		//registry.addViewController("/index.html").setViewName("index");
		registry.addViewController("/login.html").setViewName("login");
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	@Autowired ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean;
	@Bean
	public ch.ralscha.extdirectspring.controller.Configuration configuration() throws Exception {
		SecurityContextHolder.setStrategyName("MODE_INHERITABLETHREADLOCAL");
		ch.ralscha.extdirectspring.controller.Configuration config = new ch.ralscha.extdirectspring.controller.Configuration();
		config.setSendStacktrace(environment.acceptsProfiles("development"));
		config.setExceptionToMessage(new ImmutableMap.Builder<Class<?>, String>().put(AccessDeniedException.class,
				"accessdenied").build());
		config.setBatchedMethodsExecutionPolicy(BatchedMethodsExecutionPolicy.CONCURRENT);
	    config.setBatchedMethodsExecutorService(threadPoolExecutorFactoryBean.getObject());
		return config;
	}

	@Bean
	public JsonHandler jsonHandler() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());

		JsonHandler jsonHandler = new JsonHandler();
		jsonHandler.setMapper(mapper);
		return jsonHandler;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	@Bean
	public MessageSource messageSource() {

		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setFallbackToSystemLocale(false);

		// development
		// messageSource.setCacheSeconds(60);

		return messageSource;
	}

	@Bean
	public LocaleResolver localeResolver() {
		AppLocaleResolver resolver = new AppLocaleResolver();
		resolver.setDefaultLocale(Locale.ENGLISH);
		return resolver;
	}
}