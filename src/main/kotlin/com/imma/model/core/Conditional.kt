package com.imma.model.core

import com.imma.model.compute.ParameterJointDelegate

interface Conditional {
    var conditional: Boolean
    var on: ParameterJointDelegate
}
