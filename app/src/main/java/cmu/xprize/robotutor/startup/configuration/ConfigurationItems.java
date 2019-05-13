package cmu.xprize.robotutor.startup.configuration;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class ConfigurationItems implements ILoadableObject {

    private static final String TAG = "ConfigurationItems";

    public static final String CONFIG_VERSION = "CONFIG_VERSION";
    public static final String LANGUAGE_OVERRIDE = "LANGUAGE_OVERRIDE";
    public static final String SHOW_TUTOR_VERSION = "SHOW_TUTOR_VERSION";
    public static final String SHOW_DEBUG_LAUNCHER = "SHOW_DEBUG_LAUNCHER";
    public static final String LANGUAGE_SWITCHER = "LANGUAGE_SWITCHER";
    public static final String NO_ASR_APPS = "NO_ASR_APPS";
    public static final String LANGUAGE_FEATURE_ID = "LANGUAGE_FEATURE_ID";
    public static final String SHOW_DEMO_VIDS = "SHOW_DEMO_VIDS";
    public static final String USE_PLACEMENT = "USE_PLACEMENT";
    public static final String RECORD_AUDIO = "RECORD_AUDIO";

    public String config_version;
    public boolean language_override;
    public boolean show_tutorversion;
    public boolean show_debug_launcher;
    public boolean language_switcher;
    public boolean no_asr_apps;
    public String language_feature_id;
    public boolean show_demo_vids;
    public boolean use_placement;
    public boolean record_audio;

    public ConfigurationItems() {
        String dataPath = TCONST.DOWNLOAD_PATH + "/config.json";
        String jsonData = JSON_Helper.cacheDataByName(dataPath);

        try {
            loadJSON(new JSONObject(jsonData), null);
        } catch (Exception e) {
            Log.e(TAG, "Invalid Data Source for : " + dataPath, e);
            setDefaults();
        }
    }

    // used for QuickOptions
    public ConfigurationItems(String config_version, boolean language_override,
                              boolean show_tutorversion, boolean show_debug_launcher,
                              boolean language_switcher, boolean no_asr_apps,
                              String language_feature_id, boolean show_demo_vids,
                              boolean use_placement, boolean record_audio) {

        this.config_version = config_version;
        this.language_override = language_override;
        this.show_tutorversion = show_tutorversion;
        this.show_debug_launcher = show_debug_launcher;
        this.language_switcher = language_switcher;
        this.no_asr_apps = no_asr_apps;
        this.language_feature_id = language_feature_id;
        this.show_demo_vids = show_demo_vids;
        this.use_placement = use_placement;
        this.record_audio = record_audio;
    }

    public void setDefaults() {
        // use the swahili versions as default
        config_version = "release_sw";
        language_override = true;
        show_tutorversion = true;
        show_debug_launcher = false;
        language_switcher = false;
        no_asr_apps = false;
        language_feature_id = "LANG_SW";
        show_demo_vids = true;
        use_placement = true;
        record_audio = false;
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, ConfigurationClassMap.classMap, scope);
    }
}
