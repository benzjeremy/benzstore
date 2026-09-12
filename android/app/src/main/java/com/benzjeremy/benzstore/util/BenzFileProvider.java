package com.benzjeremy.benzstore.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

public class BenzFileProvider extends ContentProvider {
    public static final String AUTHORITY = "com.benzjeremy.benzstore.fileprovider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File cacheDir = getContext().getCacheDir();
        String path = uri.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        File file = new File(cacheDir, path != null ? path : "");
        if (!file.exists()) {
            throw new FileNotFoundException("File not found in store cache: " + path);
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        String p = uri.getPath();
        if (p != null && p.endsWith(".apk")) {
            return "application/vnd.android.package-archive";
        }
        return "application/octet-stream";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String path = uri.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        File file = new File(getContext().getCacheDir(), path != null ? path : "");
        String[] cols = projection != null ? projection : new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor cursor = new MatrixCursor(cols);
        MatrixCursor.RowBuilder row = cursor.newRow();
        for (String col : cols) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                row.add(file.getName());
            } else if (OpenableColumns.SIZE.equals(col)) {
                row.add(file.length());
            } else {
                row.add(null);
            }
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
