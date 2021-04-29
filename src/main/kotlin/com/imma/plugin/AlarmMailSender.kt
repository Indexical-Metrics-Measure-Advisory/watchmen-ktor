package com.imma.plugin

import com.imma.service.core.action.AlarmAction
import com.imma.service.core.action.AlarmActionSeverity
import com.imma.service.core.action.AlarmConsumer
import com.imma.utils.EnvConstants
import com.imma.utils.Envs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

class AlarmMailSenderInitializer : PluginInitializer {
	override fun register() {
		val mailEnabled = Envs.boolean(EnvConstants.ALARM_MAIL_ENABLED, false)
		if (mailEnabled) {
			AlarmAction.register(AlarmMailSender(createJavaMailSender()))
		}
	}
}

class AlarmMailSender(private val sender: JavaMailSender) : AlarmConsumer {
	private val logger: Logger by lazy {
		LoggerFactory.getLogger(this::class.java)
	}

	private val mailFrom: String by lazy { Envs.string(EnvConstants.ALARM_MAIL_FROM) }
	private val mailTo: List<String> by lazy { Envs.list(EnvConstants.ALARM_MAIL_TO) }

	override fun alarm(severity: AlarmActionSeverity, message: String) {
		try {
			SimpleMailMessage().apply {
				from = mailFrom
				setTo(*mailTo.toTypedArray())
				subject = "[${severity.severity}] Alarm from pipeline."
				text = message
			}.run { sender.send(this) }
		} catch (t: Throwable) {
			logger.error("Failed to send mail for alarm[severity=$severity, message=$message]", t)
		}
	}
}