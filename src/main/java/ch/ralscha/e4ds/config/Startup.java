package ch.ralscha.e4ds.config;

import java.util.Date;

import org.apache.shiro.authc.credential.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ralscha.e4ds.entity.Role;
import ch.ralscha.e4ds.entity.User;
import ch.ralscha.e4ds.repository.RoleRepository;
import ch.ralscha.e4ds.repository.UserRepository;

import com.google.common.collect.Sets;

@Component
public class Startup implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordService passwordService;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (userRepository.count() == 0) {
			//admin user
			User adminUser = new User();
			adminUser.setUserName("admin");
			adminUser.setEmail("test@test.ch");
			adminUser.setFirstName("admin");
			adminUser.setName("admin");
			adminUser.setPasswordHash(passwordService.encryptPassword("admin"));
			adminUser.setEnabled(true);
			adminUser.setLocale("en");
			adminUser.setCreateDate(new Date());

			Role adminRole = roleRepository.findByName("ROLE_ADMIN");
			adminUser.setRoles(Sets.newHashSet(adminRole));

			userRepository.save(adminUser);

			//normal user
			User normalUser = new User();
			normalUser.setUserName("user");
			normalUser.setEmail("user@test.ch");
			normalUser.setFirstName("user");
			normalUser.setName("user");

			normalUser.setPasswordHash(passwordService.encryptPassword("user"));
			normalUser.setEnabled(true);
			normalUser.setLocale("de");
			normalUser.setCreateDate(new Date());

			Role userRole = roleRepository.findByName("ROLE_USER");
			normalUser.setRoles(Sets.newHashSet(userRole));

			userRepository.save(normalUser);
		}
	}

}
