package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.web.action.JiraWebActionSupport;
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

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Unpair(PluginSettingsFactory pluginSettingsFactory) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.latchUtilities = new Utilities(pluginSettingsFactory);
	}
	
	/**
	 * 
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		System.out.println("Entro en doValidation:Unpair");
		
		String username = latchUtilities.getUsername();
		if(!username.equals("")){
			if (latchUtilities.isPaired(username)) {
				if(request.getMethod().equals("POST")){
					unpair(username);
				}
			}else{
				latchUtilities.redirectTo("/secure/LatchIndex.jspa");
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
			    	latchUtilities.redirectTo("/secure/LatchIndex.jspa");
			    }else{
			    	com.elevenpaths.latch.Error error = unpairResponse.getError();
			    	if (error != null) {
						
						if(error.getCode() == 102){
							setError(getError()+"A problem occurred while trying to unpair your account: "
									+ "	Latch is not configured correctly. Please talk with your admin.\n");
						}
			    	}
			    }
			    modelo.deleteAccountId(username);
			    latchUtilities.redirectTo("/secure/LatchIndex.jspa");
				
			}else{
				setError(getError()+"A problem occurred while trying to unpair your account: "
						+ "	Latch is not configured correctly. Please talk with your admin.\n");
				latchUtilities.redirectTo("/secure/LatchIndex.jspa");
			}
		}else{
			latchUtilities.redirectTo("/secure/LatchIndex.jspa");
		}
	}
	
	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
