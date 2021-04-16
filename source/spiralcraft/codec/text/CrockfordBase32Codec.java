package spiralcraft.codec.text;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import spiralcraft.log.ClassLog;

public class CrockfordBase32Codec
{
  private static final ClassLog log=ClassLog.getInstance(CrockfordBase32Codec.class);
  
  private static final char[] encoding = 
      {'0','1','2','3','4','5','6','7'
      ,'8','9','A','B','C','D','E','F'
      ,'G','H','J','K','M','N','P','Q'
      ,'R','S','T','V','W','X','Y','Z'
      };
      
  private static final char[] decoding = new char['z'+1];

  static
  {
    Arrays.fill(decoding, (char) -1);
    for (int i=0;i<32;i++)
    { decoding[Character.toLowerCase(encoding[i])]=Character.forDigit(i, 32);
    }
    for (int i=0;i<32;i++)
    { decoding[Character.toUpperCase(encoding[i])]=Character.forDigit(i, 32);
    }
    decoding['o']='0';
    decoding['O']='0';
    decoding['i']='1';
    decoding['I']='1';
    decoding['l']='1';
    decoding['L']='1';
    
  }
 
  public static String encodeLong(long number)
  { return encodeNumber(BigInteger.valueOf(number));
  }
  
  public static long decodeLong(String encoded)
  { return decodeNumber(encoded).longValue();
  }
  
  public static String encodeNumber(BigInteger number)
  {
    String numberString=number.toString(32);
    // log.fine("Base32 of "+number+" = "+numberString);
    StringBuilder out=new StringBuilder();
    for (char c: numberString.toCharArray())
    { 
      // log.fine(c+" -> "+Character.digit(c, 32)+" -> "+encoding[Character.digit(c,32)]);
      out.append(encoding[Character.digit(c, 32)]);
    }
    return out.toString();
  }
  
  public static BigInteger decodeNumber(String encoded)
  {
    // log.fine("decoding "+encoded);
    StringBuilder out=new StringBuilder();
    for (char c: encoded.toCharArray())
    { 
      // log.fine(c+" -> "+decoding[c]);
      out.append(decoding[c]);
    }
    return new BigInteger(out.toString(),32);
  }
  


}
