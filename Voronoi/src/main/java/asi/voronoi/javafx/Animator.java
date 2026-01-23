package asi.voronoi.javafx;

import asi.voronoi.anim.StoryboardRecorder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages animation Timeline for frame-by-frame playback.
 * Provides play, pause, stop, step, and export controls.
 */
public class Animator {
    private Timeline timeline;
    private List<StoryboardRecorder.Frame> frames = List.of();
    private int currentIndex = 0;
    private double frameMs = 200;
    private Consumer<StoryboardRecorder.Frame> onFrame;

    /**
     * Set the list of frames to animate.
     */
    public void setFrames(List<StoryboardRecorder.Frame> frames) {
        this.frames = new ArrayList<>(frames);
        this.currentIndex = 0;
    }

    /**
     * Set callback to be invoked when a frame changes.
     */
    public void setOnFrame(Consumer<StoryboardRecorder.Frame> callback) {
        this.onFrame = callback;
    }

    /**
     * Get frame duration in milliseconds.
     */
    public double getFrameMs() {
        return frameMs;
    }

    /**
     * Set frame duration in milliseconds.
     */
    public void setFrameMs(double frameMs) {
        this.frameMs = frameMs;
    }

    /**
     * Start playing animation from the beginning.
     */
    public void play() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        timeline = new Timeline();
        for (int i = 0; i < frames.size(); i++) {
            final int idx = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frameMs * idx), ev -> {
                currentIndex = idx;
                if (onFrame != null) {
                    onFrame.accept(frames.get(currentIndex));
                }
            }));
        }
        timeline.setCycleCount(1);
        timeline.playFromStart();
    }

    /**
     * Pause the animation.
     */
    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    /**
     * Resume the paused animation.
     */
    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    /**
     * Stop the animation.
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Step forward to the next frame.
     */
    public void stepForward() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.min(currentIndex + 1, frames.size() - 1);
        if (onFrame != null) {
            onFrame.accept(frames.get(currentIndex));
        }
    }

    /**
     * Step backward to the previous frame.
     */
    public void stepBack() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.max(currentIndex - 1, 0);
        if (onFrame != null) {
            onFrame.accept(frames.get(currentIndex));
        }
    }

    /**
     * Set animation speed multiplier.
     */
    public void setSpeed(double rate) {
        if (timeline != null) {
            timeline.setRate(rate);
        }
    }

    /**
     * Get current frame index.
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Get current frame, or null if no frames.
     */
    public StoryboardRecorder.Frame getCurrentFrame() {
        if (frames.isEmpty()) {
            return null;
        }
        return frames.get(currentIndex);
    }

    /**
     * Get all frames.
     */
    public List<StoryboardRecorder.Frame> getFrames() {
        return frames;
    }

    /**
     * Export all frames as PNG files using the provided renderer.
     */
    public void exportPngs(File dir, String prefix, FrameRenderer renderer) throws Exception {
        if (frames.isEmpty()) {
            return;
        }
        if (!dir.exists()) {
            java.nio.file.Files.createDirectories(dir.toPath());
        }
        boolean wasPlaying = timeline != null && timeline.getStatus() == javafx.animation.Animation.Status.RUNNING;
        stop();
        for (int i = 0; i < frames.size(); i++) {
            renderer.renderFrame(frames.get(i), i);
        }
        if (wasPlaying) {
            play();
        }
    }

    /**
     * Functional interface for rendering a frame during export.
     */
    @FunctionalInterface
    public interface FrameRenderer {
        void renderFrame(StoryboardRecorder.Frame frame, int index) throws Exception;
    }
}
