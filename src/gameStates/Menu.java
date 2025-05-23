package gameStates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import audio.AudioManager;
import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

public class Menu extends State implements StateMethods {
	
	private MenuButton[] buttons = new MenuButton[3];
	private BufferedImage backgroundImg;
	private int menuX, menuY, menuWidth, menuHeight;
	private int buttonOffset = (int) (252 * Game.SCALE); 
	public Menu(Game game) {
		super(game);
		loadButtons();
		loadBackground();
		
		AudioManager.playMusic("res/audio/menu_bg.wav");
	}

	private void loadBackground() { 
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
		menuWidth = (int)(backgroundImg.getWidth() * Game.SCALE);
		menuHeight = (int)(backgroundImg.getHeight() * Game.SCALE);
		menuX = 0; 
		menuY = 0; 
	}

	private void loadButtons() {	// for x axis					// change this y axis    // the row in sprite
		buttons[0] = new MenuButton(Game.GAME_WIDTH - buttonOffset, (int) (130 * Game.SCALE), 0, Gamestate.PLAYING);
		buttons[1] = new MenuButton(Game.GAME_WIDTH - buttonOffset, (int) (195  * Game.SCALE), 1, Gamestate.OPTIONS);
		buttons[2] = new MenuButton(Game.GAME_WIDTH - buttonOffset, (int) (260  * Game.SCALE), 2, Gamestate.QUIT);
	}

	@Override
	public void update() {
		for(MenuButton mb : buttons) {
			mb.update();
		}
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, menuX, menuY,  menuWidth, menuHeight, null);
		
		for(MenuButton mb : buttons) {
			mb.draw(g);;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for(MenuButton mb : buttons) {
			if(isIn(e,mb)) {
				mb.setMousePressed(true);
				break;
			}
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    for(MenuButton mb : buttons) {
	        if(isIn(e,mb)) {
	            if(mb.isMousePressed())
	                // Use the game's changeGameState method instead of directly modifying state
	                game.changeGameState(mb.getState());
	            break;
	        }
	    }        
	    resetButtons();
	}

	private void resetButtons() {
		for(MenuButton mb : buttons) 
			mb.resetBools();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    for (MenuButton mb : buttons) 
	        mb.setMouseOver(false); // Reset all buttons to not hovered
	    
	    for (MenuButton mb : buttons) {
	        if (isIn(e, mb)) {
	            mb.setMouseOver(true);
	            break; // Break here to stop after finding the first button hovered
	        }
	    }
	}

	@Override
	public void keyPressed(KeyEvent e) {
	
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			Gamestate.state = Gamestate.PLAYING;
			System.out.println("PLAYING");
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
}
