package org.tourmanager;

import org.tourmanager.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TourManagerApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private MainWindow mainWindow;

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "org.tourmanager.ui.Preloader");
        System.setProperty("prism.lcdtext", "false"); // Poprawa renderowania tekstu
        System.setProperty("prism.text", "t2k"); // Lepsze renderowanie fontów

        launch(args);
    }

    @Override
    public void init() throws Exception {
        try {
            springContext = SpringApplication.run(TourManagerApplication.class);
            mainWindow = springContext.getBean(MainWindow.class);
        } catch (Exception e) {
            System.err.println("Błąd podczas inicjalizacji Spring Context: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            if (mainWindow == null) {
                showErrorAndExit("Nie udało się zainicjalizować głównego okna aplikacji");
                return;
            }
            mainWindow.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("Błąd podczas uruchamiania aplikacji: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            if (springContext != null) {
                springContext.close();
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas zamykania Spring Context: " + e.getMessage());
        } finally {
            Platform.exit();
        }
    }

    private void showErrorAndExit(String message) {
        System.err.println(message);
        Platform.exit();
        System.exit(1);
    }
}