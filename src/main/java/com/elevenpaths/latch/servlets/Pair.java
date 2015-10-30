package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;
import com.google.gson.JsonObject;

public class Pair extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;
	private LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private I18nResolver i18nResolver;
	private JiraAuthenticationContext jiraAuthenticationContext;

	private final String TOKEN_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.tokenError1";
	private final String TOKEN_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.tokenError2";
	private final String PAIR_ERROR_206 = "com.elevenpaths.latch.latch-plugin-jira.pairError206";
	private final String PAIR_ERROR_CONF = "com.elevenpaths.latch.latch-plugin-jira.pairErrorConf";

	private final String LATCH_UNPAIR = "/secure/LatchUnpair.jspa";

	/**
	 * Constructor
	 * 
	 * @param pluginSettingsFactory
	 *            object to save data
	 * @param i18nResolver
	 *            translate
	 */
	public Pair(PluginSettingsFactory pluginSettingsFactory, I18nResolver i18nResolver) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.i18nResolver = i18nResolver;
		this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
	}

	@Override
	protected void doValidation() {
		this.error = "";

		String username = Utilities.getUsername(jiraAuthenticationContext);
		if (username.equals("")) {
			Utilities.redirectToLogin();
		} else {
			if (Utilities.isPaired(username, modelo)) {
				Utilities.redirectTo(LATCH_UNPAIR);
			}
		}
	}

	@Override
	protected String doExecute() throws Exception {
		if (request.getMethod().equals("POST")) {
			String token = request.getParameter("token") == null ? "" : request.getParameter("token");

			if (token.length() != 6) {
				setError(getError() + i18nResolver.getText(TOKEN_ERROR_1));
			} else if (!token.matches("[a-zA-Z0-9]+")) {
				setError(getError() + i18nResolver.getText(TOKEN_ERROR_2));
			} else {
				pair(token);
			}
		}
		return SUCCESS;
	}

	@com.atlassian.jira.security.xsrf.RequiresXsrfCheck
	private void pair(String token) {

		XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
		xsrfTokenGenerator.generateToken(request);

		String username = Utilities.getUsername(jiraAuthenticationContext);

		if (modelo.getAccountId(username) == null) {

			String appId = modelo.getAppId();
			String secret = modelo.getSecret();

			if (appId != null && secret != null) {

				LatchApp latch = new LatchApp(appId, secret);
				LatchResponse pairResponse = null;
				try {
					pairResponse = latch.pair(token);
				} catch (NullPointerException ignored) {
				}

				com.elevenpaths.latch.Error error;
				if (pairResponse != null) {
					error = pairResponse.getError();
					if (error != null) {
						switch (error.getCode()) {
							case 205:
								JsonObject jObject = pairResponse.getData();
								String accountId = jObject.get("accountId").getAsString();
								modelo.setAccountId(username, accountId);
								Utilities.redirectTo(LATCH_UNPAIR);
								break;
							case 206:
								setError(getError() + i18nResolver.getText(PAIR_ERROR_206));
								break;
							case 102:
								setError(getError() + i18nResolver.getText(PAIR_ERROR_CONF));
								break;
							default:
								break;
						}
					} else {
						JsonObject jObject = pairResponse.getData();
						if(jObject != null){
							String accountId = jObject.get("accountId").getAsString();
							modelo.setAccountId(username, accountId);
							Utilities.redirectTo(LATCH_UNPAIR);
						}else{
							setError(getError() + i18nResolver.getText(PAIR_ERROR_CONF));
						}
					}
				}
			} else {
				setError(getError() + i18nResolver.getText(PAIR_ERROR_CONF));
			}
		} else {
			Utilities.redirectTo(LATCH_UNPAIR);
		}
	}

	public String getError() {
		return this.error.length() == 0 ? "" : this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
