//
package spiralcraft.data.util;

/**
 * An expanding, lexicographically ordered sequence of Crockford Base32 strings.
 *
 * The first digit of each value determines its length, so a value is self-delimiting
 *   and the sequence never needs a terminator. The 32 possible first digits are shared
 *   out among the lengths, more of them to the short lengths and fewer to the long ones:
 *
 *   <pre>
 *   first digit   length
 *   0-3           1
 *   4-7           2
 *   8-B           3
 *   C-F           4
 *   G-H           5
 *   J-K           6
 *   M-N           7
 *   P-Q           8
 *   R             9
 *   S             10
 *   T             11
 *   V             12
 *   W             13
 *   X             14
 *   Y             15
 *   Z             16
 *   </pre>
 *
 * The values run 0, 1, 2, 3, 40, 41, ... 7Z, 800, ... and so on. About 3.9e22 values
 *   are available before the sequence is exhausted. The short lengths hold the most
 *   values per digit, so a small count stays short.
 *
 * The encoding is prefix-free and order-preserving: comparing two values as plain
 *   strings gives the same result as comparing their positions in the sequence, and
 *   concatenations of values compare the same as the lists of values, so a value can be
 *   followed by other data without a delimiter (the length of a value is known from its
 *   first digit, see {@link #lengthAt}).
 *
 * The next value is computed from the current value as a string; no integer position is
 *   involved. Null or empty starts the sequence at "0".
 *
 * The digit table is the Crockford table, the same one CrockfordBase32Codec uses (0-9,
 *   then A-Z without I, L, O and U), so ASCII order is the digit order. Input must be
 *   exactly as generated (upper case, no aliases).
 *
 * The allocation of first digits to lengths is part of the format: every reader must use
 *   the same one, so it cannot change once values have been stored.
 */
public final class CrockfordExpandingSequence
{
  private static final String DIGITS="0123456789ABCDEFGHJKMNPQRSTVWXYZ";

  // Number of first digits given to each length, starting at length 1
  private static final int[] DIGITS_PER_LENGTH
    ={ 4,4,4,4, 2,2,2,2, 1,1,1,1,1,1,1,1 };

  // The length of a value, by its first digit
  private static final int[] LENGTH_BY_FIRST_DIGIT=new int[DIGITS.length()];
  static
  {
    int digit=0;
    for (int i=0;i<DIGITS_PER_LENGTH.length;i++)
    { for (int j=0;j<DIGITS_PER_LENGTH[i];j++)
      { LENGTH_BY_FIRST_DIGIT[digit++]=i+1;
      }
    }
    if (digit!=DIGITS.length())
    { throw new ExceptionInInitializerError("Digit allocation must cover all "+DIGITS.length()+" first digits");
    }
  }

  private CrockfordExpandingSequence()
  { }

  /**
   * The value after the given one.
   *
   * @param current The current value, or null or empty to start the sequence
   * @return The next value ("0" if current is null or empty)
   * @throws IllegalArgumentException if current is not a value this class generates
   * @throws IllegalStateException if current is the last value of the sequence
   */
  public static String next(String current)
  {
    if (current==null || current.isEmpty())
    { return "0";
    }

    int first=DIGITS.indexOf(current.charAt(0));
    if (first<0 || current.length()!=LENGTH_BY_FIRST_DIGIT[first])
    { throw new IllegalArgumentException("Not a valid sequence value: "+current);
    }

    char[] d=current.toCharArray();
    for (char c:d)
    { if (DIGITS.indexOf(c)<0)
      { throw new IllegalArgumentException("Not a valid sequence value: "+current);
      }
    }

    // Increment as a base 32 number, carrying from the last digit
    int i=d.length-1;
    while (i>=0 && d[i]=='Z')
    { d[i--]='0';
    }
    if (i<0)
    { throw new IllegalStateException("Sequence exhausted");
    }
    d[i]=DIGITS.charAt(DIGITS.indexOf(d[i])+1);

    // The first digit may have moved into a longer length: pad with zeros to that length
    int length=LENGTH_BY_FIRST_DIGIT[DIGITS.indexOf(d[0])];
    if (length==d.length)
    { return new String(d);
    }
    StringBuilder b=new StringBuilder(length);
    b.append(d);
    while (b.length()<length)
    { b.append('0');
    }
    return b.toString();
  }

  /**
   * The length in characters of the value that starts at the given offset, from its
   *   first digit.
   */
  public static int lengthAt(CharSequence s,int offset)
  {
    int first=DIGITS.indexOf(s.charAt(offset));
    if (first<0)
    { throw new IllegalArgumentException("Not a valid sequence value at offset "+offset);
    }
    return LENGTH_BY_FIRST_DIGIT[first];
  }
}