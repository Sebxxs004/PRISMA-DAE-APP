package com.prisma.views;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.prisma.data.CasoRepository;
import com.prisma.models.Caso;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminView {
    private BorderPane view;
    private VBox casosCardsContainer;
    private TextArea detalleArea;

    public AdminView() {
        view = new BorderPane();
        view.getStyleClass().add("app-shell");
        view.setPadding(new Insets(26));

        VBox mainCard = new VBox(18);
        mainCard.getStyleClass().add("panel-card");
        mainCard.setPadding(new Insets(24));

        Label title = new Label("Panel Admin · Crear caso");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Registra víctimas, victimarios, delitos y actores para alimentar el tablero del fiscal.");
        subtitle.getStyleClass().add("app-subtitle");
        subtitle.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);

        TextField nombreField = createField("Ej. Caso Aurora");
        TextArea descripcionField = createTextArea("Resumen de los hechos...");
        TextField lugarField = createField("Ciudad / provincia");
        DatePicker fechaField = new DatePicker();
        fechaField.getStyleClass().add("date-picker");
        fechaField.setPrefWidth(320);
        TextArea victimasField = createTextArea("María López, ...");
        TextArea victimariosField = createTextArea("Sujeto A, ...");
        TextArea delitosField = createTextArea("Fraude procesal, ...");
        TextArea actoresField = createTextArea("Fiscalía, Policía Judicial, ...");

        addRow(form, 0, "Nombre del caso", nombreField);
        addRow(form, 1, "Descripción", descripcionField);
        addRow(form, 2, "Lugar", lugarField);
        addRow(form, 3, "Fecha de hechos", fechaField);
        addRow(form, 4, "Víctimas", victimasField);
        addRow(form, 5, "Victimarios", victimariosField);
        addRow(form, 6, "Delitos", delitosField);
        addRow(form, 7, "Actores involucrados", actoresField);

        Button btnSave = new Button("Guardar caso");
        btnSave.getStyleClass().add("primary-button");

        Button btnClear = new Button("Limpiar");
        btnClear.getStyleClass().add("ghost-button");

        HBox actions = new HBox(12, btnSave, btnClear);

        Label listTitle = new Label("Casos cargados");
        listTitle.getStyleClass().add("section-title");

        casosCardsContainer = new VBox(12);
        casosCardsContainer.getStyleClass().add("case-cards-container");
        casosCardsContainer.setPadding(new Insets(4));

        ScrollPane casosScrollPane = new ScrollPane(casosCardsContainer);
        casosScrollPane.getStyleClass().add("group-scroll");
        casosScrollPane.setFitToWidth(true);
        casosScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        casosScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        casosScrollPane.setPrefViewportHeight(280);

        VBox detallePanel = new VBox(14);
        detallePanel.getStyleClass().add("panel-card");
        detallePanel.setPadding(new Insets(16));
        detallePanel.setStyle("-fx-border-width: 1; -fx-border-color: rgba(56, 189, 248, 0.3);");

        Label detalleTitle = new Label("Detalles del caso");
        detalleTitle.getStyleClass().add("section-title");

        detalleArea = new TextArea();
        detalleArea.getStyleClass().add("text-area");
        detalleArea.setEditable(false);
        detalleArea.setWrapText(true);
        detalleArea.setPrefRowCount(12);
        VBox.setVgrow(detalleArea, Priority.ALWAYS);

        detallePanel.getChildren().addAll(detalleTitle, detalleArea);

        refreshList();
        CasoRepository.getCasos().addListener((ListChangeListener<Caso>) change -> refreshList());
        refreshCaseCards();

        btnSave.setOnAction(e -> {
            if (nombreField.getText().isBlank() || descripcionField.getText().isBlank() || lugarField.getText().isBlank() || fechaField.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Completa nombre, descripción, lugar y fecha.");
                return;
            }

            Caso caso = new Caso(
                    nombreField.getText().trim(),
                    descripcionField.getText().trim(),
                    lugarField.getText().trim(),
                    fechaField.getValue(),
                    parseCsv(victimasField.getText()),
                    parseCsv(victimariosField.getText()),
                    parseCsv(delitosField.getText()),
                    parseCsv(actoresField.getText())
            );

            CasoRepository.addCaso(caso);
            showAlert(Alert.AlertType.INFORMATION, "Caso agregado al repositorio. El tablero del fiscal lo tomará desde el catálogo de casos.");
            nombreField.clear();
            descripcionField.clear();
            lugarField.clear();
            fechaField.setValue(null);
            victimasField.clear();
            victimariosField.clear();
            delitosField.clear();
            actoresField.clear();
            refreshCaseCards();
        });

        btnClear.setOnAction(e -> {
            nombreField.clear();
            descripcionField.clear();
            lugarField.clear();
            fechaField.setValue(null);
            victimasField.clear();
            victimariosField.clear();
            delitosField.clear();
            actoresField.clear();
        });

        mainCard.getChildren().addAll(title, subtitle, form, actions);

        VBox rightCard = new VBox(14, listTitle, casosScrollPane);
        rightCard.getStyleClass().add("sidebar-card");
        rightCard.setPadding(new Insets(22));
        rightCard.setPrefWidth(420);
        VBox.setVgrow(rightCard, Priority.SOMETIMES);

        VBox rightContainer = new VBox(14, rightCard, detallePanel);
        VBox.setVgrow(detallePanel, Priority.ALWAYS);

        view.setCenter(mainCard);
        view.setRight(rightContainer);
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.getStyleClass().add("input-field");
        field.setPromptText(prompt);
        return field;
    }

    private TextArea createTextArea(String prompt) {
        TextArea area = new TextArea();
        area.getStyleClass().add("text-area");
        area.setPromptText(prompt);
        area.setPrefRowCount(3);
        return area;
    }

    private void addRow(GridPane form, int rowIndex, String label, Control control) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("muted-text");
        form.add(fieldLabel, 0, rowIndex);
        form.add(control, 1, rowIndex);
    }

    private List<String> parseCsv(String text) {
        if (text == null || text.isBlank()) {
            return List.of("Sin dato");
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private void refreshList() {
        refreshCaseCards();
    }

    private void refreshCaseCards() {
        casosCardsContainer.getChildren().setAll(CasoRepository.getCasos().stream()
                .map(this::buildCaseCard)
                .collect(Collectors.toList()));

        if (!CasoRepository.getCasos().isEmpty() && detalleArea.getText().isBlank()) {
            detalleArea.setText(buildCaseDetails(CasoRepository.getCasos().get(0)));
        }
    }

    private VBox buildCaseCard(Caso caso) {
        Label cardTitle = new Label(caso.getNombre());
        cardTitle.getStyleClass().add("section-title");

        Label cardMeta = new Label(caso.getLugar() + " · " + caso.getFechaHechosFormateada());
        cardMeta.getStyleClass().add("app-subtitle");

        Label cardSummary = new Label(caso.getDescripcion());
        cardSummary.getStyleClass().add("muted-text");
        cardSummary.setWrapText(true);

        Button detailButton = new Button("Ver detalles");
        detailButton.getStyleClass().add("secondary-button");
        detailButton.setOnAction(e -> detalleArea.setText(buildCaseDetails(caso)));

        VBox card = new VBox(10, cardTitle, cardMeta, cardSummary, detailButton);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(14));
        card.setOnMouseClicked(e -> detalleArea.setText(buildCaseDetails(caso)));
        return card;
    }

    private String buildCaseDetails(Caso caso) {
        return "📋 " + caso.getNombre() + "\n\n"
                + "📍 Lugar: " + caso.getLugar() + "\n"
                + "📅 Fecha: " + caso.getFechaHechosFormateada() + "\n\n"
                + "📝 Descripción:\n" + caso.getDescripcion() + "\n\n"
                + "👥 Víctimas:\n" + String.join("\n", caso.getVictimas()) + "\n\n"
                + "⚖️ Victimarios:\n" + String.join("\n", caso.getVictimarios()) + "\n\n"
                + "⚔️ Delitos:\n" + String.join("\n", caso.getDelitos()) + "\n\n"
                + "🏛️ Actores Involucrados:\n" + String.join("\n", caso.getActoresInvolucrados()) + "\n";
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("PRISMA DAE");
        alert.showAndWait();
    }

    public BorderPane getView() {
        return view;
    }
}
