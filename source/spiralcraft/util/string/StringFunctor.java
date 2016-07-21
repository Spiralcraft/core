package spiralcraft.util.string;


import spiralcraft.lang.ParseException;
import spiralcraft.lang.UnaryFunctionBinding;
import spiralcraft.lang.reflect.BeanReflector;

public class StringFunctor
  extends UnaryFunctionBinding<String,String,RuntimeException>
{

  public StringFunctor(String expression)
    throws ParseException
  { 
    super(expression);
    setInputReflector(BeanReflector.<String>getInstance(String.class));
  }

  
}