package com.groundspeak.rove

import android.app.Application
import com.groundspeak.rove.datasources.api.ApiDestinationDataSource
import com.groundspeak.rove.datasources.api.retrofit.RetrofitDestinationApi
import com.groundspeak.rove.datasources.api.retrofit.RetrofitDestinationApiCreator
import com.groundspeak.rove.datasources.api.retrofit.RetrofitDestinationDataSource
import com.groundspeak.rove.viewmodels.DestinationViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RoveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        inject()
    }

    private fun inject() {
        val appModule = module {
            single {
                RetrofitDestinationApiCreator.create(
                    baseUrl = "https://run.mocky.io/",
                    httpClientBuilder = OkHttpClient.Builder()
                )
            }
            single<ApiDestinationDataSource> {
                RetrofitDestinationDataSource(get())
            }
            viewModel {
                DestinationViewModel(get())
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@RoveApplication)
            modules(appModule)
        }
    }
}
