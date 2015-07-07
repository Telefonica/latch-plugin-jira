package com.elevenpaths.latch.modelo;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;


public class LatchModel {

	private static final String PLUGIN_STORAGE_ACCOUNTID = "com.elevenpaths.latch.plugin.accountId.";
	private static final String PLUGIN_STORAGE_APP_ID = "com.elevenpaths.latch.plugin.app_id";
	private static final String PLUGIN_STORAGE_SECRET = "com.elevenpaths.latch.plugin.secret";
	private static final String PLUGIN_STORAGE_TOKEN = "com.elevenpaths.latch.plugin.otp.";
	private static final String PLUGIN_STORAGE_ATTEMPTS = "com.elevenpaths.latch.plugin.attempts.";
	private final PluginSettingsFactory pluginSettingsFactory;
	private final PluginSettings pluginSettings;

	public LatchModel(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
	}

	// ACCOUNTID
	
	/**
	 * 
	 * @param username
	 * @return the accountId of that user
	 */
	public String getAccountId(String username) {
		return (String) pluginSettings.get(PLUGIN_STORAGE_ACCOUNTID + username);
	}

	/**
	 * assigns an accountId to a user
	 * @param username
	 * @param accountId
	 */
	public void setAccountId(String username, String accountId) {
		pluginSettings.put(PLUGIN_STORAGE_ACCOUNTID + username, accountId);
	}

	/**
	 * delete the entry of an user
	 * @param username
	 */
	public void deleteAccountId(String username) {
		pluginSettings.remove(PLUGIN_STORAGE_ACCOUNTID + username);
	}

	// APPID

	/**
	 * 
	 * @return the appId value of the application which is used to configure the plugin
	 */
	public String getAppId() {
		return (String) pluginSettings.get(PLUGIN_STORAGE_APP_ID);
	}

	/**
	 * save the appId 
	 * @param appId
	 */
	public void setAppId(String appId) {
		pluginSettings.put(PLUGIN_STORAGE_APP_ID, appId);
	}

	// SECRET

	/**
	 * 
	 * @return the secret value which is used to configure the plugin
	 */
	public String getSecret() {
		return (String) pluginSettings.get(PLUGIN_STORAGE_SECRET);
	}

	/**
	 * save the secret value
	 * @param secret
	 */
	public void setSecret(String secret) {
		pluginSettings.put(PLUGIN_STORAGE_SECRET, secret);
	}
	
	// OTP

	public String getToken(String username) {
		return (String) pluginSettings.get(PLUGIN_STORAGE_TOKEN + username);
	}

	public void setToken(String username, String token) {
		pluginSettings.put(PLUGIN_STORAGE_TOKEN + username, token);
	}
	
	public void deleteToken(String username){
		pluginSettings.remove(PLUGIN_STORAGE_TOKEN+username);
	}
	
	// ATTEMPTS
	
	public void getAndIncreaseAttempts(String username) {
		int attempts = Integer.parseInt((String) pluginSettings.get(PLUGIN_STORAGE_ATTEMPTS + username));
		attempts += 1;
		pluginSettings.put(PLUGIN_STORAGE_ATTEMPTS + username, attempts);
	}
	
	public void resetAttempts(String username) {
		pluginSettings.remove(PLUGIN_STORAGE_ATTEMPTS + username);
	}

}
