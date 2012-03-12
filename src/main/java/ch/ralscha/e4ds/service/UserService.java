package ch.ralscha.e4ds.service;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.FORM_POST;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_MODIFY;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_READ;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.ralscha.e4ds.config.UserPrincipal;
import ch.ralscha.e4ds.entity.QUser;
import ch.ralscha.e4ds.entity.Role;
import ch.ralscha.e4ds.entity.User;
import ch.ralscha.e4ds.repository.RoleRepository;
import ch.ralscha.e4ds.repository.UserCustomRepository;
import ch.ralscha.e4ds.repository.UserRepository;
import ch.ralscha.e4ds.util.Util;
import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.bean.ExtDirectResponseBuilder;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResponse;
import ch.ralscha.extdirectspring.filter.StringFilter;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.mysema.query.BooleanBuilder;

@Controller
@Lazy
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserCustomRepository userCustomRepository;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private MessageSource messageSource;

	@ExtDirectMethod(STORE_READ)
	@RequiresRoles("ROLE_ADMIN")
	public ExtDirectStoreResponse<User> load(ExtDirectStoreReadRequest request) {

		String filterValue = null;
		if (!request.getFilters().isEmpty()) {
			StringFilter filter = (StringFilter) request.getFilters().iterator().next();
			filterValue = filter.getValue();
		}

		Page<User> page = userCustomRepository.findWithFilter(filterValue, Util.createPageRequest(request));
		return new ExtDirectStoreResponse<User>((int) page.getTotalElements(), page.getContent());
	}

	@ExtDirectMethod(STORE_READ)
	@RequiresAuthentication
	public List<Role> loadAllRoles() {
		return roleRepository.findAll();
	}

	@ExtDirectMethod(STORE_MODIFY)
	@RequiresRoles("ROLE_ADMIN")
	public void destroy(List<User> destroyUsers) {
		for (User user : destroyUsers) {
			userRepository.delete(user);
		}
	}

	@ExtDirectMethod(FORM_POST)
	@ResponseBody
	@RequestMapping(value = "/userFormPost", method = RequestMethod.POST)
	@Transactional
	@RequiresAuthentication
	public ExtDirectResponse userFormPost(HttpServletRequest request, Locale locale,
			@RequestParam(required = false, defaultValue = "false") boolean options,
			@RequestParam(required = false) String roleIds, @RequestParam(value = "id", required = false) Long userId,
			@Valid User modifiedUser, BindingResult result) {

		//Check uniqueness of userName and email
		if (!result.hasErrors()) {
			if (!options) {
				BooleanBuilder bb = new BooleanBuilder(QUser.user.userName.equalsIgnoreCase(modifiedUser.getUserName()));
				if (userId != null) {
					bb.and(QUser.user.id.ne(userId));
				}
				if (userRepository.count(bb) > 0) {
					result.rejectValue("userName", null, messageSource.getMessage("user_usernametaken", null, locale));
				}
			}

			BooleanBuilder bb = new BooleanBuilder(QUser.user.email.equalsIgnoreCase(modifiedUser.getEmail()));
			if (userId != null && !options) {
				bb.and(QUser.user.id.ne(userId));
			} else if (options) {
				UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
				bb.and(QUser.user.id.ne(principal.getUserId()));
			}

			if (userRepository.count(bb) > 0) {
				result.rejectValue("email", null, messageSource.getMessage("user_emailtaken", null, locale));
			}
		}

		if (!result.hasErrors()) {

			if (StringUtils.hasText(modifiedUser.getPasswordHash())) {
				modifiedUser.setPasswordHash(passwordService.encryptPassword(modifiedUser.getPasswordHash()));
			}

			if (!options) {
				Set<Role> roles = Sets.newHashSet();
				if (StringUtils.hasText(roleIds)) {
					Iterable<String> roleIdsIt = Splitter.on(",").split(roleIds);
					for (String roleId : roleIdsIt) {
						roles.add(roleRepository.findOne(Long.valueOf(roleId)));
					}
				}

				if (userId != null) {
					User dbUser = userRepository.findOne(userId);
					if (dbUser != null) {
						dbUser.getRoles().clear();
						dbUser.getRoles().addAll(roles);
						dbUser.update(modifiedUser, false);
					}
				} else {
					modifiedUser.setCreateDate(new Date());
					modifiedUser.setRoles(roles);
					userRepository.save(modifiedUser);
				}
			} else {
				UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
				User dbUser = userRepository.findOne(principal.getUserId());
				if (dbUser != null) {
					dbUser.update(modifiedUser, true);
				}

			}
		}

		ExtDirectResponseBuilder builder = new ExtDirectResponseBuilder(request);
		builder.addErrors(result);
		return builder.build();

	}

	@ExtDirectMethod
	@RequiresAuthentication
	@Transactional(readOnly = true)
	public User getLoggedOnUserObject() {
		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
		return userRepository.findOne(principal.getUserId());

	}
}
