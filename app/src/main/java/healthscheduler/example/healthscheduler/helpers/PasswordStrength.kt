package healthscheduler.example.healthscheduler.helpers

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.MutableLiveData
import healthscheduler.example.healthscheduler.R
import java.util.regex.Matcher
import java.util.regex.Pattern

class PasswordStrength : TextWatcher {

    var strengthLevel: MutableLiveData<String> = MutableLiveData()
    var strengthColor: MutableLiveData<Int> = MutableLiveData()
    var lowerCase:     MutableLiveData<Int> = MutableLiveData()
    var upperCase:     MutableLiveData<Int> = MutableLiveData()
    var digit:         MutableLiveData<Int> = MutableLiveData()
    var specialChar:   MutableLiveData<Int> = MutableLiveData()

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    //ao introduzir a palavra passe verifica cada carater e atribui um valor 1 se for verdadeiro e 0 se for falso
    override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if(char != null) {
            lowerCase.value     = if (char.hasLowerCase()) {1} else {0}
            upperCase.value     = if (char.hasUpperCase()) {1} else {0}
            digit.value         = if (char.hasDigit()) {1} else {0}
            specialChar.value   = if (char.hasSpecialChar()) {1} else {0}
            calculateStrength(char)
        }
    }

    //verifica a forca da palavra passe consoante os valores anteriores e o comprimento da palavra passe
    private fun calculateStrength(password: CharSequence){
        if(password.length in 0..7){
            strengthColor.value = R.color.weak
            strengthLevel.value = "FRACA"
        }else if(password.length in 8..10){
            if(lowerCase.value == 1 || upperCase.value == 1 || digit.value == 1 || specialChar.value == 1){
                if(lowerCase.value == 1 && upperCase.value == 1) {
                    strengthColor.value = R.color.medium
                    strengthLevel.value = "MÉDIA"
                }
            }
        }else if(password.length > 10){
            if(lowerCase.value == 1 || upperCase.value == 1 || digit.value == 1 || specialChar.value == 1){
                if(lowerCase.value == 1 && upperCase.value == 1){
                    strengthColor.value = R.color.strong
                    strengthLevel.value = "FORTE"
                }
            }
        }
    }

    private fun CharSequence.hasLowerCase(): Boolean{
        val pattern: Pattern = Pattern.compile("[a-z]")
        val hasLowerCase: Matcher = pattern.matcher(this)
        return hasLowerCase.find()
    }

    private fun CharSequence.hasUpperCase(): Boolean{
        val pattern: Pattern = Pattern.compile("[A-Z]")
        val hasUpperCase: Matcher = pattern.matcher(this)
        return hasUpperCase.find()
    }

    private fun CharSequence.hasDigit(): Boolean{
        val pattern: Pattern = Pattern.compile("[0-9]")
        val hasDigit: Matcher = pattern.matcher(this)
        return hasDigit.find()
    }

    private fun CharSequence.hasSpecialChar(): Boolean{
        val pattern: Pattern = Pattern.compile("[!@#£$§%&/|_()]")
        val hasSpecialChar: Matcher = pattern.matcher(this)
        return hasSpecialChar.find()
    }
}