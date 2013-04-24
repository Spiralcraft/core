package spiralcraft.util;


/**
 * Utility class to support the generation of random numbers and Strings.
 * 
 * @author mike
 *
 */
public class RandomUtil
{

  public static String generateString(int length)
  { return Random.secureInstance().generateString(length);
  }

  
}
