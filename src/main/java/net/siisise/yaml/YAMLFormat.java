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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.ContentBind;
import net.siisise.bind.format.JavaFormat;
import net.siisise.block.ReadableBlock;
import net.siisise.lang.Binary16;
import net.siisise.lang.CodePoint;

/**
 * YAML 出力用.
 * まだアルファ版
 */
public class YAMLFormat implements ContentBind<String> {
    
    JavaFormat jf = new JavaFormat();
    
    public YAMLFormat() {
    }

    @Override
    public String contentType() {
        return "application/x-yaml";
    }

    @Override
    public String nullFormat() {
        return "null";
    }

    /**
     * booleanはjsonとおなじ
     * @param bool
     * @return 
     */
    @Override
    public String booleanFormat(boolean bool) {
        return Boolean.toString(bool);
    }

    /**
     * 数値のYAML化
     * @param num 数値
     * @return 
     */
    @Override
    public String numberFormat(Number num) {
        if ( num instanceof Binary16 ) {
            num = ((Binary16)num).floatValue();
        }
        if ( num instanceof Float ) {
            Float f = (Float)num;
            if ( f.isNaN()) {
                return ".nan";
            } else if ( f.isInfinite() ) {
                return ".inf";
            }
        } else if ( num instanceof Double ) {
            Double d = (Double)num;
            if ( d.isNaN()) {
                return ".nan";
            } else if ( d.isInfinite() ) {
                return ".inf";
            }
        }
        return num.toString();
    }

    private static String esc(String val) {
        StringBuilder sb = new StringBuilder();
        ReadableBlock pac = ReadableBlock.wrap(val);
        int ch;
        while (pac.length() > 0) {
            ch = CodePoint.utf8(pac);
            switch (ch) {
                case 0x22: // quotation mark " *必須
                case 0x2f: // solidus /
                case 0x5c: // reverse solidus \ *必須
                    sb.append((char) 0x5c);
                    sb.append((char) ch);
                    break;
                case 0x08: // backspace \b
                    sb.append((char) 0x5c);
                    sb.append((char) 0x62);
                    break;
                case 0x0c: // form feed \f
                    sb.append((char) 0x5c);
                    sb.append((char) 0x66);
                    break;
                case 0x0a: // line feed \n
                    sb.append((char) 0x5c);
                    sb.append((char) 0x6e);
                    break;
                case 0x0d: // carriage return \r
                    sb.append((char) 0x5c);
                    sb.append((char) 0x72);
                    break;
                case 0x09: // tab \t
                    sb.append((char) 0x5c);
                    sb.append((char) 0x74);
                    break;
                default:
                    /* if ( ch > 0xffff) {
                        char[] l = Character.toChars(ch);
                        String a = Integer.toHexString(0x10000 + l[0]).substring(1);
                        String b = Integer.toHexString(0x10000 + l[0]).substring(1);
                        sb.append((char)0x5c);
                        sb.append((char)0x75);
                        sb.append(a);
                        sb.append((char)0x5c);
                        sb.append((char)0x75);
                        sb.append(b);
                    } else */ if (ch < 0x20) { // escape 必須
                        String a = Integer.toHexString(0x10000 + ch).substring(1);
                        sb.append((char) 0x5c);
                        sb.append((char) 0x75);
                        sb.append(a);
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }
    
    /**
     * 最後の改行が1つのとき
     * @param src
     * @return 
     */
    static String mlYAMLString(String src) {
        if (  src.endsWith("\r\n\r\n")) {
            return "|+\r\n" + src;
        }
        return "|\r\n" + src;
    }


    /**
     * JSON風エスケープしておけば大丈夫か
     * @param str
     * @return 
     */
    static String escJSON(String str) {
        return "\"" + esc(str) + "\"";
    }

    static List<String> RES = Arrays.asList(new String[] {"true","false","yes","no","on","off","null"});
    static char[] RESCODE = {'#','{','[',':',','};
            

    /**
     * ToDo: まだ
     * @param src
     * @return 
     */
    static String escYAML(String src) {
        // reserved 予約語のとき
        if ( RES.contains(src) || src.isEmpty()) {
            return "'" + src + "'";
        }
        for (char ch : RESCODE ) {
            if ( src.contains(String.valueOf(ch)) ) {
                return escJSON(src);
            }
        }
        // 1文字目 改行 または | のとき
        int ch = src.charAt(0);
        if ( ch == 0x0a || ch == 0x0d || src.startsWith("|\r\n")) {
            return mlYAMLString(src);
        } else if ( src.indexOf('"') >= 0) { // 要escape ?
            return mlYAMLString(src);
        }
        if ( src.contains("\r\n")) { // 改行
            return mlYAMLString(src);
        }
        return src;
    }

    /**
     * 文字列のYAML化
     * ToDo: エスケープはあとでしらべる 今はJSONのフルエスケープ
     * @param str
     * @return YAML な文字列
     */
    @Override
    public String stringFormat(String str) {
        return escYAML(str);
//        return escJSON(str.toString());
//        return "\"" + esc(str.toString()) + "\"";
    }

    @Override
    public String datetimeFormat(Calendar cal) {
        long datetime = cal.getTimeInMillis();
        cal.setTimeInMillis(datetime);
        SimpleDateFormat rfc3339format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        rfc3339format.setCalendar(cal);
//        rfc3339format.setTimeZone(TimeZone.getTimeZone("Z"));
        String dfstr = rfc3339format.format(new Date(datetime));
        return dfstr;
    }
/*
    @Override
    public String byteArrayFormat(byte[] bytes) {
        BASE64 b64 = new BASE64(BASE64.URL, 0);
        return stringFormat(b64.encode(bytes));
    }
*/
    /**
     * 連想配列.
     * 空の連想配列は存在しない方がいい?
     * @param obj
     * @return 
     */
    @Override
    public String mapFormat(Map obj) {
        if ( obj.isEmpty() ) return "{}";
        return ((Map<?,?>)obj).entrySet().parallelStream()
                .map(e -> {return Rebind.valueOf(e.getKey(), this) + ":" + tab(e.getValue());})
                .collect(Collectors.joining("\r\n"));
    }

    /**
     * リスト.
     * 空のリストは存在しない方がいい?
     * @param col
     * @return 
     */
    @Override
    public String collectionFormat(Collection col) {
        if ( col.isEmpty() ) { return "[]"; }
        return (String) col.stream().map(v -> tab(v))
                .collect(Collectors.joining("\r\n-", "-", ""));
    }

    String tab(Object v) {
        Object jo = Rebind.valueOf(v, jf);
        String s = Rebind.valueOf(v, this);
        if ( jo instanceof Map || jo instanceof Collection ) {
            return "\r\n  " + s.replace("\r\n", "\r\n  ");
        }
        Class c = jo.getClass();
        if ( c.isArray() ) {
            Class ct = c.getComponentType();
            if ( !(ct == Byte.TYPE) && !(ct == Character.TYPE) ) {
                return "\r\n  " + s.replace("\r\n", "\r\n  ");
            }
        }
        return " " + s;
    }
}
