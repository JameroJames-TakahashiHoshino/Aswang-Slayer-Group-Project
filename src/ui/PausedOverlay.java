package ui;

import static utilz.Constants.UI.PausedButtons.SOUND_SIZE;
import static utilz.Constants.UI.URMButtons.*;
import static utilz.Constants.UI.VolumeButtons.*;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import audio.AudioManager;
import gameStates.Gamestate;
import gameStates.Playing;
import main.Game;
import utilz.LoadSave;

public class PausedOverlay {
	
	private Playing playing;
	private BufferedImage backgroundImg;
	private int bgX, bgY, bgW, bgH;
	private AudioOptions audioOptions;
	private UrmButton menuButton, replayButton, unPauseButton;
	
	
	public PausedOverlay(Playing playing) {
		this.playing = playing;
		loadBackground();
		audioOptions = playing.getGame().getAudioOptions();
		createUrmButton();
	}
	
	

	private void createUrmButton() {  // positions of urmButtons
		int menuX = (int)(313 * Game.SCALE);
		int replayX = (int)(387 * Game.SCALE);
		int unPauseX = (int)(462 * Game.SCALE);
		int urmY = (int)(288 * Game.SCALE);
		
		unPauseButton = new UrmButton(unPauseX, urmY, URM_SIZE, URM_SIZE, 0);
		replayButton = new UrmButton(replayX, urmY, URM_SIZE, URM_SIZE,1);
		menuButton= new UrmButton(menuX, urmY, URM_SIZE, URM_SIZE, 2);
	}

	

	private void loadBackground() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PAUSED_BACKGROUND);
		bgW = (int)(backgroundImg.getWidth() * Game.SCALE);
		bgH = (int)(backgroundImg.getHeight() * Game.SCALE);
		bgX = 0;
		bgY = 0;
	}

	public void update() {
		menuButton.update();
		replayButton.update();
		unPauseButton.update();
		
		audioOptions.update();
	}
	
	public void draw(Graphics g) {
		// BACKGROUNMD
		g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null);
		
		menuButton.draw(g);
		replayButton.draw(g);
		unPauseButton.draw(g);
		
		audioOptions.draw(g);
	}
	
	public void mouseDragged(MouseEvent e) {
		audioOptions.mouseDragged(e);
	}
	public void mousedPressed(MouseEvent e) {
	   if (isIn(e, menuButton)) {
	        menuButton.setMousePressed(true);
	    } else if (isIn(e, replayButton)) {
	        replayButton.setMousePressed(true);
	    } else if (isIn(e, unPauseButton)) {
	        unPauseButton.setMousePressed(true);
	    } else {
	    	audioOptions.mousedPressed(e);
	    }
	    
	}

	public void mouseReleased(MouseEvent e) {
	   if (isIn(e, menuButton)) {
	        if (menuButton.isMousePressed()) {
	            Gamestate.state = Gamestate.MENU;
	            playing.unpauseGame();
	            AudioManager.playMusic("res/audio/menu_bg.wav");
	        }
	    } else if (isIn(e, replayButton)) {
	        if (replayButton.isMousePressed()) {
	        	playing.resetAll();
	        	playing.unpauseGame();
	        }
	    } else if (isIn(e, unPauseButton)) {
	        if (unPauseButton.isMousePressed()) {
	            playing.unpauseGame();
	        }
	    } else {
	    	audioOptions.mouseReleased(e);
	    }
	    // Reset all button states
	    menuButton.resetBools();
	    replayButton.resetBools();
	    unPauseButton.resetBools();
	}

	public void mouseMoved(MouseEvent e) {
		menuButton.setMouseOver(false);
		replayButton.setMouseOver(false);
		unPauseButton.setMouseOver(false);
		
		
		if (isIn(e, menuButton))
			menuButton.setMouseOver(true); 
		else if (isIn(e, replayButton))
			replayButton.setMouseOver(true); 
		else if (isIn(e, unPauseButton))
			unPauseButton.setMouseOver(true); 
		else 
			audioOptions.mouseMoved(e);
		
	}
	
	private boolean isIn(MouseEvent e, PausedButton b) {
	    boolean result = b.getBounds().contains(e.getX(), e.getY());
	    return result;
	}



	
	
}
