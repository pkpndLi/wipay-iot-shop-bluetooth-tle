package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.cypto.DataConverter
import com.example.wipay_iot_shop.cypto.iDES
import com.example.wipay_iot_shop.cypto.iRSA
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and
import com.imohsenb.ISO8583.utils.StringUtil.hexStringToByteArray

import com.imohsenb.ISO8583.exceptions.ISOClientException

import com.imohsenb.ISO8583.exceptions.ISOException
import com.imohsenb.ISO8583.builders.ISOClientBuilder

import com.imohsenb.ISO8583.interfaces.ISOClient
import com.imohsenb.ISO8583.utils.StringUtil

import com.imohsenb.ISO8583.utils.StringUtil.hexStringToByteArray
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.lang.reflect.InvocationTargetException

class FunctionTest : AppCompatActivity() {

    var log = "log"
    var indicator = "HTLE"
    var version = "04"
    var downlondType = "4"
    var reqType = "1"
    var acqID = "120"
    var LITD = "00000000"
    var vendorID = "12000002"
    var stan = "000002"
    var TE_ID = "12002002"
    var rsaExp = "010001"
    var rsaMod = "E1BD7DE8B0B38989D37AAC0D0115B988BFC679D09B15B66F28B8F3CA0891D04A2A6579FA423C0FE97F6D8CE02E381D6B92149B25F2E57263829CA643465DB25D7DA415EEDA8D715AD0B52C8BDB4132C3A53612D2283BBBCBC5BDB39D8EB0072CA52013E41D551A6F75EEE80A439D09ED9577B9C5314039CD6630CB4B081236058BF239F6636DBAF4728B89F5B6F9743D9B8CD58564A18762BF0CD8F5DB1B297BFDC64BC460543612437C5EF8ADB033BA79BC0CB93299A886466A93BB2DA10309EC8587C0E2F69F9C702CF973B51B7C9E55A1381C0B78132D6B0769CD1E1AF6859719F41314DD67885C62FEBFD7892C67F1B499215F233557463E692580EC05A5"
    var TE_PIN = "22222222"
    var ltmkId = "2809"
    var ltwkIdCount = "0000"

    var tid = "22222222"
    var mid = "222222222222222"
    var padding = "1234"
    var pinHash:String  = ""
    var txnHash:String = ""
    var stringHash = ""
    var cardNO = "4162026250958064"
    var cardEXD = "2512"
    var totalAmount = 200

    //tle parameter
    var makKey = "3991E2A306727C99B85BB694E4AD7F54"
    var dekKey = "139EB7CE451189AA8E613C0BA77D9045"
    var ltwkId = "9227"
    var macRawEncrypted = "6357B82E4E7F4C952FC02EDB6818E988206F56061A079FBB173D016F9A76351F8E1ABE0C88C6B566065A2872D3AEAE9EC1E5065B3B6587A1F9AFD70124FC4C5BFAB588B8D777AFA4"
    var cipherText = "03E9D376D0C0D20C39540D3E2C1C5791"
    var encryptMethod = "2000"
    var encryptCounter = "0048"
    var reserved = "00000000"
//    var eBit57 = ""
    var eBit64 = "8354917A00000000"
    var _bit64: ByteArray = ByteArray(8)
    var dCipherText = "0406000000020000"
    var _bit57 = ""

    var strBit62Ltmk:String = ""
    var strBit62Ltwk:String = ""

    val rsa = iRSA()
    val des = iDES()
    val dataConverter = DataConverter()

//    private val HOST = "192.168.58.89"
//    var PORT = 5000

    private val HOST = "223.27.234.243"
    var PORT = 5000

//    private val HOST = "192.168.43.24"
//    var PORT = 3000

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_function_test)

        var ltmkBtn = findViewById<Button>(R.id.mk)
        var ltwkBtn = findViewById<Button>(R.id.wk)
        var saleBtn = findViewById<Button>(R.id.saleBtn)
        var responseBtn = findViewById<Button>(R.id.responseBtn)
        var getStanBtn = findViewById<EditText>(R.id.stan)

        LITD = serialNumber()

//        Log.e(log,"convert to hash: " + bytesArrayToHexString(sha1("12002002222222221234")))
//        pinHash = bytesArrayToHexString(sha1("12002002222222221234"))!!.substring(0,8)
//        stringHash = pinHash + LITD + TraceNum.substring(2)
//        Log.e(log,"stringHash: " + stringHash)
//        txnHash = bytesArrayToHexString(sha1(stringHash))!!
//        Log.e(log,"salePacket: " + salePacket().toString())

//        LITD = "77777777"
        Log.e(log,"serialNumber: " + serialNumber())
        txnHash = TXN_Hash(TE_ID,TE_PIN,LITD,stan)
        Log.e(log,"txnHash: " + txnHash)

        strBit62Ltmk = bit62Ltmk(indicator,version,downlondType,reqType,acqID,LITD,vendorID,TE_ID,txnHash,rsaExp,rsaMod)
        Log.e(log,"bit62Ltmk: " + strBit62Ltmk)

        strBit62Ltwk = bit62Ltwk(indicator,version,reqType,acqID,acqID,LITD,vendorID,ltmkId,ltwkIdCount)
        Log.e(log,"bit62Ltwk: " + strBit62Ltwk)
        Log.e(log,"ltwkPacket: " + ltwkPacket())

        //tleMsg
//        Log.e(log,"sale original: " + salePacket().toString())
////        Log.e(log,"test bit64Mac func.: " + bytesArrayToHexString(bit64Mac(salePacket().toString(),makKey)))
//        var tlvMsg = buildTLVMsg(cardNO,cardEXD)
//        Log.e(log,"test buildTLVMsg func: " + tlvMsg)
//        var tlvLen = (tlvMsg.length/2).toString()
//        var tlvLen = (buildTLVMsg(cardNO,cardEXD).length).toString()
//        Log.e(log,"tlvLen: "+ tlvLen)
//        var eTLVMsg = eTLV(tlvMsg,dekKey)
//        Log.e(log,"etleMsg: "+ eTLVMsg)
//        var tleMsg = bit57Ver4(indicator,version,acqID,LITD,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved,cipherText)
//        Log.e(log,"test bit57 func: " + tleMsg)



//        Log.e(log,"test unpackIso func: " + unpackIso("6080010126081020380100028000049700000000080746411126012030303232323232323232029348544c45303434313132304dab11d7ba05376fab31bec7a1f5b030829949333d5435789d22a62930c7f07dd8a8270d518ec85460b61af42cac847bb4c4650c04d6a786f422b880b05126ceb092ec5d155ba9e88470b366ead10ce1a5c6a53dcf811eac713b4fd0dc26b07bd981a5365ae59f4ce1ddbba1c953af25261646c60ea15a6766428afc86d435bf42c4fed0aad732fd4c9859adee2d9855b1abcc4a52f102ee57e6dae57692f14944a2f35ed8a8527e78ebfe1d72995cde4a7b4432bc208e030c3dccef7972db1f7d5bf5600238802cbd756aba3050e0f76eb0f861229b0ab238b0c61a969e2c10bf9b9d02b1bdd2ec01fa043af458a8a7fc67b287d0d4e372f95283fe62805db23646463431443136303520202020202020202020202020202020"))
//        Log.e(log,"salePacketTestMac(): " + salePacketTestMac().toString())
//        Log.e(log,"test bit64 func.: " + bit64Mac(salePacketTestMac().toString()))

//
//        val hex = convertStringToHex(input, false)
//        Log.e(log,"hex : $hex")

        ltmkBtn.setOnClickListener{


            Log.e(log, "send ltmk")
            Log.e(log, "ltmk msg: " + ltmkPacket())
//            var salePacket = salePacket()
//            Log.e(log,"salePacketNoMac: " + salePacketNoMac())
//            Log.e(log,"salePacket: " + salePacket)
//            Log.e(log,"test MacMsg: " + salePacketTestMac())
            sendPacket(ltmkPacket())
        }

        ltwkBtn.setOnClickListener{

           Log.e(log, "send ltwk")
           Log.w(log, "ltwk msg: " + ltwkPacket())
           sendPacket(ltwkPacket())
        }

        saleBtn.setOnClickListener{

//            Log.d(log,"...build tleSalePacket...")
//            Log.e(log,"sale original: " + salePacket().toString())
//            _bit64 = bit64Mac(salePacket().toString(),makKey)
//            Log.e(log,"_bit64: " + _bit64)
//            var tlvMsg = buildTLVMsg(cardNO,cardEXD)
//            Log.e(log,"test buildTLVMsg func: " + tlvMsg)
//            var tlvLen = (tlvMsg.length/2).toString()
//            Log.e(log,"tlvLen: "+ tlvLen)
//            cipherText = eTLV(tlvMsg,dekKey)
//            Log.e(log,"cipherText: "+ cipherText)
//            var _bit57 = bit57Ver4(indicator,version,acqID,LITD,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved,cipherText)
//            Log.e(log,"_bit57: " + _bit57)
//            var salePacketTLE = salePacketTle(hexStringToByteArray(_bit57)!!,_bit64)
//            Log.e(log,"saleTleMsg: " + salePacketTLE)
//            sendPacket(salePacketTLE)
            Log.e(log,"test reverseMsg: " + reversePacketWithMac())
            Log.e(log,"test reverseTleMsg: " + reverseTlePacket(reversePacketWithMac().toString()))
            sendPacket(reverseTlePacket(reversePacketWithMac().toString()))
        }

        responseBtn.setOnClickListener{
//            Log.d(log,"...check MAC response...")
//            var responseMsg = "60800101270210203801000E8000850040000000040934311202012054455354303930303030303434303134303130303232323232323232004948544C45303431323035353238613130383230303039323237303034380000000800000000000000001AF68872FEAC3BD20006303030343841AA51690900000000"
//            var bit57Msg = codeUnpack(responseMsg,57).toString().uppercase(Locale.getDefault())
//            Log.w(log,"dBit57Msg: " + bit57Msg)
//            var tlvResponseData = dCipherText(bit57Msg,dekKey)
//            Log.w(log,"tlvResponseData: " + tlvResponseData)
//            var bit64Response = macResponse(tlvResponseData,responseMsg,makKey)
//            Log.w(log,"bit64Response: " + bit64Response)
            var responseMsg = "60012780010200303801000E80000500400000000002000000000409343112020120544553543039303030303034343031343031303032323232323232320006303030343841"
            var newResponseMsg = ArrayList<String>()
            Log.d(log,"...response...")
            unpackIso(responseMsg,newResponseMsg)
            var saleMsg = "600127800102007024058000C000051641620262509580640040000000000200000000042512001001200032323232323232323232323232323232323232323232320006303030343841"
            var newSaleMsg = ArrayList<String>()
            Log.d(log,"...sale...")
            unpackIso(saleMsg,newSaleMsg)
        }

    }

    fun reverseTlePacket(isoMsg: String):ISOMessage{

        Log.d(log,"...build tleReversePacket...")
        Log.e(log,"original packet: " + isoMsg)
        _bit64 = bit64Mac(isoMsg,makKey)
        Log.e(log,"_bit64: " + _bit64)
        var tlvMsg = buildTLVMsg(cardNO,cardEXD)
        Log.e(log,"test buildTLVMsg func: " + tlvMsg)
        var tlvLen = (tlvMsg.length/2).toString()
        Log.e(log,"tlvLen: "+ tlvLen)
        cipherText = eTLV(tlvMsg,dekKey)
        Log.e(log,"cipherText: "+ cipherText)
        _bit57 = bit57Ver4(indicator,version,acqID,LITD,encryptMethod,ltwkId,encryptCounter,tlvLen,reserved,cipherText)
        Log.e(log,"_bit57: " + _bit57)
        var tlePacket = reversePacketTle(hexStringToByteArray(_bit57)!!,_bit64)
        Log.e(log,"reverseTleMsg: " + tlePacket)

        return tlePacket
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

    fun eTLV(tlvMsg: String,key: String): String{

        var eTLVMsg = des.enDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(tlvMsg + "00"))
        var eTLV = dataConverter.HexByteToHexString(eTLVMsg)
//        Log.e(log,"eTLV: " + eTLV)

        return eTLV
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

    fun dCipherText(bit57Msg: String,key: String):String{

        var cipherMsg = bit57Msg.substring(bit57Msg.length - 16)
        Log.w(log,"cipherMsg: " + cipherMsg)
        //add Decryption TDES func.
        var dCipherMsg = des.deDESede(dataConverter.HexString2HexByte(key),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(cipherMsg))
        var dCipherData = dataConverter.HexByteToHexString(dCipherMsg)
//        Log.w(log,"dCipherMsg: " + dCipherData)

        return dCipherData
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

//    fun bit57(indicator:String,version:String,acqID:String,tid:String,encryptMethod:String,ltwkId:String,encryptCount:String,TLVLen:String,reserved:String,cipherText:String):String{
//
//            var tlvLen = ("000" + TLVLen).substring(TLVLen.length)
//
//            var data = indicator + version + acqID + tid + encryptMethod  + ltwkId + encryptCount + tlvLen + reserved
//            Log.e(log,"bit57Data: " + data)
//            var hexData = convertStringToHex(data,false)
//            var bit57Msg = hexData + cipherText
//
//        return bit57Msg
//    }

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

    fun serialNumber():String {
        var androidId: String = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        var androidId = "24215d325528a108"
        var sn = androidId.substring(androidId.length - 8)
        return sn
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

    fun octToDec(octal: Int): Int {
        var octal = octal
        var decimalNumber = 0
        var i = 0

        while (octal != 0) {
            decimalNumber += (octal % 10 * Math.pow(8.0, i.toDouble())).toInt()
            ++i
            octal /= 10
        }

        return decimalNumber
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun print(bytes: ByteArray): String {
        val sb = java.lang.StringBuilder()
        sb.append("[ ")
        for (b in bytes) {
            sb.append(String.format("0x%02X ", b))
        }
        sb.append("]")
        return sb.toString()
    }

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

    fun manageResponse(event: MessageEvent){

        Log.i("log_tag", "Response Message:" + event.message)
        var responseMsg = event.message

        var responseCode = codeUnpack(responseMsg,39).toString()
        Log.e(log, "response code:"+ responseCode)

        if(responseCode == "3030"){

//                setNormalDialog("","Download Master Key Success.")
                responseMsg = "60800101270210203801000E8000850040000000040934311202012054455354303930303030303434303134303130303232323232323232004948544C45303431323035353238613130383230303039323237303034380000000800000000000000001AF68872FEAC3BD20006303030343841AA51690900000000"
                var bit57Msg = codeUnpack(responseMsg,57).toString()


        }else{

            var errorMsg = codeUnpack(responseMsg,39).toString()
            Log.e(log,"Download Key Error: " + errorMsg)
//            setNormalDialog("Download Key Fail.",errorMsg)

        }

    }


    fun sendPacket(packet: ISOMessage?){

        Thread {
            try {

                var client = ISOClientBuilder.createSocket(HOST, PORT)
                    .configureBlocking(false)
                    .setReadTimeout(10000)
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
                    Log.e(log, "Read Timeout")
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                }
            }
        }.start()
    }

    fun bit62Ltmk(indicator:String,version:String,downlondType:String,requestType:String,acqID:String,LTID:String,vendorID:String,TEID:String,txnHash:String,rsaExp:String,rsaMod:String):String {

        var str = indicator + version + downlondType + requestType + acqID + LITD + vendorID + TEID + txnHash
        var strToHex = convertStringToHex(str,false)
        var buildBit62 = strToHex + rsaExp + rsaMod

        return buildBit62
    }

   fun bit62Ltwk(indicator:String,version:String,requestType:String,LTMKacqID:String,acqID:String,LTID:String,vendorID:String,ltmkId:String,ltwkId:String):String {

       var str = indicator + version + requestType + LTMKacqID + acqID + LITD + vendorID + ltmkId + ltwkId
       var strToHex: String = convertStringToHex(str,false).toString()

       return strToHex
   }


    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun sha1(str: String): ByteArray = MessageDigest.getInstance("SHA-1").digest(str.toByteArray(UTF_8))


    fun TXN_Hash(TE_ID:String,TE_PIN:String,LITD:String,STAN:String):String{
        var pinHash:String  = ""
        var txnHash:String = ""
        pinHash = bytesArrayToHexString(sha1(TE_ID+TE_PIN+padding))?.uppercase(Locale.getDefault()) ?: String()
        pinHash = pinHash.substring(0,8)
        txnHash = bytesArrayToHexString(sha1(pinHash+LITD+STAN.substring(STAN.length-4,STAN.length)))?.uppercase(Locale.getDefault()) ?: String()
        Log.e(log,"convert to hash: " + txnHash)
        return txnHash.substring(0,8)
    }

//    @Throws(ISOException::class, ISOClientException::class, IOException::class,InvocationTargetException::class)
//    private fun salePacketNoMac(): String {
//          try{
//              var isoMsg :ISOMessage = ISOMessageBuilder.Packer(VERSION.V1987)
//                  .financial()
//                  .setLeftPadding(0x00.toByte())
//                  .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
//                  .processCode("000000")
//                  .setField(FIELDS.F2_PAN, cardNO)
//                  .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
//                  .setField(FIELDS.F11_STAN, stan)
//                  .setField(FIELDS.F14_ExpirationDate, cardEXD)
//                  .setField(FIELDS.F22_EntryMode, "0010")
//                  .setField(FIELDS.F24_NII_FunctionCode, "120")
//                  .setField(FIELDS.F25_POS_ConditionCode, "00")
//                  .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
//                  .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
//                  .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
//                  .setHeader("6001268001")
//                  .build()
//
//              return isoMsg.toString()
//          }  catch (err: ISOClientException){
//              return err.message.toString()
//
//          } catch(err: ISOException){
//              return err.message.toString()
//          }
//
//    }

    @Throws(ISOException::class, ISOClientException::class, IOException::class)
    fun salePacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
            .setField(FIELDS.F11_STAN, stan)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()

    }

    fun salePacketTle(bit57:ByteArray,bit64Mac:ByteArray): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
            .setField(FIELDS.F11_STAN, stan)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,bit64Mac)
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
            .setField(FIELDS.F41_CA_TerminalID,
                StringUtil.hexStringToByteArray(convertStringToHex(tid, false)))
            .setField(FIELDS.F42_CA_ID,
                StringUtil.hexStringToByteArray(convertStringToHex(mid, false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001278001")
            .build()
    }

    fun reversePacketTle(bit57:ByteArray,bit64Mac:ByteArray): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reversal()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("004000")
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
            .setField(FIELDS.F11_STAN, stan)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(convertStringToHex(mid, false)))
            .setField(FIELDS.F57_Reserved_National,bit57)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,bit64Mac)
            .setHeader("6001278001")
            .build()

    }




    fun salePacketOriginal(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
            .setField(FIELDS.F11_STAN, stan)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001268001")
            .build()

    }

    fun salePacketTestMac(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("003000")
            .setField(FIELDS.F2_PAN, "4830990000183673")
            .setField(FIELDS.F4_AmountTransaction, "000000000556")
            .setField(FIELDS.F11_STAN, "000001")
            .setField(FIELDS.F14_ExpirationDate, "2208")
            .setField(FIELDS.F22_EntryMode, "0022")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F35_Track2,"374830990000183673D22082210000006300000F")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3131313131313131"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("313131313131313131313131313131"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001208000")
            .build()

    }

    private fun ltwkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970400")
            .setField(FIELDS.F11_STAN,stan)
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID, hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltwk))
            .setHeader("6001268001")
            .build()
    }

    private fun ltmkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970000")
            .setField(FIELDS.F11_STAN,stan)
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID, hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltmk))
            .setHeader("6001268001")
            .build()
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

    fun convertStringToHex(str: String, lowercase: Boolean): String? {
        val HEX_ARRAY: CharArray = if (lowercase) HEX_LOWER else HEX_UPPER
        val bytes = str.toByteArray(UTF_8)

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

    fun convertToFloat(Totalamount : Double ):String{
        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]
        return Amount
    }


}