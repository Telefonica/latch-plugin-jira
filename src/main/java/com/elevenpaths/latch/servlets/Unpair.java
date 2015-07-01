package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;
import com.google.gson.JsonObject;

public class Unpair extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	private final LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private Utilities latchUtilities;
	private I18nResolver i18nResolver;
	
	private final String UNPAIR_ERROR_CONF = "com.elevenpaths.latch.latch-plugin-jira.unpairErrorConf";
	
	private final String LATCH_INDEX = "/secure/LatchIndex.jspa";

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Unpair(PluginSettingsFactory pluginSettingsFactory, I18nResolver i18nResolver) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.latchUtilities = new Utilities(pluginSettingsFactory);
		this.i18nResolver = i18nResolver;
	}
	
	/**
	 * 
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		
		String username = latchUtilities.getUsername();
		if(!username.equals("")){
			if (latchUtilities.isPaired(username)) {
				if(request.getMethod().equals("POST")){
					unpair(username);
				}
			}else{
				latchUtilities.redirectTo(LATCH_INDEX);
			}
		}else{
			latchUtilities.redirectToLogin();
		}
	}
	
	/**
	 * Call to the api to unpair the user
	 * regardless of the response, the user is deleted
	 * @param username who is gonna unpair
	 */
	private void unpair(String username){
		
		String accountId = modelo.getAccountId(username);
		if(accountId != null){
			
			String appId = modelo.getAppId();
			String secret = modelo.getSecret();
			
			if(appId != null && secret != null){
				
				LatchApp latch = new LatchApp(appId, secret);
				LatchResponse unpairResponse = null;
				try{
					unpairResponse = latch.unpair(accountId);
				}catch(Exception e){ }
				JsonObject jObject = unpairResponse.getData();
			    
			    if(jObject == null){
			    	modelo.deleteAccountId(username);
			    }else{
			    	com.elevenpaths.latch.Error error = unpairResponse.getError();
			    	if (error != null) {
						
						if(error.getCode() == 102){
							setError(getError()+i18nResolver.getText(UNPAIR_ERROR_CONF));
						}
			    	}
			    }
			    modelo.deleteAccountId(username);
				
			}else{
				setError(getError()+i18nResolver.getText(UNPAIR_ERROR_CONF));
			}
		}
		latchUtilities.redirectTo(LATCH_INDEX);
	}
	
	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
