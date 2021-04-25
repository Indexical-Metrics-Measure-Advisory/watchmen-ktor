package com.imma.persist.rdbms

import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.AbstractPersistKit
import com.imma.persist.core.Select
import com.imma.persist.core.Updates
import com.imma.persist.core.Where
import com.imma.persist.core.where
import com.imma.persist.defs.PersistObject
import com.imma.utils.toDataPage
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

abstract class RDBMSPersistKit : AbstractPersistKit() {
	@Suppress("MemberVisibilityCanBePrivate")
	protected val connection: Connection by lazy { createConnection() }

	protected abstract fun buildMaterial(one: Any?, entityClass: Class<*>, entityName: String): RDBMSMapperMaterial

	protected abstract fun buildMaterial(entityClass: Class<*>, entityName: String): RDBMSMapperMaterial

	protected abstract fun createConnection(): Connection

	protected fun createConnection(url: String, user: String, password: String): Connection {
		return DriverManager.getConnection(url, user, password).also {
			it.autoCommit = true
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun createPreparedStatement(sql: String): PreparedStatement {
		return connection.prepareStatement(sql)
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun executeQuery(sql: String, values: List<Any?>): List<PersistObject> {
		val rows = mutableListOf<PersistObject>()
		createPreparedStatement(sql).use { statement ->
			values.forEachIndexed { index, value ->
				statement.setObject(index + 1, value)
			}

			statement.executeQuery().use { rst ->
				val meta = rst.metaData
				val columnNames = (1 .. meta.columnCount).map { meta.getColumnName(it) }
				while (rst.next()) {
					rows.add(columnNames.map { it to rst.getObject(it) }.toMap().toMutableMap())
				}
			}
		}
		return rows
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun executeUpdate(sql: String, values: List<Any?>): Int {
		return createPreparedStatement(sql).use { statement ->
			values.forEachIndexed { index, value ->
				statement.setObject(index + 1, value)
			}

			statement.executeUpdate()
		}
	}

	override fun <T : Any> insertOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = this.buildMaterial(one, entityClass, entityName)
		val po = material.toPersistObject { nextSnowflakeIdStr() }

		val fields = po.map { (key, value) -> key to value }
		val columnNames = fields.joinToString(", ") { it.first }
		val values = fields.map { it.second }
		val placeholders = arrayOfNulls<String>(po.size).joinToString(", ") { "?" }
		val sql = "INSERT INTO ${material.toCollectionName()}($columnNames) VALUES ($placeholders)"
		executeUpdate(sql, values)

		return one
	}

	override fun <T : Any> insertAll(list: List<T>, entityClass: Class<*>, entityName: String): List<T> {
		list.map { one -> this.insertOne(one, entityClass, entityName) }
		return list
	}

	override fun <T : Any> updateOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = this.buildMaterial(one, entityClass, entityName)
		val po = material.toPersistObject()

		val values = mutableListOf<Any?>()
		val sets = po.map { (key, value) ->
			values.add(value)
			"$key = ?"
		}.joinToString(", ")
		val where = material.generateIdFilter().let {
			values.add(it.second)
			"${it.first} = ?"
		}
		val sql = "UPDATE ${material.toCollectionName()} SET $sets WHERE $where"
		executeUpdate(sql, values)

		return one
	}

	override fun updateOne(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
		this.update(where, updates, entityClass, entityName)
	}

	override fun <T : Any> upsertOne(one: T, entityClass: Class<*>, entityName: String): T {
		val material = this.buildMaterial(one, entityClass, entityName)
		val id = material.getIdValue()?.toString()

		when {
			id.isNullOrBlank() -> this.insertOne(one, entityClass, entityName)
			!this.exists(
				where { factor(material.getIdFieldName()) eq { value(id) } },
				entityClass,
				entityName
			) -> this.insertOne(one, entityClass, entityName)
			else -> this.updateOne(one, entityClass, entityName)
		}

		return one
	}

	override fun update(where: Where, updates: Updates, entityClass: Class<*>, entityName: String) {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		val sets = material.toUpdates(updates)

		executeUpdate(
			"UPDATE ${material.toCollectionName()} SET ${sets.statement} WHERE ${filter.statement}",
			sets.values + filter.values
		)
	}

	override fun deleteById(id: String, entityClass: Class<*>, entityName: String) {
		val material = this.buildMaterial(entityClass, entityName)
		val where = material.generateIdFilter()

		executeUpdate(
			"DELETE FROM ${material.toCollectionName()} WHERE ${where.first} = ?",
			listOf(where.second)
		)
	}

	override fun delete(where: Where, entityClass: Class<*>, entityName: String) {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		executeUpdate(
			"DELETE FROM ${material.toCollectionName()} WHERE ${filter.statement}",
			filter.values
		)
	}

	override fun deleteAll(entityClass: Class<*>, entityName: String) {
		val material = this.buildMaterial(entityClass, entityName)
		executeUpdate("TRUNCATE TABLE ${material.toCollectionName()}", listOf())
	}

	override fun <T> findById(id: String, entityClass: Class<*>, entityName: String): T? {
		val material = this.buildMaterial(entityClass, entityName)
		val where = material.buildIdFilter(id)
		return executeQuery(
			"SELECT * FROM ${material.toCollectionName()} WHERE ${where.statement}",
			where.values
		).let {
			if (it.isNotEmpty()) {
				@Suppress("UNCHECKED_CAST")
				material.fromPersistObject(it[0]) as T
			} else {
				null
			}
		}
	}

	override fun <T> findOne(where: Where, entityClass: Class<*>, entityName: String): T? {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		return executeQuery(
			"SELECT * FROM ${material.toCollectionName()} WHERE ${filter.statement} LIMIT 1",
			filter.values
		).let {
			if (it.isNotEmpty()) {
				@Suppress("UNCHECKED_CAST")
				material.fromPersistObject(it[0]) as T
			} else {
				null
			}
		}
	}

	override fun <T> findOne(select: Select, where: Where, entityClass: Class<*>, entityName: String): T? {
		val material = this.buildMaterial(entityClass, entityName)
		val projection = material.toProjection(select)
		val filter = material.toFilter(where)
		return executeQuery(
			"SELECT $projection FROM ${material.toCollectionName()} WHERE ${filter.statement} LIMIT 1",
			filter.values
		).let {
			if (it.isNotEmpty()) {
				@Suppress("UNCHECKED_CAST")
				material.fromPersistObject(it[0]) as T
			} else {
				null
			}
		}
	}

	override fun <T> listAll(entityClass: Class<*>, entityName: String): List<T> {
		val material = this.buildMaterial(entityClass, entityName)
		return executeQuery(
			"SELECT * FROM ${material.toCollectionName()}",
			listOf()
		).map {
			@Suppress("UNCHECKED_CAST")
			material.fromPersistObject(it) as T
		}
	}

	override fun <T> listAll(select: Select, entityClass: Class<*>, entityName: String): List<T> {
		val material = this.buildMaterial(entityClass, entityName)
		val projection = material.toProjection(select)
		return executeQuery(
			"SELECT $projection FROM ${material.toCollectionName()}",
			listOf()
		).map {
			@Suppress("UNCHECKED_CAST")
			material.fromPersistObject(it) as T
		}
	}

	override fun <T> list(where: Where, entityClass: Class<*>, entityName: String): List<T> {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		return executeQuery(
			"SELECT * FROM ${material.toCollectionName()} WHERE ${filter.statement}",
			filter.values
		).map {
			@Suppress("UNCHECKED_CAST")
			material.fromPersistObject(it) as T
		}
	}

	override fun <T> list(select: Select, where: Where, entityClass: Class<*>, entityName: String): List<T> {
		val material = this.buildMaterial(entityClass, entityName)
		val projection = material.toProjection(select)
		val filter = material.toFilter(where)
		return executeQuery(
			"SELECT $projection FROM ${material.toCollectionName()} WHERE ${filter.statement}",
			filter.values
		).map {
			@Suppress("UNCHECKED_CAST")
			material.fromPersistObject(it) as T
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun computeSkipCount(pageable: Pageable): Int {
		return (pageable.pageNumber - 1) * pageable.pageSize
	}

	@Suppress("MemberVisibilityCanBePrivate")
	protected fun getCountValue(po: PersistObject, name: String? = "CNT"): Long {
		return when (val count = po[name]) {
			null -> 0
			is Number -> count.toLong()
			is BigDecimal -> count.toLong()
			else -> 0
		}
	}

	override fun <T> page(pageable: Pageable, entityClass: Class<*>, entityName: String): DataPage<T> {
		val material = this.buildMaterial(entityClass, entityName)
		val count = getCountValue(
			executeQuery("SELECT COUNT(NULL) AS CNT FROM ${material.toCollectionName()}", listOf())[0]
		)
		val skipCount = computeSkipCount(pageable)
		return if (count <= skipCount) {
			toDataPage(mutableListOf(), count, pageable)
		} else {
			executeQuery(
				"SELECT * FROM ${material.toCollectionName()} LIMIT $skipCount, ${pageable.pageSize}",
				listOf()
			).map { item ->
				@Suppress("UNCHECKED_CAST")
				material.fromPersistObject(item) as T
			}.let { list ->
				toDataPage(list, count, pageable)
			}
		}
	}

	override fun <T> page(
		where: Where,
		pageable: Pageable,
		entityClass: Class<*>,
		entityName: String
	): DataPage<T> {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		val count = getCountValue(
			executeQuery(
				"SELECT COUNT(NULL) AS CNT FROM ${material.toCollectionName()} WHERE ${filter.statement}",
				filter.values
			)[0]
		)
		val skipCount = computeSkipCount(pageable)
		return if (count <= skipCount) {
			toDataPage(mutableListOf(), count, pageable)
		} else {
			return executeQuery(
				"SELECT * FROM ${material.toCollectionName()} WHERE ${filter.statement}",
				filter.values
			).map { item ->
				@Suppress("UNCHECKED_CAST")
				material.fromPersistObject(item) as T
			}.let { list ->
				toDataPage(list, count, pageable)
			}
		}
	}

	override fun exists(where: Where, entityClass: Class<*>, entityName: String): Boolean {
		val material = this.buildMaterial(entityClass, entityName)
		val filter = material.toFilter(where)
		return executeQuery(
			"SELECT COUNT(NULL) AS CNT FROM ${material.toCollectionName()} WHERE $where",
			filter.values
		)[0].let {
			getCountValue(it) > 0
		}
	}

	override fun close() {
		try {
			connection.close()
		} catch (t: Throwable) {
			LoggerFactory.getLogger(javaClass).error("Failed to close connection.", t)
			// ignore this error
		}
	}
}