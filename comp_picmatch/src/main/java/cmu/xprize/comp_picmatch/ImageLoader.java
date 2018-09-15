package cmu.xprize.comp_picmatch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;


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

        // // ALAN_HILL (5) here is how to load the image... NEXT NEXT NEXT replace this...
        public RequestLoader loadDrawable(String imagePath) {
            this.imageResource = context.getResources().getIdentifier(imagePath, "drawable", context.getPackageName());
            return this;
        }

        public void into(ImageView imageView) {
            Drawable image = ContextCompat.getDrawable(context, imageResource);
            imageView.setImageDrawable(image);
        }
    }
}
