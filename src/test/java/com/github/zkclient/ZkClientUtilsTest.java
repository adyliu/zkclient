/**
 * 
 */
package com.github.zkclient;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author adyliu(imxylz@gmail.com)
 * @since 2012-12-3
 */
public class ZkClientUtilsTest {

    /**
     * Test method for {@link com.github.zkclient.ZkClientUtils#leadingZeros(long, int)}.
     */
    @Test
    public void testLeadingZeros() {
        assertEquals("0010", ZkClientUtils.leadingZeros(10,4));
        assertEquals("99", ZkClientUtils.leadingZeros(99, 1));
        assertEquals("99", ZkClientUtils.leadingZeros(99, 2));
    }

}
