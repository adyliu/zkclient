package com.github.zkclient;

/**
 * @author adyliu (imxylz@gmail.com)
 * @since 1.1
 */
class DataWithVersion {

    public final byte[] data;

    public final int version;

    public DataWithVersion(byte[] data, int version) {
        super();
        this.data = data;
        this.version = version;
    }
}
