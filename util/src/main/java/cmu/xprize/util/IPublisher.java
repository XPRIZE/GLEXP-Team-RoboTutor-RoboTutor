package cmu.xprize.util;

/**
 * Created by kevin on 10/27/2016.
 */

public interface IPublisher {

    public void publishState();

    public void publishValue(String varName, String value);

    public void publishValue(String varName, int value);

    public void publishFeature(String feature);

    public void retractFeature(String feature);

}
