package utilz;

import main.Game;

public class Constants {
	
	public static final float GRAVITY = 0.03f * Game.SCALE;
	
	public static class PlayerConstants{
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int JUMP_1 = 2;
		public static final int JUMP_2 = 3;
		public static final int DASH = 4;
		public static final int FALL = 5;
		public static final int GROUND = 6;
		public static final int ATTACK_1= 7;
		public static final int ATTACK_2= 8;
		public static final int ATTACK_3= 9;
		public static final int DEATH= 10;
		public static final int HIT= 11;
		
		
		public static int GetSpriteAmount(int playerAction) {
			
			switch(playerAction) {
				case IDLE : return 30;
				case FALL :  
				case RUNNING : return 25;
				case GROUND :
				case HIT:
				case JUMP_1 : 
				case JUMP_2 : return 20; 
				case ATTACK_1:
				case DASH : return 13;
				case DEATH: 
				case ATTACK_2: return 15;
				case ATTACK_3: return 11;
				default : return 1;
			}
			
		}
	}
	
	public static class UI{
		
		public static class VolumeButtons{
			public static final int VOLUME_DEFAULT_WIDTH = 28;
			public static final int VOLUME_DEFAULT_HEIGHT = 44;
			public static final int SLIDER_DEFAULT_WIDTH = 215;
			
			public static final int VOLUME_WIDTH = (int) (VOLUME_DEFAULT_WIDTH * Game.SCALE);
			public static final int VOLUME_HEIGHT = (int) (VOLUME_DEFAULT_HEIGHT * Game.SCALE);
			public static final int SLIDER_WIDTH = (int) (SLIDER_DEFAULT_WIDTH * Game.SCALE);

		}
		
		public static class PausedButtons{
			public static final int SOUND_SIZE_DEFAULT = 42;
			public static final int SOUND_SIZE = (int) (SOUND_SIZE_DEFAULT * Game.SCALE);
			
		}
		
		public static class URMButtons{
			public static final int URM_SIZE_DEFAULT = 56;
			public static final int URM_SIZE = (int) (URM_SIZE_DEFAULT * Game.SCALE);
		}
		
		public static class Buttons{
			public static final int B_WIDTH_DEFAULT = 276; 
			public static final int B_HEIGHT_DEFAULT = 55;
			public static final int B_WIDTH = (int)(B_WIDTH_DEFAULT * Game.SCALE);
			public static final int B_HEIGHT = (int)(B_HEIGHT_DEFAULT * Game.SCALE);
			
		}
	}
	
	public static class Directions{
		public static final int LEFT = 0;
		public static final int UP = 1;
		public static final int RIGHT = 2;
		public static final int DOWN = 3;
	}
	
	public static class EnemyConstants{
		// this values will be used as the color of the green 
		public static final int SIGBIN = 16; 
		public static final int TIKBALANG = 32;
		public static final int DUWENDE = 48;
		
		// states for enemies
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int ATTACK = 2;
		public static final int HIT = 3;
		public static final int DEAD = 4;
		
		//new states for Tikbalang 
		
		public static final int SPECIAL_ATTACK = 3;
		public static final int TIKBALANG_HIT = 4;		
		public static final int TIKBALANG_DEAD = 5;

		//new states for DUWENDE
		public static final int DUWENDE_DISAPPEAR = 5;
		
		//size and pos for SIGBIN
		public static final int WIDTH_DEFAULT = 256;
		public static final int HEIGHT_DEFAULT = 256;
		
		public static final int SIGBIN_WIDTH = (int) (WIDTH_DEFAULT * Game.SCALE);
		public static final int SIGBIN_HEIGHT = (int) (HEIGHT_DEFAULT * Game.SCALE);
		
		public static final int SIGBIN_DRAWOFFSET_X = (int) (90 * Game.SCALE);
		public static final int SIGBIN_DRAWOFFSET_Y = (int) (68 * Game.SCALE);  // sprite pos relative to hitbox + to move down - to move up
		
		//size and pos for TIKBALANG
		public static final int TIKBALANG_WIDTH = (int) ((WIDTH_DEFAULT * 2) * Game.SCALE);
		public static final int TIKBALANG_HEIGHT = (int) ((HEIGHT_DEFAULT * 2) * Game.SCALE);
		
		public static final int TIKBALANG_DRAWOFFSET_X = (int) (180 * Game.SCALE);
		public static final int TIKBALANG_DRAWOFFSET_Y = (int) (114 * Game.SCALE);  // sprite pos relative to hitbox + to move down - to move up
		
		//size and pos for DUWENDE - smaller than Sigbin
	    public static final int DUWENDE_WIDTH = (int) ((WIDTH_DEFAULT) * Game.SCALE);
	    public static final int DUWENDE_HEIGHT = (int) ((HEIGHT_DEFAULT) * Game.SCALE);
	    
	    public static final int DUWENDE_DRAWOFFSET_X = (int) (120 * Game.SCALE);
	    public static final int DUWENDE_DRAWOFFSET_Y = (int) (82 * Game.SCALE);
	    
	    
		public static int GetSpriteAmount(int enemy_type, int enemy_state) {
			
			switch(enemy_type) {
			case SIGBIN:
				switch(enemy_state) {
					case RUNNING : return 20;
					case ATTACK: return 24;
					case HIT: return 19;
					case IDLE : 
					case DEAD: return 30;
				}
			case TIKBALANG:
	            switch(enemy_state) {
	                case IDLE: 
	                case TIKBALANG_HIT: 
	                case TIKBALANG_DEAD: 
	                case RUNNING: return 30;
	                case ATTACK: return 32;
	                case SPECIAL_ATTACK: return 29;
	            }
			case DUWENDE:
				switch(enemy_state) {
				case IDLE : return 32;
				case ATTACK: return 21;
					case RUNNING : return 20;
					case HIT: return 4;
					case DEAD:
					case DUWENDE_DISAPPEAR: return 3;
				}
			}
			return 0;
		}
		
		public static int GetMaxHealth(int enemy_type) {
				switch(enemy_type) {
					case SIGBIN : return 50;
					case TIKBALANG: return 300;
					case DUWENDE: return 20;
					default : return 1;
				}
		}
		
		public static int GetEnemyDmg(int enemy_type) {
			switch(enemy_type) {
				case SIGBIN: return 10;
				case TIKBALANG: return 25; // Boss deals more damage
				case DUWENDE: return 5;
				default: return 1;
			}
	}
	
	
		
	}
}
