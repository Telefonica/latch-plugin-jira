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

public class Unpair extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	private final Modelo modelo;
	private JiraAuthenticationContext jiraAuthenticationContext;
	private HttpServletRequest request;
	private String error;

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Unpair(PluginSettingsFactory pluginSettingsFactory) {
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
		System.out.println("Entro en doValidation:Unpair");
		
		String username = getUser();
		if(username.equals("")){
			redirectToLogin();
		}else{
			if(request.getMethod().equals("GET")){
				doGet(username);
			}else if(request.getMethod().equals("POST")){
				doPost(username);
			}else{
				
			}
		}
	}
	
	/**
	 * it's necessary the user is logged if the user is paired with latch shows
	 * unpaired view if not, shows the paired view
	 */
	private void doGet(String username) {
		if (!isPaired(username)) {
			redirectTo("LatchIndex");
		}
	}

	/**
	 * it's necessary the user is logged redirect to unpair view
	 */
	private void doPost(String username) {
		if (!isPaired(username)) {
			redirectTo("LatchIndex");
		} else {
			unpair(username);
			return;
		}
	}
	
	/**
	 * get the id and the secret to create the Latch object
	 * send unpair request signed with Latch object
	 * analyze the response, if it's empty, ok
	 * if not, error
	 * @param username which is saved in the model
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	
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
				
				Latch latch = new Latch(appId, secret);
				LatchResponse unpairResponse = null;
				try{
					unpairResponse = latch.unpair(accountId);
				}catch(Exception e){ }
				JsonObject jObject = unpairResponse.getData();
			    
			    if(jObject == null){
			    	modelo.deleteAccountId(username);
			    	redirectTo("LatchIndex");
			    }else{
			    	com.elevenpaths.latch.Error error = unpairResponse.getError();
			    	if (error != null) {
						modelo.deleteAccountId(username);
						redirectTo("LatchIndex");
						if(error.getCode() == 102){
							setError(getError()+"A problem occurred while trying to unpair your account: "
									+ "	Latch is not configured correctly. Please talk with your admin.\n");
						}
			    	}
			    }
				
			}else{
				setError(getError()+"A problem occurred while trying to unpair your account: "
						+ "	Latch is not configured correctly. Please talk with your admin.\n");
				redirectTo("LatchIndex");
			}
		}else{
			redirectTo("LatchIndex");
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
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
