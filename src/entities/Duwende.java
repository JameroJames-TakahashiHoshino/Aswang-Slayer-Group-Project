package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.GRAVITY;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import audio.AudioManager;
import main.Game;

public class Duwende extends Enemy {
    
    // State constants
    private static final int STATE_HIDING = 3;
    
    // Add state tracking variable
    private int enemyBehaviorState = STATE_PATROLLING;
    private float chaseSpeed = 0.9f * Game.SCALE; // Faster than Sigbin
    
    // Variables for natural movement
    private long lastDirectionChangeTime = 0;
    private long minDirectionChangeDelay = 500; // milliseconds
    
    // Variables for attack control
    private boolean attackChecked = false;
    int attackBoxOffsetX;
    
    // Special ability: disappear/hide
    private boolean isInvisible = false;
    private long lastDisappearTime = 0;
    private int disappearCooldown = 2000; // 2 seconds cooldown
    private int invisibilityDuration = 2000; // 2 seconds duration
    private float alphaValue = 1.0f; // For transparency effects
    
    // attack hitbox
    private Rectangle2D.Float attackBox;
    
    public Duwende(float x, float y) {
        // Use the dimensions you defined in Constants.java
        super(x, y-(19 * Game.SCALE)-scaleCalc(Game.SCALE, DUWENDE), DUWENDE_WIDTH, DUWENDE_HEIGHT, DUWENDE);
        
        float hitboxWidth = DUWENDE_WIDTH * 0.10f;
        float hitboxHeight = DUWENDE_HEIGHT * 0.2f;
        float hitboxX = x + (DUWENDE_WIDTH/2) - (hitboxWidth/2);
        
        initHitbox(hitboxX, y-(19 * Game.SCALE)-scaleCalc(Game.SCALE, DUWENDE), (int)hitboxWidth, (int)hitboxHeight);
        
        initAttackBox();
    }
    
   
    // The attack hitbox of the enemy
    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int)(20 * Game.SCALE), (int)(20* Game.SCALE));
        attackBoxOffsetX = (int)(Game.SCALE * 10);
    }

    public void update(int[][] lvlData, Player player) {
        // First update behavior based on environment
        updateBehavior(lvlData, player);
        
        // Then handle animation ticks
        updateAnimationTick();
        
        // Update attack box position
        updateAttackBox();
        
        // Update invisibility status
        updateInvisibility();
    }
    
    private void updateAttackBox() {
        // Position attack box based on direction the Duwende is facing
        if (walkDir == LEFT) {
            // Attack box on left side
            attackBox.x = hitbox.x - attackBox.width + (hitbox.width / 2);
        } else {
            // Attack box on right side
            attackBox.x = hitbox.x + (hitbox.width / 2);
        }
        
        // Keep attack box aligned vertically with hitbox
        attackBox.y = hitbox.y;
    }
    
    private void updateInvisibility() {
        if (isInvisible) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDisappearTime > invisibilityDuration) {
                isInvisible = false;
                alphaValue = 1.0f;
            }
        }
    }

    
    
    private void updateBehavior(int[][] lvlData, Player player) {
        if (firstUpdate) {
            firstUpdateCheck(lvlData);
            return;
        }

        if (inAir) {
            updateInAir(lvlData);
            return;
        }
        
        // State machine based on enemy state
        switch (enemyState) {
            case IDLE:
                newState(RUNNING);
                break;
            case RUNNING:
                // Check if we can see the player
                if (canSeePlayer(lvlData, player))
                    turnTowardsPlayer(player);
                
                // Check if we should hide
                if (shouldDisappear(player))
                    startDisappear();
                // Check if we should attack
                else if (isPlayerCloseForAttack(player))
                    newState(ATTACK);
                
                // Move based on behavior state
                if (enemyBehaviorState == STATE_PATROLLING)
                    move(lvlData);
                else if (enemyBehaviorState == STATE_CHASING)
                    chasePlayer(lvlData, player);
                break;
            case ATTACK:
                if (aniIndex == 0)
                    attackChecked = false;
                
                // Check for player hit at specific animation frame
                if (aniIndex == 10 && !attackChecked)
                    checkPlayerHit(attackBox, player);
                break;
            case DUWENDE_DISAPPEAR:
                // Handle disappear animation
                if (aniIndex == GetSpriteAmount(enemyType, enemyState) - 1) {
                    isInvisible = true;
                    lastDisappearTime = System.currentTimeMillis();
                    newState(RUNNING);
                }
                break;
            case HIT:
                // No movement when hit
                break;
            case DEAD:
                // No movement when dead
                break;
        }
        
        // Update behavior state
        updateBehaviorState(player);
    }
    
    // Check if the Duwende should use its disappear ability
    private boolean shouldDisappear(Player player) {
        // Calculate player distance
        float playerDistX = Math.abs(player.getHitbox().x - hitbox.x);
        
        // Can only disappear when cooldown is ready
        if (!canDisappear())
            return false;
        
        // Disappear when player gets too close but not close enough for attack
        return playerDistX < attackDistance * 3 && playerDistX > attackDistance * 1.5;
    }
    
    private boolean canDisappear() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastDisappearTime > disappearCooldown + invisibilityDuration;
    }
    
    private void startDisappear() {
        newState(DUWENDE_DISAPPEAR);
        // Play disappear sound if you have one
        // AudioManager.playSFX("res/audio/duwende_disappear.wav");
    }
    
    protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
        if (!player.isInvincible() && attackBox.intersects(player.getHitbox())) {
            player.changeHealth(-GetEnemyDmg(enemyType));
            
            // Add knockback (less than other enemies)
            float knockbackDirection = player.getHitbox().x < hitbox.x ? -0.7f : 0.7f;
            player.applyKnockback(knockbackDirection);
        }
        attackChecked = true;
    }
    
    private void updateBehaviorState(Player player) {
        // Don't change state if hit, dead, or disappearing
        if (enemyState == HIT || enemyState == DEAD || enemyState == DUWENDE_DISAPPEAR)
            return;
            
        // Calculate player distance
        float playerDistX = Math.abs(player.getHitbox().x - hitbox.x);
        float playerDistY = Math.abs(player.getHitbox().y - hitbox.y);
        
        // Different behavior states based on distance
        if (playerDistX <= attackDistance && playerDistY < 50 * Game.SCALE) {
            enemyBehaviorState = STATE_ATTACKING;
        }
        else if (playerDistX < 350 * Game.SCALE && playerDistY < 200 * Game.SCALE) {
            enemyBehaviorState = STATE_CHASING;
            
            // Turn towards player occasionally
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDirectionChangeTime > minDirectionChangeDelay) {
                turnTowardsPlayer(player);
                lastDirectionChangeTime = currentTime;
            }
        }
        else {
            enemyBehaviorState = STATE_PATROLLING;
        }
        
        // Special case: if invisible, don't attack
        if (isInvisible) {
            enemyBehaviorState = STATE_HIDING;
        }
    }
    
    // Flipping method for sprite
    public int flipX() {
        if(walkDir == RIGHT)
            return 0;
        else
            return width;
    }
    
    public int flipW() {
        if(walkDir == RIGHT)
            return 1;
        else
            return -1;
    }
    
    private void chasePlayer(int[][] lvlData, Player player) {
        // If invisible, move at normal speed
        if (isInvisible) {
            move(lvlData);
            return;
        }
        
        float xSpeed;
        
        // Use appropriate speed based on direction
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
    
    // Override hurt method to match your state definitions
    @Override
    public void hurt(int amount) {
        currentHealth -= amount;
        
        if (currentHealth <= 0) {
            newState(DEAD);
        } else {
            newState(HIT);
        }
    }
    
    // For debugging hitboxes
    @Override
    public void drawHitbox(Graphics g, int xLvlOffset) {
        // Skip drawing if invisible (for debugging)
        if (isInvisible)
            return;
            
        // Basic hitbox
        g.setColor(Color.RED);
        g.drawRect((int)hitbox.x - xLvlOffset, (int)hitbox.y, 
                  (int)hitbox.width, (int)hitbox.height);
        
        // Show state indicator above enemy head
        g.setColor(getStateColor());
        g.fillRect((int)hitbox.x - xLvlOffset, (int)hitbox.y - 15, 20, 10);
        
        // Show state text and animation info for debugging
        g.setColor(Color.WHITE);
        String stateText = getStateText();	
        g.drawString(stateText, (int)hitbox.x - xLvlOffset - 10, (int)hitbox.y - 20);
        
        // Show animation info
        String aniText = "A:" + aniIndex + "/" + GetSpriteAmount(enemyType, enemyState);
        g.drawString(aniText, (int)hitbox.x - xLvlOffset - 10, (int)hitbox.y - 35);
        
        // Add visual indicator for direction faced
        String dirText = (walkDir == LEFT) ? "←" : "→";
        g.drawString(dirText, (int)hitbox.x - xLvlOffset, (int)hitbox.y - 50);
        
        g.setColor(Color.yellow);
        g.drawRect((int)(attackBox.x - xLvlOffset), (int)attackBox.y, (int)attackBox.width, (int)attackBox.height);
    }
    
    private Color getStateColor() {
        switch (enemyBehaviorState) {
            case STATE_PATROLLING: return Color.BLUE;
            case STATE_CHASING: return Color.GREEN;
            case STATE_ATTACKING: return Color.RED;
            case STATE_HIDING: return Color.LIGHT_GRAY;
            default: return Color.WHITE;
        }
    }
    
    private String getStateText() {
        switch (enemyBehaviorState) {
            case STATE_PATROLLING: return "Patrol";
            case STATE_CHASING: return "Chase";
            case STATE_ATTACKING: return "Attack";
            case STATE_HIDING: return "Hidden";
            default: return "Unknown";
        }
    }
    
    // Reset enemy to initial state
    public void resetEnemy() {
        hitbox.x = x;
        hitbox.y = y;
        firstUpdate = true;
        currentHealth = maxHealth;
        newState(IDLE);
        active = true;
        fallSpeed = 0;
        isInvisible = false;
        alphaValue = 1.0f;
    }
    
    // Allow other classes to check visibility
    public boolean isInvisible() {
        return isInvisible;
    }
    
    public float getAlphaValue() {
        return alphaValue;
    }
}