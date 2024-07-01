/*
 * Copyright 2022 okome.
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

import net.siisise.lang.Binary16;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.siisise.bind.Rebind;
import net.siisise.block.ReadableBlock;
import net.siisise.io.BASE64;
import net.siisise.io.Input;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.lang.Bin;

/**
 * RFC 8949 CBOR. バイナリパック Parse系
 *
 */
public class CBOR {

    /**
     * JavaScriptなどで使われる undefined がないので仮の値
     */
    public static final Object UNDEFINED = new Object();
    public static final Object BREAK = CBOR.class;

    public static Object parse(byte[] src) {
        return parse(ReadableBlock.wrap(src));
    }

    /*
    public static class UnknownLengthException extends RuntimeException {

        private UnknownLengthException(String n) {
            super(n);
        }
    }
     */
    /**
     * 64bitまで可能な長さ.
     *
     * @param in
     * @param code
     * @return -1: 引数なし 0-: 64bit length
     */
    static long parseLen(Input in, int code) {
        long len = 0;
        switch (code & 0x1f) {
            case 24:
                len = in.read();
                break;
            case 25:
                len = in.read();
                len <<= 8;
                len |= in.read();
                break;
            case 26:
                for (int i = 0; i < 4; i++) {
                    len <<= 8;
                    len |= in.read();
                }
                break;
            case 27:
                for (int i = 0; i < 8; i++) {
                    len <<= 8;
                    len |= in.read();
                }
                break;
            case 28:
            case 29:
            case 30:
                throw new UnsupportedOperationException("予約コード");
            case 31: // 引数なし 仮に-1
//                throw new UnknownLengthException("引数なし(仮)");
                len = -1;
                break;
            default: // 0 - 23
                len = code & 0x1f;
                break;

        }
        return len;
    }

    /**
     * 基本型
     *
     * @param in
     * @return 抽出データ
     */
    public static Object parse(Input in) {
        int code = in.read();
        long len = parseLen(in, code);
        switch (code >> 5) {
            case 0: // 符号無し整数
                return parseNumber(len);
            case 1: // 負の整数
                return parseUnSigned(len);
            case 2: // bin 大きいものを扱うときはPacket形式に変えるかも
                return parseBin(len, in);
            case 3: // UTF-8 String Javaで扱えそうな範囲内で
                return parseString(len, in);
            case 4: // 配列/List / Array
                return parseList(len, in);
            case 5: // Map
                return parseMap(len, in);
            case 6: // tag Section 3.4.
                return tag(len, in);
//            case 7: // float / simple / Section 3.3.
            default:
                return other(code & 0x1f, len, in);
        }
    }

    /**
     * 0 正の整数.
     *
     * @param len
     * @return long または BigInteger
     */
    private static Number parseNumber(long len) {
        if (len < 0) {
            return BigInteger.valueOf(len).add(BigInteger.ONE.shiftLeft(64));
        }
        return len;
    }

    /**
     * 1 負の整数.
     *
     * @param len
     * @return long または BigInteger
     */
    private static Number parseUnSigned(long len) {
        if (len < 0) {
            BigInteger b = BigInteger.valueOf(len).add(BigInteger.ONE.shiftLeft(64));
            return b.negate().subtract(BigInteger.ONE);
        }
        return -len - 1;
    }

    /**
     * 2 bin 大きいものを扱うときはPacket形式に変えるかも
     *
     * @param len
     */
    private static byte[] parseBin(long len, Input in) {
        if (len == -1) { // nest
            Packet ret = new PacketA();
            Object v = parse(in);
            while (v != BREAK) {
                ret.dwrite((byte[]) v);
                v = parse(in);
            }
            return ret.toByteArray();
        }
        byte[] bin = new byte[(int) len];
        in.read(bin);
        return bin;
    }

    private static String parseString(long len, Input in) {
        PacketA ret = new PacketA();
        if (len == -1) {
            Object v = parse(in);
            while (v != BREAK) {
                ret.write((byte[]) v);
                v = parse(in);
            }
            return new String(ret.toByteArray(), StandardCharsets.UTF_8);
        } else {
            byte[] str = new byte[(int) len];
            in.read(str);
            return new String(str, StandardCharsets.UTF_8);
        }
    }

    /**
     * List / Array っぽいもの.
     *
     * @param len 数
     * @param in
     * @return List
     */
    private static List parseList(long len, Input in) {
        List list = new ArrayList();
        if (len == -1) {
            Object v = parse(in);
            while (v != BREAK) {
                list.add(v);
                v = parse(in);
            }
        } else {
            for (int i = 0; i < len; i++) {
                list.add(parse(in));
            }
        }
        return list;
    }

    private static Map parseMap(long len, Input in) {
        Map obj = new LinkedHashMap();
        if (len == -1) {
            Object k = parse(in);
            while (k != BREAK) {
                Object val = parse(in);
                obj.put((String) k, val);
                k = parse(in);
            }
        } else {
            for (int i = 0; i < len; i++) {
                String key = (String) parse(in);
                Object val = parse(in);
                obj.put(key, val);
            }
        }
        return obj;
    }

    /**
     * 6 タグ付けされた型
     *
     * Section 3.4. RFC 7049
     *
     * @param tag
     * @param in
     * @return
     */
    static Object tag(long tag, Input in) {
        Object src = parse(in);
        if (tag >= 0 && tag <= Integer.MAX_VALUE) {
            switch ((int) tag) {
                case (int) CBORTag.EXPECTED_CONVERSION_BASE64URL:
                    BASE64 b64url = new BASE64(BASE64.URL, 0);
                    src = b64url.encode((byte[]) src);
                    break;
                case (int) CBORTag.EXPECTED_CONVERSION_BASE64:
                    BASE64 b64 = new BASE64(BASE64.BASE64, 0);
                    src = b64.encode((byte[]) src);
                    break;
                case (int) CBORTag.EXPECTED_CONVERSION_BASE16:
                    src = Bin.toUpperHex((byte[]) src);
                    break;
            }
        }

        return new CBORTag(tag, src);
    }

    /**
     * 7. その他分類.
     *
     * @param code
     * @param len
     * @param in
     * @return
     */
    private static Object other(int code, long len, Input in) {
        switch (code & 0x1f) {
            // 0..23 1 byte
            // 0..19 unassigned
            case 20:  // 20
                return false;
            case 21:  // 21
                return true;
            case 22:  // 22
                return null;
            case 23:  // 23
                return UNDEFINED;
            // 24 - 2 byte
            case 24:
                // 24..31 reserved
                // 32..255 unassigned
                if (len < 24) {
                    throw new java.lang.IllegalStateException(); // ない
                } else if (len < 32) {
                    throw new UnsupportedOperationException("reserved");
                } else {
                    throw new UnsupportedOperationException("unassigned");
                }
            case 25: // IEEE 754 16bit float Appendix D
                return Binary16.binary16BitsToFloat((short) len);
            case 26: // IEEE 754 Single-Percision Float (32 bits follow) 
                return Float.intBitsToFloat((int) len);
            case 27: // IEEE 754 Single-Percision Float (64 bits follow)
                return Double.longBitsToDouble(len);
            case 0x1c: // 28
            case 0x1d: // 29
            case 0x1e: // 30
                throw new UnsupportedOperationException("reserved");
            case 0x1f: // 31
                return BREAK;
            default: // 0 ... 19
                throw new UnsupportedOperationException("unassigned");
        }

    }

    public static byte[] build(Object obj) {
        return Rebind.valueOf(obj, CBOR.class);
    }
}
