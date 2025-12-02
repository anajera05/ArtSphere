
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

Current features: 
* User Authentication:
  * Users can create their accounts and log in with the same credentials
  * Passwords must be +6 characters
  * User can sign out of their account and log back in
* Interactive Feed
  * News Integration: displays recent news and trends from the art industry using the NYTimes API.
* Navigation:
  * Navigate between the 4 main screens: Home, Map, Inbox, and Profile

<!-- build/run instructions -->
## How to build and run the application 
1. ```git clone https://github.com/anajera05/ArtSphere```
2. ```cd Artsphere```
3. Run emulator
4. run application

<!-- USAGE EXAMPLES -->
## Users 

Those who love art and/or want to sell!


<!-- APIs Used -->
## Planned API  

Google Maps

New York Times API


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
Tools Used: ChatGPT

Where and How Used: 

* Camera: Asked questions about AndroidView, factory, and ImageCapture in CameraX to understand how to set up the camera preview and take photos in a Jetpack Compose application.
  * Example prompt: What is AndroidView and factory, and why is it used in CameraX Compose?
* Map: Used ChatGPT to learn how to customize map pins in my Android project, including loading images from URLs with Coil, converting them to bitmaps, cropping them into circular icons, and applying them as custom markers in Google Maps Compose.
  * Example prompt: Explain how to load an image from a URL in Jetpack Compose, convert it into a bitmap, make it circular, and use it as a custom marker on Google Maps. I want to understand how each step works.

Analysis of Helpfulness and Limitations:
* Provided clear explanations, step-by-step breakdowns, and example code snippets that helped me understand the concepts and apply them directly in my project. It helped clarify how Jetpack Compose interacts with traditional Android Views, and how to manipulate camera and map features effectively.
* Limitations: Some answers were high-level and required me to verify or adapt the examples to my project context. For instance, actual implementation required adjustments to the codebase, such as modifying the Firebase data items to include longitude and latitude.

Corrections Required / Demonstration of Understanding:

While using ChatGPT, I verified its suggestions by checking official documentation, testing the code in my project, and interacting with the emulator to ensure each component worked correctly.
### Top contributors:

<a href="https://github.com/anajera05/ArtSphere/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=anajera05/ArtSphere" />
</a>

