package main;

import entities.*;
import graficos.Spritesheet;
import graficos.UI;
import world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends Canvas implements Runnable, KeyListener, MouseListener {

    public static JFrame frame;
    private Thread thread;
    private boolean isRunning;
    public static final int WIDTH = 240;
    public static final int HEIGHT = 160;
    public static final int SCALE = 3;


    private int CUR_LEVEL = 1, MAX_LEVEL = 2;
    public BufferedImage image;

    public static List<Entity> entities;
    public static List<Enemy> enemies;
    public static List<BulletShoot> bulletShoots;

    public static Spritesheet spritesheet;

    public static World world;

    public static Player player;
    public static Random random;

    public UI ui;

    public static String gameState =  "MENU";
    private boolean showMessageGameOver = true;
    private int framesGameOver = 0;
    private boolean restartGame = false;

    public Menu menu;

    public Game() {
        random = new Random();
        addKeyListener(this);
        addMouseListener(this);
        //definir tamanho da tela
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        //iniciar tela
        initFrame();
        //inicializando objetos
        ui = new UI();
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        entities = new ArrayList<Entity>();
        enemies = new ArrayList<Enemy>();
        bulletShoots = new ArrayList<BulletShoot>();
        spritesheet = new Spritesheet("/spritesheet.png");
        player = new Player(0, 0, 16, 16,spritesheet.getSpritesheet(32, 0, 16, 16));
        entities.add(player);
        world = new World("/level1.png");
        menu = new Menu();
    }

    public void initFrame(){
        //definir propriedades de tela
        frame = new JFrame("Game_01");
        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    //inicializa????o do jogo
    public synchronized void start(){
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    public synchronized void stop(){
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //metodo principal
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }

    public void update(){
        if (gameState == "NORMAL") {
            Sound.music.loop();
            this.restartGame = false;
            for (int i = 0; i < entities.size(); i++) {
                Entity e = entities.get(i);
                e.update();
            }
            for (int i = 0; i < bulletShoots.size(); i++) {
                bulletShoots.get(i).update();
            }
            if (enemies.size() == 0) {
                //avan??ar para o pr??ximo level
                CUR_LEVEL++;
                if (CUR_LEVEL > MAX_LEVEL) {
                    CUR_LEVEL = 1;
                }
                String newWorld = "level" + CUR_LEVEL + ".png";
                System.out.println(newWorld);
                World.restartGame(newWorld);
            }
        } else if (gameState == "GAME_OVER"){
            this.framesGameOver++;
            if (this.framesGameOver == 40){
                this.framesGameOver = 0;
                if (this.showMessageGameOver){
                    this.showMessageGameOver = false;
                } else {
                    this.showMessageGameOver = true;
                }
            }
            if (restartGame){
                this.restartGame = false;
                this.gameState = "NORMAL";
                CUR_LEVEL = 1;
                String newWorld = "level" + CUR_LEVEL + ".png";
                //System.out.println(newWorld);
                World.restartGame(newWorld);
            }
        } else if (gameState == "MENU"){
            menu.update();
        }
    }

    public void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        Graphics g  = image.getGraphics();
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        /* renderiza????o do jogo*/
        //Graphics2D g2 = (Graphics2D) g;//cast
        world.render(g);
        for (int i = 0; i < entities.size(); i++){
            Entity e = entities.get(i);
            e.render(g);
        }
        for (int i = 0; i < bulletShoots.size(); i++){
            bulletShoots.get(i).render(g);
        }
        ui.render(g);
        /* * */
        g.dispose();
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 255, 255));
        g.drawString("Muni????o: " + player.ammo, 580, 20);
        if (gameState == "GAME_OVER"){
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0,0,0,100));
            g2.fillRect(0,0, WIDTH * SCALE, HEIGHT * SCALE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(new Color(255, 255, 255));
            g.drawString("GAME OVER!", (WIDTH * SCALE) / 2 - 140, (HEIGHT * SCALE) / 2 - 15);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            if (showMessageGameOver)
            g.drawString(">Pressione ENTER para reiniciar<", (WIDTH * SCALE) / 2 - 220, (HEIGHT * SCALE) / 2 + 30);
        } else if(gameState == "MENU"){
            menu.render(g);

        }
        bs.show();
    }

    //game looping avan??ado
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int frames = 0;
        double timer = System.currentTimeMillis();
        requestFocus();
        while (isRunning){
            long now = System.nanoTime();
            delta+= (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1){
                update();
                render();
                frames++;
                delta--;
            }
            if(System.currentTimeMillis() - timer >= 1000){
                System.out.println("FPS: " + frames);
                frames = 0;
                timer += 1000;
            }
        }
        stop();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
            player.left = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
            player.right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
            player.up = true;
            if (gameState == "MENU"){
                menu.up = true;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
            player.down = true;
            if (gameState == "MENU"){
                menu.down = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_L || e.getKeyCode() == KeyEvent.VK_Z){
            player.shoot = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            this.restartGame = true;
            if (gameState == "MENU"){
                menu.enter = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            gameState = "MENU";
            menu.pause = true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
            player.left = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
            player.right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
            player.up = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
            player.down = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent args0) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        player.mouseShoot = true;
        player.mx = (e.getX() / 3);
        player.my = (e.getY() / 3);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent args0) {

    }

    @Override
    public void mouseExited(MouseEvent args0) {

    }
}

