package android.support.navigation

object DestinationTag {

    fun create(destination: Destination): String {
        return with(destination) {
            val keep = if (keepInstance) "single" else "new"
            "navigator:destination:${kClass.java.simpleName}:tag:$keep:$tagId"
        }
    }
}