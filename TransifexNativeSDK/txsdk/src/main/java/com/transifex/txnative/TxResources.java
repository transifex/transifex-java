package com.transifex.txnative;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Native;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

/**
 * Resources wrapper that enables TxNative functionality by overriding specific String related
 * methods.
 * <p>
 * We override some of the calls that return strings. The rest of the string methods, call
 * internally the ones we have overridden. The remaining methods are delegated to the
 * wrapped {@link Resources} object.
 * <p>
 * All resource method calls made programmatically by the user will reach here. String resources
 * used during the view inflation process will also be read from here, after having the process
 * intercepted by {@link TxInterceptor} and having the view transformed by the respective
 * {@link com.transifex.txnative.transformers.ViewTransformer} class.
 */
public class TxResources extends Resources {

    private final Resources mResources;
    private final NativeCore mNativeCore;

    /**
     * Creates a new instance.
     *
     * @param baseResources The {@link Resources} object to wrap.
     * @param nativeCore A {@link NativeCore} instance.
     */
    public TxResources(@NonNull Resources baseResources, @NonNull NativeCore nativeCore) {
        super(baseResources.getAssets(), baseResources.getDisplayMetrics(), baseResources.getConfiguration());
        mResources = baseResources;
        mNativeCore = nativeCore;
    }

    //region Overrides

    @NonNull
    @Override
    public CharSequence getText(@StringRes int id) throws NotFoundException {
        return mNativeCore.translate(this, id);
    }

    @Override
    public CharSequence getText(@StringRes int id, CharSequence def) {
        return mNativeCore.translate(this, id, def);
    }

    @NonNull
    @Override
    public CharSequence getQuantityText(@StringRes int id, int quantity) throws NotFoundException {
        CharSequence originalString = mResources.getQuantityText(id, quantity);

        if (isAndroidStringResource(id)) {
            return originalString;
        }

        if (quantity == 1) {
            return "test: " + originalString;
        }
        else {
            return  "tests " + originalString;
        }
    }

    @NonNull
    @Override
    public CharSequence[] getTextArray(@StringRes int id) throws NotFoundException {
        CharSequence[] originalTextArray = mResources.getTextArray(id);

        if (isAndroidStringResource(id)) {
            return originalTextArray;
        }

        return new CharSequence[]{"test1", "test2"};
    }

    @NonNull
    @Override
    public String[] getStringArray(@StringRes int id) throws NotFoundException {
        String[] originalStringArray = mResources.getStringArray(id);

        if (isAndroidStringResource(id)) {
            return originalStringArray;
        }

        return new String[]{"test1", "test2"};
    }

    //endregion Overrides

    //region Interface

    /**
     * Checks if the provided string resource id belongs to Android's resource package.
     *
     * @param id The string resource ID to check.
     *
     * @return true if it belong's to Android's resource package, false otherwise.
     *
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     */
    boolean isAndroidStringResource(@StringRes int id) {
        String resourcePackageName = mResources.getResourcePackageName(id);
        return resourcePackageName.equals("android");
    }

    @NonNull CharSequence getOriginalText(@StringRes int id) {
        return mResources.getText(id);
    }

    @Nullable CharSequence getOriginalText(@StringRes int id, @Nullable CharSequence def) {
        return mResources.getText(id, def);
    }

    //endregion Interface

    //region Delegation

    /*
     * Delegates all `Resources` methods to the base `Resources` object (mResources), except for
     * all string related methods which are overridden in this class or reach the super class. Code
     * below has been copied from `ResourcesWrapper` and  allows any customization of the
     * wrapped Resources object, done by some subclass, to be also picked up.
     */

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        return mResources.getIntArray(id);
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        return mResources.obtainTypedArray(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        return mResources.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        return mResources.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        return mResources.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
        return mResources.getFraction(id, base, pbase);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return mResources.getDrawable(id);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        return mResources.getDrawable(id, theme);
    }

    @SuppressWarnings("deprecation")
    @RequiresApi(15)
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return mResources.getDrawableForDensity(id, density);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        return mResources.getDrawableForDensity(id, density, theme);
    }

    @SuppressWarnings("deprecation")
    @Override
    public android.graphics.Movie getMovie(int id) throws NotFoundException {
        return mResources.getMovie(id);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getColor(int id) throws NotFoundException {
        return mResources.getColor(id);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        return mResources.getColorStateList(id);
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        return mResources.getBoolean(id);
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        return mResources.getInteger(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return mResources.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return mResources.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        return mResources.getXml(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        return mResources.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        return mResources.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        return mResources.openRawResourceFd(id);
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        mResources.getValue(id, outValue, resolveRefs);
    }

    @RequiresApi(15)
    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        mResources.getValueForDensity(id, density, outValue, resolveRefs);
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        mResources.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return mResources.obtainAttributes(set, attrs);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        super.updateConfiguration(config, metrics);
        if (mResources != null) { // called from super's constructor. So, need to check.
            mResources.updateConfiguration(config, metrics);
        }
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return mResources.getDisplayMetrics();
    }

    @Override
    public Configuration getConfiguration() {
        return mResources.getConfiguration();
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        return mResources.getIdentifier(name, defType, defPackage);
    }

    @Override
    public String getResourceName(@AnyRes int resid) throws NotFoundException {
        return mResources.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(@AnyRes int resid) throws NotFoundException {
        return mResources.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(@AnyRes int resid) throws NotFoundException {
        return mResources.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(@AnyRes int resid) throws NotFoundException {
        return mResources.getResourceEntryName(resid);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
            throws XmlPullParserException, IOException {
        mResources.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
            throws XmlPullParserException {
        mResources.parseBundleExtra(tagName, attrs, outBundle);
    }

    //endregion Delegation
}
