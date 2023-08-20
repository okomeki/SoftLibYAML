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

import net.siisise.abnf.ABNF;
import net.siisise.abnf.ABNFReg;

/**
 * YAMLのBNFっぽいものをABNFにするよ
 * https://yaml.org/spec/1.2.2/
 */
public class YAMLReg {
    static final ABNFReg REG = new ABNFReg();
    // 5.1. Character Set
    static final ABNF cPrintable = REG.rule("c-printable", ABNF.binlist("\t\n\r\u0085")
            .or(ABNF.range(0x20, 0x7e), 
                    ABNF.range(0xa0,0xd7ff),
                    ABNF.range(0xe000,0xfffd),
                    ABNF.range(0x10000,0x10ffff)));
    static final ABNF nbJson = REG.rule("nb-json",ABNF.bin(0x09).or(ABNF.range(0x20,0x10ffff)));
    // 5.2.
    static final ABNF cByteOrderMark = REG.rule("c-byte-order-mark", ABNF.bin(0xfeff));
    // 5.3.
    static final ABNF cSequenceEntry = REG.rule("c-sequence-entry", ABNF.bin('-'));
    static final ABNF cMappingKey = REG.rule("c-mapping-key",ABNF.bin('?'));
    static final ABNF cMappingValue = REG.rule("c-mapping-value", ABNF.bin(':'));
    static final ABNF cCollectEntry = REG.rule("c-collect-entry", ABNF.bin(','));
    static final ABNF cSequenceStart = REG.rule("c-sequence-start", ABNF.bin('['));
    static final ABNF cSequenceEnd = REG.rule("c-sequence-end", ABNF.bin(']'));
    static final ABNF cMappingStart = REG.rule("c-mapping-start", ABNF.bin('{'));
    static final ABNF cMappingEnd = REG.rule("c-mapping-end", ABNF.bin('}'));
    static final ABNF cComment = REG.rule("c-comment", ABNF.bin('#'));
    static final ABNF cAnchor = REG.rule("c-anchor", ABNF.bin('&'));
    static final ABNF cAlias = REG.rule("c-alias", ABNF.bin('*'));
    static final ABNF cTag = REG.rule("c-tag", ABNF.bin('!'));
    static final ABNF cLiteral = REG.rule("c-literal", ABNF.bin('|'));
    static final ABNF cFolded = REG.rule("c-folded",ABNF.bin('>'));
    static final ABNF cSingleQuote = REG.rule("c-single-quote", ABNF.bin('\''));
    static final ABNF cDoubleQuote = REG.rule("c-double-quote", ABNF.bin('"'));
    static final ABNF cDirective = REG.rule("c-directive", ABNF.bin('%'));
    static final ABNF cReserved = REG.rule("c-reserved", ABNF.binlist("@`"));
    static final ABNF cIndicator = REG.rule("c-indicator", cSequenceEntry.or(cMappingKey,
            cMappingValue, cCollectEntry, cSequenceStart, cSequenceEnd, cMappingStart, cMappingEnd,
            cComment, cAnchor, cAlias, cTag, cLiteral, cFolded, cSingleQuote, cDoubleQuote,
            cDirective, cReserved));
    static final ABNF cFlowIndicator = REG.rule("c-flow-indicator", cCollectEntry.or(cSequenceStart,
            cSequenceEnd, cMappingStart, cMappingEnd));
    // 5.4. Line Break Characters
    static final ABNF bLineFeed = REG.rule("b-line-feed", ABNF.bin(0x0a));
    static final ABNF bCarriageReturn = REG.rule("b-carriage-return", ABNF.bin(0x0d));
    static final ABNF bChar = REG.rule("b-char", bLineFeed.or(bCarriageReturn));
    static final ABNF nbChar = REG.rule("nb-char", cPrintable.mn(bChar).mn(cByteOrderMark));
    static final ABNF bBreak = REG.rule("b-break", bCarriageReturn.pl(bLineFeed).or(bCarriageReturn,bLineFeed));
    static final ABNF bAsLineFeed = REG.rule("b-as-line-feed", bBreak);
    static final ABNF bNonContent = REG.rule("b-non-content", bBreak);
    // 5.5. White Space Characters
    static final ABNF sSpace = REG.rule("s-space", ABNF.bin(0x20));
    static final ABNF sTab = REG.rule("s-tab", ABNF.bin(0x09));
    static final ABNF sWhite = REG.rule("s-white", sSpace.or(sTab));
    static final ABNF nsChar = REG.rule("ns-char",nbChar.mn(sWhite));
    // 5.6. Miscellaneous Characters
    static final ABNF nsDecDigit = REG.rule("ns-dec-digit", ABNF.range(0x30,0x39)); // 0-9
    static final ABNF nsHexDigit = REG.rule("ns-hex-digit", nsDecDigit.or(ABNF.range(0x41,0x46), ABNF.range(0x61,0x66)));
    static final ABNF nsAsciiLetter = REG.rule("ns-ascii-letter", ABNF.range(0x41,0x5a).or(ABNF.range(0x61, 0x7a)));
    static final ABNF nsWordChar = REG.rule("ns-word-char", nsDecDigit.or(nsAsciiLetter));
    static final ABNF nsUriChar = REG.rule("ns-uri-char", ABNF.bin('%').pl(nsHexDigit.x(2))
            .or(nsWordChar, ABNF.binlist("#;/?:@&=+$,_.!~*'()[]")));
    static final ABNF nsTagChar = REG.rule("ns-tag-char", nsUriChar.mn(cTag).mn(cFlowIndicator));
    static final ABNF cEscape = REG.rule("c-escape",ABNF.bin('\\'));
    static final ABNF nsEscNull = REG.rule("ns-esc-null", ABNF.bin('0'));
    static final ABNF nsEscBell = REG.rule("ns-esc-bell", ABNF.bin('a'));
    static final ABNF nsEscBackspace = REG.rule("ns-esc-backspace", ABNF.bin('b'));
    static final ABNF nsEscHorizontalTab = REG.rule("ns-esc-horizontal-tab", ABNF.bin('t').or(ABNF.bin(0x09)));
    static final ABNF nsEscLineFeed = REG.rule("ns-esc-line-feed", ABNF.bin('n'));
    static final ABNF nsEscVerticalTab = REG.rule("ns-esc-vertical-tab", ABNF.bin('v'));
    static final ABNF nsEscFormFeed = REG.rule("ns-esc-form-feed", ABNF.bin('f'));
    static final ABNF nsEscCarriageReturn = REG.rule("ns-esc-carriage-return", ABNF.bin('r'));
    // [50]
    static final ABNF nsEscEscape = REG.rule("ns-esc-escape", ABNF.bin('e'));
    static final ABNF nsEscSpace = REG.rule("ns-esc-space", ABNF.bin(0x20));
    static final ABNF nsEscDoubleQuote = REG.rule("ns-esc-double-quote", ABNF.bin('"'));
    static final ABNF nsEscSlash = REG.rule("ns-esc-slash", ABNF.bin('/'));
    static final ABNF nsEscBackslash = REG.rule("ns-esc-backslash", ABNF.bin('\\'));
    static final ABNF nsEscNextLine = REG.rule("ns-esc-next-line", ABNF.bin('N'));
    static final ABNF nsEscNonBreakingSpace = REG.rule("ns-esc-non-breaking-space", ABNF.bin('_'));
    static final ABNF nsEscLineSeparator = REG.rule("ns-esc-line-separator", ABNF.bin('L'));
    static final ABNF nsEscParagraphSeparator = REG.rule("ns-esc-paragraph-separator", ABNF.bin('p'));
    static final ABNF nsEsc8bit = REG.rule("ns-esc-8-bit", ABNF.bin('x').pl(nsHexDigit.x(2)));
    static final ABNF nsEsc16bit = REG.rule("ns-esc-16bit", ABNF.bin('u').pl(nsHexDigit.x(4)));
    static final ABNF nsEsc32bit = REG.rule("ns-esc-32bit", ABNF.bin('U').pl(nsHexDigit.x(8)));
    public static final ABNF cNsEscChar = REG.rule("c-ns-esc-char", cEscape.pl(
            nsEscNull.or(nsEscBell, nsEscBackspace, nsEscHorizontalTab,nsEscLineFeed, nsEscVerticalTab,
            nsEscFormFeed, nsEscCarriageReturn, nsEscEscape, nsEscSpace, nsEscDoubleQuote, nsEscSlash,
            nsEscBackslash,nsEscNextLine, nsEscNonBreakingSpace, nsEscLineSeparator, nsEscParagraphSeparator,
            nsEsc8bit, nsEsc16bit, nsEsc32bit)));
    // 6.1. Indentation Spaces
    // メソッドで別に実装する
    static final ABNF sIndent = REG.rule("s-indent", sSpace.x());
    static final ABNF sIndentLessThan = REG.rule("s-indent-less-than", sSpace.x());
    static final ABNF sIndentLessOrEqual = REG.rule("s-indent-less-or-equal", sSpace.x());
    // 6.2.
// ToDo:
    static final ABNF sSeparateInLine = REG.rule("s-separate-in-line", sWhite.x().or(REG.ref("start-of-line")));
    // 6.3. Line Prefixes
    static final ABNF sBlockLinePrefix = REG.rule("s-block-line-prefix", sIndent);
// ToDo:
    static final ABNF sFlowLinePrefix = REG.rule("s-flow-line-prefix", sIndent.pl(sSeparateInLine.c()));
    static final ABNF sLinePrefixBLOCK = REG.rule("s-line-prefix-block", sBlockLinePrefix);
    static final ABNF sLinePrefixFLOW = REG.rule("s-line-prefix-flow", sFlowLinePrefix);
    // 6.4.
    static final ABNF lEmpty = REG.rule("l-empty", sLinePrefixBLOCK.or(sLinePrefixFLOW, sIndentLessThan).pl(bAsLineFeed));
    static final ABNF lEmptyBLOCK = REG.rule("l-empty-block", sLinePrefixBLOCK.or(sIndentLessThan).pl(bAsLineFeed));
    static final ABNF lEmptyFLOW = REG.rule("l-empty-flow", sLinePrefixFLOW.or(sIndentLessThan).pl(bAsLineFeed));
            
    
}
