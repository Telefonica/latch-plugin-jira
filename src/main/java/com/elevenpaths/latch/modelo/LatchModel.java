package com.elevenpaths.latch.modelo;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class LatchModel {

	private static final String PLUGIN_STORAGE_ACCOUNTID = "com.elevenpaths.latch.plugin.accountId.";
	private static final String PLUGIN_STORAGE_APP_ID = "com.elevenpaths.latch.plugin.app_id";
	private static final String PLUGIN_STORAGE_SECRET = "com.elevenpaths.latch.plugin.secret";
	private final PluginSettingsFactory pluginSettingsFactory;
	private final PluginSettings pluginSettings;

	public LatchModel(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
	}

	// ACCOUNTID
	public String getAccountId(String username) {
		return (String) pluginSettings.get(PLUGIN_STORAGE_ACCOUNTID + username);
	}

	public void setAccountId(String username, String accountId) {
		pluginSettings.put(PLUGIN_STORAGE_ACCOUNTID + username, accountId);
	}

	public void deleteAccountId(String username) {
		pluginSettings.remove(PLUGIN_STORAGE_ACCOUNTID + username);
	}

	// APPID
	public String getAppId() {
		return (String) pluginSettings.get(PLUGIN_STORAGE_APP_ID);
	}

	public void setAppId(String appId) {
		pluginSettings.put(PLUGIN_STORAGE_APP_ID, appId);
	}

	// SECRET
	public String getSecret() {
		return (String) pluginSettings.get(PLUGIN_STORAGE_SECRET);
	}

	public void setSecret(String secret) {
		pluginSettings.put(PLUGIN_STORAGE_SECRET, secret);
	}

}
