package jp.co.ssk.sm;

import android.os.Message;
import android.support.annotation.NonNull;

@SuppressWarnings("unused")
interface IState {

    void enter();

    boolean processMessage(@NonNull Message msg);

    void exit();

    @NonNull
    String getName();
}
