package android.support.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel


@Suppress("unchecked_cast")
class BeanContainer(private val notFoundCallback: BeanNotFoundCallback) : BeanLocator {
    private lateinit var mApplicationBean: ApplicationBean
    private val mBean = hashMapOf<Class<*>, Bean<*>>()

    override fun <T> lookup(clazz: Class<T>): Bean<T> {
        if (!mBean.containsKey(clazz)) {
            if (clazz.isAssignableFrom(Application::class.java)
                || clazz.isAssignableFrom(Context::class.java)
            ) {
                if (!::mApplicationBean.isInitialized) {
                    error("Application is not set yet")
                }
                return mApplicationBean as Bean<T>
            }
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
            ViewModel::class.java.isAssignableFrom(clazz) -> {
                notFoundCallback.onDefinitionNotFounded(
                    clazz,
                    clazz,
                    ShareScope.None
                )
            }
            clazz.isInterface -> {
                val annotation = clazz.getAnnotation(InjectBy::class.java)
                    ?: error("Not found provider for ${clazz.simpleName}")
                @Suppress("unchecked_cast")
                notFoundCallback.onDefinitionNotFounded(
                    clazz,
                    annotation.clazz.java as Class<out T>,
                    annotation.share
                )
            }
            else -> {
                val annotation = clazz.getAnnotation(Inject::class.java)
                    ?: error("Not found declaration for ${clazz.simpleName}")

                notFoundCallback.onDefinitionNotFounded(clazz, clazz, annotation.share)
            }
        }
    }

    fun set(app: Application) {
        if (::mApplicationBean.isInitialized && !mApplicationBean.isDiff(app)) return
        mApplicationBean = ApplicationBean(app)
        mBean.forEach { (_, v) -> v.close() }
        mBean.clear()
    }
}

interface BeanNotFoundCallback {
    fun <T> onDefinitionNotFounded(
        keyClass: Class<T>,
        implementClass: Class<out T>,
        shareIn: ShareScope,
    )
}