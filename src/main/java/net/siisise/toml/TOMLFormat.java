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
package net.siisise.toml;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.ContentBind;

/**
 * https://toml.io/ja/v1.0.0
 */
public class TOMLFormat implements ContentBind<String> {

    @Override
    public String contentType() {
        return "application/toml";
    }

    @Override
    public String nullFormat() {
        return null;
    }

    /**
     * boolean value. true または false
     *
     * @param bool
     * @return true | false
     */
    @Override
    public String booleanFormat(boolean bool) {
        return Boolean.toString(bool);
    }

    @Override
    public String numberFormat(Number num) {
        if (num instanceof Float) {
            num = ((Float) num).doubleValue();
        }
        if (num instanceof Double) {
            Double d = (Double) num;
            if (d.equals(Double.NEGATIVE_INFINITY)) {
                return "-inf";
            } else if (d.equals(Double.POSITIVE_INFINITY)) {
                return "+inf";
            } else if (d.isNaN()) {
                return "nan";
            }
        }
        return num.toString();
    }

    /**
     * String
     *
     * @param str 文字列
     * @return TOML value
     */
    @Override
    public String stringFormat(String str) {
        String e = str.codePoints().mapToObj(ch -> {
            switch (ch) {
                case 0x08:
                    return "\\b";
                case 0x09:
                    return "\\t";
                case 0x0a:
                    return "\\n";
                case 0x0c:
                    return "\\f";
                case 0x0d:
                    return "\\r";
                case 0x22:
                    return "\\\"";
                case 0x5c:
                    return "\\\\";
                default:
                    if (ch < 0x20 || ch == 0x7f) {
                        String hex = "000" + Integer.toHexString(ch);
                        return "\\u" + hex.substring(hex.length() - 4);
                    } else {
                        return Character.toString(ch);
                    }
            }
        }).collect(java.util.stream.Collectors.joining());
        return "\"" + e + "\"";
    }

    /**
     * Map. key null 不可
     *
     * @param map
     * @return
     */
    @Override
    public String mapFormat(Map map) {
        if (map.isEmpty()) {
            return "";
        }
        return tab("", map);
    }

    String tab(String pre, Map map) {
        return ((Map<?, ?>) map).entrySet().parallelStream()
                .map(e -> {
                    String key = encKey(e.getKey());
                    Object o = e.getValue();
                    return tab(pre + key, o);
                })
                .collect(Collectors.joining("\r\n"));
        /*
        if (map.isEmpty()) {
            return "[]";
        }
        return (String) map.stream().map(v -> tab(v))
                .collect(Collectors.joining("\r\n-", "-", ""));
         */
    }
    
    /**
     *
     * @param k null 不可
     * @return 文字列のようなもの
     */
    private String encKey(Object k) {
        if (!(k instanceof CharSequence)) {
            k = (String) Rebind.valueOf(k, this);
        }
        String s = (String)k;
        if ( TOMLReg.unquotedKey.eq(s)) {
            return s;
        }
        return stringFormat(s);
    }

    String tab(String pre, Object val) {
        return pre + " = " + Rebind.valueOf(val, this) + "\r\n";
    }
    
    /**
     * 配列内で1行に収めるMap.
     * 
     * @param map
     * @return 
     */
    String oneMap(Map map) {
        return ((Map<?,?>)map).entrySet().stream()
                .map(e -> {
                    String key = encKey(e.getKey());
                    Object o = e.getValue();
                    return one(key, o);
                }).collect(Collectors.joining(", "));
    }
    
    String one(String key, Object val) {
        return key + " = " + Rebind.valueOf(val, this);
    }

    /**
     * 配列.
     *
     * @param col
     * @return
     */
    @Override
    public String collectionFormat(Collection col) {
        return (String) col.stream().map(v -> {
            if (v instanceof Map) {
                return (String)oneMap((Map)v);
            }
            return (String)Rebind.valueOf(v, this);
        }).collect(Collectors.joining(", ", "[ ", " ]"));
    }

}
