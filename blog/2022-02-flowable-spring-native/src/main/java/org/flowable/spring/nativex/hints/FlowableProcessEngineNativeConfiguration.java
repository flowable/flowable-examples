package org.flowable.spring.nativex.hints;

import java.util.EnumSet;
import java.util.HashSet;

import org.apache.ibatis.type.TypeHandler;
import org.flowable.common.engine.api.query.Query;
import org.flowable.common.engine.impl.db.ListQueryParameterObject;
import org.flowable.common.engine.impl.persistence.cache.EntityCacheImpl;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayRef;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.common.engine.impl.persistence.entity.EntityManager;
import org.flowable.common.engine.impl.persistence.entity.TablePageQueryImpl;
import org.flowable.eventregistry.impl.db.SetChannelDefinitionTypeAndImplementationCustomChange;
import org.flowable.variable.api.types.VariableType;
import org.flowable.variable.service.impl.InternalVariableInstanceQueryImpl;
import org.reflections.Reflections;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeResourcesEntry;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * @author Filip Hrisafov
 * @author Joram Barrez
 */
public class FlowableProcessEngineNativeConfiguration implements NativeConfiguration {

    @Override
    public void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) {
        registry.resources()
                .add(NativeResourcesEntry.of("org/flowable/db/.*\\.xml"))
                .add(NativeResourcesEntry.of("org/flowable/.*/db/.*\\.xml"))
                .add(NativeResourcesEntry.of("org/flowable/db/.*\\.sql"))
                .add(NativeResourcesEntry.of("org/flowable/.*/db/.*\\.sql"))
                .add(NativeResourcesEntry.of("org/flowable/common/db/properties/.*\\.properties"))
                .add(NativeResourcesEntry.of("org/flowable/impl/bpmn/parser/.*\\.xsd"))
                .add(NativeResourcesEntry.ofBundle("org/flowable/common/engine/impl/de/odysseus/el/misc/LocalStrings"))
        ;

        TypeAccess[] values = EnumSet.complementOf(EnumSet.of(TypeAccess.JNI)).toArray(new TypeAccess[0]);
        Reflections reflections = new Reflections("org.flowable");
        Class<?>[] subTypes = {
                TypeHandler.class,
                EntityManager.class,
                Entity.class,
                Query.class,
                VariableType.class,
        };

        NativeConfigurationRegistry.ReflectionConfiguration reflectionConfiguration = registry.reflection();
        for (Class<?> subType : subTypes) {
            for (Class<?> type : reflections.getSubTypesOf(subType)) {
                reflectionConfiguration.forType(type).withAccess(values);
            }
        }

        Class<?>[] types = {
                ListQueryParameterObject.class,
                TablePageQueryImpl.class,
                SetChannelDefinitionTypeAndImplementationCustomChange.class,
                ByteArrayRef.class,
                InternalVariableInstanceQueryImpl.class
        };

        for (Class<?> type : types) {
            reflectionConfiguration.forType(type).withAccess(values);
        }

        reflectionConfiguration.forType(EntityCacheImpl.class).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS);

        reflectionConfiguration.forType(HashSet.class).withAccess(TypeAccess.PUBLIC_METHODS);
    }

}
