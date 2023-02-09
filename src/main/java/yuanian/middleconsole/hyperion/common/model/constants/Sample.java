package yuanian.middleconsole.hyperion.common.model.constants;

import com.hyperion.css.*;
import com.hyperion.css.common.CSSGroupIF;
import com.hyperion.css.common.CSSPrincipalIF;
import com.hyperion.css.common.CSSRoleIF;
import com.hyperion.css.common.CSSUserIF;
import com.hyperion.css.common.configuration.CSSConfigurationException;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public abstract class Sample {

	/**
	 * Handle to Instance of CSS System
	 */
    protected CSSSystem system = null;
	/**
	 * Handle to Instance of CSSAPI
	 */
	protected CSSAPIIF cssAPI = null;
	/**
	 * Handle to Directory management API
	 */
	protected CSSDirectoryManagementAPIIF cssDMAPI;
	/**
	 * Handle to User Provisioning API
	 */
	protected CSSUserProvisioningAPIIF cssUPAPI;
	/**
	 * the identity of the entity performing the operation.
	 */
	protected CSSPrincipalIF principal = null;

	protected HashMap context;

	public static String PRODUCT = "HUB-11.1.2.0";


	public Sample(String adminUser, String password) throws Exception {
		initialize(adminUser,password);
	}

   /**
    * Initialize the CSS Framework
    */
    private void initialize(String adminUser, String password) throws Exception
    {
    	System.out.println("Initializing CSS ...");
    	context = new HashMap(3);

		/**
		 * Get Instance of the CSS System. Specify the location of
		 * the Shared Services Client log file.
		 * CSS will be intialized from configuration stored in Registry.
		 */
    	system = CSSSystem.getInstance(context, "logs");
    	System.out.println("Initialized CSS.");

    	// get Handle the CSS API Interface
    	cssAPI = system.getCSSAPI();

    	cssDMAPI = cssAPI.getDirectoryManagementAPI(new HashMap(3));

    	cssUPAPI = cssAPI.getUserProvisioningAPI(new HashMap(3));

    	System.out.println("Getting the Principal ...");
    	principal = getPrincipal(adminUser,password);
    	System.out.println("Got Principal.\n");

    	context = new HashMap(3);
    }

	/**
	 * Shuts down the CSS framework.
	 */
    public void shutdown() {
    	try {
    		system.shutdown();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

      private CSSPrincipalIF getPrincipal(String adminUser, String password) throws Exception {
    	  CSSUserIF admin  = authenticate(adminUser,password);
    	  return admin.getPrincipal();
      }


      protected HashMap getContext() {
    	return new HashMap();
      }

      
      public CSSUserIF[] getUserByLoginName(String name) throws CSSIllegalArgumentException, CSSConfigurationException, CSSCommunicationException, CSSException{
    	  return cssAPI.getUsers(context, principal, name);
      }

      protected CSSGroupIF[] getGroupByName(String name) throws CSSIllegalArgumentException, CSSConfigurationException, CSSCommunicationException, CSSException{
    	  return cssAPI.getGroups(context, principal, name);
      }
      
      protected CSSUserIF authenticate(String username, String password) throws CSSException
      {
          CSSUserIF user = null;
          System.out.println("Authenticating user: " + username);

          Map context = new HashMap();
          user = cssAPI.authenticate(context, username, password);
          System.out.println("Got User: " + user);

         
          return user;
      }

      protected void dump(PrintStream out, String msg, String [] arr){
    	  out.println(msg);
    	  for (int i = 0; i < arr.length; i++){
    		  out.println(arr[i]);
    	  }
      }

      protected void dumpUsers(PrintStream out, String msg, CSSUserIF[] users){
    	  out.println(msg);
		  out.println();
    	  for (int i=0 ; i <users.length; i++) {
    		  out.println("User Name --> "+ users[i].getLoginName());
    		  out.println("User Email-->" + users[i].getEmailAddress()[0]);
    		  out.println("User Description-->" + users[i].getDescription());
    		  out.println("User FN -->" + users[i].getFirstName());
    		  out.println("User Identity-->" + users[i].getIdentity());
    		  out.println();
    	  }

      }

      protected void dumpGroups(PrintStream out, String msg, CSSGroupIF[] groups){
    	  out.println(msg);
		  out.println();
    	  for (int i=0 ; i <groups.length; i++) {
    		  out.println("Group Name-->" + groups[i].getName());
    		  out.println("Group Description-->" + groups[i].getDescription());
    		  out.println("Group Identity-->" + groups[i].getIdentity());
    		  out.println();
    	  }

      }

      // print group names
      protected void dumpGroups(PrintStream out, String msg, String[] groups){
    	  out.println(msg);
    	  out.println();
    	  for (int i=0 ; i <groups.length; i++) {
    		  CSSGroupIF group = null;
    		  try {
    			  group = cssAPI.getGroupByIdentity(context, principal, groups[i]);
    		  } catch (CSSException csse) {
    			  // should work
    		  }
    		  out.println("Group Name-->" + (group != null ? group.getName() : null));
    	  }
    	  out.println();
      }

      // print user names
      protected void dumpUsers(PrintStream out, String msg, String[] users){
    	  out.println(msg);
    	  out.println();
    	  for (int i=0 ; i <users.length; i++) {
    		  CSSUserIF user = null;
    		  try {
    			  user = cssAPI.getUserByIdentity(context, principal, users[i]);
    		  } catch (CSSException csse) {
    			  // should work
    		  }
    		  out.println("User Name-->" + (user != null ? user.getLoginName() : null));
    	  }
    	  out.println();
      }

      protected void dumpRoles(PrintStream out, String msg, CSSRoleIF[] roles){
    	  out.println(msg);
		  out.println();
    	  for (int i=0 ; i <roles.length; i++) {
    		  out.println("role Name-->" + roles[i].getName());
    		  out.println("role Description-->" + roles[i].getDescription());
    		  out.println("role Identity-->" + roles[i].getIdentity());
    		  out.println();
    	  }
      }

      protected abstract void cleanup(String []identities) throws CSSException;


	  protected void printMsg(PrintStream out, String msg) {
		  out.println(msg);
	  }

}