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



