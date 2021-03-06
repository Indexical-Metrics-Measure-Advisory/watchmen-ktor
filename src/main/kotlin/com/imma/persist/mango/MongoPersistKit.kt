package com.imma.persist.mango

import com.imma.model.core.Topic
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.AbstractPersistKit
import com.imma.persist.PersistKit
import com.imma.persist.PersistKitProvider
import com.imma.persist.PersistKits
import com.imma.persist.core.Select
import com.imma.persist.core.Updates
import com.imma.persist.core.Where
import com.imma.persist.core.where
import com.imma.plugin.PluginInitializer
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import com.imma.utils.toDataPage
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

class MongoInitializer : PluginInitializer {
	override fun register() {
		val mongoEnabled = Envs.boolean(EnvConstants.MONGO_ENABLED, false)
		if (mongoEnabled) {
			PersistKits.register(MongoPersistKitProvider("mongo"))
		}
	}
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

	override fun registerDynamicTopic(topic: Topic) {
		MongoEntityMapper.registerDynamicTopic(topic)
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

	private fun getMongoCollection(material: MongoMapperMaterial): MongoCollection<Document> {
		return getMongoCollection(material.toCollectionName())
	}

	override fun <T : Any> insertOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = MongoMapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
		getMongoCollection(material).insertOne(material.toDocument { nextSnowflakeIdStr() })
		return one
	}

	override fun <T : Any> insertAll(list: List<T>, entityClass: Class<*>, entityName: String): List<T> {
		if (list.isNotEmpty()) {
			val material = MongoMapperMaterialBuilder.create(list[0]).type(entityClass).name(entityName).build()
			getMongoCollection(material).insertMany(list.map { entity ->
				@Suppress("NAME_SHADOWING")
				val material = MongoMapperMaterialBuilder.create(entity).type(entityClass).name(entityName).build()
				material.toDocument { nextSnowflakeIdStr() }
			})
		}
		return list
	}

	override fun <T : Any> updateOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = MongoMapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
		getMongoCollection(material).replaceOne(
			material.generateIdFilter(),
			material.toDocument()
		)
		return one
	}

	override fun updateOne(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		getMongoCollection(material).findOneAndUpdate(material.toFilter(where), material.toUpdates(updates))
	}

	override fun <T : Any> upsertOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = MongoMapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
		getMongoCollection(material).findOneAndUpdate(
			material.buildIdFilter(),
			material.toDocument { nextSnowflakeIdStr() },
			FindOneAndUpdateOptions().upsert(true)
		)

		return one
	}

	override fun update(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		getMongoCollection(material).updateMany(material.toFilter(where), material.toUpdates(updates))
	}

	override fun deleteById(id: String, entityClass: Class<*>, entityName: String) {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		getMongoCollection(material).deleteOne(material.buildIdFilter(id))
	}

	override fun delete(where: Where, entityClass: Class<*>, entityName: String) {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		getMongoCollection(material).deleteMany(material.toFilter(where))
	}

	override fun deleteAll(entityClass: Class<*>, entityName: String) {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		// is not empty to match all
		val where = where { factor(material.getIdFieldName()).isNotEmpty() }
		getMongoCollection(material).deleteMany(material.toFilter(where))
	}

	override fun <T> findById(id: String, entityClass: Class<*>, entityName: String): T? {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).find(material.buildIdFilter(id))
		val first: Document? = docs.first()
		return first?.let {
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(first) as T
		}
	}

	override fun <T> findOne(where: Where, entityClass: Class<*>, entityName: String): T? {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).find(material.toFilter(where))
		val first: Document? = docs.first()
		return first?.let {
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(first) as T
		}
	}

	override fun <T> findOne(select: Select, where: Where, entityClass: Class<*>, entityName: String): T? {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).aggregate(
			listOf(
				material.toProjection(select),
				material.toMatch(where),
				material.toLimit(1)
			)
		)
		val first: Document? = docs.first()
		return first?.let {
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(first) as T
		}
	}

	override fun exists(where: Where, entityClass: Class<*>, entityName: String): Boolean {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		return getMongoCollection(material).countDocuments(material.toFilter(where)) > 0
	}

	override fun <T> listAll(entityClass: Class<*>, entityName: String): List<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).find()
		return docs.map { doc ->
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(doc) as T
		}.toMutableList()
	}

	override fun <T> listAll(select: Select, entityClass: Class<*>, entityName: String): List<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).aggregate(listOf(material.toProjection(select)))
		return docs.map { doc ->
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(doc) as T
		}.toMutableList()
	}

	override fun <T> list(where: Where, entityClass: Class<*>, entityName: String): List<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).find(material.toFilter(where))
		return docs.map { doc ->
			@Suppress("UNCHECKED_CAST")
			material.fromDocument(doc) as T
		}.toMutableList()
	}

	override fun <T> list(select: Select, where: Where, entityClass: Class<*>, entityName: String): List<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val docs = getMongoCollection(material).aggregate(
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

	private fun computeSkipCount(pageable: Pageable): Int {
		return (pageable.pageNumber - 1) * pageable.pageSize
	}

	override fun <T> page(pageable: Pageable, entityClass: Class<*>, entityName: String): DataPage<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val count = getMongoCollection(material).countDocuments()
		val skipCount = computeSkipCount(pageable)
		return if (count <= skipCount) {
			toDataPage(mutableListOf(), count, pageable)
		} else {
			val docs = getMongoCollection(material).aggregate(
				listOf(
					material.toSkip(skipCount),
					material.toLimit(pageable.pageSize)
				)
			)
			val items = docs.map { doc ->
				@Suppress("UNCHECKED_CAST")
				material.fromDocument(doc) as T
			}.toMutableList()
			toDataPage(items, count, pageable)
		}
	}

	override fun <T> page(
		where: Where,
		pageable: Pageable,
		entityClass: Class<*>,
		entityName: String
	): DataPage<T> {
		val material = MongoMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
		val filter = material.toFilter(where)
		val count = getMongoCollection(material).countDocuments(filter)
		val skipCount = computeSkipCount(pageable)
		return if (count <= skipCount) {
			toDataPage(mutableListOf(), count, pageable)
		} else {
			val docs = getMongoCollection(material).aggregate(
				listOf(
					material.toMatch(filter),
					material.toSkip(skipCount),
					material.toLimit(pageable.pageSize)
				)
			)
			val items = docs.map { doc ->
				@Suppress("UNCHECKED_CAST")
				material.fromDocument(doc) as T
			}.toMutableList()
			toDataPage(items, count, pageable)
		}
	}

	override fun entityExists(entityClass: Class<*>, entityName: String): Boolean {
		TODO("How to check a collection is existed in mongo database?")
	}

	override fun createEntity(entityClass: Class<*>, entityName: String) {
		// create is unnecessary in mongo
	}

	override fun close() {
		mongoClient.close()
	}
}