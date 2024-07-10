package cz.upol.zp4jv.ukol09;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


// Logika hry umístěna v GameState
// Dále jednotlivé třídy Projectile, EnemyShip
// A výčty Direction a ProjectileStatus

public class SpaceInvadersApp extends Application {

    // Čas mezi jednotlivými aktualizacemi hry v jednotce ms
    // public static final int DELAY = 90;
    public static final int DELAY = 200;

    // V logice hry pracuji se sloupci
    // Zde je velikost jednoho sloupce v pixelech
    public static final int UNIT_SIZE = 25;

    // Stav hry
    private GameState gameState = new GameState();

    // Herní plocha
    private Pane gamePane;

    // Textový popisek se získanými body
    private Text scoreLabel;

    // Info Label
    private Text infoLabel;

    // Title
    private Text titleLabel;

    // Tlačítko start
    private Button startButton;

    // Úvodní panel
    private VBox startPane;

    // Zdroj casu pro aktualizace hry
    Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
        createGamePane();

        // Vytvoření labelu pro score
        scoreLabel = new Text();
        scoreLabel.textProperty().bind(gameState.scoreProperty().asString());

        // Vytvoření updatů pro hru
        timeline = new Timeline();
        KeyFrame updates = new KeyFrame(
                Duration.millis(DELAY),
                e -> {
                    createProjectiles();
                    if (!gameState.update()) {
                        gameOver();
                    }
                });
        timeline.getKeyFrames().add(updates);
        timeline.setCycleCount(Animation.INDEFINITE);

        // Vytvoření informací při spuštění

        // Vytvořím panel, pro info
        startPane = new VBox(20);
        startPane.setAlignment(Pos.CENTER);
        // Title
        titleLabel = new Text("SPACE INVADERS!");
        titleLabel.setFill(Color.WHITE);
        titleLabel.setScaleX(2);
        titleLabel.setScaleY(2);

        // Tlačítko
        startButton = new Button("Start");
        startButton.visibleProperty().bind(gameState.isActiveProperty().not());
        startButton.setOnAction(e -> {
            startPane.setVisible(false);
            gameState.reinitialize();
            gameState.setIsActive(true);
            gamePane.setOpacity(1);
            timeline.play();
        });
        startButton.setScaleX(2);
        startButton.setScaleY(2);

        // InfoLabel
        infoLabel = new Text("Use Arrows to move and space to shoot.");
        infoLabel.setFill(Color.WHITE);

        // Přidání do úvodního panelu
        startPane.getChildren().addAll(titleLabel, infoLabel, startButton);

        // Vytvoření hlavního panelu
        BorderPane mainPane = new BorderPane();

        // Vytvoření herní plochy
        StackPane gameStack = new StackPane();
        gameStack.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        gameStack.getChildren().addAll(gamePane, startPane);
        gameStack.setAlignment(Pos.CENTER);

        // Score label
        HBox scorePane = new HBox(10, new Text("Score: "),scoreLabel);

        // Přidání do hlavního panelu
        mainPane.setTop(scorePane);
        mainPane.setCenter(gameStack);

        // Vytvoření hlavního okna, plátna
        Scene scene = new Scene(mainPane, GameState.COLUMNS * UNIT_SIZE, GameState.ROWS * UNIT_SIZE);
        scene.setOnKeyPressed(this::keyActionSetup);
        primaryStage.setTitle("SPACE INVANDERS");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Metody
    // Vytvoření herní plochy
    private void createGamePane() {
        gamePane = new Pane();
        gamePane.setMaxWidth(UNIT_SIZE * GameState.COLUMNS);
        gamePane.setMaxHeight(UNIT_SIZE * GameState.ROWS);

        // Inicializace nepřátel
        for(int i = 0; i < gameState.getEnemies().size(); i++) {
            try {
                EnemyShip s = gameState.getEnemies().get(i);
                gamePane.getChildren().add(createEnemy(s));
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        // Inicializace hráče
        try {
            gamePane.getChildren().add(createPlayer());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Inicializace střel -> na začátku žádné -> musím přidávat při běhu programu
    }

    // Inicializace a aktualizace střel
    private void createProjectiles() {
        for(int i = 0; i < gameState.getProjectiles().size(); i++) {

            try {
                Projectile p = gameState.getProjectiles().get(i);
                gamePane.getChildren().add(createProjectile(p));
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Vytvoření nepřítele + provázání
    private ImageView createEnemy(EnemyShip enemy) throws FileNotFoundException {
        // Vytvoření ikony
        InputStream stream = new FileInputStream("resources/EnemyImage.png");
        Image enemyImage = new Image(stream);
        ImageView result = new ImageView(enemyImage);

        // Provázání vlastností
        result.xProperty().bind(enemy.colProperty().multiply(UNIT_SIZE));
        result.yProperty().bind(enemy.rowProperty().multiply(UNIT_SIZE));
        result.visibleProperty().bind(enemy.isActiveProperty());

        // Úprava ikony
        result.setFitHeight(UNIT_SIZE);
        result.setFitWidth(UNIT_SIZE);
        result.setPreserveRatio(true);

        return result;
    }

    // Vytvoření hráče
    private ImageView createPlayer() throws FileNotFoundException {
        // Vytvoření ikony
        InputStream stream = new FileInputStream("resources/PlayerImage.png");
        Image playerImage = new Image(stream);
        ImageView result = new ImageView(playerImage);


        // Provázání vlastností
        result.xProperty().bind(gameState.playerPosProperty().multiply(UNIT_SIZE));
        result.setY((GameState.PLAYER_ROW - 1) * UNIT_SIZE);

        // Úprava ikony
        result.setFitHeight(UNIT_SIZE);
        result.setFitWidth(UNIT_SIZE);
        result.setPreserveRatio(true);

        return result;
    }

    // Vytvoření projectilu -> enemy - RED, player - GOLD
    private Ellipse createProjectile(Projectile p) {
        Ellipse result = new Ellipse(UNIT_SIZE / 6, UNIT_SIZE / 6 - 1);
        if(p.getStatus() == ProjectileStatus.ENEMY) {
            result.setFill(Color.RED);
        }
        else {
            result.setFill(Color.GOLD);
        }
        // Provázání vlastností - > + půl UNIT_SIZE aby střela šla přímo ze střílny mého obrázku
        result.centerXProperty().bind(p.colProperty().multiply(UNIT_SIZE).add(UNIT_SIZE / 2));
        result.centerYProperty().bind(p.rowProperty().multiply(UNIT_SIZE));
        result.visibleProperty().bind(p.isActiveProperty());

        return result;
    }

    // Game over
    private void gameOver() {
        timeline.stop();
        gamePane.setOpacity(0.2);
        startPane.setVisible(true);
    }

    // Navázání akcí na klávesy
    private void keyActionSetup(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT:
                gameState.moveLeft();
                break;
            case RIGHT:
                gameState.moveRight();
                break;
            case SPACE:
                gameState.playerShooted();
                break;
            default:
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
