package spiralcraft.lang;


/**
 * <p>A Telescoped Focus, referencing a context internal to a single expression
 * </p>
 * 
 * <p>The parent focus context is used to resolve all context names 
 *   (non-dot-prefixed names), and subject references (dot-prefixed names)
 *   will resolve to the provided subject
 * </p>
 * 
 * @author mike
 *
 */
public class TeleFocus<T>
  extends BaseFocus<T>
  implements Focus<T>
{
  
 /**
  * <p>The parent focus context is used to resolve all context names 
  *   (non-dot-prefixed names), and subject references (dot-prefixed names)
  *   will resolve to the provided subject
  * </p>
  * 
  * @param parentFocus The parentFocus which provides the context
  * @param subject The channel which provides the subject.
  */
  public TeleFocus(Focus<?> parentFocus,Channel<T> subject)
  { super(parentFocus,subject);
  }
  
  @Override
  public Channel<?> getContext()
  { 
    if (parent!=null)
    { return parent.getContext();
    }
    return null;
  }
  

  @Override
  public String toString()
  {
    return super.toString()
      +(getContext()!=null
        ?"{ context: "+getContext().getReflector().getTypeURI()+" }"
        :"null context"
       )
      +(namespaceResolver!=null
        ?"{ ns:"+namespaceResolver.toString()+" }"
        :""
        );
  }


}
