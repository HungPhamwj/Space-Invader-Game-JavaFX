package com.example.spaceshootergamejavafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;


/** Represents a bullet in the game */
public class Bullet extends GameObject {

  /** Width of the bullet */
  public static final int WIDTH = 4;

  /** Height of the bullet */
  public static final int HEIGHT = 15;

  /** Speed of the bullet */
  public static final double SPEED = 10;

  /** Flag to indicate if the bullet is dead */
  private boolean dead = false;

  /**
   * Creates a new bullet at the specified position
   *
   * @param x The x-coordinate of the bullet
   * @param y The y-coordinate of the bullet
   */
  public Bullet(double x, double y) {
    super(x, y, WIDTH, HEIGHT);
    playSound("/sounds/shooting.mp3");
  }

  /** Updates the bullet's position and sounds */
  @Override
  public void update() {
    y -= SPEED;
  }

  /** Plays a sound for the bullet */
  private void playSound(String resourcePath) {
    try {
      String path = getClass().getResource(resourcePath).toExternalForm();
      Media sound = new Media(path);
      MediaPlayer mediaPlayer = new MediaPlayer(sound);
      mediaPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Renders the bullet on the screen
   *
   * @param gc The GraphicsContext to render the bullet
   */
  @Override
  public void render(GraphicsContext gc) {
    gc.setFill(Color.LIGHTGOLDENRODYELLOW);
    gc.fillRect(x - WIDTH / 2, y - HEIGHT / 2, WIDTH, HEIGHT);
  }

  /**
   * Returns the width of the bullet
   *
   * @return The width of the bullet
   */
  @Override
  public double getWidth() {
    return WIDTH;
  }

  /**
   * Returns the height of the bullet
   *
   * @return The height of the bullet
   */
  @Override
  public double getHeight() {
    return HEIGHT;
  }

  /**
   * Sets the dead flag for the bullet
   *
   * @param dead The dead flag to set
   */
  public void setDead(boolean dead) { this.dead = dead; }

  /**
   * Returns whether the bullet is dead
   *
   * @return True if the bullet is dead, false otherwise
   */
  @Override
  public boolean isDead() {
    return dead;
  }
}
