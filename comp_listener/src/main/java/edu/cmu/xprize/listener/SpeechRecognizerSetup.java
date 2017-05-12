package edu.cmu.xprize.listener;

/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

// AW: variant of the edu.cmu.pocketsphinx.SpeechRecognizer2 class code from
// pocketsphinx-android so we can customize it as SpeechRecognizer22 in our
// package. Needed to access other decoder methods.

import java.io.File;

import edu.cmu.pocketsphinx.Config;

import static edu.cmu.pocketsphinx.Decoder.defaultConfig;
import static edu.cmu.pocketsphinx.Decoder.fileConfig;

/**
 * Wrapper for the decoder configuration to implement builder pattern.
 * Configures most important properties of the decoder
 */
public class SpeechRecognizerSetup {

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    private final Config config;

    /**
     * Creates new speech recognizer builder with default configuration.
     */
    public static SpeechRecognizerSetup defaultSetup() {
        return new SpeechRecognizerSetup(defaultConfig());
    }

    /**
     * Creates new speech recognizer builder from configuration file.
     * Configuration file should consist of lines containing key-value pairs.
     *
     * @param configFile configuration file
     */
    public static SpeechRecognizerSetup setupFromFile(File configFile) {
        return new SpeechRecognizerSetup(fileConfig(configFile.getPath()));
    }

    private SpeechRecognizerSetup(Config config) {
        this.config = config;
    }

    public SpeechRecognizer getRecognizer() {
        return new SpeechRecognizer(config);
    }

    public SpeechRecognizerSetup setAcousticModel(File model) {
        return setString("-hmm", model.getPath());
    }

    public SpeechRecognizerSetup setDictionary(File dictionary) {
        return setString("-dict", dictionary.getPath());
    }

    public SpeechRecognizerSetup setSampleRate(int rate) {
        return setFloat("-samprate", rate);
    }

    public SpeechRecognizerSetup setRawLogDir(File dir) {
        return setString("-rawlogdir", dir.getPath());
    }

    public SpeechRecognizerSetup setKeywordThreshold(float threshold) {
        return setFloat("-kws_threshold", threshold);
    }

    public SpeechRecognizerSetup setBoolean(String key, boolean value) {
        config.setBoolean(key, value);
        return this;
    }

    public SpeechRecognizerSetup setInteger(String key, int value) {
        config.setInt(key, value);
        return this;
    }

    public SpeechRecognizerSetup setFloat(String key, float value) {
        config.setFloat(key, value);
        return this;
    }

    public SpeechRecognizerSetup setDouble(String key, double value) {
        config.setFloat(key, value);
        return this;
    }

    public SpeechRecognizerSetup setString(String key, String value) {
        config.setString(key, value);
        return this;
    }
}
