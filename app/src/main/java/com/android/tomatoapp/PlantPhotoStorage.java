package com.android.tomatoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper for saving monitoring photos to app-specific storage.
 */
public final class PlantPhotoStorage {

    private static final String DIRECTORY = "monitoring_photos";
    private static final SimpleDateFormat FILE_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private PlantPhotoStorage() {}

    /**
     * Saves the bitmap as JPEG inside the app's private files dir.
     *
     * @return absolute path of the saved file.
     */
    @Nullable
    public static String saveBitmap(@NonNull Context context,
                                    @NonNull Bitmap bitmap,
                                    @Nullable String prefix) {
        File dir = new File(context.getFilesDir(), DIRECTORY);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }

        String timeStamp = FILE_FORMAT.format(new Date());
        String fileName = (prefix != null ? prefix + "_" : "") + timeStamp + ".jpg";
        File file = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (IOException e) {
            if (file.exists()) {
                // Delete partial file
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) { }
            }
        }
    }

    public static boolean deletePhoto(@Nullable String path) {
        if (path == null) return false;
        File file = new File(path);
        return file.exists() && file.delete();
    }

    /**
     * Returns a content URI for sharing the photo via FileProvider (optional).
     */
    @Nullable
    public static Uri getContentUri(@NonNull Context context, @Nullable String path) {
        if (path == null) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        String authority = context.getPackageName() + ".fileprovider";
        try {
            return FileProvider.getUriForFile(context, authority, file);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

