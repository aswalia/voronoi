package asi.voronoi.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

import asi.voronoi.anim.StoryboardRecorder;

/**
 * Handles animation playback and stepping. Rendering of each frame is delegated
 * to a callback (onFrame).
 */
public class Animator {

    private List<StoryboardRecorder.Frame> frames = List.of(); // List of animation frames
    private Timeline timeline;                                // Timeline for playback
    private double frameMs = 200;                             // Milliseconds per frame
    private int currentIndex = 0;                             // Current frame index
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
        System.out.println("[Animator] play() frames=" + frames.size() + " frameMs=" + frameMs);
        if (frames == null || frames.isEmpty()) {
            return;
        }

        stop(); // Stop any existing timeline for a fresh play

        currentIndex = 0; // Start from the first frame
        onFrame.accept(frames.get(currentIndex)); // Render the first frame immediately

        timeline = new Timeline(new KeyFrame(Duration.millis(frameMs), ev -> {
            currentIndex++;
            if (currentIndex < frames.size()) {
                onFrame.accept(frames.get(currentIndex)); // Trigger rendering for the next frame
            } else {
                stop(); // Stop playback once all frames are done
                if (onAnimationEnd != null) {
                    onAnimationEnd.run(); // Invoke end-of-animation callback
                }
            }
        }));

        timeline.setCycleCount(frames.size() - currentIndex); // Set remaining frames to play
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
     * Returns the list of animation frames.
     *
     * @return The list of animation frames.
     */
    public List<StoryboardRecorder.Frame> getFrames() {
        return frames;
    }

    /**
     * Returns the index of the current frame being played.
     *
     * @return The current frame index.
     */
    public int getCurrentFrameIndex() {
        return currentIndex;
    }
}
