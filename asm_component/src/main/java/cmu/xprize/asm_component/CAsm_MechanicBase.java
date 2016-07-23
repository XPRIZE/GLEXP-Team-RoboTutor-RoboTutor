package cmu.xprize.asm_component;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 *
 */
public class CAsm_MechanicBase implements IDotMechanics {

    protected ArrayList<CAsm_Alley> allAlleys;
    protected CAsm_Component parent;
    protected String operation = "";

    static final String TAG = "CAsm_MechanicBase";

    protected void init(CAsm_Component parent) {

        this.parent = parent;
        this.allAlleys = parent.allAlleys;

    }

    public void preClickSetup() {

    }

    public void handleClick() {

    }


    public String getOperation() {
        return operation;
    }

    // TODO: fix this function - copied from stack overflow
    public static void setAllParentsClip(View v, boolean enabled) {
        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }


}
