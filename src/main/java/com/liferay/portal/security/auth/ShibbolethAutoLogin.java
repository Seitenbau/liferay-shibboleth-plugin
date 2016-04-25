package com.liferay.portal.security.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.ldap.PortalLDAPImporterUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.shibboleth.util.ShibbolethPropsKeys;
import com.liferay.portal.shibboleth.util.Util;
import com.liferay.portal.util.PortalUtil;

/**
 * Performs autologin based on the header values passed by Shibboleth.
 *
 * The Shibboleth user ID header set in the configuration must contain the user ID.
 * (Portal settings --> Authentication --> General).
 *
 * @author Romeo Sheshi
 * @author Ivan Novakov <ivan.novakov@debug.cz>
 * @author Michael Trunner <michael.trunner@seitenbau.com>
 */
public class ShibbolethAutoLogin implements AutoLogin {

	private static Log _log = LogFactoryUtil.getLog(ShibbolethAutoLogin.class);

    @Override
    public String[] handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws AutoLoginException {
        // taken from BaseAutoLogin
        if (Validator.isNull(request.getAttribute(AutoLogin.AUTO_LOGIN_REDIRECT))) {
            throw new AutoLoginException(e);
        }
        _log.error(e, e);
        return null;
    }

    @Override
    public String[] login(HttpServletRequest req, HttpServletResponse res) throws AutoLoginException {

        User user;
        String[] credentials = null;
        HttpSession session = req.getSession(false);
        long companyId = PortalUtil.getCompanyId(req);


        try {
            _log.info("Shibboleth Autologin [modified 2]");

            if (!Util.isEnabled(companyId)) {
                return credentials;
            }

            user = loginFromSession(companyId, session);
            if (Validator.isNull(user)) {
                return credentials;
            }

            credentials = new String[3];
            credentials[0] = String.valueOf(user.getUserId());
            credentials[1] = user.getPassword();
            credentials[2] = Boolean.TRUE.toString();
            return credentials;

        } catch (NoSuchUserException e) {
            logError(e);
        } catch (Exception e) {
            logError(e);
            throw new AutoLoginException(e);
        }

        return credentials;
    }

    private User loginFromSession(long companyId, HttpSession session) throws Exception {
        String login;
        User user = null;

        login = (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_LOGIN);
        if (Validator.isNull(login)) {
            return null;
        }

        String authType = Util.getAuthType(companyId);

        try {
            if (authType.equals(CompanyConstants.AUTH_TYPE_SN)) {
                _log.info("Trying to find user with screen name: " + login);
                user = UserLocalServiceUtil.getUserByScreenName(companyId, login);
            } else if (authType.equals(CompanyConstants.AUTH_TYPE_EA)) {
                _log.info("Trying to find user with email: " + login);
                user = UserLocalServiceUtil.getUserByEmailAddress(companyId, login);
            } else {
                throw new NoSuchUserException();
            }

            _log.info("User found: " + user.getScreenName() + " (" + user.getEmailAddress() + ")");

            if (Util.autoUpdateUser(companyId)) {
                _log.info("Auto-updating user...");
                updateUserFromSession(companyId, user, session);
            }

        } catch (NoSuchUserException e) {
            _log.error("User "  + login + " not found");

            if (Util.autoCreateUser(companyId)) {
                _log.info("Importing user from session...");
                user = createUserFromSession(companyId, session);
                _log.info("Created user with ID: " + user.getUserId());
            } else if (Util.importUser(companyId)) {
                _log.info("Importing user from LDAP...");
                user = PortalLDAPImporterUtil.importLDAPUser(companyId, StringPool.BLANK, login);
            }
        }

        try {
            updateUserRolesFromSession(companyId, user, session);
        } catch (Exception e) {
            _log.error("Exception while updating user roles from session: " + e.getMessage());
        }

        return user;
    }

    /**
     * Create user from session
     */
    protected User createUserFromSession(long companyId, HttpSession session) throws Exception {
        User user = null;

        String screenName = convertAttribute(companyId, (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_LOGIN));
        if (Validator.isNull(screenName)) {
            _log.error("Cannot create user - missing screen name");
            return user;
        }

        // SHIBBOLETH email address is not unique. Liferay will generate one if no email address is given.
        String emailAddress = StringPool.BLANK;

        String firstname = convertAttribute(companyId, (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_HEADER_FIRSTNAME));
        if (Validator.isNull(firstname)) {
            _log.error("Cannot create user - missing firstname");
            return user;
        }

        String surname = convertAttribute(companyId, (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_HEADER_SURNAME));
        if (Validator.isNull(surname)) {
            _log.error("Cannot create user - missing surname");
            return user;
        }

        _log.info("Creating user: screen name = [" + screenName + "], emailAddress = [" + emailAddress
                + "], first name = [" + firstname + "], surname = [" + surname + "]");


        return addUser(
            companyId, cutString(screenName), cutString(emailAddress),
            cutString(firstname), cutString(surname));
    }

    private String cutString(String field)
    {
      return cutString(field, 75);
    }
    
    private String cutString(String field, int maxlength)
    {
      if (field == null)
      {
        return null;
      }
      if (field.length() <= maxlength)
      {
        return field;
      }
      return field.substring(0, maxlength);
    }
    
    /**
     * Store user
     */
    private User addUser(long companyId, String screenName, String emailAddress, String firstName, String lastName)
            throws Exception {

        long creatorUserId = 0;
        boolean autoPassword = true;
        String password1 = null;
        String password2 = null;
        boolean autoScreenName = false;
        long facebookId = 0;
        String openId = StringPool.BLANK;
        Locale locale = Locale.GERMANY;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        boolean male = true;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = StringPool.BLANK;

        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;

        boolean sendEmail = false;
        ServiceContext serviceContext = null;

        User user =  UserLocalServiceUtil.addUser(creatorUserId, companyId, autoPassword, password1, password2,
                autoScreenName, screenName, emailAddress, facebookId, openId, locale, firstName, middleName, lastName,
                prefixId, suffixId, male, birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
                organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

        user.setPasswordReset(false);
        if (Util.isUserPasswordReset(companyId))
    	{
          user.setPasswordReset(true);
    	}

//        user.setAgreedToTermsOfUse(true);
//        user.setReminderQueryAnswer("default");
//        user.setReminderQueryAnswer("default");
        UserLocalServiceUtil.updateUser(user);

        return user;
    }

    protected void updateUserFromSession(long companyId, User user, HttpSession session) throws Exception {
        boolean modified = false;

        String firstname = cutString(convertAttribute(companyId, (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_HEADER_FIRSTNAME)));
        if (Validator.isNotNull(firstname) && !user.getFirstName().equals(firstname)) {
            _log.info("User [" + user.getScreenName() + "]: update first name [" + user.getFirstName() + "] --> ["
                    + firstname + "]");
            user.setFirstName(firstname);
            modified = true;
        }

        String surname = cutString(convertAttribute(companyId, (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_HEADER_SURNAME)));
        if (Validator.isNotNull(surname) && !user.getLastName().equals(surname)) {
            _log.info("User [" + user.getScreenName() + "]: update last name [" + user.getLastName() + "] --> ["
                    + surname + "]");
            user.setLastName(surname);
            modified = true;
        }

        if (modified) {
            UserLocalServiceUtil.updateUser(user);
        }
    }

	/**
	 * Shibboleth attributes are by default UTF-8 encoded. However, depending on
	 * the servlet contaner configuration they are interpreted as ISO-8859-1
	 * values. This causes problems with non-ASCII characters. The solution is
	 * to re-encode attributes, e.g. with:
	 *
	 * @see https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPAttributeAccess
	 *      #NativeSPAttributeAccess-Tool-SpecificExamples
	 *
	 * @param attribute
	 *            shibboleth attribute
	 * @return return utf-8 converted attribute, if enabled
	 * @throws Exception
	 */
    private String convertAttribute(long companyId, String attribute) throws Exception
    {
    	if (attribute != null && Util.isAttributeUtf8Conversion(companyId))
    	{
    		return new String(attribute.getBytes("ISO-8859-1"), "UTF-8");
    	}
    	return attribute;
    }

    private void updateUserRolesFromSession(long companyId, User user, HttpSession session) throws Exception {
        if (!Util.autoAssignUserRole(companyId)) {
            return;
        }

        List<Role> currentFelRoles = getRolesFromSession(companyId, session);
        long[] currentFelRoleIds = roleListToLongArray(currentFelRoles);

        List<Role> felRoles = getAllRolesWithConfiguredSubtype(companyId);
        long[] felRoleIds = roleListToLongArray(felRoles);

        RoleLocalServiceUtil.unsetUserRoles(user.getUserId(), felRoleIds);
        RoleLocalServiceUtil.addUserRoles(user.getUserId(), currentFelRoleIds);

        _log.info("User '" + user.getScreenName() + "' has been assigned " + currentFelRoleIds.length + " role(s): "
                + Arrays.toString(currentFelRoleIds));
    }

    private long[] roleListToLongArray(List<Role> roles) {
        long[] roleIds = new long[roles.size()];

        for (int i = 0; i < roles.size(); i++) {
            roleIds[i] = roles.get(i).getRoleId();
        }

        return roleIds;
    }

    private List<Role> getAllRolesWithConfiguredSubtype(long companyId) throws Exception {
        String roleSubtype = Util.autoAssignUserRoleSubtype(companyId);
        return RoleLocalServiceUtil.getSubtypeRoles(roleSubtype);
    }

    private List<Role> getRolesFromSession(long companyId, HttpSession session) throws SystemException {
        List<Role> currentFelRoles = new ArrayList<Role>();
        String affiliation = (String) session.getAttribute(ShibbolethPropsKeys.SHIBBOLETH_HEADER_AFFILIATION);

        if (Validator.isNull(affiliation)) {
            return currentFelRoles;
        }

        String[] affiliationList = affiliation.split(",");
        for (String roleName : affiliationList) {
            Role role;
            try {
                role = RoleLocalServiceUtil.getRole(companyId, roleName);
            } catch (PortalException e) {
                _log.info("Exception while getting role with name '" + roleName + "': " + e.getMessage());
                try{
                    if(Util.isCreateRoleEnabled(companyId)){
                        List<Role> roleList = RoleLocalServiceUtil.getRoles(companyId);
                        long [] roleIds = roleListToLongArray(roleList);
                        Arrays.sort(roleIds);
                        long newId = roleIds[roleIds.length-1];
                        newId = newId+1;
                        role = RoleLocalServiceUtil.createRole(newId);

                        long classNameId = 0;
                        try{
                        	classNameId = RoleLocalServiceUtil.getRole(roleIds[roleIds.length-1]).getClassNameId();
                        }catch (PortalException ex){
                        	_log.info("classname error");
                        }
                        role.setClassNameId(classNameId);
        				role.setCompanyId(companyId);
        				role.setClassPK(newId);
        				role.setDescription(null);
        				role.setTitleMap(null);
        				role.setName(roleName);
        				role.setType(1);
                final String subtype = Util.autoAssignUserRoleSubtype(companyId);
                if (subtype != null & subtype.length() != 0)
                {
                  role.setSubtype(subtype);
                }
        				RoleLocalServiceUtil.addRole(role);
                    }
                    else
                    {
                      continue;
                    }
                }catch (Exception exc){
                    continue;
                }
            }

            currentFelRoles.add(role);
        }

        return currentFelRoles;
    }

    private void logError(Exception e) {
        _log.error("Exception message = " + e.getMessage() + " cause = " + e.getCause());
        if (_log.isDebugEnabled()) {
            _log.error(e);
        }

    }

}