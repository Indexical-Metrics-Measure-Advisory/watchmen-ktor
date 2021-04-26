package com.imma.utils

object EnvConstants {
	const val ENV_MODE: String = "ktor.mode"
	const val ENV_MODE_DEV: String = "dev"
	const val ENV_MODE_PROD: String = "prod"
	const val TOKEN_EXPIRE_MINUTES: String = "ktor.auth.token.expire.minutes"
	const val JWT_DOMAIN = "ktor.jwt.domain"
	const val JWT_AUDIENCE = "ktor.jwt.audience"
	const val JWT_REALM = "ktor.jwt.realm"
	const val ADMIN_USERNAME = "ktor.admin.username"
	const val ADMIN_PASSWORD = "ktor.admin.password"
	const val ADMIN_ENABLED = "ktor.admin.enabled"

	const val DEFAULT_PERSIST_KIT: String = "ktor.persist.defaultKit"

	const val MONGO_ENABLED: String = "ktor.mongo.enabled"
	const val MONGO_HOST: String = "ktor.mongo.host"
	const val MONGO_PORT: String = "ktor.mongo.port"
	const val MONGO_NAME: String = "ktor.mongo.name"

	const val MYSQL_ENABLED: String = "ktor.mysql.enabled"
	const val MYSQL_HOST: String = "ktor.mysql.host"
	const val MYSQL_PORT: String = "ktor.mysql.port"
	const val MYSQL_NAME: String = "ktor.mysql.name"
	const val MYSQL_USER: String = "ktor.mysql.username"
	const val MYSQL_PASSWORD: String = "ktor.mysql.password"

	const val ORACLE_ENABLED: String = "ktor.oracle.enabled"
	const val ORACLE_HOST: String = "ktor.oracle.host"
	const val ORACLE_PORT: String = "ktor.oracle.port"
	const val ORACLE_NAME: String = "ktor.oracle.name"
	const val ORACLE_USER: String = "ktor.oracle.username"
	const val ORACLE_PASSWORD: String = "ktor.oracle.password"

	const val SNOWFLAKE_WORKER: String = "ktor.snowflake.worker"
	const val SNOWFLAKE_DATA_CENTER: String = "ktor.snowflake.dataCenter"

	const val CONTENT_DATE_FORMAT: String = "ktor.content.date.format"

	const val ALARM_MAIL_ENABLED: String = "ktor.alarm.mail.enabled"
	const val ALARM_MAIL_HOST: String = "ktor.alarm.mail.host"
	const val ALARM_MAIL_PORT: String = "ktor.alarm.mail.port"
	const val ALARM_MAIL_PROTOCOL: String = "ktor.alarm.mail.protocol"
	const val ALARM_MAIL_AUTH: String = "ktor.alarm.mail.auth"
	const val ALARM_MAIL_USERNAME: String = "ktor.alarm.mail.username"
	const val ALARM_MAIL_PASSWORD: String = "ktor.alarm.mail.password"
	const val ALARM_MAIL_TLS_ENABLE: String = "ktor.alarm.mail.tls.enable"
	const val ALARM_MAIL_TLS_REQUIRED: String = "ktor.alarm.mail.tls.required"
	const val ALARM_MAIL_FROM: String = "ktor.alarm.mail.from"
	const val ALARM_MAIL_TO: String = "ktor.alarm.mail.to"
}