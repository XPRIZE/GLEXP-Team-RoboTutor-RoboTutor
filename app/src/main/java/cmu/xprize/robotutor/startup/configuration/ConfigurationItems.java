package cmu.xprize.robotutor.startup.configuration;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class ConfigurationItems implements ILoadableObject {

    public static final String CONFIG_VERSION = "CONFIG_VERSION";
    public static final String LANGUAGE_OVERRIDE = "LANGUAGE_OVERRIDE";
    public static final String SHOW_TUTOR_VERSION = "SHOW_TUTOR_VERSION";
    public static final String SHOW_DEBUG_LAUNCHER = "SHOW_DEBUG_LAUNCHER";
    public static final String LANGUAGE_SWITCHER = "LANGUAGE_SWITCHER";
    public static final String NO_ASR_APPS = "NO_ASR_APPS";
    public static final String LANGUAGE_FEATURE_ID = "LANGUAGE_FEATURE_ID";
    public static final String SHOW_DEMO_VIDS = "SHOW_DEMO_VIDS";

    public String config_version;
    public boolean language_override;
    public boolean show_tutorversion;
    public boolean show_debug_launcher;
    public boolean language_switcher;
    public boolean no_asr_apps;
    public String language_feature_id;
    public boolean show_demo_vids;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, ConfigurationClassMap.classMap, scope);
    }
}
