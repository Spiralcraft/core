package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Context;


public class FocusResolveNode
  extends Node
{

  private final FocusNode _source;
  private final IdentifierNode _identifier;

  public FocusResolveNode(FocusNode source,IdentifierNode identifier)
  { 
    _source=source;
    _identifier=identifier;
  }

  
  public Optic bind(final Focus focus)
    throws BindException
  { 
    String identifier=_identifier.getIdentifier();

    Focus specifiedFocus
      =_source!=null?_source.findFocus(focus):focus;

    Context context=specifiedFocus.getContext();
    
    Optic ret=null;
    if (context!=null)
    { ret=context.resolve(_identifier.getIdentifier());
    }

    if (ret==null)
    { 
      try
      { 
        Optic subject=specifiedFocus.getSubject();
        if (subject!=null)
        { ret=subject.resolve(specifiedFocus,_identifier.getIdentifier(),null);
        }
      }
      catch (BindException x)
      { 
        throw new BindException
          ("Could not resolve identifier '"
          +_identifier.getIdentifier()
          +"' in Context or Subject of Focus"
          );
      }
    }
    
    if (ret==null)
    {
      throw new BindException
        ("Could not resolve identifier '"
        +_identifier.getIdentifier()
        +"' in Context or Subject of Focus"
        );
    }
    return ret;  
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("FocusResolve");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    _identifier.dumpTree(out,prefix);
  }
  
}
