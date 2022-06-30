package com.daon.mvc.regreso.models;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;

import java.io.IOException;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletResponse;

public class LiferayUser {

	private static Log log = LogFactoryUtil.getLog(LiferayUser.class.getName());

	private String email;
	private String nombre;
	private ThemeDisplay themeDisplay;
	private User user;
	private RenderRequest renderRequest;
	private RenderResponse renderResponse;

	public LiferayUser() {
	}

	public RenderResponse getRenderResponse() {
		return renderResponse;
	}



	public void setRenderResponse(RenderResponse renderResponse) {
		this.renderResponse = renderResponse;
	}



	public String getNombre() {
		nombre = getUser().getFirstName().concat(" ").concat(getUser().getLastName());
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDaonEmail() {
		email = getUser().getEmailAddress();
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ThemeDisplay getThemeDisplay() {
		return themeDisplay;
	}

	public void setThemeDisplay(ThemeDisplay themeDisplay) {
		this.themeDisplay = themeDisplay;
	}

	public User getUser() {
		try {
			user = PortalUtil.getUser(renderRequest);
		} catch (PortalException e) {
			log.info(LiferayUser.class.getName() + e.getCause());
			e.printStackTrace();
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LiferayUser(ThemeDisplay themeDisplay, User user, RenderRequest renderRequest, RenderResponse renderResponse) {
		this.themeDisplay = themeDisplay;
		this.user = user;
		this.renderRequest = renderRequest;
		this.renderResponse = renderResponse;
	}

	public boolean isValid() {
		return user != null && getNombre() != null && getDaonEmail() != null;
	}
	public String getUrlServer() {
		return renderRequest.getScheme().concat("://").concat(renderRequest.getServerName().concat(":").concat(String.valueOf(renderRequest.getServerPort()).concat(renderRequest.getContextPath())));
	}
	public void sendPage(String namePage) {
		try {
			HttpServletResponse httpServletResponse = PortalUtil.getHttpServletResponse(renderResponse);
			httpServletResponse.sendRedirect(themeDisplay.getCDNHost().concat(themeDisplay.getPathFriendlyURLPublic()).concat("/").concat(namePage));
		} catch (IOException e) {
			log.error("sendPage() ".concat(e.getCause().toString()));
			e.printStackTrace();
		}
	}
	
	public void sendLogin() {
		try {
			HttpServletResponse httpServletResponse = PortalUtil.getHttpServletResponse(renderResponse);
			httpServletResponse.sendRedirect(themeDisplay.getURLSignIn());
		} catch (IOException e) {
			log.error("sendLogin() ".concat(e.getCause().toString()));
			e.printStackTrace();
		}
	}
	public boolean isLogin() {
		 if(getUser() != null) {
			 return Boolean.TRUE;
		 }else {
			 return Boolean.FALSE;
		 }
	 }
	
	public boolean isAdmin() {
		for (Role role : user.getRoles()) {
			if(role.getName().equalsIgnoreCase("Administrator")) return true;
		}
		return false;
	}
	

}
