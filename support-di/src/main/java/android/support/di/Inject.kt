package android.support.di

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(
    val share: ShareScope = ShareScope.None,
)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectBy(
    val clazz: KClass<out Injectable>,
    val share: ShareScope = ShareScope.None,
)

interface Injectable