### Introduction

Purpose: Create a parallel program that tries to fit X amount of stations into an NxN 2d space and have it iterate over Z times. Each station should take a different amount of space in the facility and should have "likes" and "dislikes" regarding proximity to other stations.

Core Concepts: Java Multithreading, Countdown Latch, JavaFX, Thread Management and Use


## Lessons Learned

1. Don't try to do everything in one class. Return values to use in other classes!
2. Completely draft out my plan, including pseudocode. I only thought and drew it conceptually, so writing the methods ended up being more complicated than I had imagined.
3. When drafting, write what methods you might need in each class and what they should return.
4. NEVER EXTEND THREAD! IMPLEMENT RUNNABLE. Extending threads causes problems that should not exist. 


## Resources:
1. https://sid2697.github.io/external_pages/ELOPE.html 
2. https://gee.cs.oswego.edu/dl/csc375/a1.html 


Installation:
Basic Run
1. Download JavaFX
   2. https://gluonhq.com/products/javafx/
2. Go to Run > Edit Configurations.
3. Select your Application configuration (likely HelloApplication)
4. Click "Modify Options" next to "Build and run" tab
5. Click add VM options
6. Put the following code in your VM bar

--module-path /path/to/your/javafx-sdk/lib --add-modules=javafx.controls,javafx.fxml
Note: Change "/path/to/your/javafx-sdk/lib" to the location where your unzipped JavaFX file is located.

Installating Scenebuilder:
An easier way to edit GUIs
1. Download Scenebuilder
   2. https://gluonhq.com/products/scene-builder/
3. IntelliJ IDEA -> Settings -> Languages & Frameworks -> JavaFX
4. /Applications/Scenebuilder.app
5. Open resources -> com.example.geneticalgorithmfx 
6. Right click on, "hello-view.fxml" and "Open in Scenebuilder"
