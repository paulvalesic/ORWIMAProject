import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    // Podaci o korisniku
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var friendsList = mutableStateOf<List<String>>(emptyList())

    // Metoda za postavljanje podataka
    fun setUserData(username: String, email: String, friends: List<String>) {
        this.username.value = username
        this.email.value = email
        this.friendsList.value = friends
    }
}
