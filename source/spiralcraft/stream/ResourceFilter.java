package spiralcraft.stream;

/**
 * Filter interface for implementing pluggable functionality
 *   for Resource scanners. 
 */
public interface ResourceFilter
{
  public boolean accept(Resource resource);

}
