package com.imma.persist.oracle

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

class OraclePersistKitProvider(name: String) : PersistKitProvider(name) {
    init {
        Class.forName("com.mysql.cj.jdbc.Driver")
    }

    override fun createKit(): PersistKit {
        return OraclePersistKit()
    }
}

/**
 * register initializer in META-INF/plugin.factories to
 */
@Suppress("unused")
class OracleInitializer : PluginInitializer {
    override fun register() {
        val oracleEnabled = Envs.boolean(EnvConstants.ORACLE_ENABLED, false)
        if (oracleEnabled) {
            PersistKits.register(OraclePersistKitProvider("oracle"))
        }
    }
}

/**
 * thread unsafe
 */
class OraclePersistKit : RDBMSPersistKit() {
    override fun registerDynamicTopic(topic: Topic) {
        OracleEntityMapper.registerDynamicTopic(topic)
    }

    override fun buildMaterial(one: Any?, entityClass: Class<*>, entityName: String): RDBMSMapperMaterial {
        return OracleMapperMaterialBuilder.create(one).type(entityClass).name(entityName).build()
    }

    override fun buildMaterial(entityClass: Class<*>, entityName: String): RDBMSMapperMaterial {
        return OracleMapperMaterialBuilder.create().type(entityClass).name(entityName).build()
    }

    override fun createConnection(): Connection {
        val host = Envs.string(EnvConstants.ORACLE_HOST)
        val port = Envs.string(EnvConstants.ORACLE_PORT)
        val name = Envs.string(EnvConstants.ORACLE_NAME)
        val user = Envs.string(EnvConstants.ORACLE_USER)
        val password = Envs.string(EnvConstants.ORACLE_PASSWORD)

        return this.createConnection("jdbc:oracle:thin:@$host:$port:$name", user, password)
    }

    /**
     * filter column ROWNUM_ if exists
     */
    override fun filterUselessColumnNames(columnNames: List<String>): List<String> {
        return columnNames.filter { it != "ROWNUM_" }
    }

    override fun toPageSQL(sql: String, skipCount: Int, pageSize: Int, pageNumber: Int): String {
        return "SELECT * FROM (${
            sql.replaceFirst(
                "SELECT ",
                "SELECT ROWNUM AS ROWNUM_ "
            )
        }) WHERE ROWNUM_ > $skipCount AND ROWNUM_ <= ${pageNumber * pageSize}"
    }

    override fun entityExists(entityClass: Class<*>, entityName: String): Boolean {
        TODO()
    }

    override fun createEntity(entityClass: Class<*>, entityName: String) {
        TODO()
    }
}