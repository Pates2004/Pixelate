package pl.pixelate;

/**
 * Chooses when ClearAllController should stop trusting native accessibility scroll actions.
 * This class deliberately has no Android dependencies so the decision logic can be unit-tested.
 */
final class ScrollProgressTracker {
    static final int STAGNANT_ACTIONS_BEFORE_GESTURE = 2;
    static final int MAX_NATIVE_ACTIONS_WITHOUT_SIGNAL = 20;

    private String signatureBeforeLastAction;
    private boolean awaitingResult;
    private boolean gestureMode;
    private int stagnantActions;
    private int acceptedNativeActions;

    void reset() {
        signatureBeforeLastAction = null;
        awaitingResult = false;
        gestureMode = false;
        stagnantActions = 0;
        acceptedNativeActions = 0;
    }

    void observe(String currentSignature) {
        if (!awaitingResult || gestureMode) {
            return;
        }
        awaitingResult = false;

        if (signatureBeforeLastAction != null && currentSignature != null) {
            if (signatureBeforeLastAction.equals(currentSignature)) {
                stagnantActions++;
                if (stagnantActions >= STAGNANT_ACTIONS_BEFORE_GESTURE) {
                    gestureMode = true;
                }
            } else {
                stagnantActions = 0;
            }
        }
    }

    boolean shouldTryNativeAction() {
        return !gestureMode
                && acceptedNativeActions < MAX_NATIVE_ACTIONS_WITHOUT_SIGNAL;
    }

    void recordAcceptedNativeAction(String currentSignature) {
        acceptedNativeActions++;
        signatureBeforeLastAction = currentSignature;
        awaitingResult = true;
        if (acceptedNativeActions >= MAX_NATIVE_ACTIONS_WITHOUT_SIGNAL) {
            gestureMode = true;
        }
    }

    void useGestureFromNowOn() {
        awaitingResult = false;
        gestureMode = true;
    }
}
