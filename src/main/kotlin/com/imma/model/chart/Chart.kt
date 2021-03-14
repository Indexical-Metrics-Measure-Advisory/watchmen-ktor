package com.imma.model.chart

enum class ChartType(val type: String) {
    count("count"),
    bar("bar"),
    line("line"),
    scatter("scatter"),
    pie("pie"),
    doughnut("doughnut"),
    nightingale("nightingale"),
    sunburst("sunburst"),
    tree("tree"),
    treemap("treemap"),
    map("map");
}

data class Chart(
    var type: ChartType = ChartType.count,
    var settings: Any? = null
)