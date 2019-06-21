package cmu.xprize.robotutor.startup.configuration;

/**
 * RoboTutor
 * <p>Similar to QuickDebugTutor, this class offers the developer a way to store multiple pre-set configuration
 * option sets without changing the config file.</p>
 * Created by kevindeland on 5/9/19.
 */

public class ConfigurationQuickOptions {


    // Both SW and EN versions, and they both have the debugger menu.
    public static ConfigurationItems DEBUG_SW_EN = new ConfigurationItems(
            "debug_sw_en",
            false,
            true,
            true,
            true,
            false,
            "LANG_NULL",
            false,
            false,
            false
    );

    // EN version, and they both have the debugger menu.
    public static ConfigurationItems DEBUG_EN = new ConfigurationItems(
            "debug_en",
            true,
            true,
            true,
            false,
            false,
            "LANG_EN",
            false,
            false,
            false
    );
}
