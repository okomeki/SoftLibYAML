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
package net.siisise.cbor;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.ContentBind;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.lang.Bin;
import net.siisise.lang.Binary16;

/**
 * CBOR出力
 * RFC 8949
 */
public class CBORFormat implements ContentBind<Packet> {

    @Override
    public String contentType() {
        return "application/cbor";
    }

    /**
     * コマンド組み.
     *
     * @param major コマンド
     * @param len 符号なしで処理するデータ長 またはデータ数
     * @return パケット
     */
    Packet cmd(int major, long len) {
        Packet pac = new PacketA();
        major <<= 5;
        if (len < 0 || len >= 0x100000000l) {
            pac.write(major | 27);
            pac.write(Bin.toByte(len));
        } else if (len < 24) {
            pac.write(major | (int) len);
        } else if (len < 256) {
            pac.write(major | 24);
            pac.write((int) len);
        } else if (len < 0x10000) {
            pac.write(major | 25);
            pac.write(Bin.toByte((short) len));
        } else {
            pac.write(major | 26);
            pac.write(Bin.toByte((int) len));
        }
        return pac;
    }

    /**
     * major7系コマンド.
     * 長さは同じだが判定基準が違ったので分ける.
     *
     * @param code
     * @return
     */
    Packet major7(int code) {
        Packet pac = new PacketA();
        int t = 0xe0;
        pac.write(t | code);
        return pac;
    }

    /**
     * 拡張タグ タグのみ.
     * null, undefinded 以外のデータの前に複数タグを挿入できるのかも.
     *
     * @param tag 拡張タグ番号
     * @return タグ
     */
    public Packet tag(long tag) {
        return cmd(6, tag);
    }

    @Override
    public Packet nullFormat() {
        return major7(0x16);
    }

    @Override
    public Packet undefinedFormat() {
        return major7(0x17);
    }

    @Override
    public Packet booleanFormat(boolean bool) {
        return major7(bool ? 0x15 : 0x14);
    }

//    static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger LONG_MAX = BigInteger.ONE.shiftLeft(1).pow(64);

    /**
     * 整数 major 0 1、浮動小数点 major 7 拡張 2, 3 BigNum
     *
     * @param num 数値
     * @return
     */
    @Override
    public Packet numberFormat(Number num) {
        if (num instanceof BigInteger) {
            BigInteger bi = (BigInteger) num;
            Packet pac;
            int flag = (bi.signum() < 0) ? 1 : 0;
            if (flag != 0) {
                bi = bi.add(BigInteger.ONE).negate();
            }
            if (bi.compareTo(LONG_MAX) < 0) {
                return cmd(flag, bi.longValue()); // 符号なしで収まる
            }
            pac = tag(2 + flag);
            pac.write(arrayFormat(bi.toByteArray()));
            return pac;
        }
        if (num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte) {
            long l = num.longValue();
            if (l >= 0) {
                return cmd(0, l);
            } else {
                return cmd(1, -l - 1);
            }

        }
        if (num instanceof Double) {
            Double d = (Double) num;
            if (d.equals(Double.valueOf(d.floatValue()))) {
                num = ((Double) num).floatValue();
            } else {
                Packet pac = major7(27);
                pac.dwrite(Bin.toByte(Double.doubleToRawLongBits((Double) num)));
                return pac;
            }
        }
        if (num instanceof Float) {
            Float f = (Float) num;
            Binary16 b16 = Binary16.valueOf(Binary16.FloatToBinary16bits(f.floatValue()));
            if (f.equals(b16.floatValue())) {
                num = Binary16.valueOf(Binary16.FloatToBinary16bits(f));
            } else {
                Packet pac = major7(26);
                pac.dwrite(Bin.toByte(Float.floatToRawIntBits((Float) num)));
                return pac;
            }
        }
        if (num instanceof Binary16) {
            Packet pac = major7(25);
            if (((Binary16) num).binary16Value() == 0x8000) { // -0.0 は非対応?
                num = Binary16.valueOf((short) 0.0);
            }
            // NaN を正規化する?
            pac.dwrite(Bin.toByte(Binary16.binary16ToShortBits(((Binary16) num).binary16Value())));
            return pac;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 文字列 をCBOR text string 変換.
     *
     * @param str 文字列
     * @return CBOR text string
     */
    @Override
    public Packet stringFormat(String str) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        Packet pac = cmd(3, data.length);
        pac.write(data);
        return pac;
    }

    /**
     * major 5 Map系 CBOR map 変換.
     *
     * @param map
     * @return CBOR map
     */
    @Override
    public Packet mapFormat(Map map) {
        Packet pac = new PacketA();
        int size = map.size();
        for (Object es : map.entrySet()) {
            Map.Entry e = (Map.Entry) es;
            pac.write(Rebind.valueOf(e.getKey(), this));
            pac.write(Rebind.valueOf(e.getValue(), this));
        }
        pac.backWrite(cmd(5, size));
        return pac;
    }

    /**
     * major 4 Collection CBOR array 変換.
     *
     * @param col
     * @return CBOR array
     */
    @Override
    public Packet collectionFormat(Collection col) {
        int size = col.size();
        Packet pac = (Packet) col.parallelStream().map(v -> {
            return (Packet) Rebind.valueOf(v, this);
        }).collect(PacketA::new,
                (a, b) -> {
                    ((Packet) a).write((Packet) b);
                },
                (c, d) -> {
                    ((Packet) c).write((Packet) d);
                });
        pac.backWrite(cmd(4, size));
        return pac;
    }

    /**
     * エポックタイムからの秒数を使っておく. 拡張 0 date/time string 拡張 1 Epoch-based date/time
     * (ToDo:とりあえず)ミリ秒は切り捨て.
     *
     * @param cal Java基準 ミリ秒
     * @return CBOR number
     */
    @Override
    public Packet datetimeFormat(Calendar cal) {
        long t = cal.getTimeInMillis();
        int ms = cal.get(Calendar.MILLISECOND);
        Packet pac;
        if (ms == 0) { // ミリ秒なし
            pac = tag(1);
            pac.write(numberFormat(t / 1000));
        } else {
            pac = tag(0);
            java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
            Date date = new Date();
            date.setTime(cal.getTimeInMillis());
            df.format(date);
            pac.write(stringFormat(""));
            throw new UnsupportedOperationException("まだ");
        }
        return pac;
    }

    /**
     * バイト列. byte string.
     *
     * @param data
     * @return CBOR byte string
     */
    @Override
    public Packet byteArrayFormat(byte[] data) {
        Packet pac = cmd(2, data.length);
        pac.write(data);
        return pac;
    }

    /**
     * text string として扱う?
     *
     * @param chars 文字列
     * @return CBOR text string
     */
    @Override
    public Packet charArrayFormat(char[] chars) {
        String s = String.valueOf(chars);
        return stringFormat(s);
    }
}
