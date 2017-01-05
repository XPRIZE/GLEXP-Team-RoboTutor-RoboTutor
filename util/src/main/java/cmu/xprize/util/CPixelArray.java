package cmu.xprize.util;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class CPixelArray {

    private Bitmap  _sourceBitmap;

    private int     _left, _top;
    private int     _width, _height;
    private int     _stride;
    private int[]   _pixels;


    public CPixelArray(Bitmap _source ) {

        _sourceBitmap = _source;

        _stride = 0;
        _left   = 0;
        _top    = 0;

        _width = _sourceBitmap.getWidth();
        _height = _sourceBitmap.getHeight();

        _pixels = new int[_width * _height];

        getPixels();
    }


    public CPixelArray(Bitmap _source, Rect srcRect) {

        _sourceBitmap = _source;

        _stride = 0;

        _left = srcRect.left;
        _top = srcRect.top;

        _width = srcRect.width();
        _height = srcRect.height();

        _pixels = new int[_width * _height];

        getPixels();
    }


    public void getPixels() {

        _sourceBitmap.getPixels(_pixels,_stride, _width, _left, _top, _width, _height);
    }


    public int scanForColor(int testColor) {

        int count = 0;
        int size  = getSize();

        for (int i1 = 0; i1 < size ; i1++) {

            if (_pixels[i1] == testColor) {

                count++;
            }
        }

        return count;
    }


    public int scanNotColor(int testColor) {

        int count = 0;
        int size  = getSize();

        for (int i1 = 0; i1 < size ; i1++) {

            if (_pixels[i1] != 0 && _pixels[i1] != testColor) {

                count++;
            }
        }

        return count;
    }


    public int scanAndReplace(int testColor, int replaceColor) {

        int errValue = 0;
        int size     = getSize();

        for (int i1 = 0; i1 < size ; i1++) {

            if (_pixels[i1] == testColor) {

                _pixels[i1] =  replaceColor;
                errValue++;
            }
        }

        return errValue;
    }



    public int getPixel(int x, int y){
        return _pixels[x+y* _width];
    }


    public int getPixel(int x){
        return _pixels[x];
    }


    public void setPixel(int x, int y, int color){
        _pixels[x+y* _width]=color;
    }

    public void setPixel(int x, int color){
        _pixels[x]=color;
    }


    public void applyBitmap(){

        _sourceBitmap.setPixels(_pixels,0, _width,0,0, _width, _height);
        _sourceBitmap = null;
    }


    public int getWidth(){
        return _width;
    }


    public int getHeight(){
        return _height;
    }


    public int getSize() { return _width * _height;}
}
