package spiralcraft.util;

import java.security.SecureRandom;


/**
 * Utility class to support the generation of random numbers and Strings.
 * 
 * @author mike
 *
 */
public class Random
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

  
  private static final Random INSTANCE=new Random();
  private static final Random SECURE_INSTANCE=new Random(new SecureRandom());

  public static final Random instance()
  { return INSTANCE;
  }
  
  public static final Random secureInstance()
  { return SECURE_INSTANCE;
  }
  
  private final java.util.Random random;
  
  public Random()
  { this.random=new java.util.Random();
  }
  
  protected Random(java.util.Random random)
  { this.random=random;
  }
  
  public String generateString(int length)
  {
    StringBuilder out=new StringBuilder(length);
    for (int i=0;i<length;i++)
    { out.append(chars[ Math.abs(random.nextInt()%36) ]);
    }
    return out.toString();
  }

  
}
