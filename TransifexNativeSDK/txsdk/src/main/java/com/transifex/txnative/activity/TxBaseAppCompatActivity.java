package com.transifex.txnative.activity;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.TxContextWrappingDelegateJava;

/**
 * A base activity that extends AppCompatActivity and implements context wrapping so that the
 * TxNative functionality is enabled.
 * <p>
 * Make sure your activities extend this class or have the same implementation.
 * <p>
 * If your app does not use <code>Appcompat</code> don't use this class. Instead use
 * {@link TxBaseActivity}.
 */
public class TxBaseAppCompatActivity extends AppCompatActivity {

   private TxContextWrappingDelegateJava mAppCompatDelegate;

   @NonNull
   @Override
   public AppCompatDelegate getDelegate() {
      // Wrap AppCompat delegate
      if (mAppCompatDelegate == null) {
         mAppCompatDelegate = new TxContextWrappingDelegateJava(super.getDelegate());
      }
      return mAppCompatDelegate;
   }

   @Override
   public Resources getResources() {
      // Calling "getBaseContext().getResources()", instead of "super.getResources()",  returns the
      // resources straight from TxResources and makes sure that the underlying assets are updated.
      // "super.getResources()" returns a cached  resources object which may contain older assets.
      return getBaseContext().getResources();
   }

   // If your app uses AppCompat 1.2 and
   // "AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);"  is set , uncomment the
   // following lines. Otherwise, TxNative functionality will be impaired when running on older
   // platforms (< API 21).
   //
   // Starting from AppCompat 1.5.0, "setCompatVectorFromResourcesEnabled()" does not need this
   // workaround.

//   private Resources mResources; // final resources (VectorEnabledTintResourcesWrapper or TxResources)
//   private Resources mPreviousResources; // resources as returned by base context (TxResources)
//
//   @SuppressLint("RestrictedApi")
//   @Override
//   public Resources getResources() {
//      // If the resources have changed, we need to create a new VectorEnabledTintResourcesWrapper
//      // if needed.
//      Resources resources = getBaseContext().getResources();
//      if (resources != mPreviousResources) {
//         mPreviousResources = resources;
//         mResources = null;
//      }
//
//      if (mResources == null && VectorEnabledTintResources.shouldBeUsed()) {
//         // We wrap the Resources returned by the base context in VectorEnabledTintResourcesWrapper,
//         // similarly to what AppCompatActivity does. However, these resources are not always
//         // the same as the ones used internally by AppCompatActivity, but that's the best
//         // we can do.
//         //TODO: perhaps we could use reflection
//         mResources = new VectorEnabledTintResourcesWrapper(this, resources);
//      }
//      return mResources == null ? resources : mResources;
//   }
}
