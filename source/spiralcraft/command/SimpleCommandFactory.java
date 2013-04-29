package spiralcraft.command;

public abstract class SimpleCommandFactory
  extends AbstractCommandFactory<Void,Void,Void>
{

  @Override
  public Command<Void, Void, Void> command()
  {
    return new CommandAdapter<Void,Void,Void>()
      {
        @Override
        protected void run()
        { SimpleCommandFactory.this.execute();
        }
      };
  }
  
  public abstract void execute();

}
