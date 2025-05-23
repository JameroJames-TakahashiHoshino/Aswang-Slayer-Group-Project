package entities;

import static utilz.Constants.PlayerConstants.*;

import audio.AudioManager; 
import static utilz.HelpMethods.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gameStates.Playing;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.GRAVITY;

public class Player extends Entity {
    
	private boolean dying = false;
	
	// Animation variables
    private BufferedImage[][] animations;
    private int aniSpeed = 4;
    private int playerAction = IDLE;
    
    // Movement state variables
    private boolean moving = false, attacking = false;
    private boolean left, right, jump;
    private float playerSpeed = 1.0f * Game.SCALE;
    private int[][] lvlData;
    
    // Sprite offsets for rendering
    private float xDrawOffset = 100 * Game.SCALE; 
    private float yDrawOffset = 27 * Game.SCALE;
    
    // Jumping / Gravity
    private float airSpeed = 0f;
    
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.1f * Game.SCALE;
    private boolean inAir = false;
    
    // STATUS BAR UI
    private BufferedImage statusBarImg;
    private int statusBarWidth = (int) (3*(80 * Game.SCALE));
    private int statusBarHeight = (int) (3*(27 * Game.SCALE));
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);
    
    // HEALTH VARIABLES
    private int healthBarWidth = (int) (171 * Game.SCALE);
    private int healthBarHeight = (int) (12 * Game.SCALE);
    private int healthBarXStart = (int) (58 * Game.SCALE);
    private int healthBarYStart = (int) (24 * Game.SCALE);
    private int maxHealth = 100;
    private int currentHealth = maxHealth;
    private int healthWidth = healthBarWidth;
    
    // ENERGY VARIABLES - Properly scaled for the UI
    private int maxEnergy = 100;
    private int currentEnergy = maxEnergy;
    private int energyBarWidth = (int) (120 * Game.SCALE); 
    private int energyWidth; // Calculated dynamically
    private int energyBarXStart = (int) (60 * Game.SCALE);
    private int energyBarYStart = (int) (51 * Game.SCALE);
    private int energyBarHeight = (int) (13 * Game.SCALE);
    private int dashEnergyCost = 40;
    // Use integer accumulation for smooth fractional energy regeneration
    private float energyAccumulator = 2f;
    private float energyRegenRate = 0.1f;
    
    // Hit Related Variables 
    private boolean hit = false;
    private int hitTimer = 0;
    private int hitTimerDuration = 25; // How long the hit animation plays
    private boolean invincible = false;
    private int invincibilityTimer = 0;
    private int invincibilityDuration = 120; // How long the player is invincible after being hit
    
    // Dash related variables
    private boolean dashing = false;
    private float dashSpeed = 4.0f * Game.SCALE;
    private float dashDistance = 4 * Game.TILES_SIZE; 
    private float dashStartX;
    private float dashDir = 1;
    
    // Sprite flipping
    private int flipX = 0;
    private int flipW = 1;
    
    // ATTACK BOX
    private Rectangle2D.Float attackBox;
    
    private Playing playing;
    
    // Attack variables
    private int attackIndex = 0; // To cycle through attacks
    private final int ATTACK_COUNT = 3; // Total number of attack animations
    private boolean attackChecked = false; // Flag to ensure we only apply damage once per attack
    private long lastAttackTime = 0; // For combo timing
    private long attackComboWindow = 800; // Time window in milliseconds to chain attacks (0.8 seconds)
    
    private boolean attackSoundPlayed = false;
    
    // Constructor
    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        loadAnimations();
        
        // Adjust hitbox size to match visible character
        float hitboxWidth = width * 0.2f;
        float hitboxHeight = height * 0.4f;
        float hitboxX = x + width/2 - hitboxWidth/2;
        float hitboxY = y + height - hitboxHeight - 2;
        
        initHitbox(hitboxX, hitboxY, (int)hitboxWidth, (int)hitboxHeight);
        initAttackBox();
    }
    
    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int)(60* Game.SCALE), (int)(60* Game.SCALE));
    }
    
    // Main update method
    public void update() {
        // First check if player is dying or dead
        if (dying) {
            updateAnimationTick();
            // When death animation copmpletes, move to game over state
            if (playerAction == DEATH && aniIndex >= GetSpriteAmount(DEATH) - 1 && aniTick >= aniSpeed - 1) {
                playing.setGameOver(true);
                dying = false;
            }
            return; // Skip other updates while dying
        }
        
        //DIE WHEN FALL IN THE BORDER OF THE SCREEN
        if (hitbox.y + hitbox.height > Game.GAME_HEIGHT - 2) {
        	dying = true;
            hit = false;  // Cancel hit animation if dying
            playerAction = DEATH;
            resetAniTick();
            playing.setPlayerDying(true);
            
            AudioManager.playSFX("res/audio/death.wav");
        }
        
        // Update hit state
        updateHitState();
        
        // Update health and UI
        updateHealthBar();
        updateAttackBox();
        updateEnergyRestore();
        updateEnergyBar();
        
        // Only allow movement and attacks if not in hit animation
        if (!hit) {
            updatePos();
            
            if (attacking)
                checkAttack();
        }
        
        updateAnimationTick();
        setAnimation();
        
        // Update invincibility
        updateInvincibility();
    }
    
    private void updateHitState() {
        if (hit) {
            hitTimer++;
            if (hitTimer >= hitTimerDuration) {
                hit = false;
                hitTimer = 0;
                // Enable controls again after hit animation
            }
        }
    }
    
    private void updateInvincibility() {
        if (invincible) {
            invincibilityTimer++;
            if (invincibilityTimer >= invincibilityDuration) {
                invincible = false;
                invincibilityTimer = 0;
            }
        }
    }
    
    private void updateEnergyRestore() {
    	if (!dashing && currentEnergy < maxEnergy) {
            // Accumulate fractional energy
            energyAccumulator += energyRegenRate;
            
            // When we've accumulated at least 1 energy point, add it
            if (energyAccumulator >= 1.0f) {
                int pointsToAdd = (int)energyAccumulator;
                currentEnergy += pointsToAdd;
                energyAccumulator -= pointsToAdd;  // Keep the remainder
                
                if (currentEnergy > maxEnergy)
                    currentEnergy = maxEnergy;
            }
        }
	}

	// Movement and physics
    private void updatePos() {
    	
    	// Don't move if currently in hit animation
        if (hit)
            return;
            
        moving = false;
    	
        
        // Handle dashing - THIS IS THE KEY FIX!
        if (dashing) {
            handleDashing();
            return; // Skip normal movement when dashing
        }
        
        if (jump)
            jump();
            
        if (!inAir)
            if ((!left && !right) || (left && right))
                return;

        float xSpeed = 0;

        if (left) {
            xSpeed -= playerSpeed;
            flipX = width;
            flipW = -1;
        }
            
        if (right) {
            xSpeed += playerSpeed;
            flipX = 0;
            flipW = 1;
        }
            
        if (!inAir)
            if (!IsEntityOnFloor(hitbox, lvlData))
                inAir = true;

        if (inAir) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0)
                    resetInAir();
                else
                    airSpeed = fallSpeedAfterCollision;
                updateXPos(xSpeed);
            }
        } else {
            updateXPos(xSpeed);
        }
        
        moving = true;
    }
    
    // Dash implementation
    private void dash() {
        // Check if we have enough energy to dash
        if (dashing || currentEnergy < dashEnergyCost)
            return;
            
        // Set dash direction based on where player is facing
        if (right)
            dashDir = 1;
        else if (left)
            dashDir = -1;
        else
            dashDir = (flipW > 0) ? 1 : -1; // Use current facing direction
            
        dashing = true;
        dashStartX = hitbox.x;
        
        // Consume energy
        currentEnergy -= dashEnergyCost;
        if (currentEnergy < 0)
            currentEnergy = 0;
        
        // Set dash animation
        playerAction = DASH;
        resetAniTick();
        
        
        AudioManager.playSFX("res/audio/dash.wav");
    }
    
    private void handleDashing() {
        // Calculate how far we've dashed so far
        float distanceDashed = Math.abs(hitbox.x - dashStartX);
        
        // Check if we've reached the dash distance
        if (distanceDashed >= dashDistance) {
            dashing = false;
            return;
        }
        
        // Calculate the dash movement with dash direction
        float xSpeed = dashSpeed * dashDir;
        
        // Check if we can move to the new position
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
        } else {
            // Hit a wall, stop dashing
            dashing = false;
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
        }
        
        // We're definitely moving while dashing
        moving = true;
    }
    
    
    // Animation methods
    private void updateAnimationTick() {
    	aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(playerAction)) {
                aniIndex = 0;
                
                // Reset states after animations complete
                if (playerAction == ATTACK_1 || playerAction == ATTACK_2 || playerAction == ATTACK_3) {
                    attacking = false;
                } else if (playerAction == DASH) {
                    dashing = false;
                } else if (playerAction == DEATH && dying) {
                    // When death animation completes, trigger game over
                    dying = false;
                    playing.setGameOver(true);
                }
            }
        }
    }
    
    private void setAnimation() {
        int startAni = playerAction;

        // Priority order: Death > Hit > Dash > Attack > Jump > Move > Idle
        if (dying) {
            playerAction = DEATH;
        } else if (hit) {
            playerAction = HIT;
        } else if (dashing) {
            playerAction = DASH;
        } else if (attacking) {
            // Your attack animation logic
            switch (attackIndex) {
                case 0:
                    playerAction = ATTACK_1;
                    break;
                case 1:
                    playerAction = ATTACK_2;
                    break;
                case 2:
                    playerAction = ATTACK_3;
                    break;
            }
        } else if (inAir) {
            if (airSpeed < 0)
                playerAction = JUMP_1;
            else
                playerAction = FALL;
        } else if (moving) {
            playerAction = RUNNING;
        } else {
            playerAction = IDLE;
        }

        if (startAni != playerAction)
            resetAniTick();
    }
    
    
    // UI rendering
    public void render(Graphics g, int lvlOffset) {
        // If invincible, make player flash by only rendering on even frames
        if (invincible && invincibilityTimer % 4 >= 2) {
            // Skip rendering to create blinking effect
        } else {
            g.drawImage(
                animations[playerAction][aniIndex], 
                (int)(hitbox.x - xDrawOffset) - lvlOffset + flipX, 
                (int)(hitbox.y - yDrawOffset), 
                width * flipW, 
                height, 
                null
            );
        }
        
//        drawAttackBox(g, lvlOffset);
//        drawHitbox(g, lvlOffset);
        
        drawUI(g);
    }
    
    private void drawAttackBox(Graphics g, int lvlOffset) {
        g.setColor(Color.yellow);
        g.drawRect((int)attackBox.x - lvlOffset, (int)attackBox.y, (int)attackBox.width, (int)attackBox.height);
    }
    
    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        
        // Draw health bar
        g.setColor(new Color(172, 50, 50));
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
        
        // Draw energy bar
        g.setColor(new Color(160, 152, 3));  // Light blue color for energy
        g.fillRect(energyBarXStart + statusBarX, energyBarYStart + statusBarY, energyWidth, energyBarHeight);
        
    }
    
    // Utility methods
    private void updateAttackBox() {
        if (right) {
            attackBox.x = hitbox.x + hitbox.width + (int)(Game.SCALE * -20);   
        } else if (left) {
            attackBox.x = hitbox.x - hitbox.width - (int)(Game.SCALE * -20);
        }
        attackBox.y = hitbox.y + (int)(Game.SCALE * 40);
    }
    
    private void updateHealthBar() {
        healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
    }
    
    private void updateEnergyBar() {
        // This ensures the bar scales with both the UI and is proportional to energy amount
        energyWidth = (int) ((currentEnergy / (float) maxEnergy) * energyBarWidth);
    }
    
    private void jump() {
        if (inAir)
            return;
        inAir = true;
        airSpeed = jumpSpeed;
        
        AudioManager.playSFX("res/audio/jump.wav");
    }
    
    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }
    
    private void updateXPos(float xSpeed) {
        boolean canMove = CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData);
        
        if (canMove) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
        }
    }
    
    private void checkAttack() {
        // Only check attack once at the right frame of animation
        if (attacking) {
            // Different attacks have different "impact" frames
            boolean checkNow = false;
            boolean playSound = false;
            
            switch (attackIndex) {
                case 0: // First attack (quicker)
                    if (aniIndex == 1) {
                        checkNow = true;
                        if (!attackSoundPlayed) {
                            playSound = true;
                            attackSoundPlayed = true;
                        }
                    }
                    break;
                case 1: // Second attack (mid point)
                    if (aniIndex == 2) {
                        checkNow = true;
                        if (!attackSoundPlayed) {
                            playSound = true;
                            attackSoundPlayed = true;
                        }
                    }
                    break;
                case 2: // Third attack (heavy, later frames)
                    if (aniIndex == 3) {
                        checkNow = true;
                        if (!attackSoundPlayed) {
                            playSound = true;
                            attackSoundPlayed = true;
                        }
                    }
                    break;
            }
            
            // Play the sound if needed
            if (playSound) {
                switch (attackIndex) {
                    case 0:
                        AudioManager.playSFX("res/audio/attack_1.wav");
                        break;
                    case 1:
                        AudioManager.playSFX("res/audio/attack_2.wav");
                        break;
                    case 2:
                        AudioManager.playSFX("res/audio/attack_3.wav");
                        break;
                }
            }
            
            // Check for damage if needed
            if (checkNow && !attackChecked) {
                // Apply different effects based on attack type
                int damage = 1; // Default damage
                switch (attackIndex) {
                    case 0: // Light attack
                        damage = 10;
                        break;
                    case 1: // Medium attack
                        damage = 20;
                        break;
                    case 2: // Heavy attack
                        damage = 10;
                        break;
                }
                
                // Check for hits with the appropriate damage
                playing.checkEnemyHit(attackBox, damage);
                attackChecked = true;
            }
        }
    }
    
    private void loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        animations = new BufferedImage[12][30];
        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 256, j * 256, 256, 256);
        
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }
    
    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }
    
    // Public methods
    public void changeHealth(int value) {
        // Only apply damage if not currently invincible
        if (value < 0 && !invincible) {
            // Trigger hit animation
            if (!hit && !dying) {
                hit = true;
                hitTimer = 0;
                invincible = true;
                invincibilityTimer = 0;
                
                AudioManager.playSFX("res/audio/hit.wav");
                
                // Stop other actions
                attacking = false;
                dashing = false;
                
                // Set hit animation
                playerAction = HIT;
                resetAniTick();
            }
            
            // Apply health change
            currentHealth += value;
            
            // Handle death
            if (currentHealth <= 0) {
                currentHealth = 0;
                // Play death animation
                if (!dying) {
                    dying = true;
                    hit = false;  // Cancel hit animation if dying
                    playerAction = DEATH;
                    resetAniTick();
                    playing.setPlayerDying(true);
                    
                    AudioManager.playSFX("res/audio/death.wav");
                }
            }
        } else if (value > 0) {
            // For healing, just add health without hit animation
            currentHealth += value;
            if (currentHealth >= maxHealth)
                currentHealth = maxHealth;
        }
    }
    
    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }
    
    public void resetDirBooleans() {
        left = false;
        right = false;
    }
    
    public void resetAll() {
        resetDirBooleans();
        dashing = false;
        attacking = false;
        inAir = false;
        airSpeed = 0;
        currentEnergy = maxEnergy;
        currentHealth = maxHealth;
        dying = false;
        hit = false;
        hitTimer = 0;
        invincible = false;
        invincibilityTimer = 0;
        playerAction = IDLE;
        aniIndex = 0;
        aniTick = 0;
    }
    
    // Setters & getters
    public void setAttacking(boolean attacking) {
        // Don't allow attacking if in hit animation or dying
        if (!hit && !dying && attacking && !this.attacking) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastAttackTime <= attackComboWindow) {
                attackIndex = (attackIndex + 1) % ATTACK_COUNT;
            } else {
                attackIndex = 0;
            }
            
            // Play attack sound based on the attack type
            switch (attackIndex) {
                case 0:
                    AudioManager.playSFX("res/audio/attack_1.wav");
                    break;
                case 1:
                    AudioManager.playSFX("res/audio/attack_2.wav");
                    break;
                case 2:
                    AudioManager.playSFX("res/audio/attack_3.wav");
                    break;
            }
            
            this.attacking = true;
            attackChecked = false;
            resetAniTick();
            lastAttackTime = currentTime;
        }
    }
    
    public void setDash(boolean dash) {
        if (dash && !hit && !dying)
            dash();
    }
    
    public void setLeft(boolean left) {
        this.left = left;
    }
    
    public void setRight(boolean right) {
        this.right = right;
    }
    
    
    public void setJump(boolean jump) {
        this.jump = jump;
    } 
    
    public int getCurrentHealth() {
		return currentHealth;
	}

	public void resetPosition(){
    	// Reset player to starting position
        float x = 0; // Starting X
        float y = 0; // Starting Y - adjust if needed
        
        // Reset the hitbox position
        hitbox.x = x + width/2 - hitbox.width/2;
        hitbox.y = y + height - hitbox.height - 2;
        
        // Reset entity position (super.x and super.y)
        super.x = hitbox.x;
        super.y = hitbox.y;
    }
    
    public boolean isLeft() {
        return left;
    }
    
    public boolean isRight() {
        return right;
    }
    
    public boolean isInvincible() {
        return invincible;
    }
    

    public void applyKnockback(float direction) {
        // Apply a horizontal knockback
        float knockbackStrength = 3.0f * Game.SCALE;
        
        // Check if we can move to the knocked-back position
        float newX = hitbox.x + (direction * knockbackStrength);
        if (CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x = newX;
        }
        
        // Also make the player jump slightly if on ground
        if (!inAir) {
            inAir = true;
            airSpeed = jumpSpeed / 2; // Half of normal jump strength
        }
    }
    
}