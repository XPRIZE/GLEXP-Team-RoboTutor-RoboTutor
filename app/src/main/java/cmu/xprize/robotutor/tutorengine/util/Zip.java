package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cmu.xprize.util.TCONST;

public class Zip {

    private ZipFile  _zipFile;
    private Context   mContext;

    private LocalBroadcastManager   bManager;


    public Zip(ZipFile zipFile, Context _context) {

        _zipFile = zipFile;
        init(_context);
    }


    public Zip(String pathToZipFile, Context _context) throws IOException {

        _zipFile = new ZipFile(pathToZipFile);
        init(_context);
    }

    public void init(Context _context) {

        mContext = _context;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(mContext);
    }

    public void close() throws IOException {
        _zipFile.close();
    }


    public void broadcast(String Action, String Msg) {

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.TEXT_FIELD, Msg);

        bManager.sendBroadcast(msg);
    }


    public void broadcast(String Action, int Msg) {

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.INT_FIELD, Msg);

        bManager.sendBroadcast(msg);
    }


    public void extractAll(String extractName, String extractPath) throws IOException {

        File targetDir = new File(extractPath);
        int  fileCnt   = 0;

        if(!targetDir.exists() && !targetDir.mkdirs()){
            throw new IOException("Unable to create directory");
        }

        if(!targetDir.isDirectory()){
            throw new IOException("Unable to extract to a non-directory");
        }

        Enumeration<? extends ZipEntry> zipEntries = _zipFile.entries();

        broadcast(TCONST.START_PROGRESSIVE_UPDATE, new Integer(_zipFile.size()).toString());
        broadcast(TCONST.PROGRESS_TITLE, TCONST.ASSET_UPDATE_MSG + extractName + TCONST.PLEASE_WAIT);

        while(zipEntries.hasMoreElements()){

            ZipEntry zipEntry = zipEntries.nextElement();

            String path = extractPath + zipEntry.getName();

            broadcast(TCONST.UPDATE_PROGRESS, new Integer(++fileCnt).toString());

            if(zipEntry.isDirectory()){

				File newDir = new File(path);

				if(!newDir.exists() && !newDir.mkdirs()){
					throw new IOException("Unable to extract the zip entry " + path);
				}
            }
            else {

                broadcast(TCONST.PROGRESS_MSG2, path);

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
