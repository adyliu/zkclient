/**
 * 
 */
package com.github.zkclient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author adyliu(imxylz@gmail.com)
 * @since 2012-12-3
 */
public class ZkClientUtilsTest {

    /**
     * Test method for
     * {@link com.github.zkclient.ZkClientUtils#leadingZeros(long, int)}.
     */
    @Test
    public void testLeadingZeros() {
        assertEquals("99", ZkClientUtils.leadingZeros(99, 1));
        assertEquals("99", ZkClientUtils.leadingZeros(99, 2));
        assertEquals("099", ZkClientUtils.leadingZeros(99, 3));
        assertEquals("0099", ZkClientUtils.leadingZeros(99, 4));
        assertEquals("00099", ZkClientUtils.leadingZeros(99, 5));
        assertEquals("0100", ZkClientUtils.leadingZeros(100, 4));
    }

}
