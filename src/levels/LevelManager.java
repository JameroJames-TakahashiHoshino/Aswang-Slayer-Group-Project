package levels;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gameStates.Gamestate;
import main.Game;
import utilz.LoadSave;

public class LevelManager {

	private Game game;
	private BufferedImage[] levelSprite;
	private ArrayList<Level>levels;
	
	private int lvlIndex = 0;
	BufferedImage ins_left, ins_right, ins_jump, ins_dash, ins_attack, ins_defeat;
	public LevelManager(Game game) {
		this.game = game;
		importOutsideSprites();
		levels = new ArrayList<>();
		buildAllLevels();
		
	}
	
	public void loadNextLevel(){
		lvlIndex++;
		
		if(lvlIndex	>= levels.size()) {
			lvlIndex = 0;
			System.out.println("YOU COMPLETED THE GAME! TITLE ACHIEVED: ASWANG SLAYER. THE GAME IS NOW  COMPLETED");
			Gamestate.state = Gamestate.MENU; 
		}
		
		Level newLevel = levels.get(lvlIndex);
		game.getPlaying().getEnemyManager().loadEnemies(newLevel);
		game.getPlaying().getPlayer().loadLvlData(newLevel.getLvlData());
		game.getPlaying().setMaxLvlOffset(newLevel.getLvlOffset());
    }
	
	private void buildAllLevels() {
		BufferedImage [] allLevels = LoadSave.GetAllLevels();
		for(BufferedImage img: allLevels)
			levels.add(new Level(img));
	}
	
	//load of tilesprites
	private void importOutsideSprites() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
		levelSprite = new BufferedImage[48];
		for (int j = 0; j < 4; j++)
			for (int i = 0; i < 4; i++) {
				int index = j * 4 + i;
				levelSprite[index] = img.getSubimage(i * 32, j * 32, 32, 32);
			}
	}

	public void draw(Graphics g, int lvlOffset) {
	    // Draw the current level's tiles
	    for (int j = 0; j < Game.TILES_IN_HEIGHT; j++) {
	        for (int i = 0; i < levels.get(lvlIndex).getLvlData()[0].length; i++) {
	            int index = levels.get(lvlIndex).getSpriteIndex(i, j);
	            g.drawImage(levelSprite[index], Game.TILES_SIZE * i - lvlOffset, Game.TILES_SIZE * j, Game.TILES_SIZE, Game.TILES_SIZE, null);
	        }
	    }

	    // Only draw instructions for the first level
	    if (lvlIndex == 0) {
	        initDrawInstructionsOverlay();
	        drawInstructionsOverlay(g, lvlOffset); // Pass the camera offset
	    }
	}

	public void update() {

	}
	
	public void initDrawInstructionsOverlay() {
		ins_left = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_LEFT);
		ins_right = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_RIGHT);
		ins_jump = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_JUMP);
		ins_dash = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_DASH);
		ins_attack = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_ATTACK);
		ins_defeat = LoadSave.GetSpriteAtlas(LoadSave.INSTRUCTION_DEFEAT);
	}
	
	public void drawInstructionsOverlay(Graphics g, int lvlOffset) {
	    // Adjust positions with lvlOffset for camera movement
	    int y = (int)(150 * Game.SCALE); // Static Y position
	    
	    int left_x = (int) (20 * Game.SCALE) - lvlOffset; // Adjusted X with lvlOffset
	    int right_x = (int) (400 * Game.SCALE) - lvlOffset;
	    int jumpz_x = (int) (1000 * Game.SCALE) - lvlOffset;
	    int attackz_x = (int) (1500 * Game.SCALE) - lvlOffset;
	    int dash_x = (int) (3200 * Game.SCALE) - lvlOffset;
	    int defeat_x = (int) (3600 * Game.SCALE) - lvlOffset;
	    
	    int width = (int) (242 * Game.SCALE); // Width of the images
	    int height = (int) (18 * Game.SCALE); // Height of the images

	    // Draw the instruction images at their adjusted positions
	    g.drawImage(ins_left, left_x, y, width, height, null);
	    g.drawImage(ins_right, right_x , y , width, height, null);
	    g.drawImage(ins_jump, jumpz_x , y , width, height, null);
	    g.drawImage(ins_dash, dash_x , y , width, height, null);
	    g.drawImage(ins_attack, attackz_x, (int)( y * 1.5), (int)(350 * Game.SCALE), (int)(30 * Game.SCALE), null);
	    g.drawImage(ins_defeat, defeat_x, (int) (y * 1.5), (int)(350 * Game.SCALE), (int)(30 * Game.SCALE), null);
	}
	
	
	public Level getCurrentLevel() {
		return levels.get(lvlIndex);
	}
	
	public int getLvl() {
		return lvlIndex;
	}
	
	public int getAmountOfLevels() {
		return levels.size();
	}
}