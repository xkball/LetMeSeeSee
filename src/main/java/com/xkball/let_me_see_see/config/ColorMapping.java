package com.xkball.let_me_see_see.config;

public enum ColorMapping {
    ERROR(0xF75464),
    KEY_WORDS(0xCF8E6D),
    CLASS_DEF(0xF5C08F),
    METHOD_DEF(0x57AAF7),
    METHOD_CALL(0xE3EEC2),
    NUMBER_LITERAL(0x2AACB8),
    STRING_LITERAL(0x6AAB73),
    FIELD(0x7CE8BB),
    STATIC_FIELD(0xCD6EC0),
    ANNOTATION(0xB3AE60);
    public final int defaultColor;
    public int color;
    
    ColorMapping(int defaultColor) {
        this.defaultColor = defaultColor;
        this.color = defaultColor;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
}
