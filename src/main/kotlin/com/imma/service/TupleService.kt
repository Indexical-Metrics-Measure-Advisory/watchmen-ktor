package com.imma.service

import com.imma.model.Tuple
import com.imma.model.assignDateTimePair
import com.imma.model.forceAssignDateTimePair
import io.ktor.application.*

open class TupleService(application: Application) : Service(application) {
    protected fun createTuple(tuple: Tuple) {
        forceAssignDateTimePair(tuple)
        this.writeIntoMongo { it.insert(tuple) }
    }

    protected fun updateTuple(tuple: Tuple) {
        assignDateTimePair(tuple)
        writeIntoMongo { it.save(tuple) }
    }
}