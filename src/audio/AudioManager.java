package audio;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioPlayer currentMusic;
    private static Map<String, AudioPlayer> soundEffects = new HashMap<>();
    private static boolean soundMuted = false;
    private static float soundVolume = 1.0f;
    private static boolean musicMuted = false;
    private static float musicVolume = 1.0f;
    
    // Play background music
    public static void playMusic(String musicPath) {
        if (musicMuted)
            return;
            
        if (currentMusic != null) {
            currentMusic.stop();
        }
        
        try {
            currentMusic = new AudioPlayer(musicPath);
            currentMusic.setVolume(musicVolume);
            currentMusic.loop();
        } catch (Exception e) {
            System.err.println("Error playing music: " + musicPath);
            e.printStackTrace();
        }
    }
    
    // Stop background music
    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }
    
    // Play a sound effect once
    public static void playSFX(String sfxPath) {
        if (soundMuted)
            return;
            
        try {
            // Try to reuse existing sound player
            AudioPlayer sfx = soundEffects.get(sfxPath);
            
            if (sfx == null) {
                sfx = new AudioPlayer(sfxPath);
                soundEffects.put(sfxPath, sfx);
            }
            
            sfx.setVolume(soundVolume);
            sfx.play();
        } catch (Exception e) {
            System.err.println("Error playing sound effect: " + sfxPath);
            e.printStackTrace();
        }
    }
    
    // Set music volume
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }
    
    // Set sound effects volume
    public static void setSoundVolume(float volume) {
        soundVolume = volume;
    }
    
    // Toggle music on/off
    public static void toggleMusicMute() {
        musicMuted = !musicMuted;
        if (musicMuted) {
            stopMusic();
        } else if (currentMusic != null) {
            currentMusic.loop();
        }
    }
    
    // Toggle sound effects on/off
    public static void toggleSoundMute() {
        soundMuted = !soundMuted;
    }
    
    // Clean up resources
    public static void cleanUp() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        
        for (AudioPlayer sfx : soundEffects.values()) {
            sfx.stop();
        }
    }
    
    public static boolean isMusicMuted() {
        return musicMuted;
    }

    public static boolean isSoundMuted() {
        return soundMuted;
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    public static float getSoundVolume() {
        return soundVolume;
    }
    
}