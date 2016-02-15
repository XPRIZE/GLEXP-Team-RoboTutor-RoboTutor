
This pattern is critical to operation of the engine

    private CTutorObjectDelegate mSceneObject;

    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CTutorObjectDelegate(this);
        mSceneObject.init(context, attrs);
    }
