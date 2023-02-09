package yuanian.middleconsole.hyperion.service.impl;

import com.hyperion.css.CSSException;
import com.hyperion.css.common.CSSNativeUserIF;
import yuanian.middleconsole.hyperion.common.model.constants.Sample;

import java.util.HashMap;
import java.util.Map;

/**
 * @author meizhiliang
 * @projectName hyperion
 * @date 2022/10/19
 * @menu: TODO
 */
public class UserFunctionsSample extends Sample {

    public UserFunctionsSample(String adminUser, String password) throws Exception {
        super(adminUser,password);
    }

    /**
     * Delete User
     */
    public void deleteUser(String identity) throws CSSException {
        cssDMAPI.deleteNativeUsers(new HashMap(3), principal, new String[] { identity });
    }

    /**
     * Modify User. Only possible with a native provider.
     */
    public void modifyUser(Map<String,String> requestinfo ) throws CSSException {
        HashMap context =  new HashMap(3);
        CSSNativeUserIF user = cssDMAPI.getNativeUserInstance(context,principal);
        user.setLoginName(requestinfo.get("externalId"));
        user.setDescription(requestinfo.get("displayName"));
        user.setFirstName(requestinfo.get("userName"));
        user.setEmailAddress(requestinfo.get("emails"));
        cssDMAPI.updateNativeUser(context, principal, user);
    }

    /**
     * create User.
     */
    public CSSNativeUserIF createUser(Map<String,String> requestinfo) throws CSSException {
        HashMap context =  new HashMap(3);
        CSSNativeUserIF user = cssDMAPI.getNativeUserInstance(context,principal);
        user.setLoginName(requestinfo.get("externalId"));
        user.setDescription(requestinfo.get("displayName"));
        user.setFirstName(requestinfo.get("userName"));
        user.setEmailAddress(requestinfo.get("emails"));
        cssDMAPI.addNativeUser(context, principal, user);
        return user;
    }


    @Override
    protected void cleanup(String[] identities) throws CSSException {
        // delete the test users created
        if (identities != null && identities.length != 0) {
            for (int i = 0; i < identities.length; i++) {
                try {
                    deleteUser(identities[i]);
                } catch (CSSException ce) {
                    ce.printStackTrace();
                }
            }
        }
    }
}
