package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;


public class Admin extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	
	private LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private Utilities latchUtilites;
	private I18nResolver i18nResolver;
	
	private final String APP_ID_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.appIdError1";
	private final String APP_ID_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.appIdError2";
	private final String SECRET_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.secretError1";
	private final String SECRET_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.secretError2";

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 * @param userManager
	 */
	public Admin( PluginSettingsFactory pluginSettingsFactory, UserManager userManager, I18nResolver i18nResolver) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.latchUtilites = new Utilities(pluginSettingsFactory, userManager);
		this.i18nResolver = i18nResolver;
	}
	
	
	/**
	 * check if the user is admin
	 * if the request method is POST, get the new values of appId and secret and then save them
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		if (latchUtilites.isAdmin()){
			String metodo = request.getMethod();
			if(metodo.equals("POST")){
				String appId = request.getParameter("appId") != null ? request.getParameter("appId") : "";
				String secret = request.getParameter("secret") != null ? request.getParameter("secret") : "";
				doPost(appId, secret);
			}
			
		}else{
			latchUtilites.redirectToLogin();
		}
		
	}
	
	/**
	 * save the applicationId and the secret in the model
	 * @param appId
	 * @param secret
	 */
	private void doPost(String appId, String secret){
		
		if(!appId.matches("[a-zA-Z0-9]+")){
			setError(getError()+i18nResolver.getText(APP_ID_ERROR_1));
		} else if(appId.length() != 20){
			setError(getError()+i18nResolver.getText(APP_ID_ERROR_2));
		} else {
			modelo.setAppId(appId);
		}
		
		if(!secret.matches("[a-zA-Z0-9]+")){
			setError(getError()+i18nResolver.getText(SECRET_ERROR_1));
		} else if(secret.length() != 40){
			setError(getError()+i18nResolver.getText(SECRET_ERROR_2));
		} else {
			modelo.setSecret(secret);
		}
	}
	
	public String getAppId(){
		return modelo.getAppId() == null ? "" : modelo.getAppId();
	}
	
	public String getSecret(){
		return modelo.getSecret() == null ? "" : modelo.getSecret();
	}
	
	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}