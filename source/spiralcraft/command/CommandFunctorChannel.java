package spiralcraft.command;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.AbstractFunctorChannel;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

/**
 * Obtains a Command from a CommandFactory, applies a set of parameters to
 *   the Command.context, executes the command and returns the result. 
 * 
 * @author mike
 *
 * @param <Ttarget>
 * @param <Tcontext>
 * @param <Tresult>
 */
public class CommandFunctorChannel<Ttarget,Tcontext,Tresult>
  extends AbstractFunctorChannel<Tresult>
{
  
  private static final ClassLog log
    =ClassLog.getInstance(CommandFunctorChannel.class);
  
  private ThreadLocalChannel<Tcontext> context;
  private final CommandFactory<Ttarget,Tcontext,Tresult> factory;
  
  // XXX Move to new CommandReflector class as generic subtype
  @SuppressWarnings("unchecked")
  protected static <X> Reflector<X> 
    getResultReflector(CommandFactory<?,?,?> factory)
      throws BindException
  {
    Channel cc=new SimpleChannel(factory.getCommandReflector());
    return cc.resolve(null, "result", null).getReflector();
  }

  // XXX Move to new CommandReflector class as generic subtype
  @SuppressWarnings("unchecked")
  protected static <X> Reflector<X> 
    getContextReflector(CommandFactory<?,?,?> factory)
      throws BindException
  {
    Channel cc=new SimpleChannel(factory.getCommandReflector());
    return cc.resolve(null, "context", null).getReflector();
  }
  
  public CommandFunctorChannel
    (CommandFactory<Ttarget,Tcontext,Tresult> factory)
    throws BindException
  { 
    super(CommandFunctorChannel.<Tresult>getResultReflector(factory));
    this.factory=factory;
  }

  @Override
  protected void bindTarget(Focus<?> focus)
    throws BindException
  {
    Reflector<Tcontext> contextReflector
      =CommandFunctorChannel.<Tcontext>getContextReflector(factory);
    
    context=new ThreadLocalChannel<Tcontext>
      (contextReflector);
    Focus<?> contextFocus=focus.chain(context);
    super.bindTarget(contextFocus);
  }
  
  @Override
  protected Tresult retrieve()
  {
    try
    {
      context.push(null);
      Command<Ttarget,Tcontext,Tresult> command
        =factory.command();
      
      Tcontext contextVal=command.getContext();
      context.set(contextVal);
      
      applyContextBindings();
      
      Tcontext newContextVal=context.get();
      if (newContextVal!=contextVal)
      { 
        command.setContext(newContextVal);
        log.fine("Set context to "+context.get());
      }
      
      command.execute();
      if (command.getException()!=null)
      { 
        throw new AccessException
          ("Error executing command ",command.getException());
      }
      return command.getResult();

    }
    finally
    { context.pop();
    }
      
  }

  @Override
  protected boolean store(
    Tresult val)
    throws AccessException
  { throw new UnsupportedOperationException("Command results are read only");
  }

}
