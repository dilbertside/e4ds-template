package ch.rasc.e4ds.service;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;


@Service
public class FaultyService implements InitializingBean{
	
	private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Autowired private EventBus eventBus;
	
	@ExtDirectMethod
	@PreAuthorize("isAuthenticated()")
	public String getSomething() {
		return "something";
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("FaultyService::afterPropertiesSet");
		eventBus.register(this);
	}

	@Subscribe 
	public void testSubscribe(String e) {
		logger.debug("FaultyService::testSubscribe, "+e.toString());
	}
}
