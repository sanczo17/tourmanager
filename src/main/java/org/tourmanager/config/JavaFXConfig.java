package org.tourmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.tourmanager.ui.MainWindow;

@Configuration
public class JavaFXConfig {

    @Bean
    @Scope("singleton")
    public MainWindow mainWindow() {
        return new MainWindow();
    }
}
