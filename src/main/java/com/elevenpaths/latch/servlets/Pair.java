package com.elevenpaths.latch.servlets;

import javax.servlet.http.HttpServletRequest;

import webwork.action.ServletActionContext;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;
import com.google.gson.JsonObject;

public class Pair extends JiraWebActionSupport{
	
	private static final long serialVersionUID = 1L;
	private LatchModel modelo;
	private HttpServletRequest request;
	private String error;
	private Utilities latchUtilities;
	private I18nResolver i18nResolver;
	
	private final String TOKEN_ERROR_1 = "com.elevenpaths.latch.latch-plugin-jira.tokenError1";
	private final String TOKEN_ERROR_2 = "com.elevenpaths.latch.latch-plugin-jira.tokenError2";
	private final String PAIR_ERROR_206 = "com.elevenpaths.latch.latch-plugin-jira.pairError206";
	private final String PAIR_ERROR_CONF = "com.elevenpaths.latch.latch-plugin-jira.pairErrorConf";
	
	private final String LATCH_PAIR = "/secure/LatchPair.jspa";
	private final String LATCH_UNPAIR = "/secure/LatchUnpair.jspa";

	/**
	 * Constructor
	 * @param pluginSettingsFactory
	 */
	public Pair( PluginSettingsFactory pluginSettingsFactory, I18nResolver i18nResolver) {
		this.modelo = new LatchModel(pluginSettingsFactory);
		this.latchUtilities = new Utilities(pluginSettingsFactory);
		this.request = ServletActionContext.getRequest();
		this.i18nResolver = i18nResolver;
	}
	
	/**
	 * 
	 */
	@Override
	protected void doValidation() {
		this.error = "";
		
		String username = latchUtilities.getUsername();
		if(username.equals("")){
			latchUtilities.redirectToLogin();
		}else{
			if(latchUtilities.isPaired(username)){
				latchUtilities.redirectTo(LATCH_UNPAIR);
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
			setError(getError() + i18nResolver.getText(TOKEN_ERROR_1));
		} else if (!token.matches("[a-zA-Z0-9]+")) {
			setError(getError() + i18nResolver.getText(TOKEN_ERROR_2));
		} else {
			pair(token, username);
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
				
				LatchApp latch = new LatchApp(appId, secret);
				LatchResponse pairResponse = null;
				try{
					pairResponse = latch.pair(token);
				}catch(NullPointerException e){ }
				
			    com.elevenpaths.latch.Error error = pairResponse.getError();
			    
			    if(error != null){
					switch (error.getCode()) {

					case 205:
						JsonObject jObject = pairResponse.getData();
						String accountId = jObject.get("accountId").getAsString();
						modelo.setAccountId(username, accountId);
						latchUtilities.redirectTo(LATCH_UNPAIR);
						break;
					case 206:
						setError(getError()+i18nResolver.getText(PAIR_ERROR_206));
						break;
					case 102:
						setError(getError()+i18nResolver.getText(PAIR_ERROR_CONF));
						break;
					default:
						break;
					}
			    }else{
			    	JsonObject jObject = pairResponse.getData();
					String accountId = jObject.get("accountId").getAsString();
					modelo.setAccountId(username, accountId);
					latchUtilities.redirectTo(LATCH_UNPAIR);
			    }
			}else{
				setError(getError()+i18nResolver.getText(PAIR_ERROR_CONF));
				latchUtilities.redirectTo(LATCH_PAIR);
			}
		}else{
			latchUtilities.redirectTo(LATCH_UNPAIR);
		}
	}
	
	public String getError() {
		return this.error.length() == 0 ? "" : this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
