/*
 * Copyright 2023 okome.
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
package net.siisise.yaml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import net.siisise.json.JSON;
import net.siisise.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 仕様は見てないのでエスケープなどはまだてきとう.
 * @author okome
 */
public class YAMLFormatTest {
    
    public YAMLFormatTest() {
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
     * Test of nullFormat method, of class YAMLFormat.
     */
    @Test
    public void testNullFormat() {
        System.out.println("nullFormat");
        YAMLFormat instance = new YAMLFormat();
        String expResult = "null";
        String result = instance.nullFormat();
        assertEquals(expResult, result);
    }

    /**
     * Test of booleanFormat method, of class YAMLFormat.
     */
    @Test
    public void testBooleanFormat() {
        System.out.println("booleanFormat");
        boolean bool = true;
        YAMLFormat instance = new YAMLFormat();
        String expResult = "true";
        String result = instance.booleanFormat(bool);
        assertEquals(expResult, result);
        bool = false;
        expResult = "false";
        result = instance.booleanFormat(bool);
        assertEquals(expResult, result);
    }

    /**
     * Test of numberFormat method, of class YAMLFormat.
     */
    @Test
    public void testNumberFormat() {
        System.out.println("numberFormat");
        Number num = 9;
        YAMLFormat instance = new YAMLFormat();
        String expResult = "9";
        String result = instance.numberFormat(num);
        assertEquals(expResult, result);
    }

    /**
     * Test of mlYAMLString method, of class YAMLFormat.
     */
    @Test
    public void testMlYAMLString() {
        System.out.println("mlYAMLString");
        String src = "か\\んんん";
        String expResult = "|\r\nか\\んんん";
        String result = YAMLFormat.mlYAMLString(src);
        assertEquals(expResult, result);
    }

    /**
     * Test of escJSON method, of class YAMLFormat.
     */
    @Test
    public void testEscJSON() {
        System.out.println("escJSON");
        String str = "\"";
        String expResult = "\"\\\"\"";
        String result = YAMLFormat.escJSON(str);
        assertEquals(expResult, result);
    }

    /**
     * Test of escYAML method, of class YAMLFormat.
     */
    @Test
    public void testEscYAML() {
        System.out.println("escYAML");
        String src = "";
        String expResult = "''";
        String result = YAMLFormat.escYAML(src);
        assertEquals(expResult, result);
    }

    /**
     * Test of stringFormat method, of class YAMLFormat.
     */
    @Test
    public void testStringFormat() {
        System.out.println("stringFormat");
        String str = "か\\んんん";
        YAMLFormat instance = new YAMLFormat();
        String expResult = "か\\んんん";
        String result = instance.stringFormat(str);
        assertEquals(expResult, result);
        
        str = "";
        expResult = "''";
        result = instance.stringFormat(str);
        assertEquals(expResult, result);
    }

    /**
     * Test of datetimeFormat method, of class YAMLFormat.
     */
    @Test
    public void testDatetimeFormat() {
        System.out.println("datetimeFormat");
        long datetime = 1690618830000L;
        YAMLFormat instance = new YAMLFormat();
        String expResult = "2023-07-29T08:20:30Z";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(datetime);
        String result = instance.datetimeFormat(cal);
        assertEquals(expResult, result);
    }

    /**
     * Test of arrayFormat method, of class YAMLFormat.
     */
    @Test
    public void testArrayFormat() {
        System.out.println("arrayFormat");
        Object array = new byte[0];
        YAMLFormat instance = new YAMLFormat();
        String expResult = "''";
        String result = instance.arrayFormat(array);
        assertEquals(expResult, result);
        
        array = new char[0];
        result = instance.arrayFormat(array);
        assertEquals(expResult, result);
    }

    /**
     * Test of listFormat method, of class YAMLFormat.
     */
    @Test
    public void testListFormat() {
        System.out.println("listFormat");
        String[] src = {"a", "b", ",", "c" };
        List array = (List) JSON.valueOf(src);
        array.add(new String[] {"d","e","f"});
        YAMLFormat instance = new YAMLFormat();
        String expResult = "- a\r\n- b\r\n- \",\"\r\n- c\r\n-\r\n  - d\r\n  - e\r\n  - f";
        String result = instance.listFormat(array);
        assertEquals(expResult, result);
    }

    /**
     * Test of setFormat method, of class YAMLFormat.
     */
    @Test
    public void testSetFormat() {
        System.out.println("setFormat");
        Set set = new HashSet();
        YAMLFormat instance = new YAMLFormat();
        String expResult = "[]";
        String result = instance.setFormat(set);
        assertEquals(expResult, result);
    }

    /**
     * Test of mapFormat method, of class YAMLFormat.
     */
    @Test
    public void testMapFormat() {
        System.out.println("mapFormat");
        Map obj = new HashMap();
        YAMLFormat instance = new YAMLFormat();
        String expResult = "{}";
        String result = instance.mapFormat(obj);
        assertEquals(expResult, result);
        
        obj = new JSONObject();
        obj.put("a","d");
        obj.put("b","e");
        obj.put("c","f");
        expResult = "a: d\r\nb: e\r\nc: f";
        result = instance.mapFormat(obj);
        assertEquals(expResult, result);
    }

    /**
     * Test of collectionFormat method, of class YAMLFormat.
     */
    @Test
    public void testCollectionFormat() {
        System.out.println("collectionFormat");
        Collection col = new ArrayList();
        YAMLFormat instance = new YAMLFormat();
        String expResult = "[]";
        String result = instance.collectionFormat(col);
        assertEquals(expResult, result);
    }

    /**
     * Test of tab method, of class YAMLFormat.
     */
    @Test
    public void testTab() {
        System.out.println("tab");
        Object v = new Object[] {"a","b",new Object[] {"bc"}};

        YAMLFormat instance = new YAMLFormat();
        String expResult = "\r\n  - a\r\n  - b\r\n  -\r\n    - bc";
        String result = instance.tab(v);
        assertEquals(expResult, result);
    }
    
}
