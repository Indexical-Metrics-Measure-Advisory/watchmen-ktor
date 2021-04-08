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
    const val MONGO_HOST: String = "ktor.mongo.host"
    const val MONGO_PORT: String = "ktor.mongo.port"
    const val MONGO_NAME: String = "ktor.mongo.name"

    const val SNOWFLAKE_WORKER: String = "ktor.snowflake.worker"
    const val SNOWFLAKE_DATA_CENTER: String = "ktor.snowflake.dataCenter"

    const val CONTENT_DATE_FORMAT: String = "ktor.content.date.format"

    const val ALARM_MAIL_ENABLED = "ktor.alarm.mail.enabled"
    const val ALARM_MAIL_HOST = "ktor.alarm.mail.host"
    const val ALARM_MAIL_PORT = "ktor.alarm.mail.port"
    const val ALARM_MAIL_PROTOCOL = "ktor.alarm.mail.protocol"
    const val ALARM_MAIL_AUTH = "ktor.alarm.mail.auth"
    const val ALARM_MAIL_USERNAME = "ktor.alarm.mail.username"
    const val ALARM_MAIL_PASSWORD = "ktor.alarm.mail.password"
    const val ALARM_MAIL_TLS_ENABLE = "ktor.alarm.mail.tls.enable"
    const val ALARM_MAIL_TLS_REQUIRED = "ktor.alarm.mail.tls.required"
    const val ALARM_MAIL_FROM = "ktor.alarm.mail.from"
    const val ALARM_MAIL_TO = "ktor.alarm.mail.to"
}