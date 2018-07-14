package jp.co.ssk.sample_lib;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import jp.co.ssk.utility.SynchronousCallback;
import jp.co.ssk.utility.Cast;
import jp.co.ssk.sm.State;
import jp.co.ssk.sm.StateMachine;

@SuppressWarnings({"unused", "WeakerAccess"})
final class SampleStateMachine extends StateMachine {

    public interface Listener {
        default void onStateChanged(@NonNull SampleState sampleState) {}
    }

    private enum Event {
        Activate, Deactivate, Connect, Disconnect,
        Conn1Comp, Conn2Comp, Conn3Comp,
    }

    private final State mInactiveState = new InactiveState();
    private final State mActiveState = new ActiveState();
    private final State mUnconnectedState = new UnconnectedState();
    private final State mUnconnected1State = new Unconnected1State();
    private final State mUnconnected2State = new Unconnected2State();
    private final State mUnconnected3State = new Unconnected3State();
    private final State mConnectingState = new ConnectingState();
    private final State mConnecting1State = new Connecting1State();
    private final State mConnecting2State = new Connecting2State();
    private final State mConnecting3State = new Connecting3State();
    private final State mConnectedState = new ConnectedState();
    private final State mConnected1State = new Connected1State();
    private final State mConnected2State = new Connected2State();
    private final State mConnected3State = new Connected3State();
    private final State mDisconnectingState = new DisconnectingState();
    private final State mDisconnecting1State = new Disconnecting1State();
    private final State mDisconnecting2State = new Disconnecting2State();
    private final State mDisconnecting3State = new Disconnecting3State();

    @NonNull
    private final Context mContext;
    @NonNull
    private final Listener mListener;
    @NonNull
    private SampleState mSampleState;

    public SampleStateMachine(@NonNull Context context, @Nullable Looper looper, @NonNull Listener listener) {
        super(looper);
        mContext = context;
        mListener = listener;

        State defaultState = new DefaultState();
        addState(defaultState);
        addState(mInactiveState, defaultState);
        addState(mActiveState, defaultState);
        addState(mUnconnectedState, mActiveState);
        addState(mUnconnected1State, mUnconnectedState);
        addState(mUnconnected2State, mUnconnectedState);
        addState(mUnconnected3State, mUnconnectedState);
        addState(mConnectingState, mActiveState);
        addState(mConnecting1State, mConnectingState);
        addState(mConnecting2State, mConnectingState);
        addState(mConnecting3State, mConnectingState);
        addState(mConnectedState, mActiveState);
        addState(mConnected1State, mConnectedState);
        addState(mConnected2State, mConnected1State);
        addState(mConnected3State, mConnected2State);
        addState(mDisconnectingState, mActiveState);
        addState(mDisconnecting1State, mDisconnectingState);
        addState(mDisconnecting2State, mDisconnectingState);
        addState(mDisconnecting3State, mDisconnectingState);

        mSampleState = SampleState.UnknownState;
        setDbg(true);
        setInitialState(mInactiveState);
        start();
    }

    public void activate() {
        Log.i(getName(), "[In] activate");
        sendMessageSyncIf(Event.Activate.ordinal());
        Log.i(getName(), "[Out] activate");
    }

    public void deactivate() {
        Log.i(getName(), "[In] deactivate");
        sendMessageSyncIf(Event.Deactivate.ordinal());
        Log.i(getName(), "[Out] deactivate");
    }

    public void connect() {
        Log.i(getName(), "[In] connect");
        sendMessageSyncIf(Event.Connect.ordinal());
        Log.i(getName(), "[Out] connect");
    }

    public void disconnect() {
        Log.i(getName(), "[In] disconnect");
        sendMessageSyncIf(Event.Disconnect.ordinal());
        Log.i(getName(), "[Out] disconnect");
    }

    @NonNull
    public SampleState getSampleState() {
        final SampleState ret;
        if (getHandler().isCurrentThread()) {
            ret = mSampleState;
        } else {
            final SynchronousCallback callback = new SynchronousCallback();
            getHandler().post(() -> {
                callback.setResult(mSampleState);
                callback.unlock();
            });
            callback.lock();
            ret = Cast.auto(callback.getResult());
        }
        return ret;
    }

    @Override
    protected void outputMessageLog(@NonNull String currentStateName, @NonNull Message msg) {
        Log.i(getName(), "processMessage: " + currentStateName + " " + Event.values()[msg.what]);
    }

    private void _setSampleState(@NonNull SampleState sampleState) {
        mSampleState = sampleState;
        mListener.onStateChanged(sampleState);
    }

    private static class DefaultState extends State<SampleStateMachine> {
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            return StateMachine.HANDLED;
        }
    }

    private static class InactiveState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.InactiveState);
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Activate:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mUnconnected1State);
                    break;
            }
            return ret;
        }
    }

    private static class ActiveState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.ActiveState);
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Deactivate:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mInactiveState);
                    break;
            }
            return ret;
        }
    }

    private static class UnconnectedState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.UnconnectedState);
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Connect:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mConnecting1State);
                    break;
            }
            return ret;
        }
    }

    private static class Unconnected1State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Unconnected1State);
        }
    }

    private static class Unconnected2State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Unconnected2State);
        }
    }

    private static class Unconnected3State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Unconnected3State);
        }
    }

    private static class ConnectingState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.ConnectingState);
        }
    }

    private static class Connecting1State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connecting1State);
            owner.sendMessageDelayed(Event.Conn1Comp.ordinal(), 1000);
        }
        @Override
        public void exit(@NonNull SampleStateMachine owner) {
            owner.removeMessages(Event.Conn1Comp.ordinal());
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Conn1Comp:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mConnecting2State);
                    break;
            }
            return ret;
        }
    }

    private static class Connecting2State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connecting2State);
            owner.sendMessageDelayed(Event.Conn2Comp.ordinal(), 1000);
        }
        @Override
        public void exit(@NonNull SampleStateMachine owner) {
            owner.removeMessages(Event.Conn2Comp.ordinal());
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Conn2Comp:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mConnecting3State);
                    break;
            }
            return ret;
        }
    }

    private static class Connecting3State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connecting3State);
            owner.sendMessageDelayed(Event.Conn3Comp.ordinal(), 1000);
        }
        @Override
        public void exit(@NonNull SampleStateMachine owner) {
            owner.removeMessages(Event.Conn3Comp.ordinal());
        }
        @Override
        public boolean processMessage(@NonNull SampleStateMachine owner, @NonNull Message msg) {
            boolean ret = StateMachine.NOT_HANDLED;
            switch (Event.values()[msg.what]) {
                case Conn3Comp:
                    ret = StateMachine.HANDLED;
                    owner.transitionTo(owner.mConnected3State);
                    break;
            }
            return ret;
        }
    }

    private static class ConnectedState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.ConnectedState);
        }
    }

    private static class Connected1State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connected1State);
        }
    }

    private static class Connected2State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connected2State);
        }
    }

    private static class Connected3State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Connected3State);
        }
    }

    private static class DisconnectingState extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.DisconnectingState);
        }
    }

    private static class Disconnecting1State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Disconnecting1State);
        }
    }

    private static class Disconnecting2State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Disconnecting2State);
        }
    }

    private static class Disconnecting3State extends State<SampleStateMachine> {
        @Override
        public void enter(@NonNull SampleStateMachine owner) {
            owner._setSampleState(SampleState.Disconnecting3State);
        }
    }
}
