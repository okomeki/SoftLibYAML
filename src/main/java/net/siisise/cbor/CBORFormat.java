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
import java.util.Map;
import net.siisise.bind.Rebind;
import net.siisise.bind.format.ContentBind;
import net.siisise.io.Packet;
import net.siisise.io.PacketA;
import net.siisise.lang.Bin;

/**
 * CBOR出力
 */
public class CBORFormat implements ContentBind<Packet> {

    @Override
    public String contentType() {
        return "application/cbor";
    }

    /**
     * 
     * @param major コマンド
     * @param len 符号なしで処理する
     * @return パケット
     */
    private Packet cmd(int major, long len) {
        Packet pac = new PacketA();
        major <<= 5;
        if ( len < 0 || len >= 0x100000000l) {
            pac.write(major | 27);
            pac.write(Bin.toByte(len));
        } else if ( len < 24) {
            pac.write(major | (int)len);
        } else if ( len < 256 ) {
            pac.write(major | 24);
            pac.write((int)len);
        } else if ( len < 0x10000 ) {
            pac.write(major | 25);
            pac.write(Bin.toByte((short)len));
        } else {
            pac.write(major | 26);
            pac.write(Bin.toByte((int)len));
        }
        return pac;
    }
    
    private Packet tag(long tag) {
        return cmd(6,tag);
    }
    
    @Override
    public Packet nullFormat() {
        return cmd(7, 0x16);
    }

    @Override
    public Packet booleanFormat(boolean bool) {
        return cmd( 7, bool ? 0x15 : 0x14);
    }
    
//    static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger LONG_MAX = BigInteger.ONE.shiftLeft(1).pow(64);

    @Override
    public Packet numberFormat(Number num) {
        if ( num instanceof BigInteger ) {
            BigInteger bi = (BigInteger)num;
            Packet pac;
            int flag = ( bi.signum() < 0 ) ? 1 : 0;
            if ( flag != 0 ) {
                bi = bi.add(BigInteger.ONE).negate();
            }
            if ( bi.compareTo(LONG_MAX) < 0 ) {
                return cmd(flag, bi.longValue()); // 符号なしで収まる
            }
            pac = tag(2 + flag);
            pac.write(arrayFormat(bi.toByteArray()));
            return pac;
        }
        if ( num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte ) {
            long l = num.longValue();
            if ( l >= 0 ) {
                return cmd(0,l);
            } else {
                return cmd( 1, -l -1);
            }
            
        }
        if ( num instanceof Float ) {
            Packet pac = cmd( 7, 26);
            pac.dwrite(Bin.toByte(Float.floatToRawIntBits((Float)num)));
            return pac;
        }
        if ( num instanceof Double ) {
            Packet pac = cmd( 7, 27);
            pac.dwrite(Bin.toByte(Double.doubleToRawLongBits((Double)num)));
            return pac;
        }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Packet stringFormat(String str) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        Packet pac = cmd(3, data.length);
        pac.write(data);
        return pac;
    }

    @Override
    public Packet mapFormat(Map map) {
        Packet pac = new PacketA();
        for ( Object es : map.entrySet() ) {
            Map.Entry e = (Map.Entry)es;
            pac.write(Rebind.valueOf(e.getKey(), this));
            pac.write(Rebind.valueOf(e.getValue(), this));
        }
        pac.backWrite(cmd(5,pac.length()));
        return pac;
    }

    @Override
    public Packet collectionFormat(Collection col) {
        Packet pac = (Packet) col.parallelStream().map(v -> { return (Packet)Rebind.valueOf(v, this); }).collect(PacketA::new,
                (a, b) -> { ((Packet)a).write((Packet)b); },
                (c, d) -> { ((Packet)c).write((Packet)d); } );
        pac.backWrite(cmd(4,pac.length()));
        return pac;
    }

    /**
     * エポックタイムからの秒数.
     * ミリ秒は切り捨て.
     * 
     * @param cal Java基準 ミリ秒 
     * @return 
     */
    @Override
    public Packet datetimeFormat(Calendar cal) {
        Packet pac = tag(1);
        pac.write(numberFormat(cal.getTimeInMillis() / 1000));
        return pac;
    }

    @Override
    public Packet byteArrayFormat(byte[] data) {
        Packet pac = cmd(2, data.length);
        pac.write(data);
        return pac;
    }
    
    @Override
    public Packet charArrayFormat(char[] chars) {
        String s = String.valueOf(chars);
        return stringFormat(s);
    }
}
