package pl.skotar.intellij.plugin.alfrescojvmconsole.extension

import org.apache.commons.lang3.exception.ExceptionUtils

fun Throwable.toFullString(): String =
    ExceptionUtils.getStackTrace(this)