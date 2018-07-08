package jp.co.ssk.sm;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.co.ssk.utility.Handler;
import jp.co.ssk.utility.SynchronousCallback;

@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public abstract class StateMachine {

    protected static final boolean HANDLED = State.HANDLED;
    protected static final boolean NOT_HANDLED = State.NOT_HANDLED;

    @NonNull
    private final Handler mHandler;
    @NonNull
    private final HashMap<State, StateInfo> mStateInfoMap = new HashMap<>();
    @NonNull
    private final Deque<StateInfo> mStateStack = new ArrayDeque<>();
    @NonNull
    private final LinkedList<Message> mDeferredMessages = new LinkedList<>();
    @Nullable
    private State mInitialState;
    @Nullable
    private State mDestState;
    @Nullable
    private Message mCurrentMessage;
    @NonNull
    private AtomicBoolean mDbg = new AtomicBoolean(false);

    protected StateMachine() {
        this(null);
    }

    protected StateMachine(@Nullable Looper looper) {
        if (null == looper) {
            HandlerThread thread = new HandlerThread(getName());
            thread.start();
            looper = thread.getLooper();
        }
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                _handleMessage(msg);
            }
        };
    }

    @NonNull
    protected Handler getHandler() {
        return mHandler;
    }

    protected void addState(@NonNull State state) {
        addState(state, null);
    }

    protected void addState(@NonNull final State state, @Nullable final State parent) {
        if (mHandler.isCurrentThread()) {
            _addState(state, parent);
        } else {
            mHandler.post(() -> _addState(state, parent));
        }
    }

    protected void setInitialState(@NonNull final State state) {
        if (mHandler.isCurrentThread()) {
            mInitialState = state;
        } else {
            mHandler.post(() -> mInitialState = state);
        }
    }

    protected void start() {
        if (mHandler.isCurrentThread()) {
            _start();
        } else {
            mHandler.post(this::_start);
        }
    }

    protected void transitionTo(@NonNull final State state) {
        if (mHandler.isCurrentThread()) {
            mDestState = state;
        } else {
            mHandler.post(() -> mDestState = state);
        }
    }

    @Nullable
    protected final Message getCurrentMessage() {
        final Message ret;
        if (mHandler.isCurrentThread()) {
            ret = mCurrentMessage;
        } else {
            final SynchronousCallback<Message> callback = new SynchronousCallback<>();
            mHandler.post(() -> {
                callback.setResult(mCurrentMessage);
                callback.unlock();
            });
            callback.lock();
            ret = callback.getResult();
        }
        return ret;
    }

    @NonNull
    protected State getCurrentState() {
        final State ret;
        if (mHandler.isCurrentThread()) {
            ret = mStateStack.peekFirst().state;
        } else {
            final SynchronousCallback<State> callback = new SynchronousCallback<>();
            mHandler.post(() -> {
                callback.setResult(mStateStack.peekFirst().state);
                callback.unlock();
            });
            callback.lock();
            if (null == callback.getResult()) {
                throw new UnknownError("An unlikely error.");
            }
            ret = callback.getResult();
        }
        return ret;
    }

    protected boolean isMatch(@NonNull State state) {
        final boolean ret;
        if (mHandler.isCurrentThread()) {
            ret = _isMatch(state);
        } else {
            final SynchronousCallback<Boolean> callback = new SynchronousCallback<>();
            mHandler.post(() -> {
                callback.setResult(_isMatch(state));
                callback.unlock();
            });
            callback.lock();
            if (null == callback.getResult()) {
                throw new UnknownError("An unlikely error.");
            }
            ret = callback.getResult();
        }
        return ret;
    }

    protected void sendMessage(int what) {
        mHandler.sendMessage(what);
    }

    protected void sendMessage(int what, int arg1) {
        mHandler.sendMessage(what, arg1);
    }

    protected void sendMessage(int what, int arg1, int arg2) {
        mHandler.sendMessage(what, arg1, arg2);
    }

    protected void sendMessage(int what, int arg1, int arg2, @Nullable Object obj) {
        mHandler.sendMessage(what, arg1, arg2, obj);
    }

    protected void sendMessage(int what, @Nullable Object obj) {
        mHandler.sendMessage(what, obj);
    }

    protected void sendMessageSyncIf(int what) {
        mHandler.sendMessageSyncIf(what);
    }

    protected void sendMessageSyncIf(int what, int arg1) {
        mHandler.sendMessageSyncIf(what, arg1);
    }

    protected void sendMessageSyncIf(int what, int arg1, int arg2) {
        mHandler.sendMessageSyncIf(what, arg1, arg2);
    }

    protected void sendMessageSyncIf(int what, int arg1, int arg2, @Nullable Object obj) {
        mHandler.sendMessageSyncIf(what, arg1, arg2, obj);
    }

    protected void sendMessageSyncIf(int what, @Nullable Object obj) {
        mHandler.sendMessageSyncIf(what, obj);
    }

    protected void sendMessageDelayed(int what, long delayMillis) {
        mHandler.sendMessageDelayed(what, delayMillis);
    }

    protected void sendMessageDelayed(int what, int arg1, long delayMillis) {
        mHandler.sendMessageDelayed(what, arg1, delayMillis);
    }

    protected void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
        mHandler.sendMessageDelayed(what, arg1, arg2, delayMillis);
    }

    protected void sendMessageDelayed(int what, int arg1, int arg2, @Nullable Object obj, long delayMillis) {
        mHandler.sendMessageDelayed(what, arg1, arg2, obj, delayMillis);
    }

    protected void sendMessageDelayed(int what, @Nullable Object obj, long delayMillis) {
        mHandler.sendMessageDelayed(what, obj, delayMillis);
    }

    protected boolean hasMessages(int what) {
        return mHandler.hasMessages(what);
    }

    protected void removeMessages(int what) {
        mHandler.removeMessages(what);
    }

    protected final void deferMessage(@NonNull final Message msg) {
        if (mHandler.isCurrentThread()) {
            _deferMessage(msg);
        } else {
            mHandler.post(() -> _deferMessage(msg));
        }
    }

    protected final void removeDeferredMessages(final int what) {
        if (mHandler.isCurrentThread()) {
            _removeDeferredMessages(what);
        } else {
            mHandler.post(() -> _removeDeferredMessages(what));
        }
    }

    protected void outputEnterLog(@NonNull String currentStateName) {
        log("invokeEnterMethods: " + currentStateName);
    }

    protected void outputExitLog(@NonNull String currentStateName) {
        log("invokeExitMethods: " + currentStateName);
    }

    protected void outputMessageLog(@NonNull String currentStateName, @NonNull Message msg) {
        log("processMessage: " + currentStateName + String.format(Locale.US, " what=0x%08x", msg.what));
    }

    protected void setDbg(boolean dbg) {
        mDbg.set(dbg);
    }

    @NonNull
    private StateInfo _addState(@NonNull State state, @Nullable State parent) {
        if (mStateInfoMap.containsKey(state)) {
            throw new RuntimeException("State already added.");
        }
        StateInfo parentStateInfo = null;
        if (parent != null) {
            parentStateInfo = mStateInfoMap.get(parent);
            if (parentStateInfo == null) {
                parentStateInfo = _addState(parent, null);
            }
        }
        return mStateInfoMap.put(state, new StateInfo(state, parentStateInfo));
    }

    private void _start() {
        if (mInitialState == null) {
            throw new RuntimeException("Unset initial state.");
        }
        _performTransitions(mInitialState);
    }

    @SuppressWarnings("unchecked")
    private void _performTransitions(@NonNull State destState) {
        StateInfo tempStateInfo = mStateInfoMap.get(destState);
        State foundRootState = null;
        Deque<StateInfo> destStateDeque = new ArrayDeque<>();
        while (tempStateInfo != null) {
            if (tempStateInfo.active) {
                foundRootState = tempStateInfo.state;
                break;
            }
            destStateDeque.offerFirst(tempStateInfo);
            tempStateInfo = tempStateInfo.parentStateInfo;
        }
        while (null != (tempStateInfo = mStateStack.peekFirst())) {
            if (foundRootState != null && foundRootState == tempStateInfo.state) {
                break;
            }
            outputExitLog(tempStateInfo.state.getName());
            tempStateInfo.state.exit(this);
            tempStateInfo.active = false;
            mStateStack.pollFirst();
        }
        for (StateInfo stateInfo : destStateDeque) {
            outputEnterLog(stateInfo.state.getName());
            stateInfo.state.enter(this);
            stateInfo.active = true;
            mStateStack.offerFirst(stateInfo);
        }
        _moveDeferredMessageAtFrontOfQueue();
    }

    @SuppressWarnings("unchecked")
    private void _processMessage(@NonNull Message msg) {
        for (StateInfo stateInfo : mStateStack) {
            outputMessageLog(stateInfo.state.getName(), msg);
            if (stateInfo.state.processMessage(this, msg)) {
                break;
            }
        }
    }

    private void _handleMessage(@NonNull Message msg) {
        mCurrentMessage = msg;
        _processMessage(msg);
        if (mDestState != null) {
            _performTransitions(mDestState);
            mDestState = null;
        }
    }

    private void _moveDeferredMessageAtFrontOfQueue() {
        for (Message message : mDeferredMessages) {
            mHandler.sendMessageAtFrontOfQueue(message);
        }
        mDeferredMessages.clear();
    }

    private void _deferMessage(@NonNull Message msg) {
        Message newMsg = mHandler.obtainMessage();
        newMsg.copyFrom(msg);
        mDeferredMessages.add(newMsg);
    }

    private void _removeDeferredMessages(int what) {
        mDeferredMessages.removeIf(msg -> msg.what == what);
    }

    private boolean _isMatch(@NonNull State state) {
        boolean ret = false;
        for (StateInfo currentStateInfo : mStateStack) {
            if (currentStateInfo.state == state) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @NonNull
    protected String getName() {
        return getClass().getSimpleName();
    }

    private void log(@NonNull String log) {
        if (mDbg.get()) Log.i(getName(), log);
    }

    private static class StateInfo {
        @NonNull
        private final State state;
        @Nullable
        private final StateInfo parentStateInfo;
        private boolean active;

        public StateInfo(@NonNull State state, @Nullable StateInfo parentStateInfo) {
            this.state = state;
            this.parentStateInfo = parentStateInfo;
            this.active = false;
        }

        @Override
        public String toString() {
            String str = "{state=" + state;
            if (parentStateInfo != null) {
                str += ", parent=" + parentStateInfo.state;
            }
            str = str + ", active=" + active;
            str += '}';
            return str;
        }
    }
}
