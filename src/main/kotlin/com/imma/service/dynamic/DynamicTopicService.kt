package com.imma.service.dynamic

import com.imma.model.core.Topic
import com.imma.persist.core.Where
import com.imma.service.Service
import com.imma.service.Services

class DynamicTopicService(services: Services) : Service(services) {
    fun exists(topic: Topic, where: Where): Boolean {
        return persist().exists(where, Map::class.java, topic.name!!)
    }
}