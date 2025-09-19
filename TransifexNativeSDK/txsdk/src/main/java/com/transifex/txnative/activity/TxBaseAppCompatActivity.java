package com.transifex.txnative.activity;

import com.transifex.txnative.TxNative;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * A base activity that extends AppCompatActivity and implements context wrapping so that the
 * TxNative functionality is enabled.
 * <p>
 * Make sure your activities extend this class or have the same implementation.
 */
public class TxBaseAppCompatActivity extends AppCompatActivity {

   private AppCompatDelegate  mAppCompatDelegate;

   @NonNull
   @Override
   public AppCompatDelegate getDelegate() {
      // Wrap AppCompat delegate
      if (mAppCompatDelegate == null) {
         mAppCompatDelegate = TxNative.wrapAppCompatDelegate(super.getDelegate(), this);
      }
      return mAppCompatDelegate;
   }

   // If your app uses AppCompat 1.3 or older and
   // "AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);"  is set , uncomment the
   // following lines. Otherwise, TxNative functionality will be impaired when running on older
   // platforms (< API 21).
   //
   // Starting from AppCompat 1.4.0, "setCompatVectorFromResourcesEnabled()" does not need this
   // workaround.

//   private Resources mResources; // final resources (VectorEnabledTintResourcesWrapper or TxResources)
//
//   @SuppressLint("RestrictedApi")
//   @Override
//   public Resources getResources() {
//      Resources resources = getBaseContext().getResources();
//      if (mResources == null && VectorEnabledTintResources.shouldBeUsed()) {
//         // We wrap the Resources returned by the base context in VectorEnabledTintResourcesWrapper,
//         // similarly to what AppCompatActivity does. However, these resources are not always
//         // the same as the ones used internally by AppCompatActivity, but that's the best
//         // we can do.
//         mResources = new VectorEnabledTintResourcesWrapper(this, resources);
//      }
//      return mResources == null ? resources : mResources;
//   }
}
