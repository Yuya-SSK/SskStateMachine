package jp.co.ssk.sample_lib;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import jp.co.ssk.utility.SynchronousCallback;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings({"unused"})
public class SampleManagerTest {
    @BeforeClass
    public static void beforeClass() {

    }
    @AfterClass
    public static void afterClass() {

    }
    @Before
    public void before() {
        SampleManager.init(InstrumentationRegistry.getTargetContext());
    }
    @After
    public void after() {
        SampleManager.deinit();
    }
    @Test
    public void getInitState() {
        SampleState sampleState = SampleManager.sharedInstance().getSampleState();
        assertEquals(SampleState.InactiveState, sampleState);
    }
    @Test
    public void activate() {
        final List<SampleState> stateList = new ArrayList<>();
        stateList.add(SampleState.ActiveState);
        stateList.add(SampleState.UnconnectedState);
        stateList.add(SampleState.Unconnected1State);
        SampleManager manager = SampleManager.sharedInstance();
        SynchronousCallback callback = new SynchronousCallback();
        manager.setListener(new SampleManager.Listener() {
            @Override
            public void onStateChanged(@NonNull SampleState sampleState) {
                stateList.remove(sampleState);
                if (stateList.isEmpty()) {
                    callback.unlock();
                }
            }
        });
        manager.activate();
        callback.lock();
        assertEquals(SampleState.UnconnectedState, manager.getSampleState());
    }
    @Test
    public void connect() {
        SampleManager manager = SampleManager.sharedInstance();

        final SynchronousCallback synchronousCallback1 = new SynchronousCallback();
        manager.setListener(new SampleManager.Listener() {
            @Override
            public void onStateChanged(@NonNull SampleState sampleState) {
                if (sampleState == SampleState.Unconnected1State) {
                    synchronousCallback1.unlock();
                }
            }
        });
        manager.activate();
        synchronousCallback1.lock();
        assertEquals(SampleState.Unconnected1State, manager.getSampleState());

        final SynchronousCallback synchronousCallback2 = new SynchronousCallback();
        manager.setListener(new SampleManager.Listener() {
            @Override
            public void onStateChanged(@NonNull SampleState sampleState) {
                if (sampleState == SampleState.Connected3State) {
                    synchronousCallback2.unlock();
                }
            }
        });
        manager.connect();
        synchronousCallback2.lock();
        assertEquals(SampleState.Connected3State, manager.getSampleState());

        final SynchronousCallback synchronousCallback3 = new SynchronousCallback();
        manager.setListener(new SampleManager.Listener() {
            @Override
            public void onStateChanged(@NonNull SampleState sampleState) {
                if (sampleState == SampleState.InactiveState) {
                    synchronousCallback3.unlock();
                }
            }
        });
        manager.deactivate();
        synchronousCallback3.lock();
        assertEquals(SampleState.InactiveState, manager.getSampleState());
    }
}
