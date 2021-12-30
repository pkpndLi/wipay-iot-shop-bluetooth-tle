package com.example.wipay_iot_shop

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.testpos.database.transaction.SaleEntity
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.cypto.DataConverter
import com.example.wipay_iot_shop.cypto.iDES
import com.example.wipay_iot_shop.cypto.iRSA
import com.example.wipay_iot_shop.transaction.ResponseDao
import com.example.wipay_iot_shop.transaction.ResponseEntity
import com.example.wipay_iot_shop.transaction.TransactionDao
import com.example.wipay_iot_shop.transaction.TransactionEntity
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
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import kotlin.experimental.and

class SettlementTLEActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null
    var transactionDAO : TransactionDao? = null

    // Get SharedPreferences
    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var saleCountTxt: TextView? = null
    var saleAmountTxt: TextView? = null

    var stringValue = ""
    var booleanValue : Boolean? = null
    var log = "log"

    var readStan: Int? = null
    var stan: Int? = null
    var readId: Int? = null
    var isoMsgList = ArrayList<String>()
    var newIsoMsgList = ArrayList<String>()
    var isoMsg: String? = null
    var readIsoMsg: String? = null
    var readResponseMsg:String? = null
    var responseMsg:String? = null

    var saleCount: Int? = 0
    var saleAmount: Int? = 0
    var responseCount: Int? = 0
    var responseAmount: Int? = 0
    var batchTotals: String? = null


    var settlementFirstPccCode: String = "920000"
    var settlementLastPccCode: String = "960000"
    var amount:String = ""
    var cardNO:String = ""
    var cardEXD:String = ""
    var MTI:String = ""
    var oldStan:String = ""
    var TID: String = "3232323232323232"
    var MID: String = "323232323232323232323232323232"
    var batchNumber: String = "000142"

    var responseCode: String? = null

    var batchResponseFlag: Int? = 1
    var batchUploadLoopFlag: Boolean? = null

    private var saleReport: View? = null
    private var bitmap: Bitmap? = null
    private val RC_WRITE_EXTERNAL_STORAGE = 123



    //    private val HOST = "192.168.43.195"
//    var PORT = 5000
//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//    var PORT = 5000
//    private val HOST = "192.168.43.24"
//    var PORT = 3000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001
//    private val HOST = "203.148.160.47"
//    var PORT = 7500

//    private val HOST = "192.168.178.187"
//    var PORT = 5000

//    private val HOST = "192.168.68.107"
//    var PORT = 3000
//Tle host
    private val HOST = "223.27.234.243"
    var PORT = 5000

    var settlementFlag:Boolean? = null
    var firstTransactionFlag:Boolean? = null
    var oldStartId:Int? = null
    var startId:Int? = null
    var endId: Int? = null
    var lastSettlementFlag: Boolean? = null
    var batchStan: Int? = null
    var settlementPacketOri = ""
    var lastSettlementPacket: ISOMessage? = null


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

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement_tleactivity)

        Log.d(log,"on settlementActivity.")
//        LTID = serialNumber()
        LTID = "5528a108"
        saleReport = findViewById(R.id.SaleReportActivity)

        var confirmBtn = findViewById<Button>(R.id.confirmBtn)
        saleCountTxt = findViewById<TextView>(R.id.saleCountTxt)
        saleAmountTxt = findViewById<TextView>(R.id.sumAmountTxt)

        var batchBtn = findViewById<Button>(R.id.batchBtn)

        intent.apply {
            lastSettlementFlag = getBooleanExtra("lastSettlementFlag",false)
            batchStan = getIntExtra("batchStan",1)
//
        }

        Log.i(log,"lastSettlementFlag: " + lastSettlementFlag)

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
        startId = sp.getInt("startId",1)
        oldStartId = sp.getInt("oldStartId",0)
        makKey = sp.getString("MAK",null).toString()
        dekKey = sp.getString("DEK",null).toString()
        ltwkId = sp.getString("LTWK_ID",null).toString()
        Log.w(log,"oldStartId: " + oldStartId)
        Log.w(log,"startId: " + startId)

        //for test
        makKey = "3991E2A306727C99B85BB694E4AD7F54"
        dekKey = "139EB7CE451189AA8E613C0BA77D9045"
        ltwkId = "9227"

        if(oldStartId == startId){
            setDialog("Processing failed.","There has never been any transaction.")//
        }else if(lastSettlementFlag == true){

            saleCountTxt?.setText(sp.getString("saleCount","saleCount"))
            saleAmountTxt?.setText(sp.getString("saleAmount","saleAmount"))

            Log.w(log,"endId" + endId)
            Log.i(log,"In LastSettlement path")

            stan = batchStan?.plus(1)
            batchTotals = sp.getString("batchTotals","11111111111")
            Log.e(log,"stan: "+ stan + "," + "batchTotals: " + batchTotals)
//            Log.e(log,"send lastSettlement Packet: " + lastSettlementPacket())
            lastSettlementPacket = lastSettlementTlePacket(lastSettlementPacketWithMac().toString())
            Log.e(log,"send lastSettlement Packet: " + lastSettlementPacket)
            setDialogNormal("","please confirm transaction again.")

        }else{
            setDialogQueryTransaction("","Wait a moment, the system is processing...")
        }

        confirmBtn.setOnClickListener{
            //set settlementFlag = 1

            if(lastSettlementFlag == true){

                sendPacket(lastSettlementPacket)

                val editor: SharedPreferences.Editor = sp.edit()
                editor.putBoolean("lastSettlementFlag", false)
                editor.commit()

            }else{

                stan = readStan?.plus(1)
                batchTotals = buildBatchTotals(saleCount!!, subStringCutZero(saleAmount!!).toDouble())

                val editor: SharedPreferences.Editor = sp.edit()
                editor.putBoolean("settlementFlag", true)
                editor.putString("batchTotals",batchTotals)
                editor.commit()

                //test settlementPacket
                settlementFlag =  sp.getBoolean("settlementFlag",false)
                Log.w(log,"settlementFlag: " + settlementFlag)
                Log.e(log,"stan: "+ stan + "\n" + "batchTotals: " + batchTotals)
                var settlementPacket: ISOMessage = settlementTlePacket(settlementPacketWithMac().toString())
                Log.e(log,"Settlement Packet: " + settlementPacket)

                sendPacket(settlementPacket)
            }
        }

        batchBtn.setOnClickListener {


        }

//
//        makKey = "3991E2A306727C99B85BB694E4AD7F54"
//        dekKey = "139EB7CE451189AA8E613C0BA77D9045"
//        ltwkId = "9227"
//        batchTotals = "303032303030303030323131323235303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030"
//        stan = 1
//        Log.e(log,"ltid: " + LTID)
//        Log.e(log,"dekKey: " + dekKey)
//        Log.e(log,"makKey: " + makKey)
//        Log.e(log,"ltwkKeyId: " + ltwkId)
//        Log.e(log,"test settlemetTleMsg: " + settlementTlePacket(settlementPacketWithMac().toString()))
    }

    //...tleFunc...

    fun settlementTlePacket(isoMsg: String):ISOMessage{

        Log.d(log,"...build tlePacket...")
        Log.e(log,"original packet: " + isoMsg)
        _bit64 = bit64Mac(isoMsg,makKey)
        Log.e(log,"_bit64: " + _bit64)
        var tlvLen = "0000"
        _bit57 = bit57Ver4(indicator,version,acqID,LTID,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved)
        Log.e(log,"_bit57: " + _bit57)
        var tlePacket = settlementPacketTle(hexStringToByteArray(_bit57)!!,_bit64)
        Log.e(log,"settlementTleMsg: " + tlePacket)

        return tlePacket!!
    }

    fun lastSettlementTlePacket(isoMsg: String):ISOMessage{

        Log.d(log,"...build tlePacket...")
        Log.e(log,"original packet: " + isoMsg)
        _bit64 = bit64Mac(isoMsg,makKey)
        Log.e(log,"_bit64: " + _bit64)
        var tlvLen = "0000"
        _bit57 = bit57Ver4(indicator,version,acqID,LTID,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved)
        Log.e(log,"_bit57: " + _bit57)
        var tlePacket = lastSettlementPacketTle(hexStringToByteArray(_bit57)!!,_bit64)
        Log.e(log,"settlementTleMsg: " + tlePacket)

        return tlePacket!!
    }


    fun settlementPacketWithMac(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("920000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()
    }

    fun lastSettlementPacketWithMac(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("960000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F60_Reserved_National, batchNumber)
            .setField(FIELDS.F62_Reserved_Private, hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private, hexStringToByteArray(batchTotals.toString()))
            .setField(FIELDS.F64_MAC, "")
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

    fun bit57Ver4(indicator:String,version:String,acqID:String,tid:String,encryptMethod:String,ltwkId:String,encryptCount:String,TLVLen:String,reserved:String):String{

        var tlvLen = ("0000" + TLVLen).substring(TLVLen.length)
//        var tlvLen = "0015"
//        var data = indicator + version + acqID + tid + encryptMethod  + ltwkId + encryptCount + "00" + tlvLen + reserved
        var data = indicator + version + acqID + tid + encryptMethod  + ltwkId + encryptCount + "00"
        Log.e(log,"bit57Data: " + data)
        var hexData = convertStringToHex(data,false)
        var tlvLenHex = convertStringToHex(tlvLen,false)
        var reserved = convertStringToHex(reserved,false)
//        var bit57Msg = hexData + hexStringToByteArray(tlvLenHex) + reserved + cipherText

        var _bit57 = hexData + tlvLen + reserved
        return _bit57
    }

    fun settlementPacketTle(bit57:ByteArray,bit64Mac:ByteArray): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("920000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setField(FIELDS.F64_MAC,bit64Mac)
            .setHeader("6001278001")
            .build()
    }

    fun lastSettlementPacketTle(bit57:ByteArray,bit64Mac:ByteArray): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("960000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setField(FIELDS.F64_MAC,bit64Mac)
            .setHeader("6001278001")
            .build()
    }

    fun serialNumber():String {
        var androidId: String = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        var androidId = "24215d325528a108"
        var sn = androidId.substring(androidId.length - 8)
        return sn
    }

    //...tleFunc...

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)



    }

    override fun onResume() {
        super.onResume()


    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    val SEND_MESSAGE = "send_message"
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        if (event.type == "iso_response") {
            manageResponse(event)
        }

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
                Log.e(log, "error1 is ${err.message}")
                if (err.message.equals("Read Timeout")) {

                    runOnUiThread {
                        setDialog("Transaction failed!.","Response Timeout.")
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                    runOnUiThread {
                        setDialog("Transaction failed!.","Unable to connect to bank.")
                    }
                }
            }
        }.start()
    }

    fun manageResponse(event: MessageEvent){

        Log.i("log_tag", "Response Message:" + event.message)
        responseMsg = event.message
        responseCode = codeUnpack(event.message,39)
        Log.e("log_tag", "response code:"+ responseCode)

        if(responseCode == "3030"){

            manageSettlementApprove()

        }else{

//            var settlementError  = SaleEntity(null,null,stan)
            var settlementError  = TransactionEntity(null,null,null,stan)
            var responseSettlementError = ResponseEntity(null,null)

            Thread{

                accessDatabase()
//                saleDAO?.insertSale(settlementError)
                transactionDAO?.insertTransaction(settlementError)
                responseDAO?.insertResponseMsg(responseSettlementError)
                readStan = transactionDAO?.getTransaction()?.STAN
                readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
                Log.w(log,"saveSTAN : " + readStan)
                Log.w(log,"saveResponse : " + readResponseMsg)

            }.start()


            if(responseCode == "3935"){

                Log.i(log,"go to batch upload transaction.")

                val itn = Intent(this,BatchUploadActivity::class.java).apply{
                    putExtra("startId",startId)
                    putExtra("endId",endId)
                }
                startActivity(itn)

            } else{

                errorCode(responseCode,"Please check your problem.")
                Log.e(log,"Settlement Error!!!.")
            }

        }

    }


    fun manageSettlementApprove(){

        Log.i(log, "Settlement Approve.")

        val editor: SharedPreferences.Editor = sp.edit()
        editor.putBoolean("settlementFlag", false)
        editor.putBoolean("firstTransactionFlag", true)
        editor.putInt("oldStartId", startId!!)
        editor.commit()

        setDialog(null,"Settlement complete.")

        settlementFlag =  sp.getBoolean("settlementFlag",false)
        firstTransactionFlag = sp.getBoolean("firstTransactionFlag",false)
        Log.w(log,"settlementFlag: " + settlementFlag)
        Log.w(log,"firstTransactionFlag: " + firstTransactionFlag)

//        var settlementApprove  = SaleEntity(null,null,stan)
        var settlementApprove  = TransactionEntity(null,null,null,stan)
        var responseSettlementApprove = ResponseEntity(null,null)

        Thread{

            accessDatabase()
//            saleDAO?.insertSale(settlementApprove)
            transactionDAO?.insertTransaction(settlementApprove)
            responseDAO?.insertResponseMsg(responseSettlementApprove)
//            readStan = saleDAO?.getSale()?.STAN
            readStan = transactionDAO?.getTransaction()?.STAN
            readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
            Log.w(log,"saveSTAN : " + readStan)
            Log.w(log,"saveResponse : " + readResponseMsg)

        }.start()

        //save  sale report in photo album
        bitmap = ScreenShott.getInstance().takeScreenShotOfView(saleReport)
        screenshotTask()
        Log.i(log,"save sale report already.")
    }


    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()
        transactionDAO = appDatabase?.transactionDao()

    }

    fun settlementPacket(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("920000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setHeader("6001208000")
            .build()
    }

    fun lastSettlementPacket(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("960000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setHeader("6001208000")
            .build()
    }


    fun setDialogQueryTransaction(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()

                Thread{
                    //query transaction from DB and sum saleCount and saleAmount
                    accessDatabase()
//                    readStan = saleDAO?.getSale()?.STAN
                    readStan = transactionDAO?.getTransaction()?.STAN
//                    readId = saleDAO?.getSale()?._id
                    readId = transactionDAO?.getTransaction()?._id
                    endId = readId!!

                    Log.w(log, "startId: " + startId)
                    Log.w(log, "endId: " + readId)
                    Log.w(log, "Read STAN: " + readStan)

                    for(i in startId?.rangeTo(endId!!)!!){
//                     for(i in 1..3){
//                        readIsoMsg = saleDAO?.getSaleWithID(i)?.isoMsg
                        readIsoMsg = transactionDAO?.getTransactionWithID(i)?.isoMsg
                        readResponseMsg = responseDAO?.getResponseMsgWithID(i)?.responseMsg
//                    isoMsgArray.add(readIsoMsg!!)
                        if(readIsoMsg != null){
                            saleCount = saleCount?.plus(1)
                            saleAmount = saleAmount?.plus(codeUnpack(readIsoMsg!!,4)!!.toInt())
                            isoMsgList.add(readIsoMsg!!)

                        }

                        if(readResponseMsg != null){
                            responseCount = responseCount?.plus(1)
//                            responseAmount = responseAmount?.plus(codeUnpack(readResponseMsg!!,4)!!.toInt())
//                            responseAmount = null
                        }

                        Log.e(log, "Read isoMsg: " + readIsoMsg)
                        Log.e(log, "Read responseMsg: " + readResponseMsg)

                    }

                    Log.e(log, "Sale Count: " + saleCount)
                    Log.e(log, "Response Count: " + responseCount)
                    Log.e(log, "Sale Amount: " + saleAmount)
                    Log.e(log, "Response Amount: " + responseAmount)

                    runOnUiThread {

                        saleCountTxt?.setText(saleCount.toString())
                        saleAmountTxt?.setText(subStringCutZero(saleAmount!!).toString())

                        val editor: SharedPreferences.Editor = sp.edit()
                        editor.putString("saleCount", saleCount.toString())
                        editor.putString("saleAmount", subStringCutZero(saleAmount!!).toString())
                        editor.commit()

                    }

//                    accessDatabase()
//                    readStan = saleDAO?.getSale()?.STAN
//                    readFlagReverse = flagReverseDAO?.getFlagReverse()?.flagReverse
//                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse
//                    reReversal = reversalDAO?.getReversal()?.isoMsg
//                    Log.i("log_tag","readSTAN : " + readStan)
//                    Log.i("log_tag","readFlagReverse : " + readFlagReverse)
//                    Log.i("log_tag","readStuckReverse : " + readStuckReverse)
////                    Log.i("log_tag","reReversal : $reReversal ")
                }.start()
            })

        DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

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

    fun setDialogNormal(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
//                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()
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
        Log.w(log, "requestCode: $requestCode")
        Log.w(log, "permissions: $permissions")
        Log.w(log, "grantResults: $grantResults")
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

    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
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

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun mtiUnpack(response: String): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val mti: String? = isoMessageUnpacket.getMti()
        return mti
    }



    fun buildBatchTotals(Salecount :Int,Saleamount :Double):String{
        var DE63 =""
        var salecount = Salecount.toString().padStart(3,'0')
        var saleamount = String.format("%.2f",Saleamount)
        var arr : Array<String>
        arr = saleamount.split('.').toTypedArray()
        saleamount = arr[0]+arr[1]
        saleamount = saleamount.padStart(12,'0')
        var data = salecount+saleamount
        data = data.padEnd(90,'0')
        var data_arr :CharArray = data.toCharArray()
        for (i:Int in 0..data.length-1){
            val c = data_arr[i]
            val ascii = c.code
            DE63 += String.format("%02X", ascii)
        }
        return DE63
    }

    fun totalamount(Totalamount : Double ):String{

        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]

        return Amount
    }

    fun subStringCutZero(amount : Int):Double{
        var a = amount.toString()
        a = a.substring(0,a.length-2)
        return a.toDouble()
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