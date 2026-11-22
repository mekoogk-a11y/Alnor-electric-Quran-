package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AlNoorAppRoot
import com.example.ui.viewmodel.ExploreViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MushafViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as AlNoorApplication).container

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.provideFactory(container)
            )
            val mushafViewModel: MushafViewModel = viewModel(
                factory = MushafViewModel.provideFactory(container)
            )
            val exploreViewModel: ExploreViewModel = viewModel(
                factory = ExploreViewModel.provideFactory(container)
            )
            AlNoorAppRoot(
                mainViewModel = mainViewModel,
                mushafViewModel = mushafViewModel,
                exploreViewModel = exploreViewModel
            )
        }
    }
}
