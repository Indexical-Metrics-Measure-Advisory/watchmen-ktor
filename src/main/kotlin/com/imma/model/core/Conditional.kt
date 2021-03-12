package com.imma.model.core

import com.imma.model.compute.ParameterJoint

interface Conditional {
    var conditional: Boolean
    var on: ParameterJoint
}
