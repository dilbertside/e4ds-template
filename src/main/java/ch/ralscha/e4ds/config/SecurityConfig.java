package ch.ralscha.e4ds.config;

import java.util.Map;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;

@Configuration
public class SecurityConfig {

	@Bean
	public DefaultPasswordService passwordService() {
		return new DefaultPasswordService();
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilter() {
		ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();

		shiroFilter.setSecurityManager(securityManager());

		Map<String, String> filterChainDefs = Maps.newLinkedHashMap();

		filterChainDefs.put("/favicon.ico", "anon");
		filterChainDefs.put("/resources/**", "anon");
		filterChainDefs.put("/extjs/**", "anon");
		filterChainDefs.put("/ux/**", "anon");
		filterChainDefs.put("/login.js", "anon");
		filterChainDefs.put("/wro/login*", "anon");
		filterChainDefs.put("/i18n.js", "anon");

		filterChainDefs.put("/logout", "logout");

		filterChainDefs.put("/**", "authc");

		shiroFilter.setFilterChainDefinitionMap(filterChainDefs);

		shiroFilter.setLoginUrl("/login.html");
		shiroFilter.setSuccessUrl("/index.html");

		return shiroFilter;
	}

	@Bean
	public DefaultWebSecurityManager securityManager() {
		return new DefaultWebSecurityManager(appRealm());
	}

	@Bean
	public AppRealm appRealm() {
		AppRealm appRealm = new AppRealm();

		PasswordMatcher credentialsMatcher = new PasswordMatcher();
		credentialsMatcher.setPasswordService(passwordService());

		appRealm.setCredentialsMatcher(credentialsMatcher);

		return appRealm;
	}

	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	//	@Bean
	//	@DependsOn("lifecycleBeanPostProcessor")
	//	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
	//		return new DefaultAdvisorAutoProxyCreator();
	//	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
		return authorizationAttributeSourceAdvisor;
	}
}
