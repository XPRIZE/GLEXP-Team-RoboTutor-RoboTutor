package cmu.xprize.robotutor.tutorengine.graph;


import java.util.HashMap;

public class CGraphEvent implements IGraphEvent {

    private HashMap<String, Object> map;
    private String mType;

    public CGraphEvent() {
        map = new HashMap();
    }
    public CGraphEvent(String type) {
        this();
        mType = type;
    }
    public CGraphEvent(String type, String key, Object value) {
        this(type);
        map.put(key, value);
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public Object getString(String key) {
        return map.get(key);
    }

}
