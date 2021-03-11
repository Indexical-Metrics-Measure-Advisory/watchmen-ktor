package com.imma.model.chart

enum class ChartType(val type: String) {
    COUNT("count"),
    BAR("bar"),
    LINE("line"),
    SCATTER("scatter"),
    PIE("pie"),
    DOUGHNUT("doughnut"),
    NIGHTINGALE("nightingale"),
    SUNBURST("sunburst"),
    TREE("tree"),
    TREEMAP("treemap"),
    MAP("map")
}

data class Chart(
    var type: ChartType = ChartType.COUNT,
    var settings: Any? = null
)