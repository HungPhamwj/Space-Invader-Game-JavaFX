package com.example.spaceshootergamejavafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Explosion extends GameObject {
  private static final int WIDTH = 60;
  private static final int HEIGHT = 60;
  private static final int MAX_FRAMES = 20;
  private int frame = 0;

  public Explosion(double x, double y) { super(x, y, WIDTH, HEIGHT); }

  @Override
  public void update() {
    frame++;
  }

  @Override
  public void render(GraphicsContext gc) {
    double progress = (double)frame / MAX_FRAMES;
    double radius = WIDTH / 2 + progress * 30;
    double alpha = 1.0 - progress;

    // Vòng ngoài màu vàng nhạt
    gc.setGlobalAlpha(alpha * 0.5);
    gc.setFill(Color.YELLOW);
    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

    // Vòng giữa màu cam
    gc.setGlobalAlpha(alpha * 0.7);
    gc.setFill(Color.ORANGE);
    gc.fillOval(x - radius * 0.7, y - radius * 0.7, radius * 1.4, radius * 1.4);

    // Vòng trong màu đỏ
    gc.setGlobalAlpha(alpha);
    gc.setFill(Color.RED);
    gc.fillOval(x - radius * 0.4, y - radius * 0.4, radius * 0.8, radius * 0.8);

    gc.setGlobalAlpha(1.0); // Reset alpha
  }

  @Override
  public boolean isDead() {
    return frame > MAX_FRAMES;
  }

  @Override
  public double getWidth() {
    return WIDTH;
  }

  @Override
  public double getHeight() {
    return HEIGHT;
  }
}
