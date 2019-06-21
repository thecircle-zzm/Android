package nl.thecirclezzm.seechangecamera.ui.login.model;

import java.security.MessageDigest;

public class sha256 {
    public static byte[] encryptSha256(byte[] data) throws Exception{
        MessageDigest md5 = MessageDigest.getInstance("sha256");
        md5.update(data);
        return md5.digest();
    }
}