package ui;

import static utilz.Constants.UI.PausedButtons.SOUND_SIZE;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import audio.AudioManager; // Add this import
import gameStates.Gamestate;
import main.Game;

public class AudioOptions {

    private VolumeButton volumeButton;
    private SoundButton musicButton, sfxButton;
    private float volumeValue = 1.0f; // Default volume
    
    public AudioOptions() {
        createSoundButton();
        createVolumeButton();
        
        // Initialize music and SFX buttons based on AudioManager states
        musicButton.setMuted(AudioManager.isMusicMuted());
        sfxButton.setMuted(AudioManager.isSoundMuted());
        
        // Initialize volume slider position
        float currentVolume = AudioManager.getMusicVolume(); // Assume music and SFX volumes are the same
        int sliderPos = (int)(volumeButton.getMinX() + (currentVolume * (volumeButton.getMaxX() - volumeButton.getMinX())));
        volumeButton.changeX(sliderPos);
    }
    
    private void createVolumeButton() {  // positions of VolButtons
        int vX = (int)(310 * Game.SCALE);
        int vY = (int)(240 * Game.SCALE);
        volumeButton = new VolumeButton(vX, vY, SLIDER_WIDTH, VOLUME_HEIGHT);
    }
    
    private void createSoundButton() { // positions of soundButtons
        int soundX = (int)(490 * Game.SCALE);
        int musicY = (int)(80 * Game.SCALE);
        int sfxY = (int)(130 * Game.SCALE);
        musicButton = new SoundButton(soundX, musicY, SOUND_SIZE, SOUND_SIZE);
        sfxButton = new SoundButton(soundX, sfxY, SOUND_SIZE, SOUND_SIZE);
    }
    
    public void update() {
        musicButton.update();
        sfxButton.update();
        volumeButton.update();
        
        // Update volume based on slider position
        updateVolumeFromSlider();
    }
    
    // New method to calculate volume from slider position
    private void updateVolumeFromSlider() {
        // Calculate volume value (0.0 to 1.0) based on slider position
        float min = volumeButton.getMinX();
        float max = volumeButton.getMaxX();
        float current = volumeButton.getButtonX();
        
        volumeValue = (current - min) / (max - min);
        
        // Update both music and SFX volume with the same value
        AudioManager.setMusicVolume(volumeValue);
        AudioManager.setSoundVolume(volumeValue);
    }
    
    public void draw(Graphics g) {
        //SOUND BUTTONS
        musicButton.draw(g);
        sfxButton.draw(g);
        
        //VOLUME BUTTONS
        volumeButton.draw(g);
    }
    
    public void mouseDragged(MouseEvent e) {
        if (volumeButton.isMousePressed()) {
            volumeButton.changeX(e.getX());
            // Update volume in real-time while dragging
            updateVolumeFromSlider();
        }
    }
    
    public void mousedPressed(MouseEvent e) {
        if (isIn(e, musicButton)) {
            musicButton.setMousePressed(true);
        } else if (isIn(e, sfxButton)) {
            sfxButton.setMousePressed(true);
        } else if (isIn(e, volumeButton)) {
            volumeButton.setMousePressed(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, musicButton)) {
            if (musicButton.isMousePressed()) {
                musicButton.setMuted(!musicButton.isMuted());
                // Toggle music mute in AudioManager
                AudioManager.toggleMusicMute();
            }
        } else if (isIn(e, sfxButton)) {
            if (sfxButton.isMousePressed()) {
                sfxButton.setMuted(!sfxButton.isMuted());
                // Toggle SFX mute in AudioManager
                AudioManager.toggleSoundMute();
            }
        }
        // Reset all button states
        musicButton.resetBools();
        sfxButton.resetBools();
        volumeButton.resetBools();
    }

    public void mouseMoved(MouseEvent e) {
        musicButton.setMouseOver(false);
        sfxButton.setMouseOver(false);
        volumeButton.setMouseOver(false);
        
        if (isIn(e, musicButton))
            musicButton.setMouseOver(true);
        else if (isIn(e, sfxButton))
            sfxButton.setMouseOver(true); 
        else if (isIn(e, volumeButton))
            volumeButton.setMouseOver(true); 
    }
    
    private boolean isIn(MouseEvent e, PausedButton b) {
        boolean result = b.getBounds().contains(e.getX(), e.getY());
        return result;
    }
    
    // Method to get current volume value
    public float getVolumeValue() {
        return volumeValue;
    }
}