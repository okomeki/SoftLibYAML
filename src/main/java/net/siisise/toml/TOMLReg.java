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

import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;
import net.siisise.abnf.parser5234.ABNF5234;

/**
 *
 */
public class TOMLReg {
    static final ABNFReg REG = new ABNFReg(ABNF5234.BASE);
    
    // whitespace
    static final ABNF wschar = REG.rule("wschar", ABNF.bin(0x20).or(ABNF.bin(0x09)));
    // newline
    static final ABNF newline = REG.rule("newline", ABNF.bin(new byte[] {0x0d,0x0a}).or1(ABNF.bin(0x0a)));
    static final ABNF ws = REG.rule("ws", wschar.x());
    static final ABNF dotSep = REG.rule("dot-sep", ws.pl(ABNF.bin(0x2e), ws));
    static final ABNF nonAscii = REG.rule("non-ascii", ABNF.range(0x80, 0xd7ff).or1(ABNF.range(0xe000,0x10ffff)));

    static final ABNF escape = REG.rule("escape", ABNF.bin(0x5c));
    static final ABNF escapeSeqChar = REG.rule("escape-seq-char", ABNF.binlist("=\\bfnrt").or(ABNF.bin('u').pl(ABNF5234.HEXDIG.x(4), ABNF.bin(0x55).pl(ABNF5234.HEXDIG.x(8)))));
    static final ABNF escaped = REG.rule("escaped", escape.pl(escapeSeqChar));
    static final ABNF basicUnescaped = REG.rule("basic-unescaped", wschar.or1(ABNF.bin(0x21), ABNF.range(0x23, 0x5b), ABNF.range(0x5d, 0x7e), nonAscii));
    static final ABNF basicChar = REG.rule("basic-char", basicUnescaped.or(escaped));
    static final ABNF quotationMark = REG.rule("quotation-mark", ABNF.bin(0x22));
    static final ABNF basicString = REG.rule("basic-string", quotationMark.pl(basicChar.x(), quotationMark));
    static final ABNF apostrophe = REG.rule("apostrophe", ABNF.bin(0x27));
    static final ABNF literalChar = REG.rule("literal-char", ABNF.bin(0x09).or1(ABNF.range(0x20,0x26), ABNF.range(0x28, 0x7e), nonAscii));
    static final ABNF literalString = REG.rule("literal-string", apostrophe.pl(literalChar, apostrophe));
    static final ABNF unquotedKey = REG.rule("unquoted-key", ABNF5234.ALPHA.or1(ABNF5234.DIGIT, ABNF.bin(0x2d), ABNF.bin(0x5f)).ix());
    static final ABNF quotedKey = REG.rule("quoted-key", basicString.or1(literalString));
    static final ABNF simpleKey = REG.rule("simple-key", quotedKey.or1(unquotedKey));
    static final ABNF dottedKey = REG.rule("dotted-key", simpleKey.pl(dotSep.pl(simpleKey).ix()));
    static final ABNF key = REG.rule("key", simpleKey.or1(dottedKey));
}
