/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import griffon.util.ServiceLoaderUtils;

import javax.inject.Provider;

public class ObjectMapperProvider implements Provider<ObjectMapper> {
    @Override
    @SuppressWarnings("unchecked")
    public ObjectMapper get() {
        final ObjectMapper om = new ObjectMapper();

        ServiceLoaderUtils.load(getClass().getClassLoader(), "META-INF/services", JsonEntity.class, (classLoader, type, line) -> {
            try {
                String className = line.trim();
                Class<? extends JsonEntity> clazz = (Class<? extends JsonEntity>) classLoader.loadClass(className);
                om.registerSubtypes(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        });

        return om;
    }
}