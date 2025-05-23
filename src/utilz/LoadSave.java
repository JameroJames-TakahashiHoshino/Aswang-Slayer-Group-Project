package utilz;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import entities.Sigbin;
import main.Game;
import static utilz.Constants.EnemyConstants.*;

public class LoadSave {
	// player sprite
	public static final String PLAYER_ATLAS = "player_atlas.png";
	
	// enemy sprites
	public static final String SIGBIN_ATLAS = "sigbin_atlas.png";
	public static final String TIKBALANG_ATLAS = "tikbalang_atlas.png";
	public static final String DUWENDE_ATLAS = "duwende_atlas.png";
	
	
	// level sprites
	public static final String LEVEL_ATLAS = "outside_sprites.png";
	
	// level sprites bgs
	public static final String LEVEL_MAIN_BG= "parallax_main_bg.png";
	public static final String PARALLAX_1= "parallax_trees_1_bg.png";
	public static final String PARALLAX_2= "parallax_trees_2_bg.png";
	public static final String PARALLAX_3= "parallax_trees_3_bg.png";
	
	// UI sprites
	public static final String MENU_BUTTONS = "button_atlas.png";
	public static final String MENU_BACKGROUND = "menu_background.png";
	public static final String OPTIONS_BACKGROUND = "options_background.png";
	public static final String PAUSED_BACKGROUND = "pause_menu.png";
	public static final String COMPLETED_IMG = "lvl_completed_background.png";
	public static final String DEATH_SCREEN = "death_background.png";
	public static final String SOUND_BUTTONS = "sound_button.png";
	public static final String URM_BUTTONS = "urm_buttons.png";
	public static final String VOLUME_BUTTONS = "volume_buttons.png";
	public static final String STATUS_BAR = "status_bar.png";
	
	//INSTRUCTIONS overlay
	public static final String INSTRUCTION_LEFT = "press_a.png";
	public static final String INSTRUCTION_RIGHT = "press_d.png";
	public static final String INSTRUCTION_JUMP = "press_space.png";
	public static final String INSTRUCTION_DASH = "press_shift.png";
	public static final String INSTRUCTION_ATTACK = "click.png";
	public static final String INSTRUCTION_DEFEAT = "defeat_instructions.png";


	public static BufferedImage GetSpriteAtlas(String fileName) {
		BufferedImage img = null;
		InputStream is = LoadSave.class.getResourceAsStream("/" + fileName);
		try {
			img = ImageIO.read(is);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return img;
	}

	
	
	
	public static BufferedImage [] GetAllLevels() {
		URL url = LoadSave.class.getResource("/lvls");
		File file = null;
		
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		File[] files = file.listFiles();
		File[] filesSorted = new File[files.length];
		
		
		//sorting
		for(int i = 0; i< filesSorted.length; i++) {
			for(int j = 0; j< files.length; j++) {
				if(files[j].getName().equals((i+1) + ".png"))
					filesSorted[i] = files[j];
			}	
		}
		
		BufferedImage [] imgs = new BufferedImage [filesSorted.length];
		
		for(int i = 0; i< files.length; i++) {
			try {
				imgs[i] = ImageIO.read(filesSorted[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	

		
		return imgs;
	}
	
	
}
