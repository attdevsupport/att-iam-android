AT&T In-App Messaging (IAM) SDK for Android

Sample App Install Guide

1.	Browse to the folder where you extracted the IAM SDK files.
2.	Copy the library iamsdk.jar to the 'libs' folder of the sample app.
3.	Open the sample app project in Android Studio.
4.	Update the following information in SpeechConfig.java (you can get this from
    your app registration on the developer site). 
•	appKey
•	secret
•	redirect url as registered in AT&T developer portal app.

IAM service allows a developer to send SMS or MMS message on behalf of an AT&T
mobile subscriber.

The In-App Messaging SDK is an android library for natively consuming the
RESTful resources for AT&T's In-App Messaging API.

Quick Start
--------------
Sending a message can be as easy as:

	//Initialize the Manager
	iamManager = new IAMManager(...);
	
	//Send a Message
	iamManager.SendMessage(addresses, "This is a test message",null, true, null);

Requirements
- Git
- Android SDK

Overview
--------
There are two main components to the library :

- A  manager --HTTP client-- to handle all the In-App Messaging requests.
- A set of request wrappers for the HTTP requests to access the API's resources.

Request Wrappers

The SDK provides the wrappers for the  actual REST resources that are accessed by the application.The main request parameters are exposed.
	
	APICreateMessageIndex
	APISendMessage
	APIGetMessageList
	APIGetMessage
	APIGetMessageContent
	APIGetDelta
	APIUpdateMessages
	APIUpdateMessage
	APIDeleteMessages
	APIDeleteMessage
	APIGetMessageIndexInfo
	APIGetNotificationConnectionDetails
	
Request Management

The networking layer is abstracted away by providing the manager--IAMManager-- to handle the request made by the application.
The IAMManager creates the actual HTTP requests and allows the developer to define success and error callbacks using listeners

	iamManager = new IAMManager(......new sendMessageListener());
	
	private class sendMessageListener implements ATTIAMListener {
	@Override
		public void onSuccess(Object arg0) {
		//Your code  for success
		}
	@Override
		public void onError(Object arg0) {
		//Your code for error
		}


	
Using the In-App messaging SDK in your app
------------------------------------------
Using the Binary
- Add the files under InAppMessaging
- Link the static library IAMSDK by configuring the build Path

Using the Sources
- Open the Eclipse 
- Download the Android SDK
- Create a New Android Project in the workspace
- Link the IAMSDK library to the project
- Add the Activity UserConsentActivity in the AndroidManifest file

You can start building your APP with In-App Messaging support

Usage
-----
Initialize the app client
	
	final String domainName = "https://api.att.com";
		
	// Enter the value from 'App Key' field
	final String clientId = " appId";

	// Enter the value from 'Secret' field
	final String clientSecret = "appSecret";

Add the Activity UserConsentActivity in the AndroidManifest file

	<activity
            android:name="com.att.api.consentactivity.UserConsentActivity"
            android:label="@string/title_activity_user_consent" >
        </activity>


Create service for requesting an OAuth token
	OAuthService osrvc = new OAuthService(domainName, appId, appSecret);
Start the UserconsentActivity for result by passing the domainName, appId and appSecret as extras to get the oAuthCode on the onActivityResult callback.

	Intent i = new Intent(this,com.att.api.consentactivity.UserConsentActivity.class);
	i.putExtra("domainName", domainName);
	i.putExtra("appId", appId);
	i.putExtra("appSecret", appSecret);
	i.putExtra("appScope", appScope);
	i.putExtra("customParam", customParam);
	startActivityForResult(i, REQUEST_CODE);

Initialize the static variables in IAMManager:
	IAMManager.SetApiFqdn(Config.fqdn);
	IAMManager.SetTokenUpdatedListener(new TokenUpdatedListener(getApplicationContext()));
	
Obtain the token by passing the oAuthCode in onActivityResult
						 
	osrvc.getOAuthToken(oAuthCode,new listener());				  

OnSuccess call back of the token listener, you can set the access token and (optionally) send message as follows:

	IAMManager.SetCurrentToken(adjustedAuthToken);		
	IAMManager iamManager = new IAMManager(domainName, token, new listener());
	iamManager.SendMessage(addresses, "This is a test message",null, false, null);

OnSuccess call back of the SendMessage Listener, you can add your code
	
	private class sendMessageListener implements ATTIAMListener {
	@Override
		public void onSuccess(Object arg0) {
		//Your code  for success
		}
	@Override
		public void onError(Object arg0) {
		//Your code for error
		}

In-App Messaging SDK is compatible with Android version Version 4.0.4 and up.
	 

Legal Disclaimer
This document and the information contained herein (collectively, the "Information") is provided to you (both the individual receiving this document and any legal entity on behalf of which such individual is acting) ("You" and "Your") by AT&T, on behalf of itself and its affiliates ("AT&T") for informational purposes only. AT&T is providing the Information to You because AT&T believes the Information may be useful to You. The Information is provided to You solely on the basis that You will be responsible for making Your own assessments of the Information and are advised to verify all representations, statements and information before using or relying upon any of the Information. Although AT&T has exercised reasonable care in providing the Information to You, AT&T does not warrant the accuracy of the Information and is not responsible for any damages arising from Your use of or reliance upon the Information. You further understand and agree that AT&T in no way represents, and You in no way rely on a belief, that AT&T is providing the Information in accordance with any standard or service (routine, customary or otherwise) related to the consulting, services, hardware or software industries. 
AT&T DOES NOT WARRANT THAT THE INFORMATION IS ERROR-FREE.  AT&T IS PROVIDING THE INFORMATION TO YOU "AS IS" AND "WITH ALL FAULTS."  AT&T DOES NOT WARRANT, BY VIRTUE OF THIS DOCUMENT, OR BY ANY COURSE OF PERFORMANCE, COURSE OF DEALING, USAGE OF TRADE OR ANY COLLATERAL DOCUMENT HEREUNDER OR OTHERWISE, AND HEREBY EXPRESSLY DISCLAIMS, ANY REPRESENTATION OR WARRANTY OF ANY KIND WITH RESPECT TO THE INFORMATION, INCLUDING, WITHOUT LIMITATION, ANY REPRESENTATION OR WARRANTY OF DESIGN, PERFORMANCE, MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, OR ANY REPRESENTATION OR WARRANTY THAT THE INFORMATION IS APPLICABLE TO OR INTEROPERABLE WITH ANY SYSTEM, DATA, HARDWARE OR SOFTWARE OF ANY KIND. AT&T DISCLAIMS AND IN NO EVENT SHALL BE LIABLE FOR ANY LOSSES OR DAMAGES OF ANY KIND, WHETHER DIRECT, INDIRECT, INCIDENTAL, CONSEQUENTIAL, PUNITIVE, SPECIAL OR EXEMPLARY, INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF BUSINESS PROFITS, BUSINESS INTERRUPTION, LOSS OF BUSINESS INFORMATION, LOSS OF GOODWILL, COVER, TORTIOUS CONDUCT OR OTHER PECUNIARY LOSS, ARISING OUT OF OR IN ANY WAY RELATED TO THE PROVISION, NON-PROVISION, USE OR NON-USE OF THE INFORMATION, EVEN IF AT&T HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH LOSSES OR DAMAGES.

ChangeLog
=========
Release 1.1.1 - Mar 27 2014

Initial release of IAM SDK.

Relase 1.2.1 - Dec 2 2014

Added support for AT&T's OAuth 2.0 version 4 impementation for RememberMe, Refresh Token and Revoke Token functionality.

Release 1.3 - June 2015

Converted from ADT to Android Studio. Removed the Address Book (AAB) service.