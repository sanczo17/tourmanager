package org.tourmanager.ui;

import jakarta.annotation.PostConstruct;
import org.tourmanager.model.*;
import org.tourmanager.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.scene.input.KeyCode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MainWindow {

    @Autowired(required = false)
    private OfertaService ofertaService;

    @Autowired(required = false)
    private UmowaService umowaService;

    @Autowired(required = false)
    private PilotService pilotService;

    @Autowired(required = false)
    private RaportService raportService;

    private Stage primaryStage;
    private BorderPane mainLayout;
    private VBox centerContent;
    private Label statusLabel;

    private TableView<OfertaTurystyczna> ofertyTable = new TableView<>();

    @PostConstruct
    public void initialize() {
        if (ofertyTable == null) {
            ofertyTable = new TableView<>();
        }
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("TourManager - System Zarządzania Biurem Podróży");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        mainLayout = new BorderPane();
        mainLayout.setTop(createMenuBar());
        mainLayout.setLeft(createSidePanel());

        centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);
        showWelcomeScreen();

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        mainLayout.setCenter(scrollPane);

        mainLayout.setBottom(createStatusBar());

        Scene scene = new Scene(mainLayout, 1200, 800);

        setupKeyboardShortcuts(scene);

        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku styles.css - kontynuuję bez stylów");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Zatrzymaj domyślne zamknięcie
            handleWindowClose();
        });

        updateStatus("Aplikacja uruchomiona pomyślnie");
    }

    // ================================================================
    // TWORZENIE INTERFEJSU
    // ================================================================

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuOferty = new Menu("Oferty");
        MenuItem dodajOferte = new MenuItem("Dodaj ofertę");
        MenuItem przegladajOferty = new MenuItem("Przeglądaj oferty");
        MenuItem wyszukajOferty = new MenuItem("Wyszukaj oferty");
        MenuItem aktualizujOferty = new MenuItem("Aktualizuj dostępność");

        dodajOferte.setOnAction(e -> showDodajOferteForm());
        przegladajOferty.setOnAction(e -> showPrzegladajOferty());
        wyszukajOferty.setOnAction(e -> showWyszukajOferty());
        aktualizujOferty.setOnAction(e -> aktualizujOferty());

        menuOferty.getItems().addAll(dodajOferte, przegladajOferty, wyszukajOferty,
                new SeparatorMenuItem(), aktualizujOferty);

        Menu menuUmowy = new Menu("Umowy");
        MenuItem nowaUmowa = new MenuItem("Nowa umowa");
        MenuItem przegladajUmowy = new MenuItem("Przeglądaj umowy");
        MenuItem zaliczki = new MenuItem("Zarządzaj zaliczkami");
        MenuItem anulujUmowe = new MenuItem("Anuluj umowę");

        nowaUmowa.setOnAction(e -> showNowaUmowaForm());
        przegladajUmowy.setOnAction(e -> showPrzegladajUmowy());
        zaliczki.setOnAction(e -> showZaliczki());
        anulujUmowe.setOnAction(e -> showAnulujUmowe());

        menuUmowy.getItems().addAll(nowaUmowa, przegladajUmowy, zaliczki,
                new SeparatorMenuItem(), anulujUmowe);

        Menu menuPiloci = new Menu("Piloci");
        MenuItem dodajPilota = new MenuItem("Dodaj pilota");
        MenuItem przegladajPilotow = new MenuItem("Przeglądaj pilotów");
        MenuItem dostepnoscPilotow = new MenuItem("Dostępność");
        MenuItem licencjePilotow = new MenuItem("Sprawdź licencje");
        MenuItem przydzielPilota = new MenuItem("Przydziel do wycieczki");

        dodajPilota.setOnAction(e -> showDodajPilotaForm());
        przegladajPilotow.setOnAction(e -> showPrzegladajPilotow());
        dostepnoscPilotow.setOnAction(e -> showDostepnoscPilotow());
        licencjePilotow.setOnAction(e -> sprawdzLicencjePilotow());
        przydzielPilota.setOnAction(e -> showPrzydzielPilota());

        menuPiloci.getItems().addAll(dodajPilota, przegladajPilotow, dostepnoscPilotow,
                new SeparatorMenuItem(), licencjePilotow, przydzielPilota);

        Menu menuRaporty = new Menu("Raporty");
        MenuItem raportMiesieczny = new MenuItem("Raport miesięczny");
        MenuItem raportTopKlientow = new MenuItem("TOP klienci");
        MenuItem raportOfert = new MenuItem("Statystyki ofert");
        MenuItem raportPilotow = new MenuItem("Obciążenie pilotów");

        raportMiesieczny.setOnAction(e -> showRaportMiesieczny());
        raportTopKlientow.setOnAction(e -> showRaportTopKlientow());
        raportOfert.setOnAction(e -> showRaportOfert());
        raportPilotow.setOnAction(e -> showRaportPilotow());

        menuRaporty.getItems().addAll(raportMiesieczny, raportTopKlientow, raportOfert, raportPilotow);

        Menu menuNarzedzia = new Menu("Narzędzia");
        MenuItem eksportDanych = new MenuItem("Eksport danych");
        MenuItem importDanych = new MenuItem("Import danych");
        MenuItem ustawienia = new MenuItem("Ustawienia");

        eksportDanych.setOnAction(e -> showEksportDanych());
        importDanych.setOnAction(e -> showImportDanych());
        ustawienia.setOnAction(e -> showUstawienia());

        menuNarzedzia.getItems().addAll(eksportDanych, importDanych,
                new SeparatorMenuItem(), ustawienia);

        Menu menuPomoc = new Menu("Pomoc");
        MenuItem oProgramie = new MenuItem("O programie");
        MenuItem pomoc = new MenuItem("Pomoc");
        MenuItem kontakt = new MenuItem("Kontakt");

        oProgramie.setOnAction(e -> showOProgramie());
        pomoc.setOnAction(e -> showPomoc());
        kontakt.setOnAction(e -> showKontakt());

        menuPomoc.getItems().addAll(pomoc, kontakt, new SeparatorMenuItem(), oProgramie);

        menuBar.getMenus().addAll(menuOferty, menuUmowy, menuPiloci, menuRaporty,
                menuNarzedzia, menuPomoc);
        return menuBar;
    }

    private VBox createSidePanel() {
        VBox sidePanel = new VBox(10);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setPrefWidth(200);
        sidePanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Szybkie akcje");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnNowaOferta = new Button("Nowa oferta");
        Button btnNowaUmowa = new Button("Nowa umowa");
        Button btnNowPilot = new Button("Nowy pilot");
        Button btnRaport = new Button("Raport miesięczny");

        String buttonStyle = "-fx-pref-width: 180; -fx-pref-height: 35;";
        btnNowaOferta.setStyle(buttonStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnNowaUmowa.setStyle(buttonStyle + " -fx-background-color: #2196F3; -fx-text-fill: white;");
        btnNowPilot.setStyle(buttonStyle + " -fx-background-color: #FF9800; -fx-text-fill: white;");
        btnRaport.setStyle(buttonStyle + " -fx-background-color: #9C27B0; -fx-text-fill: white;");

        btnNowaOferta.setOnAction(e -> showDodajOferteForm());
        btnNowaUmowa.setOnAction(e -> showNowaUmowaForm());
        btnNowPilot.setOnAction(e -> showDodajPilotaForm());
        btnRaport.setOnAction(e -> showRaportMiesieczny());

        Separator separator = new Separator();

        Label statsTitle = new Label("Statystyki");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label statsOferty = new Label("Aktywne oferty: " + getAktywneOferty());
        Label statsUmowy = new Label("Umowy w tym miesiącu: " + getUmowyWTymMiesiacu());
        Label statsPiloci = new Label("Aktywni piloci: " + getAktywniPiloci());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidePanel.getChildren().addAll(
                title, btnNowaOferta, btnNowaUmowa, btnNowPilot, btnRaport,
                separator, statsTitle, statsOferty, statsUmowy, statsPiloci, spacer
        );

        return sidePanel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Gotowy");
        Label dateLabel = new Label("Data: " + LocalDate.now());
        Label versionLabel = new Label("TourManager v1.0");

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, spacer1, dateLabel, spacer2, versionLabel);
        return statusBar;
    }

    // ================================================================
    // EKRANY GŁÓWNE
    // ================================================================

    private void showWelcomeScreen() {
        centerContent.getChildren().clear();

        Label welcomeTitle = new Label("Witaj w systemie TourManager!");
        welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label welcomeText = new Label(
                "System zarządzania biurem podróży \"TRAVELER\"\n\n" +
                        "Funkcje systemu:\n" +
                        "• Zarządzanie ofertami turystycznymi\n" +
                        "• Obsługa umów z klientami\n" +
                        "• Ewidencja pilotów i ich dostępności\n" +
                        "• Generowanie raportów sprzedaży\n" +
                        "• Zarządzanie hotelami i partnerami\n\n" +
                        "Wybierz akcję z menu lub użyj przycisków z panelu bocznego."
        );
        welcomeText.setStyle("-fx-font-size: 14px;");
        welcomeText.setWrapText(true);
        welcomeText.setMaxWidth(600);

        HBox quickActions = new HBox(20);
        quickActions.setAlignment(Pos.CENTER);

        Button btnQuickOffer = new Button("Dodaj ofertę");
        Button btnQuickContract = new Button("Nowa umowa");
        Button btnQuickPilot = new Button("Dodaj pilota");
        Button btnQuickReport = new Button("Wygeneruj raport");

        String quickButtonStyle = "-fx-pref-width: 150; -fx-pref-height: 40; -fx-font-size: 12px;";
        btnQuickOffer.setStyle(quickButtonStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnQuickContract.setStyle(quickButtonStyle + " -fx-background-color: #2196F3; -fx-text-fill: white;");
        btnQuickPilot.setStyle(quickButtonStyle + " -fx-background-color: #FF9800; -fx-text-fill: white;");
        btnQuickReport.setStyle(quickButtonStyle + " -fx-background-color: #9C27B0; -fx-text-fill: white;");

        btnQuickOffer.setOnAction(e -> showDodajOferteForm());
        btnQuickContract.setOnAction(e -> showNowaUmowaForm());
        btnQuickPilot.setOnAction(e -> showDodajPilotaForm());
        btnQuickReport.setOnAction(e -> showRaportMiesieczny());

        quickActions.getChildren().addAll(btnQuickOffer, btnQuickContract, btnQuickPilot, btnQuickReport);

        VBox todayTasks = new VBox(10);
        todayTasks.setAlignment(Pos.CENTER_LEFT);
        todayTasks.setMaxWidth(600);

        Label tasksTitle = new Label("Dzisiejsze zadania:");
        tasksTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label task1 = new Label("• Sprawdź licencje pilotów wygasające w ciągu 30 dni");
        Label task2 = new Label("• Przejrzyj umowy z przekroczonym terminem dopłaty");
        Label task3 = new Label("• Zaktualizuj dostępność ofert");

        todayTasks.getChildren().addAll(tasksTitle, task1, task2, task3);

        centerContent.getChildren().addAll(welcomeTitle, welcomeText, quickActions, todayTasks);
        updateStatus("Ekran powitalny");
    }

    // ================================================================
    // FORMULARZE OFERT
    // ================================================================

    private void showDodajOferteForm() {
        centerContent.getChildren().clear();

        Label title = new Label("Dodaj nową ofertę turystyczną");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setMaxWidth(700);
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");

        TextField nazwaField = new TextField();
        nazwaField.setPromptText("Wprowadź nazwę oferty");

        TextArea opisField = new TextArea();
        opisField.setPrefRowCount(3);
        opisField.setPromptText("Opis oferty turystycznej");

        TextField krajField = new TextField();
        krajField.setPromptText("Kraj docelowy");

        DatePicker dataWyjazduField = new DatePicker();
        dataWyjazduField.setPromptText("Data wyjazdu");

        DatePicker dataPowrotuField = new DatePicker();
        dataPowrotuField.setPromptText("Data powrotu");

        TextField cenaField = new TextField();
        cenaField.setPromptText("Cena za osobę (PLN)");

        TextField miejscaField = new TextField();
        miejscaField.setPromptText("Maksymalna liczba uczestników");

        ComboBox<String> typField = new ComboBox<>();
        typField.getItems().addAll("Wypoczynkowa", "Objazdowa", "Krajoznawcza",
                "Pielgrzymka", "Biznesowa", "Ekstremalalna");
        typField.setPromptText("Wybierz typ wycieczki");

        form.add(new Label("Nazwa oferty:"), 0, 0);
        form.add(nazwaField, 1, 0);
        form.add(new Label("Opis:"), 0, 1);
        form.add(opisField, 1, 1);
        form.add(new Label("Kraj docelowy:"), 0, 2);
        form.add(krajField, 1, 2);
        form.add(new Label("Data wyjazdu:"), 0, 3);
        form.add(dataWyjazduField, 1, 3);
        form.add(new Label("Data powrotu:"), 0, 4);
        form.add(dataPowrotuField, 1, 4);
        form.add(new Label("Cena za osobę (PLN):"), 0, 5);
        form.add(cenaField, 1, 5);
        form.add(new Label("Maks. uczestników:"), 0, 6);
        form.add(miejscaField, 1, 6);
        form.add(new Label("Typ wycieczki:"), 0, 7);
        form.add(typField, 1, 7);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        Button saveButton = new Button("Zapisz ofertę");
        Button cancelButton = new Button("Anuluj");
        Button clearButton = new Button("Wyczyść");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 120; -fx-pref-height: 35;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 120; -fx-pref-height: 35;");
        clearButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 120; -fx-pref-height: 35;");

        saveButton.setOnAction(e -> {
            try {
                if (walidujFormularzOferty(nazwaField, krajField, dataWyjazduField,
                        dataPowrotuField, cenaField, miejscaField, typField)) {

                    OfertaTurystyczna oferta = new OfertaTurystyczna(
                            nazwaField.getText().trim(),
                            opisField.getText().trim(),
                            krajField.getText().trim(),
                            dataWyjazduField.getValue(),
                            dataPowrotuField.getValue(),
                            new BigDecimal(cenaField.getText().trim()),
                            Integer.parseInt(miejscaField.getText().trim()),
                            typField.getValue()
                    );

                    boolean success;
                    if (ofertaService != null) {
                        success = ofertaService.dodajOferte(oferta);
                    } else {
                        success = oferta.dodajOferte();
                    }

                    if (success) {
                        showAlert("Sukces", "Oferta została pomyślnie dodana!", Alert.AlertType.INFORMATION);
                        showWelcomeScreen();
                        updateStatus("Dodano nową ofertę: " + nazwaField.getText());
                    } else {
                        showAlert("Błąd", "Nie udało się dodać oferty. Sprawdź wprowadzone dane.", Alert.AlertType.ERROR);
                    }
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd w danych: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        clearButton.setOnAction(e -> {
            nazwaField.clear();
            opisField.clear();
            krajField.clear();
            dataWyjazduField.setValue(null);
            dataPowrotuField.setValue(null);
            cenaField.clear();
            miejscaField.clear();
            typField.setValue(null);
        });

        cancelButton.setOnAction(e -> showWelcomeScreen());

        buttons.getChildren().addAll(saveButton, clearButton, cancelButton);

        centerContent.getChildren().addAll(title, form, buttons);
        updateStatus("Formularz nowej oferty");
    }

    private void showPrzegladajOferty() {
        centerContent.getChildren().clear();

        Label title = new Label("Przeglądaj oferty turystyczne");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<OfertaTurystyczna> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<OfertaTurystyczna, String> nazwaCol = new TableColumn<>("Nazwa");
        nazwaCol.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        nazwaCol.setPrefWidth(200);

        TableColumn<OfertaTurystyczna, String> krajCol = new TableColumn<>("Kraj");
        krajCol.setCellValueFactory(new PropertyValueFactory<>("krajDocelowy"));
        krajCol.setPrefWidth(100);

        TableColumn<OfertaTurystyczna, LocalDate> dataWyjazdCol = new TableColumn<>("Data wyjazdu");
        dataWyjazdCol.setCellValueFactory(new PropertyValueFactory<>("dataWyjazdu"));
        dataWyjazdCol.setPrefWidth(120);

        TableColumn<OfertaTurystyczna, LocalDate> dataPowrotCol = new TableColumn<>("Data powrotu");
        dataPowrotCol.setCellValueFactory(new PropertyValueFactory<>("dataPowrotu"));
        dataPowrotCol.setPrefWidth(120);

        TableColumn<OfertaTurystyczna, BigDecimal> cenaCol = new TableColumn<>("Cena");
        cenaCol.setCellValueFactory(new PropertyValueFactory<>("cenaZaOsobe"));
        cenaCol.setPrefWidth(100);

        TableColumn<OfertaTurystyczna, Integer> miejscaCol = new TableColumn<>("Dostępne miejsca");
        miejscaCol.setCellValueFactory(new PropertyValueFactory<>("dostepneMiejsca"));
        miejscaCol.setPrefWidth(130);

        TableColumn<OfertaTurystyczna, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusOferty"));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(nazwaCol, krajCol, dataWyjazdCol, dataPowrotCol,
                cenaCol, miejscaCol, statusCol);

        ObservableList<OfertaTurystyczna> oferty = FXCollections.observableArrayList();
        try {
            if (ofertaService != null) {
                oferty.addAll(ofertaService.pobierzAktywneOferty());
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się załadować ofert: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        table.setItems(oferty);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button refreshButton = new Button("Odśwież");
        Button detailsButton = new Button("Szczegóły");
        Button editButton = new Button("Edytuj");
        Button deleteButton = new Button("Usuń");

        refreshButton.setOnAction(e -> showPrzegladajOferty());
        detailsButton.setOnAction(e -> {
            OfertaTurystyczna selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showSzczególyOferty(selected);
            } else {
                showAlert("Informacja", "Wybierz ofertę z listy", Alert.AlertType.INFORMATION);
            }
        });

        buttons.getChildren().addAll(refreshButton, detailsButton, editButton, deleteButton);

        centerContent.getChildren().addAll(title, table, buttons);
        updateStatus("Przeglądanie ofert - załadowano " + oferty.size() + " ofert");
    }

    private void showWyszukajOferty() {
        centerContent.getChildren().clear();

        Label title = new Label("Wyszukaj oferty");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane searchForm = new GridPane();
        searchForm.setHgap(10);
        searchForm.setVgap(10);
        searchForm.setMaxWidth(600);
        searchForm.setAlignment(Pos.CENTER);

        TextField krajField = new TextField();
        krajField.setPromptText("Kraj docelowy");

        DatePicker dataOdField = new DatePicker();
        dataOdField.setPromptText("Data od");

        DatePicker dataDoField = new DatePicker();
        dataDoField.setPromptText("Data do");

        ComboBox<String> typField = new ComboBox<>();
        typField.getItems().addAll("Wszystkie", "Wypoczynkowa", "Objazdowa", "Krajoznawcza", "Pielgrzymka");
        typField.setValue("Wszystkie");

        searchForm.add(new Label("Kraj:"), 0, 0);
        searchForm.add(krajField, 1, 0);
        searchForm.add(new Label("Data od:"), 2, 0);
        searchForm.add(dataOdField, 3, 0);
        searchForm.add(new Label("Data do:"), 0, 1);
        searchForm.add(dataDoField, 1, 1);
        searchForm.add(new Label("Typ:"), 2, 1);
        searchForm.add(typField, 3, 1);

        Button searchButton = new Button("Wyszukaj");
        searchButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        ListView<String> resultsView = new ListView<>();
        resultsView.setPrefHeight(300);

        searchButton.setOnAction(e -> {
            try {
                String kraj = krajField.getText().trim().isEmpty() ? null : krajField.getText().trim();
                String typ = "Wszystkie".equals(typField.getValue()) ? null : typField.getValue();

                if (ofertaService != null) {
                    List<OfertaTurystyczna> wyniki = ofertaService.wyszukajOferty(
                            kraj, dataOdField.getValue(), dataDoField.getValue(), typ);

                    ObservableList<String> items = FXCollections.observableArrayList();
                    for (OfertaTurystyczna oferta : wyniki) {
                        items.add(oferta.toString() + " - " + oferta.getCenaZaOsobe() + " PLN");
                    }
                    resultsView.setItems(items);
                    updateStatus("Znaleziono " + wyniki.size() + " ofert");
                } else {
                    resultsView.getItems().clear();
                    resultsView.getItems().add("Brak połączenia z serwisem ofert");
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas wyszukiwania: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        centerContent.getChildren().addAll(title, searchForm, searchButton, resultsView, backButton);
        updateStatus("Wyszukiwanie ofert");
    }

    // ================================================================
    // FORMULARZE UMÓW
    // ================================================================

    private void showNowaUmowaForm() {
        centerContent.getChildren().clear();

        Label title = new Label("Zawrzyj nową umowę");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(600);
        form.setAlignment(Pos.CENTER);

        Label klientSection = new Label("1. Dane klienta");
        klientSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        GridPane klientGrid = new GridPane();
        klientGrid.setHgap(10);
        klientGrid.setVgap(10);

        TextField imieField = new TextField();
        imieField.setPromptText("Imię");
        TextField nazwiskoField = new TextField();
        nazwiskoField.setPromptText("Nazwisko");
        TextField peselField = new TextField();
        peselField.setPromptText("PESEL");
        TextField telefonField = new TextField();
        telefonField.setPromptText("Telefon");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        klientGrid.add(new Label("Imię:"), 0, 0);
        klientGrid.add(imieField, 1, 0);
        klientGrid.add(new Label("Nazwisko:"), 2, 0);
        klientGrid.add(nazwiskoField, 3, 0);
        klientGrid.add(new Label("PESEL:"), 0, 1);
        klientGrid.add(peselField, 1, 1);
        klientGrid.add(new Label("Telefon:"), 2, 1);
        klientGrid.add(telefonField, 3, 1);
        klientGrid.add(new Label("Email:"), 0, 2);
        klientGrid.add(emailField, 1, 2, 3, 1);

        Label ofertaSection = new Label("2. Wybór oferty");
        ofertaSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        ComboBox<String> ofertaCombo = new ComboBox<>();
        ofertaCombo.setPromptText("Wybierz ofertę turystyczną");
        ofertaCombo.setPrefWidth(400);

        try {
            if (ofertaService != null) {
                List<OfertaTurystyczna> oferty = ofertaService.pobierzAktywneOferty();
                for (OfertaTurystyczna oferta : oferty) {
                    ofertaCombo.getItems().add(oferta.getId() + " - " + oferta.getNazwa() +
                            " (" + oferta.getKrajDocelowy() + ")");
                }
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się załadować ofert: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        Label szczegółySection = new Label("3. Szczegóły umowy");
        szczegółySection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        GridPane szczegółyGrid = new GridPane();
        szczegółyGrid.setHgap(10);
        szczegółyGrid.setVgap(10);

        TextField liczbaOsobField = new TextField();
        liczbaOsobField.setPromptText("Liczba osób");
        Label cenaLabel = new Label("Cena zostanie obliczona automatycznie");
        cenaLabel.setStyle("-fx-text-fill: #666;");

        szczegółyGrid.add(new Label("Liczba osób:"), 0, 0);
        szczegółyGrid.add(liczbaOsobField, 1, 0);
        szczegółyGrid.add(cenaLabel, 0, 1, 2, 1);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button createButton = new Button("Utwórz umowę");
        Button calculateButton = new Button("Oblicz cenę");
        Button cancelButton = new Button("Anuluj");

        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 120;");
        calculateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 120;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 120;");

        calculateButton.setOnAction(e -> {
            try {
                if (walidujFormularzUmowy(imieField, nazwiskoField, telefonField, ofertaCombo, liczbaOsobField)) {
                    Klient klient = new Klient(
                            imieField.getText().trim(),
                            nazwiskoField.getText().trim(),
                            peselField.getText().trim(),
                            telefonField.getText().trim(),
                            emailField.getText().trim()
                    );

                    showAlert("Sukces", "Umowa została utworzona!\nKlient: " +
                                    klient.getImie() + " " + klient.getNazwisko() +
                                    "\nLiczba osób: " + liczbaOsobField.getText(),
                            Alert.AlertType.INFORMATION);

                    showWelcomeScreen();
                    updateStatus("Utworzono nową umowę");
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas tworzenia umowy: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        createButton.setOnAction(e -> {
            try {
                if (walidujFormularzUmowy(imieField, nazwiskoField, telefonField,
                        ofertaCombo, liczbaOsobField)) {
                    // Tworzenie klienta
                    Klient klient = new Klient(
                            imieField.getText().trim(),
                            nazwiskoField.getText().trim(),
                            peselField.getText().trim(),
                            telefonField.getText().trim(),
                            emailField.getText().trim()
                    );

                    showAlert("Sukces", "Umowa została utworzona!\nKlient: " +
                                    klient.getImie() + " " + klient.getNazwisko() +
                                    "\nLiczba osób: " + liczbaOsobField.getText(),
                            Alert.AlertType.INFORMATION);

                    showWelcomeScreen();
                    updateStatus("Utworzono nową umowę");
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas tworzenia umowy: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        cancelButton.setOnAction(e -> showWelcomeScreen());

        buttons.getChildren().addAll(calculateButton, createButton, cancelButton);

        form.getChildren().addAll(klientSection, klientGrid, ofertaSection, ofertaCombo,
                szczegółySection, szczegółyGrid, buttons);

        centerContent.getChildren().addAll(title, form);
        updateStatus("Formularz nowej umowy");
    }

    private void showPrzegladajUmowy() {
        centerContent.getChildren().clear();

        Label title = new Label("Przeglądaj umowy");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<String> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<String, String> nrCol = new TableColumn<>("Nr umowy");
        nrCol.setPrefWidth(120);
        TableColumn<String, String> klientCol = new TableColumn<>("Klient");
        klientCol.setPrefWidth(200);
        TableColumn<String, String> ofertaCol = new TableColumn<>("Oferta");
        ofertaCol.setPrefWidth(250);
        TableColumn<String, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(nrCol, klientCol, ofertaCol, statusCol);

        ObservableList<String> umowy = FXCollections.observableArrayList(
                "UM/2024/001 - Jan Kowalski - Grecja - PODPISANA",
                "UM/2024/002 - Anna Nowak - Chorwacja - OPLACONA",
                "UM/2024/003 - Piotr Wiśniewski - Hiszpania - ZALICZKA_WPLACONA"
        );
        table.setItems(umowy);

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        centerContent.getChildren().addAll(title, table, backButton);
        updateStatus("Przeglądanie umów");
    }

    private void showZaliczki() {
        centerContent.getChildren().clear();

        Label title = new Label("Zarządzanie zaliczkami");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label info = new Label("Funkcja zarządzania zaliczkami:\n" +
                "• Rejestracja wpłat zaliczek\n" +
                "• Kontrola terminów płatności\n" +
                "• Wysyłanie przypomnień\n" +
                "• Raport nieopłaconych zaliczek");
        info.setStyle("-fx-font-size: 14px;");

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        content.getChildren().addAll(info, backButton);
        centerContent.getChildren().addAll(title, content);
        updateStatus("Zarządzanie zaliczkami");
    }

    private void showAnulujUmowe() {
        centerContent.getChildren().clear();

        Label title = new Label("Anuluj umowę");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setAlignment(Pos.CENTER);

        TextField nrUmowyField = new TextField();
        nrUmowyField.setPromptText("Numer umowy (np. UM/2024/001)");

        TextArea powodField = new TextArea();
        powodField.setPromptText("Powód anulowania");
        powodField.setPrefRowCount(3);

        Button anulujButton = new Button("Anuluj umowę");
        anulujButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        Button backButton = new Button("Powrót");

        anulujButton.setOnAction(e -> {
            if (!nrUmowyField.getText().trim().isEmpty() && !powodField.getText().trim().isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Potwierdzenie");
                confirm.setHeaderText("Czy na pewno chcesz anulować umowę?");
                confirm.setContentText("Umowa: " + nrUmowyField.getText() + "\nPowód: " + powodField.getText());

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() == ButtonType.OK) {
                    showAlert("Sukces", "Umowa została anulowana", Alert.AlertType.INFORMATION);
                    showWelcomeScreen();
                }
            } else {
                showAlert("Błąd", "Wypełnij wszystkie pola", Alert.AlertType.ERROR);
            }
        });

        backButton.setOnAction(e -> showWelcomeScreen());

        form.getChildren().addAll(new Label("Numer umowy:"), nrUmowyField,
                new Label("Powód anulowania:"), powodField,
                anulujButton, backButton);

        centerContent.getChildren().addAll(title, form);
        updateStatus("Anulowanie umowy");
    }

    // ================================================================
    // FORMULARZE PILOTÓW
    // ================================================================

    private void showDodajPilotaForm() {
        centerContent.getChildren().clear();

        Label title = new Label("Dodaj nowego pilota");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setMaxWidth(700);
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");

        TextField imieField = new TextField();
        imieField.setPromptText("Imię");
        TextField nazwiskoField = new TextField();
        nazwiskoField.setPromptText("Nazwisko");
        TextField telefonField = new TextField();
        telefonField.setPromptText("Telefon");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField licencjaField = new TextField();
        licencjaField.setPromptText("Numer licencji");
        DatePicker licencjaDataField = new DatePicker();
        licencjaDataField.setPromptText("Ważność licencji");

        TextField jezykiField = new TextField();
        jezykiField.setPromptText("Języki (oddzielone przecinkami)");
        TextField specjalizacjeField = new TextField();
        specjalizacjeField.setPromptText("Specjalizacje");

        form.add(new Label("Imię:"), 0, 0);
        form.add(imieField, 1, 0);
        form.add(new Label("Nazwisko:"), 2, 0);
        form.add(nazwiskoField, 3, 0);
        form.add(new Label("Telefon:"), 0, 1);
        form.add(telefonField, 1, 1);
        form.add(new Label("Email:"), 2, 1);
        form.add(emailField, 3, 1);
        form.add(new Label("Nr licencji:"), 0, 2);
        form.add(licencjaField, 1, 2);
        form.add(new Label("Ważność licencji:"), 2, 2);
        form.add(licencjaDataField, 3, 2);
        form.add(new Label("Języki:"), 0, 3);
        form.add(jezykiField, 1, 3, 3, 1);
        form.add(new Label("Specjalizacje:"), 0, 4);
        form.add(specjalizacjeField, 1, 4, 3, 1);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        Button saveButton = new Button("Zapisz pilota");
        Button clearButton = new Button("Wyczyść");
        Button cancelButton = new Button("Anuluj");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 120;");
        clearButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 120;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 120;");

        saveButton.setOnAction(e -> {
            try {
                if (walidujFormularzPilota(imieField, nazwiskoField, telefonField,
                        emailField, licencjaField, licencjaDataField)) {

                    Pilot pilot = new Pilot(
                            imieField.getText().trim(),
                            nazwiskoField.getText().trim(),
                            telefonField.getText().trim(),
                            emailField.getText().trim(),
                            licencjaField.getText().trim()
                    );
                    pilot.setDataWaznosciLicencji(licencjaDataField.getValue());

                    boolean success;
                    if (pilotService != null) {
                        success = pilotService.dodajPilota(pilot);
                    } else {
                        success = pilot.dodajPilota();
                    }

                    if (success) {
                        showAlert("Sukces", "Pilot został pomyślnie dodany!", Alert.AlertType.INFORMATION);
                        showWelcomeScreen();
                        updateStatus("Dodano nowego pilota: " + imieField.getText() + " " + nazwiskoField.getText());
                    } else {
                        showAlert("Błąd", "Nie udało się dodać pilota.", Alert.AlertType.ERROR);
                    }
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd w danych: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        clearButton.setOnAction(e -> {
            imieField.clear();
            nazwiskoField.clear();
            telefonField.clear();
            emailField.clear();
            licencjaField.clear();
            licencjaDataField.setValue(null);
            jezykiField.clear();
            specjalizacjeField.clear();
        });

        cancelButton.setOnAction(e -> showWelcomeScreen());
        buttons.getChildren().addAll(saveButton, clearButton, cancelButton);

        centerContent.getChildren().addAll(title, form, buttons);
        updateStatus("Formularz nowego pilota");
    }

    private void showPrzegladajPilotow() {
        centerContent.getChildren().clear();

        Label title = new Label("Przeglądaj pilotów");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Pilot> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Pilot, String> imieCol = new TableColumn<>("Imię");
        imieCol.setCellValueFactory(new PropertyValueFactory<>("imie"));
        imieCol.setPrefWidth(100);

        TableColumn<Pilot, String> nazwiskoCol = new TableColumn<>("Nazwisko");
        nazwiskoCol.setCellValueFactory(new PropertyValueFactory<>("nazwisko"));
        nazwiskoCol.setPrefWidth(120);

        TableColumn<Pilot, String> telefonCol = new TableColumn<>("Telefon");
        telefonCol.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        telefonCol.setPrefWidth(120);

        TableColumn<Pilot, String> licencjaCol = new TableColumn<>("Licencja");
        licencjaCol.setCellValueFactory(new PropertyValueFactory<>("numerLicencji"));
        licencjaCol.setPrefWidth(120);

        TableColumn<Pilot, LocalDate> waznoscCol = new TableColumn<>("Ważność");
        waznoscCol.setCellValueFactory(new PropertyValueFactory<>("dataWaznosciLicencji"));
        waznoscCol.setPrefWidth(120);

        TableColumn<Pilot, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusPilota"));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(imieCol, nazwiskoCol, telefonCol, licencjaCol, waznoscCol, statusCol);

        ObservableList<Pilot> piloci = FXCollections.observableArrayList();
        try {
            if (pilotService != null) {
                piloci.addAll(pilotService.pobierzWszystkichPilotow());
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się załadować pilotów: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        table.setItems(piloci);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button refreshButton = new Button("Odśwież");
        Button detailsButton = new Button("Szczegóły");
        Button licencjeButton = new Button("Sprawdź licencje");
        Button backButton = new Button("Powrót");

        refreshButton.setOnAction(e -> showPrzegladajPilotow());
        detailsButton.setOnAction(e -> {
            Pilot selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showSzczegółyPilota(selected);
            } else {
                showAlert("Informacja", "Wybierz pilota z listy", Alert.AlertType.INFORMATION);
            }
        });
        licencjeButton.setOnAction(e -> sprawdzLicencjePilotow());
        backButton.setOnAction(e -> showWelcomeScreen());

        buttons.getChildren().addAll(refreshButton, detailsButton, licencjeButton, backButton);

        centerContent.getChildren().addAll(title, table, buttons);
        updateStatus("Przeglądanie pilotów - załadowano " + piloci.size() + " pilotów");
    }

    private void showDostepnoscPilotow() {
        centerContent.getChildren().clear();

        Label title = new Label("Dostępność pilotów");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setAlignment(Pos.CENTER);

        Label info = new Label("Sprawdź dostępność pilotów w określonym okresie:");
        info.setStyle("-fx-font-size: 14px;");

        DatePicker dataOdField = new DatePicker();
        dataOdField.setPromptText("Data od");
        DatePicker dataDoField = new DatePicker();
        dataDoField.setPromptText("Data do");

        Button sprawdzButton = new Button("Sprawdź dostępność");
        sprawdzButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        ListView<String> resultsView = new ListView<>();
        resultsView.setPrefHeight(200);

        sprawdzButton.setOnAction(e -> {
            if (dataOdField.getValue() != null && dataDoField.getValue() != null) {
                try {
                    if (pilotService != null) {
                        List<Pilot> dostepni = pilotService.wyszukajDostepnychPilotow(
                                dataOdField.getValue(), dataDoField.getValue());

                        ObservableList<String> items = FXCollections.observableArrayList();
                        for (Pilot pilot : dostepni) {
                            items.add(pilot.getImie() + " " + pilot.getNazwisko() + " - " + pilot.getNumerLicencji());
                        }
                        resultsView.setItems(items);
                        updateStatus("Znaleziono " + dostepni.size() + " dostępnych pilotów");
                    } else {
                        resultsView.getItems().clear();
                        resultsView.getItems().add("Brak połączenia z serwisem pilotów");
                    }
                } catch (Exception ex) {
                    showAlert("Błąd", "Błąd podczas sprawdzania dostępności: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Błąd", "Wybierz okres dat", Alert.AlertType.ERROR);
            }
        });

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        form.getChildren().addAll(info, new Label("Data od:"), dataOdField,
                new Label("Data do:"), dataDoField, sprawdzButton,
                resultsView, backButton);

        centerContent.getChildren().addAll(title, form);
        updateStatus("Sprawdzanie dostępności pilotów");
    }

    private void sprawdzLicencjePilotow() {
        if (pilotService != null) {
            try {
                pilotService.sprawdzWaznoscLicencjiWszystkichPilotow();
                List<Pilot> wygasajace = pilotService.pobierzPilotowZWygasajacymiLicencjami();

                if (wygasajace.isEmpty()) {
                    showAlert("Informacja", "Wszystkie licencje są ważne", Alert.AlertType.INFORMATION);
                } else {
                    StringBuilder message = new StringBuilder("Piloci z wygasającymi licencjami:\n\n");
                    for (Pilot pilot : wygasajace) {
                        message.append("• ").append(pilot.getImie()).append(" ").append(pilot.getNazwisko())
                                .append(" - ważność do: ").append(pilot.getDataWaznosciLicencji()).append("\n");
                    }
                    showAlert("Uwaga", message.toString(), Alert.AlertType.WARNING);
                }
                updateStatus("Sprawdzono licencje pilotów");
            } catch (Exception e) {
                showAlert("Błąd", "Błąd podczas sprawdzania licencji: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Błąd", "Brak połączenia z serwisem pilotów", Alert.AlertType.ERROR);
        }
    }

    private void showPrzydzielPilota() {
        centerContent.getChildren().clear();

        Label title = new Label("Przydziel pilota do wycieczki");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setAlignment(Pos.CENTER);

        ComboBox<String> wycieczkaCombo = new ComboBox<>();
        wycieczkaCombo.setPromptText("Wybierz wycieczkę");
        wycieczkaCombo.getItems().addAll("Wycieczka 1 - Grecja", "Wycieczka 2 - Chorwacja", "Wycieczka 3 - Hiszpania");

        ComboBox<String> pilotCombo = new ComboBox<>();
        pilotCombo.setPromptText("Wybierz pilota");

        try {
            if (pilotService != null) {
                List<Pilot> piloci = pilotService.pobierzAktywnychPilotow();
                for (Pilot pilot : piloci) {
                    pilotCombo.getItems().add(pilot.getId() + " - " + pilot.getImie() + " " + pilot.getNazwisko());
                }
            }
        } catch (Exception e) {
            showAlert("Błąd", "Nie udało się załadować pilotów: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        Button przydzielButton = new Button("Przydziel");
        przydzielButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button backButton = new Button("Powrót");

        przydzielButton.setOnAction(e -> {
            if (wycieczkaCombo.getValue() != null && pilotCombo.getValue() != null) {
                showAlert("Sukces", "Pilot został przydzielony do wycieczki", Alert.AlertType.INFORMATION);
                showWelcomeScreen();
                updateStatus("Przydzielono pilota do wycieczki");
            } else {
                showAlert("Błąd", "Wybierz wycieczkę i pilota", Alert.AlertType.ERROR);
            }
        });

        backButton.setOnAction(e -> showWelcomeScreen());

        form.getChildren().addAll(new Label("Wycieczka:"), wycieczkaCombo,
                new Label("Pilot:"), pilotCombo,
                przydzielButton, backButton);

        centerContent.getChildren().addAll(title, form);
        updateStatus("Przydzielanie pilota");
    }

    // ================================================================
    // RAPORTY
    // ================================================================

    private void showRaportMiesieczny() {
        centerContent.getChildren().clear();

        Label title = new Label("Miesięczny raport sprzedaży");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox dateSelector = new HBox(10);
        dateSelector.setAlignment(Pos.CENTER);

        ComboBox<Integer> monthCombo = new ComboBox<>();
        ComboBox<Integer> yearCombo = new ComboBox<>();

        for (int i = 1; i <= 12; i++) {
            monthCombo.getItems().add(i);
        }
        for (int i = 2020; i <= 2030; i++) {
            yearCombo.getItems().add(i);
        }

        monthCombo.setValue(LocalDate.now().getMonthValue());
        yearCombo.setValue(LocalDate.now().getYear());

        Button generateButton = new Button("Generuj raport");
        generateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        dateSelector.getChildren().addAll(
                new Label("Miesiąc:"), monthCombo,
                new Label("Rok:"), yearCombo,
                generateButton
        );

        TextArea reportArea = new TextArea();
        reportArea.setPrefHeight(400);
        reportArea.setEditable(false);
        reportArea.setStyle("-fx-font-family: monospace;");

        generateButton.setOnAction(e -> {
            try {
                String raport;
                if (raportService != null) {
                    raport = raportService.generujMiesieznyRaportSprzedazy(yearCombo.getValue(), monthCombo.getValue());
                } else {
                    raport = generateMockMiesieznyyRaport(yearCombo.getValue(), monthCombo.getValue());
                }
                reportArea.setText(raport);
                updateStatus("Wygenerowano raport miesięczny");
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas generowania raportu: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button saveButton = new Button("Zapisz raport");
        Button printButton = new Button("Drukuj");
        Button backButton = new Button("Powrót");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        printButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        saveButton.setOnAction(e -> showAlert("Informacja", "Funkcja zapisu zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        printButton.setOnAction(e -> showAlert("Informacja", "Funkcja drukowania zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        backButton.setOnAction(e -> showWelcomeScreen());

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.getChildren().addAll(saveButton, printButton, backButton);

        centerContent.getChildren().addAll(title, dateSelector, reportArea, actionButtons);
        updateStatus("Generator raportu miesięcznego");
    }

    private void showRaportTopKlientow() {
        centerContent.getChildren().clear();

        Label title = new Label("TOP Klienci");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.setAlignment(Pos.CENTER);

        HBox limitSelector = new HBox(10);
        limitSelector.setAlignment(Pos.CENTER);

        Label limitLabel = new Label("Liczba klientów:");
        ComboBox<Integer> limitCombo = new ComboBox<>();
        limitCombo.getItems().addAll(5, 10, 20, 50);
        limitCombo.setValue(10);

        Button generateButton = new Button("Generuj raport");
        generateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        limitSelector.getChildren().addAll(limitLabel, limitCombo, generateButton);

        TextArea reportArea = new TextArea();
        reportArea.setPrefHeight(400);
        reportArea.setEditable(false);
        reportArea.setStyle("-fx-font-family: monospace;");

        generateButton.setOnAction(e -> {
            try {
                String raport;
                if (raportService != null) {
                    raport = raportService.generujRaportTopKlientow(limitCombo.getValue());
                } else {
                    raport = generateMockTopKlientowRaport(limitCombo.getValue());
                }
                reportArea.setText(raport);
                updateStatus("Wygenerowano raport TOP klientów");
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas generowania raportu: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        form.getChildren().addAll(limitSelector, reportArea, backButton);
        centerContent.getChildren().addAll(title, form);
        updateStatus("Generator raportu TOP klientów");
    }

    private void showRaportOfert() {
        centerContent.getChildren().clear();

        Label title = new Label("Statystyki ofert");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setStyle("-fx-padding: 20; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label aktywneOferty = new Label("Aktywne oferty: " + getAktywneOferty());
        Label najpopularniejszyKraj = new Label("Najpopularniejszy kraj: Grecja");
        Label sredniaCena = new Label("Średnia cena: 2,450 PLN");
        Label najdrozszaOferta = new Label("Najdroższa oferta: 8,500 PLN");

        aktywneOferty.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        najpopularniejszyKraj.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        sredniaCena.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        najdrozszaOferta.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        statsGrid.add(aktywneOferty, 0, 0);
        statsGrid.add(najpopularniejszyKraj, 1, 0);
        statsGrid.add(sredniaCena, 0, 1);
        statsGrid.add(najdrozszaOferta, 1, 1);

        Label chartTitle = new Label("Rozkład ofert według krajów");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox chartArea = new VBox(5);
        chartArea.setStyle("-fx-padding: 20; -fx-background-color: #ffffff; -fx-border-color: #ddd;");
        chartArea.setMaxWidth(400);

        ProgressBar grecjaBar = new ProgressBar(0.35);
        ProgressBar chorwacjaBar = new ProgressBar(0.25);
        ProgressBar hiszpaniaBar = new ProgressBar(0.20);
        ProgressBar wlochyBar = new ProgressBar(0.15);
        ProgressBar inneBar = new ProgressBar(0.05);

        chartArea.getChildren().addAll(
                new Label("Grecja (35%)"), grecjaBar,
                new Label("Chorwacja (25%)"), chorwacjaBar,
                new Label("Hiszpania (20%)"), hiszpaniaBar,
                new Label("Włochy (15%)"), wlochyBar,
                new Label("Inne (5%)"), inneBar
        );

        Button refreshButton = new Button("Odśwież");
        Button exportButton = new Button("Eksportuj");
        Button backButton = new Button("Powrót");

        refreshButton.setOnAction(e -> showRaportOfert());
        exportButton.setOnAction(e -> showAlert("Informacja", "Funkcja eksportu zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        backButton.setOnAction(e -> showWelcomeScreen());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(refreshButton, exportButton, backButton);

        content.getChildren().addAll(statsGrid, chartTitle, chartArea, buttons);
        centerContent.getChildren().addAll(title, content);
        updateStatus("Statystyki ofert");
    }

    private void showRaportPilotow() {
        centerContent.getChildren().clear();

        Label title = new Label("Obciążenie pilotów");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea reportArea = new TextArea();
        reportArea.setPrefHeight(400);
        reportArea.setEditable(false);
        reportArea.setStyle("-fx-font-family: monospace;");

        try {
            String raport;
            if (pilotService != null) {
                raport = pilotService.generujRaportObciazeniaPilotow();
            } else {
                raport = generateMockObciazeniePilotowRaport();
            }
            reportArea.setText(raport);
        } catch (Exception e) {
            reportArea.setText("Błąd podczas ładowania raportu: " + e.getMessage());
        }

        Button refreshButton = new Button("Odśwież");
        Button csvButton = new Button("Eksport CSV");
        Button backButton = new Button("Powrót");

        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        csvButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        refreshButton.setOnAction(e -> showRaportPilotow());
        csvButton.setOnAction(e -> {
            try {
                if (pilotService != null) {
                    String csv = pilotService.eksportujPilotowDoCSV();
                    showAlert("Sukces", "Dane pilotów zostały wyeksportowane", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Informacja", "Funkcja eksportu CSV zostanie zaimplementowana", Alert.AlertType.INFORMATION);
                }
            } catch (Exception ex) {
                showAlert("Błąd", "Błąd podczas eksportu: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        backButton.setOnAction(e -> showWelcomeScreen());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(refreshButton, csvButton, backButton);

        centerContent.getChildren().addAll(title, reportArea, buttons);
        updateStatus("Raport obciążenia pilotów");
    }

    // ================================================================
    // NARZĘDZIA
    // ================================================================

    private void showEksportDanych() {
        centerContent.getChildren().clear();

        Label title = new Label("Eksport danych");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);

        Label info = new Label("Wybierz typ danych do eksportu:");
        info.setStyle("-fx-font-size: 14px;");

        VBox exportOptions = new VBox(10);
        exportOptions.setAlignment(Pos.CENTER_LEFT);

        CheckBox ofertyCheck = new CheckBox("Oferty turystyczne");
        CheckBox umowyCheck = new CheckBox("Umowy");
        CheckBox klienciCheck = new CheckBox("Klienci");
        CheckBox pilociCheck = new CheckBox("Piloci");
        CheckBox plateCheck = new CheckBox("Płatności");

        exportOptions.getChildren().addAll(ofertyCheck, umowyCheck, klienciCheck, pilociCheck, plateCheck);

        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("CSV", "Excel", "PDF", "JSON");
        formatCombo.setValue("CSV");
        formatCombo.setPromptText("Wybierz format");

        Button exportButton = new Button("Eksportuj");
        exportButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 150;");

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        exportButton.setOnAction(e -> {
            StringBuilder selectedData = new StringBuilder("Wyeksportowano:\n");
            if (ofertyCheck.isSelected()) selectedData.append("• Oferty turystyczne\n");
            if (umowyCheck.isSelected()) selectedData.append("• Umowy\n");
            if (klienciCheck.isSelected()) selectedData.append("• Klienci\n");
            if (pilociCheck.isSelected()) selectedData.append("• Piloci\n");
            if (plateCheck.isSelected()) selectedData.append("• Płatności\n");
            selectedData.append("\nFormat: ").append(formatCombo.getValue());

            showAlert("Sukces", selectedData.toString(), Alert.AlertType.INFORMATION);
            updateStatus("Wyeksportowano dane w formacie " + formatCombo.getValue());
        });

        content.getChildren().addAll(info, exportOptions,
                new Label("Format eksportu:"), formatCombo,
                exportButton, backButton);

        centerContent.getChildren().addAll(title, content);
        updateStatus("Eksport danych");
    }

    private void showImportDanych() {
        centerContent.getChildren().clear();

        Label title = new Label("Import danych");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);

        Label info = new Label("Import danych z plików zewnętrznych:");
        info.setStyle("-fx-font-size: 14px;");

        VBox importOptions = new VBox(15);
        importOptions.setAlignment(Pos.CENTER);

        Button importOfertyButton = new Button("Importuj oferty");
        Button importKlientowButton = new Button("Importuj klientów");
        Button importPilotowButton = new Button("Importuj pilotów");
        Button importUmowButton = new Button("Importuj umowy");

        String buttonStyle = "-fx-pref-width: 200; -fx-pref-height: 35;";
        importOfertyButton.setStyle(buttonStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;");
        importKlientowButton.setStyle(buttonStyle + " -fx-background-color: #2196F3; -fx-text-fill: white;");
        importPilotowButton.setStyle(buttonStyle + " -fx-background-color: #FF9800; -fx-text-fill: white;");
        importUmowButton.setStyle(buttonStyle + " -fx-background-color: #9C27B0; -fx-text-fill: white;");

        importOfertyButton.setOnAction(e -> showAlert("Informacja", "Funkcja importu ofert zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        importKlientowButton.setOnAction(e -> showAlert("Informacja", "Funkcja importu klientów zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        importPilotowButton.setOnAction(e -> showAlert("Informacja", "Funkcja importu pilotów zostanie zaimplementowana", Alert.AlertType.INFORMATION));
        importUmowButton.setOnAction(e -> showAlert("Informacja", "Funkcja importu umów zostanie zaimplementowana", Alert.AlertType.INFORMATION));

        importOptions.getChildren().addAll(importOfertyButton, importKlientowButton, importPilotowButton, importUmowButton);

        Label supportedFormats = new Label("Obsługiwane formaty: CSV, Excel (.xlsx), JSON");
        supportedFormats.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        content.getChildren().addAll(info, importOptions, supportedFormats, backButton);
        centerContent.getChildren().addAll(title, content);
        updateStatus("Import danych");
    }

    private void showUstawienia() {
        centerContent.getChildren().clear();

        Label title = new Label("Ustawienia aplikacji");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);

        Label dbSection = new Label("Ustawienia bazy danych:");
        dbSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        GridPane dbGrid = new GridPane();
        dbGrid.setHgap(10);
        dbGrid.setVgap(10);

        TextField dbUrlField = new TextField("jdbc:h2:mem:tourmanager");
        TextField dbUserField = new TextField("sa");
        PasswordField dbPasswordField = new PasswordField();

        dbGrid.add(new Label("URL bazy:"), 0, 0);
        dbGrid.add(dbUrlField, 1, 0);
        dbGrid.add(new Label("Użytkownik:"), 0, 1);
        dbGrid.add(dbUserField, 1, 1);
        dbGrid.add(new Label("Hasło:"), 0, 2);
        dbGrid.add(dbPasswordField, 1, 2);

        Label appSection = new Label("Ustawienia aplikacji:");
        appSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        GridPane appGrid = new GridPane();
        appGrid.setHgap(10);
        appGrid.setVgap(10);

        TextField firmaNazwaField = new TextField("Biuro Podróży TRAVELER");
        TextField firmaAdresField = new TextField("ul. Podróżnicza 123, 00-000 Warszawa");
        TextField firmaTelefonField = new TextField("+48 22 123 45 67");

        appGrid.add(new Label("Nazwa firmy:"), 0, 0);
        appGrid.add(firmaNazwaField, 1, 0);
        appGrid.add(new Label("Adres:"), 0, 1);
        appGrid.add(firmaAdresField, 1, 1);
        appGrid.add(new Label("Telefon:"), 0, 2);
        appGrid.add(firmaTelefonField, 1, 2);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Zapisz ustawienia");
        Button resetButton = new Button("Przywróć domyślne");
        Button backButton = new Button("Powrót");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 150;");
        resetButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 150;");

        saveButton.setOnAction(e -> {
            showAlert("Sukces", "Ustawienia zostały zapisane", Alert.AlertType.INFORMATION);
            updateStatus("Zapisano ustawienia aplikacji");
        });

        resetButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Potwierdzenie");
            confirm.setHeaderText("Czy na pewno chcesz przywrócić ustawienia domyślne?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() == ButtonType.OK) {
                showAlert("Informacja", "Przywrócono ustawienia domyślne", Alert.AlertType.INFORMATION);
            }
        });

        backButton.setOnAction(e -> showWelcomeScreen());

        buttons.getChildren().addAll(saveButton, resetButton, backButton);

        content.getChildren().addAll(dbSection, dbGrid, appSection, appGrid, buttons);
        centerContent.getChildren().addAll(title, content);
        updateStatus("Ustawienia aplikacji");
    }

    // ================================================================
    // POMOC I INFORMACJE
    // ================================================================

    private void showOProgramie() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("O programie");
        alert.setHeaderText("TourManager v1.0");
        alert.setContentText(
                "System zarządzania biurem podróży\n\n" +
                        "Autorzy: Zespół TourManager\n" +
                        "Technologie: Java, JavaFX, Spring Boot, H2 Database\n" +
                        "Licencja: MIT License\n\n" +
                        "© 2024 TourManager. Wszystkie prawa zastrzeżone."
        );
        alert.showAndWait();
    }

    private void showPomoc() {
        centerContent.getChildren().clear();

        Label title = new Label("Pomoc - TourManager");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMaxWidth(700);

        Label wprowadzenie = new Label("WPROWADZENIE");
        wprowadzenie.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        TextArea helpText = new TextArea();
        helpText.setPrefHeight(400);
        helpText.setEditable(false);
        helpText.setWrapText(true);
        helpText.setText(
                "INSTRUKCJA OBSŁUGI SYSTEMU TOURMANAGER\n\n" +
                        "1. ZARZĄDZANIE OFERTAMI\n" +
                        "   • Dodawanie nowych ofert turystycznych\n" +
                        "   • Przeglądanie i edycja istniejących ofert\n" +
                        "   • Wyszukiwanie ofert według kryteriów\n" +
                        "   • Kontrola dostępności miejsc\n\n" +
                        "2. OBSŁUGA UMÓW\n" +
                        "   • Zawieranie nowych umów z klientami\n" +
                        "   • Rejestracja wpłat zaliczek\n" +
                        "   • Anulowanie umów z obliczeniem kosztów\n" +
                        "   • Śledzenie statusu płatności\n\n" +
                        "3. ZARZĄDZANIE PILOTAMI\n" +
                        "   • Dodawanie nowych pilotów do systemu\n" +
                        "   • Sprawdzanie dostępności pilotów\n" +
                        "   • Kontrola ważności licencji\n" +
                        "   • Przydzielanie pilotów do wycieczek\n\n" +
                        "4. RAPORTY I ANALIZY\n" +
                        "   • Miesięczne raporty sprzedaży\n" +
                        "   • Statystyki TOP klientów\n" +
                        "   • Analizy popularności ofert\n" +
                        "   • Raporty obciążenia pilotów\n\n" +
                        "5. NARZĘDZIA\n" +
                        "   • Eksport danych do różnych formatów\n" +
                        "   • Import danych z plików zewnętrznych\n" +
                        "   • Konfiguracja ustawień systemu\n\n" +
                        "SKRÓTY KLAWISZOWE:\n" +
                        "F1 - Ta pomoc\n" +
                        "Ctrl+N - Nowa oferta\n" +
                        "Ctrl+U - Nowa umowa\n" +
                        "Ctrl+P - Nowy pilot\n" +
                        "Ctrl+R - Raport miesięczny\n\n" +
                        "W razie problemów skontaktuj się z administratorem systemu."
        );

        Button backButton = new Button("Powrót");
        backButton.setOnAction(e -> showWelcomeScreen());

        content.getChildren().addAll(wprowadzenie, helpText, backButton);
        centerContent.getChildren().addAll(title, content);
        updateStatus("Pomoc systemu");
    }

    private void showKontakt() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kontakt");
        alert.setHeaderText("Informacje kontaktowe");
        alert.setContentText(
                "Biuro Podróży TRAVELER\n\n" +
                        "Adres:\nul. Podróżnicza 123\n00-000 Warszawa\n\n" +
                        "Telefon: +48 22 123 45 67\n" +
                        "Email: kontakt@traveler.pl\n" +
                        "WWW: www.traveler.pl\n\n" +
                        "Godziny pracy:\n" +
                        "Pon-Pt: 9:00-18:00\n" +
                        "Sob: 10:00-14:00\n" +
                        "Niedz: nieczynne\n\n" +
                        "Wsparcie techniczne:\n" +
                        "Email: support@traveler.pl\n" +
                        "Tel: +48 22 123 45 68"
        );
        alert.showAndWait();
    }

    // ================================================================
    // METODY SZCZEGÓŁOWE
    // ================================================================

    private void showSzczególyOferty(OfertaTurystyczna oferta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Szczegóły oferty");
        alert.setHeaderText(oferta.getNazwa());
        alert.setContentText(
                "Kraj: " + oferta.getKrajDocelowy() + "\n" +
                        "Data wyjazdu: " + oferta.getDataWyjazdu() + "\n" +
                        "Data powrotu: " + oferta.getDataPowrotu() + "\n" +
                        "Cena za osobę: " + oferta.getCenaZaOsobe() + " PLN\n" +
                        "Dostępne miejsca: " + oferta.getDostepneMiejsca() + "\n" +
                        "Status: " + oferta.getStatusOferty() + "\n\n" +
                        "Opis:\n" + (oferta.getOpis() != null ? oferta.getOpis() : "Brak opisu")
        );
        alert.showAndWait();
    }

    private void showSzczegółyPilota(Pilot pilot) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Szczegóły pilota");
        alert.setHeaderText(pilot.getImie() + " " + pilot.getNazwisko());
        alert.setContentText(
                "Telefon: " + pilot.getTelefon() + "\n" +
                        "Email: " + pilot.getEmail() + "\n" +
                        "Numer licencji: " + pilot.getNumerLicencji() + "\n" +
                        "Ważność licencji: " + pilot.getDataWaznosciLicencji() + "\n" +
                        "Status: " + pilot.getStatusPilota() + "\n" +
                        "Data zatrudnienia: " + pilot.getDataZatrudnienia() + "\n\n" +
                        "Języki: " + (pilot.getZnajomoscJezykow() != null ? pilot.getZnajomoscJezykow() : "Brak danych") + "\n" +
                        "Specjalizacje: " + (pilot.getSpecjalizacje() != null ? pilot.getSpecjalizacje() : "Brak danych")
        );
        alert.showAndWait();
    }

    // ================================================================
    // METODY POMOCNICZE I WALIDACYJNE
    // ================================================================

    private void aktualizujOferty() {
        try {
            if (ofertaService != null) {
                ofertaService.aktualizujDostepnoscOfert();
                showAlert("Sukces", "Dostępność ofert została zaktualizowana", Alert.AlertType.INFORMATION);
                updateStatus("Zaktualizowano dostępność ofert");
            } else {
                showAlert("Informacja", "Brak połączenia z serwisem ofert", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Błąd", "Błąd podczas aktualizacji ofert: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean walidujFormularzOferty(TextField nazwa, TextField kraj, DatePicker dataWyjazdu,
                                           DatePicker dataPowrotu, TextField cena, TextField miejsca,
                                           ComboBox<String> typ) {
        if (nazwa.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Nazwa oferty jest wymagana", Alert.AlertType.ERROR);
            return false;
        }
        if (kraj.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Kraj docelowy jest wymagany", Alert.AlertType.ERROR);
            return false;
        }
        if (dataWyjazdu.getValue() == null || dataPowrotu.getValue() == null) {
            showAlert("Błąd walidacji", "Daty wyjazdu i powrotu są wymagane", Alert.AlertType.ERROR);
            return false;
        }
        if (dataWyjazdu.getValue().isBefore(LocalDate.now())) {
            showAlert("Błąd walidacji", "Data wyjazdu nie może być wcześniejsza niż dzisiaj", Alert.AlertType.ERROR);
            return false;
        }
        if (dataPowrotu.getValue().isBefore(dataWyjazdu.getValue())) {
            showAlert("Błąd walidacji", "Data powrotu nie może być wcześniejsza niż data wyjazdu", Alert.AlertType.ERROR);
            return false;
        }

        try {
            double cenaValue = Double.parseDouble(cena.getText().trim());
            if (cenaValue <= 0) {
                showAlert("Błąd walidacji", "Cena musi być większa od 0", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Błąd walidacji", "Cena musi być liczbą", Alert.AlertType.ERROR);
            return false;
        }

        try {
            int miejscaValue = Integer.parseInt(miejsca.getText().trim());
            if (miejscaValue <= 0) {
                showAlert("Błąd walidacji", "Liczba miejsc musi być większa od 0", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Błąd walidacji", "Liczba miejsc musi być liczbą całkowitą", Alert.AlertType.ERROR);
            return false;
        }

        if (typ.getValue() == null) {
            showAlert("Błąd walidacji", "Typ oferty jest wymagany", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean walidujFormularzUmowy(TextField imie, TextField nazwisko, TextField telefon,
                                          ComboBox<String> oferta, TextField liczbaOsob) {
        if (imie.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Imię klienta jest wymagane", Alert.AlertType.ERROR);
            return false;
        }
        if (nazwisko.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Nazwisko klienta jest wymagane", Alert.AlertType.ERROR);
            return false;
        }
        if (telefon.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Telefon jest wymagany", Alert.AlertType.ERROR);
            return false;
        }
        if (oferta.getValue() == null) {
            showAlert("Błąd walidacji", "Oferta jest wymagana", Alert.AlertType.ERROR);
            return false;
        }

        try {
            int miejsca = Integer.parseInt(liczbaOsob.getText().trim());
            if (miejsca <= 0) {
                showAlert("Błąd walidacji", "Liczba osób musi być większa od 0", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Błąd walidacji", "Liczba osób musi być liczbą całkowitą", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean walidujFormularzPilota(TextField imie, TextField nazwisko, TextField telefon,
                                           TextField email, TextField licencja, DatePicker waznosc) {
        if (imie.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Imię jest wymagane", Alert.AlertType.ERROR);
            return false;
        }
        if (nazwisko.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Nazwisko jest wymagane", Alert.AlertType.ERROR);
            return false;
        }
        if (telefon.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Telefon jest wymagany", Alert.AlertType.ERROR);
            return false;
        }
        if (email.getText().trim().isEmpty() || !email.getText().contains("@")) {
            showAlert("Błąd walidacji", "Poprawny email jest wymagany", Alert.AlertType.ERROR);
            return false;
        }
        if (licencja.getText().trim().isEmpty()) {
            showAlert("Błąd walidacji", "Numer licencji jest wymagany", Alert.AlertType.ERROR);
            return false;
        }
        if (waznosc.getValue() == null) {
            showAlert("Błąd walidacji", "Data ważności licencji jest wymagana", Alert.AlertType.ERROR);
            return false;
        }
        if (waznosc.getValue().isBefore(LocalDate.now())) {
            showAlert("Błąd walidacji", "Data ważności licencji nie może być przeszła", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    // ================================================================
    // METODY GENERUJĄCE DANE TESTOWE (MOCK)
    // ================================================================

    private String generateMockMiesieznyyRaport(int rok, int miesiac) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MIESIĘCZNY RAPORT SPRZEDAŻY ===\n");
        sb.append("Okres: ").append(String.format("%02d/%d", miesiac, rok)).append("\n");
        sb.append("Data generowania: ").append(LocalDate.now()).append("\n\n");

        sb.append("PODSUMOWANIE SPRZEDAŻY:\n");
        sb.append("• Liczba zawartych umów: 23\n");
        sb.append("• Całkowita wartość sprzedaży: 125,450 PLN\n");
        sb.append("• Średnia wartość umowy: 5,454 PLN\n");
        sb.append("• Liczba anulowań: 2\n");
        sb.append("• Wartość anulowań: 8,900 PLN\n\n");

        sb.append("TOP 5 NAJLEPIEJ SPRZEDAJĄCYCH SIĘ OFERT:\n");
        sb.append("1. Grecja - Santorini (8 dni) - 6 sprzedanych\n");
        sb.append("2. Chorwacja - Plitvice (5 dni) - 4 sprzedane\n");
        sb.append("3. Hiszpania - Barcelona (7 dni) - 4 sprzedane\n");
        sb.append("4. Włochy - Rzym (6 dni) - 3 sprzedane\n");
        sb.append("5. Francja - Paryż (4 dni) - 2 sprzedane\n\n");

        sb.append("ANALIZA KRAJÓW DOCELOWYCH:\n");
        sb.append("• Grecja: 35% udziału (8 umów)\n");
        sb.append("• Chorwacja: 25% udziału (6 umów)\n");
        sb.append("• Hiszpania: 20% udziału (5 umów)\n");
        sb.append("• Włochy: 15% udziału (3 umów)\n");
        sb.append("• Pozostałe: 5% udziału (1 umowa)\n\n");

        sb.append("WPŁYWY I NALEŻNOŚCI:\n");
        sb.append("• Otrzymane zaliczki: 45,200 PLN\n");
        sb.append("• Pozostałe należności: 80,250 PLN\n");
        sb.append("• Procent wpłaconych zaliczek: 36%\n\n");

        sb.append("PORÓWNANIE Z POPRZEDNIM MIESIĄCEM:\n");
        sb.append("• Wzrost sprzedaży: +15%\n");
        sb.append("• Wzrost liczby umów: +8%\n");
        sb.append("• Spadek anulowań: -20%\n");

        return sb.toString();
    }

    private String generateMockTopKlientowRaport(int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RAPORT TOP KLIENTÓW ===\n");
        sb.append("Liczba wyświetlanych klientów: ").append(limit).append("\n");
        sb.append("Data generowania: ").append(LocalDate.now()).append("\n\n");

        String[] klienci = {
                "1. Kowalski Jan - 5 umów, 24,500 PLN",
                "2. Nowak Anna - 4 umowy, 18,200 PLN",
                "3. Wiśniewski Piotr - 3 umowy, 15,600 PLN",
                "4. Kaczmarek Maria - 3 umowy, 14,800 PLN",
                "5. Lewandowski Tomasz - 2 umowy, 12,400 PLN",
                "6. Dąbrowska Katarzyna - 2 umowy, 11,200 PLN",
                "7. Zieliński Marek - 2 umowy, 10,800 PLN",
                "8. Szymańska Agnieszka - 2 umowy, 9,600 PLN",
                "9. Woźniak Michał - 1 umowa, 8,500 PLN",
                "10. Kozłowska Ewa - 1 umowa, 7,800 PLN"
        };

        sb.append("RANKING KLIENTÓW (według wartości zakupów):\n\n");
        for (int i = 0; i < Math.min(limit, klienci.length); i++) {
            sb.append(klienci[i]).append("\n");
        }

        sb.append("\nSTATYSTYKI:\n");
        sb.append("• Łączna liczba aktywnych klientów: 156\n");
        sb.append("• Średnia wartość zakupów na klienta: 3,245 PLN\n");
        sb.append("• Klient z najwyższymi zakupami: Kowalski Jan\n");
        sb.append("• Procent stałych klientów (>1 umowy): 45%\n");

        return sb.toString();
    }

    private String generateMockObciazeniePilotowRaport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RAPORT OBCIĄŻENIA PILOTÓW ===\n");
        sb.append("Stan na dzień: ").append(LocalDate.now()).append("\n\n");

        sb.append("AKTYWNI PILOCI I ICH OBCIĄŻENIE:\n\n");

        sb.append("1. Nowak Anna (Lic: PIL001)\n");
        sb.append("   • Aktywne wycieczki: 3\n");
        sb.append("   • Następna wycieczka: 15.06.2025 (Grecja)\n");
        sb.append("   • Obciążenie: 75%\n");
        sb.append("   • Status: DOSTĘPNY\n\n");

        sb.append("2. Kowalski Piotr (Lic: PIL002)\n");
        sb.append("   • Aktywne wycieczki: 2\n");
        sb.append("   • Następna wycieczka: 20.06.2025 (Chorwacja)\n");
        sb.append("   • Obciążenie: 50%\n");
        sb.append("   • Status: DOSTĘPNY\n\n");

        sb.append("3. Wiśniewska Maria (Lic: PIL003)\n");
        sb.append("   • Aktywne wycieczki: 4\n");
        sb.append("   • Następna wycieczka: 10.06.2025 (Hiszpania)\n");
        sb.append("   • Obciążenie: 100%\n");
        sb.append("   • Status: ZAJĘTY\n\n");

        sb.append("4. Dąbrowski Tomasz (Lic: PIL004)\n");
        sb.append("   • Aktywne wycieczki: 1\n");
        sb.append("   • Następna wycieczka: 25.06.2025 (Włochy)\n");
        sb.append("   • Obciążenie: 25%\n");
        sb.append("   • Status: DOSTĘPNY\n\n");

        sb.append("5. Lewandowska Ewa (Lic: PIL005)\n");
        sb.append("   • Aktywne wycieczki: 0\n");
        sb.append("   • Następna wycieczka: BRAK\n");
        sb.append("   • Obciążenie: 0%\n");
        sb.append("   • Status: DOSTĘPNY\n\n");

        sb.append("PODSUMOWANIE:\n");
        sb.append("• Łączna liczba pilotów: 5\n");
        sb.append("• Piloci dostępni: 4\n");
        sb.append("• Piloci zajęci: 1\n");
        sb.append("• Średnie obciążenie: 50%\n");
        sb.append("• Najbardziej obciążony: Wiśniewska Maria (100%)\n");
        sb.append("• Najmniej obciążony: Lewandowska Ewa (0%)\n\n");

        sb.append("OSTRZEŻENIA:\n");
        sb.append("• Brak pilotów z wygasającymi licencjami\n");
        sb.append("• Wiśniewska Maria - maksymalne obciążenie\n");

        return sb.toString();
    }

    private int getAktywneOferty() {
        try {
            if (ofertaService != null) {
                List<OfertaTurystyczna> oferty = ofertaService.getAllOferty();
                return oferty != null ? oferty.size() : 0;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania liczby aktywnych ofert: " + e.getMessage());
        }
        return 0;
    }

    // ================================================================
    // METODY UTILITARNE
    // ================================================================

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("Status: " + message + " | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                showPomoc();
            } else if (event.isControlDown()) {
                switch (event.getCode()) {
                    case N:
                        showDodajOferteForm();
                        break;
                    case U:
                        showNowaUmowaForm();
                        break;
                    case P:
                        showDodajPilotaForm();
                        break;
                    case R:
                        showRaportMiesieczny();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    // ================================================================
    // OBSŁUGA ZDARZEŃ OKNA
    // ================================================================

    private void handleWindowClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zamknięcie aplikacji");
        alert.setHeaderText("Czy na pewno chcesz zamknąć aplikację?");
        alert.setContentText("Wszystkie niezapisane zmiany zostaną utracone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    private ObservableList<OfertaTurystyczna> createMockOferty() {
        ObservableList<OfertaTurystyczna> mockOferty = FXCollections.observableArrayList();

        try {
            OfertaTurystyczna oferta1 = new OfertaTurystyczna(
                    "Grecja - Santorini",
                    "Piękna wyspa z białymi domkami",
                    "Grecja",
                    LocalDate.of(2025, 7, 15),
                    LocalDate.of(2025, 7, 22),
                    new BigDecimal("2500.00"),
                    15,
                    "Wypoczynkowa"
            );
            oferta1.setId(1L); // Ustawienie ID po utworzeniu

            OfertaTurystyczna oferta2 = new OfertaTurystyczna(
                    "Chorwacja - Plitvice",
                    "Jeziora Plitvickie - cud natury",
                    "Chorwacja",
                    LocalDate.of(2025, 8, 1),
                    LocalDate.of(2025, 8, 8),
                    new BigDecimal("1800.00"),
                    20,
                    "Krajoznawcza"
            );
            oferta2.setId(2L); // Ustawienie ID po utworzeniu

            mockOferty.addAll(oferta1, oferta2);
        } catch (Exception e) {
            System.err.println("Błąd podczas tworzenia mock ofert: " + e.getMessage());
        }

        return mockOferty;
    }

    private int getUmowyWTymMiesiacu() {
        return 15;
    }

    private int getAktywniPiloci() {
        try {
            if (pilotService != null) {
                List<Pilot> piloci = pilotService.pobierzAktywnychPilotow();
                return piloci != null ? piloci.size() : 0;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania liczby aktywnych pilotów: " + e.getMessage());
        }
        return 0;
    }

    private void inicjalizujDaneTestowe() {
        updateStatus("Dane testowe dostępne");
    }

    private void handleServiceError(String operation, Exception e) {
        String message = "Błąd podczas " + operation + ": ";
        if (e instanceof NullPointerException) {
            message += "Serwis nie jest dostępny";
        } else {
            message += e.getMessage();
        }
        showAlert("Błąd", message, Alert.AlertType.ERROR);
        updateStatus("Błąd: " + operation);
    }

    private boolean sprawdzDostepnoscSerwisow() {
        if (ofertaService == null) {
            showAlert("Błąd", "Serwis ofert nie jest dostępny", Alert.AlertType.ERROR);
            return false;
        }
        if (umowaService == null) {
            showAlert("Błąd", "Serwis umów nie jest dostępny", Alert.AlertType.ERROR);
            return false;
        }
        if (pilotService == null) {
            showAlert("Błąd", "Serwis pilotów nie jest dostępny", Alert.AlertType.ERROR);
            return false;
        }
        if (raportService == null) {
            showAlert("Błąd", "Serwis raportów nie jest dostępny", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean sprawdzCzyPolaNieNull(TextField... pola) {
        for (TextField pole : pola) {
            if (pole == null || pole.getText() == null) {
                showAlert("Błąd", "Wykryto nieprawidłowości w formularzu", Alert.AlertType.ERROR);
                return false;
            }
        }
        return true;
    }

    // ================================================================
    // GETTERY I SETTERY
    // ================================================================

    public void setOfertaService(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    public void setUmowaService(UmowaService umowaService) {
        this.umowaService = umowaService;
    }

    public void setPilotService(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    public void setRaportService(RaportService raportService) {
        this.raportService = raportService;
    }
}