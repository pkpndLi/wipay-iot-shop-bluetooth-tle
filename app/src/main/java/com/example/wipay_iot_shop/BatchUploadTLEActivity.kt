package com.example.wipay_iot_shop

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.imohsenb.ISO8583.utils.StringUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import kotlin.experimental.and


class BatchUploadTLEActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null
    var transactionDAO : TransactionDao? = null

    // Get SharedPreferences
    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var log = "log"

    var startId:Int? = null
    var endId: Int? = null
    var readIsoMsg: String? = null
    var readResponseMsg:String? = null
    var responseMsg:String? = null
    var readId: Int? = null

    var readStan: Int? = null
    var batchUploadList = ArrayList<String>()

    var amount:String = ""
    var cardNO:String = ""
    var cardEXD:String = ""
    var MTI:String = "0200"
    var time:String = ""
    var date:String = ""
    var responseCode: String = ""
    var saleStan:String = ""
    var batchStan:Int? = 0
    var pccCode: String = "000001"
    var lastPccCode: String = "000000"
    var TID: String = "3232323232323232"
    var MID: String = "323232323232323232323232323232"

    var lastSettlementFlag: Boolean? = null

    var responseCount: Int? = 0
    var batchCount: Int? = 0



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
    var batchUploadOri = ""

    //    private val HOST = "192.168.43.195"
//    var PORT = 5000

//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//    var PORT = 5000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001

//    private val HOST = "192.168.43.24"
//    var PORT = 3000
//    private val HOST = "203.148.160.47"
//    var PORT = 7500

//    private val HOST = "192.168.178.187"
//    var PORT = 5000

//    private val HOST = "192.168.68.107"
//    var PORT = 3000

//Tle host
    private val HOST = "223.27.234.243"
    var PORT = 5000


    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_upload_tleactivity)

        intent.apply {
            startId = getIntExtra("startId",1)
            endId = getIntExtra("endId",2)
        }

//        startId = 1
//        endId = 3

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
        makKey = sp.getString("MAK",null).toString()
        dekKey = sp.getString("DEK",null).toString()
        ltwkId = sp.getString("LTWK_ID",null).toString()
//        startId = sp.getInt("startId",1)
        makKey = "3991E2A306727C99B85BB694E4AD7F54"
        dekKey = "139EB7CE451189AA8E613C0BA77D9045"
        ltwkId = "9227"
//        LTID = serialNumber()
        LTID = "5528a108"
        setDialogQueryTransaction("","Wait a moment, the system is processing...")



//        saleStan ="1"
//        time = "135000"
//        date = "0803"
//        cardNO = "4162026250958064"
//        cardEXD = "2512"
//        amount = "200"
//        responseMsg = "3030"
//        batchStan = 1
//
//        var sale = "600120800002007024058000C000041644444444444444440000000000001000000011062409001001200032323232323232323232323232323232323232323232320006303030343839"
//        var saleResponse = "60800001200210303801000E80000400000000000010000000110610193806170120544553543130303031313036343931383932303032323232323232320006303030343839"
//        isoUnpackResponse(saleResponse)
//        isoUnpackSale(sale)
//
//        batchUploadOri = batchUploadWithMac(pccCode).toString()
//        var batchUploagTle = batchUploadTlePacket(batchUploadOri,pccCode)
//        Log.e(log,"batchUploadOri: " + batchUploadOri)
//        Log.e(log,"batchUploagTle: " + batchUploagTle.toString())
//        Log.e(log,"bit60: " + buildDE60_OriginalData(MTI,saleStan))
//        Log.e(log,"debug batchUpload: " + batchUpload(pccCode))
    }

    //...tleFunc...

    fun batchUploadTlePacket(isoMsg: String,pccCode:String):ISOMessage{

        Log.d(log,"...build tleBatchUploadPacket...")
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
        var tlePacket = batchUploadPacketTle(pccCode,hexStringToByteArray(_bit57)!!,_bit64)
        Log.e(log,"batchUploadTleMsg: " + tlePacket)

        return tlePacket!!
    }

    fun batchUploadWithMac(pccCode: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .fileAction()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Advice, MESSAGE_ORIGIN.Acquirer)
            .processCode(pccCode)
            .setField(FIELDS.F2_PAN,cardNO)
            .setField(FIELDS.F4_AmountTransaction,amount)
            .setField(FIELDS.F11_STAN, batchStan.toString())
            .setField(FIELDS.F12_LocalTime,time)
            .setField(FIELDS.F13_LocalDate,date)
            .setField(FIELDS.F14_ExpirationDate,cardEXD)
            .setField(FIELDS.F22_EntryMode,"0051")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode,"00")
            .setField(FIELDS.F37_RRN,"544553543133303031313234")
            .setField(FIELDS.F38_AuthIdResponse,"323432313339")
            .setField(FIELDS.F39_ResponseCode,responseCode)
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F60_Reserved_National,hexStringToByteArray(buildDE60_OriginalData(MTI,saleStan)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()
    }

    fun batchUploadPacketTle(pccCode: String,bit57:ByteArray,bit64Mac:ByteArray): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .fileAction()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Advice, MESSAGE_ORIGIN.Acquirer)
            .processCode(pccCode)
            .setField(FIELDS.F4_AmountTransaction,amount)
            .setField(FIELDS.F11_STAN, batchStan.toString())
            .setField(FIELDS.F12_LocalTime,time)
            .setField(FIELDS.F13_LocalDate,date)
            .setField(FIELDS.F22_EntryMode,"0051")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode,"00")
            .setField(FIELDS.F37_RRN,"544553543133303031313234")
            .setField(FIELDS.F38_AuthIdResponse,"323432313339")
            .setField(FIELDS.F39_ResponseCode,responseCode)
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid, false)!!))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)!!))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F60_Reserved_National,hexStringToByteArray(buildDE60_OriginalData(MTI,saleStan)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,bit64Mac)
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

    fun serialNumber():String {
        var androidId: String = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        var androidId = "24215d325528a108"
        var sn = androidId.substring(androidId.length - 8)
        return sn
    }

    fun unpackIso(isoMsg: String,field: ArrayList<String>): ArrayList<String> {
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

    //...tleFunc...

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onResume() {
        super.onResume()

//        if(trigger == true){
//            manageBatchUpload()
//            trigger = false
//        }


    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        if(event.type == "sendPacketTrigger"){

            manageBatchUpload()
        }

        if(event.type == "iso_response"){
            manageResponse(event)
        }

    }

    fun manageBatchUpload(){
        //implement batch upload
        Log.i(log,"In manageBatchUpload function.")

        batchCount = 0
        var response: String? = null

        if(batchCount!! < batchUploadList.size){

            sendBatchUploadPacket()

        }

    }

    fun manageResponse(event: MessageEvent){
        Log.i(log,"In manageResponse function.")
//        Log.w(log,"batchCount: " + batchCount)
        Log.w(log, "Response Message:" + event.message)
        responseMsg = event.message
        responseCode = codeUnpack(event.message,39).toString()
        Log.w(log, "response code:"+ responseCode)

        var stan = codeUnpack(responseMsg.toString(),11).toString()
//        var batchUpload  = SaleEntity(null,null,stan.toInt())
        var batchUpload  = TransactionEntity(null,null,null,stan.toInt())
        var responseBatch = ResponseEntity(null,null)

        Thread{

            accessDatabase()
//            saleDAO?.insertSale(batchUpload)
            transactionDAO?.insertTransaction(batchUpload)
            responseDAO?.insertResponseMsg(responseBatch)
//            readStan = saleDAO?.getSale()?.STAN
            readStan = transactionDAO?.getTransaction()?.STAN
            readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
            Log.w(log,"saveSTAN[${batchCount}] : " + readStan)
            Log.w(log,"saveResponse : " + readResponseMsg)

        }.start()

        if(responseCode == "3030"){

            batchCount = batchCount?.plus(1)

            if(batchCount!! < batchUploadList.size){

                sendBatchUploadPacket()

            } else{

                //implement lastSettlement in SettlementActivity
                Log.e(log,"BatchUpload Finish.Back to implement LastSettlement.")
                Log.e(log,"nowStan: " + batchStan)

                val itn = Intent(this,SettlementActivity::class.java).apply{
                    putExtra("lastSettlementFlag",true)
                    putExtra("batchStan",batchStan)
                }
                startActivity(itn)
            }

        }else{
            errorCode(responseCode,"Please check your problem.")
            Log.e(log,"BatchUpload Error!!!.")
        }
    }

    fun sendBatchUploadPacket(){

        Log.i(log,"In sendBatchUploadPacket function.")
        Log.e(log,"send batchUpload packet[${batchCount}]: " + batchUploadList[batchCount!!])
        sendPacket(reBuildISOPacket(batchUploadList[batchCount!!]))
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

    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

    fun buildBatchUploadPacket(){


    }

    fun setDialogQueryTransaction(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()

                Thread{
                    accessDatabase()
//                    readStan = saleDAO?.getSale()?.STAN
                    readStan = transactionDAO?.getTransaction()?.STAN
//                    readId = saleDAO?.getSale()?._id
                    readId = transactionDAO?.getTransaction()?._id

//        endId = readId
                    batchStan = readStan

                    if(readStan == null){
                        batchStan = 1
                    }

                    var setPccCode = pccCode
                    var batchUploadPacket: String = ""

                    Log.w(log, "Read STAN: " + readStan)
                    Log.w(log, "startId: " + startId)
                    Log.w(log, "endId: " + endId)

                    for(n in startId?.rangeTo(endId!!)!!){
//                    for(n in startId..endId!!){
                        readResponseMsg = responseDAO?.getResponseMsgWithID(n)?.responseMsg
//                        readIsoMsg = saleDAO?.getSaleWithID(n)?.isoMsg
                        readIsoMsg = transactionDAO?.getTransactionWithID(n)?.isoMsg

                        Log.w(log,"isoResponseMsg[${n}]: "+ readResponseMsg)
                        Log.w(log,"isoMsg[${n}]: "+ readIsoMsg)
                        if(readResponseMsg != null){

                            responseCount = responseCount?.plus(1)
                            batchStan = batchStan?.plus(1)           //set stan
                            isoUnpackResponse(readResponseMsg.toString())   //set bit 4,12,13,39,11
                            isoUnpackSale(readIsoMsg.toString())            //set bit 2,14

                            if(n == endId){
                                setPccCode = lastPccCode                    //set bit 3
                            }

//                            batchUploadPacket = batchUpload(setPccCode).toString()
                            batchUploadOri = batchUploadWithMac(setPccCode).toString()
                            batchUploadPacket = batchUploadTlePacket(batchUploadOri,pccCode).toString()

                            batchUploadList.add(batchUploadPacket)

                            Log.i(log, "batchStan: " + batchStan)
                            Log.i(log, "processingCode: " + setPccCode)
                            Log.e(log, "batchUpload packet[${n}]: " + batchUploadPacket)
                        }

                    }
                    Log.w(log, "Response Count: " + responseCount)
                    Log.w(log,"batchLen: " + batchUploadList.size)

                    EventBus.getDefault().post(
                        MessageEvent(
                        "sendPacketTrigger","true")
                    )

                    runOnUiThread {

                    }

                }.start()
            })

        DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        }

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


    fun batchUpload(pccCode: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .fileAction()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Advice, MESSAGE_ORIGIN.Acquirer)
            .processCode(pccCode)
            .setField(FIELDS.F2_PAN,cardNO)
            .setField(FIELDS.F4_AmountTransaction,amount)
            .setField(FIELDS.F11_STAN, batchStan.toString())
            .setField(FIELDS.F12_LocalTime,time)
            .setField(FIELDS.F13_LocalDate,date)
            .setField(FIELDS.F14_ExpirationDate,cardEXD)
            .setField(FIELDS.F22_EntryMode,"0051")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode,"00")
            .setField(FIELDS.F37_RRN,"544553543133303031313234")
            .setField(FIELDS.F38_AuthIdResponse,"323432313339")
            .setField(FIELDS.F39_ResponseCode,responseCode)
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,hexStringToByteArray(buildDE60_OriginalData(MTI,saleStan)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }

    fun isoUnpackResponse(isoMsg: String){
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()

        amount = bytesArrayToHexString(isoMessageUnpacket.getField(4)).toString()
        time = bytesArrayToHexString(isoMessageUnpacket.getField(12)).toString()
        date = bytesArrayToHexString(isoMessageUnpacket.getField(13)).toString()
        saleStan = bytesArrayToHexString(isoMessageUnpacket.getField(11)).toString()
        responseCode = bytesArrayToHexString(isoMessageUnpacket.getField(39)).toString()
        Log.i(log,"MTI:" + MTI)
        Log.i(log,"amount:" + amount)
        Log.i(log,"saleStan:" + saleStan)
        Log.i(log,"time:" + time)
        Log.i(log,"date:" + date)
        Log.i(log,"responseCode:" + responseCode)

    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun isoUnpackSale(isoMsg: String){
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()
        cardNO = bytesArrayToHexString(isoMessageUnpacket.getField(2)).toString()
        cardEXD = bytesArrayToHexString(isoMessageUnpacket.getField(14)).toString()

        Log.i(log,"cardNO:" + cardNO)
        Log.i(log,"cardEXD:" + cardEXD)

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

    fun buildDE60_OriginalData(MTI: String, STAN: String, DE37: String? = null):String {
        var de37 = ""
        if (DE37!=null){
            de37 = DE37
        }
        var DE60 = ""
        var mti = MTI
        var stan = STAN.padStart(6,'0')
        var reserve =""
        var data = mti+stan+de37+reserve.padEnd(12,'0')
        var data_arr :CharArray = data.toCharArray()
        for (i:Int in 0..data.length-1){
            val c = data_arr[i]
            val ascii = c.code
            DE60 += String.format("%02X", ascii)
        }
        return DE60
    }


    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()
        transactionDAO = appDatabase?.transactionDao()

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
}