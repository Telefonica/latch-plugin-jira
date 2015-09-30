package com.elevenpaths.latch.util;

import java.io.IOException;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.elevenpaths.latch.modelo.LatchModel;

public class Utilities {

    public static boolean isPaired(String username, LatchModel model){
    	return model.getAccountId(username) != null;
    }
    
	public static void redirectTo(String nextUrl) {
		JiraWebActionSupport redirect = new JiraWebActionSupport();
		String contextPath = redirect.getHttpRequest().getContextPath();
		if (contextPath != null) {
			nextUrl = contextPath + nextUrl;
		}
		try {
			redirect.getHttpResponse().sendRedirect(nextUrl);
		} catch (IOException e) { 
		} catch(IllegalStateException e){ }
	}
	
	public static void redirectToLogin() {
		JiraWebActionSupport redirect = new JiraWebActionSupport();
		String contextPath = redirect.getHttpRequest().getContextPath();
		try {
			redirect.getHttpResponse().sendRedirect(contextPath);
		} catch (IOException e) { 
		} catch(IllegalStateException e){ }
	}
	
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
	
    public static String getUsername(JiraAuthenticationContext jiraAuthenticationContext){
    	ApplicationUser user = jiraAuthenticationContext.getUser();
    	return (user != null) ? user.getUsername() : "";
    }

}
