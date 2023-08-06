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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import net.siisise.bind.Rebind;

/**
 * YAML Flow型
 */
public class YAMLFlowFormat extends YAMLFormat {
    
    @Override
    public String mapFormat(Map map) {
        return ((Map<?,?>)map).entrySet().parallelStream()
                .map(e -> { return e.getKey() + ": " + Rebind.valueOf(e.getValue(), this);})
                .collect( Collectors.joining(", ", "{", "}"));
    }

    @Override
    public String collectionFormat(Collection col) {
        return (String)col.parallelStream()
                .map(v -> Rebind.valueOf(v, this))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
