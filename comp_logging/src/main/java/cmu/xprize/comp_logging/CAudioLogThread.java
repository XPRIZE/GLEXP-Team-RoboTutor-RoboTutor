package cmu.xprize.comp_logging;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CAudioLogThread extends Thread {

    /**
     * size of the buffer to use. For mapping stream-based frame time, we want buffer size to be a multiple of
     * centisecond frame size = 160.  This size receives updated hypothesis 10 times a second
     */
    static final int SAMPLERATE = 16000;                          // for sample rate check
    static final int AUDIO_BUFFER_SIZE = SAMPLERATE / 1;          // 1 second worth of data
    public static final int BUFFER_SIZE = SAMPLERATE / 10;        // 1/10 seconds worth at 16 Khz

    public static short[] audioBuffer = new short[AUDIO_BUFFER_SIZE];
    static int readIndex = 0;
    static int writeIndex = 0;

    private boolean isRecording = false;

    private final String logDir;
    private final String logFileName;

    public CAudioLogThread(String logDir, String logFileName) {
        this.logFileName = logFileName;
        this.logDir = logDir;
    }

    @Override
    public void run() {

        OutputStream output = null;
        AudioRecord recorder = null;
        short[] buffer = new short[BUFFER_SIZE];
        int readCount;

        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            output = new FileOutputStream(new File(logDir, logFileName + ".raw"));

            recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE);

            while (!Thread.interrupted()) {
                // Ensure we are recording while the thread is running.
                //
                if (!isRecording) {
                    Log.i("AudioLog", "Resume recording");
                    recorder.startRecording();
                    isRecording = true;
                    readCount = 0;
                } else {
                    readCount = recorder.read(buffer, 0, BUFFER_SIZE);
                    Log.i("AudioLog", "Read from recorder: read_count = " + readCount);
                }

                if (readCount == AudioRecord.ERROR_INVALID_OPERATION || readCount == AudioRecord.ERROR_BAD_VALUE) {
                    Log.i("AudioLog", "Read Error");
                    throw new RuntimeException("error reading from recorder");
                } else {
                    try {
                        writeBuffer(buffer, readCount);
                        for (int i = 0; i < readCount; i++) writeShort(output, buffer[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            Log.e("AudioLog", "Exception " + e.getMessage());

        } finally {
            try {
                Log.i("AudioLog", "Stop session");

                recorder.stop();

                readCount = recorder.read(buffer, 0, buffer.length);
                for (int i = 0; i < readCount; i++) writeShort(output, buffer[i]);
                Log.i("AudioLog", "Final read from recorder: read_count = " + readCount);

                recorder.release();

                output.close();

                // convert raw capture to wav format
                convertRawToWav(new File(logDir, logFileName + ".raw"), new File(logDir, logFileName + ".wav"));

            } catch (IOException e) {
                Log.e("AudioLog", "Closing streams: " + e.getMessage());
            }
        }
    }

    private static void writeShort(OutputStream out, short val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
    }

    synchronized public static int readBuffer(short[] buffer, int length) {
        for (int i = 0; i < length; i++) {
            if (readIndex == writeIndex) return i;
            buffer[i] = audioBuffer[readIndex];
            readIndex = ++readIndex % AUDIO_BUFFER_SIZE;
        }
        return length;
    }

    synchronized static void writeBuffer(short[] buffer, int length) {
        if (!(length == AudioRecord.ERROR_INVALID_OPERATION || length == AudioRecord.ERROR_BAD_VALUE)) {
            for (int i = 0; i < length; i++) {
                audioBuffer[writeIndex] = buffer[i];
                if ((writeIndex = ++writeIndex % AUDIO_BUFFER_SIZE) == readIndex) readIndex = ++readIndex % AUDIO_BUFFER_SIZE;
            }
        }
    }

    /**
     * utility to convert raw audio capture file to wav format. Assumes 16Khz mono
     */
    public static void convertRawToWav(File rawFile, File wavFile) {

        InputStream input = null;
        OutputStream output = null;

        try {
            input = new FileInputStream(rawFile);
            output = new FileOutputStream(wavFile);
            // first write appropriate wave file header
            ByteArrayOutputStream hdrBytes = new ByteArrayOutputStream();
            new WaveHeader(WaveHeader.FORMAT_PCM, (short) 1, 16000, (short) 16, (int) rawFile.length()).write(hdrBytes);
            output.write(hdrBytes.toByteArray());
            // then copy raw bytes to output file
            byte[] audioBuffer = new byte[4096];
            int nRead;
            while ((nRead = input.read(audioBuffer)) > 0) {
                output.write(audioBuffer, 0, nRead);
            }
            // finish up
            output.close();
            input.close();
            // on success, delete raw file
            rawFile.delete();

        } catch (Exception e) {
            Log.e("convertRawToWav", "Exception " + e.getMessage());

        } finally {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
            } catch (IOException e) {
                Log.e("convertRawToWav", "Closing streams: " + e.getMessage());
            }
        }
    }
}
