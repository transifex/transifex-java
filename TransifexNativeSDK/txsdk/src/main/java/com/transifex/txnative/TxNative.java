package com.transifex.txnative;

import android.app.Application;
import android.content.Context;

import com.transifex.txnative.wrappers.TxContextWrapper;

import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * The entry point of TransifexNative SDK.
 */
public class TxNative {

    /**
     * Initialize the SDK.
     * <p>
     *     Should be called in {@link Application#onCreate()}.
     * </p>
     */
    public static void init() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new TxInterceptor())
                .build());
    }

    /**
     * Wraps the context to enable TransifexNative functionality.
     *
     * <p>
     *     Check out the installation guide regarding the usage of this method.
     * </p>
     *
     * @param context The context to wrap.
     * @return The wrapped context.
     */
    public static Context wrap(Context context) {
        //return ViewPumpContextWrapper.wrap(context);

        //return new TxContextWrapper(context);

        return ViewPumpContextWrapper.wrap(new TxContextWrapper(context));
    }
}
