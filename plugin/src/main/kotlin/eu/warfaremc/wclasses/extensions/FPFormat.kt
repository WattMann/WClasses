package eu.warfaremc.wclasses.extensions

fun Double.format(digits: Int) = "%.${digits}f".format(this)