package jp.co.ssk.sm;

import android.os.Message;
import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class State<T extends StateMachine> {

    static final boolean HANDLED = true;
    static final boolean NOT_HANDLED = false;

    public void enter(@NonNull T owner) {
    }

    public boolean processMessage(@NonNull T owner, @NonNull Message msg) {
        return NOT_HANDLED;
    }

    public void exit(@NonNull T owner) {
    }

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
