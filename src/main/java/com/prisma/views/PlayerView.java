package com.prisma.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.prisma.data.CasoRepository;
import com.prisma.models.Caso;
import com.prisma.ui.Theme;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PlayerView {
    private static final double NODE_DIAMETER = 72;
    private static final double NODE_RADIUS = NODE_DIAMETER / 2.0;
    private static final double TARGET_SPEED = 0.45;
    private static final double REPULSION_DISTANCE = 120.0;
    private static final double REPULSION_STRENGTH = 650.0;
    private static final double GROUP_PADDING = 30.0;

    private final BorderPane view;
    private final Pane board;
    private final Pane groupLayer;
    private final Pane connectionLayer;
    private final Pane nodeLayer;

    private final HBox topBar;
    private final HBox modeTabs;
    private final Button analyticalTab;
    private final Button casesTab;
    private final Label timerLabel;
    private final Label sessionLabel;
    private final Label instructionLabel;
    private final StackPane moduleHost;
    private final VBox boardModule;
    private final VBox casesModule;
    private final VBox sidebar;
    private final ListView<String> connectionList;
    private final ListView<GroupCluster> groupList;
    private final VBox groupCardsContainer;
    private final ScrollPane groupScrollPane;
    private final ColorPicker groupColorPicker;
    private final TextArea groupReasonField;
    private final Label groupSummaryLabel;
    private final Label statusLabel;

    private final List<CaseNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final Map<String, GroupMeta> metadataBySignature = new HashMap<>();
    private final Map<String, GroupOverlay> overlayBySignature = new HashMap<>();

    private List<GroupCluster> currentClusters = List.of();
    private CaseNode selectedNode;
    private String selectedGroupSignature;
    private Stage stage;
    private final VBox casesCardsContainer;
    private final TextArea caseDetailArea;

    public PlayerView(Stage stage) {
        this.stage = stage;
        view = new BorderPane();
        view.getStyleClass().add("app-shell");
        view.setPadding(new Insets(14, 14, 14, 14));

        topBar = new HBox(18);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label appMark = new Label("MODO INVESTIGADOR");
        appMark.getStyleClass().add("top-brand");

        modeTabs = new HBox(10);
        modeTabs.getStyleClass().add("mode-tabs");
        analyticalTab = new Button("Tablero Analítico");
        analyticalTab.getStyleClass().addAll("mode-tab", "mode-tab-active");
        casesTab = new Button("Gestión de Casos");
        casesTab.getStyleClass().add("mode-tab");
        modeTabs.getChildren().addAll(analyticalTab, casesTab);
        analyticalTab.setOnAction(e -> showBoardModule());
        casesTab.setOnAction(e -> showCasesModule());

        HBox topLeft = new HBox(18, appMark, modeTabs);
        topLeft.setAlignment(Pos.CENTER_LEFT);

        timerLabel = new Label("00:00:00");
        timerLabel.getStyleClass().add("timer-pill");
        sessionLabel = new Label("Sesión: FISCAL");
        sessionLabel.getStyleClass().add("session-pill");
        Button logoutButton = new Button("Cerrar sesión");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setOnAction(e -> {
            LoginView loginView = new LoginView(stage);
            Scene scene = new Scene(loginView.getView(), 980, 680);
            Theme.apply(scene);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
        });

        HBox topRight = new HBox(12, timerLabel, sessionLabel, logoutButton);
        topRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(topLeft, javafx.scene.layout.Priority.ALWAYS);
        topBar.getChildren().addAll(topLeft, topRight);

        instructionLabel = new Label("Conecta esferas para crear hipótesis investigativas. Cada componente conectado se convierte en un grupo.");
        instructionLabel.getStyleClass().add("instruction-strip");
        instructionLabel.setMaxWidth(Double.MAX_VALUE);

        moduleHost = new StackPane();
        moduleHost.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        board = new Pane();
        board.getStyleClass().add("board-surface");
        board.setPrefSize(1260, 720);
        board.setMinSize(900, 560);

        groupLayer = new Pane();
        groupLayer.setMouseTransparent(true);
        connectionLayer = new Pane();
        connectionLayer.setMouseTransparent(true);
        nodeLayer = new Pane();

        board.getChildren().addAll(groupLayer, connectionLayer, nodeLayer);

        VBox boardWrapper = new VBox(12);
        boardWrapper.setPadding(new Insets(0, 16, 0, 0));
        Label boardTitle = new Label("Casos en movimiento");
        boardTitle.getStyleClass().add("section-title");
        statusLabel = new Label("Selecciona dos nodos para conectarlos.");
        statusLabel.getStyleClass().add("app-subtitle");
        statusLabel.setWrapText(true);
        HBox boardHeader = new HBox(12, boardTitle, statusLabel);
        boardHeader.setAlignment(Pos.CENTER_LEFT);
        boardHeader.getStyleClass().add("board-header");
        boardWrapper.getChildren().addAll(boardHeader, board);

        sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar-card");
        sidebar.setPrefWidth(380);
        sidebar.setPadding(new Insets(16));

        Label sidebarTitle = new Label("Conexiones y grupos");
        sidebarTitle.getStyleClass().add("section-title");

        connectionList = new ListView<>();
        connectionList.getStyleClass().add("connection-list");
        connectionList.setPrefHeight(190);

        groupList = new ListView<>();
        groupList.getStyleClass().add("group-list");
        groupList.setPrefHeight(180);
        groupList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> onGroupSelected(newValue));

        groupCardsContainer = new VBox(12);
        groupCardsContainer.setFillWidth(true);

        groupScrollPane = new ScrollPane(groupCardsContainer);
        groupScrollPane.getStyleClass().add("group-scroll");
        groupScrollPane.setFitToWidth(true);
        groupScrollPane.setPrefViewportHeight(360);

        groupSummaryLabel = new Label("Selecciona un grupo para ver sus casos conectados.");
        groupSummaryLabel.getStyleClass().add("app-subtitle");
        groupSummaryLabel.setWrapText(true);

        groupColorPicker = new ColorPicker(Color.web("#38bdf8"));
        groupColorPicker.setVisible(false);
        groupColorPicker.setManaged(false);

        groupReasonField = new TextArea();
        groupReasonField.getStyleClass().add("text-area");
        groupReasonField.setPromptText("Justificación del grupo");
        groupReasonField.setPrefRowCount(4);

        Button refreshButton = new Button("Recalcular grupos");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(e -> refreshGroups());

        VBox connectionsCard = new VBox(10, new Label("Conexiones (1)"), connectionList);
        connectionsCard.getStyleClass().add("panel-card");
        connectionsCard.setPadding(new Insets(14));
        VBox.setVgrow(connectionList, javafx.scene.layout.Priority.ALWAYS);

        VBox groupsCard = new VBox(10, new Label("Grupos detectados"), groupScrollPane, refreshButton);
        groupsCard.getStyleClass().add("panel-card");
        groupsCard.setPadding(new Insets(14));
        VBox.setVgrow(groupScrollPane, javafx.scene.layout.Priority.ALWAYS);

        sidebar.getChildren().addAll(sidebarTitle, connectionsCard, groupsCard);
        VBox.setVgrow(connectionsCard, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(groupsCard, javafx.scene.layout.Priority.ALWAYS);

        boardModule = new VBox(14, new HBox(14, boardWrapper, sidebar));
        HBox.setHgrow(boardWrapper, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(sidebar, javafx.scene.layout.Priority.NEVER);

        casesCardsContainer = new VBox(12);
        casesCardsContainer.getStyleClass().add("case-cards-container");
        casesCardsContainer.setPadding(new Insets(4));

        ScrollPane casesScroll = new ScrollPane(casesCardsContainer);
        casesScroll.getStyleClass().add("group-scroll");
        casesScroll.setFitToWidth(true);
        casesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        casesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        caseDetailArea = new TextArea();
        caseDetailArea.getStyleClass().add("text-area");
        caseDetailArea.setEditable(false);
        caseDetailArea.setWrapText(true);
        caseDetailArea.setPrefRowCount(12);

        VBox caseDetailCard = new VBox(12,
            new Label("Detalle del caso"),
            caseDetailArea);
        caseDetailCard.getStyleClass().add("panel-card");
        caseDetailCard.setPadding(new Insets(16));

        VBox casesHeader = new VBox(8,
            new Label("Gestión de Casos"),
            new Label("Selecciona una tarjeta para leer los detalles completos."));
        casesHeader.getChildren().get(0).getStyleClass().add("section-title");
        casesHeader.getChildren().get(1).getStyleClass().add("app-subtitle");

        VBox casesListCard = new VBox(12, new Label("Casos cargados"), casesScroll);
        casesListCard.getStyleClass().add("sidebar-card");
        casesListCard.setPadding(new Insets(16));
        VBox.setVgrow(casesScroll, javafx.scene.layout.Priority.ALWAYS);

        casesModule = new VBox(16, casesHeader, new HBox(16, casesListCard, caseDetailCard));
        HBox.setHgrow(casesListCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(caseDetailCard, javafx.scene.layout.Priority.ALWAYS);

        moduleHost.getChildren().add(boardModule);

        VBox content = new VBox(14, topBar, instructionLabel, moduleHost);
        view.setCenter(content);

        loadCasos();
        refreshConnections();
        refreshGroups();
        refreshCasesModule();
        showBoardModule();

        AnimationTimer physicsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                stepPhysics();
                updateConnections();
                updateGroupOverlays();
            }
        };
        physicsTimer.start();
    }

    private void loadCasos() {
        List<Caso> casos = CasoRepository.getCasos();
        double width = safeWidth();
        double height = safeHeight();
        double margin = NODE_RADIUS + 20;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Caso caso : casos) {
            CaseNode node = new CaseNode(caso);
            double startX = random.nextDouble(margin, Math.max(margin + 1, width - margin));
            double startY = random.nextDouble(margin, Math.max(margin + 1, height - margin));
            node.setBoardPosition(startX, startY);
            setRandomVelocity(node);
            registerNode(node);
            nodes.add(node);
            nodeLayer.getChildren().add(node);
        }
    }

    private void registerNode(CaseNode node) {
        node.setOnMousePressed(event -> {
            node.dragging = true;
            Point2D local = board.sceneToLocal(event.getSceneX(), event.getSceneY());
            node.dragOffsetX = local.getX() - node.getLayoutX();
            node.dragOffsetY = local.getY() - node.getLayoutY();
        });

        node.setOnMouseDragged(event -> {
            Point2D local = board.sceneToLocal(event.getSceneX(), event.getSceneY());
            double newX = local.getX() - node.dragOffsetX;
            double newY = local.getY() - node.dragOffsetY;
            node.setBoardPosition(clamp(newX, 0, safeWidth() - NODE_DIAMETER), clamp(newY, 0, safeHeight() - NODE_DIAMETER));
            node.vx = 0;
            node.vy = 0;
        });

        node.setOnMouseReleased(event -> {
            node.dragging = false;
            if (Math.hypot(node.vx, node.vy) < 0.001) {
                setRandomVelocity(node);
            }
        });

        node.setOnMouseClicked(event -> {
            if (selectedNode == null) {
                selectedNode = node;
                node.setSelected(true);
                statusLabel.setText("Seleccionado: " + node.getCaso().getNombre() + ". Elige otro nodo para asociarlo.");
                return;
            }

            if (selectedNode == node) {
                node.setSelected(false);
                selectedNode = null;
                statusLabel.setText("Selección cancelada.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Justificación de asociación");
            dialog.setHeaderText("Explica por qué relacionas estos casos");
            dialog.setContentText(selectedNode.getCaso().getNombre() + " → " + node.getCaso().getNombre());

            dialog.showAndWait().ifPresent(reason -> {
                String trimmed = reason.trim();
                if (!trimmed.isEmpty()) {
                    connections.add(new Connection(selectedNode, node, trimmed));
                    refreshConnections();
                    refreshGroups();
                    statusLabel.setText("Conexión creada entre " + selectedNode.getCaso().getNombre() + " y " + node.getCaso().getNombre() + ".");
                }
            });

            selectedNode.setSelected(false);
            selectedNode = null;
        });
    }

    private void stepPhysics() {
        double width = safeWidth();
        double height = safeHeight();

        for (int i = 0; i < nodes.size(); i++) {
            CaseNode a = nodes.get(i);
            if (a.dragging) {
                continue;
            }

            for (int j = i + 1; j < nodes.size(); j++) {
                CaseNode b = nodes.get(j);
                double dx = a.centerX() - b.centerX();
                double dy = a.centerY() - b.centerY();
                double distanceSq = Math.max(dx * dx + dy * dy, 80.0);
                double distance = Math.sqrt(distanceSq);
                if (distance < REPULSION_DISTANCE) {
                    double force = REPULSION_STRENGTH / distanceSq;
                    double fx = force * dx / distance;
                    double fy = force * dy / distance;
                    a.vx += fx * 0.12;
                    a.vy += fy * 0.12;
                    b.vx -= fx * 0.12;
                    b.vy -= fy * 0.12;
                }
            }

            normalizeVelocity(a);

            double nextX = a.getLayoutX() + a.vx;
            double nextY = a.getLayoutY() + a.vy;

            if (nextX <= 0 || nextX + NODE_DIAMETER >= width) {
                a.vx = -a.vx;
                nextX = clamp(nextX, 0, width - NODE_DIAMETER);
            }
            if (nextY <= 0 || nextY + NODE_DIAMETER >= height) {
                a.vy = -a.vy;
                nextY = clamp(nextY, 0, height - NODE_DIAMETER);
            }

            a.setBoardPosition(nextX, nextY);
            normalizeVelocity(a);
        }
    }

    private void normalizeVelocity(CaseNode node) {
        double speed = Math.hypot(node.vx, node.vy);
        if (speed < 0.001) {
            setRandomVelocity(node);
            return;
        }

        node.vx = node.vx / speed * TARGET_SPEED;
        node.vy = node.vy / speed * TARGET_SPEED;
    }

    private void setRandomVelocity(CaseNode node) {
        double angle = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2.0);
        node.vx = Math.cos(angle) * TARGET_SPEED;
        node.vy = Math.sin(angle) * TARGET_SPEED;
    }

    private void updateConnections() {
        for (Connection connection : connections) {
            connection.line.setStartX(connection.from.centerX());
            connection.line.setStartY(connection.from.centerY());
            connection.line.setEndX(connection.to.centerX());
            connection.line.setEndY(connection.to.centerY());
        }
    }

    private void refreshConnections() {
        connectionList.getItems().setAll(connections.stream()
                .map(connection -> connection.from.getCaso().getNombre() + " ↔ " + connection.to.getCaso().getNombre() + " | " + connection.reason)
                .collect(Collectors.toList()));

        connectionLayer.getChildren().setAll(connections.stream()
                .map(connection -> connection.line)
                .collect(Collectors.toList()));
    }

    private void refreshGroups() {
        currentClusters = detectClusters();

        Set<String> activeSignatures = currentClusters.stream()
                .map(cluster -> cluster.signature)
                .collect(Collectors.toSet());

        overlayBySignature.entrySet().removeIf(entry -> {
            boolean remove = !activeSignatures.contains(entry.getKey());
            if (remove) {
                groupLayer.getChildren().remove(entry.getValue().rectangle);
            }
            return remove;
        });

        groupList.getItems().setAll(currentClusters);

        if (selectedGroupSignature != null && activeSignatures.stream().noneMatch(signature -> signature.equals(selectedGroupSignature))) {
            clearGroupSelection();
        }

        renderGroupCards();
        updateGroupOverlays();
    }

    private void refreshCasesModule() {
        if (casesCardsContainer == null) {
            return;
        }

        casesCardsContainer.getChildren().setAll(CasoRepository.getCasos().stream()
                .map(this::buildCaseCard)
                .collect(Collectors.toList()));

        if (!CasoRepository.getCasos().isEmpty() && caseDetailArea.getText().isBlank()) {
            caseDetailArea.setText(buildCaseDetails(CasoRepository.getCasos().get(0)));
        }
    }

    private VBox buildCaseCard(Caso caso) {
        Label title = new Label(caso.getNombre());
        title.getStyleClass().add("section-title");

        Label meta = new Label(caso.getLugar() + " · " + caso.getFechaHechosFormateada());
        meta.getStyleClass().add("app-subtitle");

        Label summary = new Label(caso.getDescripcion());
        summary.getStyleClass().add("muted-text");
        summary.setWrapText(true);

        Button detailButton = new Button("Ver detalles");
        detailButton.getStyleClass().add("secondary-button");
        detailButton.setOnAction(e -> caseDetailArea.setText(buildCaseDetails(caso)));

        VBox card = new VBox(10, title, meta, summary, detailButton);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(14));
        card.setOnMouseClicked(e -> caseDetailArea.setText(buildCaseDetails(caso)));
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

    private void showBoardModule() {
        if (!moduleHost.getChildren().contains(boardModule)) {
            moduleHost.getChildren().setAll(boardModule);
        }
        setActiveTab(analyticalTab, casesTab);
        instructionLabel.setText("Conecta esferas para crear hipótesis investigativas. Cada componente conectado se convierte en un grupo.");
        board.setVisible(true);
        board.setManaged(true);
    }

    private void showCasesModule() {
        refreshCasesModule();
        if (!moduleHost.getChildren().contains(casesModule)) {
            moduleHost.getChildren().setAll(casesModule);
        }
        setActiveTab(casesTab, analyticalTab);
        instructionLabel.setText("Revisa las tarjetas de cada caso y usa sus detalles para construir asociaciones más precisas.");
        board.setVisible(false);
        board.setManaged(false);
    }

    private void setActiveTab(Button active, Button inactive) {
        active.getStyleClass().remove("mode-tab");
        if (!active.getStyleClass().contains("mode-tab-active")) {
            active.getStyleClass().add("mode-tab-active");
        }

        inactive.getStyleClass().remove("mode-tab-active");
        if (!inactive.getStyleClass().contains("mode-tab")) {
            inactive.getStyleClass().add("mode-tab");
        }
    }

    private void renderGroupCards() {
        groupCardsContainer.getChildren().setAll(currentClusters.stream()
                .map(this::buildGroupCard)
                .collect(Collectors.toList()));
    }

    private VBox buildGroupCard(GroupCluster cluster) {
        GroupMeta meta = cluster.meta;

        Label nameLabel = new Label(meta.name);
        nameLabel.getStyleClass().add("group-card-title");

        Button colorButton = new Button();
        colorButton.getStyleClass().add("group-swatch-button");
        colorButton.setMinSize(30, 30);
        colorButton.setPrefSize(30, 30);
        colorButton.setMaxSize(30, 30);
        colorButton.setStyle(buildSwatchStyle(meta.color));
        colorButton.setCursor(javafx.scene.Cursor.HAND);
        colorButton.setOnAction(e -> {
            ColorPicker picker = new ColorPicker(meta.color);
            picker.setOnAction(ce -> {
                Color selectedColor = picker.getValue();
                meta.color = selectedColor;
                colorButton.setStyle(buildSwatchStyle(selectedColor));
                updateGroupOverlays();
            });
            picker.show();
        });

        ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll("Asociado por modalidad");
        modeBox.setValue(meta.mode);

        TextArea reasonField = new TextArea(meta.reason);
        reasonField.getStyleClass().add("text-area");
        reasonField.setPromptText("Justificación general del grupo...");
        reasonField.setPrefRowCount(3);

        Button finalizeButton = new Button("Finalizar grupo");
        finalizeButton.getStyleClass().add("primary-button");
        finalizeButton.setOnAction(e -> saveGroupCard(cluster.signature, nameLabel, modeBox, reasonField, meta, colorButton));

        Label countLabel = new Label(cluster.members.size() + " casos conectados");
        countLabel.getStyleClass().add("app-subtitle");

        VBox card = new VBox(8);
        card.getStyleClass().add("group-card");
        card.setPadding(new Insets(12));

        HBox headerRow = new HBox(10, colorButton, nameLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                headerRow,
                modeBox,
                reasonField,
                finalizeButton,
                countLabel
        );
        return card;
    }

    private void saveGroupCard(String signature, Label nameLabel, ComboBox<String> modeBox, TextArea reasonField, GroupMeta meta, Button colorButton) {
        String reason = reasonField.getText().trim();
        if (reason.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "El grupo debe tener justificación.");
            return;
        }

        meta.name = nameLabel.getText().trim();
        meta.mode = modeBox.getValue();
        meta.reason = reason;
        metadataBySignature.put(signature, meta);
        colorButton.setStyle(buildSwatchStyle(meta.color));
        statusLabel.setText("Grupo actualizado: " + meta.name + ".");
        renderGroupCards();
        updateGroupOverlays();
    }

    private String buildSwatchStyle(Color color) {
        return "-fx-background-color: " + toRgb(color) + "; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: rgba(103,232,249,0.40); -fx-border-width: 1;";
    }

    private String toRgb(Color color) {
        int red = (int) Math.round(color.getRed() * 255);
        int green = (int) Math.round(color.getGreen() * 255);
        int blue = (int) Math.round(color.getBlue() * 255);
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    private List<GroupCluster> detectClusters() {
        List<GroupCluster> clusters = new ArrayList<>();
        Set<CaseNode> visited = new HashSet<>();
        int sequence = 1;

        for (CaseNode node : nodes) {
            if (visited.contains(node)) {
                continue;
            }

            Set<CaseNode> component = new HashSet<>();
            collectGroup(node, component);
            visited.addAll(component);

            if (component.size() < 2) {
                continue;
            }

            List<CaseNode> members = component.stream()
                    .sorted(Comparator.comparing(caseNode -> caseNode.getCaso().getNombre()))
                    .collect(Collectors.toList());
            String signature = createSignature(members);
                String defaultGroupName = "Grupo " + sequence;
            GroupMeta meta = metadataBySignature.computeIfAbsent(signature, key -> new GroupMeta(
                    defaultGroupName,
                    defaultGroupColor(signature),
                    "Asociado por modalidad",
                    "Sin justificación registrada"
            ));

            clusters.add(new GroupCluster(signature, members, meta));
            sequence++;
        }

        return clusters;
    }

    private void collectGroup(CaseNode node, Set<CaseNode> group) {
        if (!group.add(node)) {
            return;
        }

        for (Connection connection : connections) {
            if (connection.from == node) {
                collectGroup(connection.to, group);
            } else if (connection.to == node) {
                collectGroup(connection.from, group);
            }
        }
    }

    private void updateGroupOverlays() {
        // Limpiar overlays obsoletos
        Set<String> validSignatures = currentClusters.stream()
                .map(c -> c.signature)
                .collect(Collectors.toSet());
        
        overlayBySignature.entrySet().removeIf(entry -> {
            if (!validSignatures.contains(entry.getKey())) {
                groupLayer.getChildren().removeAll(entry.getValue().rectangle, entry.getValue().nameLabel);
                return true;
            }
            return false;
        });
        
        // Actualizar overlays válidos
        for (GroupCluster cluster : currentClusters) {
            GroupOverlay overlay = overlayBySignature.computeIfAbsent(cluster.signature, key -> {
                Rectangle rectangle = new Rectangle();
                rectangle.setMouseTransparent(true);
                rectangle.setArcWidth(26);
                rectangle.setArcHeight(26);
                Text nameLabel = new Text();
                nameLabel.setMouseTransparent(true);
                nameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
                nameLabel.setFill(Color.web("#38bdf8"));
                groupLayer.getChildren().addAll(rectangle, nameLabel);
                return new GroupOverlay(rectangle, nameLabel);
            });

            GroupMeta meta = cluster.meta;
            GroupBounds bounds = computeBounds(cluster.members);
            overlay.rectangle.setX(bounds.minX - GROUP_PADDING);
            overlay.rectangle.setY(bounds.minY - GROUP_PADDING);
            overlay.rectangle.setWidth(bounds.width + GROUP_PADDING * 2.0);
            overlay.rectangle.setHeight(bounds.height + GROUP_PADDING * 2.0);
            overlay.rectangle.setFill(Color.color(meta.color.getRed(), meta.color.getGreen(), meta.color.getBlue(), 0.08));
            overlay.rectangle.setStroke(Color.color(meta.color.getRed(), meta.color.getGreen(), meta.color.getBlue(), 0.92));
            overlay.rectangle.setStrokeWidth(3.2);
            
            overlay.nameLabel.setText(meta.name);
            overlay.nameLabel.setX(bounds.minX - GROUP_PADDING + 12);
            overlay.nameLabel.setY(bounds.minY - GROUP_PADDING + 26);
        }
    }

    private GroupBounds computeBounds(List<CaseNode> members) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (CaseNode member : members) {
            minX = Math.min(minX, member.getLayoutX());
            minY = Math.min(minY, member.getLayoutY());
            maxX = Math.max(maxX, member.getLayoutX() + NODE_DIAMETER);
            maxY = Math.max(maxY, member.getLayoutY() + NODE_DIAMETER);
        }

        return new GroupBounds(minX, minY, maxX - minX, maxY - minY);
    }

    private String createSignature(List<CaseNode> members) {
        return members.stream()
                .map(member -> member.getCaso().getNombre())
                .sorted()
                .collect(Collectors.joining("|"));
    }

    private Color defaultGroupColor(String signature) {
        int hash = Math.abs(signature.hashCode());
        double hue = hash % 360;
        return Color.hsb(hue, 0.72, 0.95);
    }

    private void onGroupSelected(GroupCluster cluster) {
        if (cluster == null) {
            selectedGroupSignature = null;
            groupSummaryLabel.setText("Selecciona un grupo para ver sus casos conectados.");
            groupReasonField.clear();
            groupColorPicker.setValue(Color.web("#38bdf8"));
            return;
        }

        selectedGroupSignature = cluster.signature;
        GroupMeta meta = cluster.meta;
        groupSummaryLabel.setText("Casos del grupo: " + cluster.members.stream()
                .map(node -> node.getCaso().getNombre())
                .collect(Collectors.joining(", ")));
        groupReasonField.setText(meta.reason);
        groupColorPicker.setValue(meta.color);
    }

    private void clearGroupSelection() {
        selectedGroupSignature = null;
        groupList.getSelectionModel().clearSelection();
        groupSummaryLabel.setText("Selecciona un grupo para ver sus casos conectados.");
        groupReasonField.clear();
        groupColorPicker.setValue(Color.web("#38bdf8"));
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("PRISMA DAE");
        alert.showAndWait();
    }

    private HBox buildActionChip(String labelText) {
        Label checkbox = new Label("☐");
        checkbox.getStyleClass().add("muted-text");
        Label label = new Label(labelText);
        label.getStyleClass().add("action-chip");
        HBox chip = new HBox(10, checkbox, label);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("action-chip");
        return chip;
    }

    private double safeWidth() {
        double width = board.getWidth();
        return width > 0 ? width : 1050;
    }

    private double safeHeight() {
        double height = board.getHeight();
        return height > 0 ? height : 820;
    }

    private double clamp(double value, double min, double max) {
        if (max <= min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public BorderPane getView() {
        return view;
    }

    private static final class Connection {
        private final CaseNode from;
        private final CaseNode to;
        private final String reason;
        private final Line line;

        private Connection(CaseNode from, CaseNode to, String reason) {
            this.from = from;
            this.to = to;
            this.reason = reason;
            this.line = new Line();
            this.line.setStroke(Color.web("#67e8f9", 0.78));
            this.line.setStrokeWidth(2.2);
            this.line.setMouseTransparent(true);
        }
    }

    private static final class GroupMeta {
        private String name;
        private Color color;
        private String mode;
        private String reason;

        private GroupMeta(String name, Color color, String mode, String reason) {
            this.name = name;
            this.color = color;
            this.mode = mode;
            this.reason = reason;
        }
    }

    private static final class GroupOverlay {
        private final Rectangle rectangle;
        private final Text nameLabel;

        private GroupOverlay(Rectangle rectangle, Text nameLabel) {
            this.rectangle = rectangle;
            this.nameLabel = nameLabel;
        }
    }

    private static final class GroupBounds {
        private final double minX;
        private final double minY;
        private final double width;
        private final double height;

        private GroupBounds(double minX, double minY, double width, double height) {
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
        }
    }

    private static final class GroupCluster {
        private final String signature;
        private final List<CaseNode> members;
        private final GroupMeta meta;

        private GroupCluster(String signature, List<CaseNode> members, GroupMeta meta) {
            this.signature = signature;
            this.members = members;
            this.meta = meta;
        }

        @Override
        public String toString() {
            return meta.name + " · " + members.size() + " casos";
        }
    }

    private static final class CaseNode extends StackPane {
        private final Caso caso;
        private double vx;
        private double vy;
        private boolean dragging;
        private double dragOffsetX;
        private double dragOffsetY;

        private CaseNode(Caso caso) {
            this.caso = caso;
            setPrefSize(NODE_DIAMETER, NODE_DIAMETER);
            setMinSize(NODE_DIAMETER, NODE_DIAMETER);
            setMaxSize(NODE_DIAMETER, NODE_DIAMETER);
            getStyleClass().add("case-node");
            setAlignment(Pos.CENTER);

            javafx.scene.shape.Circle sphere = new javafx.scene.shape.Circle(NODE_RADIUS);
            sphere.getStyleClass().add("case-sphere");

            Text title = new Text(caso.getNombre());
            title.setFill(Color.WHITE);
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            title.setWrappingWidth(NODE_DIAMETER - 12);
            title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            VBox content = new VBox(2, sphere, title);
            content.setAlignment(Pos.CENTER);
            getChildren().add(content);
        }

        private void setSelected(boolean selected) {
            if (selected) {
                if (!getStyleClass().contains("selected")) {
                    getStyleClass().add("selected");
                }
            } else {
                getStyleClass().remove("selected");
            }
        }

        private Caso getCaso() {
            return caso;
        }

        private void setBoardPosition(double x, double y) {
            setLayoutX(x);
            setLayoutY(y);
        }

        private double centerX() {
            return getLayoutX() + NODE_RADIUS;
        }

        private double centerY() {
            return getLayoutY() + NODE_RADIUS;
        }
    }
}