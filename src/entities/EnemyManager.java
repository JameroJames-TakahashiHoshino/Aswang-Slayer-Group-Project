package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gameStates.Playing;
import levels.Level;
import levels.LevelManager;
import main.Game;
import utilz.LoadSave;
import static utilz.Constants.EnemyConstants.*;

public class EnemyManager {

    private Playing playing;
    private BufferedImage[][] sigbinArr;
    private BufferedImage[][] tikbalangArr;
    private BufferedImage[][] duwendeArr;
    
    private ArrayList<Sigbin> sigbins = new ArrayList<>();
    private ArrayList<Tikbalang> tikbalangs = new ArrayList<>(); 
    private ArrayList<Duwende> duwendes = new ArrayList<>(); 
    

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
    }
    
    
    public void loadEnemies(Level level) {
        sigbins = level.getSigbins();
        tikbalangs = level.getTikbalangs();
        duwendes = level.getDuwendes(); 
    }

    public void update(int[][] lvlData, Player player, LevelManager levelManager) {
        boolean isAnyActive = false;
        
        // Update regular enemies
        for (Sigbin s : sigbins) {
            if(s.isActive()) {
                s.update(lvlData, player);
                isAnyActive = true;
            }
        }
        
        // Update boss enemies
        for (Tikbalang t : tikbalangs) {
            if(t.isActive()) {
                t.update(lvlData, player, levelManager );
                isAnyActive = true;
            }
        }
        
        // Update duwende enemies
        for (Duwende d : duwendes) {
            if(d.isActive()) {
                d.update(lvlData, player);
                isAnyActive = true;
            }
        }
        
        if(!isAnyActive)
            playing.setLevelCompleted(true);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawSigbins(g, xLvlOffset);
        drawTikbalangs(g, xLvlOffset);
        drawDuwendes(g, xLvlOffset); 
    }

    private void drawSigbins(Graphics g, int xLvlOffset) {
        for (Sigbin c : sigbins) {
            if(c.isActive()) {
                g.drawImage(sigbinArr[c.getEnemyState()][c.getAniIndex()], 
                           (int) c.getHitbox().x - xLvlOffset - SIGBIN_DRAWOFFSET_X + c.flipX(),
                           (int) c.getHitbox().y - SIGBIN_DRAWOFFSET_Y, 
                           SIGBIN_WIDTH * c.flipW(), 
                           SIGBIN_HEIGHT, null);
                
//                 c.drawHitbox(g, xLvlOffset); // Uncomment for debugging
            }
        }
    }
    
    // Add this method
    private void drawTikbalangs(Graphics g, int xLvlOffset) {
        for (Tikbalang t : tikbalangs) {
            if(t.isActive()) {
                g.drawImage(tikbalangArr[t.getEnemyState()][t.getAniIndex()], 
                           (int) t.getHitbox().x - xLvlOffset - TIKBALANG_DRAWOFFSET_X + t.flipX(),
                           (int) t.getHitbox().y - TIKBALANG_DRAWOFFSET_Y, 
                           TIKBALANG_WIDTH * t.flipW(), 
                           TIKBALANG_HEIGHT, null);
                
//                 t.drawHitbox(g, xLvlOffset); // Uncomment for debugging
            
            }
        }
    }
    
    // Add method to draw duwendes
    private void drawDuwendes(Graphics g, int xLvlOffset) {
        for (Duwende d : duwendes) {
            if(d.isActive() && !d.isInvisible()) { // Skip drawing if duwende is invisible
                g.drawImage(duwendeArr[d.getEnemyState()][d.getAniIndex()], 
                           (int) d.getHitbox().x - xLvlOffset - DUWENDE_DRAWOFFSET_X + d.flipX(),
                           (int) d.getHitbox().y - DUWENDE_DRAWOFFSET_Y, 
                           DUWENDE_WIDTH * d.flipW(), 
                           DUWENDE_HEIGHT, null);
                
//                 d.drawHitbox(g, xLvlOffset); // Uncomment for debugging
            }
        }
    }
    
    private void loadEnemyImgs() {
        // Load Sigbin sprites
        sigbinArr = new BufferedImage[5][30];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SIGBIN_ATLAS);
        for (int j = 0; j < sigbinArr.length; j++)
            for (int i = 0; i < sigbinArr[j].length; i++)
                sigbinArr[j][i] = temp.getSubimage(i * WIDTH_DEFAULT, j * HEIGHT_DEFAULT, WIDTH_DEFAULT, HEIGHT_DEFAULT);
        
        // Load Tikbalang sprites - Add this section
        tikbalangArr = new BufferedImage[6][32]; // Note: 6 states including special attack
        BufferedImage bossTemp = LoadSave.GetSpriteAtlas(LoadSave.TIKBALANG_ATLAS);
        for (int j = 0; j < tikbalangArr.length; j++)
            for (int i = 0; i < tikbalangArr[j].length; i++)
                tikbalangArr[j][i] = bossTemp.getSubimage(i * WIDTH_DEFAULT, j * HEIGHT_DEFAULT, WIDTH_DEFAULT, HEIGHT_DEFAULT);
        
        // Load Duwende sprites
        duwendeArr = new BufferedImage[6][32]; // 6 rows and 32 columns as per your atlas
        BufferedImage duwendeTemp = LoadSave.GetSpriteAtlas(LoadSave.DUWENDE_ATLAS);
        for (int j = 0; j < duwendeArr.length; j++)
            for (int i = 0; i < duwendeArr[j].length; i++)
                duwendeArr[j][i] = duwendeTemp.getSubimage(i * WIDTH_DEFAULT, j * HEIGHT_DEFAULT, WIDTH_DEFAULT, HEIGHT_DEFAULT);
    }
    
    public void checkPlayerHit(Player player) {
        if (player.isInvincible())
            return;
            
        // Check Sigbin enemies
        for (Sigbin s : sigbins) {
            if (s.isActive()) {
                if (s.getHitbox().intersects(player.getHitbox())) {
                    player.changeHealth(-GetEnemyDmg(SIGBIN));
                    float knockbackDirection = player.getHitbox().x < s.getHitbox().x ? -1 : 1;
                    player.applyKnockback(knockbackDirection);
                    return;
                }
            }
        }
        
        // Check Tikbalang enemies - Add this section
        for (Tikbalang t : tikbalangs) {
            if (t.isActive()) {
                if (t.getHitbox().intersects(player.getHitbox())) {
                    player.changeHealth(-GetEnemyDmg(TIKBALANG));
                    float knockbackDirection = player.getHitbox().x < t.getHitbox().x ? -1.5f : 1.5f;
                    player.applyKnockback(knockbackDirection);
                    return;
                }
            }
        }
        
        // Check Duwende enemies - Only check if not invisible
        for (Duwende d : duwendes) {
            if (d.isActive() && !d.isInvisible()) {
                if (d.getHitbox().intersects(player.getHitbox())) {
                    player.changeHealth(-GetEnemyDmg(DUWENDE));
                    float knockbackDirection = player.getHitbox().x < d.getHitbox().x ? -0.7f : 0.7f;
                    player.applyKnockback(knockbackDirection);
                    return;
                }
            }
        }
    }
    
    public void checkEnemyHit(Rectangle2D.Float attackBox, int damage) {
        // Check Sigbin enemies
        for (Sigbin s : sigbins) {
            if (s.isActive() && attackBox.intersects(s.getHitbox())) {
                s.hurt(damage);
                return;
            }
        }
        
        // Check Tikbalang enemies
        for (Tikbalang t : tikbalangs) {
            if (t.isActive() && attackBox.intersects(t.getHitbox())) {
                t.hurt(damage / 2); // Boss takes less damage
                return;
            }
        }
        
        // Check Duwende enemies - Cannot hit if invisible
        for (Duwende d : duwendes) {
            if (d.isActive() && !d.isInvisible() && attackBox.intersects(d.getHitbox())) {
                d.hurt(damage);
                return;
            }
        }
    }
    
    public void resetAllEnemies() {
        for (Sigbin s : sigbins)
            s.resetEnemy();
            
        // Reset Tikbalang enemies 
        for (Tikbalang t : tikbalangs)
            t.resetEnemy();
            
        // Reset Duwende enemies
        for (Duwende d : duwendes)
            d.resetEnemy();
    }
}