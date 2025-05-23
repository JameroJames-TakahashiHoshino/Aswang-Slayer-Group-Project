package gameStates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import entities.EnemyManager;
import entities.Player;
import levels.LevelManager;
import main.Game;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PausedOverlay;
import utilz.LoadSave;

public class Playing extends State implements StateMethods{
	
	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private PausedOverlay pausedOverlay;
	private boolean paused = false;	
	
	//screen movement variables
	private int xLvlOffset;
	private int leftBorder = (int)(0.5 * Game.GAME_WIDTH);
	private int rightBorder = (int)(0.6 * Game.GAME_WIDTH);
	private int maxLvlOffsetX;
	
	private boolean gameOver = false;
	private GameOverOverlay gameOverOverlay;
	
	private LevelCompletedOverlay levelCompletedOverlay;
	private Boolean lvlCompleted = false;
	
	private boolean playerDying = false;

	
	private BufferedImage mainBackground, parallax_1, parallax_2, parallax_3; // background images
	
	public Playing(Game game) {
		super(game);
		initClasses();
		pausedOverlay = new PausedOverlay(this);
		
		mainBackground = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_MAIN_BG);
		parallax_1 = LoadSave.GetSpriteAtlas(LoadSave.PARALLAX_1);
		parallax_2 = LoadSave.GetSpriteAtlas(LoadSave.PARALLAX_2);
		parallax_3 = LoadSave.GetSpriteAtlas(LoadSave.PARALLAX_3);
		
		calcLvlOffset();
		loadStartLevel();
	}
	
	public void loadNextLevel() {
		resetAll();
		levelManager.loadNextLevel();
	}
	
	private void loadStartLevel() {
		enemyManager.loadEnemies(levelManager.getCurrentLevel());
	}

	private void calcLvlOffset() {
		maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
	}

	private void initClasses() {
	    levelManager = new LevelManager(game);
	    enemyManager = new EnemyManager(this);
	    
	    player = new Player(0, 0, (int)(256 * Game.SCALE), (int)(256 * Game.SCALE), this);
	    player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
	    pausedOverlay = new PausedOverlay(this);
	    gameOverOverlay = new GameOverOverlay(this);
	    levelCompletedOverlay = new LevelCompletedOverlay(this);
	}
	

	@Override
	public void update() {
	    if (paused) {
	        pausedOverlay.update();
	    } else if (lvlCompleted) {
	        levelCompletedOverlay.update();
	    } else if (gameOver) {
	        gameOverOverlay.update();
	    } else if (playerDying) {
	        player.update();
	    } else {
	        levelManager.update();
	        player.update();
	        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player, levelManager);
	        
	        checkCloseToBorder();
	    }
	}


	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset;
		
		if(diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;
		
		if(xLvlOffset > maxLvlOffsetX)
			xLvlOffset = maxLvlOffsetX;
		else if (xLvlOffset < 0)
			xLvlOffset = 0;
	}

	// for debug tiles
	private void drawGrid(Graphics g, int lvlOffset) {
	    g.setColor(Color.WHITE);
	    for (int i = 0; i < Game.GAME_HEIGHT; i += Game.TILES_SIZE) {
	        g.drawLine(0, i, Game.GAME_WIDTH, i); // Horizontal lines
	    }
	    
	    // Get level width from current level data
	    int levelWidth = levelManager.getCurrentLevel().getLvlData()[0].length;
	    for (int i = 0; i < levelWidth * Game.TILES_SIZE; i += Game.TILES_SIZE) {
	        g.drawLine(i - lvlOffset, 0, i - lvlOffset, Game.GAME_HEIGHT); // Vertical lines
	    }
	}
	

	@Override
    public void draw(Graphics g) {
        // Draw background
        g.drawImage(mainBackground, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        // Draw parallax layers
        int levelWidth = levelManager.getCurrentLevel().getLvlData()[0].length * Game.TILES_SIZE;
        int parallax1Offset = (int) (xLvlOffset * 0.1);
        int parallax2Offset = (int) (xLvlOffset * 0.3);
        int parallax3Offset = (int) (xLvlOffset * 0.7);

        g.drawImage(parallax_1, -parallax1Offset, 0, levelWidth, Game.GAME_HEIGHT, null);
        g.drawImage(parallax_2, -parallax2Offset, 0, levelWidth, Game.GAME_HEIGHT, null);
        g.drawImage(parallax_3, -parallax3Offset, 0, levelWidth, Game.GAME_HEIGHT, null);

        // Draw game elements
        levelManager.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        enemyManager.draw(g, xLvlOffset);
        
        // Draw overlays
        if (paused) {
            pausedOverlay.draw(g);
        } else if (gameOver) {
            gameOverOverlay.draw(g);
        } else if (lvlCompleted) {
            levelCompletedOverlay.draw(g);
        }
        
        //tile debug
//        drawGrid(g, xLvlOffset);
    }
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			player.setAttacking(true);
		
	}

	@Override
    public void mousePressed(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mousePressed(e);
        } else if (!gameOver && !playerDying) {
            if (paused)
                pausedOverlay.mousedPressed(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mouseReleased(e);
        } else if (!gameOver && !playerDying) {
            if (paused)
                pausedOverlay.mouseReleased(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mouseReleased(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mouseMoved(e);
        } else if (!gameOver && !playerDying) {
            if (paused)
                pausedOverlay.mouseMoved(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mouseMoved(e);
        }
    }
	
	public void mouseDragged(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pausedOverlay.mouseDragged(e);
	}



	@Override
	public void keyPressed(KeyEvent e) {
	    if (gameOver) {
	        gameOverOverlay.keyPressed(e);
	    } else if (!paused) {
	        switch (e.getKeyCode()) {
	        case KeyEvent.VK_A:
	            player.setLeft(true);
	            break;
	        case KeyEvent.VK_D:
	            player.setRight(true);
	            break;
	        case KeyEvent.VK_SPACE:
	            player.setJump(true);
	            break;
	        case KeyEvent.VK_SHIFT:  // Use Shift key for dashing
	            player.setDash(true);
	            break;
	        case KeyEvent.VK_ESCAPE:
	            paused = !paused;    
	            break;
	        }
	    } else {
	        switch (e.getKeyCode()) {
	        case KeyEvent.VK_ESCAPE:
	            paused = !paused;
	            break;
	        }
	    }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_A:
			player.setLeft(false);
			break;
		case KeyEvent.VK_D:
			player.setRight(false);
			break;
		case KeyEvent.VK_SPACE:
			player.setJump(false);
			break;
		}
		
	}
	
	public void unpauseGame() {
		paused = false;
	}
	
	public void windowFocusLost() {
		player.resetDirBooleans();
	}

	public Player getPlayer() {
		return player;
	}
	
	public void setGameOver(boolean gameOver) {
	    this.gameOver = gameOver;
	}
	
	public void resetAll() {
        // Reset game state
        gameOver = false;
        paused = false;
        lvlCompleted = false;
        playerDying = false;
        
        // Reset player
        player.resetDirBooleans();
        player.resetPosition();
        player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        player.changeHealth(100); // Reset health to full
        
        // Reset enemies
        enemyManager.resetAllEnemies();
        
        // Reset level offset if needed
        xLvlOffset = 0;
    }

	public void checkEnemyHit(Rectangle2D.Float attackBox, int damage) {
	    enemyManager.checkEnemyHit(attackBox, damage);
	}
	
	public EnemyManager getEnemyManager() {
	    	return enemyManager;
	}
	
	public void setMaxLvlOffset(int lvlOffSet) {
		this.maxLvlOffsetX = lvlOffSet;
	}

	public void setLevelCompleted(boolean levelCompleted) {
		this.lvlCompleted = levelCompleted; 
	}
	
	public void setPlayerDying(boolean playerDying) {
        this.playerDying = playerDying;
    }
}


