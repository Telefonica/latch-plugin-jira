package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonObject;
import webwork.action.ServletActionContext;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;

public class Admin extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;

    private LatchModel modelo;
    private HttpServletRequest request;
    private String error;
    private I18nResolver i18nResolver;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private UserManager userManager;

    private final String APP_ID_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.appIdError1";
    private final String APP_ID_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.appIdError2";
    private final String SECRET_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.secretError1";
    private final String SECRET_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.secretError2";
    private final String INVALID_CREDENTIALS = "com.elevenpaths.latch.latch-plugin-jira.invalidCredentials";

    /**
     * Constructor
     *
     * @param pluginSettingsFactory object to save data
     * @param userManager           manage users of JIRA
     * @param i18nResolver          translate
     */
    public Admin(PluginSettingsFactory pluginSettingsFactory, UserManager userManager, I18nResolver i18nResolver) {
        this.modelo = new LatchModel(pluginSettingsFactory);
        this.request = ServletActionContext.getRequest();
        this.i18nResolver = i18nResolver;
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        this.userManager = userManager;
    }

    @Override
    protected void doValidation() {
        this.error = "";
        if (!Utilities.isAdmin(jiraAuthenticationContext, userManager)) {
            Utilities.redirectToLogin();
        }
    }

    @Override
    protected String doExecute() throws Exception {
        if (request.getMethod().equals("POST")) {
            saveAppIdAndSecret();
        }
        return SUCCESS;
    }

    @com.atlassian.jira.security.xsrf.RequiresXsrfCheck
    private void saveAppIdAndSecret() {
        XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
        xsrfTokenGenerator.generateToken(request);

        String appId = request.getParameter("appId") != null ? request.getParameter("appId") : "";
        String secret = request.getParameter("secret") != null ? request.getParameter("secret") : "";

        if (!appId.matches("[a-zA-Z0-9]+")) {
            setError(getError() + i18nResolver.getText(APP_ID_ERROR_1));
        } else if (appId.length() != 20) {
            setError(getError() + i18nResolver.getText(APP_ID_ERROR_2));
        } else {
            if (!secret.matches("[a-zA-Z0-9]+")) {
                setError(getError() + i18nResolver.getText(SECRET_ERROR_1));
            } else if (secret.length() != 40) {
                setError(getError() + i18nResolver.getText(SECRET_ERROR_2));
            } else {
                if (verifyAppIdAndSecret(appId, secret)) {
                    modelo.setAppId(appId);
                    modelo.setSecret(secret);
                } else {
                    setError(getError() + i18nResolver.getText(INVALID_CREDENTIALS));
                }
            }
        }
    }

    private boolean verifyAppIdAndSecret(String appId, String secret) {
        LatchApp latch = new LatchApp(appId, secret);
        LatchResponse pairResponse = null;
        try {
            pairResponse = latch.pair("123456");
        } catch (NullPointerException ignored) {
        }

        com.elevenpaths.latch.Error error;
        if (pairResponse != null) {
            error = pairResponse.getError();
            if (error != null) {
                if (error.getCode() == 102) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getAppId() {
        return modelo.getAppId() == null ? "" : modelo.getAppId();
    }

    public String getSecret() {
        return modelo.getSecret() == null ? "" : modelo.getSecret();
    }

    public String getError() {
        return this.error;
    }

    private void setError(String error) {
        this.error = error;
    }

}