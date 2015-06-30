package com.elevenpaths.latch.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.Modelo;
import com.google.gson.JsonObject;

public class Pair extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	private final Modelo modelo;
	private JiraAuthenticationContext jiraAuthenticationContext;
	private HttpServletRequest request;
	private String error;

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Pair( PluginSettingsFactory pluginSettingsFactory) {
		this.modelo = new Modelo(pluginSettingsFactory);
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		this.request = ServletActionContext.getRequest();
	}
	
	/**
	 * 
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		System.out.println("Entro en doValidation:Pair");
		
		String username = getUser();
		if(username.equals("")){
			redirectToLogin();
		}else{
			if(isPaired(username)){
				redirectTo("LatchUnpair");
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
		}
		if (!token.matches("[a-zA-Z0-9]+")) {
			setError(getError()
					+ "Only alphanumeric values are permitted in Token field.\n");
		}
		if (token.length() == 6 && token.matches("[a-zA-Z0-9]+")) {
			pair(token, username);
			return;
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
				
				Latch latch = new Latch(appId, secret);
				LatchResponse pairResponse = null;
				try{
					pairResponse = latch.pair(token);
				}catch(Exception e){ }
				
			    com.elevenpaths.latch.Error error = pairResponse.getError();
			    
			    if(error == null){
			    	JsonObject jObject = pairResponse.getData();
					String accountId = jObject.get("accountId").getAsString();
					modelo.setAccountId(username, accountId);
					redirectTo("LatchUnpair");
			    }else{
					System.err.println("Error: " + error.getCode());
					switch (error.getCode()) {

					case 205:
						JsonObject jObject = pairResponse.getData();
						String accountId = jObject.get("accountId").getAsString();
						modelo.setAccountId(username, accountId);
						redirectTo("LatchUnpair");
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
			    }
			}else{
				//habla con tu admin
				setError(getError()+"A problem occurred while trying to pair your account: "
						+ "	Latch is not configured correctly. Please talk with your admin.\n");
				redirectTo("LatchPair");
			}
		}else{
			redirectTo("LatchUnpair");
		}
	}
	
	/**
	 * Redirect to another page
	 * @param path where the user goes
	 */
    private void redirectTo(String path) {
        String nextUrl = "/secure/"+path+".jspa";
        String contextPath = this.request.getContextPath();
        if (contextPath != null) {
            nextUrl = contextPath + nextUrl;
        }
        try {
			ServletActionContext.getResponse().sendRedirect(nextUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
   }

    /**
     * check if the user is paired with latch
     * @param username user logged currently
     * @return if is paired or not
     */
    private boolean isPaired(String username){
    	if (modelo.getAccountId(username) == null){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    /**
     * check if exists a logged user
     * @return the name of the user logged, if not exists return a empty string
     */
    private String getUser(){
    	ApplicationUser user = jiraAuthenticationContext.getUser();
    	if(user == null){
    		return "";
    	}else{
    		return user.getUsername();
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
	
	public String getError() {
		return this.error.length() == 0 ? "" : this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
