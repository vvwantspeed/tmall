package com.tmall.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.tmall.entity.User;
import com.tmall.service.UserService;

public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // System.out.println("AuthorizationInfo -> 授权");

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // // info.addStringPermission("king");
        // // 拿到当前登录的对象
        // Subject subject = SecurityUtils.getSubject();
        // // 拿到user对象，来自下面认证里返回的值
        // User currentUser = (User) subject.getPrincipal();
        // System.out.println(currentUser.getPerms());
        // info.addStringPermission(currentUser.getPerms());

        return info;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("AuthenticationInfo -> 认证");

        String userName = authenticationToken.getPrincipal().toString();
        // String userName =  ((UsernamePasswordToken) authenticationToken).getUsername();

        User user = userService.getOneByName(userName);
        // if (user == null) {
        //     return null; // 抛出异常 UnknownAccountException
        // }

        String passwordInDB = user.getPassword();
        String salt = user.getSalt();

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                userName, passwordInDB, ByteSource.Util.bytes(salt), getName());
        return authenticationInfo;
    }
}
