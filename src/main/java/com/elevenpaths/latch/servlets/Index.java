package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.message.I18nResolver;
import webwork.action.ServletActionContext;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;

public class Index extends JiraWebActionSupport {

    private HttpServletRequest request;
    private LatchModel model;
    private String error;
    private I18nResolver i18nResolver;
    private JiraAuthenticationContext jiraAuthenticationContext;

    private final String CSRF_ERROR = "com.elevenpaths.latch.latch-plugin-jira.xsrfError";

    private final String LATCH_PAIR = "/secure/LatchPair.jspa";
    private final String LATCH_UNPAIR = "/secure/LatchUnpair.jspa";


    /**
     * Constructor.
     *
     * @param pluginSettingsFactory object to save data
     */

    public Index(PluginSettingsFactory pluginSettingsFactory, I18nResolver i18nResolver) {
        this.request = ServletActionContext.getRequest();
        this.i18nResolver = i18nResolver;
        this.model = new LatchModel(pluginSettingsFactory);
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    @Override
    protected void doValidation() {
        this.error = "";
        String username = Utilities.getUsername(jiraAuthenticationContext);
        System.out.println(username);
        if (!username.equals("")) {
            if (Utilities.isPaired(username, model)) {
                Utilities.redirectTo(LATCH_UNPAIR);
            }
        } else {
            Utilities.redirectToLogin();
        }
    }

    @Override
    protected String doExecute() throws Exception {
        if (request.getMethod().equals("POST")) {
            XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
            String token = request.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY) != null ? request.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY) : "";

            if (xsrfTokenGenerator.validateToken(request, token)) {
                Utilities.redirectTo(LATCH_PAIR);
            } else {
                setError(getError() + i18nResolver.getText(CSRF_ERROR));
            }
        }
        return SUCCESS;
    }

    public String getError() {
        return this.error.length() == 0 ? "" : this.error;
    }

    public void setError(String error) {
        this.error = error;
    }
}