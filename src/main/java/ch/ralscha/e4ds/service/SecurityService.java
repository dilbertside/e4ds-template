package ch.ralscha.e4ds.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.stereotype.Service;

import ch.ralscha.e4ds.config.UserPrincipal;
import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;

@Service
public class SecurityService {

	@ExtDirectMethod
	@RequiresAuthentication
	public String getLoggedOnUser() {
		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
		return principal.getFullName();
	}

}
