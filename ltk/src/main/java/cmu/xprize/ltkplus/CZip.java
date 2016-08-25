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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CZip {

    private ZipFile _zipFile;

    public CZip(ZipFile zipFile) {
        this._zipFile = zipFile;
    }

    public CZip(String pathToZipFile) throws IOException {
        this._zipFile = new ZipFile(pathToZipFile);
    }

    public void close() throws IOException {
        _zipFile.close();
    }

    public void unzip(String extractPath) throws IOException {
        File targetDir = new File(extractPath);
        if(!targetDir.exists() && !targetDir.mkdirs()){
            throw new IOException("Unable to create directory");
        }

        if(!targetDir.isDirectory()){
            throw new IOException("Unable to extract to a non-directory");
        }
        Enumeration<? extends ZipEntry> zipEntries = _zipFile.entries();

        while(zipEntries.hasMoreElements()){
            ZipEntry zipEntry = zipEntries.nextElement();
            String path = extractPath + zipEntry.getName();
            if(zipEntry.isDirectory()){
				/*File newDir = new File(path);
				if(!newDir.mkdirs()){
					throw new IOException("Unable to extract the zip entry " + path);
				}*/
            }
            else {
                BufferedInputStream inputStream = new BufferedInputStream(_zipFile.getInputStream(zipEntry));

                File outputFile = new File(path);
                File outputDir = new File(outputFile.getParent());

                if(!outputDir.exists() && !outputDir.mkdirs()){
                    throw new IOException("unable to make directory for entry " + path);
                }

                if(!outputFile.exists() && !outputFile.createNewFile()){
                    throw new IOException("Unable to create directory for " + path);
                }

                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                try {
                    int currByte;
                    while((currByte = inputStream.read()) != -1) {
                        outputStream.write(currByte);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                finally{
                    outputStream.close();
                    inputStream.close();
                }
            }
        }
    }
}
