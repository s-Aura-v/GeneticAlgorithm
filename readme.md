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