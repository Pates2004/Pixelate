package pl.pixelate;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ScrollProgressTrackerTest {
    @Test
    public void keepsNativeActionsWhileCarouselAdvances() {
        ScrollProgressTracker tracker = new ScrollProgressTracker();
        tracker.reset();

        tracker.recordAcceptedNativeAction("Calendar@100");
        tracker.observe("Camera@100");
        tracker.recordAcceptedNativeAction("Camera@100");
        tracker.observe("Maps@100");

        assertTrue(tracker.shouldTryNativeAction());
    }

    @Test
    public void switchesToGestureAfterTwoAcceptedActionsWithoutProgress() {
        ScrollProgressTracker tracker = new ScrollProgressTracker();
        tracker.reset();

        tracker.recordAcceptedNativeAction("Pixelate@100");
        tracker.observe("Pixelate@100");
        assertTrue(tracker.shouldTryNativeAction());

        tracker.recordAcceptedNativeAction("Pixelate@100");
        tracker.observe("Pixelate@100");
        assertFalse(tracker.shouldTryNativeAction());
    }

    @Test
    public void aLaterProgressSignalResetsTheStagnationCount() {
        ScrollProgressTracker tracker = new ScrollProgressTracker();
        tracker.reset();

        tracker.recordAcceptedNativeAction("Pixelate@100");
        tracker.observe("Pixelate@100");
        tracker.recordAcceptedNativeAction("Pixelate@100");
        tracker.observe("Calendar@100");
        tracker.recordAcceptedNativeAction("Calendar@100");
        tracker.observe("Calendar@100");

        assertTrue(tracker.shouldTryNativeAction());
    }

    @Test
    public void retainsHardFallbackWhenLauncherExposesNoProgressSignal() {
        ScrollProgressTracker tracker = new ScrollProgressTracker();
        tracker.reset();

        for (int i = 0; i < ScrollProgressTracker.MAX_NATIVE_ACTIONS_WITHOUT_SIGNAL; i++) {
            assertTrue(tracker.shouldTryNativeAction());
            tracker.recordAcceptedNativeAction(null);
            tracker.observe(null);
        }

        assertFalse(tracker.shouldTryNativeAction());
    }

    @Test
    public void rejectedNativeActionLatchesGestureMode() {
        ScrollProgressTracker tracker = new ScrollProgressTracker();
        tracker.reset();

        tracker.useGestureFromNowOn();

        assertFalse(tracker.shouldTryNativeAction());
    }
}
