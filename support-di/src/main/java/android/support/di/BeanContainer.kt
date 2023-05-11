package android.support.di


@Suppress("unchecked_cast")
class BeanContainer(private val notFoundCallback: BeanNotFoundCallback) : BeanLocator {
    private var register: BeanRegister? = null
    private val mBean = hashMapOf<Class<*>, Bean<*>>()

    override fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) {
            checkAnnotation(clazz)
        }
        if (mBean.containsKey(clazz)) return mBean[clazz] as Bean<T>
        error("Not found ${clazz.name}")
    }

    fun contains(clazz: Class<*>): Boolean {
        return mBean.containsKey(clazz)
    }

    operator fun <T> set(clazz: Class<T>, value: Bean<T>) {
        mBean[clazz] = value
    }

    operator fun <T> get(clazz: Class<T>): Bean<T> {
        return mBean[clazz] as Bean<T>
    }

    private fun <T> checkAnnotation(clazz: Class<T>) {
        when {
            register?.registry(clazz, notFoundCallback) == true -> {}
            clazz.isInterface -> {
                val annotation = clazz.getAnnotation(InjectBy::class.java)
                if (annotation != null) @Suppress("unchecked_cast")
                return notFoundCallback.onDefinitionNotFounded(
                    clazz,
                    annotation.clazz.java as Class<out T>,
                    annotation.share
                )

                val annotation1 = clazz.getAnnotation(InjectScopeBy::class.java)
                    ?: error("Not found provider for ${clazz.simpleName}")
                notFoundCallback.onDefinitionNotFounded(
                    clazz,
                    annotation1.clazz.java as Class<out T>,
                    annotation1.names
                )
            }
            else -> {
                val annotation = clazz.getAnnotation(Inject::class.java)
                if (annotation != null) {
                    return notFoundCallback.onDefinitionNotFounded(clazz, clazz, annotation.share)
                }

                val annotation1 = clazz.getAnnotation(InjectScope::class.java)
                    ?: error("Not found declaration for ${clazz.simpleName}")

                notFoundCallback.onDefinitionNotFounded(clazz, clazz, annotation1.names)
            }
        }
    }

    fun clear() {
        mBean.forEach { (_, v) -> v.close() }
        mBean.clear()
    }

    fun setBeanRegister(register: BeanRegister) {
        this.register = register
    }

}

interface BeanNotFoundCallback {
    fun <T> onDefinitionNotFounded(
        keyClass: Class<T>,
        implementClass: Class<out T>,
        shareIn: ShareScope,
    )

    fun <T> onDefinitionNotFounded(
        keyClass: Class<T>,
        implementClass: Class<out T>,
        shareIn: Array<out String>,
    )
}