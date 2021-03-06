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
package org.modelmapper;

import java.lang.reflect.Type;
import java.util.Collection;

import org.modelmapper.config.Configuration;
import org.modelmapper.internal.Errors;
import org.modelmapper.internal.InheritingConfiguration;
import org.modelmapper.internal.MappingEngineImpl;
import org.modelmapper.internal.util.Assert;
import org.modelmapper.internal.util.TypeResolver;
import org.modelmapper.internal.util.Types;

/**
 * ModelMapper - Performs object mapping, maintains {@link Configuration} and stores {@link TypeMap
 * TypeMaps}.
 * 
 * <ul>
 * <li>To perform object mapping use {@link #map(Object, Class) map}.</li>
 * <li>To configure the mapping of one type to another use {@link #createTypeMap(Class, Class)
 * createTypeMap}.</li>
 * <li>To add mappings for specific properties use {@link #addMappings(PropertyMap) addMappings}
 * supplying a {@link PropertyMap}.</li>
 * <li>To configure ModelMapper use {@link #getConfiguration}.
 * <li>To validate mappings use {@link #validate}.
 * </ul>
 * 
 * @author Jonathan Halterman
 */
public class ModelMapper {
  private final InheritingConfiguration config;
  private final MappingEngineImpl engine;

  /**
   * Creates a new ModelMapper.
   */
  public ModelMapper() {
    config = new InheritingConfiguration();
    engine = new MappingEngineImpl(config);
  }

  /**
   * Registers the {@code converter} to use when mapping instances of types {@code S} to {@code D}.
   * The {@code converter} will be {@link TypeMap#setConverter(Converter) set} against TypeMap
   * corresponding to the {@code converter}'s type arguments {@code S} and {@code D}.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param converter to register
   * @throws IllegalArgumentException if {@code converter} is null or if type arguments {@code S}
   *           and {@code D} are not declared for the {@code converter}
   * @see TypeMap#setConverter(Converter)
   */
  @SuppressWarnings("unchecked")
  public <S, D> void addConverter(Converter<S, D> converter) {
    Assert.notNull(converter, "converter");
    Class<?>[] typeArguments = TypeResolver.resolveArguments(converter.getClass(), Converter.class);
    Assert.notNull("Must declare source type argument <S> and destination type argument <D> for converter");
    config.typeMapStore.<S, D>getOrCreate(null, (Class<S>) typeArguments[0],
        (Class<D>) typeArguments[1], null, converter, engine);
  }

  /**
   * Adds mappings from the {@code propertyMap} into the TypeMap corresponding to source type
   * {@code S} and destination type {@code D}. Explicit mappings defined in the {@code propertyMap}
   * will override any implicit mappings for the same properties.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param propertyMap from which mappings should be loaded
   * @return TypeMap corresponding to the {@code propertyMap}
   * @throws IllegalArgumentException if {@code propertyMap} is null
   * @throws ConfigurationException if a configuration error occurs while adding mappings for the
   *           {@code propertyMap}
   */
  public <S, D> TypeMap<S, D> addMappings(PropertyMap<S, D> propertyMap) {
    Assert.notNull(propertyMap, "propertyMap");
    return config.typeMapStore.getOrCreate(null, propertyMap.sourceType,
        propertyMap.destinationType, propertyMap, null, engine);
  }

  /**
   * Creates a TypeMap for the {@code sourceType} and {@code destinationType} using the
   * ModelMapper's configuration.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param sourceType
   * @param destinationType
   * @throws IllegalArgumentException if {@code sourceType} or {@code destinationType} are null
   * @throws IllegalStateException if a TypeMap already exists for {@code sourceType} and
   *           {@code destinationType}
   * @throws ConfigurationException if the ModelMapper cannot create the TypeMap
   * @see #getTypeMap(Class, Class)
   */
  public <S, D> TypeMap<S, D> createTypeMap(Class<S> sourceType, Class<D> destinationType) {
    return this.<S, D>createTypeMap(sourceType, destinationType, config);
  }

  /**
   * Creates a TypeMap for the {@code sourceType} and {@code destinationType} using the
   * {@code configuration}.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param sourceType
   * @param destinationType
   * @param configuration to apply to TypeMap
   * @throws IllegalArgumentException if {@code sourceType}, {@code destinationType} or
   *           {@code configuration} are null
   * @throws IllegalStateException if a TypeMap already exists for {@code sourceType} and
   *           {@code destinationType}
   * @throws ConfigurationException if the ModelMapper cannot create the TypeMap
   * @see #getTypeMap(Class, Class)
   */
  public <S, D> TypeMap<S, D> createTypeMap(Class<S> sourceType, Class<D> destinationType,
      Configuration configuration) {
    Assert.notNull(sourceType, "sourceType");
    Assert.notNull(destinationType, "destinationType");
    Assert.notNull(configuration, "configuration");
    return this.<S, D>createTypeMapInternal(sourceType, destinationType, null, configuration);
  }

  /**
   * Creates a TypeMap for the {@code sourceType} and {@code destinationType} identified by the
   * {@code typeMapName} using the ModelMapper's configuration.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param sourceType
   * @param destinationType
   * @param typeMapName
   * @throws IllegalArgumentException if {@code sourceType}, {@code destinationType} or
   *           {@code typeMapName} are null
   * @throws IllegalStateException if a TypeMap already exists for {@code sourceType},
   *           {@code destinationType} and {@code typeMapName}
   * @throws ConfigurationException if the ModelMapper cannot create the TypeMap
   * @see #getTypeMap(Class, Class, String)
   */
  public <S, D> TypeMap<S, D> createTypeMap(Class<S> sourceType, Class<D> destinationType,
      String typeMapName) {
    return this.<S, D>createTypeMap(sourceType, destinationType, typeMapName, config);
  }

  /**
   * Creates a TypeMap for the {@code sourceType} and {@code destinationType} identified by the
   * {@code typeMapName} using the {@code configuration}.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @param sourceType
   * @param destinationType
   * @param typeMapName
   * @param configuration to apply to TypeMap
   * @throws IllegalArgumentException if {@code sourceType}, {@code destinationType},
   *           {@code typeMapName} or {@code configuration} are null
   * @throws IllegalStateException if a TypeMap already exists for {@code sourceType},
   *           {@code destinationType} and {@code typeMapName}
   * @throws ConfigurationException if the ModelMapper cannot create the TypeMap
   * @see #getTypeMap(Class, Class, String)
   */
  public <S, D> TypeMap<S, D> createTypeMap(Class<S> sourceType, Class<D> destinationType,
      String typeMapName, Configuration configuration) {
    Assert.notNull(sourceType, "sourceType");
    Assert.notNull(destinationType, "destinationType");
    Assert.notNull(typeMapName, "typeMapName");
    Assert.notNull(configuration, "configuration");
    return createTypeMapInternal(sourceType, destinationType, typeMapName, configuration);
  }

  /**
   * Returns the ModelMapper's configuration.
   */
  public Configuration getConfiguration() {
    return config;
  }

  /**
   * Returns the TypeMap for the {@code sourceType} and {@code destinationType}, else returns
   * {@code null} if none exists.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @throws IllegalArgumentException is {@code sourceType} or {@code destinationType} are null
   * @see #createTypeMap(Class, Class)
   */
  public <S, D> TypeMap<S, D> getTypeMap(Class<S> sourceType, Class<D> destinationType) {
    Assert.notNull(sourceType, "sourceType");
    Assert.notNull(destinationType, "destinationType");
    return config.typeMapStore.<S, D>get(sourceType, destinationType);
  }

  /**
   * Returns the TypeMap for the {@code sourceType}, {@code destinationType} and {@code typeMapName}
   * , else returns {@code null} if none exists.
   * 
   * @param <S> source type
   * @param <D> destination type
   * @throws IllegalArgumentException is {@code sourceType}, {@code destinationType} or
   *           {@code typeMapName} are null
   * @see #createTypeMap(Class, Class, String)
   */
  public <S, D> TypeMap<S, D> getTypeMap(Class<S> sourceType, Class<D> destinationType,
      String typeMapName) {
    Assert.notNull(sourceType, "sourceType");
    Assert.notNull(destinationType, "destinationType");
    Assert.notNull(typeMapName, "typeMapName");
    return config.typeMapStore.<S, D>get(sourceType, destinationType, typeMapName);
  }

  /**
   * Returns all TypeMaps for the ModelMapper.
   */
  public Collection<TypeMap<?, ?>> getTypeMaps() {
    return config.typeMapStore.get();
  }

  /**
   * Maps {@code source} to an instance of {@code destinationType}. Mapping is performed according
   * to the corresponding TypeMap. If no TypeMap exists for {@code source.getClass()} and
   * {@code destinationType} then one is created.
   * 
   * @param <D> destination type
   * @param source object to map from
   * @param destinationType type to map to
   * @return fully mapped instance of {@code destinationType}
   * @throws IllegalArgumentException if {@code source} or {@code destinationType} are null
   * @throws ConfigurationException if the ModelMapper cannot find or create the TypeMap
   * @throws MappingException if a runtime error occurs while mapping
   */
  public <D> D map(Object source, Class<D> destinationType) {
    return map(source, (Type) destinationType);
  }

  /**
   * Maps {@code source} to {@code destination}. Mapping is performed according to the corresponding
   * TypeMap. If no TypeMap exists for {@code source.getClass()} and {@code destination.getClass()}
   * then one is created.
   * 
   * @param source object to map from
   * @param destination object to map to
   * @throws IllegalArgumentException if {@code source} or {@code destination} are null
   * @throws ConfigurationException if the ModelMapper cannot find or create the TypeMap
   * @throws MappingException if an error occurs while mapping
   */
  public void map(Object source, Object destination) {
    Assert.notNull(source, "source");
    Assert.notNull(destination, "destination");
    engine.<Object, Object>map(source, Types.<Object>deProxy(source.getClass()), destination,
        TypeToken.of(Types.<Object>deProxy(destination.getClass())));
  }

  /**
   * Maps {@code source} to an instance of {@code destinationType}. Mapping is performed according
   * to the corresponding TypeMap. If no TypeMap exists for {@code source.getClass()} and
   * {@code destinationType} then one is created.
   * 
   * <p>
   * To map a parameterized destination type, subclass {@link TypeToken} and obtain its Type:
   * 
   * <pre>
   * Type listType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
   * List&lt;String&gt; strings = modelMapper.map(source, listType);
   * </pre>
   * 
   * @param <D> destination type
   * @param source object to map from
   * @param destinationType type to map to
   * @return fully mapped instance of {@code destinationType}
   * @throws IllegalArgumentException if {@code source} or {@code destinationType} are null
   * @throws ConfigurationException if the ModelMapper cannot find or create the TypeMap
   * @throws MappingException if a runtime error occurs while mapping
   */
  public <D> D map(Object source, Type destinationType) {
    Assert.notNull(source, "source");
    Assert.notNull(destinationType, "destinationType");
    return engine.<Object, D>map(source, Types.<Object>deProxy(source.getClass()), null,
        TypeToken.<D>of(destinationType));
  }

  /**
   * Validates that <b>every</b> top level destination property for each configured TypeMap is
   * mapped to one and only one source property, or that a {@code Converter} was
   * {@link TypeMap#setConverter(Converter) set} for the TypeMap. If not, a ConfigurationException
   * is thrown detailing any missing mappings.
   * 
   * @throws ValidationException if any TypeMaps contain unmapped properties
   */
  public void validate() {
    Errors errors = new Errors();
    for (TypeMap<?, ?> typeMap : getTypeMaps()) {
      try {
        typeMap.validate();
      } catch (ValidationException e) {
        errors.merge(e.getErrorMessages());
      }
    }

    errors.throwValidationExceptionIfErrorsExist();
  }

  private <S, D> TypeMap<S, D> createTypeMapInternal(Class<S> sourceType, Class<D> destinationType,
      String typeMapName, Configuration configuration) {
    synchronized (config.typeMapStore.lock()) {
      Assert.state(config.typeMapStore.get(sourceType, destinationType) == null,
          String.format("A TypeMap already exists for %s and %s", sourceType, destinationType));
      return config.typeMapStore.create(null, sourceType, destinationType, typeMapName,
          (InheritingConfiguration) configuration, engine);
    }
  }
}
