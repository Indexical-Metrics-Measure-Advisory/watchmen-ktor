package com.imma.persist.oracle

import com.imma.model.core.Topic
import com.imma.persist.defs.AbstractDynamicTopicDef

class OracleDynamicTopicDef(topic: Topic) : AbstractDynamicTopicDef(topic), OracleEntityDef

fun createOracleDynamicTopicDef(topic: Topic): OracleDynamicTopicDef {
	return OracleDynamicTopicDef(topic)
}
