package ch.rasc.e4ds.service;

import java.lang.invoke.MethodHandles;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.ralscha.extdirectspring.controller.MethodRegistrar;
import ch.rasc.e4ds.repository.LoggingEventRepository;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * 
 * @author dbs
 *
 */
@Service
public class BusService implements InitializingBean/*, ApplicationListener<ContextRefreshedEvent>*/{
	
	private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Autowired private LoggingEventRepository loggingEventRepository;
	@Autowired private EventBus eventBus;
	@Autowired private MethodRegistrar methodRegistrar;
	//to prevent to run twice scheduler as we receive twice ContextRefreshedEvent
	//private boolean initialized = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("BusService::afterPropertiesSet");
		eventBus.register(this);
		eventBus.register(LoggingEventService.class);
	}

	/*@Override @Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.debug("BusService::onApplicationEvent "+ event.getApplicationContext().getDisplayName());
		
		//if (null != event.getApplicationContext().getParent() && !initialized) {//not yet initialized, wait for the second pass
			//if(env.equalsIgnoreCase("dev"))
			eventBus.register(LoggingEventService.class);
			//initialized = true;
			initAtLast();
			//we rescan and register the API methods
			//methodRegistrar.onApplicationEvent(new ContextRefreshedEvent(event.getApplicationContext()));
			//ContextRefreshedEvent event2 = new ContextRefreshedEvent(event.getApplicationContext()) ;
			//event.getApplicationContext().publishEvent(event2);
		//}
	}*/
	
	/**
	 * this method will broadcast something to all subscribers of Poll event
	 * and we want to do that at last possible moment after webapp ini and DB init
	 */
	private void initAtLast() {
		eventBus.post(new String("application has finally started at " +DateTime.now()));
	}

	
	@Subscribe 
	public void testSubscribe(String e) {
		logger.debug("BusService::testSubscribe, "+e.toString());
	}


}
