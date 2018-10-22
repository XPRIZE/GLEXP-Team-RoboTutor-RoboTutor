package cmu.xprize.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class ImageLoader {

    public static RequestLoader with(Context context) {
        return new RequestLoader(context);
    }

    static class RequestLoader {
        private Context context;
        private int imageResource;

        RequestLoader(@NonNull Context context) {
            this.context = context;
        }

        /**
         * Load a Drawable into the RequestLoader
         *
         * @param imagePath
         * @return
         */
        public RequestLoader loadDrawable(String imagePath) {
            this.imageResource = context.getResources().getIdentifier(imagePath, "drawable", context.getPackageName());
            return this;
        }



        public void into(ImageView imageView) {

            Drawable image = ContextCompat.getDrawable(context, imageResource);
            imageView.setImageDrawable(image);

        }
    }

    public static BitmapLoader makeBitmapLoader(String path) { return new BitmapLoader(path);}

    // NEW_THUMBS (3) copy this BitmapLoader. Use it to load images.
    public static class BitmapLoader {

        private Bitmap bitmapResource;
        private String _path;

        BitmapLoader(String path) {
            this._path = path;
        }

        /**
         * Load a Bitmap
         * @param imageName
         * @return
         */
        public BitmapLoader loadBitmap(String imageName) throws FileNotFoundException {
            String fullPath = _path + imageName;

            InputStream in = new FileInputStream(_path + imageName);
            bitmapResource = BitmapFactory.decodeStream(in);

            return this;
        }

        public void into(ImageView imageView) {
            imageView.setImageBitmap(this.bitmapResource);
        }
    }
}
