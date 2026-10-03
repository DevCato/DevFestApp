package pe.gdg.open.devfest.app.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Recuerda la Activity visible. Los flujos de login de Google, Apple y GitHub necesitan una
 * Activity para mostrar su interfaz.
 */
class CurrentActivityHolder(application: Application) : Application.ActivityLifecycleCallbacks {

    private var reference: WeakReference<Activity>? = null

    val current: Activity? get() = reference?.get()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        reference = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        reference = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (reference?.get() === activity) reference = null
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
