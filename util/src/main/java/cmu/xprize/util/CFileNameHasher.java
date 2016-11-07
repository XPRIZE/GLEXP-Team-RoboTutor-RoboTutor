package cmu.xprize.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

        System.out.println("filename   :" + filename);

        hashName = filename.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        System.out.println("prunedname :" + hashName);

        byte[] nameBytes = hashName.getBytes( Charset.forName("UTF-8" ));

        nameBytes = md.digest(nameBytes);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nameBytes.length; i++) {
            sb.append(Integer.toString((nameBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("hashname :" + sb.toString());

        return sb.toString();
    }

}
