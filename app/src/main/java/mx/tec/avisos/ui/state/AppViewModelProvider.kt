package mx.tec.avisos.ui.state

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import mx.tec.avisos.AvisosApplication

/**
 * Cómo se construye cada ViewModel de la app. Igual que en la Práctica 5: los
 * repositorios necesitan un Context para existir, así que los ViewModels ya
 * no se construyen solos.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {

        initializer { SesionViewModel(avisosApplication().container.sesionRepository) }

        initializer { LoginViewModel(avisosApplication().container.sesionRepository) }

        initializer { AvisosViewModel(avisosApplication().container.avisosRepository) }

        initializer { PublicarViewModel(avisosApplication().container.avisosRepository) }
    }
}

/** El atajo para llegar al contenedor desde dentro de un initializer. */
private fun CreationExtras.avisosApplication(): AvisosApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AvisosApplication
