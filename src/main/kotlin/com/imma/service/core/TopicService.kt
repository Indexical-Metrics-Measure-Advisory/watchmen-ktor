package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.core.Topic
import com.imma.model.core.TopicForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.service.TupleService
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.contracts.ExperimentalContracts

class TopicService(application: Application) : TupleService(application) {
    @ExperimentalContracts
    fun saveTopic(topic: Topic) {
        val fake = determineFakeOrNullId({ topic.topicId }, true, { topic.topicId = nextSnowflakeId().toString() })

        if (fake) {
            createTuple(topic)
        } else {
            updateTuple(topic)
        }
    }

    fun findTopicById(topicId: String): Topic? {
        return persistKit.findById(topicId, Topic::class.java, CollectionNames.TOPIC)
    }

    fun findTopicsByName(name: String? = "", pageable: Pageable): DataPage<Topic> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(Topic::class.java, CollectionNames.TOPIC, query, pageable)
    }

    fun findTopicsByNameForHolder(name: String? = ""): List<TopicForHolder> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("topicId", "name")
        return findListFromMongo(TopicForHolder::class.java, CollectionNames.TOPIC, query)
    }

    fun findTopicsByIdsForHolder(topicIds: List<String>): List<TopicForHolder> {
        val query: Query = Query.query(Criteria.where("topicId").`in`(topicIds))
        query.fields().include("topicId", "name")
        return findListFromMongo(TopicForHolder::class.java, CollectionNames.TOPIC, query)
    }

    fun findAllTopics(): List<Topic> {
        val query: Query = Query.query(Criteria.where("name").all())
        return findListFromMongo(Topic::class.java, CollectionNames.TOPIC, query)
    }
}

