package cmu.xprize.robotutor.tutorengine.graph;


import java.util.HashMap;
import java.util.List;

public interface IGraphEventSource {

    public boolean isGraphEventSource();
    public void addEventListener(String listener);
    public void addEventListener(IGraphEventSink listener);
    public void dispatchEvent(IGraphEvent event);
}
