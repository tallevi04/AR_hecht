# Hecht AR APP
## Architecture Diagram 
![image](https://user-images.githubusercontent.com/62984042/194009909-507f0b4d-b79c-4de1-8025-3d00967e56eb.png)
## Application Flow
### Admin
![image](https://user-images.githubusercontent.com/62984042/194010502-2a990e97-5920-4fbd-96b1-a410c9a54f3b.png)
### User
![image](https://user-images.githubusercontent.com/62984042/194010580-9202f6a0-f81d-41d7-8f7c-4fcfaabeb991.png)
## Project Structure
### Application Structure
![image](https://user-images.githubusercontent.com/62984042/194011189-b2488e1f-80c2-49f2-a16a-2fb1e6f103ac.png)

Note : All the customization will be done In the work directory. Helper directory contains database helpers, Rendering, cloud Helpers.
### UI / Layout
##### Application UI / layout Related Files under work directory.
![image](https://user-images.githubusercontent.com/62984042/194011388-6a3c35f6-0068-48e7-8b62-8105cdd10ebc.png)

##### Customize UI of the application according to your UI structure.
![image](https://user-images.githubusercontent.com/62984042/194011678-6333b571-033c-40d5-a2f9-554982d6aebb.png)

## ARCORE
### Getting all the necessary permissions to host Anchor.
![image](https://user-images.githubusercontent.com/62984042/194012581-42aba99b-3a0c-4860-941a-e26b7ee1a7cd.png)

### How to Enable ARcore Cloud Anchor API
##### •	Go to the [ARCore Cloud Anchor API](https://console.cloud.google.com/apis/library/arcorecloudanchor.googleapis.com) service page
##### •	In the projects list, select a project or create a new one.
##### •	Click Enable.
![image](https://user-images.githubusercontent.com/62984042/194013910-152b492f-6c7c-481e-8a9f-bb2b16eeaf53.png)

### How to Set up OAuth
##### •	Go to the [Google Cloud Platform Console](https://console.cloud.google.com/).
##### •	From the projects list, select a project.
##### •	If the APIs & services page isn't already open, open the console left side menu and select APIs & Services.
##### •	On the left, click Credentials.
##### • Click Create Credentials, then select OAuth client ID.
##### • Fill in the following values: Application type: Android , Package name: com.hecht.ar
##### •	Retrieve your debug signing certificate fingerprint: 
##### 1. In your Android Studio project, open the Gradle toolpane.
##### 2. In cloud-anchors > work > Tasks > android, run the signingReport task.
![image](https://user-images.githubusercontent.com/62984042/194016019-d08df6a2-b257-4e93-868e-05ffb6dd5b3f.png)
##### 3. Copy the SHA-1 fingerprint into the SHA-1 certificate fingerprint field in Google Cloud.

### How to Configure ARcore Session
##### Here we will modify the app to create a hosted anchor on a user tap instead of a regular one. To do that, you will need to configure the ARCore Session to enable Cloud Anchors.
![image](https://user-images.githubusercontent.com/62984042/194016905-1bb0eeb8-14b3-413e-997e-43920969bb75.png)
### How to Create Hosted Anchor
##### Here we will create a hosted anchor that will be uploaded to the ARCore Cloud Anchor Service.
##### In CloudAnchorFragment.java two helper classes are called these would be responsible to create hosted anchor.
![image](https://user-images.githubusercontent.com/62984042/194017022-636a95a5-bb04-4112-ba9e-60152022dfd6.png)
##### •	SnackbarHelper.java ( helper/src/main/java)
##### •	CloudAnchorManager.java ( helper/src/main/java)
##### •	OnDrawFrame() Method will handle one tap per frame.
![image](https://user-images.githubusercontent.com/62984042/194017141-15f470e6-8a18-48e9-bc06-6325e39ab13b.png)
##### And in this method CloudAnchorManager.update () Obtain the current frame from ARSession and update the cloud anchor.

### When app was Hosting Anchor
##### User will see the app generated message at the bottom that the Anchor is being Hosted after Successfull configuration.
![image](https://user-images.githubusercontent.com/62984042/194017708-da8d50c5-f69b-4592-9a28-7a8f48006188.png)

### Hosted Anchor Cross check
##### If user was prompted with Error message See ( Possible Errors below) while hosting.
##### The app will generate a short code(ID) if the anchor was hosted Successfully. And user can resolve the Anchor by entering short Code.
![image](https://user-images.githubusercontent.com/62984042/194017908-43f8614d-0173-4b6a-a1df-7caf6cc2a18b.png)

### How to Clear Anchor from scene
##### onClearButtonPressd() is used to clear the anchor from the scene when it was rendered
![image](https://user-images.githubusercontent.com/62984042/194017989-1f4257a4-285a-41ed-bd09-bd803401a9a1.png)

## Firebase
### How to work with firebase for sharing hosted anchor to Multiple devices
##### SignIn to Firebase with your Gmail (Keep the Browser open to see if the project was created from the Android Studio)
##### •	Open the code in Android Studio and head to Tools -> Firebase Panel will pop from the side.
##### •	Select Realtime Database
##### •	Connect to Firebase
##### •	Select Work Module
##### •	Press OK ( Now you will see connect dialog)
##### •	Now add real time database 
![image](https://user-images.githubusercontent.com/62984042/194018569-7d4f97b4-cb69-4bab-80be-ac604572cb66.png)
##### •	In the dialog that pops up, select work from the Target module drop-down, then click Accept Changes. This will add google-services.json file in work directory.
![image](https://user-images.githubusercontent.com/62984042/194018623-22b675a5-1c03-430d-aebe-bd9ca7c973c2.png)
##### •	In Work module build.gradle file find and remove (xxxx from the) dependencies {implementation 'com.google.firebase:firebase-database:xxxx'}

## 3D Model Randering
##### This class is used in both Admin and Client side and it is responsible for rendering our model in the real world and it will be responsible for shading of the 3D Models you can change its material properties here.
![image](https://user-images.githubusercontent.com/62984042/194019362-98d45990-397f-48d7-8ba5-353eb03e8dc0.png)
### Project location of our 3D Models(helper/src/main/assets/models).
![image](https://user-images.githubusercontent.com/62984042/194019459-9c579cf2-3e04-44ec-908b-926fad14523e.png)

## Methods and classed with application flow
### Splash Screen
![image](https://user-images.githubusercontent.com/62984042/194028556-a315f8b6-2216-4b70-bc47-d5be39ea4cf0.png)
##### User will see a splash screen when entering the application
##### • Make sure you are connected to the internet
##### •	Now our SplashFragment.java  is triggered.
##### •	This java class will inflate the layout fragment_splash.xml which contains all the UI/UX design of the screen. It will also trigger animations like(fade in) implemented on the logo.
##### •	onCreate method is called when the fragment instance is created each time.

### Home Screen/Menu
![image](https://user-images.githubusercontent.com/62984042/194029248-1dbe1a17-1510-4be6-aa10-f9f1523874ac.png)
##### •	User/Admin will see the Menu to login in.
##### •	Admin is password protected and the password is stored in the firebase 
##### •	User can enter without any restriction.
##### •	HomeFragment.java is the class for home screen it will get/inflate fragment_home.xml which is the UI for homescreen.
##### •	onClickListener() methods are used for the client choice, when pressed it will launch the next fragment or screen for the user or admin.
##### •	CustomDialog() is used to get Admin credentials

### Admin Hosting Anchor
![image](https://user-images.githubusercontent.com/62984042/194029668-f2f27311-e318-4339-839a-500b48242d59.png)
##### • Now here Application will ask for camera permissions and detect surface
##### •	onResume() method is used to check if the device has ARcore functionality installed(if not download it from Google play store).
##### •	In case you wonder what is GLES( OpenGL is a cross-platform graphics API that specifies a standard software interface for 3D graphics processing hardware. OpenGL ES is a flavor of the OpenGL specification intended for embedded devices.)
##### •	onSurfaceCreated() method is used to show 3D model and shadow configurations.
##### •	onDrawFrame() Frames are being controlled so that you only see the recently created frames it works on camera matrix, projection matrix, tracking plane surface(planeRenderer is a used for drawing 3D model and its shadows after surface detection)
##### •	handleTap() will only restrict the application from creating multiple models and only single tap will work to Host the anchor
##### •	onHostedAnchorAvailable() if every thing goes will from permessions to surface detection this method will host the generate the shortcode itself then push the anchor to the firebase.
##### •	onResolveButtonPressed() this method is used by the Admin to get the hosted anchor from the firebase using the short code.
#### Helper Classes Used to handle different Actions on User and Admin Side 
##### •	CamerPermissionHelper.java from Helper directory will handle permission related task.
##### •	CloudAnchorManager.java from Helper directory is a helper class to handle all the Cloud Anchors logic, and add a callback-like mechanism on top of the existing ARCore API.
##### •	DisplayRotationHelper.java is a Helper class to track the display rotations. In particular, the 180 degree rotations are not notified by the onSurfaceChanged() callback, and thus they require listening to the android display events.
##### •	FirebaseManager.java is used to store cloud Anchor ID’s.
##### •	SnackbarHelper.java is a helper for resolving error messages as these messages will appear on the bottom of the screen. It is Useful for notifying errors, where no further interaction with the activity is possible.
##### •	StorageManager.java is really important to understand as it is storing the Cloud Anchor ID’s on the local device.
##### •	TapHelper.java will work as intermediate to detect taps using Android GestureDetector and pass the taps between UI thread and render thread. 
##### •	TrackingStateHelper.java it will show all errors relate to person movement and enviornment both for admin and user.

### Dialog box for hosting anchor for Admin
![image](https://user-images.githubusercontent.com/62984042/194031135-edc1f41a-2591-4746-a784-776bd01cc6b2.png)
##### •	Admin will add Name, Description, URL to the Hosted anchor so that when the user will augment the model he can see the Description and also check the Website link.
##### •	Dialog.java will handle all the functionality for dialog box.
##### •	shared_cloud_anchors that’s the Firebase Database table where the Hosted anchors are Stored.

### Resolve Hosted Anchor( User )
![image](https://user-images.githubusercontent.com/62984042/194031525-e9bb2156-10c7-42c7-a4ab-77f25fc1d683.png)
##### •	Application will ask the user to enter the short code to resolve the anchor.
##### •	Application will perform same steps to from permissions to surface detection to showing 3D models on its respective coordinates stored on the cloud.
##### •	UserFragment.Java will perform this functionality.
##### •	ResolveDialogFragment.java from the helper directory will apply validation for numeric digits for short code

### Resolved Anchor Details
![image](https://user-images.githubusercontent.com/62984042/194031833-07006a2c-57ef-47fd-a0de-074f3256b92d.png)
##### •	Anchor is Resolved according to its short code
##### •	DetailsFragment.Java is handling the functionality for this box.
##### •	Url loader will load the url when view more is pressed and the user will be redirected to the website link.

## Possible Error’s and How to Resolve them

1)	**ERROR_CLOUD_ID_NOT_FOUND** Resolving failed because the ARCore Cloud Anchor API could not find the provided Cloud Anchor ID.
2)	**ERROR_HOSTING_DATASET_PROCESSING_FAILED** Hosting failed because the server could not successfully process the dataset for the given anchor. Try again after the device has gathered more data from the environment.
3)	**ERROR_HOSTING_SERVICE_UNAVAILABLE** The ARCore Cloud Anchor API was unreachable. This can happen for a number of reasons. The device might be in airplane mode or may not have a working Internet connection. The request sent to the server might have timed out with no response. There might be a bad network connection, DNS unavailability, firewall issues, or anything else that might affect the device's ability to connect to the ARCore Cloud Anchor API.
4)	**ERROR_INTERNAL** A hosting or resolving task for this anchor finished with an internal error. The app should not attempt to recover from this error.
5)	**ERROR_NOT_AUTHORIZED** The authorization provided by the application is not valid. The Google Cloud project may not have enabled the ARCore Cloud Anchor API, or the operation you are trying to perform is not allowed. If using API key authorization: the API key in the manifest is invalid, unauthorized or missing. It may also fail if the API key is restricted to a set of apps not including the current one.
a.	If using keyless authorization: you have failed to create an OAuth client.
b.	Google Play Services isn't installed, is too old, or is malfunctioning for some reason (e.g. services killed due to memory pressure).
6)	**ERROR_RESOLVING_SDK_VERSION_TOO_NEW**	The Cloud Anchor could not be resolved because the SDK version used to resolve the anchor is newer than, and thus incompatible with, the version used to host it.
7)	**ERROR_RESOLVING_SDK_VERSION_TOO_OLD** The Cloud Anchor could not be resolved because the SDK version used to resolve the anchor is older than, and thus incompatible with, the version used to host it.
8)	**ERROR_RESOURCE_EXHAUSTED** The application has exhausted the request quota allotted to the given Google Cloud project. You should request additional quota for the ARCore Cloud Anchor API for your project from the Google Developers Console.
9)	**NONE** The anchor is purely local. It has never been hosted using hostCloudAnchor(), and has not been resolved using resolveCloudAnchor().
10)	**SUCCESS** A hosting or resolving task for this anchor completed successfully.
11)	**TASK_IN_PROGRESS** A hosting or resolving task for the anchor is in progress. Once the task completes in the background, the anchor will get a new cloud state after the next call to Session.update().





