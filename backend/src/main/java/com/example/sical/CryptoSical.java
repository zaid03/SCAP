package com.example.sical;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class CryptoSical {
    
    // helper for generating a hashed + encoded string
    public static String encodeSha1Base64(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(digest);
    }

    // SHA512 + Base64 (does the same but with 64 bytes)
    public static String encodeSha512Base64(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    // Base64 encode no encryption
    public static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    // Base64 decode (reverse the above one)
    public static String decodeBase64(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.ISO_8859_1);
    }

    // Pad with zeros from the left to match a specific length
    public static String padLeftZeros(String input, int length) {
        return String.format("%1$" + length + "s", input).replace(' ', '0');
    }

    // Convert DB date string into yyyyMMddHHmmss
    public static String encodeDate(String dbDate) throws Exception {
        // expected format: yyyy-MM-dd HH:mm:ss
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = inputFormat.parse(dbDate);
        return outputFormat.format(date);
    }

    // Generate NONCE, TOKEN, CREATED (sent in header to sicalwin)
    public static SecurityFields calculateSecurityFields(String publicKey) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String created = sdf.format(new Date());

        SecureRandom random = new SecureRandom();
        int nonce = random.nextInt(1_000_000_000);
        String nonceStr = String.valueOf(nonce);

        String origin = nonceStr + created + publicKey;
        String token = encodeSha512Base64(origin);

        return new SecurityFields(token, nonceStr, created, origin);
    }

    public static class SecurityFields {
        public final String token;
        public final String nonce;
        public final String created;
        public final String origin;

        public SecurityFields(String token, String nonce, String created, String origin) {
            this.token = token;
            this.nonce = nonce;
            this.created = created;
            this.origin = origin;
        }
    }
}