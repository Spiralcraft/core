package spiralcraft.util;

/**
 * A growable byte[]
 */
public final class ByteBuffer
{
	private byte[] _array;
	private int _length=0;
  private final float _growthFactor=1.5f;

	public ByteBuffer()
	{ this(0);
  }

  public ByteBuffer(int initialCapacity)
  { _array=new byte[initialCapacity];
  }

	public final void append(byte b)
  {
		if (_length>=_array.length)
    { ensureCapacity(_length+1);
    }
		_array[_length++]=b;
  }

  public final void append(int i)
  { append((byte) i);
  }

  public final void append(byte[] b)
  { append(b,0,b.length);
  }

  public final void append(byte[] b,int start,int len)
  {
    if (_length+len>_array.length)
    { ensureCapacity(_length+len);
    }
    System.arraycopy(b,start,_array,_length,len);
    _length+=len;
  }


	private final void ensureCapacity(int capacity)
	{
		if (capacity>_array.length)
		{ 
			int targetCapacity
        =(int) Math.max(_length*_growthFactor,capacity);
			byte[] newarray=new byte[targetCapacity];
			System.arraycopy(_array,0,newarray,0,_array.length);
			_array=newarray;
		}
	}

  public final void clear()
  { _length=0;
  }
    
  public final int length()
  { return _length;
  }

  public final byte[] removeBeginning(int len)
  {
		byte[] ret=new byte[Math.min(len,_length)];
 		System.arraycopy(_array,0,ret,0,ret.length);
    
    byte[] replace=new byte[_length-ret.length];
    System.arraycopy(_array,ret.length,replace,0,replace.length);
    _array=replace;

		return ret;
  }

	public final byte[] toByteArray()
	{
		final byte[] ret=new byte[_length];
 		System.arraycopy(_array,0,ret,0,_length);
		return ret;
	}

  /**
   * Copy contents to an ASCII char[] by
   *   casting bytes to chars.
   */
  public final char[] toAsciiCharArray()
  { 
    final char[] ret=new char[_length];
    for (int i=_length;--i>=0;)
    { ret[i]=(char) _array[i];
    }
		return ret;
    
  }

  public final String toAsciiString()
  { return new String(toAsciiCharArray());
  }
}
