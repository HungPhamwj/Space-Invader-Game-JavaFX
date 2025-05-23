package com.example.spaceshootergamejavafx;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Main game class for the Space Shooter game. */
public class SpaceShooter extends Application {

  /** Width of the game window. */
  public static final int WIDTH = 500;

  /** Height of the game window. */
  public static final int HEIGHT = 900;

  /** Number of lives the player starts with. */
  public static int numLives = 3;

  /** Current score of the player. */
  private int score = 0;

  /** Number of bosses defeated by the player. */
  private int BossesDefeated = 0;

  /** Flag to indicate if a boss enemy exists. */
  private boolean bossExists = false;

  /** Flag to indicate if the game should be reset. */
  private boolean reset = false;

  /** Condition to check if the game is end. */
  private final int MAX_BOSES = 5;

  /** Achievement variable MAX_SCORE. */
  private int highestScore = 0;

  /** Label to display the player's score. */
  private final Label scoreLabel = new Label("Score: " + score);

  /** Label to display the player's remaining lives. */
  private final Label lifeLabel = new Label("Lives: " + numLives);

  /** Label to display the number of bosses defeated. */
  private final Label BossDefeatLable =
      new Label("Bosses Defeated: " + BossesDefeated + "/" + MAX_BOSES);

  /** Label to display the hightest score. */
  private final Label HighestScoreLabel =
      new Label("Highest Score: " + highestScore);

  /** List of game objects in the game. */
  private final List<GameObject> gameObjects = new ArrayList<>();

  /** List of new game objects to add to the game. */
  private final List<GameObject> newObjects = new ArrayList<>();

  /** Player object for the game. */
  private Player player = new Player(WIDTH / 2, HEIGHT - 40);

  /** Root pane for the game scene. */
  private Pane root = new Pane();

  /** Game scene for the game. */
  private Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

  /** Flag to indicate if the level up message has been displayed. */
  private boolean levelUpMessageDisplayed = false;

  /** Flag to indicate if the level up message has beenbos shown. */
  private boolean levelUpShown = false;

  /** Primary stage for the game. */
  private Stage primaryStage;

  /** Flag to indicate if the game is running. */
  private boolean gameRunning = false;

  /** Background music player for the game. */
  private MediaPlayer menuMusicPlayer;

  /** background screen. */
  private Image backgroundImage;
  private double backgroundY = 0;
  private final double backgroundSpeed = 2.0;

  /** Flag to indicate if AI mode is active. */
  private boolean aiMode = false;
  private AIController aiController;

  /** Transition for power up reset. */
  private PauseTransition powerUpResetTransition = null;

  /** Main method to launch the game. */
  public static void main(String[] args) { launch(args); }

  /**
   * Starts the game and initializes the game window.
   *
   * @param primaryStage The primary stage for the game
   */
  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    primaryStage.setScene(scene);
    primaryStage.setTitle("Space Shooter");
    primaryStage.setResizable(false);
    primaryStage.getIcons().add(new Image(
        Objects.requireNonNull(getClass().getResourceAsStream("/player.png"))));

    Canvas canvas = new Canvas(WIDTH, HEIGHT);
    scoreLabel.setTranslateX(10);
    scoreLabel.setTranslateY(10);
    scoreLabel.setTextFill(Color.LIGHTSTEELBLUE);
    scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

    backgroundImage = new Image(Objects.requireNonNull(
        getClass().getResourceAsStream("/background.png")));
    root.getChildren().addAll(canvas, scoreLabel, lifeLabel, BossDefeatLable);

    lifeLabel.setTranslateX(10);
    lifeLabel.setTranslateY(40);
    lifeLabel.setTextFill(Color.LIGHTSTEELBLUE);
    lifeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

    BossDefeatLable.setTranslateX(10);
    BossDefeatLable.setTranslateY(70);
    BossDefeatLable.setTextFill(Color.LIGHTSTEELBLUE);
    BossDefeatLable.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

    HighestScoreLabel.setTranslateX(10);
    HighestScoreLabel.setTranslateY(100);
    HighestScoreLabel.setTextFill(Color.LIGHTSTEELBLUE);
    HighestScoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));

    root.getChildren().add(HighestScoreLabel);

    Pane menuPane = createMenu();
    Scene menuScene = new Scene(menuPane, WIDTH, HEIGHT);

    GraphicsContext gc = canvas.getGraphicsContext2D();
    gameObjects.add(player);

    primaryStage.setScene(menuScene);
    primaryStage.setTitle("Space Shooter");
    primaryStage.setResizable(false);
    initEventHandlers(scene);

    AnimationTimer gameLoop = new AnimationTimer() {
      private long lastEnemySpawned = 0;

      private long lastPowerUpSpawned = 0;

      @Override
      public void handle(long now) {
        if (!gameRunning)
          return;

        if (reset) {
          this.start();
          reset = false;
        }

        // Update background location
        backgroundY += backgroundSpeed;
        if (backgroundY >= HEIGHT) {
          backgroundY = 0;
        }

        // Draw two images in series to create a continuous scrolling effect
        gc.drawImage(backgroundImage, 0, backgroundY, WIDTH, HEIGHT);
        gc.drawImage(backgroundImage, 0, backgroundY - HEIGHT, WIDTH, HEIGHT);

        if (now - lastEnemySpawned > 1_000_000_000) {
          spawnEnemy();
          lastEnemySpawned = now;
        }

        if (now - lastPowerUpSpawned > 10_000_000_000L) {
          spawnPowerUp();
          lastPowerUpSpawned = now;
        }

        if (score >= 100 && score % 100 == 0) {
          boolean bossExists = false;
          for (GameObject obj : gameObjects) {
            if (obj instanceof BossEnemy) {
              bossExists = true;
              break;
            }
          }
          if (!bossExists) {
            spawnBossEnemy();
          }
        }

        checkCollisions();
        checkEnemiesReachingBottom();

        gameObjects.addAll(newObjects);
        newObjects.clear();

        for (GameObject obj : gameObjects) {
          obj.update();
          obj.render(gc);
        }

        if (aiMode && aiController != null) {
          aiController.update();
        }

        Iterator<GameObject> iterator = gameObjects.iterator();
        while (iterator.hasNext()) {
          GameObject obj = iterator.next();
          if (obj.isDead()) {
            iterator.remove();
          }
        }
      }
    };

    gameLoop.start();
    primaryStage.show();
  }

  /**
   * Checks for collisions between game objects and updates the score and game
   * state accordingly.
   */
  private void checkCollisions() {
    List<Bullet> bullets = new ArrayList<>();
    List<Enemy> enemies = new ArrayList<>();
    List<PowerUp> powerUps = new ArrayList<>();
    List<Bomb> bombs = new ArrayList<>();
    for (GameObject obj : gameObjects) {
      if (obj instanceof Bullet) {
        bullets.add((Bullet)obj);
      } else if (obj instanceof Enemy) {
        enemies.add((Enemy)obj);
      } else if (obj instanceof PowerUp) {
        powerUps.add((PowerUp)obj);
      } else if (obj instanceof Bomb) {
        bombs.add((Bomb)obj);
      }
    }

    for (Bullet bullet : bullets) {
      for (Enemy enemy : enemies) {
        if (bullet.getBounds().intersects(enemy.getBounds())) {
          bullet.setDead(true);
          if (enemy instanceof BossEnemy) {
            ((BossEnemy)enemy).takeDamage();
            score += 50;
            if (((BossEnemy)enemy).isDead()) {
              bossExists = false;
              BossesDefeated++;
              if (BossesDefeated >= MAX_BOSES) {
                gameRunning = false;
                showWinningScreen();
                return;
              }
            }
          } else {
            enemy.setDead(true);
            score += 10;
          }
          scoreLabel.setText("Score: " + score);
          BossDefeatLable.setText("Bosses Defeated: " + BossesDefeated + "/" + MAX_BOSES);

          if (score % 100 == 0) {
            Enemy.SPEED += 0.8;
          }
        }
      }

      // Check collisions between bullets and power-ups
      for (PowerUp powerUp : powerUps) {
        if (bullet.getBounds().intersects(powerUp.getBounds())) {
          bullet.setDead(true);
          powerUp.setDead(true);
          playPowerupSound();

          score += 50;
          scoreLabel.setText("Score: " + score);
          player.setBulletLevel(player.getBulletLevel() + 1);

          if (powerUpResetTransition != null) {
            powerUpResetTransition.stop();
          }
          // Duration of the Bullet Level > 1
          powerUpResetTransition = new PauseTransition(Duration.seconds(10));
          powerUpResetTransition.setOnFinished(event -> {
            player.setBulletLevel(1);
            powerUpResetTransition = null;
          });
          powerUpResetTransition.play();
        }
      }
    }

    // Compare score with highest score
    if (score > highestScore) {
      highestScore = score;
      HighestScoreLabel.setText("Highest Score: " + highestScore);
    }

    if (score % 100 == 0 && score > 0 && !levelUpShown) {
      showTempMessage("Level Up!", 230, HEIGHT / 2, 2);
      levelUpShown = true;
    } else if (score % 100 != 0) {
      levelUpShown = false;
    }

    for (Bomb bomb : bombs) {
      if (bomb.getBounds().intersects(player.getBounds())) {
        bomb.setDead(true);
        numLives--;
        lifeLabel.setText("Lives: " + numLives);
        if (numLives < 0) {
          gameRunning = false;
          showLosingScreen();
          return;
        }
      }
    }

    // Check collisions between power-ups and the player
    for (PowerUp powerUp : powerUps) {
      if (powerUp.getBounds().intersects(player.getBounds())) {
        powerUp.setDead(true);
        player.setBulletLevel(player.getBulletLevel() + 1);
      }
    }
  }

  /**
   * Checks if any enemies have reached the bottom of the screen and updates the
   * game state accordingly.
   */
  private void checkEnemiesReachingBottom() {
    List<Enemy> enemies = new ArrayList<>();

    for (GameObject obj : gameObjects) {
      if (obj instanceof Enemy) {
        enemies.add((Enemy)obj);
      }
    }

    for (Enemy enemy : enemies) {
      if (enemy.getY() + enemy.getHeight() / 2 >= HEIGHT) {
        enemy.setDead(true);
        enemy.SPEED = enemy.SPEED + 0.8;
        numLives--;
        score -= 10;
        lifeLabel.setText("Lives: " + numLives);
        if (numLives < 0) {
          gameRunning = false;
          showLosingScreen();
          return;
        }
      }
    }
  }

  /** Restarts the game when the player chooses to try again. */
  private void restartGame() {
    resetGameState();
    primaryStage.setScene(scene);
    menuMusicPlayer.play();
  }

  /** Starts the game when the player clicks the start button. */
  private void startGame() {
    resetGameState();
    primaryStage.setScene(scene);
  }

  /** Starts the AI game when the player clicks the AI button. */
  private void startAIGame() {
    resetGameState();
    aiController = new AIController(player, gameObjects, newObjects);
    aiMode = true;
    primaryStage.setScene(scene);
  }

  /** Reset game state when player enters the button menu/try-again. */
  private void resetGameState() {
    gameObjects.clear();
    numLives = 3;
    score = 0;
    BossesDefeated = 0;
    bossExists = false;
    aiMode = false;
    aiController = null;
    Enemy.SPEED = 1.0;
    lifeLabel.setText("Lives: " + numLives);
    scoreLabel.setText("Score: " + score);
    BossDefeatLable.setText("Bosses Defeated: " + BossesDefeated + "/" + MAX_BOSES);
    reset = true;
    gameRunning = true;
    if (menuMusicPlayer != null) {
      menuMusicPlayer.play();
    }

    // Reset trạng thái player về mặc định
    player = new Player(WIDTH / 2, HEIGHT - 40);
    player.setBulletLevel(1);

    gameObjects.add(player);
    initEventHandlers(scene);
  }

  /** Spawns an enemy at a random x-coordinate at the top of the screen. */
  private void spawnEnemy() {
    Random random = new Random();
    int x = random.nextInt(WIDTH - 50) + 25;

    if (score % 200 == 0 && score > 0 && !bossExists) {
      BossEnemy boss = new BossEnemy(x, -50);
      boss.setNewObjects(newObjects);
      gameObjects.add(boss);
      showTempMessage("A boss is ahead, watch out!", 200, HEIGHT / 2 - 200, 5);
      bossExists = true; // Ensure we don't spawn multiple bosses
    } else {
      Enemy enemy = new Enemy(x, -40);
      gameObjects.add(enemy);
    }
  }

  /** Spawns a power-up at a random x-coordinate at the top of the screen. */
  private void spawnPowerUp() {
    Random random = new Random();
    int x = random.nextInt(WIDTH - PowerUp.WIDTH) + PowerUp.WIDTH / 2;
    PowerUp powerUp = new PowerUp(x, -PowerUp.HEIGHT / 2);
    gameObjects.add(powerUp);
  }

  /** Spawns a boss enemy at the top of the screen. */
  private void spawnBossEnemy() {
    if (gameObjects.stream().noneMatch(obj -> obj instanceof BossEnemy)) {
      BossEnemy bossEnemy = new BossEnemy(WIDTH / 2, -40);
      gameObjects.add(bossEnemy);
    }
  }

  /** Create button exit and button try again, button menu. */
  private Button exitButton = new Button("Exit Game");
  private Button tryAgainButton = new Button("Try Again");
  private Button menuButton = new Button("Menu");

  /** Method to create the try again button. */
  private Button TryButton() {
    tryAgainButton.setStyle(
        "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 18; "
        +
        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-family: 'Verdana';"
        + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
    tryAgainButton.setOnMouseEntered(event -> {
      tryAgainButton.setStyle("-fx-background-color: white; -fx-text-fill: "
                              + "black; -fx-font-size: 18; "
                              + "-fx-font-weight: bold; -fx-padding: 10 20; "
                              + "-fx-font-family: 'Verdana';"
                              +
                              "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      tryAgainButton.setEffect(new Glow(0.5));
    });
    tryAgainButton.setOnMouseExited(event -> {
      tryAgainButton.setStyle("-fx-background-color: #444; -fx-text-fill: "
                              + "white; -fx-font-size: 18; "
                              + "-fx-font-weight: bold; -fx-padding: 10 20; "
                              + "-fx-font-family: 'Verdana';"
                              +
                              "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      tryAgainButton.setEffect(null);
    });
    tryAgainButton.setLayoutX(180);
    tryAgainButton.setLayoutY(350);
    tryAgainButton.setPrefWidth(150);
    tryAgainButton.setPrefHeight(50);
    tryAgainButton.setOnAction(event -> restartGame());

    return tryAgainButton;
  }

  /** Method to create button exit*/
  private Button ExitButton() {
    exitButton.setStyle(
        "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: "
        + "18; "
        +
        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-family: 'Verdana';"
        + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
    exitButton.setOnMouseEntered(event -> {
      exitButton.setStyle(
          "-fx-background-color: white; -fx-text-fill: red; -fx-font-size: 18; "
          + "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-family: "
          + "'Verdana';"
          + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      exitButton.setEffect(new Glow(0.5));
    });
    exitButton.setOnMouseExited(event -> {
      exitButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: "
                          + "white; -fx-font-size: 18; "
                          + "-fx-font-weight: bold; -fx-padding: 10 20; "
                          + "-fx-font-family: 'Verdana';"
                          + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      exitButton.setEffect(null);
    });
    exitButton.setLayoutX(180);
    exitButton.setLayoutY(450);
    exitButton.setPrefWidth(150);
    exitButton.setPrefHeight(50);
    exitButton.setOnAction(event -> System.exit(0));

    return exitButton;
  }

  /** Method to create menu button. */
  private Button MenuButton() {
    menuButton.setStyle(
        "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 18; "
        + "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-family: "
        + "'Verdana'; "
        + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
    menuButton.setOnMouseEntered(event -> {
      menuButton.setStyle("-fx-background-color: white; -fx-text-fill: "
                          + "black; -fx-font-size: 18; "
                          + "-fx-font-weight: bold; -fx-padding: 10 20; "
                          + "-fx-font-family: 'Verdana'; "
                          + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      menuButton.setEffect(new Glow(0.5));
    });
    menuButton.setOnMouseExited(event -> {
      menuButton.setStyle("-fx-background-color: #444; -fx-text-fill: white; "
                          + "-fx-font-size: 18; "
                          + "-fx-font-weight: bold; -fx-padding: 10 20; "
                          + "-fx-font-family: 'Verdana'; "
                          + "-fx-pref-width: 150px; -fx-pref-height: 50px;");
      menuButton.setEffect(null);
    });
    menuButton.setLayoutX(180);
    menuButton.setLayoutY(550);
    menuButton.setPrefWidth(150);
    menuButton.setPrefHeight(50);
    menuButton.setOnAction(even -> {
      Scene menuScene = new Scene(createMenu(), WIDTH, HEIGHT);
      primaryStage.setScene(menuScene);
    });
    return menuButton;
  }

  /** Shows the losing screen when the player loses all lives. */
  private void showLosingScreen() {
    // End of music background before showing losing screen.
    if (menuMusicPlayer != null) {
      menuMusicPlayer.stop();
    }

    Pane losingPane = new Pane();
    losingPane.setStyle("-fx-background-color: black;");

    // Game Over Text
    Text gameOverText = new Text("GAME OVER");
    gameOverText.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
    gameOverText.setFill(Color.RED);
    gameOverText.setX((WIDTH - gameOverText.getLayoutBounds().getWidth()) / 2);
    gameOverText.setY(150);

    // Score Display
    if (score < 0) {
      score = 0;
    }
    Text scoreText = new Text("Your Score: " + score);
    scoreText.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
    scoreText.setFill(Color.WHITE);
    scoreText.setX((WIDTH - scoreText.getLayoutBounds().getWidth()) / 2);
    scoreText.setY(250);

    // Create and set the losing screen scene
    Scene losingScene = new Scene(losingPane, WIDTH, HEIGHT);
    primaryStage.setScene(losingScene);
    losingPane.getChildren().addAll(gameOverText, scoreText, ExitButton(),
                                    TryButton(), MenuButton());

    // Play the losing sound
    playLosingSound();
  }

  /** Method to show the winning screen and winning sound. */
  private void showWinningScreen() {
    // Stop music background if it's playing
    if (menuMusicPlayer != null) {
      menuMusicPlayer.stop();
    }

    // Create a pane for the winning screen
    Pane winningPane = new Pane();
    winningPane.setStyle("-fx-background-color: black;");

    // Win game Text
    Text wingameText = new Text("You Win!");
    wingameText.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
    wingameText.setFill(Color.GREEN);
    wingameText.setX((WIDTH - wingameText.getLayoutBounds().getWidth()) / 2);
    wingameText.setY(150);

    // Score Display
    if (score < 0) {
      score = 0;
    }
    Text scoreText = new Text("Your Score: " + score);
    scoreText.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
    scoreText.setFill(Color.WHITE);
    scoreText.setX((WIDTH - scoreText.getLayoutBounds().getWidth()) / 2);
    scoreText.setY(250);

    // Create and set the losing screen scene
    Scene winningsence = new Scene(winningPane, WIDTH, HEIGHT);
    primaryStage.setScene(winningsence);
    winningPane.getChildren().addAll(wingameText, scoreText, ExitButton(),
                                     TryButton(), MenuButton());

    playWinningSound();
  }

  /** Method to play the losing sound. */
  private void playLosingSound() {
    try {
      // Load the MP3 file as a Media object
      String soundPath =
              getClass().getResource("/sounds/gameover.mp3").toString();
      Media sound = new Media(soundPath);

      // Create a MediaPlayer object for the sound
      MediaPlayer mediaPlayer = new MediaPlayer(sound);

      // Play the sound
      mediaPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Method to creat the winning sound. */
  private void playWinningSound() {
    try {
      // Load the MP3 file as a Media object
      String soundPath =
          getClass().getResource("/sounds/wingame.mp3").toString();
      Media sound = new Media(soundPath);

      // Create a MediaPlayer object for the sound
      MediaPlayer mediaPlayer = new MediaPlayer(sound);

      // Play the sound
      mediaPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /** Plays a sound for the power-up. */
  private void playPowerupSound() {
    try {
      String path = getClass().getResource("/sounds/powerup.mp3").toString();
      Media sound = new Media(path);
      MediaPlayer mediaPlayer = new MediaPlayer(sound);
      mediaPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initializes the event handlers for the game scene.
   *
   * @param scene The game scene to add the event handlers to
   */
  private void initEventHandlers(Scene scene) {
    scene.setOnKeyPressed(event -> {
      // Check if AI mode is enabled
      if (aiMode)
        return;
      switch (event.getCode()) {
      case A:
      case LEFT:
        player.setMoveLeft(true);
        break;
      case D:
      case RIGHT:
        player.setMoveRight(true);
        break;
      case S:
      case DOWN:
        player.setMoveBackward(true);
        break;
      case W:
      case UP:
        player.setMoveForward(true);
        break;
      case SPACE:
        player.shoot(newObjects);
        break;
      }
    });

    scene.setOnKeyReleased(event -> {
      if (aiMode)
        return;
      switch (event.getCode()) {
      case A:
      case LEFT:
        player.setMoveLeft(false);
        break;
      case D:
      case RIGHT:
        player.setMoveRight(false);
        break;
      case S:
      case DOWN:
        player.setMoveBackward(false);
        break;
      case W:
      case UP:
        player.setMoveForward(false);
        break;
      }
    });
  }

  /**
   * Creates the main menu and the music backgroung for the game.
   *
   * @return The main menu pane
   */

  private Pane createMenu() {
    Pane menuPane = new Pane();

    // Create the menu background
    menuPane.setStyle(
        "-fx-background-image: url('/menubackground.png'); "
        +
        "-fx-background-size: cover; -fx-background-position: center bottom;");
    // Styled title
    Text welcomeText = new Text("Welcome to\nSpace Shooter!");
    welcomeText.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD,
                                  36));   // Bold and larger font
    welcomeText.setFill(Color.LIGHTCYAN); // Softer text color
    welcomeText.setEffect(
        new DropShadow(10, Color.CYAN)); // Add a shadow effect
    welcomeText.setTextAlignment(TextAlignment.CENTER);
    welcomeText.setX(WIDTH / 2 - 150);
    welcomeText.setY(100);

    // Styled buttons
    Button startButton = createStyledButton("START", 200);
    startButton.setOnAction(event -> startGame());

    Button aiButton = createStyledButton("AI", 250);
    aiButton.setOnAction(event -> {
      aiMode = true;
      startAIGame();
    });

    Button instructionsButton = createStyledButton("INSTRUCTIONS", 300);
    instructionsButton.setOnAction(event -> showInstructions());

    Button quitButton = createStyledButton("QUIT", 400);
    quitButton.setOnAction(event -> System.exit(0));

    // Button layout container
    VBox buttonsContainer = new VBox(20);
    buttonsContainer.setLayoutX(WIDTH / 2 - 75); // Center the buttons
    buttonsContainer.setLayoutY(200);
    buttonsContainer.getChildren().addAll(startButton, aiButton,
                                          instructionsButton, quitButton);

    menuPane.getChildren().addAll(welcomeText, buttonsContainer);

    // Music background
    URL resource = getClass().getResource("/background.mp3");
    if (resource != null) {
      Media menuMusic = new Media(resource.toString());
      menuMusicPlayer = new MediaPlayer(menuMusic);
      menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      menuMusicPlayer.setVolume(0.5);
      menuMusicPlayer.play();
    }

    return menuPane;
  }

  /**
   * Creates a styled button with a gradient background and hover effects.
   *
   * @param text The text to display on the button
   * @param y The y-coordinate of the button
   * @return The styled button
   */
  private Button createStyledButton(String text, double y) {
    Button button = new Button(text);
    button.setStyle(
        "-fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);"
        + "-fx-text-fill: white;"
        + "-fx-font-size: 18;"
        + "-fx-font-weight: bold;"
        + "-fx-padding: 10 20;"
        + "-fx-border-radius: 20;"
        + "-fx-background-radius: 20;"
        + "-fx-border-color: #ffffff;"
        + "-fx-border-width: 2;"
        + "-fx-font-family: 'Verdana';");
    button.setOnMouseEntered(event -> {
      button.setStyle(
          "-fx-background-color: linear-gradient(to right, #2575fc, #6a11cb);"
          + "-fx-text-fill: yellow;"
          + "-fx-font-size: 18;"
          + "-fx-font-weight: bold;"
          + "-fx-padding: 10 20;"
          + "-fx-border-radius: 20;"
          + "-fx-background-radius: 20;"
          + "-fx-border-color: yellow;"
          + "-fx-border-width: 2;"
          + "-fx-font-family: 'Verdana';");
      button.setEffect(new Glow(0.5));
    });
    button.setOnMouseExited(event -> {
      button.setStyle(
          "-fx-background-color: linear-gradient(to right, #6a11cb, #2575fc);"
          + "-fx-text-fill: white;"
          + "-fx-font-size: 18;"
          + "-fx-font-weight: bold;"
          + "-fx-padding: 10 20;"
          + "-fx-border-radius: 20;"
          + "-fx-background-radius: 20;"
          + "-fx-border-color: #ffffff;"
          + "-fx-border-width: 2;"
          + "-fx-font-family: 'Verdana';");
      button.setEffect(null);
    });
    return button;
  }

  /** Shows the instructions for the game. */
  private void showInstructions() {
    Alert instructionsAlert = new Alert(AlertType.INFORMATION);
    instructionsAlert.setTitle("Instructions");
    instructionsAlert.setHeaderText("Space Shooter Instructions");
    instructionsAlert.setContentText(
        "Use the A, W, S, and D keys or the arrow keys to move your "
        + "spaceship.\n"
        + "Press SPACE to shoot bullets and destroy the enemies.\n"
        + "If an enemy reaches the bottom of the screen, you lose a life.\n"
        + "The game resets if you lose all lives.\n"
        + "Collect power-ups to increase your score.\n"
        + "Defeat the boss enemy to level up and increase the difficulty.\n"
        + "Good luck and have fun!");
    instructionsAlert.showAndWait();
  }

  /**
   * Shows a temporary message on the screen for a specified duration.
   *
   * @param message The message to display
   * @param x The x-coordinate of the message
   * @param y The y-coordinate of the message
   * @param duration The duration to display the message
   */
  private void showTempMessage(String message, double x, double y,
                               double duration) {
    Text tempMessage = new Text(message);
    tempMessage.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
    tempMessage.setFill(Color.RED);
    tempMessage.setX(x);
    tempMessage.setY(y);
    root.getChildren().add(tempMessage);

    PauseTransition pause = new PauseTransition(Duration.seconds(duration));
    pause.setOnFinished(event -> root.getChildren().remove(tempMessage));
    pause.play();
  }
}
