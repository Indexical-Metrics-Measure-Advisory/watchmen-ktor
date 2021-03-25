package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Enum
import com.imma.model.core.EnumForHolder
import com.imma.model.core.EnumItem
import com.imma.model.determineFakeOrNullId
import com.imma.persist.core.select
import com.imma.service.Services
import com.imma.service.TupleService
import kotlin.contracts.ExperimentalContracts

class EnumService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveEnum(enumeration: Enum) {
        val fake =
            determineFakeOrNullId({ enumeration.enumId }, true, { enumeration.enumId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(enumeration, Enum::class.java, CollectionNames.ENUM)
        } else {
            updateTuple(enumeration, Enum::class.java, CollectionNames.ENUM)
        }

        // save collection name separately
        val name = enumeration.name?.replace(' ', '_')?.replace('-', '_')
        val collectionName = "e_${name}"
        persist().deleteAll(EnumItem::class.java, collectionName)
        if (enumeration.items.isNotEmpty()) {
            persist().insertAll(enumeration.items, EnumItem::class.java, collectionName)
        }
    }

    fun findEnumById(enumId: String): Enum? {
        return persist().findById(enumId, Enum::class.java, CollectionNames.ENUM)
    }

    fun findEnumsForHolder(): List<EnumForHolder> {
        return persist().listAll(
            select {
                column("enumId")
                column("name")
            },
            EnumForHolder::class.java, CollectionNames.ENUM
        )
    }

    fun findAllEnums(): List<Enum> {
        return persist().listAll(Enum::class.java, CollectionNames.ENUM)
    }
}

