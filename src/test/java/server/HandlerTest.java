package server;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.Assert.*;

public class HandlerTest {
    private String sha_1(String source) {
        StringBuilder result = new StringBuilder();
        String res = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(source.getBytes(StandardCharsets.UTF_8), 0, source.length());
            byte[] bytes = md.digest();
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                String s = Integer.toHexString(0xff & b);
                s = (s.length() == 1) ? "0" + s : s;

                result.append(s);
            }
//            res = new String(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private String base64(String source) {
        String result = null;
        byte[] bytes = Base64.getEncoder().encode(sha_1("test").getBytes());
        result = new String(bytes);

        return result;
    }

    private String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public String SHA1(String text)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    @Test
    public void testSHA_1() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String encTest = SHA1("test");
        Assert.assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", encTest);

        encTest = convertToHex(DigestUtils.sha1("test".getBytes()));
        Assert.assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", encTest);

        encTest = DigestUtils.sha1Hex("test");
        Assert.assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", encTest);
    }

    @Test
    public void testBase64() {
        String encTest = Base64.getEncoder().encodeToString("test".getBytes());
        Assert.assertEquals("dGVzdA==", encTest);
    }

    @Test
    public void testComplex() {
        byte[] sha1 = DigestUtils.sha1("test");
        String base64 = Base64.getEncoder().encodeToString(sha1);

        Assert.assertEquals("qUqP5cyxm6YcTAhz05Hph5gvu9M=", base64);
    }
}