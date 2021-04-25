package com.imma.persist.mysql

import com.imma.model.core.Topic
import com.imma.persist.defs.AbstractDynamicTopicDef

class MySQLDynamicTopicDef(topic: Topic) : AbstractDynamicTopicDef(topic), MySQLEntityDef

fun createMySQLDynamicTopicDef(topic: Topic): MySQLDynamicTopicDef {
	return MySQLDynamicTopicDef(topic)
}
