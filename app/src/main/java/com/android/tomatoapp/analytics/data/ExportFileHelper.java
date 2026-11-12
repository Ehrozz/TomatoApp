package com.android.tomatoapp.analytics.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class ExportFileHelper {

    private ExportFileHelper() {
    }

    static File createExportFile(Context context, String fileName) throws IOException {
        File exportsDir = context.getExternalFilesDir("exports");
        if (exportsDir == null) {
            throw new IOException("External files directory is unavailable");
        }
        if (!exportsDir.exists() && !exportsDir.mkdirs()) {
            throw new IOException("Unable to create exports directory");
        }
        return new File(exportsDir, fileName);
    }

    static void publishToDownloads(Context context, File sourceFile) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            publishWithMediaStore(context, sourceFile);
        } else {
            File downloadsDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "TomatoApp");
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                throw new IOException("Unable to create Downloads/TomatoApp directory");
            }
            copy(sourceFile, new File(downloadsDir, sourceFile.getName()));
        }
    }

    private static void publishWithMediaStore(Context context, File sourceFile) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.getName());
        values.put(MediaStore.Downloads.MIME_TYPE, getMimeType(sourceFile));
        values.put(MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separator + "TomatoApp");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = context.getContentResolver().insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Unable to create MediaStore download");
        }

        try {
            try (FileInputStream input = new FileInputStream(sourceFile);
                 ParcelFileDescriptor descriptor = context.getContentResolver().openFileDescriptor(uri, "w")) {
                if (descriptor == null) {
                    throw new IOException("Unable to open MediaStore download");
                }
                try (FileOutputStream output = new FileOutputStream(descriptor.getFileDescriptor())) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                }
            }

            ContentValues completed = new ContentValues();
            completed.put(MediaStore.Downloads.IS_PENDING, 0);
            context.getContentResolver().update(uri, completed, null, null);
        } catch (Exception e) {
            context.getContentResolver().delete(uri, null, null);
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Unable to publish export", e);
        }
    }

    private static void copy(File source, File destination) throws IOException {
        try (FileInputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }
    }

    private static String getMimeType(File file) {
        return file.getName().endsWith(".pdf") ? "application/pdf" : "text/csv";
    }
}
