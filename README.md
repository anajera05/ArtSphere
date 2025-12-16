
<!-- PROJECT LOGO -->
<br />
<div align="center">
  <!-- <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a> -->

  <h3 align="center">ArtSphere</h3>

  <p align="center">
  An accessible art world for artists of all levels — trade artworks, discover trends, share creativity, and connect locally.  </p>
</div>

<!-- ABOUT THE PROJECT -->
## App Functionality & Demo




### Why Artsphere 
Our app facilitates the exchange and collaboration of artwork. It allows users to sell their own creations while also discovering and purchasing art from other artists. By providing a platform for both buying and selling, the app encourages creative collaboration and helps artists reach a wider audience.

### Core Features: 
* User Authentication:
  * Users can create their accounts and log in with the same credentials
  * Passwords must be +6 characters
  * User can sign out of their account and log back in
* Interactive Feed:
  * News Integration: displays recent news and trends from the art industry using the NYTimes API.
  * Gallery: Ability to view all listed artwork and filter based on preexisting categories
  * Camera: Quick action to make a new listing from taking a photo
  * Search: Search for specific art pieces and profiles 
* Map:
  * See pins for events in your area
* Events:
  * Add events in your area
  * Add yourself to different events  
* Navigation :
  * Navigate between the 4 main screens: Home, Map, Inbox, and Profile
* Profile:
  * View your profile picture and the number of listings 
  * View Posted Art pieces 
  * View saved Art pieces 
  * Add listings
* Add listing:
  * Add listing from Camera Quick Action or the Add button on Profile 
  * If no price, contact seller 
* Inbox:
  * Start chats with artists from their art pieces
  * Chat with multiple artists in the app
 
### Completeness & Live Demo Quality

Video demo:
[video1570142258.mp4.zip](https://github.com/user-attachments/files/24197434/video1570142258.mp4.zip)


## Technical Implementation

### Integration: Newyork Times API & Google map API & sensor 
1. Newyork Times
2. Google map 
3. camera sensor 

### Architecture (MVVM structure with StateFlow and unidirectional data flow) & Jetpack Compose Usage: UI 

[Final Report.pdf](https://github.com/user-attachments/files/24200581/Final.Report.pdf)






## AI Use in Development
Tools Used: ChatGPT, Gemini

Where and How Used: 

* Camera: Asked questions about AndroidView, factory, and ImageCapture in CameraX to understand how to set up the camera preview and take photos in a Jetpack Compose application.
  * Example prompt: What is AndroidView and factory, and why is it used in CameraX Compose?
* Map: Used ChatGPT to learn how to customize map pins in my Android project, including loading images from URLs with Coil, converting them to bitmaps, cropping them into circular icons, and applying them as custom markers in Google Maps Compose.
  * Example prompt: Explain how to load an image from a URL in Jetpack Compose, convert it into a bitmap, make it circular, and use it as a custom marker on Google Maps. I want to understand how each step works.
* Firebase Storage & Firestore Setup:
  * Using Chat helped me rewrite the rules so authenticated users could upload their own profile photos while preventing unauthorized access
* Creating Previews: To help create more complex UI, we used Gemini to update our files to allow us to render previews that would not break our code
  * Example prompt: Why does the preview for X not render?
* Test cases:
  * Helped with setting up mock FirebaseAuth and FirebaseUser for testing offline authentication and session persistence, and provided example structures for unit tests, which served as a basis for artwork and event input validation
* KDoc:
  * Helped with generating KDoc comments for functions and classes, which were manually checked and reviewed to ensure accuracy and clarity.

Analysis of Helpfulness and Limitations:
* Helpfulness:
  * Provided clear explanations, step-by-step breakdowns, and example code snippets that helped me understand the concepts and apply them directly in my project. It helped clarify how Jetpack Compose interacts with traditional Android Views, and how to manipulate camera and map features effectively.
  * Chatgpt was helpful in speeding up the debugging process and helping me understand the deeper logic behind Firebase and Jetpack Compose state management.
  * Sped up the process of UI development without destroying existing code 

* Limitations:
  * Some answers were high-level and required me to verify or adapt the examples to my project context. For instance, actual implementation required adjustments to the codebase, such as modifying the Firebase data items to include longitude and latitude.
  * Some code required adjustments to fit my actual project structure. It sometimes assumed best-practice architecture, which required me to adjust my existing project design to match (e.g., hoisting ViewModels to the NavHost level).
  * Need to test code to ensure code is still functional 

Corrections Required / Demonstration of Understanding:

While using ChatGPT, I verified its suggestions by checking official documentation, testing the code in my project, and interacting with the emulator to ensure each component worked correctly.




## Testing Strategy
The app was tested using a combination of debugging techniques, manual verification, and automated unit/UI tests. Debugging included the use of Log.d and Log.e for logging events and errors, try/catch blocks for exception handling, and managing errors through UI state to provide user feedback.

Manual testing verified core components on the emulator:

* Camera: capturing and uploading artwork

* Map: displaying locations and coordinates accurately

* Saved Artwork: loading, viewing, and deleting items

* Navigation: ensuring smooth transitions between screens

Automated unit/UI tests were added to verify key composable components and flows:
* Offline/Auth Functionality: Tested user session persistence, login/logout state transitions, and access to user data without requiring a network connection.
* Artwork Input Validation: Ensured that all required fields are completed, emails are valid, price fields are correctly formatted, and optional fields behave as expected. Edit mode scenarios were also tested to handle missing images appropriately.
* Event Input Validation: Verified that required fields are filled, time and date inputs are valid, participant limits are enforced, and optional fields such as images and categories are handled correctly. Edge cases for capacity and time formatting were also tested.

This approach ensured that each feature functions correctly and that the app behaves as expected under different scenarios.





<!-- build/run instructions -->
## How to build and run the application 
1. ```git clone https://github.com/anajera05/ArtSphere```
2. ```cd Artsphere```
3. Add MAPS_API_KEY=Key to local.properties, where Key is your Google Maps API key.
4. factor the json file: [google-services.json.zip](https://github.com/user-attachments/files/24200465/google-services.json.zip)
5. Run emulator
6. run application

<!-- USAGE EXAMPLES -->
## Users 

Those who love art and/or want to sell!


### Top contributors:

<a href="https://github.com/anajera05/ArtSphere/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=anajera05/ArtSphere" />
</a>

