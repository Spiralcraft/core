package spiralcraft.lang.test;

import spiralcraft.lang.parser.ExpressionParser;
import spiralcraft.lang.parser.ParseException;

import spiralcraft.lang.Expression;
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.DefaultEnvironment;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.StringOptic;


import spiralcraft.util.Arguments;

public class ExpressionResolverTest
{

  private String _expression=null;
  private boolean _dump=false;
  private int _bindRepeats=0;
  private int _getRepeats=0;

  public static void main(String[] args)
  {
    final ExpressionResolverTest test
      =new ExpressionResolverTest();
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
        else if (option=="bindRepeats")
        { _bindRepeats=Integer.parseInt(nextArgument());
        }
        else if (option=="getRepeats")
        { _getRepeats=Integer.parseInt(nextArgument());
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

      DefaultFocus focus=new DefaultFocus();
      DefaultEnvironment environment=new DefaultEnvironment();
      environment.bind
        ("test"
        ,new StringOptic()
        {
          public Object get()
          { return "testValue";
          }
        }
        );
      focus.setEnvironment(environment);

      time=System.currentTimeMillis();

      Channel channel=expression.createChannel(focus);

      System.err.println("Bind time "+(System.currentTimeMillis()-time));

      if (_bindRepeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_bindRepeats;i++)
        { expression.createChannel(focus);
        }
        System.err.println(_bindRepeats+" repeats bind time "+(System.currentTimeMillis()-time));
      }

      System.out.println(channel.get());      

      if (_getRepeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_getRepeats;i++)
        { channel.get();
        }
        System.err.println(_getRepeats+" repeats get time "+(System.currentTimeMillis()-time));
      }
    } 
    catch (ParseException e) 
    { 
      System.err.println(e.getMessage());
      e.printStackTrace();

    }
    catch (BindException e) 
    { 
      System.err.println(e.getMessage());
      e.printStackTrace();

    }

  }

}
