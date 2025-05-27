package com.image.imageapp;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageApp extends Application {
    private Label toastMessage;
    private Label welcomeText;
    private ImageView originalImage;
    private Image loadedImage;
    private boolean imageModified = false;
    private Button saveButton;
    private Button scaleButton;
    private Button rotateLeftButton;
    private Button rotateRightButton;
    private Image originalLoadedImage;
    private ComboBox<String> operationComboBox;
    private Button executeOperationButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Aplikacja graficzna");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/pwr_logo.png")));
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        Label appName = new Label("Moja Aplikacja JavaFX");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        topBar.getChildren().addAll(logo, appName);
        root.setTop(topBar);

        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        welcomeText = new Label("Witaj w aplikacji graficznej!");
        welcomeText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        Button loadButton = new Button("Wczytaj obraz");
        loadButton.setOnAction(e -> loadImage(primaryStage));

        scaleButton = new Button("Skaluj obraz");
        scaleButton.setVisible(false);
        scaleButton.setOnAction(e -> showScaleDialog(primaryStage));

        saveButton = new Button("Zapisz obraz");
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            if (loadedImage == null) {
                showToast("Brak obrazu do zapisania");
                AppLogger.log(AppLogger.Level.ERROR, "Brak obrazu do zapisania.");
                return;
            }
            showSaveDialog(primaryStage);
        });

        operationComboBox = new ComboBox<>();
        operationComboBox.getItems().addAll("Negatyw", "Progowanie","Konturowanie");
        operationComboBox.setPromptText("Wybierz operację");
        operationComboBox.setVisible(false);
        executeOperationButton = new Button("Wykonaj");
        executeOperationButton.setVisible(false);

        executeOperationButton.setOnAction(e -> executeSelectedOperation());

        HBox operationBox = new HBox(10, operationComboBox, executeOperationButton);
        operationBox.setAlignment(Pos.CENTER);

        rotateLeftButton = new Button();
        rotateRightButton = new Button();

        ImageView rotateLeftIcon = new ImageView(new Image(getClass().getResourceAsStream("/rotate_left.png")));
        rotateLeftIcon.setFitWidth(20);
        rotateLeftIcon.setFitHeight(20);
        rotateLeftIcon.setPreserveRatio(true);
        rotateLeftButton.setGraphic(rotateLeftIcon);

        ImageView rotateRightIcon = new ImageView(new Image(getClass().getResourceAsStream("/rotate_right.png")));
        rotateRightIcon.setFitWidth(20);
        rotateRightIcon.setFitHeight(20);
        rotateRightIcon.setPreserveRatio(true);
        rotateRightButton.setGraphic(rotateRightIcon);

        rotateLeftButton.setVisible(false);
        rotateRightButton.setVisible(false);

        rotateLeftButton.setOnAction(e -> rotateImage(-90));
        rotateRightButton.setOnAction(e -> rotateImage(90));

        HBox rotationButtons = new HBox(10, rotateLeftButton, rotateRightButton);
        rotationButtons.setAlignment(Pos.CENTER);

        toastMessage = new Label("");
        toastMessage.setTextFill(Color.RED);
        toastMessage.setVisible(false);

        originalImage = new ImageView();
        originalImage.setFitWidth(250);
        originalImage.setFitHeight(250);
        originalImage.setPreserveRatio(true);
        originalImage.setStyle("-fx-border-color: black;");

        HBox imageBox = new HBox(originalImage);
        imageBox.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(welcomeText, imageBox, toastMessage, rotationButtons, loadButton, saveButton, scaleButton, operationBox);
        root.setCenter(centerBox);

        Label footer = new Label("Autor: Mateusz Andrzejewski");
        footer.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        footer.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(footer);
        BorderPane.setAlignment(footer, Pos.CENTER_RIGHT);

        Scene scene = new Scene(root, 1200, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj obraz");
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                if (!selectedFile.getName().toLowerCase().endsWith(".jpg")) {
                    showToast("Niedozwolony format pliku");
                    AppLogger.log(AppLogger.Level.ERROR, "Próba wczytania pliku o niedozwolonym formacie: " + selectedFile.getName());
                    return;
                }
                Image image = new Image(new FileInputStream(selectedFile));
                if (image.isError()) {
                    showToast("Błąd ładowania obrazu: " + image.getException().getMessage());
                    throw new IOException("Błąd ładowania");
                }

                originalImage.setImage(image);
                loadedImage = image;
                originalLoadedImage = image;
                imageModified = false;

                scaleButton.setVisible(true);
                saveButton.setDisable(false);

                rotateLeftButton.setVisible(true);
                rotateRightButton.setVisible(true);
                operationComboBox.setVisible(true);
                executeOperationButton.setVisible(true);
                welcomeText.setVisible(false);


                showToast("Pomyślnie załadowano plik");
                AppLogger.log(AppLogger.Level.ACTION, "Wczytano obraz: " + selectedFile.getName());

            } catch (Exception e) {
                showToast("Nie udało się załadować pliku");
                AppLogger.log(AppLogger.Level.ERROR, "Błąd wczytywania obrazu: " + e.getMessage());
            }
        }
    }

    private void showSaveDialog(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Zapisz obraz");

        Label warningLabel = new Label("Na pliku nie zostały wykonane żadne operacje!");
        warningLabel.setTextFill(Color.ORANGE);
        warningLabel.setVisible(!imageModified);

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa pliku (3-100 znaków)");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        VBox content = new VBox(10, warningLabel, nameField, errorLabel);
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            if (name.length() < 3) {
                errorLabel.setText("Wpisz co najmniej 3 znaki");
                event.consume();
                return;
            }

            Path targetPath = Paths.get(System.getProperty("user.home"), "Pictures", name + ".jpg");
            if (Files.exists(targetPath)) {
                showToast("Plik " + name + ".jpg już istnieje. Podaj inną nazwę.");
                AppLogger.log(AppLogger.Level.ERROR, "Próba zapisu pliku o istniejącej nazwie: " + name + ".jpg");
                event.consume();
                return;
            }

            try {

                PixelReader reader = loadedImage.getPixelReader();
                if (reader == null) {
                    showToast("Nie można zapisać obrazu — brak danych pikseli.");
                    AppLogger.log(AppLogger.Level.ERROR, "Brak danych pikseli do zapisu obrazu.");
                    return;
                }

                int width = (int) loadedImage.getWidth();
                int height = (int) loadedImage.getHeight();
                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color fxColor = reader.getColor(x, y);
                        int r = (int) (fxColor.getRed() * 255);
                        int g = (int) (fxColor.getGreen() * 255);
                        int b = (int) (fxColor.getBlue() * 255);
                        int rgb = (r << 16) | (g << 8) | b;
                        bufferedImage.setRGB(x, y, rgb);
                    }
                }

                ImageIO.write(bufferedImage, "jpg", targetPath.toFile());
                showToast("Zapisano obraz jako " + name + ".jpg");
                AppLogger.log(AppLogger.Level.ACTION, "Zapisano obraz jako: " + name + ".jpg");
            } catch (IOException e) {
                showToast("Nie udało się zapisać pliku.");
                AppLogger.log(AppLogger.Level.ERROR, "Błąd zapisu obrazu: " + e.getMessage());
            }
        });
        dialog.showAndWait();
    }

    private void showToast(String message) {
        toastMessage.setText(message);
        toastMessage.setVisible(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> toastMessage.setVisible(false));
        delay.play();
    }

    private void showScaleDialog(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Skaluj obraz");

        Label widthLabel = new Label("Szerokość (px):");
        TextField widthField = new TextField();
        Label widthError = new Label();
        widthError.setTextFill(Color.RED);

        Label heightLabel = new Label("Wysokość (px):");
        TextField heightField = new TextField();
        Label heightError = new Label();
        heightError.setTextFill(Color.RED);

        widthField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*")) widthField.setText(newV.replaceAll("[^\\d]", ""));
        });
        heightField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*")) heightField.setText(newV.replaceAll("[^\\d]", ""));
        });

        Button restoreBtn = new Button("Przywróć oryginalne wymiary");
        restoreBtn.setOnAction(ev -> {
            if (originalLoadedImage != null) {
                widthField.setText(String.valueOf((int) originalLoadedImage.getWidth()));
                heightField.setText(String.valueOf((int) originalLoadedImage.getHeight()));
            }
        });

        VBox content = new VBox(8, widthLabel, widthField, widthError, heightLabel, heightField, heightError, restoreBtn);
        dialog.getDialogPane().setContent(content);

        ButtonType scaleBtnType = new ButtonType("Zmień rozmiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scaleBtnType, ButtonType.CANCEL);

        Button scaleBtn = (Button) dialog.getDialogPane().lookupButton(scaleBtnType);
        scaleBtn.addEventFilter(ActionEvent.ACTION, event -> {
            boolean valid = true;
            widthError.setText("");
            heightError.setText("");
            String w = widthField.getText().trim();
            String h = heightField.getText().trim();

            if (w.isEmpty()) {
                widthError.setText("Pole jest wymagane");
                valid = false;
            }
            if (h.isEmpty()) {
                heightError.setText("Pole jest wymagane");
                valid = false;
            }

            int width = 0, height = 0;
            try {
                width = Integer.parseInt(w);
                if (width < 1 || width > 3000) {
                    widthError.setText("Zakres 1-3000");
                    valid = false;
                }
            } catch (Exception ex) {
                widthError.setText("Pole jest wymagane");
                valid = false;
            }

            try {
                height = Integer.parseInt(h);
                if (height < 1 || height > 3000) {
                    heightError.setText("Zakres 1-3000");
                    valid = false;
                }
            } catch (Exception ex) {
                heightError.setText("Pole jest wymagane");
                valid = false;
            }

            if (!valid) {
                event.consume();
                return;
            }

            WritableImage scaled = new WritableImage(width, height);
            PixelReader reader = loadedImage.getPixelReader();
            PixelWriter writer = scaled.getPixelWriter();

            double xRatio = loadedImage.getWidth() / width;
            double yRatio = loadedImage.getHeight() / height;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int px = (int) (x * xRatio);
                    int py = (int) (y * yRatio);
                    writer.setArgb(x, y, reader.getArgb(px, py));
                }
            }

            loadedImage = scaled;
            originalImage.setImage(loadedImage);
            imageModified = true;
            showToast("Obraz przeskalowany");
            AppLogger.log(AppLogger.Level.ACTION, "Użytkownik wykonał operację: Skalowanie obrazu");
        });

        dialog.setOnCloseRequest(ev -> {
            widthField.clear();
            heightField.clear();
            widthError.setText("");
            heightError.setText("");
        });

        dialog.showAndWait();
    }

    private void rotateImage(int angleDegrees) {
        if (loadedImage == null) return;

        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        WritableImage rotatedImage = new WritableImage(height, width);
        PixelReader reader = loadedImage.getPixelReader();
        PixelWriter writer = rotatedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                if (angleDegrees == 90) {
                    writer.setArgb(height - 1 - y, x, argb);
                } else if (angleDegrees == -90) {
                    writer.setArgb(y, width - 1 - x, argb);
                }
            }
        }

        loadedImage = rotatedImage;
        originalImage.setImage(loadedImage);
        imageModified = true;
        showToast("Obraz obrócony o " + angleDegrees + "°");
        AppLogger.log(AppLogger.Level.ACTION, "Użytkownik wykonał operację: Obrót obrazu o " + angleDegrees + "°");
    }

    private void executeSelectedOperation() {
        if (loadedImage == null) {
            showToast("Brak obrazu do przetworzenia.");
            AppLogger.log(AppLogger.Level.ERROR, "Brak obrazu do przetworzenia.");
            return;
        }

        String selected = operationComboBox.getValue();
        if (selected == null) {
            showToast("Nie wybrano operacji.");
            AppLogger.log(AppLogger.Level.ERROR, "Nie wybrano operacji do wykonania.");
            return;
        }

        if (selected.equals("Negatyw")) {
            try {
                int width = (int) loadedImage.getWidth();
                int height = (int) loadedImage.getHeight();

                WritableImage negativeImage = new WritableImage(width, height);
                PixelReader reader = loadedImage.getPixelReader();
                PixelWriter writer = negativeImage.getPixelWriter();

                int threadCount = 4;
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);

                List<Future<?>> futures = new ArrayList<>();

                int segmentHeight = height / threadCount;

                for (int t = 0; t < threadCount; t++) {
                    final int startY = t * segmentHeight;
                    final int endY = (t == threadCount - 1) ? height : (t + 1) * segmentHeight;


                    futures.add(executor.submit(() -> {
                        for (int y = startY; y < endY; y++) {
                            for (int x = 0; x < width; x++) {
                                Color color = reader.getColor(x, y);
                                Color neg = new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                                synchronized (writer) {
                                    writer.setColor(x, y, neg);
                                }
                            }
                        }
                    }));
                }
                for (Future<?> f : futures) f.get();
                executor.shutdown();
                loadedImage = negativeImage;
                originalImage.setImage(loadedImage);
                imageModified = true;
                showToast("Negatyw został wygenerowany pomyślnie!");
                AppLogger.log(AppLogger.Level.ACTION, "Użytkownik wykonał operację: Negatyw obrazu");

            } catch (Exception ex) {
                showToast("Nie udało się wykonać negatywu.");
                AppLogger.log(AppLogger.Level.ERROR, "Błąd podczas tworzenia negatywu: " + ex.getMessage());
            }
        } else if (selected.equals("Progowanie")) {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Progowanie");

            Label label = new Label("Wprowadź wartość progu (0-255):");
            Spinner<Integer> spinner = new Spinner<>(0, 255, 128);
            spinner.setEditable(true);

            Button wykonaj = new Button("Wykonaj progowanie");
            Button anuluj = new Button("Anuluj");

            HBox buttonBox = new HBox(10, wykonaj, anuluj);
            buttonBox.setAlignment(Pos.CENTER);

            VBox layout = new VBox(10, label, spinner, buttonBox);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));

            Scene scene = new Scene(layout);
            dialog.setScene(scene);
            dialog.show();

            anuluj.setOnAction(e -> dialog.close());

            wykonaj.setOnAction(e -> {
                int threshold = spinner.getValue();

                try {
                    int width = (int) loadedImage.getWidth();
                    int height = (int) loadedImage.getHeight();

                    WritableImage resultImage = new WritableImage(width, height);
                    PixelReader reader = loadedImage.getPixelReader();
                    PixelWriter writer = resultImage.getPixelWriter();

                    int threadCount = 4;
                    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

                    List<Future<?>> futures = new ArrayList<>();

                    int segmentHeight = height / threadCount;

                    for (int t = 0; t < threadCount; t++) {
                        final int startY = t * segmentHeight;
                        final int endY = (t == threadCount - 1) ? height : (t + 1) * segmentHeight;

                        futures.add(executor.submit(() -> {
                            for (int y = startY; y < endY; y++) {
                                for (int x = 0; x < width; x++) {
                                    Color color = reader.getColor(x, y);
                                    double brightness = color.getBrightness();
                                    int gray = (int) (brightness * 255);
                                    Color newColor = (gray >= threshold) ? Color.WHITE : Color.BLACK;
                                    writer.setColor(x, y, newColor);
                                }
                            }
                    }));
                }
                for (Future<?> f : futures) f.get();
                executor.shutdown();

                loadedImage = resultImage;
                originalImage.setImage(loadedImage);
                imageModified = true;
                showToast("Progowanie zostało przeprowadzone pomyślnie!");
                AppLogger.log(AppLogger.Level.ACTION, "Użytkownik wykonał operację: Progowanie obrazu z progiem " + threshold);

                } catch (Exception ex) {
                    showToast("Nie udało się wykonać progowania.");
                    AppLogger.log(AppLogger.Level.ERROR, "Błąd podczas progowania obrazu: " + ex.getMessage());
                }
                dialog.close();
            });


        }
        else if (selected.equals("Konturowanie")) {
            try {
                int width = (int) loadedImage.getWidth();
                int height = (int) loadedImage.getHeight();

                if (width < 2 || height < 2) {
                    showToast("Obraz jest zbyt mały do konturowania.");
                    AppLogger.log(AppLogger.Level.ERROR, "Obraz jest zbyt mały do konturowania.");
                    return;
                }

                PixelReader reader = loadedImage.getPixelReader();
                if (reader == null) {
                    showToast("Nie można odczytać pikseli obrazu.");
                    AppLogger.log(AppLogger.Level.ERROR, "Nie można odczytać pikseli obrazu.");
                    return;
                }

                WritableImage contouredImage = new WritableImage(width, height);
                PixelWriter writer = contouredImage.getPixelWriter();
                int threadCount = 4;
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);

                List<Future<?>> futures = new ArrayList<>();

                int segmentHeight = height / threadCount;

                for (int t = 0; t < threadCount; t++) {
                    final int startY = t * segmentHeight;
                    final int endY = (t == threadCount - 1) ? height : (t + 1) * segmentHeight;

                    futures.add(executor.submit(() -> {
                        for (int y = startY; y < endY; y++) {
                            for (int x = 0; x < width; x++) {
                                Color current = reader.getColor(x, y);
                                Color right = (x + 1 < width) ? reader.getColor(x + 1, y) : Color.BLACK;
                                Color down = (y + 1 < height) ? reader.getColor(x, y + 1) : Color.BLACK;

                                double diffX = Math.abs(current.getBrightness() - right.getBrightness());
                                double diffY = Math.abs(current.getBrightness() - down.getBrightness());

                                double edge = Math.min(1.0, Math.max(0.0, diffX + diffY));
                                Color edgeColor = new Color(edge, edge, edge, 1.0);

                                synchronized (writer) {
                                    writer.setColor(x, y, edgeColor);
                                }
                            }
                        }
                    }));
                }
                for (Future<?> f : futures) f.get();
                executor.shutdown();
                loadedImage = contouredImage;
                originalImage.setImage(loadedImage);
                imageModified = true;
                showToast("Konturowanie zostało przeprowadzone pomyślnie!");
                AppLogger.log(AppLogger.Level.ACTION, "Użytkownik wykonał operację: Konturowanie obrazu");

            } catch (Exception ex) {
                ex.printStackTrace();
                showToast("Nie udało się wykonać konturowania.");
                AppLogger.log(AppLogger.Level.ERROR, "Błąd podczas konturowania obrazu: " + ex.getMessage());
            }
        }
    }
}