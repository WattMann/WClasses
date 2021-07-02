package eu.warfaremc.wclasses.misc

fun Double.format(digits: Int) = "%.${digits}f".format(this)