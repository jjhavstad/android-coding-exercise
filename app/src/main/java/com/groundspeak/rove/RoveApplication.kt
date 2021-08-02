package com.groundspeak.rove

import android.app.Application
import com.groundspeak.rove.datasources.api.ApiDestinationDataSource
import com.groundspeak.rove.datasources.api.retrofit.RetrofitDestinationApiCreator
import com.groundspeak.rove.datasources.api.retrofit.RetrofitDestinationDataSource
import com.groundspeak.rove.datasources.local.LocalDestinationDataSource
import com.groundspeak.rove.datasources.local.room.DestinationDatabaseCreator
import com.groundspeak.rove.datasources.local.room.RoomDestinationDataSource
import com.groundspeak.rove.viewmodels.DestinationViewModel
import com.groundspeak.rove.viewmodels.LocationViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
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
            single {
                DestinationDatabaseCreator.create(
                    RoveApplication@this.androidContext()
                )
            }
            single<ApiDestinationDataSource> {
                RetrofitDestinationDataSource(get())
            }
            single<LocalDestinationDataSource> {
                RoomDestinationDataSource(get())
            }
            viewModel {
                DestinationViewModel(get(), get())
            }
            viewModel {
                LocationViewModel()
            }
            single {
                NetworkStateListener()
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@RoveApplication)
            modules(appModule)
        }
    }
}
