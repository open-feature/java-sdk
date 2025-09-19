package dev.openfeature.api.tracking;

import dev.openfeature.api.evaluation.EvaluationContext;

/**
 * Interface for Tracking events.
 */
public interface TrackingProvider {

    /**
     * Feature provider implementations can opt in for to support Tracking by implementing this method.
     *
     * @param eventName The name of the tracking event
     * @param context   Evaluation context used in flag evaluation (Optional)
     * @param details   Data pertinent to a particular tracking event (Optional)
     */
    default void track(String eventName, EvaluationContext context, TrackingEventDetails details) {}
}
