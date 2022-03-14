package android.support.di

import android.app.Application
import androidx.lifecycle.LifecycleOwner
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

class DependenceContext : ProvideContext() {

    private val mBeanContainer = BeanContainer(object : BeanNotFoundCallback {
        override fun <T> onDefinitionNotFounded(
            keyClass: Class<T>,
            implementClass: Class<out T>,
            shareIn: ShareScope,
        ) {
            registry(false, shareIn, keyClass, ReflectNewInstanceFactory(implementClass))
        }

        override fun <T> onDefinitionNotFounded(
            keyClass: Class<T>,
            implementClass: Class<out T>,
            shareIn: Array<out String>,
        ) {
            registry(false, shareIn, keyClass, ReflectNewInstanceFactory(implementClass))
        }
    })

    private val mGlobalLookup = GlobalLookupContext(mBeanContainer)

    internal fun set(application: Application) {
        mBeanContainer.set(application)
    }

    private fun error(clazz: Class<*>) {
        error("Class ${clazz.simpleName} defined, please set override true to override this")
    }

    private fun <T> registry(
        override: Boolean,
        shareIn: ShareScope,
        clazz: Class<T>,
        instanceFactory: InstanceFactory<T>,
    ) {
        if (mBeanContainer.contains(clazz) && !override) error(clazz)

        mBeanContainer[clazz] = when (shareIn) {
            ShareScope.Singleton -> SingletonBean(instanceFactory)
            ShareScope.Activity -> ActivityScopeBean(clazz, instanceFactory)
            ShareScope.Fragment -> FragmentScopeBean(clazz, instanceFactory)
            ShareScope.FragmentOrActivity -> FragmentOrActivityScopeBean(clazz, instanceFactory)
            else -> DefaultFactoryBean(instanceFactory)
        }
    }

    private fun <T> registry(
        override: Boolean,
        shareIn: Array<out String>,
        clazz: Class<T>,
        instanceFactory: InstanceFactory<T>,
    ) {
        if (mBeanContainer.contains(clazz) && !override) error(clazz)
        mBeanContainer[clazz] = NamedScopeBean(shareIn, clazz, instanceFactory)
    }

    override fun <T> single(
        override: Boolean,
        clazz: Class<T>,
        function: LookupContext.() -> T,
    ) {
        registry(override, ShareScope.Singleton, clazz, ProvideInstanceFactory(function))
    }

    override fun <T> factory(
        override: Boolean,
        shareIn: ShareScope,
        clazz: Class<T>,
        function: LookupContext.() -> T,
    ) {
        registry(override, shareIn, clazz, ProvideInstanceFactory(function))
    }

    override fun <T> factory(
        override: Boolean,
        shareIn: Array<out String>,
        clazz: Class<T>,
        function: LookupContext.() -> T,
    ) {
        registry(override, shareIn, clazz, ProvideInstanceFactory(function))
    }

    override fun modules(vararg module: Module) {
        module.forEach { it.provide() }
    }

    fun <T> getOrNull(clazz: Class<T>, owner: LifecycleOwner): T? {
        return mBeanContainer.lookup(clazz)
            .getValue(LifecycleLookupContext(mGlobalLookup, owner))
    }

    fun <T> getOrNull(clazz: Class<T>): T? {
        return mGlobalLookup.getOrNull(clazz)
    }

    fun <T> get(clazz: Class<T>, owner: LifecycleOwner): T {
        return getOrNull(clazz, owner) ?: error("Not found bean ${clazz.simpleName}")
    }

    fun <T> get(clazz: Class<T>): T {
        return getOrNull(clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    inline fun <reified T> get(owner: LifecycleOwner): T {
        return get(T::class.java, owner)
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }
}