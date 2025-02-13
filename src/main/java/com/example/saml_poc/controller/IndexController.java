package com.example.saml_poc.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;

@Controller
@Slf4j
public class IndexController {

    @RequestMapping(value = {"/", "/index"})
    public String index(Model model, Authentication authentication, HttpServletRequest req) {
        if (authentication != null && authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {
            log.info("Got Relay State {}", req.getParameter("RelayState"));
            log.info("Sample SAML Application - You are logged in!, attributes: {}", principal.getAttributes());
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            log.info("authorities: {}", authorities);

            model.addAttribute("username", principal.getAttributes().get("http://schemas.microsoft.com/identity/claims/displayname").get(0));
            model.addAttribute("authorities", authorities);
            return "logged-in";
        } else {
            log.info("Sample SAML Application - You are not logged in!");
            return "index";
        }
    }

    @RequestMapping(value = {"/logged-in"})
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {
            log.info("Sample SAML Application - You are logged in!, attributes: {}", principal.getAttributes());

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            log.info("authorities: {}", authorities);

            model.addAttribute("username", principal.getAttributes().get("http://schemas.microsoft.com/identity/claims/displayname").get(0));
            model.addAttribute("authorities", authorities);
        }
        return "logged-in";
    }

    @RequestMapping(value = {"/logout-success"})
    public String testt(Model model) {
        log.info("Sample SAML Application - You are logged out!");
        return "logged-out";
    }

}