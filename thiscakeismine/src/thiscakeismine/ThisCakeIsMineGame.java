/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package thiscakeismine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;   
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ThisCakeIsMineGame extends JPanel implements MouseMotionListener, MouseListener {
    private static final int PANEL_WIDTH = 1600;
    private static final int PANEL_HEIGHT = 900;
    private static final int ENEMY_SPAWN_RATE = 2;
    private static final int CAKE_INITIAL_HEALTH = 100;
    
    private BufferedImage backgroundMenuImage;
    private Image backgroundImage;
    private Player player;
    private Cake cake;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private Point mousePosition;
    private int countdownTime1;
    private int countdownTime2;
    private Timer gameTimer1;
    private Timer gameTimer2;
    private int cakeHealth;
    private Image menuImage;
    private BufferedImage tryAgainButtonImage;
    private BufferedImage exitButtonImage;
    private int tryAgainButtonX;
    private int tryAgainButtonY;
    private int exitButtonX;
    private int exitButtonY;
    
    private BufferedImage nextLevelButtonImage;
    private int nextLevelButtonX;
    private int nextLevelButtonY;
    private GameState gameState;
    
    private Cursor customCursor;
    
    private enum GameState {
        MENU,
        GAME1,
        GAME_SELECTION,
        GAME2,
        GAME_OVER,
    }

    public ThisCakeIsMineGame() {           
        loadBackgroundImages();
        initializeComponents();
        addMouseListeners();
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        gameState = GameState.MENU;
        addMouseListener(this);
    }
    
    private void loadBackgroundImages() {
        try {
            backgroundImage = ImageIO.read(getClass().getResource("BackgroundFix.png"));
            backgroundMenuImage = ImageIO.read(getClass().getResource("BackgroundMenu.png"));
            tryAgainButtonImage = ImageIO.read(getClass().getResource("TryAgainButtonImage.png"));
            exitButtonImage = ImageIO.read(getClass().getResource("ExitButtonImage.png"));
            menuImage = ImageIO.read(getClass().getResource("MENU.png"));
            nextLevelButtonImage = ImageIO.read(getClass().getResource("NextButton1.png"));
            BufferedImage cursorImage = ImageIO.read(getClass().getResource("cursor_image.png"));
            customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "CustomCursor");
            
            tryAgainButtonX = (PANEL_WIDTH - tryAgainButtonImage.getWidth()) / 2;
            tryAgainButtonY = 375;
            
            nextLevelButtonX = (PANEL_WIDTH - tryAgainButtonImage.getWidth()) / 2;
            nextLevelButtonY = 375;
            
            exitButtonX = (PANEL_WIDTH - exitButtonImage.getWidth()) / 2;
            exitButtonY = 450;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        player = new Player();
        cake = new Cake();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
    }
    
    public void startGame1() {
        gameState = GameState.GAME1;
        countdownTime1 = 30; // Reset the countdown time
        stopTimer2(); // Stop the timer for GAME2
        startTimer1(); // Start the countdown timer for GAME1
        
        initializeComponents();  // Reset game components
        cakeHealth = CAKE_INITIAL_HEALTH;  // Reset cake health
    }
    
    public void startGame2() {
        gameState = GameState.GAME2;
        countdownTime2 = 60; // Reset the countdown time
        stopTimer1(); // Stop the timer for GAME1
        startTimer2(); // Start the countdown timer for GAME2
        
        initializeComponents();  // Reset game components
        cakeHealth = 50;  // Reset cake health
    }

    private void startTimer1() {
        gameTimer1 = new Timer(1000, (ActionEvent e) -> {
            countdownTime1--;
            if (countdownTime1 <= 0) {
                
                gameState = GameState.GAME_SELECTION;  // Change to GAME_SELECTION
                countdownTime1 = 0; // Reset the countdown time
                stopTimer1(); // Stop the game loop
                
            }
            repaint();
        });
        gameTimer1.start();
    }
    
    private void startTimer2() {
        gameTimer2 = new Timer(1000, (ActionEvent e) -> {
            countdownTime2--;
            if (countdownTime2 <= 0) {
                
                gameState = GameState.MENU;
                countdownTime2 = 0; // Reset the countdown time
                stopTimer2(); // Stop the game loop
                
            }
            repaint();
        });
        gameTimer2.start();
    }
    
    
    private void stopTimer1() {
        if (gameTimer1 != null) {
            gameTimer1.stop();
        }
    }

    private void stopTimer2() {
        if (gameTimer2 != null) {
            gameTimer2.stop();
        }
}
    public void gameOver() {
        gameState = GameState.GAME_OVER;
        stopTimer1(); // Stop the timer for GAME1
        stopTimer2(); // Stop the timer for GAME2
        loadBackgroundImages(); // Load the "BackgroundGameOver.png" image
        repaint();
    }

    private void updateGame() {
        if (gameState == GameState.GAME1) {
            if (cakeHealth <= 0) {
                gameState = GameState.GAME_OVER;
                stopTimer1(); 
                loadBackgroundImages(); // Load the "BackgroundGameOver.png" image
            } else {
                // Existing game update logic
                player.handleShooting(bullets, mousePosition);

                Iterator<Enemy> enemyIterator = enemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy enemy = enemyIterator.next();
                    enemy.move();

                    if (enemy.hasTouchedCake(cake)) {
                        enemyIterator.remove();
                    }
                }

                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    bullet.move();
                    boolean shouldRemoveBullet = false;

                    Iterator<Enemy> enemyIterator2 = enemies.iterator();
                    while (enemyIterator2.hasNext()) {
                        Enemy enemy = enemyIterator2.next();
                        if (enemy.hasCollidedWithBullet(bullet)) {
                            enemyIterator2.remove();
                            shouldRemoveBullet = true;
                        }
                    }

                    if (shouldRemoveBullet) {
                        bulletIterator.remove();
                    }
                }

                if (new Random().nextInt(100) < ENEMY_SPAWN_RATE) {
                    spawnEnemy(2);
                }

                removeOutOfBoundsBullets();
            }
        } else if (gameState == GameState.GAME2) {
            if (cakeHealth <= 0) {
                gameState = GameState.GAME_OVER;
                stopTimer2(); 
                loadBackgroundImages(); // Load the "BackgroundGameOver.png" image
            } else {
                // Existing game update logic
                player.handleShooting(bullets, mousePosition);

                Iterator<Enemy> enemyIterator = enemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy enemy = enemyIterator.next();
                    enemy.move();

                    if (enemy.hasTouchedCake(cake)) {
                        enemyIterator.remove();
                    }
                }

                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    bullet.move();
                    boolean shouldRemoveBullet = false;

                    Iterator<Enemy> enemyIterator2 = enemies.iterator();
                    while (enemyIterator2.hasNext()) {
                        Enemy enemy = enemyIterator2.next();
                        if (enemy.hasCollidedWithBullet(bullet)) {
                            enemyIterator2.remove();
                            shouldRemoveBullet = true;
                        }
                    }

                    if (shouldRemoveBullet) {
                        bulletIterator.remove();
                    }
                }

                if (new Random().nextInt(100) < ENEMY_SPAWN_RATE) {
                    spawnEnemy(3);
                }

                removeOutOfBoundsBullets();
            }
        }
    }
    
    public int getCakeHealth() {
        return cakeHealth;
    }

    public void setCakeHealth(int cakeHealth) {
        this.cakeHealth = cakeHealth;
    }

    private void spawnEnemy(int speed) {
        int spawnY = Math.min(800, Math.max(100, new Random().nextInt(PANEL_HEIGHT - 50)));
        enemies.add(new Enemy(PANEL_WIDTH - 50, spawnY, cake.getX(), cake.getY(), speed));
    }

    private void removeOutOfBoundsBullets() {
        bullets.removeIf(bullet -> bullet.isOutOfBounds(PANEL_WIDTH, PANEL_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (null != gameState) // Draw the semi-transparent background image (BackgroundMenu.png) first
        switch (gameState) {
            case MENU:{
                g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                player.draw(g);
                cake.draw(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.drawImage(backgroundMenuImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset opacity
                g.drawImage(menuImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                    break;
                }
            case GAME1:
                // Draw the "BackgroundFix.png" as the game background
                g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                // Continue drawing other game elements in the foreground
                player.draw(g);
                cake.draw(g);
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }   for (Bullet bullet : bullets) {
                    bullet.draw(g);
                }   drawCountdownTimer(g);
                drawCakeHealth1(g);
                break;
            case GAME2:
                // Draw the "BackgroundFix.png" as the game background
                g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                // Continue drawing other game elements in the foreground
                player.draw(g);
                cake.draw(g);
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }   for (Bullet bullet : bullets) {
                    bullet.draw(g);
                }   drawCountdownTimer(g);
                drawCakeHealth2(g);
                break;
            case GAME_SELECTION:{
                g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                player.draw(g);
                cake.draw(g);
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }       for (Bullet bullet : bullets) {
                    bullet.draw(g);
                }       drawCountdownTimer(g);
                drawCakeHealth1(g);
                // Now, draw the "BackgroundGameOver.png" with 60% opacity over the existing game elements
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.drawImage(backgroundMenuImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset opacity
                g.drawImage(nextLevelButtonImage, tryAgainButtonX, tryAgainButtonY, this);
                g.drawImage(exitButtonImage, exitButtonX, exitButtonY, this);
                    break;
                }
            case GAME_OVER:{
                g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                player.draw(g);
                cake.draw(g);
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }       for (Bullet bullet : bullets) {
                    bullet.draw(g);
                }       drawCountdownTimer(g);
                drawCakeHealth1(g);
                // Now, draw the "BackgroundGameOver.png" with 60% opacity over the existing game elements
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.drawImage(backgroundMenuImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset opacity      
                g.drawImage(tryAgainButtonImage, tryAgainButtonX, tryAgainButtonY, this);
                g.drawImage(exitButtonImage, exitButtonX, exitButtonY, this);
                    break;
                }
            default:
                break;
        }
    }

    private void drawCountdownTimer(Graphics g) {
        if (gameState == GameState.GAME1) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Pixeboy", Font.PLAIN, 48));

            int minutes = countdownTime1 / 60;
            int seconds = countdownTime1 % 60;

            String timerText = String.format("%02d:%02d", minutes, seconds);

            int x = 750;
            int y = 50;

            g.drawString(timerText, x, y);
        } else if (gameState == GameState.GAME2) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Pixeboy", Font.PLAIN, 48));

            int minutes = countdownTime2 / 60;
            int seconds = countdownTime2 % 60;

            String timerText = String.format("%02d:%02d", minutes, seconds);

            int x = 750;
            int y = 50;

            g.drawString(timerText, x, y);
        }
    }
    
    private void drawCakeHealth1(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Pixeboy", Font.PLAIN, 48));

        // Load the cake icon image
        ImageIcon icon = new ImageIcon(getClass().getResource("Cake_Icon.png"));
        Image iconImage = icon.getImage();
             
        // The icon
        int iconX = 30;
        int iconY = 40 - iconImage.getHeight(null) / 2;

        // Define icon
        g.drawImage(iconImage, iconX, iconY, null);

        // Text
        String healthText = "Cake HP: " + cakeHealth + "/100";
        int x = iconX + iconImage.getWidth(null) + 10;
        int y = 50;

        g.drawString(healthText, x, y);
    }
    
    private void drawCakeHealth2(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Pixeboy", Font.PLAIN, 48));

        // Load the cake icon image
        ImageIcon icon = new ImageIcon(getClass().getResource("Cake_Icon.png"));
        Image iconImage = icon.getImage();
             
        // The icon
        int iconX = 30;
        int iconY = 40 - iconImage.getHeight(null) / 2;

        // Define icon
        g.drawImage(iconImage, iconX, iconY, null);

        // Text
        String healthText = "Cake HP: " + cakeHealth + "/50";
        int x = iconX + iconImage.getWidth(null) + 10;
        int y = 50;

        g.drawString(healthText, x, y);
    }
    
    private void addMouseListeners() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
        
        // Set the custom cursor
        if (gameState == GameState.GAME1 || gameState == GameState.GAME2) {
            setCursor(customCursor);
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameState == GameState.MENU) {
            startGame1();
            requestFocusInWindow(); // Request focus to handle key events
            startGameLoop(); // Start the game loop
        } else if (gameState == GameState.GAME_OVER || gameState == GameState.GAME_SELECTION) {

            // Check if the Try Again button is clicked
            if (e.getX() >= tryAgainButtonX && e.getX() <= tryAgainButtonX + tryAgainButtonImage.getWidth()
                && e.getY() >= tryAgainButtonY && e.getY() <= tryAgainButtonY + tryAgainButtonImage.getHeight()) {
                // Restart the game (you should add the logic for this)
                restartGame(); // You need to implement this method
            }
            
            if (e.getX() >= nextLevelButtonX && e.getX() <= nextLevelButtonX + nextLevelButtonImage.getWidth()
                && e.getY() >= nextLevelButtonY && e.getY() <= nextLevelButtonY + nextLevelButtonImage.getHeight()) {
                startGame2(); // You need to implement this method
                requestFocusInWindow(); // Request focus to handle key events
                startGameLoop(); // Start the game loop
            }

            // Check if the Exit button is clicked
            if (e.getX() >= exitButtonX && e.getX() <= exitButtonX + exitButtonImage.getWidth()
                && e.getY() >= exitButtonY && e.getY() <= exitButtonY + exitButtonImage.getHeight()) {
                // Exit the game (you should add the logic for this)
                System.exit(0); // Exit the game
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            player.setShooting(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            player.setShooting(false);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not needed
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not needed
    }

   public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("This Cake Is MINE");
            ThisCakeIsMineGame game = new ThisCakeIsMineGame();
            frame.add(game);
            frame.getContentPane().setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
        });
    }
    
    private void startGameLoop() {
        Timer timer = new Timer(0, (ActionEvent e) -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - player.getLastFrameTime() > player.getFrameDelay()) {
                player.setLastFrameTime(currentTime);
                updateGame();
                repaint();
            }
        });
        timer.setRepeats(true);
        timer.setDelay(0);
        timer.start();
    }
    
    private void restartGame() {
        // Restart the game when needed
        gameState = GameState.GAME1;
        countdownTime1 = 60;
        startTimer1();
        cakeHealth = CAKE_INITIAL_HEALTH;
        initializeComponents();
        startGameLoop();
    }
    
    public class Bullet {
        private double x;
        private double y;
        private double velocityX;
        private double velocityY;
        private static final double SPEED = 10;

        public Bullet(double x, double y, double velocityX, double velocityY) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        public void move() {
            x += velocityX;
            y += velocityY;
        }

        public void draw(Graphics g) {
            double bulletAngle = Math.atan2(velocityY, velocityX);
            AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
            transform.rotate(bulletAngle);
            ((Graphics2D) g).setTransform(transform);
            g.drawImage(player.getBulletImage(), -player.getBulletImage().getWidth() / 2, -player.getBulletImage().getHeight() / 2, null);
            ((Graphics2D) g).setTransform(new AffineTransform());
        }

        public boolean isOutOfBounds(int panelWidth, int panelHeight) {
            return x > panelWidth || y > panelHeight;
        }
    }

    public abstract class Entity {
        protected int x;
        protected int y;
        protected BufferedImage image;

        public void draw(Graphics g) {
            g.drawImage(image, x, y, null);
        }
    }

    public class Player extends Entity {
        private boolean isShooting;
        private long lastFireTime;
        private long frameDelay = 1000 / 120; // 120 FPS
        private BufferedImage bulletImage;
        private long lastFrameTime; // Add this field
        private long bulletDelay = 500;

        public Player() {
            loadImage();
            x = 300;
            y = 400;
            isShooting = false;
            lastFireTime = 0;
        }

        public void setShooting(boolean shooting) {
            isShooting = shooting;
        }

        public void handleShooting(ArrayList<Bullet> bullets, Point mousePosition) {
            if (isShooting) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFireTime >= bulletDelay) {
                    shoot(bullets, mousePosition);
                    lastFireTime = currentTime; // Update the lastFireTime
                }
            }
        }

        public void shoot(ArrayList<Bullet> bullets, Point mousePosition) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFireTime > frameDelay) {
                lastFireTime = currentTime;

                double dx = mousePosition.getX() - (x + image.getWidth() / 2);
                double dy = mousePosition.getY() - (y + image.getHeight() / 2);
                double angle = Math.atan2(dy, dx);

                double velocityX = Math.cos(angle) * Bullet.SPEED;
                double velocityY = Math.sin(angle) * Bullet.SPEED;

                bullets.add(new Bullet(x + image.getWidth() / 2, y + image.getHeight() / 2, velocityX, velocityY));
            }
        }

        private void loadImage() {
            try {
                BufferedImage originalPlayerImage = ImageIO.read(getClass().getResource("Player_One.png"));
                int scaledWidth = originalPlayerImage.getWidth() * 2;
                int scaledHeight = originalPlayerImage.getHeight() * 2;
                image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dPlayer = image.createGraphics();
                g2dPlayer.drawImage(originalPlayerImage, 0, 0, scaledWidth, scaledHeight, null);
                g2dPlayer.dispose();

                bulletImage = ImageIO.read(getClass().getResource("bulletplayer.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public long getLastFrameTime() {
            return lastFrameTime; // Return the new field
        }
        
        public void setLastFrameTime(long lastFrameTime) {
            this.lastFrameTime = lastFrameTime; // Set the new field
        }
        
        public long getFrameDelay() {
            return frameDelay;
        }

        public BufferedImage getBulletImage() {
            return bulletImage;
        }
    }

    public class Cake extends Entity {
        public Cake() {
            loadImage();
            x = 250;
            y = 410;
        }

        private void loadImage() {
            try {
                BufferedImage originalCakeImage = ImageIO.read(getClass().getResource("Cake_Object.png"));
                int scaledCakeWidth = originalCakeImage.getWidth() * 3;
                int scaledCakeHeight = originalCakeImage.getHeight() * 3;
                image = new BufferedImage(scaledCakeWidth, scaledCakeHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dCake = image.createGraphics();
                g2dCake.drawImage(originalCakeImage, 0, 0, scaledCakeWidth, scaledCakeHeight, null);
                g2dCake.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
    }

    public class Enemy extends Entity {
        private int targetX;
        private int targetY;
        private int speed;
        private int yDirection;
        private int directionChangeInterval;
        private long lastDirectionChangeTime = System.currentTimeMillis();
        
        // Define the y-range limits
        private static final int Y_MIN = 150;
        private static final int Y_MAX = 650;

        public Enemy(int x, int y, int targetX, int targetY, int speed) {
            loadImage();
            this.x = x;
            this.y = Math.min(Y_MAX, Math.max(Y_MIN, y));
            this.targetX = targetX;
            this.targetY = targetY;
            this.speed = speed;
            yDirection = new Random().nextInt(3) - 1; // Initial vertical direction: -1 (up), 0 (straight), or 1 (down)
            directionChangeInterval = new Random().nextInt(3000) + 1000; // Random interval between 1 and 4 seconds
        }

        public void move() {
            double distanceToCake = Math.hypot(targetX - x, targetY - y);

            if (distanceToCake < 1) {
                enemies.remove(this);
                return;
            }

            if (distanceToCake < 600) {
                targetX = cake.getX();
                double angle = Math.atan2(cake.getY() - y, cake.getX() - x);
                x += speed * Math.cos(angle);
                y += speed * Math.sin(angle);
            } else {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDirectionChangeTime > directionChangeInterval) {
                    lastDirectionChangeTime = currentTime;
                    yDirection = new Random().nextInt(3) - 1;
                    directionChangeInterval = new Random().nextInt(3000) + 1000;
                }

                y = Math.max(Y_MIN, Math.min(Y_MAX, y));

                double angle = Math.atan2(targetY - y, targetX - x);
                x += speed * Math.cos(angle);
                y += speed * yDirection;
            }

        }

        public boolean hasTouchedCake(Cake cake) {
            double distanceToCake = Math.hypot(cake.getX() - x, cake.getY() - y);
            
            if (distanceToCake < 1) {
                cakeHealth -= 10; // Reduce cake's HP by 10
                return true;
            }
            return false;
        }

        public boolean hasCollidedWithBullet(Bullet bullet) {
            double distanceToBullet = Math.hypot(bullet.x - x, bullet.y - y);
            return distanceToBullet < 20; // Adjust collision radius as needed
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        private void loadImage() {
            try {
                BufferedImage originalEnemyImage = ImageIO.read(getClass().getResource("ModelEnemy1.png"));
                int scaledEnemyWidth = originalEnemyImage.getWidth() * 2;
                int scaledEnemyHeight = originalEnemyImage.getHeight() * 2;
                image = new BufferedImage(scaledEnemyWidth, scaledEnemyHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dEnemy = image.createGraphics();
                g2dEnemy.drawImage(originalEnemyImage, 0, 0, scaledEnemyWidth, scaledEnemyHeight, null);
                g2dEnemy.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}