package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import java.io.IOException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.modelo.Modelo;


public class Index extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	
	private final Modelo modelo;
	private JiraAuthenticationContext jiraAuthenticationContext;
	private HttpServletRequest request;

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Index( PluginSettingsFactory pluginSettingsFactory) {
		this.modelo = new Modelo(pluginSettingsFactory);
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
		this.request = ServletActionContext.getRequest();
	}
	
	/**
	 * check if the user is logged
	 * then check if the user is paired
	 * if request method is post, redirect to pair view
	 */
	@Override
	protected void doValidation() {
		String username = getUser();
		if(username.equals("")){
			redirectToLogin();
		}else{
			if(isPaired(username)){
				redirectTo("LatchUnpair");
			}else{
				if(request.getMethod().equals("POST")){
					redirectTo("LatchPair");
				}
			}
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

}