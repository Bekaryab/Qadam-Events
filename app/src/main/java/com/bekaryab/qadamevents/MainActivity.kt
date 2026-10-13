
package com.bekaryab.qadamevents

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.bekaryab.qadamevents.di.AppModule
import com.bekaryab.qadamevents.ui.auth.LoginViewModel
import com.bekaryab.qadamevents.ui.auth.RegisterViewModel
import com.bekaryab.qadamevents.ui.events.list.EventsRoute
import com.bekaryab.qadamevents.ui.events.list.EventsViewModel
import com.bekaryab.qadamevents.ui.events.list.EventsViewModelFactory
import com.bekaryab.qadamevents.ui.navigation.AppNavGraph
import com.bekaryab.qadamevents.ui.theme.QadamEventsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // Debug-only SIS 4 entry point: honored only in debug builds, so a
        // release build always takes the production path below.
        val debugEvents = BuildConfig.DEBUG &&
            intent.getBooleanExtra(EXTRA_DEBUG_EVENTS, false)
        if (BuildConfig.DEBUG &&
            intent.getBooleanExtra(EXTRA_DEBUG_SHOULD_FAIL, false)
        ) {
            AppModule.eventRepository.shouldFail = true
        }

        val factory = AppModule.authViewModelFactory

        val loginViewModel = ViewModelProvider(
            this,
            factory
        )[LoginViewModel::class.java]

        val registerViewModel = ViewModelProvider(
            this,
            factory
        )[RegisterViewModel::class.java]

        // Obtained from the Activity's ViewModelStore (same pattern as the
        // auth ViewModels), so rotation returns the SAME instance instead of
        // creating a new one. Created only for the debug path.
        val eventsViewModel = if (debugEvents) {
            ViewModelProvider(
                this,
                EventsViewModelFactory(AppModule.eventRepository)
            )[EventsViewModel::class.java]
        } else {
            null
        }

        setContent {
            QadamEventsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (debugEvents && eventsViewModel != null) {
                        EventsRoute(viewModel = eventsViewModel)
                    } else {
                        AppNavGraph(
                            loginViewModel = loginViewModel,
                            registerViewModel = registerViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private companion object {
        const val TAG = "MainActivity"
        const val EXTRA_DEBUG_EVENTS = "debug_events"
        const val EXTRA_DEBUG_SHOULD_FAIL = "debug_should_fail"
    }
}
