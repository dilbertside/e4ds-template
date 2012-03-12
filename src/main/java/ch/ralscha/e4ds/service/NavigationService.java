package ch.ralscha.e4ds.service;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.TREE_LOAD;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import ch.ralscha.e4ds.config.UserPrincipal;
import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;

import com.google.common.collect.Sets;

@Service
public class NavigationService {

	@Autowired
	private MessageSource messageSource;

	private MenuNode root;

	public NavigationService() throws JsonParseException, JsonMappingException, IOException {
		Resource menu = new ClassPathResource("/menu.json");
		ObjectMapper mapper = new ObjectMapper();
		root = mapper.readValue(menu.getInputStream(), MenuNode.class);
	}

	@ExtDirectMethod(TREE_LOAD)
	@RequiresAuthentication
	public MenuNode getNavigation(Locale locale) {

		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();

		MenuNode copyOfRoot = new MenuNode(root, principal.getRoles());
		upateIdAndLeaf(new MutableInt(0), copyOfRoot, locale);

		return copyOfRoot;
	}

	private void upateIdAndLeaf(MutableInt id, MenuNode parent, Locale locale) {
		parent.setId(id.intValue());
		parent.setText(messageSource.getMessage(parent.getText(), null, parent.getText(), locale));
		id.add(1);

		parent.setLeaf(parent.getChildren().isEmpty());

		Set<MenuNode> removeChildren = Sets.newHashSet();
		for (MenuNode child : parent.getChildren()) {
			//Remove child if it has no children and it's not a leaf
			if (child.getView() == null && child.getChildren().isEmpty()) {
				removeChildren.add(child);
			} else {
				upateIdAndLeaf(id, child, locale);
			}
		}

		parent.getChildren().removeAll(removeChildren);
	}

}
