package android.support.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(
    val share: ShareScope = ShareScope.None
)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectBy(
    val clazz: KClass<out Injectable>,
    val share: ShareScope = ShareScope.None
)

interface Injectable

class DependenceContext : ProvideContext() {

    private val mScopeBean = hashMapOf<String, Scope>()
    private val mBean = hashMapOf<Class<*>, Bean<*>>()
    private lateinit var mApplication: ApplicationBean
    private val mGlobalLookup = GlobalLookupContext()
    val globalLookup: LookupContext get() = mGlobalLookup

    internal fun set(application: Application) {
        mApplication = ApplicationBean(application)
    }

    private fun error(clazz: Class<*>) {
        error("Class ${clazz.simpleName} defined, please set override true to override this")
    }

    override fun <T> single(
        override: Boolean,
        clazz: Class<T>,
        function: LookupContext.() -> T
    ) {
        if (mBean.containsKey(clazz) && !override) error(clazz)
        mBean[clazz] = SingletonBean { function(mGlobalLookup) }
    }

    override fun <T> factory(
        override: Boolean,
        shareIn: ShareScope,
        clazz: Class<T>,
        function: LookupContext.() -> T
    ) {
        if (mBean.containsKey(clazz) && !override) error(clazz)
        if (shareIn == ShareScope.Singleton) {
            return single(override, clazz, function)
        }
        mBean[clazz] = FactoryBean(shareIn, clazz) { function(it ?: mGlobalLookup) }
    }

    override fun <T> scope(scopeId: String, clazz: Class<T>, function: LookupContext.() -> T) {
        val scope = getScope(scopeId)
        if (scope.contains(clazz)) error("Class ${clazz.simpleName} exist in scope $scopeId")
        scope.factory(clazz) { function() }
    }

    fun getScope(scopeId: String): Scope {
        return if (!mScopeBean.containsKey(scopeId))
            SimpleScope(this).also { mScopeBean[scopeId] = it }
        else mScopeBean[scopeId]!!
    }

    override fun modules(vararg module: Module) {
        module.forEach { it.provide() }
    }

    @Suppress("unchecked_cast")
    fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) {
            if (clazz.isAssignableFrom(Application::class.java)
                || clazz.isAssignableFrom(Context::class.java)
            ) return mApplication as Bean<T>
            reflectProvideIfNeeded(clazz)
        }
        if (mBean.containsKey(clazz)) return mBean[clazz] as Bean<T>
        error("Not found ${clazz.name}")
    }

    private fun <T> reflectProvideIfNeeded(clazz: Class<T>) {
        when {
            ViewModel::class.java.isAssignableFrom(clazz) -> reflectProvide(
                clazz,
                clazz,
                ShareScope.None
            )
            clazz.isInterface -> provideByInjectBy(clazz)
            else -> provideByInject(clazz)
        }
    }

    @Suppress("unchecked_cast")
    private fun <T> provideByInjectBy(clazz: Class<T>) {
        val annotation = clazz.getAnnotation(InjectBy::class.java)
            ?: error("Not found provider for ${clazz.simpleName}")
        reflectProvide(
            clazz,
            annotation.clazz.java as Class<out T>,
            annotation.share
        )
    }

    private fun <T> provideByInject(clazz: Class<T>) {
        val annotation = clazz.getAnnotation(Inject::class.java)
            ?: error("Not found declaration for ${clazz.simpleName}")

        reflectProvide(clazz, clazz, annotation.share)
    }

    private fun <T> reflectProvide(
        clazz: Class<T>,
        implementClazz: Class<out T>,
        share: ShareScope
    ) {
        if (share == ShareScope.Singleton) {
            single(clazz = clazz) {
                InstanceFactory.create(implementClazz, this)
            }
            return
        }

        mBean[clazz] = FactoryBean(share, clazz) {
            InstanceFactory.create(implementClazz, it ?: mGlobalLookup)
        }
    }

    fun <T> getOrNull(clazz: Class<T>, owner: LifecycleOwner): T? {
        val bean = lookup(clazz)
        if (bean is FactoryBean) {
            return bean.getValue(ShareLookupContext(owner))
        }
        return bean.getValue()
    }

    fun <T> getOrNull(clazz: Class<T>): T? {
        return mGlobalLookup.getOrNull(clazz)
    }

    fun <T> getOrNull(scopeId: String, clazz: Class<T>): T? {
        return mGlobalLookup.getOrNull(scopeId, clazz)
    }

    fun <T> get(clazz: Class<T>, owner: LifecycleOwner): T {
        return getOrNull(clazz, owner) ?: error("Not found bean ${clazz.simpleName}")
    }

    fun <T> get(clazz: Class<T>): T {
        return getOrNull(clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    fun <T> get(scopeId: String, clazz: Class<T>): T {
        return getOrNull(scopeId, clazz) ?: error("Not found bean ${clazz.simpleName}")
    }

    inline fun <reified T> get(scopeId: String): T {
        return get(scopeId, T::class.java)
    }

    inline fun <reified T> get(owner: LifecycleOwner): T {
        return get(T::class.java, owner)
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }

    @Suppress("unchecked_cast")
    private inner class GlobalLookupContext : LookupContext() {
        override fun <T> getOrNull(clazz: Class<T>): T {
            return lookup(clazz).getValue()
        }

        override fun <T> getOrNull(scopeId: String, clazz: Class<T>): T {
            return getScope(scopeId).lookup(clazz).getValue()
        }
    }

    @Suppress("unchecked_cast")
    private inner class ShareLookupContext(override val owner: LifecycleOwner) : LookupContext(),
        LifecycleLookup {
        override fun <T> getOrNull(clazz: Class<T>): T? {
            val bean = lookup(clazz)
            if (bean is FactoryBean<*>) {
                return (bean as FactoryBean<T>).getValue(this)
            }
            return bean.getValue()
        }

        override fun <T> getOrNull(scopeId: String, clazz: Class<T>): T? {
            return getScope(scopeId).lookup(clazz).getValue()
        }
    }
}