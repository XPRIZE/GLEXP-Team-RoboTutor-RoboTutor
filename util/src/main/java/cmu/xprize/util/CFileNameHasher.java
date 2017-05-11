package cmu.xprize.util;

import android.util.Log;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static cmu.xprize.util.TCONST.GRAPH_MSG;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;

/**
 * Created by kevin on 11/7/2016.
 */
public class CFileNameHasher {

    private static CFileNameHasher ourInstance = new CFileNameHasher();

    public static CFileNameHasher getInstance() {
        return ourInstance;
    }

    static private MessageDigest md;

    private CFileNameHasher() {

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public String generateHash(String filename) {

        String          hashName;
        String          prunedName;

        prunedName = filename.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        byte[] nameBytes = prunedName.getBytes( Charset.forName("UTF-8" ));

        nameBytes = md.digest(nameBytes);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nameBytes.length; i++) {
            sb.append(Integer.toString((nameBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        Log.v(GRAPH_MSG, "target:CFileNameHasher,action:generatehash,filename:" + filename + " prunedname:" + prunedName + " hashname:" + sb.toString());

        return sb.toString();
    }

}
