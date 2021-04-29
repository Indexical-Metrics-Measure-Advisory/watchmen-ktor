package com.imma.persist.mysql

import com.imma.model.core.Topic
import com.imma.persist.PersistKit
import com.imma.persist.PersistKitProvider
import com.imma.persist.PersistKits
import com.imma.persist.rdbms.RDBMSMapperMaterial
import com.imma.persist.rdbms.RDBMSPersistKit
import com.imma.plugin.PluginInitializer
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import java.sql.Connection

class MySQLPersistKitProvider(name: String) : PersistKitProvider(name) {
    init {
        Class.forName("com.mysql.cj.jdbc.Driver")
    }

    override fun createKit(): PersistKit {
        return MySQLPersistKit()
    }
}

/**
 * register initializer in META-INF/plugin.factories to
 */
@Suppress("unused")
class MySQLInitializer : PluginInitializer {
    override fun register() {
        val mysqlEnabled = Envs.boolean(EnvConstants.MYSQL_ENABLED, false)
        if (mysqlEnabled) {
            PersistKits.register(MySQLPersistKitProvider("mysql"))
        }
    }
}

/**
 * thread unsafe
 */
class MySQLPersistKit : RDBMSPersistKit() {
    override fun registerDynamicTopic(topic: Topic) {
        MySQLEntityMapper.registerDynamicTopic(topic)
    }

    override fun buildMaterial(one: Any?, entityClass: Class<*>, entityName: String): RDBMSMapperMaterial {
        return MySQLMapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
    }

    override fun buildMaterial(entityClass: Class<*>, entityName: String): RDBMSMapperMaterial {
        return MySQLMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
    }

    override fun createConnection(): Connection {
        val host = Envs.string(EnvConstants.MYSQL_HOST)
        val port = Envs.string(EnvConstants.MYSQL_PORT)
        val name = Envs.string(EnvConstants.MYSQL_NAME)
        val user = Envs.string(EnvConstants.MYSQL_USER)
        val password = Envs.string(EnvConstants.MYSQL_PASSWORD)

        return this.createConnection("jdbc:mysql://$host:$port/$name", user, password)
    }

    override fun toPageSQL(sql: String, skipCount: Int, pageSize: Int, pageNumber: Int): String {
        return "$sql LIMIT $skipCount, $pageSize"
    }

    override fun entityExists(entityClass: Class<*>, entityName: String): Boolean {
        TODO()
    }

    override fun createEntity(entityClass: Class<*>, entityName: String) {
        TODO()
    }
}