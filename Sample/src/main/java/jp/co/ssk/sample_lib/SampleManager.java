package jp.co.ssk.sample_lib;

import android.content.Context;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import jp.co.ssk.utility.Handler;
import jp.co.ssk.utility.SynchronousCallback;
import jp.co.ssk.utility.Cast;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class SampleManager {

    public interface Listener extends SampleStateMachine.Listener {}

    @Nullable
    private static WeakReference<SampleManager> sInstance;
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final SampleStateMachine mSampleStateMachine;
    @Nullable
    private Listener mListener;

    private SampleManager(@NonNull final Context context) {
        final HandlerThread thread = new HandlerThread(getClass().getSimpleName() + "-Thread");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        SampleStateMachine.Listener listener = new SampleStateMachine.Listener() {
            @Override
            public void onStateChanged(@NonNull SampleState sampleState) {
                if (mListener == null) {
                    return;
                }
                mListener.onStateChanged(sampleState);
            }
        };
        mSampleStateMachine = new SampleStateMachine(context, thread.getLooper(), listener);
    }

    @NonNull
    public static SampleManager init(@NonNull Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        sInstance = new WeakReference<>(new SampleManager(context));
        return sInstance.get();
    }

    public static void deinit() {
        if (sInstance != null) {
            sInstance.clear();
            sInstance = null;
        }
    }

    @NonNull
    public static SampleManager sharedInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Don't initialized.");
        }
        return sInstance.get();
    }

    public void setListener(@Nullable final Listener listener) {
        mHandler.post(() -> mListener = listener);
    }

    @NonNull
    public SampleState getSampleState() {
        final SampleState ret;
        SynchronousCallback callback = new SynchronousCallback();
        mHandler.post(() -> {
            callback.setResult(mSampleStateMachine.getSampleState());
            callback.unlock();
        });
        callback.lock();
        ret = Cast.auto(callback.getResult());
        return ret;
    }

    public void activate() {
        mHandler.post(mSampleStateMachine::activate);
    }

    public void deactivate() {
        mHandler.post(mSampleStateMachine::deactivate);
    }

    public void connect() {
        mHandler.post(mSampleStateMachine::connect);
    }

    public void disconnect() {
        mHandler.post(mSampleStateMachine::disconnect);
    }
}
