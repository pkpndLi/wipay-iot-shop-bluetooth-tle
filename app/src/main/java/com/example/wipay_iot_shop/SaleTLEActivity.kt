package com.example.wipay_iot_shop

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.*
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.cypto.DataConverter
import com.example.wipay_iot_shop.cypto.iDES
import com.example.wipay_iot_shop.cypto.iRSA
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
import com.imohsenb.ISO8583.utils.StringUtil
import github.nisrulz.screenshott.ScreenShott
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and

class SaleTLEActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var reversalDAO : ReversalDao? = null
    var saleDAO : SaleDao? = null
    var flagReverseDAO : FlagReverseDao? = null
    var stuckReverseDAO : StuckReverseDao? = null
    var responseDAO : ResponseDao? = null

    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var log = "log"

    var processing = false
    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""
    var menuName:String = ""

    var output1: TextView? = null
    var output2: TextView? = null
    var stan: Int? = null
    var initialStan: Int? = 4
    var reverseFlag :Boolean? = null
    var reversal: String? = null
    var responseCode: String? = null
    var responseMsg: String = ""
    var reReversal: String? = null
    var reversalMsg: ISOMessage? = null
    var saleMsg: ISOMessage? = null
    var saleMsgOri: String = ""
    var responseMsgOri: String = ""
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

    //TLE config params
    val rsa = iRSA()
    val des = iDES()
    val dataConverter = DataConverter()

    var indicator = "HTLE"
    var version = "04"
    var reqType = "1"
    var acqID = "120"
    var LTID = "00000000"
    var vendorID = "12000002"
    var TE_ID = "12002002"
    var makKey = "3991E2A306727C99B85BB694E4AD7F54"
    var dekKey = "139EB7CE451189AA8E613C0BA77D9045"
    var ltwkId = "9227"
    var tid = "22222222"
    var mid = "222222222222222"
    var cipherText = "03E9D376D0C0D20C39540D3E2C1C5791"
    var encryptMethod = "2000"
    var encryptCounter = "0048"
    var reserved = "00000000"
    var _bit64:ByteArray = ByteArray(8)
    var _bit57 = ""
    var saleTlePacket = ""
    var reverseTlePacket = ""

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    //    private val HOST = "192.168.43.195"
//    var PORT = 5000
//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//      private val HOST = "192.168.178.187"
//    var PORT = 5000
//    private val HOST = "192.168.43.24"
//    var PORT = 3000
    //      private val HOST = "192.168.68.107"
//    var PORT = 3000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001

    //    private val HOST = "203.148.160.47"
//    var PORT = 7500

    //Tle host
    private val HOST = "223.27.234.243"
    var PORT = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_tleactivity)

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)

        main = findViewById(R.id.SaleTLEActivity)
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
            makKey = getStringExtra("MAK").toString()
            dekKey = getStringExtra("DEK").toString()
            ltwkId = getStringExtra("keyIdLtwk").toString()
        }

         LTID = serialNumber()
//         cardNO = "4162026250958064"
//         cardEXD = "2512"
//         totalAmount = 200
//         makKey = "3991E2A306727C99B85BB694E4AD7F54"
//         dekKey = "139EB7CE451189AA8E613C0BA77D9045"
//         ltwkId = "9227"

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

            makKey = "3991E2A306727C99B85BB694E4AD7F54"
            dekKey = "139EB7CE451189AA8E613C0BA77D9045"
            ltwkId = "9227"
//            stan = 4
            Log.d(log,"...TEST TLE FLOW...")
            Log.e(log,"dekKey: " + dekKey)
            Log.e(log,"makKey: " + makKey)
            Log.e(log,"ltwkKeyId: " + ltwkId)
            Log.e(log,"cardNO: " + cardNO)
            Log.e(log,"cardEXD: " + cardEXD)
            Log.e(log,"totalAmount: " + totalAmount)

            manageProcessing()

            //...TEST TLE FLOW...

//            cardNO = "4162026250958064"
//            cardEXD = "2512"
//            totalAmount = 200

//            saleTlePacket = saleTlePacket(salePacketWithMac().toString()).toString()
//            Log.e(log,"saleTlePacket: " + saleTlePacket)
//            var resMsg = "60800101270210203801000E8000850040000000040934311202012054455354303930303030303434303134303130303232323232323232004948544C45303431323035353238613130383230303039323237303034380000000800000000000000001AF68872FEAC3BD20006303030343841AA51690900000000"
//            Log.w(log,"checkMac: " + checkMacResponse(resMsg))
//            Log.w(log,"responseMsgOri: " + responseMsgOri)
//            var newSaleMsg = ArrayList<String>()
//            unpackIso(responseMsgOri,newSaleMsg)
//            reverseTlePacket = tlePacket(reversePacketWithMac().toString()).toString()
//            Log.e(log,"reverseTlePacket: " + reverseTlePacket)

            //...TEST TLE FLOW...

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
        responseMsg = event.message
        Log.i("log_tag", "response code:"+ responseCode)
        //
        if(responseCode == "3030"){
            //ไม่ว่าจะเข้าเงื่อนไข if หรือ else ข้างล่างก็เป็นการปลด reverse เหมือนกัน
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
                //รายการที่ทำ reverse สำเร็จต้อง save stan เอาไว้ เพื่อให้ stan ต่อเนื่อง
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

                if(checkMacResponse(responseMsg) == true){

                    Log.i(log, "Transaction Approve.")
//                transactionApprove()
                    setDialogApprove(null,"Transaction complete.")
                    var saleApprove = SaleEntity(null,saleMsgOri,stan)
                    var responseSaleApprove = ResponseEntity(null,responseMsgOri)


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


                }else{

                     setDialogApprove(null,"Check Mac Failed!!!")

                     }

            }

        }else{

            if(stuckReverse == true){ //ยังติด reverse อยู่

                errorCode(responseCode,null)

            } else{ //manage Transaction Error

                reverseFlag = false  //กรณีมี response แต่ transaction error
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
//        saleMsg = salePacket(stan.toString())
        saleMsgOri = salePacketWithMac().toString()
        saleMsg = saleTlePacket(saleMsgOri)
        Log.e(log,"saleMsgOri: " + saleMsgOri)
        Log.i(log, "Current stan: " + stan)

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
                EventBus.getDefault().post(
                    MessageEvent(
                    "iso_response",
                    response.toString())
                )

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

    //...tleMsgFunc...

    fun saleTlePacket(isoMsg: String):ISOMessage{

        Log.d(log,"...build tleSalePacket...")
        Log.e(log,"original packet: " + isoMsg)
        _bit64 = bit64Mac(isoMsg,makKey)
        Log.e(log,"_bit64: " + _bit64)
        var tlvMsg = buildTLVMsg(cardNO,cardEXD)
        Log.e(log,"test buildTLVMsg func: " + tlvMsg)
        var tlvLen = (tlvMsg.length/2).toString()
        Log.e(log,"tlvLen: "+ tlvLen)
        cipherText = eTLV(tlvMsg,dekKey)
        Log.e(log,"cipherText: "+ cipherText)
         _bit57 = bit57Ver4(indicator,version,acqID,LTID,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved,cipherText)
        Log.e(log,"_bit57: " + _bit57)
        var tlePacket = salePacketTle(hexStringToByteArray(_bit57)!!,_bit64)
        Log.e(log,"saleTleMsg: " + tlePacket)

        return tlePacket
    }

    fun salePacketWithMac(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,StringUtil.hexStringToByteArray(convertStringToHex(tid, false)))
            .setField(FIELDS.F42_CA_ID,StringUtil.hexStringToByteArray(convertStringToHex(mid, false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()

    }

    fun reversePacketWithMac(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reversal()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,StringUtil.hexStringToByteArray(convertStringToHex(tid, false)))
            .setField(FIELDS.F42_CA_ID,StringUtil.hexStringToByteArray(convertStringToHex(mid, false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()

    }

    fun bit64Mac(isoMsg: String,key: String): ByteArray{

        var preMacMsg = isoMsg.substring(10)
        var data = hexStringToByteArray(preMacMsg)
        Log.e(log,"preMacMsg: " + preMacMsg)
        var arraySize =  if(data?.size?.mod(8) != 0)
            ((data?.size?.div(8))?.plus(1))?.times(8) else
            ((data?.size?.div(8))?.plus(1))?.times(1)

        var _data: ByteArray = ByteArray(arraySize!!)
        System.arraycopy(data,0,_data,0, data?.size!!)
//        Log.e(log,"data size: " + data.size)
//        Log.e(log,"_data size: " + _data.size)
//        var macData = print(_data)
        var macData = bytesArrayToHexString(_data).toString()
        Log.e(log,"mac pre encrypt: " + macData)
        var eMacData = des.enDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(macData))
        var macRawEncrypted = dataConverter.HexByteToHexString(eMacData)
        Log.e(log,"macRawEncrypted(hex): " + macRawEncrypted)
        var _eData = hexStringToByteArray(macRawEncrypted)
        Log.e(log,"macRawEncrypted: " + _eData)
        var _mac: ByteArray = ByteArray(8)
        System.arraycopy(_eData, _eData?.size!! -8,_mac,0, 4)
        Log.e(log,"_MAC: " + bytesArrayToHexString(_mac).toString())
//        _bit64 = _mac
//        var mac = (bytesArrayToHexString(_mac).toString()).uppercase(Locale.getDefault()) ?: String()
//        Log.e(log,"mac is: " + bytesArrayToHexString(_mac).toString())
        return _mac
    }

    fun buildTLVMsg(cardNO: String,cardEXD:String): String{
        var bit2 = 2
        var bit14 = 14
        var tag2Data = decToOct(2).toString()
        var tag2 = ("00" + tag2Data).substring(tag2Data.length)
        var tag14Data = decToOct(14).toString()
        var tag14 = ("00" + tag14Data).substring(tag14Data.length)
        var bit2Data  = cardNO.length.toString() + cardNO
        var bit14Data  = cardEXD
        var tag2Lenght = (bit2Data!!.length/2).toString()
        var tag14Lenght = (bit14Data!!.length/2).toString()
        var bit2Len = ("00" + tag2Lenght).substring(tag2Lenght.length)
        var bit14Len = ("00" + tag14Lenght).substring(tag14Lenght.length)

//        Log.e(log,"bit2Data: " + bit2Data)
//        Log.e(log,"tag2Len: " + bit2Len)
//        Log.e(log,"tag14Len: " + bit14Len)

        var tlvMsg = tag2 + bit2Len + bit2Data + tag14 + bit14Len + bit14Data
        Log.e(log,"tlvMsg: " + tlvMsg)
//        var data = hexStringToByteArray("303230393136343136323032363235303935383036343136303232353132")
//        var arraySize =  if(data?.size?.mod(8) != 0)
//            ((data?.size?.div(8))?.plus(1))?.times(8) else
//            ((data?.size?.div(8))?.plus(1))?.times(1)
//
//        var _data: ByteArray = ByteArray(arraySize!!)
//        System.arraycopy(data, 0,_data,0, data?.size!!)

        return tlvMsg
    }

    fun eTLV(tlvMsg: String,key: String): String{

        var eTLVMsg = des.enDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(tlvMsg + "00"))
        var eTLV = dataConverter.HexByteToHexString(eTLVMsg)
//        Log.e(log,"eTLV: " + eTLV)

        return eTLV
    }

    fun bit57Ver4(indicator:String,version:String,acqID:String,tid:String,encryptMethod:String,ltwkId:String,encryptCount:String,TLVLen:String,reserved:String,cipherText:String):String{

        var tlvLen = ("0000" + TLVLen).substring(TLVLen.length)
//        var tlvLen = "0015"
//        var data = indicator + version + acqID + tid + encryptMethod  + ltwkId + encryptCount + "00" + tlvLen + reserved
        var data = indicator + version + acqID + tid + encryptMethod  + ltwkId + encryptCount + "00"
        Log.e(log,"bit57Data: " + data)
        var hexData = convertStringToHex(data,false)
        var tlvLenHex = convertStringToHex(tlvLen,false)
        var reserved = convertStringToHex(reserved,false)
//        var bit57Msg = hexData + hexStringToByteArray(tlvLenHex) + reserved + cipherText

        var _bit57 = hexData + tlvLen + reserved + cipherText
        return _bit57
    }

    fun salePacketTle(bit57:ByteArray,bit64Mac:ByteArray): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,StringUtil.hexStringToByteArray(convertStringToHex(tid, false)))
            .setField(FIELDS.F42_CA_ID,StringUtil.hexStringToByteArray(convertStringToHex(mid, false)))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,bit64Mac)
            .setHeader("6001278001")
            .build()

    }

    fun serialNumber():String {
//        var androidId: String = Settings.Secure.getString(
//            contentResolver,
//            Settings.Secure.ANDROID_ID
//        )
        var androidId = "24215d325528a108"
        var sn = androidId.substring(androidId.length - 8)
        return sn
    }

    //...build TLE message function...

    //...Check Response func...

    fun checkMacResponse(responseMsg:String):Boolean{

        var readBit64 = codeUnpack(responseMsg,64)
        var readBit64Substring  = readBit64?.substring(0,16)
        var bit57Msg = codeUnpack(responseMsg,57).toString().uppercase(Locale.getDefault())
        Log.w(log,"dBit57Msg: " + bit57Msg)
        var tlvResponseData = dCipherText(bit57Msg,dekKey)
        Log.w(log,"tlvResponseData: " + tlvResponseData)
        var buildBit64 = macResponse(tlvResponseData,responseMsg,makKey)
        Log.w(log,"readBit64Substring: " + readBit64Substring)
        Log.w(log,"buildBit64: " + buildBit64)
        var checkMac = false

        if(readBit64Substring == buildBit64){
            checkMac = true
        }else{
            checkMac = false
        }

        return checkMac
    }

    fun dCipherText(bit57Msg: String,key: String):String{

        var cipherMsg = bit57Msg.substring(bit57Msg.length - 16)
        Log.w(log,"cipherMsg: " + cipherMsg)
        //add Decryption TDES func.
        var dCipherMsg = des.deDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(cipherMsg))
        var dCipherData = dataConverter.HexByteToHexString(dCipherMsg)
//        Log.w(log,"dCipherMsg: " + dCipherData)

        return dCipherData
    }

    fun macResponse(dCipherText: String,responseMsg: String,key: String): String{

        var checkBit = dCipherText.subSequence(0,2)
        var checkLen = dCipherText.substring(2,4)
//        Log.w(log,"decryptMsg: " + dCipherText)
        Log.w(log,"checkBit: " + checkBit)
        Log.w(log,"checkLen: " + checkLen)

        var addField = ArrayList<String>()
        addField = ArrayList<String>()
        var field = unpackIso(responseMsg,addField)

        for (i in 0..field.size-1) {

            Log.w(log,"field[$i]: " + field[i])
        }

//                if(checkBit == "04" && checkLen == "06"){

        var amount = dCipherText.substring(4)
        var totalAmount:Int = 200
        fun responsePacket(): ISOMessage {
            return ISOMessageBuilder.Packer(VERSION.V1987)
                .financial()
                .setLeftPadding(0x00.toByte())
                .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
                .processCode(field[0])
//                            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
                .setField(FIELDS.F4_AmountTransaction, amount)
                .setField(FIELDS.F11_STAN, field[1])
                .setField(FIELDS.F12_LocalTime,field[2])
                .setField(FIELDS.F13_LocalDate,field[3])
                .setField(FIELDS.F24_NII_FunctionCode, field[4])
                .setField(FIELDS.F37_RRN, hexStringToByteArray(field[5]))
                .setField(FIELDS.F38_AuthIdResponse, hexStringToByteArray(field[6]))
                .setField(FIELDS.F39_ResponseCode, hexStringToByteArray(field[7]))
                .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(field[8]))
                //                    .setField(FIELDS.F57_Reserved_National,field[9])
                .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(field[10]))
                .setField(FIELDS.F64_MAC,"")
                .setHeader("6001278001")
                .build()
        }

        Log.w(log,"rebuildResponse: " + responsePacket().toString())
        var isoMsg = responsePacket().toString()
        var preMacMsg = "0210" + isoMsg.substring(14)
        responseMsgOri = "6001278001" + preMacMsg
        var data = hexStringToByteArray(preMacMsg)
        Log.e(log,"preMacMsg: " + preMacMsg)
        var arraySize =  if(data?.size?.mod(8) != 0)
            ((data?.size?.div(8))?.plus(1))?.times(8) else
            ((data?.size?.div(8))?.plus(1))?.times(1)

        var _data: ByteArray = ByteArray(arraySize!!)
        System.arraycopy(data,0,_data,0, data?.size!!)
        //        Log.e(log,"data size: " + data.size)
        //        Log.e(log,"_data size: " + _data.size)
        //        var macData = print(_data)
        var macData = bytesArrayToHexString(_data).toString()
        Log.w(log,"response for encrypt: " + macData)

        var eMacData = des.enDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(macData))
        var macRawEncrypted = dataConverter.HexByteToHexString(eMacData)
//                    var macRawEncrypted = "3D9ECCD42C639BF3EE67A33CC8FCBF857919A2E9F23ECD60328C6D3B27F63AC6E8AC1DB851A067DE4C10A39568BD198ECDD5B18983805DEBAF780BBB9B8724CAAA51690921A27F95"
        var _eData = hexStringToByteArray(macRawEncrypted)
        Log.w(log,"macRawEncrypted: " + _eData!!.size)
        var _mac: ByteArray = ByteArray(8)
        System.arraycopy(_eData, _eData?.size!! -8,_mac,0, 4)
        var _bit64Response = bytesArrayToHexString(_mac).toString()
//                    Log.w(log,"macRespose encrepted: " + _bit64Response)
//                }
        return _bit64Response
    }

    fun unpackIso(isoMsg: String,field:ArrayList<String>):ArrayList<String>{
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()

        Log.e(log,"HEADER : " + bytesArrayToHexString(isoMessageUnpacket.header))
        Log.e(log,"MTI : " + isoMessageUnpacket.mti)
        for (i in 3..64) {
            if (isoMessageUnpacket.fieldExits(i)) {
                Log.e(log,"FIELD[" + i + "] : " + bytesArrayToHexString(isoMessageUnpacket.getField(i)))
                field.add(bytesArrayToHexString(isoMessageUnpacket.getField(i)).toString())
            }
        }

        return field
    }


    //...Check Response func...

    fun errorCode(code: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Transaction Error.")
        builder.setMessage("Error code: " + code +",  ${msg}")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        })
        val dialog = builder.create()
        dialog.show()
    }

//    @Throws(ISOException::class, ISOClientException::class, IOException::class)
//    fun salePacket(STAN: String): ISOMessage? {
//        return ISOMessageBuilder.Packer(VERSION.V1987)
//            .financial()
//            .setLeftPadding(0x00.toByte())
//            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
//            .processCode("000000")
//            .setField(FIELDS.F2_PAN, cardNO)
//            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount!!.toDouble()))
//            .setField(FIELDS.F11_STAN, STAN)
//            .setField(FIELDS.F14_ExpirationDate, cardEXD)
//            .setField(FIELDS.F22_EntryMode, "0010")
//            .setField(FIELDS.F24_NII_FunctionCode, "120")
//            .setField(FIELDS.F25_POS_ConditionCode, "00")
//            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
//            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
//            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
//            .setHeader("6001208000")
//            .build()
//
//    }

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
                val itn = Intent(this,MenuActivity::class.java).apply{
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
            // Ask for both permissionsz
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

    fun decToOct(decimal: Int): Int {
        var decimal = decimal
        var octalNumber = 0
        var i = 1

        while (decimal != 0) {
            octalNumber += decimal % 8 * i
            decimal /= 8
            i *= 10
        }

        return octalNumber
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

    fun convertStringToHex(str: String, lowercase: Boolean): String? {
        val HEX_ARRAY: CharArray = if (lowercase) HEX_LOWER else HEX_UPPER
        val bytes = str.toByteArray(StandardCharsets.UTF_8)

        // two chars form the hex value.
        val hex = CharArray(bytes.size * 2)
        for (j in bytes.indices) {

            // 1 byte = 8 bits,
            // upper 4 bits is the first half of hex
            // lower 4 bits is the second half of hex
            // combine both and we will get the hex value, 0A, 0B, 0C
            val v = (bytes[j] and 0xFF.toByte()).toInt() // byte widened to int, need mask 0xff
            // prevent sign extension for negative number
            hex[j * 2] = HEX_ARRAY[v ushr 4] // get upper 4 bits
            hex[j * 2 + 1] = HEX_ARRAY[v and 0x0F] // get lower 4 bits
        }
        return String(hex)
    }
}