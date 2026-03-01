package com.lyco.tomi.mock;

import android.content.Context;

/**
 * Release build keeps Retrofit pointed at the real backend, so the mock controller does nothing.
 */
public final class MockServerController {
    private MockServerController() {
    }

    public static void initIfNeeded(Context context) {
        // no-op
    }
}
