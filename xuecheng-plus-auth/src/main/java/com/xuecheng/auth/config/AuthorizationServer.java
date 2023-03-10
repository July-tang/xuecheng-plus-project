package com.xuecheng.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Resource;

/**
 * 授权服务器配置
 *
 * @author july
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource(name = "authorizationServerTokenServicesCustom")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 客户端详情服务
     *
     * @param clients 认证配置客户端
     * @throws Exception 异常
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("XcWebApp")
                //客户端密钥
                .secret(new BCryptPasswordEncoder().encode("XcWebApp"))
                 //资源列表
                .resourceIds("xuecheng-plus")
                 //允许的授权类型
                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                .scopes("all")
                .autoApprove(false)
                .redirectUris("http://www.51xuecheng.cn");
    }


    /**
     * 令牌端点的访问配置
     *
     * @param endpoints 令牌配置
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        //认证管理器和令牌管理服务
        endpoints.authenticationManager(authenticationManager)
                .tokenServices(authorizationServerTokenServices)
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    /**
     * 令牌端点的安全配置
     *
     * @param security 令牌安全配置
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        //表单认证(申请令牌), 并设置权限
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

}
