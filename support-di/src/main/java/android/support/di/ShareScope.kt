package android.support.di

import kotlin.reflect.KClass

enum class ShareScope {
    None,
    Activity,
    FragmentOrActivity,
    Fragment,
    Singleton,
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NamedScope(val name: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectScope(vararg val names: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectScopeBy(
    val clazz: KClass<out Injectable>,
    vararg val names: String,
)