package main;

import java.awt.Graphics;

import audio.AudioManager;
import audio.AudioPlayer;
import gameStates.GameOptions;
import gameStates.Gamestate;
import gameStates.Menu;
import gameStates.Playing;
import ui.AudioOptions;


public class Game implements Runnable{
	
	private GameWindow gameWindow;
	private GamePanel gamePanel;
	
	private Thread gameThread;
	private final int FPS_SET = 120;
	private final int UPS_SET = 200;
	
	private Playing playing;
	private Menu menu;
	private GameOptions gameOptions;
	private AudioOptions audioOptions;
	private AudioPlayer audioPlayer;
	
	public final static int TILES_DEFAULT_SIZE = 32;
	public final static float SCALE = 2.25f;
	
	// game screen | player screen
	public final static int TILES_IN_WIDTH = 26;
	public final static int TILES_IN_HEIGHT = 14;
	
	
	public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT= TILES_SIZE * TILES_IN_HEIGHT;
	
	
	public Game() {
        initClasses();
        
        gamePanel = new GamePanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();
        
        // Play menu music on game start (since we typically start in the menu state)
        if (Gamestate.state == Gamestate.MENU) {
            AudioManager.playMusic("res/audio/menu_bg.wav");
        }
        
        startGameLoop();
    }

	private void initClasses() {
	    // Initialize AudioManager with default values
	    AudioManager.setMusicVolume(0.5f); // 50% volume as default
	    AudioManager.setSoundVolume(0.5f);
	    
	    audioOptions = new AudioOptions();
	    menu = new Menu(this);
	    playing = new Playing(this);
	    gameOptions = new GameOptions(this);
	}

	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void render(Graphics g) { // drawing of each states
		
		switch(Gamestate.state) {
		case MENU:
			menu.draw(g);
			break;
		case PLAYING:
			playing.draw(g);
			break;
		case OPTIONS:
			gameOptions.draw(g);
			break;
		default:
			break;
		}
	}
	
	private void update() { //Hello
		
		switch(Gamestate.state) {
		case MENU:
			menu.update();
			break;
		case PLAYING:
			playing.update();
			break;
		case OPTIONS:
			gameOptions.update();
			break;
		case QUIT:
		default:
			System.exit(0);
			break;
		}
		
	}
	
	// this is where the game loop is []
	@Override
	public void run() {
		
		int frames = 0;
		long lastCheck = 0;	
		//UPS
		double timePerUpdate = 1000000000/ UPS_SET;
		long previousTime = System.nanoTime();
		int updates = 0;
		double deltaU = 0;
		double deltaF = 0;
		
		double timePerFrame = 1000000000/ FPS_SET;
		
		while(true) {
			long currentTime = System.nanoTime();
			
			deltaU+=(currentTime - previousTime) / timePerUpdate;
			deltaF+=(currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;
			if(deltaU >= 1) {
				update();
				updates++;
				deltaU--;
			}
			
			if(deltaF >= 1) {
				gamePanel.repaint();
				frames++;
				deltaF--;
			}
			
			if(System.currentTimeMillis() - lastCheck >= 1000) {
				lastCheck = System.currentTimeMillis();
				System.out.println("FPS: " + frames + " UPS " + updates);
				frames= 0;
				updates = 0;
			}
		}
	}

	public void windowFocusLost() {
		if(Gamestate.state == Gamestate.PLAYING) {
			playing.getPlayer().resetDirBooleans();
		}
	}
	
	public static void changeGameState(Gamestate newState) { // method for music in states
		 // Stop any existing music BEFORE changing state
	    if (Gamestate.state == Gamestate.PLAYING && newState != Gamestate.PLAYING) {
	        // We're leaving the playing state - stop the game/level music
	        AudioManager.stopMusic();
	    }
	    
	    // Change the state
	    Gamestate.state = newState;
	    
	    // Then play appropriate music based on the new state
	    switch(newState) {
	        case MENU:
	            AudioManager.playMusic("res/audio/menu_bg.wav");
	            break;
	        case PLAYING:
	            AudioManager.playMusic("res/audio/normal_lvl_music.wav"); 
	            break;
	        case OPTIONS:
	            AudioManager.playMusic("res/audio/menu_bg.wav"); 
	            break;
	        case QUIT:
	            // Stop all music when quitting
	            AudioManager.stopMusic();
	            break;
	        default:
	            break;
	    }
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	public Playing getPlaying() {
		return playing;
	}
	
	public GameOptions getGameOptions() {
		return gameOptions;
	}

	public AudioOptions getAudioOptions() {
		return audioOptions;
	}
	
	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
	
	
	
}