package com.imma.plugin

import com.imma.service.core.action.AlarmAction
import com.imma.service.core.action.AlarmActionSeverity
import com.imma.service.core.action.AlarmConsumer
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

private fun createJavaMailSender(): JavaMailSender {
    return JavaMailSenderImpl().apply {
        host = Envs.string(EnvConstants.ALARM_MAIL_HOST)
        port = Envs.int(EnvConstants.ALARM_MAIL_PORT)

        when (val protocol = Envs.string(EnvConstants.ALARM_MAIL_PROTOCOL, "smtp")) {
            "smtp" -> {
                javaMailProperties.apply {
                    this["mail.transport.protocol"] = protocol

                    val auth = Envs.boolean(EnvConstants.ALARM_MAIL_AUTH, true)
                    this["mail.smtp.auth"] = auth.toString()
                    if (auth) {
                        username = Envs.string(EnvConstants.ALARM_MAIL_USERNAME)
                        password = Envs.string(EnvConstants.ALARM_MAIL_PASSWORD)
                    }
                    this["mail.smtp.starttls.enable"] =
                        Envs.boolean(EnvConstants.ALARM_MAIL_TLS_ENABLE, false).toString()
                    this["mail.smtp.starttls.required"] =
                        Envs.boolean(EnvConstants.ALARM_MAIL_TLS_REQUIRED, false).toString()
                }
            }
            else -> throw RuntimeException("Protocol[$protocol] is not supported yet.")
        }
    }
}

fun alarmByMail() {
    val mailEnabled = Envs.boolean(EnvConstants.ALARM_MAIL_ENABLED, false)
    if (mailEnabled) {
        AlarmAction.register(AlarmMailSender(createJavaMailSender()))
    }
}

class AlarmMailSender(private val sender: JavaMailSender) : AlarmConsumer {
    private val mailFrom: String by lazy { Envs.string(EnvConstants.ALARM_MAIL_FROM) }
    private val mailTo: List<String> by lazy { Envs.list(EnvConstants.ALARM_MAIL_TO) }

    override fun alarm(severity: AlarmActionSeverity, message: String) {
        SimpleMailMessage().apply {
            from = mailFrom
            setTo(*mailTo.toTypedArray())
            subject = "[${severity.severity}] Alarm from pipeline."
            text = message
        }.run { sender.send(this) }
    }
}