
package spiralcraft.text;

/**
 * A case insensitive String
 *
 * Intended for use in case insensitive hash tables.
 */
public final class CaseInsensitiveString
{
  public CaseInsensitiveString(String string)
  { 
    _string=string;

    // 
    // JDK1.2 standard String hash function
    //
    // s[0] * 31^(n-1) + s[1] * 31^(n-2) + ... + s[n-1]
    //

    final char[] chars=_string.toCharArray();
    int hashcode=0;
    int len=chars.length;
    for (int i=0;i<len-1;i++)
    { hashcode+=((int) Character.toLowerCase(chars[i])) * 31^(len-(i+1));
    }
    if (len>0)
    { hashcode+=((int) Character.toLowerCase(chars[len-1]));
    }
    _hashcode=hashcode;
  }

  public final int hashCode()
  { return _hashcode;
  }

  public final boolean equals(final Object target)
  {
    if (this==target)
    { return true;
    }
    else if
      (target==null
      || !(target instanceof CaseInsensitiveString)
      )
    { return false;
    }
    else
    { return target.toString().equalsIgnoreCase(_string);
    }
  }

  public final String toString()
  { return _string;
  }

  private final String _string;
  private final int _hashcode;

}
