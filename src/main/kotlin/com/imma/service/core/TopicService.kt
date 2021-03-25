package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Topic
import com.imma.model.core.TopicForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.persist.core.select
import com.imma.persist.core.where
import com.imma.service.Services
import com.imma.service.TupleService
import kotlin.contracts.ExperimentalContracts

class TopicService(services: Services) : TupleService(services) {
    @ExperimentalContracts
    fun saveTopic(topic: Topic) {
        val fake = determineFakeOrNullId({ topic.topicId }, true, { topic.topicId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(topic, Topic::class.java, CollectionNames.TOPIC)
        } else {
            updateTuple(topic, Topic::class.java, CollectionNames.TOPIC)
        }
    }

    fun findTopicById(topicId: String): Topic? {
        return persist().findById(topicId, Topic::class.java, CollectionNames.TOPIC)
    }

    fun findTopicsByName(name: String?, pageable: Pageable): DataPage<Topic> {
        return if (name.isNullOrEmpty()) {
            persist().page(pageable, Topic::class.java, CollectionNames.TOPIC)
        } else {
            persist().page(
                where {
                    column("name") regex name
                },
                pageable,
                Topic::class.java, CollectionNames.TOPIC
            )
        }
    }

    fun findTopicsByNameForHolder(name: String?): List<TopicForHolder> {
        return if (name.isNullOrEmpty()) {
            persist().listAll(TopicForHolder::class.java, CollectionNames.TOPIC)
        } else {
            persist().list(
                where {
                    column("name") regex name
                },
                TopicForHolder::class.java, CollectionNames.TOPIC
            )
        }
    }

    fun findTopicsByIdsForHolder(topicIds: List<String>): List<TopicForHolder> {
        return persist().list(
            select {
                column("topicId")
                column("name")
            },
            where {
                column("topicId") `in` topicIds
            },
            TopicForHolder::class.java, CollectionNames.TOPIC
        )
    }

    fun findAllTopics(): List<Topic> {
        return persist().listAll(Topic::class.java, CollectionNames.TOPIC)
    }
}

