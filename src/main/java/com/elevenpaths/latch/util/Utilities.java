package com.elevenpaths.latch.util;

import java.io.IOException;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.elevenpaths.latch.modelo.LatchModel;

public class Utilities {

	/**
     * check if the user is paired with latch
     * @param username user logged currently
     * @return if is paired or not
     */
    public static boolean isPaired(String username, LatchModel model){
    	return model.getAccountId(username) != null;
    }
    
	/**
	 * Redirect to another page
	 * @param path where the user goes
	 */
	public static void redirectTo(String nextUrl) {
		JiraWebActionSupport redirect = new JiraWebActionSupport();
		String contextPath = redirect.getHttpRequest().getContextPath();
		if (contextPath != null) {
			nextUrl = contextPath + nextUrl;
		}
		try {
			redirect.getHttpResponse().sendRedirect(nextUrl);
		} catch (IOException e) { }
		catch(IllegalStateException e){ }
	}
	
	/**
	 * Redirect to the login page
	 */
	public static void redirectToLogin() {
		JiraWebActionSupport redirect = new JiraWebActionSupport();
		String contextPath = redirect.getHttpRequest().getContextPath();
		try {
			redirect.getHttpResponse().sendRedirect(contextPath);
		} catch (IOException e) { }
		catch(IllegalStateException e){ }
	}
	
	
	/**
	 * check if the current user is the admin
	 * @return if the user is admin or not
	 */
	public static boolean isAdmin(JiraAuthenticationContext jiraAuthenticationContext, UserManager userManager){
		ApplicationUser user = jiraAuthenticationContext.getUser();
		if(user != null){
			String key = user.getKey();
			UserKey userKey = new UserKey(key);
			return userManager.isSystemAdmin(userKey);
		}else{
			return false;
		}
	}
	
	/**
     * check if exists a logged user
     * @return the name of the user logged, if not exists return a empty string
     */
    public static String getUsername(JiraAuthenticationContext jiraAuthenticationContext){
    	ApplicationUser user = jiraAuthenticationContext.getUser();
    	return (user != null) ? user.getUsername() : "";
    }

}
