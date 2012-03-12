package ch.ralscha.e4ds.config;

import java.util.Locale;
import java.util.Set;

import org.springframework.util.StringUtils;

import ch.ralscha.e4ds.entity.Role;
import ch.ralscha.e4ds.entity.User;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public class UserPrincipal {
	private Long userId;
	private String username;
	private String fullName;
	private Locale locale;
	private Set<String> roles;

	public UserPrincipal(User user) {
		this.userId = user.getId();
		this.username = user.getUserName();
		this.fullName = Joiner.on(" ").skipNulls().join(user.getFirstName(), user.getName());

		if (StringUtils.hasText(user.getLocale())) {
			this.locale = new Locale(user.getLocale());
		} else {
			this.locale = Locale.ENGLISH;
		}

		ImmutableSet.Builder<String> roleSetBuilder = ImmutableSet.builder();

		for (Role role : user.getRoles()) {
			roleSetBuilder.add(role.getName());
		}

		this.roles = roleSetBuilder.build();
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public String getFullName() {
		return fullName;
	}

	public Locale getLocale() {
		return locale;
	}

}
