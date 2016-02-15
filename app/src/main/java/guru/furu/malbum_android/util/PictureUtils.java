package guru.furu.malbum_android.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by richard on 2/9/16.
 *
 * Camera bitmap-related things
 */
public class PictureUtils {

    private static final String DEBUG = "PictureUtils";

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        // read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // figure out how much to scale down by
        int inSampleSize = 2;


        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // read in and create final bitmap
        Bitmap bt = BitmapFactory.decodeFile(path, options);

        // compress bitmap to reduce file size
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bt.compress(Bitmap.CompressFormat.JPEG, 40, out);

        Bitmap compressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        try {
            // save scaled version over bigger one on disk
            FileOutputStream fw = new FileOutputStream(path);

            fw.write(out.toByteArray(), 0, out.size());
            fw.close();
        } catch (IOException ioe) {
            Log.e(DEBUG, "Error saving scaled file!");
        }

        return compressed;

    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(path, size.x, size.y);
    }
}
