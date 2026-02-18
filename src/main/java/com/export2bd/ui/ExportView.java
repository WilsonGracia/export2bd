package com.export2bd.ui;

import com.export2bd.dto.ImportFailureDto;
import com.export2bd.dto.UploadResultDto;
import com.export2bd.i18n.LanguageManager;
import com.export2bd.services.ExportApiService;
import com.export2bd.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Translate;

import java.io.File;
import java.util.List;

public class ExportView {

    
    private File cachedFile;

     
    private Label title;
    private Label subtitle;
    private Label dzIcon;
    private Label dzLine1;
    private Label dzLine2;
    private Label fileNameLabel;
    private VBox  resultBox;
    private Button exportBtn;
    private VBox dropZone;

    public VBox build() {

        ThemeManager theme = ThemeManager.getInstance();
        LanguageManager lang = LanguageManager.getInstance();

        
       // HEADER
        

        title = new Label(lang.get("export.title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        subtitle = new Label(lang.get("export.subtitle"));

        VBox header = new VBox(6, title, subtitle);

        
        // DROP ZONE
        

        dzIcon = new Label(lang.get("export.dropzone.icon"));
        dzIcon.setFont(Font.font(28));

        dzLine1 = new Label(lang.get("export.dropzone.title"));
        dzLine1.setFont(Font.font("System", FontWeight.BOLD, 14));

        dzLine2 = new Label(lang.get("export.dropzone.subtitle"));
        dzLine2.setStyle("-fx-font-size: 12;");

        dropZone = new VBox(6, dzIcon, dzLine1, dzLine2);
        dropZone.setSpacing(6);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(24));
        dropZone.setMinHeight(150);

         // Drop zone style helpers 
        Runnable applyBaseStyle = () -> dropZone.setStyle(
                "-fx-background-color: " + (theme.isDark() ? "#0b1528" : "#f9fafb") + ";" +
                "-fx-border-color: "     + theme.border() + ";" +
                "-fx-border-style: dashed;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        );

        String hoverStyle =
                "-fx-background-color: #eff6ff;" +
                "-fx-border-color: #3b82f6;" +
                "-fx-border-style: dashed;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;";

        String successStyle =
                "-fx-background-color: #f0fdf4;" +
                "-fx-border-color: #22c55e;" +
                "-fx-border-style: solid;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;";

        applyBaseStyle.run();

        
        
        

        fileNameLabel = new Label();
        fileNameLabel.setVisible(false);
        fileNameLabel.setManaged(false);

        
        
        

        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone &&
                event.getDragboard().hasFiles() &&
                hasExcel(event.getDragboard().getFiles())) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles() &&
                hasExcel(event.getDragboard().getFiles())) {
                dropZone.setStyle(hoverStyle);
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            if (cachedFile != null) dropZone.setStyle(successStyle);
            else applyBaseStyle.run();
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File excel = firstExcel(db.getFiles());
                if (excel != null) {
                    cachedFile = excel;
                    dropZone.setStyle(successStyle);
                    fileNameLabel.setText("📄  " + excel.getName()
                            + "  (" + humanSize(excel.length(), lang) + ")");
                    fileNameLabel.setVisible(true);
                    fileNameLabel.setManaged(true);
                    clearResults();
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        

        resultBox = new VBox(10);
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        
        exportBtn = new Button(lang.get("export.button"));
        exportBtn.setPrefWidth(160);
        exportBtn.setPrefHeight(40);
        exportBtn.setStyle(theme.primaryButtonStyle());

        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle(theme.primaryButtonHoverStyle()));
        exportBtn.setOnMouseExited(e  -> exportBtn.setStyle(theme.primaryButtonStyle()));

        exportBtn.setOnAction(e -> {

            if (cachedFile == null) {
                showError(lang.get("export.error.nofile"));
                return;
            }

            exportBtn.setText(lang.get("export.button.loading"));
            exportBtn.setDisable(true);
            clearResults();

            try {
                ExportApiService api    = new ExportApiService();
                UploadResultDto  result = api.uploadFileWithCredentials(cachedFile);
                showResults(result, lang, theme);
            } catch (Exception ex) {
                String translatedError = lang.translateBackendError(ex.getMessage());
                showError(lang.get("export.error.prefix") + translatedError);
                ex.printStackTrace();
            } finally {
                exportBtn.setText(lang.get("export.button"));
                exportBtn.setDisable(false);
            }
        });
        

        VBox root = new VBox(16, header, dropZone, fileNameLabel, exportBtn, resultBox);
        root.setPadding(new Insets(24));
        root.setMaxWidth(700);

        
        theme.addListener(dark -> applyTheme(theme, lang));

         
        lang.addListener(newLang -> updateTexts(lang, theme));

        applyTheme(theme, lang);

        return root;
    }

    

    private void updateTexts(LanguageManager lang, ThemeManager theme) {
        title.setText(lang.get("export.title"));
        subtitle.setText(lang.get("export.subtitle"));
        dzIcon.setText(lang.get("export.dropzone.icon"));
        dzLine1.setText(lang.get("export.dropzone.title"));
        dzLine2.setText(lang.get("export.dropzone.subtitle"));
        exportBtn.setText(lang.get("export.button"));
        
       
        if (cachedFile != null) {
            fileNameLabel.setText("📄  " + cachedFile.getName()
                    + "  (" + humanSize(cachedFile.length(), lang) + ")");
        }
    }

    private void applyTheme(ThemeManager theme, LanguageManager lang) {
        VBox root = (VBox) title.getParent().getParent();
        root.setStyle("-fx-background-color: " + theme.bg() + ";");
        title.setStyle("-fx-text-fill: " + theme.text() + ";");
        subtitle.setStyle("-fx-text-fill: " + theme.muted() + ";");
        dzLine1.setStyle("-fx-text-fill: " + theme.text()  + "; -fx-font-weight: bold; -fx-font-size: 14;");
        dzLine2.setStyle("-fx-text-fill: " + theme.muted() + "; -fx-font-size: 12;");
        fileNameLabel.setStyle(
                "-fx-text-fill: "        + theme.text()  + ";" +
                "-fx-font-size: 13;"     +
                "-fx-background-color: " + theme.input() + ";" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 6 12 6 12;"
        );
        if (cachedFile == null) {
            dropZone.setStyle(
                    "-fx-background-color: " + (theme.isDark() ? "#0b1528" : "#f9fafb") + ";" +
                    "-fx-border-color: "     + theme.border() + ";" +
                    "-fx-border-style: dashed;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;"
            );
        }
        exportBtn.setStyle(theme.primaryButtonStyle());
    }


    private void clearResults() {
        resultBox.getChildren().clear();
        resultBox.setVisible(false);
        resultBox.setManaged(false);
    }

    private void showResults(UploadResultDto result, LanguageManager lang, ThemeManager theme) {
        resultBox.getChildren().clear();

        resultBox.getChildren().add(new Separator());

        Label resTitle = new Label(lang.get("export.result.title"));
        resTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        resTitle.setStyle("-fx-text-fill: " + theme.text() + ";");
        resultBox.getChildren().add(resTitle);

        HBox stats = new HBox(12,
                statCard(lang.get("export.result.processed"), String.valueOf(result.getProcessed()), "#2563eb", "#eff6ff"),
                statCard(lang.get("export.result.inserted"), String.valueOf(result.getSucceeded()), "#16a34a", "#f0fdf4"),
                statCard(lang.get("export.result.failed"),   String.valueOf(result.getFailed()),    "#dc2626", "#fef2f2")
        );
        resultBox.getChildren().add(stats);

        List<ImportFailureDto> failures = result.getFailures();

        if (failures != null && !failures.isEmpty()) {

            Label failTitle = new Label(lang.get("export.result.errors.title", failures.size()));
            failTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
            failTitle.setStyle("-fx-text-fill: #b91c1c;");
            resultBox.getChildren().add(failTitle);

            VBox failList = new VBox(6);
            for (ImportFailureDto f : failures) {
                VBox card = new VBox(2);
                card.setPadding(new Insets(8, 12, 8, 12));
                card.setStyle(
                        "-fx-background-color: #fff1f2;" +
                        "-fx-border-color: #fca5a5;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
                );
                
                String rowText = lang.get("export.result.error.row", f.getRow());
                if (f.getId_number() != null && !f.getId_number().isBlank()) {
                    rowText += "  |  " + lang.get("common.id") + ": " + f.getId_number();
                }
                
                Label rowLbl = new Label(rowText);
                rowLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
                
                
                String translatedReason = lang.translateBackendError(f.getReason());
                Label reasonLbl = new Label(translatedReason);
                reasonLbl.setStyle("-fx-text-fill: #7f1d1d; -fx-font-size: 12;");
                reasonLbl.setWrapText(true);
                
                card.getChildren().addAll(rowLbl, reasonLbl);
                failList.getChildren().add(card);
            }

            ScrollPane scroll = new ScrollPane(failList);
            scroll.setFitToWidth(true);
            scroll.setMaxHeight(220);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            resultBox.getChildren().add(scroll);

        } else if (result.getFailed() == 0) {
            Label okLbl = new Label(lang.get("export.result.success"));
            okLbl.setStyle("-fx-text-fill: #15803d; -fx-font-size: 13;");
            resultBox.getChildren().add(okLbl);
        }

        resultBox.setVisible(true);
        resultBox.setManaged(true);
    }

    private void showError(String message) {
        resultBox.getChildren().clear();

        Label errLbl = new Label(message);
        errLbl.setStyle(
                "-fx-text-fill: #991b1b;" +
                "-fx-background-color: #fef2f2;" +
                "-fx-border-color: #fca5a5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 14 10 14;" +
                "-fx-font-size: 13;"
        );
        errLbl.setWrapText(true);

        resultBox.getChildren().add(errLbl);
        resultBox.setVisible(true);
        resultBox.setManaged(true);
    }

    

    private VBox statCard(String label, String value, String textColor, String bgColor) {
        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        valueLbl.setStyle("-fx-text-fill: " + textColor + ";");

        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        VBox card = new VBox(2, valueLbl, labelLbl);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(14, 24, 14, 24));
        card.setStyle(
                "-fx-background-color: " + bgColor   + ";" +
                "-fx-border-color: "     + textColor + "33;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }


    private boolean hasExcel(List<File> files) {
        return files.stream().anyMatch(this::isExcel);
    }

    private File firstExcel(List<File> files) {
        return files.stream().filter(this::isExcel).findFirst().orElse(null);
    }

    private boolean isExcel(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".xls") || name.endsWith(".xlsx");
    }

    private String humanSize(long bytes, LanguageManager lang) {
        if (bytes < 1024)       return lang.get("common.size.bytes", bytes);
        if (bytes < 1_048_576)  return lang.get("common.size.kb", bytes / 1024.0);
        return                         lang.get("common.size.mb", bytes / 1_048_576.0);
    }
}