package ch.ralscha.e4ds.service;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.POLL;

import javax.servlet.http.HttpSession;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.stereotype.Service;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;

@Service
public class HeartbeatService {

	@ExtDirectMethod(value = POLL, event = "heartbeat")
	@RequiresAuthentication
	public void heartbeat(HttpSession session) {
		//nothing here
	}
}
