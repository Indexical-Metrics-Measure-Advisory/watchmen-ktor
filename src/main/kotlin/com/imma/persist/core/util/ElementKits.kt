package com.imma.persist.core.util

import com.imma.model.core.compute.ValueKits
import com.imma.persist.core.Element
import com.imma.persist.core.ElementShouldBe

class ElementKits {
	companion object {
		fun to(value: Any?, shouldBe: ElementShouldBe, element: Element): Any? {
			if (value == null) {
				return null
			}

			return when (shouldBe) {
				ElementShouldBe.any -> value
				ElementShouldBe.collection -> ValueKits.computeToCollection(value)
				ElementShouldBe.numeric -> ValueKits.computeToNumeric(value) {
					"Cannot cast given value[$value] to number, which is computed by element[$element]."
				}
				ElementShouldBe.date -> ValueKits.computeToDate(value) {
					"Cannot cast given value[$value] to date, which is computed by element[$element]."
				}
			}
		}
	}
}