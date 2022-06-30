package com.daon.mvc.regreso.utils;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class Utils {
	private static Log log = LogFactoryUtil.getLog(Utils.class.getName());
	
	/**
	 * @param para
	 * @param de
	 * @param nombre
	 * @param asunto
	 * @param contenido
	 * @param producto
	 */
	public static void mail(String para, String de, String nombre, String asunto, String contenido, String language){
		try{
			InternetAddress fromAddress = new InternetAddress(de);
			
			InternetAddress toAddress = new InternetAddress(para);
			log.info(fromAddress); 
			log.info(toAddress);
			
			MailMessage mailMessage = new MailMessage();
			mailMessage.setFrom(fromAddress);
			mailMessage.setTo(toAddress);
			mailMessage.setSubject(asunto); 
			mailMessage.setHTMLFormat(true);
			if(language.equalsIgnoreCase("en_US")) {
				mailMessage.setBody(""
						+ "<h3>Dear(a): "+nombre+"<h3>"
						+ "<p>"+contenido+"<p>"
						+ "Jetbank appreciates your interest in us and we welcome you.");
			}else {
				mailMessage.setBody(""
						+ "<h3>Estimado(a): "+nombre+"<h3>"
						+ "<p>"+contenido+"<p>"
						+ "Jetbank te da una coordial bienvenida.");
			}
			MailServiceUtil.sendEmail(mailMessage);
			log.info("El mensaje se envio correctamente");
			
		}catch (AddressException e) {
			// TODO: handle exception
			log.error(e.getStackTrace());
			log.error(e.getCause());
			log.error(e.getLocalizedMessage());
			log.error(e.getMessage());
		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getStackTrace());
			log.error(e.getCause());
			log.error(e.getLocalizedMessage());
			log.error(e.getMessage());
		}
	}

}
