package spiralcraft.vfs.watcher;

import spiralcraft.vfs.Resource;

public interface WatcherHandler
{
  
  
  /**
   * Handle the event of an update to a Watched resource
   * 
   * @return A time in ms:
   * <ul>
   *   <li>0, if the update was processed successfully</li>
   *   <li>+Tms, if the update was processed successfully and
   *     the Watcher should wait no less than Tms milliseconds before checking
   *     again for an update.</li>
   *   <li>-Tms, if the update was not processed successfully- the Watcher will
   *     wait Tms seconds before calling handleUpdate() again for the 
   *     <i>same</i> update.</li> 
   * </ul>
   */
  int handleUpdate(Resource resource);
}
