package spiralcraft.io;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * An PrintStream that delegates to an implementation that is flexibly
 *   determined by the subclass.
 * 
 * @author mike
 *
 */
public abstract class PrintStreamReference
  extends PrintStream
{

  public PrintStreamReference(boolean flush)
  { super(new NullOutputStream(),flush);
  }
  
  protected abstract PrintStream get();
  
  @Override
  public void write(int val)
  { get().write(val);
  }

  @Override
  public void write(byte[] b)
    throws IOException
  { get().write(b);
  }

  @Override
  public void write(byte[] b,int start,int len)
  { get().write(b,start,len);
  }

  @Override
  public void close()
  { get().close();
  }

  @Override
  public void flush()
  {  get().flush();
  }

  @Override
  public boolean checkError()
  { return get().checkError();
  }

  @Override
  public void print(boolean b)
  { get().print(b);
  }

  @Override
  public void print(char c)
  { get().print(c);
  }
  
  @Override
  public void print(int i)
  { get().print(i);
  }
  
  @Override
  public void print(long l)
  { get().print(l);
  }
  
  @Override
  public void print(float f)
  { get().print(f);
  }
  
  @Override
  public void print(double d)
  { get().print(d);
  }
  
  @Override
  public void print(char[] c)
  { get().print(c);
  }
  
  @Override
  public void print(String s)
  { get().print(s);
  }

  @Override
  public void print(Object o)
  { get().print(o);
  }

  @Override
  public void println()
  { get().println();
  }

  @Override
  public void println(boolean b)
  { get().println(b);
  }

  @Override
  public void println(char c)
  { get().println(c);
  }
  
  @Override
  public void println(int i)
  { get().println(i);
  }
  
  @Override
  public void println(long l)
  { get().println(l);
  }
  
  @Override
  public void println(float f)
  { get().println(f);
  }
  
  @Override
  public void println(double d)
  { get().println(d);
  }
  
  @Override
  public void println(char[] c)
  { get().println(c);
  }
  
  @Override
  public void println(String s)
  { get().println(s);
  }

  @Override
  public void println(Object o)
  { get().println(o);
  }
  
  @Override
  public PrintStream printf(String format, Object ... args)
  { 
    get().printf(format,args);
    return this;
  }

  @Override
  public PrintStream printf(Locale l, String format, Object ... args)
  { 
    get().printf(l,format,args);
    return this;
  }
  
  @Override
  public PrintStream format(String format, Object ... args)
  { 
    get().format(format,args);
    return this;
  }

  @Override
  public PrintStream format(Locale l, String format, Object ... args)
  { 
    get().format(l,format,args);
    return this;
  }
  
  @Override
  public PrintStream append(CharSequence c)
  { 
    get().append(c);
    return this;
  }

  @Override
  public PrintStream append(CharSequence c,int start,int len)
  { 
    get().append(c,start,len);
    return this;
  }

  @Override
  public PrintStream append(char c)
  { 
    get().append(c);
    return this;
  }
}
