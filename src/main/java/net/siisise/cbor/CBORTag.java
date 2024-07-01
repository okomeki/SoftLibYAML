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

/**
 *
 */
public class CBORTag<T> {
    
    public static final long STANDARD_DATETIME = 0;
    public static final long EPOCH_BASED_DATE_TIME = 1;
    public static final long POSITIVE_BIGNUM = 2;
    public static final long NEGATIVE_BIGNUM = 3;
    public static final long DECIMAL_FRACTION = 4;

    public static final long EXPECTED_CONVERSION_BASE64URL = 21;
    public static final long EXPECTED_CONVERSION_BASE64 = 22;
    public static final long EXPECTED_CONVERSION_BASE16 = 23;

    // UTF-8 String
    public static final long URI = 32;
    public static final long BASE64URL = 33;
    public static final long BASE64 = 34;
    public static final long REGEX = 35;
    public static final long MIME_MESSAGE = 36;
    
    Number tag;
    T value;
    
    public CBORTag(Number tag, T value) {
        this.tag = tag;
        this.value = value;
    }
    
    Number tag() {
        return tag;
    }
    
    T value() {
        return value;
    }
}
