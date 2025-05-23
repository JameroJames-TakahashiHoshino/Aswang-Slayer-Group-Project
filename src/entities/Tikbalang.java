package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.GRAVITY;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import audio.AudioManager;
import levels.LevelManager;
import main.Game;

public class Tikbalang extends Enemy {
    
    // State constants for boss behavior
    private static final int STATE_SPECIAL_ATTACK = 3;
    
    // Track boss behavior
    private int bossBehaviorState = STATE_PATROLLING;
    private float chaseSpeed = 0.8f * Game.SCALE; // Boss is faster than regular enemies
    
    // Special attack variables
    private boolean isDoingSpecialAttack = false;
    private float jumpHeight = -4.0f * Game.SCALE; // Higher jump for special attack
    private boolean jumpingForSpecialAttack = false;
    private float specialAttackDistance = Game.TILES_SIZE * 5f; // Longer range for special attack
    private boolean specialAttackChecked = false;
    private float specialAttackTargetX;
    private float horizontalJumpSpeed;
    
    // Timers
    private long lastDirectionChangeTime = 0;
    private long minDirectionChangeDelay = 500; // milliseconds
    private int specialAttackCooldown = 4000; // 6 seconds cooldown
    private long lastSpecialAttackTime = 0;
    private boolean specialAttackAnimComplete = false;
    
    // Attack boxes
    private Rectangle2D.Float attackBox;
    private Rectangle2D.Float specialAttackBox;
    
    //music
    private boolean playerSpotted = false;
    
    public Tikbalang(float x, float y) {
        // Use TIKBALNG constant from EnemyConstants
        super(x, y - (121* Game.SCALE) - 1, TIKBALANG_WIDTH, TIKBALANG_HEIGHT, TIKBALANG);
        
        // Make hitbox smaller than sprite for better gameplay
        float hitboxWidth = TIKBALANG_WIDTH * 0.32f;
        float hitboxHeight = TIKBALANG_HEIGHT * 0.30f;
        float hitboxX = x + (TIKBALANG_WIDTH/2) - (hitboxWidth/2);
        
        int scaler = (Game.SCALE == 2) ? 2 : 1;
        initHitbox(hitboxX, y - (121* Game.SCALE) - scaler , (int)hitboxWidth, (int)hitboxHeight);
        
        // Initialize attack boxes
        initAttackBoxes();
        
        // Boss has more health than regular enemies
        maxHealth = 200;
        currentHealth = maxHealth;
        
        // Boss moves faster
        walkSpeed = 0.5f * Game.SCALE;
    }
    
    private void initAttackBoxes() {
        // Normal attack box - make it smaller and more focused
        attackBox = new Rectangle2D.Float(x, y, (int)(80 * Game.SCALE), (int)(40 * Game.SCALE));
        
        // Special attack box is larger and positioned below the boss
        specialAttackBox = new Rectangle2D.Float(x, y, (int)(200 * Game.SCALE), (int)(50 * Game.SCALE));
    }
    
    public void update(int[][] lvlData, Player player, LevelManager levelManager) {
        // First update behavior based on environment
        updateBehavior(lvlData, player);
        
        updateStrengthBasedOnLevel(levelManager, player);
        
        // Then handle animation ticks
        updateAnimationTick();
        
        // Update attack box positions
        updateAttackBoxes();
    }
    

    private void updateStrengthBasedOnLevel(LevelManager levelManager, Player player) {
        int level = levelManager.getLvl();
        if (level == 3) {
        	chaseSpeed = 1.2f;
            jumpHeight = -5.0f * Game.SCALE;
            if (currentHealth <=40) 
            	specialAttackCooldown = 1100;
            else if(currentHealth <= 100)
            	chaseSpeed = 10;
        }
    }
    
    private void resetStrength() {
    	chaseSpeed = 0.8f;
    	jumpHeight = -4.0f * Game.SCALE;
    	specialAttackCooldown =  6000;
    }
    
    private void updateAttackBoxes() {
        // Make attack box smaller and positioned inside the hitbox
        float attackBoxWidth = hitbox.width * 0.8f; // 70% of hitbox width
        float attackBoxHeight = hitbox.height * 0.6f; // 60% of hitbox height
        
        // Position attack box inside the hitbox
        if (walkDir == RIGHT) {
            // Place on right side of hitbox (but still inside)
            attackBox.x = hitbox.x + (hitbox.width * 0.3f); // Start at 30% from left
        } else {
            // Place on left side of hitbox (but still inside)
            attackBox.x = hitbox.x;
        }
        
        // Set width and height
        attackBox.width = attackBoxWidth;
        attackBox.height = attackBoxHeight;
        
        // Center it vertically within the hitbox
        attackBox.y = hitbox.y + (hitbox.height - attackBox.height) / 2;
        
        // Update special attack box position (centered under boss)
        specialAttackBox.x = hitbox.x - (specialAttackBox.width - hitbox.width) / 2;
        specialAttackBox.y = hitbox.y + hitbox.height;
    }
    
    @Override
    protected boolean canSeePlayer(int[][] lvlData, Player player) {
        // Use the parent method to check if player is visible
        boolean canSee = super.canSeePlayer(lvlData, player);
        
        // If the boss sees the player for the first time, change the music
        if (canSee && !playerSpotted) {
            playerSpotted = true;
            // Stop the current background music and play boss music
            AudioManager.stopMusic();
            AudioManager.playMusic("res/audio/boss_bg_music.wav");
        }
        
        return canSee;
    }
    
    @Override
    protected boolean isPlayerCloseForAttack(Player player) {
        // Get player and Tikbalang positions
        float playerLeft = player.getHitbox().x;
        float playerRight = playerLeft + player.getHitbox().width;
        float bossLeft = hitbox.x;
        float bossRight = bossLeft + hitbox.width;

        // Check if player is within attack range horizontally
        boolean inHorizontalRange = (playerRight >= bossLeft && playerLeft <= bossRight);

        // Check if player is vertically aligned within attack range
        float playerY = player.getHitbox().y;
        float playerHeight = player.getHitbox().height;
        float playerBottom = playerY + playerHeight;
        float bossTop = hitbox.y;
        float bossBottom = bossTop + hitbox.height;

        boolean inVerticalRange = (playerBottom >= bossTop && playerY <= bossBottom);

        // Return true only if player is both horizontally and vertically within attack range
        return inHorizontalRange && inVerticalRange;
    }
    
    private void updateBehavior(int[][] lvlData, Player player) {
        if (firstUpdate) {
            firstUpdateCheck(lvlData);
            return;
        }
        
        if (inAir) {
            // Handle special logic for diving attack when in air
            if (jumpingForSpecialAttack) {
                handleSpecialAttackJump(lvlData, player);
            } else {
                updateInAir(lvlData);
            }
            return;
        }
        
        // State machine for boss behavior
        switch (enemyState) {
            case IDLE:
                newState(RUNNING);
                break;
            case RUNNING:
                // Check if we can see the player 
                if (canSeePlayer(lvlData, player))
                    turnTowardsPlayer(player);
                
                // Check for attack opportunities
                if (isPlayerCloseForSpecialAttack(player) && canDoSpecialAttack())
                    startSpecialAttack(player);
                else if (isPlayerCloseForAttack(player))
                    newState(ATTACK);
                
                // Move based on behavior state
                if (bossBehaviorState == STATE_PATROLLING)
                    move(lvlData);
                else if (bossBehaviorState == STATE_CHASING)
                    chasePlayer(lvlData, player);
                break;
            case ATTACK:
                if (aniIndex == 0)
                    attackChecked = false;
                
                // Check for player hit at specific animation frame
                if (aniIndex == 28 && !attackChecked) {
                    checkPlayerHit(attackBox, player);
                    // Play attack sound
                    AudioManager.playSFX("res/audio/boss_attack.wav");
                }
                break;
            case SPECIAL_ATTACK:
                if (aniIndex == 0) {
                    specialAttackChecked = false;
                    jumpingForSpecialAttack = true;
                    fallSpeed = jumpHeight; // Start the jump
                    inAir = true;
                }
                
                // The actual damage check happens in handleSpecialAttackJump
                break;
            case TIKBALANG_HIT:
                // No movement when hit
                break;
            case TIKBALANG_DEAD:
                // No movement when dead
                break;
        }
        
        // Update boss behavior state
        updateBossBehaviorState(player);
    }
    
    @Override
    protected void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            
            // Special handling for special attack animation
            if (enemyState == SPECIAL_ATTACK) {
                if (!specialAttackAnimComplete) {
                    aniIndex++;
                    if (aniIndex >= GetSpriteAmount(enemyType, enemyState)) {
                        // Instead of resetting animation, mark it as complete and freeze at last frame
                        specialAttackAnimComplete = true;
                        aniIndex = GetSpriteAmount(enemyType, enemyState) - 1; // Stay on last frame
                    }
                }
                // If animation is complete, don't increment the index - freeze on last frame
            } 
            // Regular animation handling for other states
            else {
                aniIndex++;
                if (aniIndex >= GetSpriteAmount(enemyType, enemyState)) {
                	// For death animation, we want to stay on the last frame
                	if (enemyState == TIKBALANG_DEAD) {
                	    aniIndex = GetSpriteAmount(enemyType, enemyState) - 1;
                	    active = false;  // Deactivate when death animation completes
                	} 
                	// For hit animation, go back to running once done
                	else if (enemyState == TIKBALANG_HIT) {
                	    aniIndex = 0;
                	    newState(RUNNING);
                	}
                    // For attack animation, go back to running after attack completes
                    else if (enemyState == ATTACK) {
                        aniIndex = 0;
                        newState(RUNNING);
                    } 
                    // For other states like IDLE and RUNNING, loop the animation
                    else {
                        aniIndex = 0;
                    }
                }
            }
        }
    }
    
    private boolean canDoSpecialAttack() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastSpecialAttackTime >= specialAttackCooldown;
    }
    
    
    
    private void startSpecialAttack(Player player) {
        isDoingSpecialAttack = true;
        newState(SPECIAL_ATTACK);
        specialAttackAnimComplete = false; // Reset animation completion flag
        lastSpecialAttackTime = System.currentTimeMillis();
        
        // Calculate target X position (the player's position)
        specialAttackTargetX = player.getHitbox().x;
        
        // Calculate horizontal jump speed based on distance to player
        float distanceX = specialAttackTargetX - hitbox.x;
        float jumpDuration = 1.0f; // estimated seconds the jump will take
        horizontalJumpSpeed = distanceX / (jumpDuration * 120); // 60 frames per second approx.
        
        // Cap the horizontal speed to a reasonable value
        float maxHorizontalSpeed = 1.01f * Game.SCALE;
        if (Math.abs(horizontalJumpSpeed) > maxHorizontalSpeed) {
            horizontalJumpSpeed = Math.signum(horizontalJumpSpeed) * maxHorizontalSpeed;
        }
        
        // Make sure we're on the ground exactly when starting
        if (!inAir) {
            // Ensure proper ground alignment before jumping
            int tileY = (int) ((hitbox.y + hitbox.height) / Game.TILES_SIZE);
            hitbox.y = tileY * Game.TILES_SIZE - hitbox.height;
        }
        
        // Play special attack sound
        AudioManager.playSFX("res/audio/boss_special.wav");
    }
    
    @Override
    public void hurt(int amount) {
        currentHealth -= amount;
        
        // If already in hit state or dead, don't reset animation
        if (enemyState != TIKBALANG_HIT && enemyState != TIKBALANG_DEAD) {
            // Use TIKBALANG_HIT instead of HIT for Tikbalang
            newState(TIKBALANG_HIT);
        }
        
        if (currentHealth <= 0) {
            // Use TIKBALANG_DEAD instead of DEAD for Tikbalang
            newState(TIKBALANG_DEAD);
            
            // Boss is dead, stop boss music and go back to level music
            AudioManager.stopMusic();
            AudioManager.playMusic("res/audio/normal_lvl_music.wav"); // Change to your actual level music path
        }
    }
    
    private void handleSpecialAttackJump(int[][] lvlData, Player player) {
        // Going up phase
        if (fallSpeed < 0) {
            // Try to move both horizontally and vertically
            float nextX = hitbox.x + horizontalJumpSpeed;
            float nextY = hitbox.y + fallSpeed;
            
            // Check if we can move to the new position
            if (CanMoveHere(nextX, nextY, hitbox.width, hitbox.height, lvlData)) {
                hitbox.x = nextX;
                hitbox.y = nextY;
                fallSpeed += GRAVITY;
            } else {
                // Check if we can move just vertically
                if (CanMoveHere(hitbox.x, nextY, hitbox.width, hitbox.height, lvlData)) {
                    hitbox.y = nextY;
                    fallSpeed += GRAVITY;
                } else {
                    // Hit ceiling, reverse direction
                    fallSpeed = 0;
                }
                
                // If we hit a wall, stop horizontal movement
                if (!CanMoveHere(nextX, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
                    horizontalJumpSpeed = 0;
                }
            }
        } 
        // Coming down phase
        else {
            // Fall faster during special attack for dramatic effect
            fallSpeed += GRAVITY * 2f;
            
            // Try to move both horizontally and vertically
            float nextX = hitbox.x + horizontalJumpSpeed;
            float nextY = hitbox.y + fallSpeed;
            
            // First check if we can move horizontally
            boolean canMoveHorizontally = CanMoveHere(nextX, hitbox.y, hitbox.width, hitbox.height, lvlData);
            if (canMoveHorizontally) {
                hitbox.x = nextX;
            } else {
                // Hit a wall, stop horizontal movement
                horizontalJumpSpeed = 0;
            }
            
            // Then check if we can move vertically
            if (CanMoveHere(hitbox.x, nextY, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y = nextY;
            } else {
                // We've hit the ground!
                
                // Find the exact ground position and add a small offset
                int maxTileY = (int)((hitbox.y + hitbox.height + fallSpeed) / Game.TILES_SIZE);
                hitbox.y = maxTileY * Game.TILES_SIZE - hitbox.height - 1; // Add a 1-pixel offset
                
                // Reset air state
                inAir = false;
                fallSpeed = 0;
                horizontalJumpSpeed = 0;
                jumpingForSpecialAttack = false;
                
                // Create ground impact effect
                if (!specialAttackChecked) {
                    specialAttackChecked = true;
                    checkSpecialAttackHit(player);
                    
                    // Play impact sound
                    AudioManager.playSFX("res/audio/boss_impact.wav");
                }
                
                // Return to running state after special attack
                newState(RUNNING);
                isDoingSpecialAttack = false;
                specialAttackAnimComplete = false; // Reset animation flag
            }
        }
    }
    
    private void checkSpecialAttackHit(Player player) {
        // Check if player is hit by special attack
        if (!player.isInvincible() && specialAttackBox.intersects(player.getHitbox())) {
            // Special attack deals more damage
            player.changeHealth(-25);
            
            // Stronger knockback from special attack
            float knockbackDirection = player.getHitbox().x < hitbox.x ? -2 : 2;
            player.applyKnockback(knockbackDirection);
        }
    }
    
    @Override
    protected void updateInAir(int[][] lvlData) {
        if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed, hitbox.width, hitbox.height, lvlData)) {
            // Can move, continue falling
            hitbox.y += fallSpeed;
            fallSpeed += GRAVITY;
        } else {
            // Collision detected
            
            if (fallSpeed > 0) {
                // We're falling and hit something below us
                int maxTileY = (int)((hitbox.y + hitbox.height + fallSpeed) / Game.TILES_SIZE);
                hitbox.y = maxTileY * Game.TILES_SIZE - hitbox.height - 20; // Add 1-pixel offset
                inAir = false;
                fallSpeed = 0;
            } else {
                // Hit ceiling
                int minTileY = (int)(hitbox.y / Game.TILES_SIZE);
                hitbox.y = minTileY * Game.TILES_SIZE + Game.TILES_SIZE;
                fallSpeed = 0;
            }
        }
    }
    
    protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
        if (!player.isInvincible() && attackBox.intersects(player.getHitbox())) {
            player.changeHealth(-GetEnemyDmg(enemyType));
            
            // Add knockback
            float knockbackDirection = player.getHitbox().x < hitbox.x ? -1 : 1;
            player.applyKnockback(knockbackDirection);
        }
        attackChecked = true;
    }
    
    private void updateBossBehaviorState(Player player) {
        // Don't change state if hit, dead, or doing special attack
        if (enemyState == TIKBALANG_HIT || enemyState == TIKBALANG_DEAD || isDoingSpecialAttack)
            return;
            
        // Calculate player distance
        float playerDistX = Math.abs(player.getHitbox().x - hitbox.x);
        float playerDistY = Math.abs(player.getHitbox().y - hitbox.y);
        
        // Different behavior states based on distance
        if (playerDistX <= attackDistance && playerDistY < 50 * Game.SCALE) {
            bossBehaviorState = STATE_ATTACKING;
        }
        else if (playerDistX <= specialAttackDistance && playerDistY < 150 * Game.SCALE && canDoSpecialAttack()) {
            bossBehaviorState = STATE_SPECIAL_ATTACK;
            
            // Occasionally use special attack when in range
            if (Math.random() < 0.1) { // 1% chance per frame when in range
                startSpecialAttack(player);
            }
        }
        else if (playerDistX < 400 * Game.SCALE) {
            bossBehaviorState = STATE_CHASING;
            
            // Turn towards player occasionally
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDirectionChangeTime > minDirectionChangeDelay) {
                turnTowardsPlayer(player);
                lastDirectionChangeTime = currentTime;
            }
        }
        else {
            bossBehaviorState = STATE_PATROLLING;
        }
    }
    
    private boolean isPlayerCloseForSpecialAttack(Player player) {
        int absValueX = (int) Math.abs(player.hitbox.x - hitbox.x);
        int absValueY = (int) Math.abs(player.hitbox.y - hitbox.y);
        return absValueX <= specialAttackDistance && absValueY <= 150 * Game.SCALE;
    }
    
    // Sprite flipping methods
    public int flipX() {
        if (walkDir == RIGHT)
            return 0;
        else
            return width;
    }
    
    public int flipW() {
        if (walkDir == RIGHT)
            return 1;
        else
            return -1;
    }
    
    // Chase player at a faster speed
    private void chasePlayer(int[][] lvlData, Player player) {
        float xSpeed;
        
        // Use chase speed which is faster than walk speed
        if (walkDir == LEFT)
            xSpeed = -chaseSpeed;
        else
            xSpeed = chaseSpeed;
            
        // Move in current direction with floor check
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            if (IsFloor(hitbox, xSpeed, lvlData)) {
                hitbox.x += xSpeed;
            } else {
                // No floor ahead, change direction
                changeWalkDir();
                lastDirectionChangeTime = System.currentTimeMillis();
            }
        } else {
            // Obstacle ahead, change direction
            changeWalkDir();
            lastDirectionChangeTime = System.currentTimeMillis();
        }
        
        
        
    }
    
    // Draw debug info for development
    @Override
    public void drawHitbox(Graphics g, int xLvlOffset) {
        // Basic hitbox
        g.setColor(Color.RED);
        g.drawRect((int)hitbox.x - xLvlOffset, (int)hitbox.y, 
                  (int)hitbox.width, (int)hitbox.height);
        
        // Attack boxes
        g.setColor(Color.YELLOW);
        g.drawRect((int)(attackBox.x - xLvlOffset), (int)attackBox.y, 
                  (int)attackBox.width, (int)attackBox.height);
        
        if (isDoingSpecialAttack || enemyState == SPECIAL_ATTACK) {
            g.setColor(Color.ORANGE);
            g.drawRect((int)(specialAttackBox.x - xLvlOffset), (int)specialAttackBox.y, 
                      (int)specialAttackBox.width, (int)specialAttackBox.height);
        }
        
        // Show state indicator above boss head
        g.setColor(getStateColor());
        g.fillRect((int)hitbox.x - xLvlOffset, (int)hitbox.y - 20, 30, 15);
        
        // Show state text and animation info
        g.setColor(Color.WHITE);
        String stateText = getStateText();
        g.drawString(stateText, (int)hitbox.x - xLvlOffset - 10, (int)hitbox.y - 25);
        
        // Show animation info
        String aniText = "A:" + aniIndex + "/" + GetSpriteAmount(enemyType, enemyState);
        g.drawString(aniText, (int)hitbox.x - xLvlOffset - 10, (int)hitbox.y - 40);
        
        // Show health
        String healthText = "HP:" + currentHealth + "/" + maxHealth;
        g.drawString(healthText, (int)hitbox.x - xLvlOffset - 10, (int)hitbox.y - 55);
    }
    
    
    
    private Color getStateColor() {
        switch (bossBehaviorState) {
            case STATE_PATROLLING: return Color.BLUE;
            case STATE_CHASING: return Color.GREEN;
            case STATE_ATTACKING: return Color.RED;
            case STATE_SPECIAL_ATTACK: return Color.MAGENTA;
            default: return Color.WHITE;
        }
    }
    
    private String getStateText() {
        switch (bossBehaviorState) {
            case STATE_PATROLLING: return "Patrol";
            case STATE_CHASING: return "Chase";
            case STATE_ATTACKING: return "Attack";
            case STATE_SPECIAL_ATTACK: return "Special";
            default: return "Unknown";
        }
    }
    
    public float getHitboxX() {
    	return hitbox.x;
    }
    
    public float getHitboxY() {
    	return hitbox.y;
    }
    
    // Reset boss to initial state
    public void resetEnemy() {
    	resetStrength();
        hitbox.x = x;
        hitbox.y = y;
        firstUpdate = true;
        currentHealth = maxHealth;
        newState(IDLE);
        active = true;
        fallSpeed = 0;
        isDoingSpecialAttack = false;
        jumpingForSpecialAttack = false;
        lastSpecialAttackTime = 0;
        playerSpotted = false; // Reset the player spotted flag
    }
}