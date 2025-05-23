package utilz;


import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utilz.Constants.EnemyConstants.TIKBALANG;
import static utilz.Constants.EnemyConstants.SIGBIN;
import static utilz.Constants.EnemyConstants.DUWENDE;

import entities.Duwende;
import entities.Enemy;
import entities.Sigbin;
import entities.Tikbalang;
import levels.LevelManager;
import main.Game;

public class HelpMethods {
	

	
    public static boolean CanMoveHere(float x, float y, float width, float height, int[][] lvlData) {
        // Check more points along the edges of the hitbox, not just the corners
        
        // Check all four corners first (current implementation)
        if (IsSolid(x, y, lvlData))
            return false;
        if (IsSolid(x + width, y + height, lvlData))
            return false;
        if (IsSolid(x + width, y, lvlData))
            return false;
        if (IsSolid(x, y + height, lvlData))
            return false;
        
        // Add additional checks along the edges
        
        // Check middle points of each edge
        // Top edge
        if (IsSolid(x + width/2, y, lvlData))
            return false;
        // Right edge
        if (IsSolid(x + width, y + height/2, lvlData))
            return false;
        // Bottom edge
        if (IsSolid(x + width/2, y + height, lvlData))
            return false;
        // Left edge
        if (IsSolid(x, y + height/2, lvlData))
            return false;
        
        // If we made it here, all checks passed
        return true;
    }

    // Making this public since it's needed for IsSightClear and IsAllTilesWalkable
    public static boolean IsSolid(float x, float y, int[][] lvlData) {
        // Check boundaries
        int maxWidth = lvlData[0].length * Game.TILES_SIZE;
        if (x < 0 || x >= maxWidth)
            return true;
        
        // Only check if y is beyond the bottom of the screen
        if (y >= Game.GAME_HEIGHT) 
            return true;
        
        // For y values above the screen, treat them as empty space
        if (y < 0)
            return false;
        
        // Convert to tile indices
        int xIndex = (int)(x / Game.TILES_SIZE);
        int yIndex = (int)(y / Game.TILES_SIZE);
        
        // Get value at this position
        int value = lvlData[yIndex][xIndex];
        
        // 0 = empty space, anything else is solid
        return value != 0;
    }

    // Added from reference - determines if a tile is solid
    public static boolean IsTileSolid(int xTile, int yTile, int[][] lvlData) {
        if (xTile < 0 || xTile >= lvlData[0].length || yTile < 0 || yTile >= lvlData.length)
            return true;
        
        int value = lvlData[yTile][xTile];
        // Following your existing pattern: 0 = empty space, anything else is solid
        return value != 0;
    }

    public static float GetEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
        // For right collision
        if (xSpeed > 0) {
            // Get the tile that the player is colliding with
            int nextTile = (int) ((hitbox.x + hitbox.width) / Game.TILES_SIZE) + 1;
            
            // Calculate the position where the right edge of player's hitbox 
            // touches the left edge of the wall tile
            return nextTile * Game.TILES_SIZE - hitbox.width - 0.1f; // Small offset to avoid rounding issues
        } 
        // For left collision
        else {
            // Get the left-most tile
            int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
            
            // Return position where the left edge of player's hitbox touches the right edge of the wall
            return currentTile * Game.TILES_SIZE + 0.1f; // Small offset to avoid rounding issues
        }
    }
    
    public static float GetEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
        if (airSpeed > 0) {
            // SIMPLE FIX: Always use the tile we're currently in + 4 for the bug of hitbox. Damn this one made me lose sleep for 2 days
           
            int currentTile = (int)(hitbox.y / Game.TILES_SIZE) + 4;
            
            // Calculate the correct position relative to this tile
            return currentTile * Game.TILES_SIZE - hitbox.height -1;
        } else {
            // Jumping - handling is fine
            int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
            return currentTile * Game.TILES_SIZE;
        }
    }
    
    public static boolean IsEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
        // Check the pixel below bottomleft and bottomright
        if (!IsSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
            if (!IsSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
                return false;

        return true;
    }
    
    /**
     * Checks if there is floor beneath the entity's feet in the direction it's moving.
     * For left movement: checks bottom-left corner
     * For right movement: checks bottom-right corner
     * This prevents enemies from walking off edges.
     */
    public static boolean IsFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] lvlData) {
        if (xSpeed < 0) {
            // Moving left: check bottom-left corner
            return IsSolid(hitbox.x + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
        } else {
            // Moving right: check bottom-right corner
            return IsSolid(hitbox.x + hitbox.width + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
        }
    }
    
   
    public static boolean IsAllTilesWalkable(int xStart, int xEnd, int y, int[][] lvlData) {
        for (int i = 0; i < xEnd - xStart; i++) {
            if (IsTileSolid(xStart + i, y, lvlData))
                return false;
            if (!IsTileSolid(xStart + i, y + 1, lvlData))
                return false;
        }
        return true;
    }

    // Added from reference - checks if sight line is clear between entities
    public static boolean IsSightClear(int[][] lvlData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int yTile) {
        int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
        int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return IsAllTilesWalkable(secondXTile, firstXTile, yTile, lvlData);
        else
            return IsAllTilesWalkable(firstXTile, secondXTile, yTile, lvlData);
    }
    
    public static int [][] GetLevelData(BufferedImage img){
		int [][] lvlData = new int[img.getHeight()][img.getWidth()];
		for(int j = 0; j< img.getHeight(); j++) {
			for(int i = 0; i<img.getWidth(); i++) {
				Color color = new Color(img.getRGB(i, j));
				int value = color.getRed()/16;
				if(value >= 16) {
					value = 0;
				}
				lvlData[j][i] = value;
			}
		}
		return lvlData;
	}
    
    public static ArrayList<Sigbin> GetSigbin(BufferedImage img) {
	    ArrayList<Sigbin> list = new ArrayList<>();
	    for (int j = 0; j < img.getHeight(); j++)
	        for (int i = 0; i < img.getWidth(); i++) {
	            Color color = new Color(img.getRGB(i, j));
	            int value = color.getGreen();
	            if (value == SIGBIN)
	                list.add(new Sigbin(i * Game.TILES_SIZE, j * Game.TILES_SIZE));
	        }
	    return list;
	}
    
    public static ArrayList<Tikbalang> GetTikbalang(BufferedImage img) {
        ArrayList<Tikbalang> list = new ArrayList<>();
        for (int j = 0; j < img.getHeight(); j++)
            for (int i = 0; i < img.getWidth(); i++) {
                Color color = new Color(img.getRGB(i, j));
                int value = color.getGreen();
                if (value == TIKBALANG)
                    list.add(new Tikbalang(i * Game.TILES_SIZE, j * Game.TILES_SIZE));
            }
        return list;
    }
    
    public static ArrayList<Duwende> GetDuwende(BufferedImage img) {
	    ArrayList<Duwende> list = new ArrayList<>();
	    for (int j = 0; j < img.getHeight(); j++)
	        for (int i = 0; i < img.getWidth(); i++) {
	            Color color = new Color(img.getRGB(i, j));
	            int value = color.getGreen();
	            if (value == DUWENDE)
	                list.add(new Duwende(i * Game.TILES_SIZE, j * Game.TILES_SIZE));
	        }
	    return list;
	}
    
    public static int scaleCalc(float scale, int enemyType ) {
    	int scaler = 0;
    	
    	switch(enemyType) {
    	case DUWENDE: 
    		if(scale%0.25 ==0 && scale!= 1.75 && scale != 1.50) {
    			scaler = 1;
    		}
    		return scaler;
    	case SIGBIN:
    		if(scale%0.25 ==0) {
    			scaler = 1;
    		}
    	}
    	return scaler;
	}

}