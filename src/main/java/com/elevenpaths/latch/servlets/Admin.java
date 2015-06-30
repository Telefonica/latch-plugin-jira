package com.elevenpaths.latch.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.elevenpaths.latch.modelo.Modelo;


public class Admin extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	
	private JiraAuthenticationContext jiraAuthenticationContext;
	private UserManager userManager;
	private final Modelo modelo;
	private HttpServletRequest request;
	private String error;

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 * @param userManager
	 */
	public Admin( PluginSettingsFactory pluginSettingsFactory, UserManager userManager) {
		this.modelo = new Modelo(pluginSettingsFactory);
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		this.userManager = userManager;
		this.request = ServletActionContext.getRequest();
	}
	
	
	/**
	 * check if the user is admin
	 * if the request method is POST, get the new values of appId and secret and then save them
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		if (isAdmin()){
			String metodo = request.getMethod();
			if(metodo.equals("POST")){
				String appId = request.getParameter("appId") == null ? "" : request.getParameter("appId");
				String secret = request.getParameter("secret") == null ? "" : request.getParameter("secret");
				doPost(appId, secret);
			}
			
		}else{
			redirectToLogin();
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
		}
		if(appId.length() != 20){
			setError(getError()+"Field Application ID must have 20 characters.\n");
		}
		if(appId.matches("[a-zA-Z0-9]+") && appId.length() == 20){
			modelo.deleteAppId();
			modelo.setAppId(appId);
		}
		
		if(!secret.matches("[a-zA-Z0-9]+")){
			setError(getError()+"Only alphanumeric values are permitted in Secret field.\n");
		}
		if(secret.length() != 40){
			setError(getError()+"Field Secret must have 40 characters.\n");
		}
		if(secret.matches("[a-zA-Z0-9]+") && secret.length() == 40){
			modelo.deleteSecret();
			modelo.setSecret(secret);
		}
	}
	
	/**
	 * Redirect to the login page
	 */
	private void redirectToLogin() {
		try {
			ServletActionContext.getResponse().sendRedirect("/jira/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * check if the current user is the admin
	 * @return if the user is admin or not
	 */
	private boolean isAdmin(){
		ApplicationUser user = jiraAuthenticationContext.getUser();
		if(user == null){
			return false;
		}else{
			String key = user.getKey();
			UserKey userKey = new UserKey(key);
			return userManager.isSystemAdmin(userKey);
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