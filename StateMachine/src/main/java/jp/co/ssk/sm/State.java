package jp.co.ssk.sm;

import android.os.Message;
import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class State implements IState {

    static final boolean HANDLED = true;
    static final boolean NOT_HANDLED = false;

    @Override
    public void enter() {
    }

    @Override
    public boolean processMessage(@NonNull Message msg) {
        return NOT_HANDLED;
    }

    @Override
    public void exit() {
    }

    @Override
    @NonNull
    public String getName() {
        String name = getClass().getName();
        int lastDollar = name.lastIndexOf('$');
        return name.substring(lastDollar + 1);
    }

    @Override
    public String toString() {
        return getName();
    }
}
