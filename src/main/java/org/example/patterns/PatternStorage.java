package org.example.patterns;

public class PatternStorage {
    static {
        aw4c_accountclientserver_rel_pattern = Pattern.parsePatternM(
                "80 E2 40 F9 2C BA F3 97 A0 1D 00 36 08 06 82 52 76 02 08 8B",
                "xx ?? xx xx ?? ?? ?? xx ?? ?? xx xx xx xx xx xx xx xx xx xx"
        );
    }
    public static final Pattern aw4c_accountclientserver_rel_pattern;
}
