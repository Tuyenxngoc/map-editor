package com.teamobi.map.controller;

import com.teamobi.map.model.MapEntry;
import com.teamobi.map.model.PlayerEntry;
import com.teamobi.map.utils.Utils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

/**
 * @author tuyen
 */
public class MainController {
    private static final String ICON_URL = "src/main/resources/com/teamobi/map/images/map/%d.png";
    private static final String PLAYER_URL = "src/main/resources/com/teamobi/map/images/player.png";
    private static final double GRID_SIZE = 50.0;

    @FXML
    private BorderPane borderPane;
    @FXML
    private GridPane gridPane;
    @FXML
    private AnchorPane mapEdit;
    @FXML
    private TextField textFieldX, textFieldY, textFieldIcon;
    @FXML
    private Label mouseXY, infoLabel;
    @FXML
    private MenuItem menuItemPreview, menuItemResize, menuItemSnapToGrid;
    @FXML
    private GridPane gridPosition, gridControl;
    @FXML
    private Button btnRemove, btnAddIcon, btnAddPlayer;

    private boolean isSnapToGridEnabled = false;
    private boolean isReadyToMove = false;
    private ImageView selectedImageView = null;
    private MapEntry selectedMapEntry = null;
    private PlayerEntry selectedPlayerEntry = null;
    private final ArrayList<MapEntry> mapEntries = new ArrayList<>();
    private final ArrayList<PlayerEntry> playerEntries = new ArrayList<>();
    private String name;
    private short width, height;
    private double gridWidth = GRID_SIZE, gridHeight = GRID_SIZE;

    public void setSize(short w, short h) {
        width = w;
        height = h;
        mapEdit.setPrefSize(w, h);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateInfoLabel() {
        infoLabel.setText("MapBrick: " + mapEntries.size() + " - Player: " + playerEntries.size());
    }

    private void highlightSelectedItem(ImageView selectedItem) {
        for (Node node : mapEdit.getChildren()) {
            if (node instanceof ImageView) {
                node.getStyleClass().remove("highlighted");
            }
        }

        selectedItem.getStyleClass().add("highlighted");
        selectedItem.toFront();
    }

    private void handleImageIconClick(ImageView imgV, MapEntry mapEntry) {
        isReadyToMove = selectedMapEntry == mapEntry;

        gridPosition.setDisable(false);
        gridControl.setDisable(false);
        btnRemove.setDisable(false);
        selectedImageView = imgV;
        selectedMapEntry = mapEntry;
        selectedPlayerEntry = null;

        highlightSelectedItem(imgV);

        textFieldIcon.setText("" + mapEntry.icon);
        textFieldX.setText("" + mapEntry.x);
        textFieldY.setText("" + mapEntry.y);
    }

    private void handleImagePlayerClick(ImageView imgV, PlayerEntry playerEntry) {
        isReadyToMove = selectedPlayerEntry == playerEntry;

        gridPosition.setDisable(true);
        gridControl.setDisable(false);
        btnRemove.setDisable(false);
        selectedImageView = imgV;
        selectedMapEntry = null;
        selectedPlayerEntry = playerEntry;

        highlightSelectedItem(imgV);
    }

    private void createAndDisplayMapImageView(MapEntry mapEntry) {
        File file = new File(String.format(ICON_URL, mapEntry.icon));
        Image image = new Image(file.toURI().toString());
        ImageView imgV = new ImageView(image);
        imgV.setX(mapEntry.x);
        imgV.setY(mapEntry.y);
        imgV.setOnMouseClicked(event -> handleImageIconClick(imgV, mapEntry));
        mapEdit.getChildren().add(imgV);
    }

    private void createAndDisplayPlayerImageView(PlayerEntry player) {
        File file = new File(PLAYER_URL);
        Image image = new Image(file.toURI().toString());
        ImageView imgV = new ImageView(image);
        imgV.setX(player.x);
        imgV.setY(player.y);
        imgV.setOnMouseClicked(event -> handleImagePlayerClick(imgV, player));
        mapEdit.getChildren().add(imgV);
    }

    private void updatePosition(short deltaX, short deltaY) {
        if (selectedMapEntry != null) {
            selectedMapEntry.x += deltaX;
            selectedMapEntry.y += deltaY;

            textFieldX.setText(String.valueOf(selectedMapEntry.x));
            textFieldY.setText(String.valueOf(selectedMapEntry.y));

            selectedImageView.setX(selectedMapEntry.x);
            selectedImageView.setY(selectedMapEntry.y);
        } else if (selectedPlayerEntry != null) {
            selectedPlayerEntry.x += deltaX;
            selectedPlayerEntry.y += deltaY;

            selectedImageView.setX(selectedPlayerEntry.x);
            selectedImageView.setY(selectedPlayerEntry.y);
        }
    }

    private void drawGrid() {
        clearGrid();

        for (double x = 0; x < mapEdit.getWidth(); x += gridWidth) {
            Line verticalLine = new Line(x, 0, x, mapEdit.getHeight());
            verticalLine.setStroke(Color.GRAY);
            verticalLine.setStrokeWidth(0.5);
            mapEdit.getChildren().add(verticalLine);
        }

        for (double y = 0; y < mapEdit.getHeight(); y += gridHeight) {
            Line horizontalLine = new Line(0, y, mapEdit.getWidth(), y);
            horizontalLine.setStroke(Color.GRAY);
            horizontalLine.setStrokeWidth(0.5);
            mapEdit.getChildren().add(horizontalLine);
        }
    }

    private void clearGrid() {
        mapEdit.getChildren().removeIf(node -> node instanceof Line);
    }

    public void loadMap(byte[] mapData) {
        mapEntries.clear();
        playerEntries.clear();

        int offset = 0;
        width = Utils.bytesToShort(mapData, offset);
        offset += 2;
        height = Utils.bytesToShort(mapData, offset);
        offset += 2;

        int entryCount = mapData[offset++];
        for (byte i = 0; i < entryCount; i++) {
            byte brickId = mapData[offset];
            short x = Utils.bytesToShort(mapData, offset + 1);
            short y = Utils.bytesToShort(mapData, offset + 3);
            MapEntry mapEntry = new MapEntry(brickId, x, y);
            mapEntries.add(mapEntry);
            offset += 5;
        }

        byte playerPointCount = mapData[offset++];
        for (byte i = 0; i < playerPointCount; i++) {
            short x = Utils.bytesToShort(mapData, offset);
            offset += 2;
            short y = Utils.bytesToShort(mapData, offset);
            offset += 2;

            PlayerEntry player = new PlayerEntry((short) (x - 16), (short) (y - 36));
            playerEntries.add(player);
        }
    }

    private byte[] saveMap() {
        int iconCount = mapEntries.size();
        int playerCount = playerEntries.size();
        int mapDataLength = 6 + (iconCount * 5) + (playerCount * 4);
        byte[] mapByteData = new byte[mapDataLength];
        Utils.shortToBytes(mapByteData, 0, width);
        Utils.shortToBytes(mapByteData, 2, height);
        mapByteData[4] = (byte) iconCount;
        int index = 5;

        for (MapEntry map : mapEntries) {
            mapByteData[index] = map.icon;
            Utils.shortToBytes(mapByteData, index + 1, map.x);
            Utils.shortToBytes(mapByteData, index + 3, map.y);
            index += 5;
        }
        mapByteData[index] = (byte) playerCount;

        for (PlayerEntry player : playerEntries) {
            Utils.shortToBytes(mapByteData, index + 1, (short) (player.x + 16));
            Utils.shortToBytes(mapByteData, index + 3, (short) (player.y + 36));
            index += 4;
        }
        return mapByteData;
    }

    public void showMap() {
        menuItemResize.setDisable(false);
        menuItemSnapToGrid.setDisable(false);
        gridPane.setDisable(false);
        menuItemPreview.setDisable(false);
        mapEdit.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(1), new Insets(0, 0, 0, 0))));
        mapEdit.getChildren().clear();
        mapEdit.setPrefSize(width, height);
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.setTitle(name);
        for (MapEntry mapEntry : mapEntries) {
            createAndDisplayMapImageView(mapEntry);
        }
        for (PlayerEntry player : playerEntries) {
            createAndDisplayPlayerImageView(player);
        }
        updateInfoLabel();
    }

    @FXML
    public void aboutShow() {
        showAlert(Alert.AlertType.INFORMATION, "About", "Application Information",
                "Map Editor\nVersion: 1.0.0\nProducts belong to Tuyenngoc");
    }

    @FXML
    public void openSettingsDialog() {
        try {
            Stage primaryStage = new Stage();

            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(320, 180);

            GridPane gridPane = new GridPane();
            gridPane.setPrefSize(220, 125);
            gridPane.setLayoutX(30);
            gridPane.setLayoutY(30);
            gridPane.setHgap(15);
            gridPane.setVgap(20);
            gridPane.add(new Label("Width: "), 0, 0);
            gridPane.add(new Label("Height: "), 0, 1);
            TextField w = new TextField(String.valueOf(gridWidth));
            TextField h = new TextField(String.valueOf(gridHeight));
            gridPane.add(w, 1, 0);
            gridPane.add(h, 1, 1);
            anchorPane.getChildren().add(gridPane);
            Button button = new Button("Save");
            button.setLayoutX(170);
            button.setLayoutY(135);
            button.setOnAction(event -> {
                try {
                    double gridWidth = Double.parseDouble(w.getText());
                    if (gridWidth < 0) {
                        throw new NumberFormatException("Grid width cannot be negative.");
                    }
                    double gridHeight = Double.parseDouble(h.getText());
                    if (gridHeight < 0) {
                        throw new NumberFormatException("Grid height cannot be negative.");
                    }

                    this.gridWidth = gridWidth;
                    this.gridHeight = gridHeight;
                    drawGrid();
                    primaryStage.close();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", null, "Please enter valid numeric values.\n" + ex.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            anchorPane.getChildren().add(button);
            primaryStage.setScene(new Scene(anchorPane));
            primaryStage.setTitle("Grid Size Settings");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void previewShow() {
        Stage stage = new Stage();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefSize(width, height);
        for (MapEntry map : mapEntries) {
            File file = new File(String.format(ICON_URL, map.icon));
            Image image = new Image(file.toURI().toString());
            ImageView imgV = new ImageView(image);
            imgV.setX(map.x);
            imgV.setY(map.y);
            anchorPane.getChildren().add(imgV);
        }
        for (PlayerEntry playerEntry : playerEntries) {
            File file = new File(PLAYER_URL);
            Image image = new Image(file.toURI().toString());
            ImageView imgV = new ImageView(image);
            imgV.setX(playerEntry.x);
            imgV.setY(playerEntry.y);
            anchorPane.getChildren().add(imgV);
        }
        Scene scene = new Scene(anchorPane);
        stage.setTitle("Preview");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void openFileAction() {
        Stage stage = (Stage) (borderPane.getScene().getWindow());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a map");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                name = file.getName();
                byte[] mapData = Utils.getFile(file.getPath());
                loadMap(mapData);
                showMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void saveAction() {
        Stage stage = (Stage) (borderPane.getScene().getWindow());
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Map Files (*.map)", "*.map");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(name);
        File fileToSave = fileChooser.showSaveDialog(stage);
        if (fileToSave != null) {
            String filePath = fileToSave.getAbsolutePath();

            byte[] mapByteData = saveMap();
            Utils.saveFile(filePath, mapByteData);
        }
    }

    @FXML
    public void newShow() {
        try {
            Stage primaryStage = new Stage();

            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(280, 210);

            GridPane gridPane = new GridPane();
            gridPane.setPrefSize(240, 125);
            gridPane.setLayoutX(30);
            gridPane.setLayoutY(30);
            gridPane.setHgap(15);
            gridPane.setVgap(20);
            gridPane.add(new Label("Name: "), 0, 0);
            gridPane.add(new Label("Width: "), 0, 1);
            gridPane.add(new Label("Height: "), 0, 2);
            TextField name = new TextField("STT_NAME");
            TextField width = new TextField("500");
            TextField height = new TextField("500");
            gridPane.add(name, 1, 0);
            gridPane.add(width, 1, 1);
            gridPane.add(height, 1, 2);
            anchorPane.getChildren().add(gridPane);
            Button button = new Button("Create");
            button.setLayoutX(185);
            button.setLayoutY(170);
            button.setOnAction(event -> {
                try {
                    this.name = name.getText();
                    this.width = Short.parseShort(width.getText());
                    this.height = Short.parseShort(height.getText());
                    mapEntries.clear();
                    playerEntries.clear();
                    primaryStage.close();
                    showMap();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", null, "Please enter valid numeric values.\n" + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            anchorPane.getChildren().add(button);
            primaryStage.setScene(new Scene(anchorPane));
            primaryStage.setTitle("New Map");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void addIconShow() {
        try {
            Stage primaryStage = new Stage();

            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(280, 210);

            GridPane gridPane = new GridPane();
            gridPane.setPrefSize(240, 125);
            gridPane.setLayoutX(30);
            gridPane.setLayoutY(30);
            gridPane.setHgap(15);
            gridPane.setVgap(20);
            gridPane.add(new Label("Icon: "), 0, 0);
            gridPane.add(new Label("X: "), 0, 1);
            gridPane.add(new Label("Y: "), 0, 2);
            TextField icon = new TextField("0");
            TextField x = new TextField("0");
            TextField y = new TextField("0");
            gridPane.add(icon, 1, 0);
            gridPane.add(x, 1, 1);
            gridPane.add(y, 1, 2);
            anchorPane.getChildren().add(gridPane);
            Button button = new Button("Add");
            button.setLayoutX(185);
            button.setLayoutY(170);
            button.setOnAction(event -> {
                try {
                    MapEntry mapEntry = new MapEntry(
                            Byte.parseByte(icon.getText()),
                            Short.parseShort(x.getText()),
                            Short.parseShort(y.getText()));
                    mapEntries.add(mapEntry);

                    createAndDisplayMapImageView(mapEntry);
                    updateInfoLabel();
                    primaryStage.close();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", null, "Please enter valid numeric values.\n" + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            anchorPane.getChildren().add(button);
            primaryStage.setScene(new Scene(anchorPane));
            primaryStage.setTitle("Add Icon");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void addPlayerShow() {
        if (playerEntries.size() >= 8) {
            showAlert(Alert.AlertType.ERROR, "Error", null, "The number of players has reached 8.");
            return;
        }

        try {
            Stage primaryStage = new Stage();

            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(240, 180);

            GridPane gridPane = new GridPane();
            gridPane.setPrefSize(220, 125);
            gridPane.setLayoutX(30);
            gridPane.setLayoutY(30);
            gridPane.setHgap(15);
            gridPane.setVgap(20);
            gridPane.add(new Label("X: "), 0, 0);
            gridPane.add(new Label("Y: "), 0, 1);
            TextField x = new TextField("0");
            TextField y = new TextField("0");
            gridPane.add(x, 1, 0);
            gridPane.add(y, 1, 1);
            anchorPane.getChildren().add(gridPane);
            Button button = new Button("Add");
            button.setLayoutX(170);
            button.setLayoutY(135);
            button.setOnAction(event -> {
                try {
                    PlayerEntry player = new PlayerEntry(
                            (short) (Short.parseShort(x.getText()) - 16),
                            (short) (Short.parseShort(y.getText()) - 36));
                    playerEntries.add(player);

                    createAndDisplayPlayerImageView(player);
                    updateInfoLabel();
                    primaryStage.close();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", null, "Please enter valid numeric values.\n" + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            anchorPane.getChildren().add(button);
            primaryStage.setScene(new Scene(anchorPane));
            primaryStage.setTitle("Add Player");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void upAction() {
        updatePosition((short) 0, (short) -1);
    }

    @FXML
    public void downAction() {
        updatePosition((short) 0, (short) 1);
    }

    @FXML
    public void leftAction() {
        updatePosition((short) -1, (short) 0);
    }

    @FXML
    public void rightAction() {
        updatePosition((short) 1, (short) 0);
    }

    @FXML
    public void clicked(MouseEvent e) {
        if (!isReadyToMove) {
            return;
        }
        try {
            double mouseX = e.getX();
            double mouseY = e.getY();

            if (isSnapToGridEnabled) {
                mouseX = Math.round(mouseX / gridWidth) * gridWidth;
                mouseY = Math.round(mouseY / gridHeight) * gridHeight;
            }

            if (selectedMapEntry != null) {
                selectedMapEntry.x = (short) mouseX;
                selectedMapEntry.y = (short) mouseY;

                selectedImageView.setX(selectedMapEntry.x);
                selectedImageView.setY(selectedMapEntry.y);

                textFieldX.setText("" + selectedMapEntry.x);
                textFieldY.setText("" + selectedMapEntry.y);
            } else if (selectedPlayerEntry != null) {
                selectedPlayerEntry.x = (short) mouseX;
                selectedPlayerEntry.y = (short) mouseY;

                selectedImageView.setX(selectedPlayerEntry.x);
                selectedImageView.setY(selectedPlayerEntry.y);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void resizeShow() {
        try {
            Stage primaryStage = new Stage();

            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setPrefSize(240, 180);

            GridPane gridPane = new GridPane();
            gridPane.setPrefSize(220, 125);
            gridPane.setLayoutX(30);
            gridPane.setLayoutY(30);
            gridPane.setHgap(15);
            gridPane.setVgap(20);
            gridPane.add(new Label("Width: "), 0, 0);
            gridPane.add(new Label("Height: "), 0, 1);
            TextField w = new TextField(String.valueOf(width));
            TextField h = new TextField(String.valueOf(height));
            gridPane.add(w, 1, 0);
            gridPane.add(h, 1, 1);
            anchorPane.getChildren().add(gridPane);
            Button button = new Button("Resize");
            button.setLayoutX(170);
            button.setLayoutY(135);
            button.setOnAction(event -> {
                try {
                    short wid = Short.parseShort(w.getText());
                    short hei = Short.parseShort(h.getText());
                    setSize(wid, hei);
                    primaryStage.close();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", null, "Please enter valid numeric values.\n" + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            anchorPane.getChildren().add(button);
            primaryStage.setScene(new Scene(anchorPane));
            primaryStage.setTitle("Resize Map");
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void toggleSnapToGrid() {
        isSnapToGridEnabled = !isSnapToGridEnabled;

        if (isSnapToGridEnabled) {
            drawGrid();
        } else {
            clearGrid();
        }
    }

    @FXML
    public void mouseXY(MouseEvent e) {
        short mouseX = (short) e.getX();
        short mouseY = (short) e.getY();
        mouseXY.setText("X: " + mouseX + " - Y: " + mouseY);
    }

    @FXML
    public void inputEnter(KeyEvent k) {
        if (k.getCode().equals(KeyCode.ENTER)) {
            try {
                byte icon = Byte.parseByte(this.textFieldIcon.getText());
                if (icon < 1 || icon > 126) {
                    throw new NumberFormatException("For input string: \"" + this.textFieldIcon.getText() + "\"");
                }
                short x = Short.parseShort(this.textFieldX.getText());
                short y = Short.parseShort(this.textFieldY.getText());
                MapEntry map = selectedMapEntry;
                map.icon = icon;
                map.x = x;
                map.y = y;
                File file = new File(String.format(ICON_URL, map.icon));
                Image img = new Image(file.toURI().toString());
                selectedImageView.setImage(img);
                selectedImageView.setX(map.x);
                selectedImageView.setY(map.y);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", null, "Please enter valid integers within the following ranges:\n" +
                        "- Icon: byte (1 to 126)\n" +
                        "- X and Y: short (-32768 to 32767)\n" +
                        "\n" + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void removeAction() {
        if (selectedMapEntry != null || selectedPlayerEntry != null) {
            selectedImageView.setVisible(false);
            btnRemove.setDisable(true);
            gridControl.setDisable(true);
            gridPosition.setDisable(true);

            if (selectedMapEntry != null) {
                mapEntries.remove(selectedMapEntry);
                btnAddIcon.setDisable(false);
                selectedMapEntry = null;
            } else if (selectedPlayerEntry != null) {
                playerEntries.remove(selectedPlayerEntry);
                btnAddPlayer.setDisable(false);
                selectedPlayerEntry = null;
            }

            updateInfoLabel();
        }
    }

}
