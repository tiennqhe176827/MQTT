package Activity

import androidx.lifecycle.MutableLiveData

object MqttDataRepository {
    val temperature = MutableLiveData<Double>()
    val humidity = MutableLiveData<Double>()
}