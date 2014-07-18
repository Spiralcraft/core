package spiralcraft.text.translator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.text.ParseException;

class TranslatorChannel
  extends SourcedChannel<String,String>
{
  private final Translator translator;
  
  public TranslatorChannel
    (Translator translator
    ,Channel<String> source
    )
  { 
    super(BeanReflector.<String>getInstance(String.class),source);
    this.translator=translator;
  }

  @Override
  protected String retrieve()
  { 
    String val=source.get();
    try
    { return translator.translateOut(val);
    }
    catch (ParseException x)
    { throw new AccessException("Error translating output :"+val,x);
    }
  }

  @Override
  protected boolean store(String val)
    throws AccessException
  { 
    try
    { return source.set(translator.translateIn(val));
    }
    catch (ParseException x)
    { throw new AccessException("Error translating input :"+val,x);
    }
  }
  
  @Override
  public boolean isWritable()
  { return source.isWritable();
  }
}