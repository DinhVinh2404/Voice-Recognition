package com.example.voicerecognition
//import các thành phần trong các gói cần thiết
import android.Manifest//Sử dụng các lớp trong android.Manifest
import android.content.pm.PackageManager//truy cập các gói được cài đặt trên thiết bị, quản lý quyền của ứng dụng
import android.os.Bundle//đóng gói và truyền các thành phần ứng dụng
import android.speech.RecognitionListener//Giao diện nhận dạng: thông báo bắt đầu, kết thúc, bị lỗi
import android.speech.RecognizerIntent// gửi yêu cầu nhận dạng giọng nói
import android.speech.SpeechRecognizer//nhận dạng giọng nói người dùng:bắt đầu, dừng, phân tích dữ liệu
import android.widget.Button// thêm tiện ích từ gói android.widget
//import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity//Tương thích ngược: Sử dụng một số tính năng mới trên Android cũ
import androidx.core.app.ActivityCompat// Làm việc với các hoạt động trong ứng dụng Android
import androidx.core.content.ContextCompat// Truy cập tài nguyên hệ thống, tương tác với các thành phần khác của ứng dụng
import android.content.Intent//Thực hiện hoạt động trong hệ thống: ví dụ kích hoạt chức năng gửi email
import android.widget.EditText
import android.widget.Toast//Hiển thị thông báo ngắn
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.BroadcastReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer// late-initialized
    private lateinit var startButton: Button
    private lateinit var resultEditText: EditText // Sửa TextView thành EditText
    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
//    private lateinit var alarmReceiver: AlarmReceiver

    inner class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Xử lý logic khi nhận được báo thức
        }
    }
//Phương thức onCreate() được gọi khi hoạt động được tạo ra.
    // Trong phương thức này, giao diện người dùng được thiết lập và các trình lắng nghe sự kiện được khởi tạo
    override fun onCreate(savedInstanceState: Bundle?) {//override: ghi đè lên lớp cha
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    // Khởi tạo các biến
    alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    resultEditText = findViewById(R.id.resultEditText)
    context = this

//Gán các view từ layout tương ứng với id
        startButton = findViewById(R.id.startButton)
        //resultTextView = findViewById(R.id.resultTextView)
        resultEditText = findViewById(R.id.resultEditText)
//Kiểm tra quyền
        startButton.setOnClickListener {
            checkPermission()
        }
//Tạo một đối tượng SpeechRecognizer và thiết lập một RecognitionListener để xử lý các sự kiện nhận dạng giọng nói.
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            //Các phương thức của RecognitionListener
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    //resultTextView.text = matches[0]
                    resultEditText.setText(matches[0])
                }
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0].lowercase()
                    if (command.contains("mở youtube")||
                        command.contains("mở ứng dụng youtube")||
                        command.contains("mở phần mềm youtube")) {
                        openYouTube()
                    }
                    if (command.contains("Đặt báo thức")||
                        command.contains("Cài báo thức")||
                        command.contains("Hẹn báo thức")) {
                        handleVoiceCommand(command)
                    }
                }
            }
            private fun openYouTube() {
                val packageManager = packageManager
                val intent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                if (intent != null) {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    startActivity(intent)
                } else {
                    // Xử lý khi không tìm thấy ứng dụng YouTube trên thiết bị
                    Toast.makeText(applicationContext, "Không thấy ứng dụng YouTube", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }
//    private fun replaceWords(input: String): String {
//        var replacedText = input
//        val wordMap = mapOf(
//            "Dô diên" to "vô duyên",
//            "Bình thủy" to "phích nước",
//            "Buồn xo" to "buồn quá",
//            "Chì" to "giỏi",
//            "Chạm lụt" to "khờ, chậm chạm",
//            "Chén" to "bát",
//            "Dĩa" to "đĩa"
//        )
// Kiểm tra quyền, nếu chưa thì yêu cầu người dùng cấp quyền
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            startSpeechRecognition()
        }
    }
// Nếu được cấp quyền, bắt đầu quá trình nhận dạng
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechRecognition()
        }
    }

    // Khởi tạo intent nhận dạng giọng nói bằng tiếng việt
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
//        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Thiết lập tính năng nhận dạng ngoại tuyến
        speechRecognizer.startListening(intent)
    }
// Xác định mã yêu cầu cấp quyền record
    companion object {
        const val RECORD_AUDIO_PERMISSION_CODE = 1
    }
    // Xử lý lệnh của người dùng
    private fun handleVoiceCommand(command: String) {
        if (command.contains("Đặt báo thức")) {
            val time = extractTime(command)
            if (time != null) {
                setAlarm(time)
            } else {
                Toast.makeText(context, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Rút trích thời gian từ lệnh người dùng
    private fun extractTime(command: String): Calendar? {
        val timeRegex = Regex("""(\d{1,2}):(\d{2})""")
        val matchResult = timeRegex.(command)

        if (matchResult != null) {
            val (hourStr, minuteStr) = matchResult.destructured
            val hour = hourStr.toInt()find
            val minute = minuteStr.toInt()

            // Tạo một đối tượng Calendar và đặt thời gian
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            return calendar
        }

        return null
    }


    // Cài đặt báo thức
    private fun setAlarm(time: Calendar) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
        Toast.makeText(context, "Báo thức đã được đặt", Toast.LENGTH_SHORT).show()
    }

}
