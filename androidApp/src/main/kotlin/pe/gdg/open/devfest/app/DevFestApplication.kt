package pe.gdg.open.devfest.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pe.gdg.open.devfest.app.di.initKoin

class DevFestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@DevFestApplication) }
    }
}
