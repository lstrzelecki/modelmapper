/*
 * Copyright 2011 the original author or authors.
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
package org.modelmapper.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.modelmapper.config.Configuration;
import org.modelmapper.internal.PropertyInfoImpl.ValueReaderPropertyInfo;

/**
 * Statically stores and retrieves TypeInfo instances by type, parent type, and configuration.
 * 
 * @author Jonathan Halterman
 */
class TypeInfoRegistry {
  private static final Map<TypeInfoKey, TypeInfoImpl<?>> cache = new ConcurrentHashMap<TypeInfoKey, TypeInfoImpl<?>>();

  private static class TypeInfoKey {
    private final Class<?> type;
    private final Class<?> parentType;
    private final Configuration configuration;

    TypeInfoKey(Class<?> type, Class<?> parentType, Configuration configuration) {
      this.type = type;
      this.parentType = parentType;
      this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this)
        return true;
      if (!(o instanceof TypeInfoKey))
        return false;
      TypeInfoKey other = (TypeInfoKey) o;
      return type.equals(other.type) && (parentType == null || parentType.equals(other.parentType))
          && configuration.equals(other.configuration);
    }

    @Override
    public int hashCode() {
      int result = 31 * type.hashCode();
      result = 31 * result + configuration.hashCode();
      return parentType == null ? result : 31 * result + parentType.hashCode();
    }
  }

  @SuppressWarnings("unchecked")
  static <T> TypeInfoImpl<T> typeInfoFor(Accessor accessor, Class<?> parentType,
      InheritingConfiguration configuration) {
    return (TypeInfoImpl<T>) TypeInfoRegistry.typeInfoFor(
        (T) (accessor instanceof ValueReaderPropertyInfo ? ((ValueReaderPropertyInfo) accessor).source
            : null), (Class<T>) accessor.getType(), parentType, configuration);
  }

  /**
   * Returns a non-cached TypeInfoImpl instance if there is no supported ValueAccessReader for the
   * {@code sourceType}, else a cached TypeInfoImpl instance is returned.
   */
  static <T> TypeInfoImpl<T> typeInfoFor(T source, Class<T> sourceType, Class<?> parentType,
      InheritingConfiguration configuration) {
    if (configuration.valueAccessStore.getFirstSupportedReader(sourceType) != null)
      return new TypeInfoImpl<T>(source, sourceType, configuration);
    return typeInfoFor(sourceType, parentType, configuration);
  }

  /**
   * Returns a statically cached TypeInfoImpl instance for the given criteria.
   */
  static <T> TypeInfoImpl<T> typeInfoFor(Class<T> type, InheritingConfiguration configuration) {
    return typeInfoFor(type, null, configuration);
  }

  /**
   * Returns a statically cached TypeInfoImpl instance for the given criteria.
   */
  @SuppressWarnings("unchecked")
  static <T> TypeInfoImpl<T> typeInfoFor(Class<T> sourceType, Class<?> parentType,
      InheritingConfiguration configuration) {
    TypeInfoKey pair = new TypeInfoKey(sourceType, parentType, configuration);
    TypeInfoImpl<T> typeInfo = (TypeInfoImpl<T>) cache.get(pair);

    if (typeInfo == null) {
      synchronized (cache) {
        typeInfo = (TypeInfoImpl<T>) cache.get(pair);
        if (typeInfo == null) {
          typeInfo = new TypeInfoImpl<T>(null, sourceType, configuration);
          cache.put(pair, typeInfo);
        }
      }
    }

    return typeInfo;
  }
}
