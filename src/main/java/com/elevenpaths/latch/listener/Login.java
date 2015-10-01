package com.elevenpaths.latch.listener;

import javax.servlet.http.HttpSession;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.LatchModel;
import com.elevenpaths.latch.util.Utilities;
import com.google.gson.JsonObject;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class Login implements InitializingBean, DisposableBean {

	private EventPublisher eventPublisher;
	private LatchModel model;

	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 * @param pluginSettingsFactory object to save data
	 */
	public Login(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory) {
		this.eventPublisher = eventPublisher;
		this.model = new LatchModel(pluginSettingsFactory);
	}

	/**
	 * Called when the plugin has been enabled.
	 * 
	 * @throws Exception
	 *             error in afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * 
	 * @throws Exception
	 *             error in destroy()
	 */
	@Override
	public void destroy() throws Exception {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
	}

	@EventListener
	public void onUserEvent(UserEvent userEvent) {
		int eventType = userEvent.getEventType();

		switch (eventType) {
		case UserEventType.USER_LOGIN:
			User user = userEvent.getUser();
			String username = null;
			if (user != null) {
				username = user.getName();
			} else {
				return;
			}

			if (!Utilities.isPaired(username, model)) {
				return;
			}
			status(username);
			break;
		case UserEventType.USER_LOGOUT:
			Utilities.redirectTo("");
			break;
		}
	}

	/**
	 * Status call to api to check if the latch is open or not
	 * 
	 * @param username
	 *            the user who log in
	 */
	private void status(String username) {

		String appId = model.getAppId();
		String secret = model.getSecret();
		String accountId = model.getAccountId(username);

		if (appId == null || secret == null || accountId == null) {
			return;
		}

		LatchApp latch = new LatchApp(appId, secret);
		LatchResponse statusResponse = null;
		try {
			statusResponse = latch.status(accountId);
		} catch (NullPointerException e) {
			return;
		}

		com.elevenpaths.latch.Error error = statusResponse.getError();

		if (error == null) {

			if (statusResponse != null) {

				if (statusResponse.getData() != null) {

					JsonObject jsonObject = (JsonObject) statusResponse.getData();
					if (jsonObject.has("operations")) {

						JsonObject operations = jsonObject.getAsJsonObject("operations");
						JsonObject applicationId = null;

						if (operations.has(appId)) {

							applicationId = operations.getAsJsonObject(appId);
							String status = null;

							if (applicationId.has("status")) {
								status = applicationId.get("status").getAsString();

								if (status.equals("off")) {

									JiraWebActionSupport logout = new JiraWebActionSupport();
									HttpSession sesion = logout.getHttpSession();
									try {
										sesion.invalidate();
									} catch (IllegalStateException e) {
										e.printStackTrace();
									}


								}  /*
									 * else if (status.equals("on")) {

									 * JsonObject two_factor = null;
									 * 
									 * if (applicationId.has("two_factor")) {
									 * two_factor =
									 * applicationId.getAsJsonObject(
									 * "two_factor"); String token = null;
									 * 
									 * if (two_factor.has("token")) { token =
									 * two_factor.get("token").getAsString();
									 * 
									 * } } }
									 */
							}
						}
					}
				}
			}

		} else {
			switch (error.getCode()) {
			case 201:
				model.deleteAccountId(username);
				break;
			}
		}
	}

}