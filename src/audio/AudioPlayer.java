package audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AudioPlayer {
    private Clip clip;

    public AudioPlayer(String soundFilePath) {
        try {
            File file = new File(soundFilePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.setFramePosition(0); // Start from the beginning
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuously
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
    
    public void setVolume(float volume) { // Volume range: 0.0 to 1.0
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10) * 20);
                gainControl.setValue(dB);
            } catch (IllegalArgumentException e) {
                // Some clips may not support volume control
                System.out.println("Volume control not supported for this clip");
            }
        }
    }
}