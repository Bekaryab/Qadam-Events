
package com.bekaryab.qadamevents

import android.os.Bundle
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
import com.bekaryab.qadamevents.ui.navigation.AppNavGraph
import com.bekaryab.qadamevents.ui.theme.QadamEventsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AppModule.authViewModelFactory

        val loginViewModel = ViewModelProvider(
            this,
            factory
        )[LoginViewModel::class.java]

        val registerViewModel = ViewModelProvider(
            this,
            factory
        )[RegisterViewModel::class.java]

        setContent {
            QadamEventsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        loginViewModel = loginViewModel,
                        registerViewModel = registerViewModel
                    )
                }
            }
        }
    }
}
