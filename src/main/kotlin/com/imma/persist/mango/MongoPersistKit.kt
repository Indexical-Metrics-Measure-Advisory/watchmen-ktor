package com.imma.persist.mango

import com.imma.model.CollectionNames
import com.imma.model.admin.UserGroup
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.AbstractPersistKit
import com.imma.persist.PersistKit
import com.imma.persist.PersistKitProvider
import com.imma.persist.PersistKits
import com.imma.persist.core.*
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.FindOneAndUpdateOptions
import org.bson.Document

class MongoPersistKitProvider(name: String) : PersistKitProvider(name) {
    override fun createKit(): PersistKit {
        return MongoPersistKit()
    }
}

fun mongo() {
    PersistKits.register(MongoPersistKitProvider("mongo"))
}

/**
 * thread unsafe
 */
class MongoPersistKit : AbstractPersistKit() {
    private val mongoClient: MongoClient by lazy {
        createMongoClient()
    }
    private val mongoDatabase: MongoDatabase by lazy {
        doGetMongoDatabase()
    }

    private fun createMongoClient(): MongoClient {
        val host = Envs.string(EnvConstants.MONGO_HOST)
        val port = Envs.string(EnvConstants.MONGO_PORT)

        return MongoClients.create("mongodb://$host:$port")
    }

    private fun doGetMongoDatabase(): MongoDatabase {
        val name = Envs.string(EnvConstants.MONGO_NAME)

        return mongoClient.getDatabase(name)
    }

    private fun getMongoCollection(name: String): MongoCollection<Document> {
        return mongoDatabase.getCollection(name)
    }

    override fun <T : Any> insertOne(one: T, entityClass: Class<*>, entityName: String): T {
        val material = MapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
        getMongoCollection(entityName).insertOne(material.toDocument { nextSnowflakeIdStr() })
        return one
    }

    override fun <T : Any> insertAll(list: List<T>, entityClass: Class<*>, entityName: String): List<T> {
        getMongoCollection(entityName).insertMany(list.map { entity ->
            val material = MapperMaterialBuilder.create(entity).type(entityClass).name(entityName).build()
            material.toDocument { nextSnowflakeIdStr() }
        })
        return list
    }

    override fun <T : Any> updateOne(one: T, entityClass: Class<*>, entityName: String): T {
        val material = MapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
        getMongoCollection(entityName).replaceOne(
            material.generateIdFilter(),
            material.toDocument()
        )
        return one
    }

    override fun updateOne(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        getMongoCollection(entityName).findOneAndUpdate(material.toFilter(where), material.toUpdates(updates))
    }

    override fun <T : Any> upsertOne(one: T, entityClass: Class<*>, entityName: String): T {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        getMongoCollection(entityName).findOneAndUpdate(
            material.buildIdFilter(),
            material.toDocument { nextSnowflakeIdStr() },
            FindOneAndUpdateOptions().upsert(true)
        )

        return one
    }

    override fun update(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        getMongoCollection(entityName).updateMany(material.toFilter(where), material.toUpdates(updates))
    }

    override fun deleteById(id: String, entityClass: Class<*>, entityName: String) {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        getMongoCollection(entityName).deleteOne(material.buildIdFilter(id))
    }

    override fun delete(where: Where, entityClass: Class<*>, entityName: String) {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        getMongoCollection(entityName).deleteMany(material.toFilter(where))
    }

    override fun deleteAll(entityClass: Class<*>, entityName: String) {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        // is not empty to match all
        val where = where { factor(material.getIdFieldName()).isNotEmpty() }
        getMongoCollection(entityName).deleteMany(material.toFilter(where))
    }

    override fun <T> findById(id: String, entityClass: Class<*>, entityName: String): T? {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).find(material.buildIdFilter(id))
        val first: Document? = docs.first()
        return first?.let {
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(first) as T
        }
    }

    override fun <T> findOne(where: Where, entityClass: Class<*>, entityName: String): T? {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).find(material.toFilter(where))
        val first: Document? = docs.first()
        return first?.let {
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(first) as T
        }
    }

    override fun exists(where: Where, entityClass: Class<*>, entityName: String): Boolean {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        return getMongoCollection(entityName).countDocuments(material.toFilter(where)) > 0
    }

    override fun <T> listAll(entityClass: Class<*>, entityName: String): List<T> {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).find()
        return docs.map { doc ->
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(doc) as T
        }.toMutableList()
    }

    override fun <T> listAll(select: Select, entityClass: Class<*>, entityName: String): List<T> {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).aggregate(listOf(material.toProjection(select)))
        return docs.map { doc ->
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(doc) as T
        }.toMutableList()
    }

    override fun <T> list(where: Where, entityClass: Class<*>, entityName: String): List<T> {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).find(material.toFilter(where))
        return docs.map { doc ->
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(doc) as T
        }.toMutableList()
    }

    override fun <T> list(select: Select, where: Where, entityClass: Class<*>, entityName: String): List<T> {
        val material = MapperMaterialBuilder.create().type(entityClass).name(entityName).build()
        val docs = getMongoCollection(entityName).aggregate(
            listOf(
                material.toProjection(select),
                material.toMatch(where)
            )
        )
        return docs.map { doc ->
            @Suppress("UNCHECKED_CAST")
            material.fromDocument(doc) as T
        }.toMutableList()
    }

    override fun <T> page(pageable: Pageable, entityClass: Class<*>, entityName: String): DataPage<T> {
        // page in PageRequest is zero-based
//        val pageRequest: PageRequest = PageRequest.of(pageable.pageNumber - 1, pageable.pageSize)
//        val query = Query().with(pageRequest)
//        val count = mongoTemplate.count(query, entityClass, entityName)
//        val items: List<T> = findPageData(count) { mongoTemplate.find(query, entityClass, entityName) }
//        return toDataPage(items, count, pageable)
        TODO()
    }

    override fun <T> page(
        where: Where,
        pageable: Pageable,
        entityClass: Class<*>,
        entityName: String
    ): DataPage<T> {
        // page in PageRequest is zero-based
//        val pageRequest: PageRequest = PageRequest.of(pageable.pageNumber - 1, pageable.pageSize)
//        val query = buildQuery(where).with(pageRequest)
//        val count = mongoTemplate.count(query, entityClass, entityName)
//        val items: List<T> = findPageData(count) { mongoTemplate.find(query, entityClass, entityName) }
//        return toDataPage(items, count, pageable)
        TODO()
    }

    override fun close() {
        mongoClient.close()
    }
}

fun testMongo() {
    MongoPersistKit().use {
//        val ug = UserGroup(userGroupId = "831505165864538112", name = "x")
//        it.updateOne(ug, UserGroup::class.java, CollectionNames.USER_GROUP)
//        val ret: Any? = it.findById("831505165864538112", UserGroup::class.java, CollectionNames.USER_GROUP)
//        println(ret)
//        val exists = it.exists(where {
//            factor("userGroupId") eq { value("831505165864538112") }
//        }, UserGroup::class.java, CollectionNames.USER_GROUP)
//        println(exists)
        val list = it.listAll<UserGroup>(select {
            factor("userGroupId")
            factor("name")
//        }, where {
//            factor("userGroupId") eq { value("831505165864538112") }
        }, UserGroup::class.java, CollectionNames.USER_GROUP)
        println(list.size)
        println(list)
    }
}