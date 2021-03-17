package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Enum
import com.imma.model.core.EnumForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.service.TupleService
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.contracts.ExperimentalContracts

class EnumService(application: Application) : TupleService(application) {
    @ExperimentalContracts
    fun saveEnum(enumeration: Enum) {
        val fake =
            determineFakeOrNullId({ enumeration.enumId }, true, { enumeration.enumId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(enumeration)
        } else {
            updateTuple(enumeration)
        }

        // save collection name separately
        val name = enumeration.name?.replace(' ', '_')?.replace('-', '_')
        writeIntoMongo {
            val collectionName = "e_${name}"
            val query: Query = Query.query(Criteria.where("code").all())
            it.remove(query, collectionName)
            if (enumeration.items.isNotEmpty()) {
                it.insert(enumeration.items, collectionName)
            }
        }
    }

    fun findEnumById(enumId: String): Enum? {
        return findFromMongo {
            it.findById(enumId, Enum::class.java, CollectionNames.ENUM)
        }
    }

    fun findEnumsForHolder(): List<EnumForHolder> {
        val query: Query = Query.query(Criteria.where("name").all())
        query.fields().include("enumId", "name")
        return findListFromMongo(EnumForHolder::class.java, CollectionNames.ENUM, query)
    }

    fun findAllEnums(): List<Enum> {
        val query: Query = Query.query(Criteria.where("name").all())
        return findListFromMongo(Enum::class.java, CollectionNames.ENUM, query)
    }
}

