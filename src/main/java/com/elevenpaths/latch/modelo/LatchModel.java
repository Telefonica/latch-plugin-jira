package com.elevenpaths.latch.modelo;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class LatchModel {

    private static final String PLUGIN_STORAGE_USERS_COUNT = "com.elevenpaths.latch.plugin.usersCount";
    private static final String PLUGIN_STORAGE_USERS = "com.elevenpaths.latch.plugin.usersName";
    private static final String PLUGIN_STORAGE_ACCOUNTID = "com.elevenpaths.latch.plugin.accountId.";
    private static final String PLUGIN_STORAGE_APP_ID = "com.elevenpaths.latch.plugin.app_id";
    private static final String PLUGIN_STORAGE_SECRET = "com.elevenpaths.latch.plugin.secret";
    private final PluginSettings pluginSettings;

    /**
     * Constructor.
     *
     * @param pluginSettingsFactory object to save data
     */
    public LatchModel(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    // USER

    public void initialize() {
        pluginSettings.put(PLUGIN_STORAGE_USERS_COUNT, "0");
    }

    public int getNumUsers() {
        String users = (String) pluginSettings.get(PLUGIN_STORAGE_USERS_COUNT);
        return (users == null) ? 0 : Integer.parseInt(users);
    }

    public void addUser(String user) {
        int users = getNumUsers();
        users += 1;
        pluginSettings.put(PLUGIN_STORAGE_USERS_COUNT, String.valueOf(users));
        pluginSettings.put(PLUGIN_STORAGE_USERS + String.valueOf(users), user);
    }

    public void deleteUsers() {
        int users = getNumUsers();
        for (int i = 1; i <= users; i++) {
            String count = String.valueOf(i);
            String user = (String) pluginSettings.get(PLUGIN_STORAGE_USERS + count);
            pluginSettings.remove(PLUGIN_STORAGE_ACCOUNTID + user);
            pluginSettings.remove(PLUGIN_STORAGE_USERS + count);
        }
        pluginSettings.remove(PLUGIN_STORAGE_USERS_COUNT);
    }

    // ACCOUNTID

    /**
     * @param username user to check
     * @return the accountId of that user
     */
    public String getAccountId(String username) {
        return (String) pluginSettings.get(PLUGIN_STORAGE_ACCOUNTID + username);
    }

    /**
     * assigns an accountId to a user
     *
     * @param username  new user
     * @param accountId new accountId
     */
    public void setAccountId(String username, String accountId) {
        addUser(username);
        pluginSettings.put(PLUGIN_STORAGE_ACCOUNTID + username, accountId);
    }

    /**
     * delete the entry of an user
     *
     * @param username user to remove
     */
    public void deleteAccountId(String username) {
        int users = getNumUsers();

        for (int i = 1; i <= users; i++) {
            String count = String.valueOf(i);
            String user = (String) pluginSettings.get(PLUGIN_STORAGE_USERS + count);
            if (user.equals(username)) {
                pluginSettings.remove(PLUGIN_STORAGE_ACCOUNTID + user);
                pluginSettings.remove(PLUGIN_STORAGE_USERS + count);
                break;
            }
        }
        users -= 1;
        pluginSettings.put(PLUGIN_STORAGE_USERS_COUNT, String.valueOf(users));
    }

    // APPID

    /**
     * @return the appId value of the application which is used to configure the
     * plugin
     */
    public String getAppId() {
        return (String) pluginSettings.get(PLUGIN_STORAGE_APP_ID);
    }


    /**
     * save the appId
     *
     * @param appId the id of the application to save
     */
    public void setAppId(String appId) {
        pluginSettings.put(PLUGIN_STORAGE_APP_ID, appId);
    }

    /**
     * delete the appId
     */
    public void deleteAppId() {
        pluginSettings.remove(PLUGIN_STORAGE_APP_ID);
    }

    // SECRET
    public String getSecret() {
        return (String) pluginSettings.get(PLUGIN_STORAGE_SECRET);
    }

    /**
     * save the secret value
     *
     * @param secret the secret value to save
     */
    public void setSecret(String secret) {
        pluginSettings.put(PLUGIN_STORAGE_SECRET, secret);
    }

    /**
     * delete the secret key
     */
    public void deleteSecret() {
        pluginSettings.remove(PLUGIN_STORAGE_SECRET);
    }
}
