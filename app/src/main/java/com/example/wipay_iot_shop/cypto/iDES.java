package com.example.wipay_iot_shop.cypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class iDES
{
	public byte[] IV=null;
	private String errString=null;
	
	public iDES()
	{
		IV = new byte[8];
	}

	public iDES(byte[] iv)
	{
		IV = iv.clone();
	}

	public void setIV(byte[] iv)
	{
		IV = iv.clone();
	}
	
	/**
	 * 
	 * @param key - Key 8 bit
	 * @param transformation - DES/NoPadding, DES/ECB/PKCS5Padding 
	 * @param op_mode - Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param data
	 * @return byte[] - cipher data
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 *  
	 */
	public byte[] DES(byte[] key, String transformation, int op_mode, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "DES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			errString = null;
			cipher = Cipher.getInstance(transformation);
			cipher.init(op_mode, ky);
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}

		return ret;
	}

	/**
	 * @param key - Key 8 bit
	 * @param transformation - DES/NoPadding, DES/ECB/PKCS5Padding 
	 * @param data
	 * @throws if something wrong.
	 */
	public byte[] enDES(byte[] key, String transformation, byte[] data) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "DES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			errString = null;
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				cipher.init(Cipher.ENCRYPT_MODE, ky);
			else
				cipher.init(Cipher.ENCRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}
		catch(InvalidAlgorithmParameterException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @param key - Key 8 bit
	 * @param transformation - DES/NoPadding, DES/ECB/PKCS5Padding 
	 * @param data
	 * @throws if something wrong.
	 */
	public byte[] deDES(byte[] key, String transformation, byte[] data) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "DES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			errString = null;
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				cipher.init(Cipher.DECRYPT_MODE, ky);
			else
				cipher.init(Cipher.DECRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}
		catch(InvalidAlgorithmParameterException e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}

	/**
	 * @param key - Key1+Key2+Key1, 24 bytes or Key1+Key2, 16 bytes
	 * @param transformation - DESede/ECB/PKCS5Padding
	 * @param op_mode - Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param data
	 * @throws if something wrong.
	 */
	public byte[] DESede(byte[] key, String transformation, int op_mode, byte[] data) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException
	{
		SecretKey ky = new SecretKeySpec(key, "DESede");
		Cipher cipher;
		byte[] ret=null;

		try
		{
			errString = null;
			cipher = Cipher.getInstance(transformation);
			cipher.init(op_mode, ky);
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}
		
		return ret;
	}

	/**
	 * @param key - Key1+Key2+Key1, 24 bytes or Key1+Key2, 16 bytes 
	 * @param transformation - DESede/ECB/PKCS5Padding, DESede/ECB/NoPadding, DESede/CBC/PKCS5Padding, DESede/CBC/NoPadding
	 * @param data
	 * @throws if something wrong.
	 */
	public byte[] enDESede(byte[] key, String transformation, byte[] data) throws InvalidAlgorithmParameterException,NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException
	{
		byte[] _key=null; 
		SecretKey ky=null;
		Cipher cipher;
//		byte[] zero={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		byte[] ret=null;

		errString = null;
		_key = new byte[24];

		if(key.length==16)
		{
			System.arraycopy(key, 0, _key, 0, key.length);
			System.arraycopy(key, 0, _key, 16, 8);
		}
		else 
		{
			System.arraycopy(key, 0, _key, 0, _key.length);
		}

		ky = new SecretKeySpec(_key, "DESede");
		
		try
		{
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				cipher.init(Cipher.ENCRYPT_MODE, ky);
			else
				cipher.init(Cipher.ENCRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(InvalidAlgorithmParameterException e) 
		{
			errString = "InvalidAlgorithmParameterException";
			throw e;
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}
		
		return ret;
	}

	/**
	 * @param key - Key1+Key2+Key1, 24 bytes or Key1+Key2, 16 bytes 
	 * @param transformation - DESede/ECB/PKCS5Padding, DESede/ECB/NoPadding, DESede/CBC/PKCS5Padding, DESede/CBC/NoPadding
	 * @param data
	 * @throws if something wrong.
	 */
	public byte[] deDESede(byte[] key, String transformation, byte[] data) throws InvalidAlgorithmParameterException,NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException
	{
		byte[] _key=null; 
		SecretKey ky=null;
		Cipher cipher;
//		byte[] zero={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		byte[] ret=null;

		errString = null;
		_key = new byte[24];

		if(key.length==16)
		{
			System.arraycopy(key, 0, _key, 0, key.length);
			System.arraycopy(key, 0, _key, 16, 8);
		}
		else
			System.arraycopy(key, 0, _key, 0, _key.length);

		ky = new SecretKeySpec(_key, "DESede");
		
		try
		{
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				cipher.init(Cipher.DECRYPT_MODE, ky);
			else
				cipher.init(Cipher.DECRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(InvalidAlgorithmParameterException e) 
		{
			errString = "InvalidAlgorithmParameterException";
			throw e;
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			errString = "IllegalBlockSizeException";
			throw e;
		}
		catch(BadPaddingException e)
		{
			errString = "BadPaddingException";
			throw e;
		}
		return ret;
	}

	/**
//	 * @param digit - digit number of random
	 * @return Random HEX number 
	 */
//	public byte[] getRandom(int digit)
//	{
//		Random rand = new Random();
//		String [] charset = {"A","7","2","5","9","B","8","C","1","D","3","0","E","6","F","4"};  // 16 elements
//		StringBuffer ranStr = new StringBuffer();
//
//		errString = null;
//		if((digit%2)!=0)
//			ranStr = ranStr.append(charset[0]);
//		for(int n=0; n<digit; n++)
//			ranStr = ranStr.append(charset[rand.nextInt(16)]);
//		return DatatypeConverter.parseHexBinary(ranStr.toString());
//	}

	public String getError()
	{
		return errString;
	}
	
	public boolean AdjustDESKeyParity(byte[] Key)
	{
	   boolean cPar;
	   byte data;
	   
	   for(int i = 0; i < Key.length; i++)
	   {
	      cPar = false;
	      data = (byte)(Key[i]&0xff);
	      for(int j = 0; j < 8; j++)
	      {
	    	  if(((data&0xff)&(0x01<<j))!=0x00)
	    		  cPar = !cPar;
	      }
	      if(!cPar)
	    	  Key[i] ^= 0x01;
	   }
	   
	   return true;
	}

}
