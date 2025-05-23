package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;

import static utilz.Constants.Directions.*;
import static utilz.Constants.GRAVITY;
import main.Game;

public abstract class Enemy extends Entity {
    protected int  enemyState, enemyType;
    protected int  aniSpeed = 4;
    protected boolean firstUpdate = true;
    protected boolean inAir;
    protected float fallSpeed;
    protected float walkSpeed = 0.3f* Game.SCALE;
    protected int walkDir = LEFT;
    protected int tileY;
    protected float attackDistance = Game.TILES_SIZE * 1f; // Slightly increased attack range
    protected int maxHealth;
    protected int currentHealth;
    protected boolean active = true;
    protected boolean attackChecked;
    protected static final int STATE_PATROLLING = 0;
    protected static final int STATE_CHASING = 1;
    protected static final int STATE_ATTACKING = 2;
    
    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        initHitbox(x, y, width, height);
        
        maxHealth = GetMaxHealth(enemyType);
        currentHealth = maxHealth;
    }
    
    // Added methods from reference
    protected void firstUpdateCheck(int[][] lvlData) {
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
        firstUpdate = false;
        tileY = (int) (hitbox.y / Game.TILES_SIZE);  // Initialize tileY
    }
    
    protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
        if (attackBox.intersects(player.hitbox))
            player.changeHealth(-GetEnemyDmg(enemyType));
        attackChecked = true;
    }
    
    protected void updateInAir(int[][] lvlData) {
        if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed, hitbox.width, hitbox.height, lvlData)) {
            hitbox.y += fallSpeed;
            fallSpeed += GRAVITY;
        } else {
            inAir = false;
            hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, fallSpeed);
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
        }
    }
    
    protected void move(int[][] lvlData) {
        float xSpeed = 0;

        if (walkDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (IsFloor(hitbox, xSpeed, lvlData)) {
                hitbox.x += xSpeed;
                return;
            }

        changeWalkDir();
    }

    // Player interaction methods
    protected void turnTowardsPlayer(Player player) {
        if (player.hitbox.x > hitbox.x)
            walkDir = RIGHT;
        else
            walkDir = LEFT;
    }
    
    protected boolean canSeePlayer(int[][] lvlData, Player player) {
        int playerTileY = (int) (player.getHitbox().y / Game.TILES_SIZE);
        
        // Allow bigger vertical tolerance (4 tiles up or down)
        if (Math.abs(playerTileY - tileY) <= 4) {
            if (isPlayerInRange(player)) {
                // Get the proper Y-tile to check sight based on player and enemy positions
                int sightCheckTile = Math.min(tileY, playerTileY);
                
                // Make sure the path is clear
                if (IsSightClear(lvlData, hitbox, player.hitbox, sightCheckTile)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected boolean isPlayerInRange(Player player) {
        int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absValue <= attackDistance * 40; // Greatly increased sight range
    }

    protected boolean isPlayerCloseForAttack(Player player) {
        int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absValue <= attackDistance;
    }

    protected void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(enemyType, enemyState)) {
                aniIndex = 0;
                
                switch(enemyState) {
	                case ATTACK , HIT -> enemyState = IDLE;
	                case DEAD -> active = false;
                }
                
                
            }
        }
    }

    protected void changeWalkDir() {
        if (walkDir == LEFT)
            walkDir = RIGHT;
        else
            walkDir = LEFT;
    }
    
    protected void newState(int enemyState) {
        this.enemyState = enemyState;
        aniTick = 0;
        aniIndex = 0;
    }
    
    public void hurt(int amount) {
        currentHealth -= amount;
        if(currentHealth <= 0) {
            newState(DEAD);
        } else {
            newState(HIT);
        }
    }
    
    public boolean isActive() {
    	return active;
    }

    public int getAniIndex() {
        return aniIndex;
    }

    public int getEnemyState() {
        return enemyState;
    }
    
    public int getEnemyType() {
    	return enemyType;
    }
}