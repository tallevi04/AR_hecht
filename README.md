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
