//    Copyright (c) 2015 Carnegie Mellon University
//
//    Permission is hereby granted, free of charge, to any person obtaining a copy
//            of this software and associated documentation files (the "Software"), to deal
//            in the Software without restriction, including without limitation the rights
//            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//            copies of the Software, and to permit persons to whom the Software is
//            furnished to do so, subject to the following conditions:
//
//            The above copyright notice and this permission notice shall be included in
//            all copies or substantial portions of the Software.
//
//            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
//            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//            THE SOFTWARE.

package cmu.xprize.ltkplus;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

public class CAssetInstaller {

    private Context   context;
    private String[]  zipNames;

    private static final String TAG = "AssetInstaller";


    public CAssetInstaller(Context context, String[] zipNames) {

        this.context  = context;
        this.zipNames = zipNames;
    }


    private void copyAssets() {

        AssetManager assetManager = context.getAssets();

        String[] files = null;

        try {
            files = assetManager.list("");

        } catch (IOException e) {

            Log.e(TAG, "Failed to get asset file list.", e);
        }

        for (String filename : files) {

            InputStream in = null;
            OutputStream out = null;

            try {
                in = assetManager.open(filename);
                out = new FileOutputStream(context.getExternalFilesDir(null)
                        .getPath() + "/" + filename);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;

            } catch (IOException e) {

                Log.e(TAG, "Failed to copy asset file: " + filename, e);
            }
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int    read;

        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read);
        }
    }


    private void explodeAsset() throws IOException {

        for(String zipName : zipNames) {

            String zipPath = context.getExternalFilesDir(null).getPath() + "/"
                    + zipName + ".zip";

            String extractPath = context.getExternalFilesDir(null).getPath() + "/";

            File file       = new File(zipPath);
            ZipFile zipFile = new ZipFile(file);

            try {
                CZip _zip = new CZip(zipFile);
                _zip.unzip(extractPath);
                _zip.close();

                file.delete();

            } catch (IOException ie) {

                Log.e(TAG, "failed extraction", ie);
            }
        }

    }


    /**
     * Ensure all archives have been expanded.
     *
     * @return
     */
    private boolean dirCheck() {

        boolean result = true;

        for(String zipName : zipNames) {

            File dir = new File(context.getExternalFilesDir(null).getPath() + "/"
                    + zipName);

            result = dir.exists();

            if(!result)
                    break;
        }

        return result;
    }


    public void execute() throws IOException {

        if (!dirCheck()) {

            copyAssets();
            explodeAsset();

            Log.d(TAG, "LTK assets installed.");
        } else {

            Log.d(TAG, "LTK assets already installed");
        }
    }
}
