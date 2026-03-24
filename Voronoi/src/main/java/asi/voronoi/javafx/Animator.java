package asi.voronoi.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import asi.voronoi.anim.StoryboardRecorder;

/**
 * Handles animation playback and stepping. Rendering of each frame is delegated
 * to a callback (onFrame).
 */
public class Animator {

    private List<StoryboardRecorder.Frame> frames = List.of();
    private Timeline timeline;
    private double frameMs = 200;
    private int currentIndex = 0;
    private Consumer<StoryboardRecorder.Frame> onFrame = f -> {
    };
    private Runnable onAnimationEnd = () -> {
    };

    public void setFrameMs(double ms) {
        this.frameMs = ms;
    }

    public void setFrames(List<StoryboardRecorder.Frame> frames) {
        this.frames = frames == null ? List.of() : frames;
        currentIndex = 0;
        System.out.println("[Animator] setFrames count=" + this.frames.size());        
        if (!this.frames.isEmpty()) {
            onFrame.accept(this.frames.get(currentIndex));
        }
    }

    public void setOnFrame(Consumer<StoryboardRecorder.Frame> cb) {
        this.onFrame = cb != null ? cb : f -> {
        };
    }

    public void setOnAnimationEnd(Runnable r) {
        this.onAnimationEnd = r != null ? r : () -> {
        };
    }

    public void play() {
        if (frames == null || frames.isEmpty()) return;
        stop();

        currentIndex = 0;
        // show first frame immediately
        onFrame.accept(frames.get(currentIndex));

        if (frames.size() <= 1) {
            // single frame, consider animation finished
            if (onAnimationEnd != null) onAnimationEnd.run();
            return;
        }

        // schedule remaining frames at interval frameMs
        timeline = new Timeline(new KeyFrame(Duration.millis(frameMs), ev -> {
            currentIndex++;
            if (currentIndex < frames.size()) {
                onFrame.accept(frames.get(currentIndex));
            }
        }));
        timeline.setCycleCount(Math.max(0, frames.size() - 1));
        timeline.setOnFinished(ev -> onAnimationEnd.run());
        timeline.playFromStart();
        
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public void stepForward() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.min(currentIndex + 1, frames.size() - 1);
        onFrame.accept(frames.get(currentIndex));
    }

    public void stepBack() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.max(currentIndex - 1, 0);
        onFrame.accept(frames.get(currentIndex));
    }

    /**
     * Export frames by calling the provided renderer for each frame and letting
     * it snapshot. Implementation of snapshot/export is delegated to the caller
     * (e.g. AnimationView).
     */
    public interface FrameRenderer {

        void renderFrame(StoryboardRecorder.Frame frame, int index) throws Exception;
    }

    public void exportPngs(File dir, String prefix, FrameRenderer renderer) throws Exception {
        if (frames.isEmpty()) {
            return;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean wasPlaying = (timeline != null);
        stop();

        for (int i = 0; i < frames.size(); i++) {
            renderer.renderFrame(frames.get(i), i);
        }

        if (wasPlaying) {
            play();
        }
    }
}
