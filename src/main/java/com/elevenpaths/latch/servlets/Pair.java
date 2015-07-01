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

public class Pair extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	private LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private Utilities latchUtilities;

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Pair( PluginSettingsFactory pluginSettingsFactory) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.latchUtilities = new Utilities(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
	}
	
	/**
	 * 
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		
		String username = latchUtilities.getUsername();
		if(username.equals("")){
			latchUtilities.redirectToLogin();
		}else{
			if(latchUtilities.isPaired(username)){
				latchUtilities.redirectTo("/secure/LatchUnpair.jspa");
			}else{
				if(request.getMethod().equals("POST")){
					String token = request.getParameter("token") == null ? "" : request.getParameter("token");
					doPost(token, username);
				}	
			}
			
		}		
	}
	
	/**
	 * check if the token is ok and send it to the pair method
	 * @param token
	 * @param username
	 */
	private void doPost(String token, String username) {
		if (token.length() != 6) {
			setError(getError() + "Field Token must have 6 characters.\n");
		} else if (!token.matches("[a-zA-Z0-9]+")) {
			setError(getError()+ "Only alphanumeric values are permitted in Token field.\n");
		} else {
			pair(token, username);
		}
	}
	
	/**
	 * Send the token to api and if ok, pair the user and save the accountId
	 * if there is a problem, treat the error
	 * @param token which is used to pair
	 * @param username who is gonna pair
	 */
	private void pair(String token, String username) {
		
		if(modelo.getAccountId(username) == null){
			
			String appId = modelo.getAppId();
			String secret = modelo.getSecret();
					
			if(appId != null && secret != null){
				
				LatchApp latch = new LatchApp(appId, secret);
				LatchResponse pairResponse = null;
				try{
					pairResponse = latch.pair(token);
				}catch(NullPointerException e){ }
				
			    com.elevenpaths.latch.Error error = pairResponse.getError();
			    
			    if(error != null){
					switch (error.getCode()) {

					case 205:
						JsonObject jObject = pairResponse.getData();
						String accountId = jObject.get("accountId").getAsString();
						modelo.setAccountId(username, accountId);
						latchUtilities.redirectTo("/secure/LatchUnpair.jspa");
						break;
					case 206:
						setError(getError()+"A problem occurred while trying to pair your account: "
								+ "Pairing token not found or expired.\n");
						break;
					case 102:
						setError(getError()+"A problem occurred while trying to pair your account: "
								+ "	Latch is not configured correctly. Please talk with your admin.\n");
						break;
					default:
						break;
					}
			    }else{
			    	JsonObject jObject = pairResponse.getData();
					String accountId = jObject.get("accountId").getAsString();
					modelo.setAccountId(username, accountId);
					latchUtilities.redirectTo("/secure/LatchUnpair.jspa");
			    }
			}else{
				setError(getError()+"A problem occurred while trying to pair your account: "
						+ "	Latch is not configured correctly. Please talk with your admin.\n");
				latchUtilities.redirectTo("/secure/LatchPair.jspa");
			}
		}else{
			latchUtilities.redirectTo("/secure/LatchUnpair.jspa");
		}
	}
	
	public String getError() {
		return this.error.length() == 0 ? "" : this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
