package com.example.wipay_iot_shop

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.*
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.transaction.*
import com.imohsenb.ISO8583.builders.ISOClientBuilder
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION
import com.imohsenb.ISO8583.exceptions.ISOClientException
import com.imohsenb.ISO8583.exceptions.ISOException
import github.nisrulz.screenshott.ScreenShott
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.experimental.and

class TransactionActivity : AppCompatActivity() {
    var appDatabase : AppDatabase? = null
    var reversalDAO : ReversalDao? = null
    var saleDAO : SaleDao? = null
    var flagReverseDAO : FlagReverseDao? = null
    var stuckReverseDAO : StuckReverseDao? = null
    var responseDAO : ResponseDao? = null

    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var processing = false
    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""
    var menuName:String = ""

    var output1: TextView? = null
    var output2: TextView? = null
    var stan: Int? = null
    var initialStan: Int? = 1244
    var reverseFlag :Boolean? = null
    var reversal: String? = null
    var responseCode: String? = null
    var reReversal: String? = null
    var reversalMsg: ISOMessage? = null
    var saleMsg: ISOMessage? = null
    var readSale: String? = null
    var readStan: Int? = null
    var stuckReverse :Boolean? = null
    var readFlagReverse :Boolean? = false
    var readStuckReverse :Boolean? = false
    var readResponseMsg:String? = null

    //get initial value from MenuActivity
    var settlementFlag:Boolean? = null
    var firstTransactionFlag:Boolean? = null
    var startId:Int = 0

//    val username = "phanida601@gmail.com"
//    val password = "1469900351198"

    private var main: View? = null
    private var bitmap: Bitmap? = null
    private val RC_WRITE_EXTERNAL_STORAGE = 123

    var TID: String = "3232323232323232"
    var MID: String = "323232323232323232323232323232"


//    private val HOST = "192.168.43.195"
//    var PORT = 5000
//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//      private val HOST = "192.168.178.187"
//    var PORT = 5000
    private val HOST = "192.168.43.24"
//      private val HOST = "192.168.68.107"
      var PORT = 3000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001

//    private val HOST = "203.148.160.47"
//    var PORT = 7500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)

        main = findViewById(R.id.mainActivity)
        val setOrder = findViewById<TextView>(R.id.order)
        val setAmount = findViewById<TextView>(R.id.amount)

        settlementFlag =  sp.getBoolean("settlementFlag",false)
        firstTransactionFlag = sp.getBoolean("firstTransactionFlag",true)

            intent.apply {
//          processing = getBooleanExtra("processing",false)
            totalAmount = getIntExtra("totalAmount",145)
            cardNO = getStringExtra("cardNO").toString()
            cardEXD = getStringExtra("cardEXD").toString()
            menuName = getStringExtra("menuName").toString()

        }

        setOrder.setText(menuName)
        setAmount.setText(totalAmount.toString())

        Log.d("log_tag","on transactionActivity.")
        Log.w("log_tag","settlementFlag: " + settlementFlag)
        Log.w("log_tag","firstTransactionFlag: " + firstTransactionFlag)

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

//        EventBus.getDefault().post(MessageEvent(
//            "runDBthread",
//            ""
//        ))

//      Check settlementFlag
        if(settlementFlag == true){

            setDialog("Transaction Error!!.","The sales report has not yet been completed.\n" +
                    "Must return to complete the sales report first.")
        }else{

            setDialogS("","Comfirm your order.")
            processing = true
        }


        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            Log.i("log_tag","processing: "+processing)
            manageProcessing()

        }, 7000)
    }

    override fun onResume() {
        super.onResume()

//        EventBus.getDefault().post(MessageEvent(
//            "runProcessing",
//            ""
//        ))

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        reversalDAO = appDatabase?.reversalDao()
        saleDAO = appDatabase?.saleDao()
        flagReverseDAO = appDatabase?.flagReverseDao()
        stuckReverseDAO = appDatabase?.stuckReverseDao()
        responseDAO = appDatabase?.responseDao()

    }

        @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

//        if(event.type == "runDBthread"){
//            Thread{
//                accessDatabase()
//                readStan = saleDAO?.getSale()?.STAN
//                Log.i("log_tag","readSTAN : " + readStan)
//            }.start()
//
//            if(readStan == null) {
//                stan = 1117
//            }
//        }
//        if(event.type == "runProcessing"){
//            manageProcessing()
//        }
//        else {
//
//        }

//            if(event.type == "afterApprove"){
//                sendEmailProcess()
//            }

            manageResponse(event)
    }

    fun manageProcessing(){

        stan = readStan
        if(readStan == null){
            stan = initialStan
        }

        reverseFlag = readFlagReverse
        if(readFlagReverse == null){
            reverseFlag = false
        }

        stuckReverse = readStuckReverse
        if(readStuckReverse == null){
            stuckReverse = false
        }

        if(processing == true){

            if (reverseFlag!!) {
                stuckReverse = true
                var reverseStuck = StuckReverseEntity(null, stuckReverse)

                Log.i("log_tag", "send reverse packet")
//                    sendPacket(reversalPacket(stan.toString()))
//                Log.i("log_tag", "reversal:  " + reReversal.toString())
                sendPacket(reBuildISOPacket(reReversal.toString()))

                Log.i("log_tag", "reverseFlag:  " + reverseFlag)

                Thread{

                    accessDatabase()
                    stuckReverseDAO?.insertStuckReverse(reverseStuck)
                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse!!
//                    Log.i("log_tag"," : " + readStan)

                }.start()

            }
            else
            {
                sendTransactionProcess()

            }

        }

    }


    fun manageResponse(event: MessageEvent){

        output1?.setText("Response Message: " + event.message)
        Log.i("log_tag", "Response Message:" + event.message)
        responseCode = codeUnpack(event.message,39)
        output2?.setText("response code: " + responseCode)
        var responseMsg = event.message
        Log.i("log_tag", "response code:"+ responseCode)
        //
        if(responseCode == "3030"){
            //???????????????????????????????????????????????????????????? if ???????????? else ???????????????????????????????????????????????????????????? reverse ???????????????????????????
            reverseFlag = false
            var flagReverse = FlagReverseEntity(null, reverseFlag)

            if(stuckReverse == true){ //manage reverse approve

                Log.i("log_tag", "Reversal Approve.")
                stuckReverse = false
                var reverseStuck = StuckReverseEntity(null, stuckReverse)
//                reversalApprove()
//                setDialog("Cenceling Success.","Successfully canceled the transaction.")

                var reStan = codeUnpack(reReversal.toString(),11)
//                var reversalApprove = SaleEntity(null,reReversal.toString(), reStan!!.toInt())
                //????????????????????????????????? reverse ?????????????????????????????? save stan ?????????????????? ???????????????????????? stan ???????????????????????????
                var reversalApprove = SaleEntity(null,null, reStan!!.toInt())
                var responseReversal = ResponseEntity(null,null)

                Thread{

                    accessDatabase()
                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    stuckReverseDAO?.insertStuckReverse(reverseStuck)
                    saleDAO?.insertSale(reversalApprove)
                    responseDAO?.insertResponseMsg(responseReversal)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
                    readStan = saleDAO?.getSale()?.STAN
                    Log.i("log_tag","saveReverse-sale :  " + readSale)
                    Log.i("log_tag","saveSTAN : " + readStan)
                    Log.w("log_tag","saveResponse : " + readResponseMsg)

                }.start()
                stan = stan?.plus(1)
                sendTransactionProcess()

            }else{      //manage transactionApprove


                Log.i("log_tag", "Transaction Approve.")
//                transactionApprove()
                setDialogApprove(null,"Transaction complete.")
                var saleApprove = SaleEntity(null,saleMsg.toString(),stan)
                var responseSaleApprove = ResponseEntity(null,responseMsg)


                Thread{

                    accessDatabase()
                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    saleDAO?.insertSale(saleApprove)
                    responseDAO?.insertResponseMsg(responseSaleApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
                    readStan = saleDAO?.getSale()?.STAN
                    startId = saleDAO?.getSale()?._id!!

                    if(firstTransactionFlag == true){

                        val editor: SharedPreferences.Editor = sp.edit()
                        editor.putInt("startId", startId)
                        editor.putBoolean("firstTransactionFlag", false)
                        editor.commit()

                        Log.i("log_tag","startId :  " + startId)
                    }

                    Log.w("log_tag","saveId :  " + startId)
                    Log.w("log_tag","firstTransactionFlag: " + sp.getBoolean("firstTransactionFlag",false))
                    Log.w("log_tag","saveTransaction :  " + readSale)
                    Log.w("log_tag","saveSTAN : " + readStan)
                    Log.w("log_tag","saveResponse : " + readResponseMsg)

                }.start()


//               screenshortProcess()
                bitmap = ScreenShott.getInstance().takeScreenShotOfView(main)
                screenshotTask()

            }

        }else{

            if(stuckReverse == true){ //?????????????????? reverse ????????????

                errorCode(responseCode,null)

            } else{ //manage Transaction Error

                reverseFlag = false  //?????????????????? response ????????? transaction error
                var flagReverse = FlagReverseEntity(null, reverseFlag)

                if(responseCode == "3934"){

                    errorCode(responseCode,"Seqence error / Duplicate transmission")

                }else{

                    errorCode(responseCode,null)

                }

                Log.i("log_tag", "Error code: " + responseCode)
                var transactionError = SaleEntity(null,null,stan)
                var responseSaleError = ResponseEntity(null,null)

                Thread{

                    accessDatabase()

                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    saleDAO?.insertSale(transactionError)
                    responseDAO?.insertResponseMsg(responseSaleError)
                    readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    startId = saleDAO?.getSale()?._id!!


                    Log.w("log_tag","saveTransaction :  " + readSale)
                    Log.w("log_tag","saveSTAN : " + readStan)
                    Log.w("log_tag","saveResponse : " + readResponseMsg)


                }.start()

            }

        }

//        val policy = ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        Log.i("log_tag", "reverseFlag:  " + reverseFlag)

    }

//    fun sendEmailProcess(){
//
//        Log.i("log_tag","send email.")
//        //Send Email Slip
//        val _txtEmail = "phanida.lip@gmail.com"
//        val username = "phanida601@gmail.com"
//        val password = "1469900351198"
//        val messageToSend = "test send eamil wipay shop."
//        val props = Properties()
//        props["mail.smtp.auth"] = "true"
//        props["mail.smtp.starttls.enable"] = "true"
//        props["mail.smtp.host"] = "smtp.gmail.com"
//        props["mail.smtp.port"] = "587"
//
//        val session = Session.getInstance(props,
//            object : Authenticator() {
//                override fun getPasswordAuthentication(): PasswordAuthentication {
//                    return PasswordAuthentication(username, password)
//                }
//            })
//        try {
//            val message: Message = MimeMessage(session)
//            message.setFrom(InternetAddress(username))
//            message.setRecipients(
//                Message.RecipientType.TO,InternetAddress.parse(_txtEmail)
//            )
//            message.subject = "Sending email without opening gmail apps"
//            message.setText(messageToSend)
//            Transport.send(message)
//            Toast.makeText(
//                applicationContext,
//                "email send successfully.",
//                Toast.LENGTH_LONG
//            ).show()
//        } catch (e: MessagingException) {
//            throw RuntimeException(e)
//        }
//
//
//    }

    fun sendTransactionProcess(){

        stan = stan?.plus(1)
        saleMsg = salePacket(stan.toString())
        Log.i("log_tag", "Current stan: " + stan)

        reversalMsg = reversalPacket(stan.toString())
        var reverseTrans = ReversalEntity(null,reversalMsg.toString())

        reverseFlag = true
        var flagReverse = FlagReverseEntity(null, reverseFlag)

        Log.i("log_tag", "send sale packet")
        sendPacket(saleMsg)
//        Log.i("log_tag", "send ${reBuildISOPacket(saleMsg.toString())}")
        Log.i("log_tag", "sale: " + saleMsg.toString())
        Log.i("log_tag", "reverseFlag:  " + reverseFlag)

        Thread{
            accessDatabase()
            reversalDAO?.insertReversal(reverseTrans)
            flagReverseDAO?.insertFlagReverse(flagReverse)
            reReversal = reversalDAO?.getReversal()?.isoMsg
            Log.i("log_tag", "reversel :$reReversal")
        }.start()
    }

    fun sendPacket(packet: ISOMessage?){
        Thread {
            try {
                var client = ISOClientBuilder.createSocket(HOST, PORT)
                    .configureBlocking(false)
                    .setReadTimeout(5000)
                    .build()
                client.connect()

                var response = bytesArrayToHexString(client.sendMessageSync(packet))
                EventBus.getDefault().post(MessageEvent(
                    "iso_response",
                    response.toString()))

                client.disconnect()

            } catch (err: ISOClientException) {
                Log.e("log_tag", "error1 is ${err.message}")
                if (err.message.equals("Read Timeout")) {

                    runOnUiThread {
//
                        if(stuckReverse == true){
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Timeout! have no response message")
                        }else{
//                            timeoutAlert()
                            setDialog("Transaction failed!!","Timeout! have no response message.This transaction must be cancelled.")
                        }

                        Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e("log_tag", "connection fail.")

                    runOnUiThread {
                        if(stuckReverse == true){
//                            reverselNonApproveConnectLoss()
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Connection failed! have no response message")
                        }else{
//                            connectionFailAlert()
                            setDialog("Transaction failed!!","Connection failed! Please check your network.This transaction must be cancelled.")
                        }

                        Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                    }
                }
            }
        }.start()
    }

    fun errorCode(code: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Transaction Error.")
        builder.setMessage("Error code: " + code +",  ${msg}")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        })
        val dialog = builder.create()
        dialog.show()
    }

    @Throws(ISOException::class, ISOClientException::class, IOException::class)
    fun salePacket(STAN: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()

    }

    fun reversalPacket(STAN: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reversal()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction,convertToFloat(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }

    private fun testNetwork(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("990000")
            .setField(FIELDS.F24_NII_FunctionCode,"120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }


    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun mtiUnpack(isoMsg: String): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()
        val mti: String? = isoMessageUnpacket.getMti()
        return mti
    }


    fun setDialogNormal(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            })
        val dialog = builder.create()
        dialog.show()
    }

    fun setDialog(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun setDialogApprove(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
//                sendEmailProcess()
                val itn =Intent(this,MenuActivity::class.java).apply{
                    putExtra("totalAmount",totalAmount)
                    putExtra("menuName",menuName)
//                    putExtra("from","transAct")
                }
                startActivity(itn)
            })
        val dialog = builder.create()
        dialog.show()
    }

    fun setDialogS(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()

                Thread{
                    accessDatabase()
                    readStan = saleDAO?.getSale()?.STAN
                    readFlagReverse = flagReverseDAO?.getFlagReverse()?.flagReverse
                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse
                    reReversal = reversalDAO?.getReversal()?.isoMsg
                    Log.i("log_tag","readSTAN : " + readStan)
                    Log.i("log_tag","readFlagReverse : " + readFlagReverse)
                    Log.i("log_tag","readStuckReverse : " + readStuckReverse)
//                    Log.i("log_tag","reReversal : $reReversal ")
                }.start()
            })
        
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            }

        val dialog = builder.create()
        dialog.show()
    }

    //receipt
//    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    open fun screenshotTask() {
        if (hasStoragePermission()) {
            // Have permissions, do the thing!
            saveScreenshot()
            Toast.makeText(this, "save receipt.", Toast.LENGTH_LONG).show()
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to can write storage.",
                RC_WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        Log.w("log", "requestCode: $requestCode")
        Log.w("log", "permissions: $permissions")
        Log.w("log", "grantResults: $grantResults")
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun saveScreenshot() {
        // Save the screenshot
        try {
            val file: File = ScreenShott.getInstance()
                .saveScreenshotToPicturesFolder(this, bitmap, "receipt")
            // Display a toast
//            Toast.makeText(
//                this, "Bitmap Saved at " + file.absolutePath,
//                Toast.LENGTH_SHORT
//            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray? {
        val b = ByteArray(s.length / 2)
        for (i in b.indices) {
            val index = i * 2
            val v = s.substring(index, index + 2).toInt(16)
            b[i] = v.toByte()
        }
        return b
    }

    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
    }

    fun convertToFloat(Totalamount : Double ):String{

        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]

        return Amount
    }

}