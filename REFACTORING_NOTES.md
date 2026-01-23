# Refactoring: AnimationPane to Focused Classes

This refactoring splits the monolithic `AnimationPane` class (1387 lines) into five focused, single-responsibility classes.

## New Classes Created

### 1. WorldBounds.java
- **Responsibility**: Immutable data holder for world coordinate bounds
- **Size**: ~40 lines
- **Key methods**: `xmin()`, `ymin()`, `xmax()`, `ymax()`, `width()`, `height()`

### 2. Animator.java
- **Responsibility**: Manages Timeline-based animation playback
- **Size**: ~180 lines
- **Key methods**: `play()`, `pause()`, `resume()`, `stop()`, `stepForward()`, `stepBack()`, `exportPngs()`
- **Features**: Frame-by-frame playback with callbacks, export functionality

### 3. ZoomPanController.java
- **Responsibility**: Encapsulates view window math and coordinate transforms
- **Size**: ~260 lines
- **Key methods**: 
  - Transforms: `sx()`, `sy()`, `screenToWorldX()`, `screenToWorldY()`
  - Operations: `zoomAt()`, `zoomAtCenter()`, `panByScreen()`, `resetView()`, `fitToData()`
  - Queries: `getZoomRatio()`, `isInsideViewRect()`

### 4. MinimapView.java
- **Responsibility**: Interactive minimap overlay showing bird's-eye view
- **Size**: ~350 lines
- **Key methods**: `draw()`, `layoutInPane()`, `setPosition()`, `setEnabled()`
- **Features**: Click/drag navigation, scroll-wheel zoom, configurable position

### 5. AnimationView.java
- **Responsibility**: Main composition Pane that orchestrates all components
- **Size**: ~1000 lines
- **Key methods**: Public API mirroring original AnimationPane interface
- **Features**: 
  - Composes Canvas, MinimapView, Animator, ZoomPanController
  - Preserves all Voronoi diagram rendering
  - Preserves all Binary Tree visualization
  - Wires component interactions via callbacks

## Changes to Main.java

- Replaced `AnimationPane animationView;` with `AnimationView animationView;`
- Updated instantiations: `new AnimationPane()` → `new AnimationView()`
- Updated enum references: 
  - `AnimationPane.Mode` → `AnimationView.Mode`
  - `AnimationPane.MinimapPos` → `MinimapView.MinimapPos`
- All existing method calls preserved (no API changes needed)

## Benefits

1. **Separation of Concerns**: Each class has a single, well-defined responsibility
2. **Testability**: Smaller classes are easier to unit test
3. **Maintainability**: Changes to zoom logic don't affect animation playback, etc.
4. **Reusability**: ZoomPanController and MinimapView could be used in other views
5. **Reduced Complexity**: No single file exceeds 1100 lines

## Preserved Behavior

✅ All Voronoi diagram rendering and animations
✅ Binary Tree visualization and animations  
✅ Zoom/pan with mouse and keyboard
✅ Minimap with interactive navigation
✅ Point capture mode
✅ PNG export functionality
✅ All toolbar controls

## Files Modified

- **Added**: 5 new files (WorldBounds, Animator, ZoomPanController, MinimapView, AnimationView)
- **Modified**: Main.java (minimal changes to use new classes)
- **Preserved**: AnimationPane.java (kept for reference, not used by Main)

## Verification Steps

1. Build: `mvn clean compile` (requires Java 21)
2. Run: `mvn javafx:run`
3. Test Voronoi animation: File → Read from file → Animate Divide & Merge
4. Test Binary Tree: Points → Show Tree
5. Verify controls: Play, Pause, Step, Zoom, Pan, Minimap, Export
6. Test point capture: Points → Add Points

## Notes

- Java 21 required for compilation (pom.xml specifies release 21)
- All drawing logic preserved byte-for-byte to maintain visual fidelity
- Callback-based architecture for component communication
- No breaking changes to public API
