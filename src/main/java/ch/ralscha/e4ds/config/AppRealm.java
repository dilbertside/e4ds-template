package ch.ralscha.e4ds.config;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ch.ralscha.e4ds.entity.Role;
import ch.ralscha.e4ds.entity.User;
import ch.ralscha.e4ds.repository.UserRepository;

public class AppRealm extends AuthorizingRealm {

	private UserRepository userRepository;

	@Autowired
	private ApplicationContext context;

	public AppRealm() {
		setName("AppRealm");
	}

	public UserRepository getUserRepository() {
		if (userRepository == null) {
			userRepository = context.getBean(UserRepository.class);
		}
		return userRepository;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		UserPrincipal principal = (UserPrincipal) getAvailablePrincipal(principals);
		User user = getUserRepository().findOne(principal.getUserId());

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		for (Role role : user.getRoles()) {
			info.addRole(role.getName());
		}

		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String username = upToken.getUsername();

		if (username == null) {
			throw new AccountException("Null usernames are not allowed by this realm.");
		}

		SimpleAuthenticationInfo info = null;

		User user = getUserRepository().findByUserName(username);
		if (user != null) {
			if (user.isEnabled()) {
				info = new SimpleAuthenticationInfo(new UserPrincipal(user), user.getPasswordHash(), getName());
			} else {
				throw new DisabledAccountException("User [" + username + "] not enabled");
			}
		} else {
			throw new UnknownAccountException("User [" + username + "] not found");
		}

		return info;
	}

}
