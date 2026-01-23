package asi.voronoi.javafx;

import asi.voronoi.anim.StoryboardRecorder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Manages Timeline-based animation playback for Voronoi diagram frames.
 * Handles play, pause, resume, stop, step forward/back, and frame export coordination.
 */
class Animator {
    private Timeline timeline;
    private final double frameMs = 200;
    private int currentIndex = 0;
    private List<StoryboardRecorder.Frame> frames = List.of();
    private IntConsumer onFrame; // callback with frame index

    /**
     * Sets the list of frames to animate.
     */
    void setFrames(List<StoryboardRecorder.Frame> frames) {
        this.frames = frames != null ? List.copyOf(frames) : List.of();
        this.currentIndex = 0;
    }

    /**
     * Sets a callback to be invoked when a frame is displayed.
     * The callback receives the frame index.
     */
    void setOnFrame(IntConsumer callback) {
        this.onFrame = callback;
    }

    /**
     * Gets the current frame index.
     */
    int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Gets the total number of frames.
     */
    int getFrameCount() {
        return frames.size();
    }

    /**
     * Gets the current frame, or null if no frames.
     */
    StoryboardRecorder.Frame getCurrentFrame() {
        if (frames.isEmpty() || currentIndex < 0 || currentIndex >= frames.size()) {
            return null;
        }
        return frames.get(currentIndex);
    }

    /**
     * Gets all frames.
     */
    List<StoryboardRecorder.Frame> getFrames() {
        return frames;
    }

    /**
     * Starts playing the animation from the beginning.
     */
    void play() {
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
                    onFrame.accept(currentIndex);
                }
            }));
        }
        timeline.setCycleCount(1);
        timeline.playFromStart();
    }

    /**
     * Pauses the animation.
     */
    void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    /**
     * Resumes the animation from where it was paused.
     */
    void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    /**
     * Stops the animation.
     */
    void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Steps forward to the next frame.
     */
    void stepForward() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.min(currentIndex + 1, frames.size() - 1);
        if (onFrame != null) {
            onFrame.accept(currentIndex);
        }
    }

    /**
     * Steps back to the previous frame.
     */
    void stepBack() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = Math.max(currentIndex - 1, 0);
        if (onFrame != null) {
            onFrame.accept(currentIndex);
        }
    }

    /**
     * Sets the playback speed multiplier.
     */
    void setSpeed(double rate) {
        if (timeline != null) {
            timeline.setRate(rate);
        }
    }

    /**
     * Checks if the animation is currently playing.
     */
    boolean isPlaying() {
        return timeline != null && timeline.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }
}
