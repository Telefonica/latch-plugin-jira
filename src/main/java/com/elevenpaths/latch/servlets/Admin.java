package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

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

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 * @param userManager
	 */
	public Admin( PluginSettingsFactory pluginSettingsFactory, UserManager userManager) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.latchUtilites = new Utilities(pluginSettingsFactory, userManager);
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
			setError(getError()+"Only alphanumeric values are permitted in Application ID field.\n");
		} else if(appId.length() != 20){
			setError(getError()+"Field Application ID must have 20 characters.\n");
		} else {
			modelo.setAppId(appId);
		}
		
		if(!secret.matches("[a-zA-Z0-9]+")){
			setError(getError()+"Only alphanumeric values are permitted in Secret field.\n");
		} else if(secret.length() != 40){
			setError(getError()+"Field Secret must have 40 characters.\n");
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