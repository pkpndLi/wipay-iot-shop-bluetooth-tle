package com.example.wipay_iot_shop.cypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class iRSA
{
	private RSAPublicKey _pubKey;
	private RSAPrivateKey _priKey;

	private String errString=null;

	public iRSA()
	{
	}
	
	public boolean isPrime(int _prime_number)
	{
		boolean result=true;
		int i,m=0;      
		
		errString = null;
		m = _prime_number/2;
		
		if((_prime_number==0) || (_prime_number==1))
		{
			errString = "Is not a prime number.";
			result = false;
		}
		else
		{  
			for(i=2; i<=m; i++)
			{      
				if(_prime_number%i==0)
				{      
					result = false;      
					errString = "Is not a prime number.";
					break;      
				}      
			}      
		}
		
		return result;
	}

	/**
	 * 
//	 * @param Key size
	 * @return
	 * @throws InvalidParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws if something wrong.
	 */
	public boolean genKeyPair(int _key_size) throws InvalidParameterException,NoSuchAlgorithmException,InvalidKeySpecException
	{
		boolean ret=false;
		KeyPairGenerator kpg;
		
        try 
        {
        	errString = null;
        	if((_key_size%1024)==0)
        	{
		    	kpg = KeyPairGenerator.getInstance("RSA");
		    	kpg.initialize(_key_size);
		    	KeyPair kp = kpg.genKeyPair();
		    	Key publicKey = kp.getPublic();
		    	Key privateKey = kp.getPrivate();
		    
		    	KeyFactory fact = KeyFactory.getInstance("RSA");
		    	RSAPublicKeySpec pubKey = (RSAPublicKeySpec) fact.getKeySpec(publicKey, RSAPublicKeySpec.class);
		    	RSAPrivateKeySpec priKey = (RSAPrivateKeySpec) fact.getKeySpec(privateKey, RSAPrivateKeySpec.class);
		    	
		    	setPublicKey("00"+pubKey.getModulus().toString(16).toUpperCase(), pubKey.getPublicExponent().toString(16).toUpperCase(), 16);
		    	setPrivateKey("00"+priKey.getModulus().toString(16).toUpperCase(), priKey.getPrivateExponent().toString(16).toUpperCase(), 16);
	
		    	ret = true;
        	}
        	else
        	{
        		errString = "Invalid Key size.";
        	}
        } 
        catch(NoSuchAlgorithmException e) 
        {
        	errString = "NoSuchAlgorithmException";
        	throw e;
        } 
        catch(InvalidParameterException e)
        {
        	errString = "InvalidParameterException";
        	throw e;
        } 
        catch(InvalidKeySpecException e) 
        {
        	errString = "InvalidKeySpecException";
        	throw e;
        }
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

        return ret;
	}

	/**
	 * 
//	 * @param key size - bit(1024, 2048, 4096, ...)
	 * @param _prime_number
	 * @return
	 * @throws InvalidParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws if something wrong.
	 */
	public boolean genKeyPair(int _key_size, int _prime_number) throws InvalidParameterException,NoSuchAlgorithmException,InvalidKeySpecException, InvalidAlgorithmParameterException
	{
		boolean ret=false;
		KeyPairGenerator kpg;
		
        try 
        {
        	errString = null;
        	if((_key_size%1024)==0)
        	{
	        	if((ret=isPrime(_prime_number))==true)
	        	{
			    	kpg = KeyPairGenerator.getInstance("RSA");
			    	RSAKeyGenParameterSpec kpgSpec = new RSAKeyGenParameterSpec(_key_size, BigInteger.valueOf(_prime_number));
			    	kpg.initialize(kpgSpec);
			    	
			    	KeyPair kp = kpg.genKeyPair();
			    	Key publicKey = kp.getPublic();
			    	Key privateKey = kp.getPrivate();
			    
			    	KeyFactory fact = KeyFactory.getInstance("RSA");
			    	RSAPublicKeySpec pubKey = (RSAPublicKeySpec) fact.getKeySpec(publicKey, RSAPublicKeySpec.class);
			    	RSAPrivateKeySpec priKey = (RSAPrivateKeySpec) fact.getKeySpec(privateKey, RSAPrivateKeySpec.class);
			    	
			    	setPublicKey("00"+pubKey.getModulus().toString(16).toUpperCase(), pubKey.getPublicExponent().toString(16).toUpperCase(), 16);
			    	setPrivateKey("00"+priKey.getModulus().toString(16).toUpperCase(), priKey.getPrivateExponent().toString(16).toUpperCase(), 16);
		
			    	ret = true;
	        	}
	        	else
	        	{
	            	errString = "Is not a prime number.";
	        	}
        	}
        	else
        	{
            	errString = "Invalid Key size.";
        	}
        } 
        catch(NoSuchAlgorithmException e) 
        {
        	errString = "NoSuchAlgorithmException";
        	throw e;
        } 
        catch(InvalidParameterException e)
        {
        	errString = "InvalidParameterException";
        	throw e;
        } 
        catch(InvalidKeySpecException e) 
        {
        	errString = "InvalidKeySpecException";
        	throw e;
        }
		catch(Exception e)
		{
        	errString = "Exception";
			throw e;
		}

        return ret;
	}

	/**
	 * 
	 * @return RSAPublicKey object
	 */
	public RSAPublicKey getPublicKey()
	{
		return _pubKey;
	}
	
	/**
	 * 
	 * @return RSAPrivateKey object
	 */
	public RSAPrivateKey getPrivateKey()
	{
		return _priKey;
	}
	
	/**
	 * @category Set Private key (Hexadecimal only)
//	 * @param private modulus
//	 * @param private exponent
	 * @return RSAPrivateKey object
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPrivateKey setPrivateKey(String private_modulus, String private_exponent) throws NoSuchAlgorithmException,InvalidKeySpecException
	{
		try
		{
			errString = null;
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(private_modulus, 16), new BigInteger(private_exponent, 16));
			_priKey = (RSAPrivateKey) kf.generatePrivate(priKeySpec);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(InvalidKeySpecException e)
		{
			errString = "InvalidKeySpecException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}
		
		return _priKey;
	}
	
	/**
	 * @category Set Public key (Hexadecimal only)
	 * @param public_modulus
	 * @param public_exponent
	 * @return RSAPublicKey object
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey setPublicKey(String public_modulus, String public_exponent) throws NoSuchAlgorithmException,InvalidKeySpecException
	{
		try
		{
			errString = null;
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(public_modulus, 16), new BigInteger(public_exponent, 16));
			_pubKey = (RSAPublicKey) kf.generatePublic(pubKeySpec);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(InvalidKeySpecException e)
		{
			errString = "InvalidKeySpecException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}
		
		return _pubKey;
	}
	
	/**
//	 * @param private modulus
//	 * @param private exponent
	 * @param radix
	 * @return RSAPrivateKey
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPrivateKey setPrivateKey(String private_modulus, String private_exponent, int radix) throws NoSuchAlgorithmException,InvalidKeySpecException
	{
		try
		{
			errString = null;
			RSAPrivateKeySpec priKeySpec;
			KeyFactory kf = KeyFactory.getInstance("RSA");
			priKeySpec = new RSAPrivateKeySpec(new BigInteger(private_modulus, radix), new BigInteger(private_exponent, radix));
			_priKey = (RSAPrivateKey) kf.generatePrivate(priKeySpec);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(InvalidKeySpecException e)
		{
			errString = "InvalidKeySpecException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}
		
		return _priKey;
	}
	
	/**
//	 * @param public modulus
//	 * @param public exponent
	 * @param radix
	 * @return RSAPublicKey object
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey setPublicKey(String public_modulus, String public_exponent, int radix) throws NoSuchAlgorithmException,InvalidKeySpecException
	{
		try
		{
			errString = null;
			RSAPublicKeySpec pubKeySpec;
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pubKeySpec = new RSAPublicKeySpec(new BigInteger(public_modulus, radix), new BigInteger(public_exponent, radix));
			_pubKey = (RSAPublicKey) kf.generatePublic(pubKeySpec);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
			throw e;
		}
		catch(InvalidKeySpecException e)
		{
			errString = "InvalidKeySpecException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}
		
		return _pubKey;
	}

	/**
	 * @category Encrypt RSA by Private key
	 * @param data
	 * @return cipher data
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 */
	public byte[] enRSA_private(byte[] data) throws NoSuchAlgorithmException,IllegalBlockSizeException,BadPaddingException,InvalidKeyException,NoSuchPaddingException
	{
		byte[] cipher_data=null;

		try
		{
			errString = null;
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.ENCRYPT_MODE, _priKey);
			cipher_data = rsaCipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
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
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

		return cipher_data;
	}

	/**
	 * @category Encrypt RSA by Public key
	 * @param data
	 * @return cipher data
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 */
	public byte[] enRSA_public(byte[] data) throws NoSuchAlgorithmException,IllegalBlockSizeException,BadPaddingException,InvalidKeyException,NoSuchPaddingException
	{
		byte[] cipher_data=null;

		try
		{
			errString = null;
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.ENCRYPT_MODE, _pubKey);
			cipher_data = rsaCipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
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
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

		return cipher_data;
	}

	public byte[] enRSA_public(byte[] data, String transformation) throws NoSuchAlgorithmException,IllegalBlockSizeException,BadPaddingException,InvalidKeyException,NoSuchPaddingException
	{
		byte[] cipher_data=null;

		try
		{
			errString = null;
			Cipher rsaCipher = Cipher.getInstance(transformation);
			rsaCipher.init(Cipher.ENCRYPT_MODE, _pubKey);
			cipher_data = rsaCipher.doFinal(data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
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
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

		return cipher_data;
	}

	/**
	 * @category Decrypt RSA by Private key
//	 * @param cipher data
	 * @return 
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 */
	public byte[] deRSA_private(byte[] cipher_data) throws NoSuchAlgorithmException,IllegalBlockSizeException,BadPaddingException,InvalidKeyException,NoSuchPaddingException
	{
		byte[] data=null;

		try
		{
			errString = null;
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.DECRYPT_MODE, _priKey);
			data = rsaCipher.doFinal(cipher_data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
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
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

		return data;
	}

	/**
	 * @category Decrypt RSA by Public key
//	 * @param cipher data
	 * @return 
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 */
	public byte[] deRSA_public(byte[] cipher_data) throws NoSuchAlgorithmException,IllegalBlockSizeException,BadPaddingException,InvalidKeyException,NoSuchPaddingException
	{
		byte[] data=null;

		try
		{
			errString = null;
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.DECRYPT_MODE, _pubKey);
			data = rsaCipher.doFinal(cipher_data);
		}
		catch(NoSuchAlgorithmException e)
		{
			errString = "NoSuchAlgorithmException";
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
		catch(InvalidKeyException e)
		{
			errString = "InvalidKeyException";
			throw e;
		}
		catch(NoSuchPaddingException e)
		{
			errString = "NoSuchPaddingException";
			throw e;
		}
		catch(Exception e)
		{
			errString = "Exception";
			throw e;
		}

		return data;
	}

	/**
//	 * @param bytes - bytes number of random
	 * @return Random HEX number 
	 */
//	public byte[] getRandom(int bytes)
//	{
//		Random rand = new Random();
//		String [] charset = {"8","B","2","A","0","3","7","E","C","4","5","D","6","9","1","F"};  // 16 elements
//		StringBuffer ranStr = new StringBuffer();
//
//		for(int n=0; n<bytes*2; n++)
//			ranStr = ranStr.append(charset[rand.nextInt(16)]);
//
//		return DatatypeConverter.parseHexBinary(ranStr.toString());
//	}
	
	public String getError()
	{
		return errString;
	}
}
