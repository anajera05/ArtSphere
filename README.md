
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
## About The Project

Our app facilitates the exchange and collaboration of artwork. It allows users to sell their own creations while also discovering and purchasing art from other artists. By providing a platform for both buying and selling, the app encourages creative collaboration and helps artists reach a wider audience.

Features: 
* User Authentication (Completed):
  * Users can create their accounts and log in with the same credentials
  * Passwords must be +6 characters
  * User can sign out of their account and log back in
* Interactive Feed:
  * News Integration (Completed): displays recent news and trends from the art industry using the NYTimes API.
  * Gallery (Completed): Ability to view all listed artwork and filter based on preexisting categories
  * Camera (Completed): Quick action to make a new listing from taking photo
  * Search (WIP): Search for specific art pieces and profiles 
* Map:
  * See pins for artworks being sold in the area
  * Add new pins 
* Navigation (Completed):
  * Navigate between the 4 main screens: Home, Map, Inbox, and Profile
* Profile:
  * View your profile picture and the number of listings (Completed)
  * View Posted Art pieces (Completed)
  * View saved Art pieces (Completed)
  * Settings page (WIP): log out function, ability to edit username and contact information
  * Add listings (Completed)
* Add listing:
  * Add listing from Camera Quick Action or the Add button on Profile (Completed)
  * If no price, contact seller (Completed)
  * Choose if you want this piece to be featured on the map (WIP)
* Transactions (WIP):
  * View Art pieces in your cart
  * In-App Purchasing
  * View previously bought items
  * View items sold
 
* Stretch Features:
  * In-App Messaging
  * Add art events in the map 


<!-- build/run instructions -->
## How to build and run the application 
1. ```git clone https://github.com/anajera05/ArtSphere```
2. ```cd Artsphere```
3. Add MAPS_API_KEY=Key to local.properties, where Key is your Google Maps API key.
4. Run emulator
5. run application

<!-- USAGE EXAMPLES -->
## Users 

Those who love art and/or want to sell!


<!-- APIs Used -->
## Planned API  

Google Maps

New York Times API

## Navigation routes:

| Screen | Route | Parent |
|--------|-------|--------|
| Login/Sign Up | `login` | Root |
| Login Screen | `login` | Root |
| Sign Up Screen | `signup` | Root |
| Bottom Navigation | `main` | Root |
| Home Screen | `home` | Bottom Nav |
| Map Screen | `map` | Bottom Nav |
| Shopping Cart | `inbox` | Bottom Nav |
| Profile Screen | `profile` | Bottom Nav |
| Camera Screen | `camera` | Any Main Screen |
| Upload Artwork | `upload_artwork` | Camera/Map/Profile |
| Art Detail | `artwork_detail` | Any Artwork |
| My Artworks | `my_artwork` | Profile |
| Saved Artworks | `saved_artwork` | Profile |
| Settings | `settings` | Profile |




## Testing Strategy
The app was tested using a combination of debugging techniques, manual verification, and automated unit/UI tests. Debugging included the use of Log.d and Log.e for logging events and errors, try/catch blocks for exception handling, and managing errors through UI state to provide user feedback.

Manual testing verified core components on the emulator:

* Camera: capturing and uploading artwork

* Map: displaying locations and coordinates accurately

* Saved Artwork: loading, viewing, and deleting items

* Navigation: ensuring smooth transitions between screens

Automated unit/UI tests were added to verify key composable components and flows:
* Login/Sign-in Screen: ensured users can type in email and password fields and that the login button is enabled
* Artwork Upload Flow: verified that users can input and submit artwork details through the UI

This approach ensured that each feature functions correctly and that the app behaves as expected under different scenarios.

## AI Usage
Tools Used: ChatGPT, Gemini

Where and How Used: 

* Camera: Asked questions about AndroidView, factory, and ImageCapture in CameraX to understand how to set up the camera preview and take photos in a Jetpack Compose application.
  * Example prompt: What is AndroidView and factory, and why is it used in CameraX Compose?
* Map: Used ChatGPT to learn how to customize map pins in my Android project, including loading images from URLs with Coil, converting them to bitmaps, cropping them into circular icons, and applying them as custom markers in Google Maps Compose.
  * Example prompt: Explain how to load an image from a URL in Jetpack Compose, convert it into a bitmap, make it circular, and use it as a custom marker on Google Maps. I want to understand how each step works.
* Firebase Storage & Firestore Setup:
  * Using Chat helped me rewrite the rules so authenticated users could upload their own profile photos while preventing unauthorized access

Analysis of Helpfulness and Limitations:
* Helpfulness:
  * Provided clear explanations, step-by-step breakdowns, and example code snippets that helped me understand the concepts and apply them directly in my project. It helped clarify how Jetpack Compose interacts with traditional Android Views, and how to manipulate camera and map features effectively.
  * Chatgpt was helpful in speeding up the debugging process and helping me understand the deeper logic behind Firebase and Jetpack Compose state management. 

* Limitations:
  * Some answers were high-level and required me to verify or adapt the examples to my project context. For instance, actual implementation required adjustments to the codebase, such as modifying the Firebase data items to include longitude and latitude.
  * Some code required adjustments to fit my actual project structure. It sometimes assumed best-practice architecture, which required me to adjust my existing project design to match (e.g., hoisting ViewModels to the NavHost level).

Corrections Required / Demonstration of Understanding:

While using ChatGPT, I verified its suggestions by checking official documentation, testing the code in my project, and interacting with the emulator to ensure each component worked correctly.
### Top contributors:

<a href="https://github.com/anajera05/ArtSphere/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=anajera05/ArtSphere" />
</a>

