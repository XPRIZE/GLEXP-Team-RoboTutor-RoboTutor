//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.util;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;

public class CLogManager implements ILogManager {

    private LogThread      logThread;                   // background thread handling log data
    private String         log_Path;
    private boolean        isLogging = false;

    private Handler logHandler;
    private HashMap queueMap    = new HashMap();
    private boolean mDisabled   = false;

    private File                       logFile;
    private FileOutputStream           logStream;
    private java.nio.channels.FileLock logLock;
    private FileWriter                 logWriter;
    private boolean                    logWriterValid = false;

    // Datashop specific

    private File                       logDSFile;
    private FileOutputStream           logDSStream;
    private java.nio.channels.FileLock logDSLock;
    private FileWriter                 logDSWriter;
    private boolean                    logDSWriterValid = false;


    final private String TAG = "CLogManager";


    // Singleton
    private static CLogManager ourInstance = new CLogManager();

    public static CLogManager getInstance() {
        return ourInstance;
    }

    private CLogManager() {
    }


    public void startLogging(String logPath) {

        log_Path = logPath;

        // Restart the log if necessary
        //
        stopLogging();

        isLogging = true;
        mDisabled = false;
        Log.d(TAG, "Startup");

        logThread = new LogThread(TAG);
        logThread.start();

        try {
            logHandler = new Handler(logThread.getLooper());
        }
        catch(Exception e) {
            Log.d(TAG, "Handler Create Failed:" + e);
        }

        lockLog();
    }


    /**
     * Stop accepting new packets -
     * Causes the thread to flush the input queue and then exit
     *
     */
    public void stopLogging() {

        if(isLogging) {
            Log.d(TAG, "Shutdown begun");

            isLogging = false;
            mDisabled = true;

            // Terminate the log thread - flush the queue prior to exit
            //
            try {

                logThread.getLooper().quitSafely();

                logThread.join();            // waits until it finishes
                Log.d(TAG, "Shutdown complete");

            } catch (InterruptedException e) {
            }

            releaseLog();
        }
    }


    /**
     *  This is a background thread on which to process all log data requests
     *
     */
    private final class LogThread extends HandlerThread {

        public LogThread(String name) {
            super(name);
        }

        public LogThread(String name, int priority) {
            super(name, priority);
        }
    }


    /**
     * This is the central processsing point of the data log - this runs on an independent thread
     * from the UI.
     */
    public class Queue implements Runnable {

        protected String dataPacket;
        protected String target = CPreferenceCache.getPrefID(TCONST.ENGINE_INSTANCE) + TCONST.JSONLOG;

        public Queue(String packet) {
            dataPacket = packet;
        }

        public Queue(String _packet, String _target) {
            dataPacket = _packet;
            target     = _target;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                writePacketToLog(dataPacket, target);

            } catch (Exception e) {
                CErrorManager.logEvent(TAG, "Write Error:", e, false);
            }
        }
    }


    /**
     * We use file locks to keep the logs around until we are finished.  The XPrize initiative used a
     * Google Drive Sync utility that required locking the files so they weren't deleted while in
     * use.  So this is not a requirement otherwise.
     *
     */
    public void lockLog() {

        // Release previous log file if still locked
        //
        if(logWriterValid) {
            releaseLog();
        }

        String path   = CPreferenceCache.getPrefID(TCONST.ENGINE_INSTANCE) + TCONST.JSONLOG;
        String dsPath = CPreferenceCache.getPrefID(TCONST.ENGINE_INSTANCE) + TCONST.DATASHOP + TCONST.JSONLOG;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            String outPath;
            String outDSPath;

            // Validate output folder
            outPath   = log_Path;
            outDSPath = log_Path;
            File outputFile = new File(outPath);

            if (!outputFile.exists())
                    outputFile.mkdir();

            // Generate a tutor instance-unique id for the log name
            //
            outPath += path;

            logFile = new File(outPath);

            try {
                logStream = new FileOutputStream(logFile);
                logLock   = logStream.getChannel().lock();
                logWriter = new FileWriter(outPath, TCONST.APPEND);

                logWriterValid = true;

                // Begin the root JSON element
                postPacket("{");

            } catch (Exception e) {
                Log.d(TAG, "lockLog Failed: " + e);
            }


            //**** DATASHOP

            // Generate a tutor instance-unique id for DataShop
            //
            outDSPath += dsPath;

            logDSFile = new File(outDSPath);

            try {
                logDSStream = new FileOutputStream(logDSFile);
                logDSLock   = logDSStream.getChannel().lock();
                logDSWriter = new FileWriter(outDSPath, TCONST.APPEND);

                logDSWriterValid = true;

            } catch (Exception e) {
                Log.d(TAG, "DataShop lockLog Failed: " + e);
            }

        }
    }


    public void releaseLog() {

        try {
            if(logWriterValid) {

                // Terminate the root JSON element
                postPacket("}");

                logWriterValid = false;

                logWriter.flush();
                logWriter.close();

                logLock.release();
                logStream.close();
            }
        }
        catch(Exception e) {
            Log.d(TAG, "releaseLog Failed: " + e);
        }

        //**** DATASHOP

        try {
            if(logDSWriterValid) {

                logDSWriterValid = false;

                logDSWriter.flush();
                logDSWriter.close();

                logDSLock.release();
                logDSStream.close();
            }
        }
        catch(Exception e) {
            Log.d(TAG, "releaseLog Failed: " + e);
        }

    }



    /**
     * Note that this is currently XPrize log specific.
     * TODO: make general Purpose
     */
    public void writePacketToLog(String jsonPacket, String path) {

        // Append Glyph Data to file
        try {
            // Throws if there is a JSON serializatin error
            //
            if(logWriterValid) {
                logWriter.write(jsonPacket);
                logWriter.flush();
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Serialization Error: " + e);
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {

        if (!mDisabled) {
            queueMap.put(qCommand, qCommand);

            logHandler.post(qCommand);
        }
    }

    /**
     * Post a command to this scenegraph queue
     *
     * @param command
     */
    public void post(String command) {

        enQueue(new Queue(command));
    }


    /**
     * Post a command to this scenegraph queue
     *
     * @param command
     */
    public void postTo(String command, String target) {

        enQueue(new Queue(command, target));
    }


    @Override
    public void postSystemEvent(String Tag, String Msg) {

        String packet;

        // Emit to LogCat
        //
        Log.i(TAG, Msg);

        packet = "{" +
                "\"type\":\"Event\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"msg\":\"" + Msg + "\"," +
                "},\n";

        postTo(packet, TCONST.ENGINE_INSTANCE + TCONST.JSONLOG);
    }


    @Override
    public void postSystemTimeStamp(String Tag) {

        String packet;

        packet = "{" +
                "\"type\":\"TimeStamp\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "},\n";

        postTo(packet, TCONST.ENGINE_INSTANCE + TCONST.JSONLOG);
    }


    @Override
    public void postEvent(String Tag, String Msg) {

        String packet;

        // Emit to LogCat
        //
        Log.i(TAG, Msg);

        packet = "{" +
                "\"type\":\"Event\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"msg\":\"" + Msg + "\"," +
                "},\n";

        post(packet);
    }


    @Override
    public void postTimeStamp(String Tag) {

        String packet;

        packet = "{" +
                "\"type\":\"TimeStamp\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "},\n";

        post(packet);
    }


    @Override
    public void postError(String Tag, String Msg) {

        String packet;

        packet = "{" +
                "\"type\":\"Error\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"msg\":\"" + Msg + "\"," +
                "},\n";

        post(packet);
    }


    @Override
    public void postError(String Tag, String Msg, Exception e) {

        String packet;

        packet = "{" +
                "\"type\":\"Error\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"msg\":\"" + Msg + "\"," +
                "\"exception\":\"" + e.toString() + "\"," +
                "},\n";

        post(packet);
    }


    @Override
    public void postPacket(String packet) {

        post(packet);
    }

}
