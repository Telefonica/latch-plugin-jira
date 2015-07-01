package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.util.Utilities;


public class Index extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	
	private HttpServletRequest request;
	private Utilities latchUtilities;
	
	private final String LATCH_PAIR = "/secure/LatchPair.jspa";
	private final String LATCH_UNPAIR = "/secure/LatchUnpair.jspa";

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Index( PluginSettingsFactory pluginSettingsFactory) {
		this.request = ServletActionContext.getRequest();
		this.latchUtilities = new Utilities(pluginSettingsFactory);
	}
	
	/**
	 * check if the user is logged
	 * then check if the user is paired
	 * if request method is post, redirect to pair view
	 */
	@Override
	protected void doValidation() {
		String username = latchUtilities.getUsername();
		if(!username.equals("")){
			if(latchUtilities.isPaired(username)){
				latchUtilities.redirectTo(LATCH_UNPAIR);
			}else{
				if(request.getMethod().equals("POST")){
					latchUtilities.redirectTo(LATCH_PAIR);
				}
			}
		}else{
			latchUtilities.redirectToLogin();
		}
	}

}