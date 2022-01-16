package android.support.navigation

interface Backable {
    fun onInterceptBackPress(): Boolean
}