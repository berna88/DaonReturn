package com.daon.mvc.regreso.portlet;

import com.daon.mvc.regreso.constants.DaonReturnPortletKeys;
import com.daon.mvc.regreso.models.LiferayUser;
import com.daon.mvc.regreso.utils.Utils;
import com.growjet.mx.api.VerifyXapp;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import mx.com.growjet.daon.api.DaonApi;

/**
 * @author berna
 */
@Component(immediate = true, property = { 
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css", 
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=DaonReturn", 
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp", 
		"javax.portlet.name=" + DaonReturnPortletKeys.DAONRETURN,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user" 
		}, service = Portlet.class)
public class DaonReturnPortlet extends MVCPortlet {

	private static Log log = LogFactoryUtil.getLog(DaonReturnPortlet.class.getName());
	
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		try {
			log.info("entrando al doview");
			User user = PortalUtil.getUser(renderRequest);
			// log.info(user.getExpandoBridge().getAttribute("RegistroDaon"));
			ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
			LiferayUser liferayUser = new LiferayUser(themeDisplay, user, renderRequest, renderResponse);
			if (themeDisplay.isSignedIn()) {
				log.info("Entrando al login");
				if (!liferayUser.isAdmin()) {
					log.info("admin");
					if (daonApi.isRegisterUserIntoDaon(liferayUser.getDaonEmail())) {
						log.info("Entrar a validacion is true");
						renderRequest.setAttribute("isRegister", true);
						super.doView(renderRequest, renderResponse);
					} else {
						log.info("Entrar a validacion is false");
						renderRequest.setAttribute("isRegister", false);
						super.doView(renderRequest, renderResponse);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error desde validador de usuario".concat(e.getCause().toString()));
			e.getStackTrace();
		}
		
	}
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {
		log.info("<--- serveResource --->");
		String reqResource = ParamUtil.getString(resourceRequest,"cmd");
		log.info(reqResource);
		if(resourceResponse.getStatus() == HttpURLConnection.HTTP_OK) {
			if(reqResource != null && !reqResource.isEmpty()) {
				log.info("despues de la validacion");
				switch (reqResource) {
				case "getEmail":
					try {
						log.info("Dentro de getEmail");
						ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
						String sender = "";
						if(themeDisplay.getSiteGroup().getExpandoBridge().getAttribute("sender") != null) {
							 sender = themeDisplay.getSiteGroup().getExpandoBridge().getAttribute("sender").toString();
						}else {
							 sender = "JETBANK no-reply <retail.consumer2022@gmail.com>";
						}
						log.info(sender);
						String email = ParamUtil.getString(resourceRequest, "getEmail");
						if(themeDisplay.getLanguageId().equalsIgnoreCase("en_US")) {
							String nombreCompleto =  themeDisplay.getUser().getFirstName().concat(themeDisplay.getUser().getLastName());
							String asunto = "Biometric Registration Confirmation";
							String contenido = "You have successfully completed your biometric registration, now you can use your card by accessing the following link: ".concat(themeDisplay.getCDNBaseURL());
							Utils.mail(email, "JETBANK no-reply <retail.consumer2022@gmail.com>", nombreCompleto , asunto, contenido, themeDisplay.getLanguageId());
						}else {
							String nombreCompleto =  themeDisplay.getUser().getFirstName().concat(themeDisplay.getUser().getLastName());
							String asunto = "Confirmación de Registro Biometrico";
							String contenido = "Haz concluido con éxito tu registro biométrico, ahora puedes hacer uso de tu tarjeta, accediendo a la siguiente liga: ".concat(themeDisplay.getCDNBaseURL());
							Utils.mail(email, "JETBANK no-reply <retail.consumer2022@gmail.com>", nombreCompleto , asunto, contenido, themeDisplay.getLanguageId());
						}
						resourceResponse.getWriter().write(email);
						super.serveResource(resourceRequest, resourceResponse);
					} catch (Exception e) {
						// TODO: handle exception
						log.error(e);
					}
					break;
				case "getToken":
					log.info("Entrando al token");
					String code = ParamUtil.getString(resourceRequest, "getCode");
					String clientId = ParamUtil.getString(resourceRequest, "getClientId");
					String clientSecret = ParamUtil.getString(resourceRequest, "getClientSecret");
					String grantType = ParamUtil.getString(resourceRequest, "getGrantType");
					String redirectUri = ParamUtil.getString(resourceRequest, "getRedirectUri");
					
					log.info(code+" : "+clientId+" : "+clientSecret+" : "+grantType+" : "+redirectUri);
					//(---------------
					String urlParameters  = "code="+code+"&client_id="+clientId+"&client_secret="+clientSecret+"&grant_type="+grantType+"&redirect_uri="+redirectUri;
					
					byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
					int postDataLength = postData.length;
			         log.info(urlParameters); 
			         
			         URL url = new URL("https://emea-saas-dobs-idp.identityx-cloud.com/auth/realms/" + clientId + "/protocol/openid-connect/token");
			        
			         
			         HttpURLConnection conn= (HttpURLConnection) url.openConnection();
			         conn.setDoOutput(true);
			         conn.setRequestMethod("POST");
			         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			         conn.setRequestProperty("charset", "utf-8");
			         conn.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
			         conn.setRequestProperty("Accept", "application/json");
			         conn.setUseCaches(false);
			         
			         try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			        	   wr.write( postData );
			        	}
			  
			  
			         try (BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8")))
			         {
			        	 StringBuilder response = new StringBuilder();
			        	 log.info("Dentro de laa respuesta");
			        	    String responseLine = null;
			        	    while ((responseLine = bf.readLine()) != null) {
			        	        response.append(responseLine.trim());
			        	    }
			        	    resourceResponse.getWriter().write(response.toString());
			         }catch (Exception e) {
						// TODO: handle exception
			        	 log.info(e.getCause());
			        	 log.info(e.getMessage());
			        	 resourceResponse.getWriter().write(e.getMessage());
			        	 log.info(e.getStackTrace());
			        	 log.info(e.getLocalizedMessage());
			        	 e.printStackTrace();
					}

					
					break;
				default:
					break; 
				}
			}	
		}	
	}
	
	
	@Reference
	DaonApi daonApi;

	@Reference
	VerifyXapp verifyXapp;
}