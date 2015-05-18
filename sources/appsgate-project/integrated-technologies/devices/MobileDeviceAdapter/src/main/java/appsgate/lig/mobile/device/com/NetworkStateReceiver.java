package appsgate.lig.mobile.device.com;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommaso Resti ->
 * http://stackoverflow.com/questions/6169059/android-event-for-internet-connectivity-state-change
 */
public class NetworkStateReceiver {

    protected List<NetworkStateReceiverListener> listeners;
    protected Boolean connected;

    public NetworkStateReceiver() {
        listeners = new ArrayList<>();
        connected = null;
    }

    public void onReceive() {

        notifyStateToAll();

    }

    private void notifyStateToAll() {
        for (NetworkStateReceiverListener listener : listeners) {
            notifyState(listener);
        }
    }

    private void notifyState(NetworkStateReceiverListener listener) {
        if (connected == null || listener == null) {
            return;
        }

        if (connected == true) {
            listener.networkAvailable();
        } else {
            listener.networkUnavailable();
        }
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    public interface NetworkStateReceiverListener {

        public void networkAvailable();

        public void networkUnavailable();
    }
}
