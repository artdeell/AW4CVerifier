package org.example.patterns;

public class PatternBufferStorage {
    public final byte[] returnBuffer, rngBuffer, secondaryRngBuffer;
    public PatternBufferStorage(int returnLength, int rngLength) {
        returnBuffer = new byte[returnLength];
        rngBuffer = new byte[rngLength];
        secondaryRngBuffer = new byte[rngLength];
    }
}
