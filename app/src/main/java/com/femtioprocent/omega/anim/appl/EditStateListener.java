package com.femtioprocent.omega.anim.appl;

import java.util.EventListener;

public interface EditStateListener extends EventListener {
    void dirtyChanged(boolean is_dirty);
}
