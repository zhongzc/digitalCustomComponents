package de.neemann.digiblock.core;

import de.neemann.digiblock.core.element.Key;

public class LatticeKeys {
    /**
     * 设置模式为NORMAL
     */
    public static final Key<Boolean> IS_NORMAL
            = new Key<>("isNormal", true).setSecondary();
    /**
     * 设置模式为read_before_write
     */
    public static final Key<Boolean> IS_READ_BEFORE_WRITE
            = new Key<>("isReadBeforeWrite", false).setSecondary();

    /**
     * 设置模式为write_through
     */
    public static final Key<Boolean> IS_WRITE_THROUGH
            = new Key<>("isWriteThrough", false).setSecondary();

    /**
     * 设置是否有输出锁存
     */
    public static final Key<Boolean> WITH_OUTPUT_REG
            = new Key<>("withOutputReg", false).setSecondary();

    /**
     * output format for numbers
     */
    public static final Key<IntFormat> INT_FORMAT
            = new Key.KeyEnum<>("intFormat", IntFormat.def, IntFormat.values()).setSecondary();
}
