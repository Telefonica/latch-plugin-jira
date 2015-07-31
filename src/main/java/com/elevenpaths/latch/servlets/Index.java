package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;


public class Index extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	
	private HttpServletRequest request;
	private LatchModel model;
	private JiraAuthenticationContext jiraAuthenticationContext;
	
	private final String LATCH_PAIR = "/secure/LatchPair.jspa";
	private final String LATCH_UNPAIR = "/secure/LatchUnpair.jspa";

	/**
	 * Constructor.
	 * @param pluginSettingsFactory object to save data
	 */
	public Index( PluginSettingsFactory pluginSettingsFactory) {
		this.request = ServletActionContext.getRequest();
		this.model = new LatchModel(pluginSettingsFactory);
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
	}
	
	@Override
	protected void doValidation() {
		String username = Utilities.getUsername(jiraAuthenticationContext);
		if(!username.equals("")){
			if(Utilities.isPaired(username, model)){
				Utilities.redirectTo(LATCH_UNPAIR);
			}
		}else{
			Utilities.redirectToLogin();
		}
	}
	
	
	@Override
	protected String doExecute() throws Exception {
		if(request.getMethod().equals("POST")){
			toPair();
		}
		return SUCCESS;
	}
	
	@com.atlassian.jira.security.xsrf.RequiresXsrfCheck
	private void toPair(){
		XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
		xsrfTokenGenerator.generateToken(request);
		Utilities.redirectTo(LATCH_PAIR);
	}

}