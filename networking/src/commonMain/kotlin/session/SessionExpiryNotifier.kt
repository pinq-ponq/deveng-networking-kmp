package networking.session

/**
 * Keeps the sign-out handler alive across networking re-initialisations. Background workers
 * rebuild the networking module in the same process as the UI, so a handler passed with the
 * module's configuration would be dropped on every worker run.
 */
public class SessionExpiryNotifier {

    private var onSessionExpired: (() -> Unit)? = null

    public fun setSessionExpiredHandler(handler: () -> Unit) {
        onSessionExpired = handler
    }

    public fun notifySessionExpired() {
        onSessionExpired?.invoke()
    }
}
