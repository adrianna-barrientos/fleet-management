package co.tcc.koga.android.ui.splash_screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import kotlinx.coroutines.Job

import javax.inject.Inject

class SplashScreenFragment : Fragment(R.layout.splash_screen_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SplashScreenViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.initApp()
        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { state ->
            when (state) {
                SplashScreenUiState.LoggedIn -> findNavController().navigate(
                    R.id.action_splashScreenFragment_to_chatsFragment,
                )
                SplashScreenUiState.LoggedOut -> findNavController().navigate(
                    R.id.action_splashScreenFragment_to_loginFragment,
                )
               SplashScreenUiState.Error -> showErrorToast()
                else -> Log.e("Splashscreen", "inicializando o app...")
            }

        }
    }


    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Erro ao inicializar o app.", Toast.LENGTH_LONG)
            .show()
    }

}