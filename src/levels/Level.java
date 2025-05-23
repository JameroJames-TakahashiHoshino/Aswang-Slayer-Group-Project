package levels;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utilz.HelpMethods.GetLevelData;
import static utilz.HelpMethods.GetSigbin;
import static utilz.HelpMethods.GetTikbalang;
import static utilz.HelpMethods.GetDuwende;

import entities.Duwende;
import entities.Sigbin;
import entities.Tikbalang;
import main.Game;

public class Level {
	
	private BufferedImage img;
	private ArrayList<Sigbin> sigbins;
	private ArrayList<Tikbalang> tikbalangs; // Add this line
	private ArrayList<Duwende> duwendes;
	private int [][] lvlData;
	
	private int lvlTilesWide;
	private int maxTilesOffset;
	private int maxLvlOffsetX;

	
	public Level(BufferedImage img) {
		this.img = img;
		createLevelData();
		createEnemies();
		calculateLevelOffsets();
	}
	
	private void calculateLevelOffsets() {
	    lvlTilesWide = img.getWidth();
	    maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
	    maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;  
	}

	private void createEnemies() {
		sigbins = GetSigbin(img);
		tikbalangs = GetTikbalang(img); // Add this line
		duwendes = GetDuwende(img);
	}

	private void createLevelData() {
		lvlData = GetLevelData(img);
	}

	public int getSpriteIndex(int x, int y) {
		return lvlData[y][x]; 
	}
	
	public int [][] getLvlData(){
		return lvlData;
	}
	
	public int getLvlOffset() {
		return maxLvlOffsetX;
	}
	
	public ArrayList<Sigbin> getSigbins(){
		return sigbins;
	}
	
	// Add this method
	public ArrayList<Tikbalang> getTikbalangs(){
		return tikbalangs;
	}
	
	public ArrayList<Duwende> getDuwendes(){
		return duwendes;
	}
}