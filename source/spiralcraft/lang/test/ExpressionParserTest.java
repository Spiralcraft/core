package spiralcraft.lang.test;

import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.parser.ExpressionParser;


import spiralcraft.util.Arguments;

public class ExpressionParserTest
{

  private String _expression=null;
  private boolean _dump=false;
  private int _repeats=0;

  public static void main(String[] args)
  {
    final ExpressionParserTest test=new ExpressionParserTest();
    test.run(args);
  }
  
  public void run(String[] args)
  {
    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="expression")
        { _expression=nextArgument();
        }
        else if (option=="dump")
        { _dump=true;
        }
        else if (option=="repeats")
        { _repeats=Integer.parseInt(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }
    }.process(args,'-');

    ExpressionParser parser = new ExpressionParser();

    try 
    {
      long time=System.currentTimeMillis();
      Expression expression = parser.parse(_expression);
      System.err.println("Initial read time "+(System.currentTimeMillis()-time));
      
      if (_dump)
      { 
        StringBuffer out=new StringBuffer();
        expression.dumpParseTree(out);
        System.err.println(out.toString());
      }

      if (_repeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_repeats;i++)
        { expression=parser.parse(_expression);
        }
        System.err.println(_repeats+" repeats time "+(System.currentTimeMillis()-time));
      }
    } 
    catch (ParseException e) 
    { 
      System.err.println(e.getMessage());
      e.printStackTrace();

    }

  }

}
