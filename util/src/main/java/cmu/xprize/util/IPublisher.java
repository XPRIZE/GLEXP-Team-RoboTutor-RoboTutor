package cmu.xprize.util;

import java.util.HashMap;

/**
 * Created by kevin on 10/27/2016.
 */

public interface IPublisher {

    public void publishState();

    public void publishValue(String varName, String value);

    public void publishValue(String varName, int value);

    public void publishFeatureSet(String featureset);

    public void retractFeatureSet(String featureset);

    public void publishFeature(String feature);

    public void retractFeature(String feature);

    public void publishFeatureMap(HashMap featureMap);

    public void retractFeatureMap(HashMap featureMap);

}
