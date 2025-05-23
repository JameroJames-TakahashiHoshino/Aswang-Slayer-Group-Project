package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.Directions.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import main.Game;

public class Sigbin extends Enemy {
    
    // Add a chase state tracking variable
    private int enemyBehaviorState = STATE_PATROLLING;
    private float chaseSpeed = 0.6f * Game.SCALE;
    
    // Variables to make chase more natural
    private long lastDirectionChangeTime = 0;
    private long minDirectionChangeDelay = 0; // milliseconds
    
    // Variables for attack control
    private boolean attackChecked = false;
    int attackBoxOffsetX;    
    
    // attack hitbox
    private Rectangle2D.Float attackBox;
    
    
    public Sigbin(float x, float y) {
        // Same constructor as before
        super(x, y - (32 * Game.SCALE)-scaleCalc(Game.SCALE, SIGBIN), SIGBIN_WIDTH, SIGBIN_HEIGHT, SIGBIN);
        
        float hitboxWidth = SIGBIN_WIDTH * 0.4f;
        float hitboxHeight = SIGBIN_HEIGHT * 0.25f;
        float hitboxX = x + (SIGBIN_WIDTH/2) - (hitboxWidth/2);
        
        initHitbox(hitboxX, y - (32 * Game.SCALE)-scaleCalc(Game.SCALE, SIGBIN), (int)hitboxWidth, (int)hitboxHeight);
        
        initAttackBox();
    }

    
    // the attackhitbox of the enemy
    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x,y,(int)(120* Game.SCALE),(int)(70* Game.SCALE));
        attackBoxOffsetX = (int)(Game.SCALE * 30);
    }
    
    
    public void update(int[][] lvlData, Player player) {
        // First update behavior based on environment
        updateBehavior(lvlData, player);
        
        // Then handle animation ticks
        updateAnimationTick();
        
        // Update attack box position
        updateAttackBox();
    }
    
    private void updateAttackBox() {
        attackBox.x = hitbox.x - attackBoxOffsetX;
        attackBox.y = hitbox.y;
    }

    // Renamed from updateMove to match reference code
    private void updateBehavior(int[][] lvlData, Player player) {
        if (firstUpdate) {
            firstUpdateCheck(lvlData);
            return;
        }

        if (inAir) {
            updateInAir(lvlData);
            return;
        }
        
        // This matches the reference code's approach - switch based on enemyState not behaviorState
        switch (enemyState) {
            case IDLE:
                newState(RUNNING);
                break;
            case RUNNING:
                // Check if we can see the player to determine chase or attack
                if (canSeePlayer(lvlData, player))
                    turnTowardsPlayer(player);
                if (isPlayerCloseForAttack(player))
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
                
                // Check for player hit at specific animation frame (frame 20)
                if (aniIndex == 20 && !attackChecked)
                    checkPlayerHit(attackBox, player);
                break;
            case HIT:
                 
                break;
            case DEAD:
                // No movement when dead
                break;
        }
        
        // Update behavioral state after handling movement
        updateBehaviorState(player);
    }
    
    // Added from reference code - checks if player is hit by enemy attack
    protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
        if (!player.isInvincible() && attackBox.intersects(player.getHitbox())) {
            player.changeHealth(-GetEnemyDmg(enemyType));
            
            // Add knockback
            float knockbackDirection = player.getHitbox().x < hitbox.x ? -1 : 1;
            player.applyKnockback(knockbackDirection);
        }
        attackChecked = true;
    }
    
    private void updateBehaviorState(Player player) {
        // Don't change state if hit or dead
        if (enemyState == HIT || enemyState == DEAD)
            return;
            	
        // Calculate player distance
        float playerDistX = Math.abs(player.getHitbox().x - hitbox.x);
        float playerDistY = Math.abs(player.getHitbox().y - hitbox.y);
        
        // If player is in attack range, prioritize attack state
        if (playerDistX <= attackDistance && playerDistY < 50 * Game.SCALE) {
            enemyBehaviorState = STATE_ATTACKING;
            // Note: We don't change enemyState here as that's handled in updateBehavior
        }
        // Otherwise, check if player is in chase range
        else if (playerDistX < 300 * Game.SCALE && playerDistY < 200 * Game.SCALE) {
            enemyBehaviorState = STATE_CHASING;
            
            // Turn towards player occasionally
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDirectionChangeTime > minDirectionChangeDelay) {
                turnTowardsPlayer(player);
                lastDirectionChangeTime = currentTime;
            }
        }
        // If player is not in sight, go back to patrolling
        else {
            enemyBehaviorState = STATE_PATROLLING;
        }
    }
    
    // fliping method for sprite
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
    
    
    // for debugging hitboxes
    @Override
    public void drawHitbox(Graphics g, int xLvlOffset) {
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
        
        g.setColor(Color.red);
        g.drawRect((int)(attackBox.x - xLvlOffset), (int)attackBox.y, (int)attackBox.width, (int)attackBox.height);
    }
    
    private Color getStateColor() {
        switch (enemyBehaviorState) {
            case STATE_PATROLLING: return Color.BLUE;
            case STATE_CHASING: return Color.GREEN;
            case STATE_ATTACKING: return Color.RED;
            default: return Color.WHITE;
        }
    }
    
    private String getStateText() {
        switch (enemyBehaviorState) {
            case STATE_PATROLLING: return "Patrol";
            case STATE_CHASING: return "Chase";
            case STATE_ATTACKING: return "Attack";
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
    }
}