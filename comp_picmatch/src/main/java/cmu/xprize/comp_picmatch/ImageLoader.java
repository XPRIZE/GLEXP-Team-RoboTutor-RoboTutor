package cmu.xprize.comp_picmatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


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

    public static BitmapLoader makeBitmapLoader() { return new BitmapLoader();}

    // NEW_THUMBS (3) copy this BitmapLoader. Use it to load images.
    static class BitmapLoader {
        // how to make language variable???
        private static final String IMAGE_SOURCE_PATH = TCONST.ROBOTUTOR_ASSETS + "/" + TCONST.STORY_ASSETS + "/sw/" + TCONST.SHARED_LITERACY_IMAGE_FOLDER + "/";
        private static final String IMAGE_EXTENSION = ".jpg";

        private Bitmap bitmapResource;

        BitmapLoader() {}

        /**
         * Load a Bitmap
         * @param imageName
         * @return
         */
        public BitmapLoader loadBitmap(String imageName) throws FileNotFoundException {
            String fullPath = IMAGE_SOURCE_PATH + imageName;

            InputStream in = new FileInputStream(IMAGE_SOURCE_PATH + imageName + IMAGE_EXTENSION);
            bitmapResource = BitmapFactory.decodeStream(in);

            return this;
        }

        public void into(ImageView imageView) {
            imageView.setImageBitmap(this.bitmapResource);
        }
    }
}
