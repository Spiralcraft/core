package spiralcraft.util;

import java.util.Random;

/**
 * Utility class to support the generation of random numbers and Strings.
 * 
 * @author mike
 *
 */
public class RandomUtil
{
  private static char[] chars=
    {'A'
    ,'B'
    ,'C'
    ,'D'
    ,'E'
    ,'F'
    ,'G'
    ,'H'
    ,'I'
    ,'J'
    ,'K'
    ,'L'
    ,'M'
    ,'N'
    ,'O'
    ,'P'
    ,'Q'
    ,'R'
    ,'S'
    ,'T'
    ,'U'
    ,'V'
    ,'W'
    ,'X'
    ,'Y'
    ,'Z'
    ,'0'
    ,'1'
    ,'2'
    ,'3'
    ,'4'
    ,'5'
    ,'6'
    ,'7'
    ,'8'
    ,'9'
    };

  private static final Random RANDOM=new Random();

  public static String generateString(int length)
  {
    StringBuilder out=new StringBuilder(length);
    for (int i=0;i<length;i++)
    { out.append(chars[ Math.abs(RANDOM.nextInt()%36) ]);
    }
    return out.toString();
  }
}
