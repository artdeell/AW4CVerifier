package org.example.patterns;

import java.util.Random;

public class Pattern {
    public static final Random patternRandom = new Random();
    private final ThreadLocal<PatternBufferStorage> localEncodeBuffers = new ThreadLocal<>();
    protected final byte[] pattern_bytes;
    private final byte[] mask_bytes;
    private final int bufferLength;
    private final int middle;

    public static Pattern parsePatternM(String manual_chars, String manual_marks) {
        String[] manual_char_array = manual_chars.split(" ");
        String[] manual_mark_array = manual_marks.split(" ");
        byte[] patternBytes = new byte[manual_char_array.length],
                maskBytes = new byte[manual_mark_array.length];
        for(int i = 0; i < patternBytes.length; i++) {
            patternBytes[i] = (byte)(Integer.parseInt(manual_char_array[i], 16) & 0xFF);
            maskBytes[i] = (byte) (manual_mark_array[i].charAt(0) == 'x' ? 1 : 0);
        }
        return new Pattern(patternBytes, maskBytes);
    }

    public static Pattern parsePattern(String classic_chars, String classic_marks) {
        byte[] patternBytes = new byte[classic_chars.length()/4],
                maskBytes = new byte[classic_marks.length()];
        for(int i = 0; i < patternBytes.length; i++) {
            patternBytes[i] = (byte)(Integer.parseInt(classic_chars.substring(i*4+2, i*4+4), 16) & 0xFF);
            maskBytes[i] = (byte) (classic_marks.charAt(i) == 'x' ? 1 : 0);
        }
        return new Pattern(patternBytes, maskBytes);
    }


    public Pattern(byte[] patternBytes, byte[] maskBytes) {
        assert patternBytes.length == maskBytes.length;
        byte[] random_buffer = new byte[patternBytes.length];
        patternRandom.nextBytes(random_buffer);
        pattern_bytes = patternBytes;
        mask_bytes = maskBytes;
        bufferLength = patternBytes.length + maskBytes.length + 1;
        middle = patternBytes.length;
    }

    public byte[] getRollingBuffer() {
        PatternBufferStorage patternBufferStorage = getLocalBuffers();
        byte[] rngBuffer = patternBufferStorage.rngBuffer;
        byte[] secondaryRngBuffer = patternBufferStorage.secondaryRngBuffer;
        byte[] outputBuffer = patternBufferStorage.returnBuffer;
        patternRandom.nextBytes(rngBuffer);
        patternRandom.nextBytes(secondaryRngBuffer);
        byte key = patternBufferStorage.rngBuffer[0];
        for(int i = 0; i < middle; i++) {
            byte mask_byte = mask_bytes[i];
            byte rng_byte = rngBuffer[1+i];
            if((rng_byte & 0x1) != (mask_byte & 0x1)) rng_byte ^= 0x01;
            int mask_offset = i + middle + 1;
            // Allows us to roll the key without two for loops
            outputBuffer[mask_offset] = (byte)(rng_byte ^ (byte)((key + mask_offset)&0xFF));

            if((mask_bytes[i] & 0x1) != 0) {
                outputBuffer[i] = (byte)(pattern_bytes[i] ^ (byte)((key + i)&0xFF));
            }else{
                // Yes, we will read garbage on the client, but the value will be ignored if mask&0x1 != 1 anyway
                // This will throw off the rev-engies though, since all the rhyme and reason in a lot of values will disappear
                outputBuffer[i] = secondaryRngBuffer[i];
            }
        }
        outputBuffer[middle] = key;
        return outputBuffer;
    }

    private PatternBufferStorage getLocalBuffers() {
        PatternBufferStorage buffer = localEncodeBuffers.get();
        if(buffer == null) {
            buffer = new PatternBufferStorage(bufferLength, middle + 1);
            localEncodeBuffers.set(buffer);
        }
        return buffer;
    }
}
