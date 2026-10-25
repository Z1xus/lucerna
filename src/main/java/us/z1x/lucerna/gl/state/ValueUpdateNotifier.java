package us.z1x.lucerna.gl.state;

/**
 * A
 */
public interface ValueUpdateNotifier {
    /**
     * Sets up a listener with this notifier. Whenever the underlying value of
     * @param listener
     */
    void setListener(Runnable listener);
}
