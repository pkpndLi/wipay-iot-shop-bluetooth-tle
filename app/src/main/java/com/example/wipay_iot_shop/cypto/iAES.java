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

public class iAES
{
	private byte[] IV;
	
	public iAES()
	{
		IV = new byte[16];
	}
	
	public iAES(byte[] iv)
	{
		IV = new byte[16];
		System.arraycopy(iv, 0, IV, 0, IV.length);
	}
	
	public void setIV(byte[] iv)
	{
		System.arraycopy(iv, 0, IV, 0, IV.length);
	}
	
	/**
	 * 
	 * @param key - Key 16/24/32 bytes
	 * @param transformation <ul><li>AES <li>AES/ECB/NoPadding <li>AES/ECB/PKCS5Padding same as AES </ul> 
	 * @param op_mode - Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param data - Input data 16 bytes
	 * @return byte[] - cipher data or plan text.<p>
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 *  
	 */
	public byte[] AES(byte[] key, String transformation, int op_mode, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "AES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			cipher = Cipher.getInstance(transformation);
			cipher.init(op_mode, ky);
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			throw e;
		}
		catch(InvalidKeyException e)
		{
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			throw e;
		}
		catch (BadPaddingException e)
		{
			throw e;
		}
		return ret;
	}

	/**
	 * @param key - Key 16/24/32 bytes
	 * @param transformation <ul><li>AES <li>AES/ECB/NoPadding <li>AES/ECB/PKCS5Padding same as AES  <li>AES/CBC/NoPadding <li>AES/CBC/PKCS5Padding same as AES </ul> 
	 * @param data - Input data
	 * @return array bytes of cipher data.<p>
	 * @throws if something wrong.
	 */
	public byte[] enAES(byte[] key, String transformation, byte[] data) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "AES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				if(transformation.indexOf("CTR")<0)
					cipher.init(Cipher.ENCRYPT_MODE, ky);
				else
					cipher.init(Cipher.ENCRYPT_MODE, ky, new IvParameterSpec(IV));
			else
				cipher.init(Cipher.ENCRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			throw e;
		}
		catch(InvalidKeyException e)
		{
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			throw e;
		}
		catch (BadPaddingException e)
		{
			throw e;
		}
		catch(InvalidAlgorithmParameterException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * @param key - Key 16/24/32 bytes
	 * @param transformation <ul><li>AES <li>AES/CBC/NoPadding <li>AES/CBC/PKCS5Padding same as AES  <li>AES/CBC/NoPadding <li>AES/CBC/PKCS5Padding same as AES </ul> 
	 * @param data - Input data
	 * @return array bytes of plan text.<p>
	 * @throws if something wrong.
	 */
	public byte[] deAES(byte[] key, String transformation, byte[] data) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException 
	{
		SecretKey ky = new SecretKeySpec(key, "AES");
		Cipher cipher;
		byte[] ret=null;
		
		try
		{
			cipher = Cipher.getInstance(transformation);
			if(transformation.indexOf("CBC")<0)
				if(transformation.indexOf("CTR")<0)
					cipher.init(Cipher.DECRYPT_MODE, ky);
				else
					cipher.init(Cipher.DECRYPT_MODE, ky, new IvParameterSpec(IV));
			else
				cipher.init(Cipher.DECRYPT_MODE, ky, new IvParameterSpec(IV));
			ret = cipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			throw e;
		}
		catch(InvalidKeyException e)
		{
			throw e;
		}
		catch(IllegalBlockSizeException e)
		{
			throw e;
		}
		catch (BadPaddingException e)
		{
			throw e;
		}
		catch(InvalidAlgorithmParameterException e)
		{
			e.printStackTrace();
		}
		return ret;
	}	
}
