package com.elevenpaths.latch.listener;

import javax.servlet.http.HttpSession;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.elevenpaths.latch.modelo.Modelo;
import com.elevenpaths.latch.servlets.LatchApp;
import com.google.gson.JsonObject;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class Login implements InitializingBean, DisposableBean {

	private final EventPublisher eventPublisher;
	private final Modelo model;

	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 */
	public Login(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory) {
		this.eventPublisher = eventPublisher;
		this.model = new Modelo(pluginSettingsFactory);
	}

	/**
	 * Called when the plugin has been enabled.
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		System.out.println("Latch listener is initialized!");
		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * @throws Exception
	 */
	@Override
	public void destroy() throws Exception {
		// unregister ourselves with the EventPublisher
		System.out.println("Latch listener is destroyed!");
		eventPublisher.unregister(this);
	}

	/**
	 * Receives any {@code UserEvent}s sent by JIRA.
	 * @param userEvent the event passed to us
	 */
	@EventListener
	public void onUserEvent(UserEvent userEvent) {
		int eventType = userEvent.getEventType();
		
		if (eventType == UserEventType.USER_LOGIN) {
			
			User user = userEvent.getUser();
			String username = null;
			try {
				username = user.getName();
			} catch (Exception e) {
				System.err.println("Usuario == null");
				return;
			}

			if(!isPaired(username)){
				return;
			}
			status(username);
		} 
		
		if(eventType==UserEventType.USER_LOGOUT){
			redirectTo("");
		}
		
	}
	
	/**
	 * Status call to api to check if the latch is open or not
	 * @param username
	 */
	private void status(String username){
		
		String appId = model.getAppId();
		String secret = model.getSecret();
		String accountId = model.getAccountId(username);
		
		if(appId == null || secret == null || accountId == null){
			System.err.println("appId == null || secret == null || accountId == null");
			return;
		}
		
		Latch latch = new Latch(appId, secret);
		LatchResponse statusResponse = null;
		try{
			statusResponse = latch.status(accountId);
		}catch(Exception e){
			System.out.println("Error connecting with Latch."); 
		}

		com.elevenpaths.latch.Error error = statusResponse.getError();
		if (error == null) {
			// depende de la respuesta pasa o no
			JsonObject jObject = null;
			try{
				jObject = statusResponse.getData().get("operations").getAsJsonObject().get(appId).getAsJsonObject();
			}catch(NullPointerException e){
				e.printStackTrace();
			}
			
			String status = null;
			try{
				 status = jObject.get("status").getAsString();
			}catch(NullPointerException e){}
			
			if (status != null) {
				
				if (status.equals("on")) {
					System.out.println("Latch abierto de: " + username);
					String two_factor = null;
					try{
						two_factor = jObject.get("two_factor").getAsJsonObject().get("token").getAsString();
					}catch(NullPointerException e){ }
					
					if(two_factor != null){
						System.out.println("Segun factor: "+two_factor);
					}
					
				} else if (status.equals("off")) {
					System.out.println("Latch cerrado de: " + username);
					JiraWebActionSupport logout = new JiraWebActionSupport();
					HttpSession sesion = logout.getHttpSession();
					try{
						sesion.invalidate();
					}catch(IllegalStateException e){
						e.printStackTrace();
					}
				} 				
			}

		} else {
			System.out.println("Error "+error.getCode());
			switch (error.getCode()) {
			case 201:
				System.out.println("Error 201");
				model.deleteAccountId(username);
				break;
			}
		}
	}
	
	/**
     * check if the user is paired with latch
     * @param username user logged currently
     * @return if is paired or not
     */
    private boolean isPaired(String username){
    	if (model.getAccountId(username) == null){
    		return false;
    	}else{
    		return true;
    	}
    }
    
	/**
	 * Redirect to another page
	 * @param path where the user goes
	 */
	private void redirectTo(String path) {
		String nextUrl = path;
		JiraWebActionSupport redirect = new JiraWebActionSupport();
		String contextPath = redirect.getHttpRequest().getContextPath();
		if (contextPath != null) {
			nextUrl = contextPath + nextUrl;
		}
		try {
			redirect.getHttpResponse().sendRedirect(nextUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}