package com.imma.service

import com.imma.model.Tuple
import com.imma.model.assignDateTimePair
import com.imma.model.forceAssignDateTimePair

open class TupleService(services: Services) : Service(services) {
    protected fun createTuple(tuple: Tuple) {
        forceAssignDateTimePair(tuple)
        services.persist().insertOne(tuple)
    }

    protected fun updateTuple(tuple: Tuple) {
        assignDateTimePair(tuple)
        services.persist().updateOne(tuple)
    }
}