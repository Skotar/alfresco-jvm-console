package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

object AlfrescoJvmConsoleRunConfigurationValidator {

    object Host {
        fun validate(value: String): Boolean =
            value.isNotBlank()
    }

    object Path {
        fun validate(value: String): Boolean =
            value.isNotBlank()
    }

    object Port {
        val RANGE = 1..65535

        fun validate(value: Int): Boolean =
            RANGE.contains(value)
    }

    object Username {
        fun validate(value: String): Boolean =
            value.isNotBlank()
    }

    object Password {
        fun validate(value: String): Boolean =
            value.isNotBlank()
    }
}