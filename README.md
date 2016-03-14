#LATCH INSTALLATION GUIDE FOR JIRA


##PREREQUISITES
 * Java version 1.6 or later.

 * Jira version 7.x.

 * To get the **"Application ID"** and **"Secret"**, (fundamental values for integrating Latch in any application), it’s necessary to register a developer account in [Latch's website](https://latch.elevenpaths.com"https://latch.elevenpaths.com"). On the upper right side, click on **"Developer area"**.


##DOWNLOADING THE JIRA PLUGIN
* When the account is activated, the user will be able to create applications with Latch and access to developer documentation, including existing SDKs and plugins. The user has to access again to [Developer area](https://latch.elevenpaths.com/www/developerArea"https://latch.elevenpaths.com/www/developerArea"), and browse his applications from **"My applications"** section in the side menu.

* When creating an application, two fundamental fields are shown: **"Application ID"** and **"Secret"**, keep these for later use. There are some additional parameters to be chosen, as the application icon (that will be shown in Latch) and whether the application will support OTP (One Time Password) or not.

* From the side menu in developers area, the user can access the **"Documentation & SDKs"** section. Inside it, there is a **"SDKs and Plugins"** menu. Links to different SDKs in different programming languages and plugins developed so far, are shown.

* Also you can download the plugin by getting the executable from our [GitHub repository](https://github.com/ElevenPaths/latch-plugin-jira"https://github.com/ElevenPaths/latch-plugin-jira") inside Releases section.


##INSTALLING THE PLUGIN IN JIRA
* Once the administrator has downloaded the plugin, it has to be added as a plugin on the administration panel in Jira. Click on **"Adds-on"** and **"Manage adds-on"**, then click on the **"Upload add-on"** link that will show a form where you can browse and select the downloaded JAR file.	

* Go to **"Configure Latch"**, inside **"Adds-on"** and introduce the **"Application ID"** and **"Secret"** previously obtained. Save the changes by clicking on **"Save Changes"**.

* From now on, on user's profile options, a new section,  called Latch, will appear. Tokens generated in the app should be introduced there.


##UNINSTALLING THE PLUGIN IN JIRA
* To remove the plugin, the administrator has to click on **"Manage adds-on"** and press the **"Uninstall"** button below the **"latch-plugin-jira"** plugin, then wait until Jira finish.


##USE OF LATCH MODULE FOR THE USERS
**Latch does not affect in any case or in any way the usual operations with an account. It just allows or denies actions over it, acting as an independent extra layer of security that, once removed or without effect, will have no effect over the accounts, that will remain with its original state.**

###Pairing a user in Jira
The user needs the Latch application installed on the phone, and follow these steps:

* **Step 1:** Log in your own Jira account and go to **"Latch"** in your profile options.

* **Step 2:** From the Latch app on the phone, the user has to generate the token, pressing on **"Add a new service"** at the bottom of the application, and pressing **"Generate new code"** will take the user to a new screen where the pairing code will be displayed.

* **Step 3:** The user has to type the characters generated on the phone into the **"Type your pairing token"** text box displayed on the web page. Click on **"Pair account"** button.

* **Step 4:** Now the user may lock and unlock the account, preventing any unauthorized access.

###Unpairing a user in Jira
* The user should access their Jira account and under the **"Latch"** section of the user's options click the **"Unpair your Latch account** button. He will receive a notification indicating that the service has been unpaired.



#### TROUBLESHOOTING ####

*A javax.net.ssl.SSLHandshakeException with a nested sun.security.validator.ValidatorException is thrown when invoking an API call.*

This exception is normally thrown when the JDK doesn't trust the CA that signs the digital certificate used in Latch's website (https://latch.elevenpaths.com). You may need to install the CA (http://www.startssl.com/certs/ca.pem) as a trusted certificate in your Atlassian's truststore (normally in /jre/lib/security/cacerts) using the keytool utility.
```
$ sudo keytool -import -trustcacerts -file ca.pem -keystore jre/lib/security/cacerts -alias "ElevenPaths"
