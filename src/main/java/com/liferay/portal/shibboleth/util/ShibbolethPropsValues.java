package com.liferay.portal.shibboleth.util;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;

/**
 * @author Romeo Sheshi
 * @author Ivan Novakov <ivan.novakov@debug.cz>
 */
public class ShibbolethPropsValues {
    public static final boolean SHIBBOLETH_ENABLED = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_ENABLED));

    public static final String SHIBBOLETH_HEADER = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER);
    
    public static final String SHIBBOLETH_HEADER_EMAIL = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER_EMAIL);
    
    public static final String SHIBBOLETH_HEADER_FIRSTNAME = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER_FIRSTNAME);
    
    public static final String SHIBBOLETH_HEADER_SURNAME = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER_SURNAME);
    
    public static final String SHIBBOLETH_HEADER_AFFILIATION = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER_AFFILIATION);
    
    public static final boolean SHIBBOLETH_USER_AUTO_CREATE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_AUTO_CREATE));
    
    public static final boolean SHIBBOLETH_USER_AUTO_UPDATE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_AUTO_UPDATE));

    public static final boolean SHIBBOLETH_USER_LDAP_IMPORT = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_LDAP_IMPORT));
    
    public static final boolean SHIBBOLETH_USER_ROLE_AUTO_ASSIGN = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_ROLE_AUTO_ASSIGN));
    
    public static final String SHIBBOLETH_USER_ROLE_AUTO_ASSIGN_SUBTYPE = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_ROLE_AUTO_ASSIGN_SUBTYPE);

    public static final boolean SHIBBOLETH_LOGOUT_ENABLE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_LOGOUT_ENABLE));
    
    public static final boolean SHIBBOLETH_LOGOUT_REFERER_REDIRECT_ENABLE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_LOGOUT_REFERER_REDIRECT_ENABLE));
    
    public static final String SHIBBOLETH_LOGOUT_REFERER_REDIRECT_PATH = GetterUtil.getString(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_LOGOUT_REFERER_REDIRECT_PATH));

    public static final boolean SHIBBOLETH_HEADERS_ENABLE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADERS_ENABLE));

    public static final boolean SHIBBOLETH_AFFILIATION_TRUNCATE_ENABLE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_AFFILIATION_TRUNCATE_ENABLE));

    public static final boolean SHIBBOLETH_SCREENNAME_TRANSFORM_ENABLE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_SCREENNAME_TRANSFORM_ENABLE));

    public static final String SHIBBOLETH_LOGOUT_URL = PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_LOGOUT_URL);

    public static final String SHIBBOLETH_HEADER_AFFILIATION_PREFIX= PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_HEADER_AFFILIATION_PREFIX);
   
    public static final boolean SHIBBOLETH_USER_ROLE_AUTO_CREATE = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_ROLE_AUTO_CREATE));
    
    public static final boolean SHIBBOLETH_USER_PASSWORD_RESET_ENABLED = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_USER_PASSWORD_RESET_ENABLED));
    
    public static final boolean SHIBBOLETH_ATTRIBUTES_UTF8CONVERSION_ENABLED = GetterUtil.getBoolean(PropsUtil.get(ShibbolethPropsKeys.SHIBBOLETH_ATTRIBUTES_UTF8CONVERSION_ENABLED));
    
}
