/*
 * Copyright 2024 okome.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.siisise.cbor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.siisise.io.Packet;
import net.siisise.lang.Bin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CBORFormatTest {
    
    public CBORFormatTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of contentType method, of class CBORFormat.
     */
/*
    @Test
    public void testContentType() {
        System.out.println("contentType");
        CBORFormat instance = new CBORFormat();
        String expResult = "";
        String result = instance.contentType();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of nullFormat method, of class CBORFormat.
     */
    @Test
    public void testNullFormat() {
        System.out.println("nullFormat");
        CBORFormat instance = new CBORFormat();
        byte[] expResult = Bin.toByteArray("F6");
        Packet result = instance.nullFormat();
        //result = Rebind.valueOf(null, "application/cbor");
        assertArrayEquals(expResult, result.toByteArray());
        
    }

    /**
     * Test of booleanFormat method, of class CBORFormat.
     */
    @Test
    public void testBooleanFormat() {
        System.out.println("booleanFormat");
        boolean bool = false;
        CBORFormat instance = new CBORFormat();
        byte[] expResult = Bin.toByteArray("F4");
        Packet result = instance.booleanFormat(bool);
        assertArrayEquals(expResult, result.toByteArray());
        // true
        expResult = Bin.toByteArray("F5");
        result = instance.booleanFormat(true);
        assertArrayEquals(expResult, result.toByteArray());
    }

    /**
     * Test of numberFormat method, of class CBORFormat.
     */
    @Test
    public void testNumberFormat() {
        System.out.println("numberFormat");
        Number num = Integer.valueOf(0);
        CBORFormat instance = new CBORFormat();
        byte[] expResult = new byte[] {00};
        Packet result = instance.numberFormat(num);
        assertArrayEquals(expResult, result.toByteArray());

        num = Integer.valueOf(10);
        expResult = new byte[] {0x0a};
        result = instance.numberFormat(num);
        assertArrayEquals(expResult, result.toByteArray());

        num = Integer.valueOf(100);
        expResult = new byte[] {0x18,0x64};
        result = instance.numberFormat(num);
        assertArrayEquals(expResult, result.toByteArray());

        num = Integer.valueOf(-1);
        expResult = new byte[] {0x20};
        result = instance.numberFormat(num);
        assertArrayEquals(expResult, result.toByteArray());
    }
    
    /**
     * 浮動小数点系.
     */
    @Test
    public void testFloat() {
        System.out.println("浮動小数点");
        Number num = Double.valueOf("0.0");
        byte[] expResult = Bin.toByteArray("f90000");
        CBORFormat instance = new CBORFormat();
        Packet result = instance.numberFormat(num);
        byte[] binResult = result.toByteArray();
        System.out.println(Bin.toHex(binResult));
        assertArrayEquals(expResult, binResult);
    } 

    /**
     * Test of stringFormat method, of class CBORFormat.
     */
    @Test
    public void testStringFormat() {
        System.out.println("stringFormat");
        CBORFormat instance = new CBORFormat();
        String str = "abcde";
        byte[] expResult = Bin.toByteArray("656162636465");
        Packet result = instance.stringFormat(str);
        assertArrayEquals(expResult, result.toByteArray());

        str = "z";
        expResult = Bin.toByteArray("617a");
        result = instance.stringFormat(str);
        assertArrayEquals(expResult, result.toByteArray());

        str = "aa";
        expResult = new byte[] { 0x62,0x61,0x61};
        result = instance.stringFormat(str);
        assertArrayEquals(expResult, result.toByteArray());
    }

    /**
     * Test of mapFormat method, of class CBORFormat.
     */
    @Test
    public void testMapFormat() {
        System.out.println("mapFormat");
        Map map = new HashMap();
        map.put(1, "a");
        CBORFormat instance = new CBORFormat();
        byte[] expResult = Bin.toByteArray("A1016161");
        Packet result = instance.mapFormat(map);
        byte[] resultBin = result.toByteArray();
        assertArrayEquals(expResult, resultBin);
    }

    /**
     * Test of collectionFormat method, of class CBORFormat.
     */
    @Test
    public void testCollectionFormat() {
        System.out.println("collectionFormat");
        Collection col = new ArrayList();
        col.add(1);
        col.add("a");
        CBORFormat instance = new CBORFormat();
        byte[] expResult = Bin.toByteArray("82016161");
        Packet result = instance.collectionFormat(col);
        byte[] resultBin = result.toByteArray();
        System.out.println(Bin.toHex(resultBin));
        assertArrayEquals(expResult, resultBin);
        
        col = new ArrayList();
        col.add(100);
        expResult = Bin.toByteArray("811864");
        result = instance.collectionFormat(col);
        resultBin = result.toByteArray();
        System.out.println(Bin.toHex(resultBin));
        assertArrayEquals(expResult, resultBin);
    }

    /**
     * Test of datetimeFormat method, of class CBORFormat.
     */
    @Test
    public void testDatetimeFormat() {
        System.out.println("datetimeFormat");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        //cal.setTimeZone();
        long t = cal.getTimeInMillis() / 1000;
        CBORFormat instance = new CBORFormat();
        Packet expResult = instance.tag(1);
        expResult.write(instance.cmd(0,t));
        Packet result = instance.datetimeFormat(cal);
        assertArrayEquals(expResult.toByteArray(), result.toByteArray());
    }

    /**
     * Test of byteArrayFormat method, of class CBORFormat.
     */
    @Test
    public void testByteArrayFormat() {
        System.out.println("byteArrayFormat");
        byte[] data = new byte[0];
        CBORFormat instance = new CBORFormat();
        byte[] expResult = new byte[] {0x40};
        Packet result = instance.byteArrayFormat(data);
        assertArrayEquals(expResult, result.toByteArray());
        data = Bin.toByteArray("6162636465");
        result = instance.byteArrayFormat(data);
        expResult = Bin.toByteArray("456162636465");
        assertArrayEquals(expResult, result.toByteArray());
        
    }

    /**
     * Test of charArrayFormat method, of class CBORFormat.
     */
    @Test
    public void testCharArrayFormat() {
        System.out.println("charArrayFormat");
        char[] chars = new char[] {};
        CBORFormat instance = new CBORFormat();
        byte[] expResult = {0x60};
        Packet result = instance.charArrayFormat(chars);
        assertArrayEquals(expResult, result.toByteArray());
    }
    
}
