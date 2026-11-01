package net.cavern.utils.ui;

import lombok.Getter;

public class Animation {

    /**
     * startValue: The value the animation starts on.
     * endValue: The value the animation finishes on.
     * duration: How long the animation should take.
     * elapsed: How long the animation has been running.
     */
    private float startValue, endValue, duration, elapsed;

    /**
     * running: true when the animation is active
     * finished: true when the animation has finished
     */
    @Getter
    private boolean running = false, finished = false;

    /**
     * animationType: the type of animation method to use:
     * LINEAR or SMOOTHSTEP
     */
    public AnimationType animationType;

    /**
     * Starts an animation from one value to another.
     *
     * @param animationType the easing method to use
     * @param from          the starting value
     * @param to            the ending value
     * @param duration      how long the animation should take
     */
    public void start(AnimationType animationType, float from, float to, float duration) {
        this.animationType = animationType;
        startValue = from;
        endValue = to;
        this.duration = duration;
        elapsed = 0f;
        running = true;
        finished = false;
    }

    /**
     * Updates the animation by adding delta time to the elapsed time
     * until the animation reaches its desired duration.
     */
    public float update(float delta) {
        if (!running) return getValue();
        elapsed += delta;
        if (elapsed >= duration) {
            elapsed = duration;
            running = false;
            finished = true;
        }
        return getValue();
    }

    /**
     * Calculates how far along the animation is.
     */
    public float getProgress() {
        return Math.min(elapsed / duration, 1f);
    }

    /**
     * Applies the selected easing and returns the interpolated value.
     */
    public float getValue() {
        float time = getProgress();
        time = animationType.apply(time);
        return startValue + (endValue - startValue) * time;
    }

    public interface Easing {
        float apply(float t);
    }

    /**
     * To simplify the code, easing math has been moved to AnimationType,
     * which implements Easing to provide the apply method.
     * This keeps the math in getValue() simple:
     * time = animationType.apply(time).
     */
    public enum AnimationType implements Easing {
        LINEAR(t -> t),
        SMOOTHSTEP(t -> t * t * (3f - 2f * t));

        private final Easing easing;

        AnimationType(Easing easing) {
            this.easing = easing;
        }

        @Override
        public float apply(float t) {
            return easing.apply(t);
        }
    }
}