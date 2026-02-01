package asi.voronoi.javafx;

import asi.voronoi.anim.StoryboardRecorder;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handles exporting animation frames to PNG files.
 * Rendering of frames is delegated externally.
 */
public class FrameExporter {

    private final Canvas canvas; // Canvas used for rendering the exported frames

    public FrameExporter(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Exports all frames as PNG images to the specified directory.
     *
     * @param frames   List of frames to export.
     * @param dir      Target directory for exported PNG files.
     * @param prefix   Prefix to add to the generated file names.
     * @param renderer A renderer that draws each frame on the canvas.
     * @throws IOException if an error occurs during file writing.
     */
    public void exportFramesToPng(List<StoryboardRecorder.Frame> frames, File dir, String prefix, FrameRenderer renderer)
        throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("Frame list is empty. No frames to export.");
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create output directory: " + dir.getAbsolutePath());
        }

        for (int i = 0; i < frames.size(); i++) {
            // Render the current frame onto the canvas
            renderer.renderFrame(frames.get(i), i);

            // Capture and save the canvas contents as a PNG image
            WritableImage snapshot = canvas.snapshot(null, null);
            File outFile = new File(dir, String.format("%s_%04d.png", prefix, i + 1));
            ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "png", outFile);
        }
    }

    /**
     * Renderer interface for rendering individual frames to the canvas.
     */
    @FunctionalInterface
    public interface FrameRenderer {
        void renderFrame(StoryboardRecorder.Frame frame, int index);
    }
}