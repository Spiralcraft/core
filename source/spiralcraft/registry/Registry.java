package spiralcraft.registry;

public class Registry
{

  private static RegistryNode _LOCAL_ROOT
    =new LocalRegistryNode(null,"");

  public static RegistryNode getLocalRoot()
  { return _LOCAL_ROOT;
  }
}
