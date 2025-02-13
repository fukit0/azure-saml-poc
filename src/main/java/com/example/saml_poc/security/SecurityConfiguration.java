package com.example.saml_poc.security;

import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static com.example.saml_poc.utils.SAMLUtils.marshallSAMLObject;


@EnableWebSecurity
@Configuration
@Order(2)
public class SecurityConfiguration {

    private static final String ENTITY_ID = "api://ef61d5fe-a65e-49c8-a676-13db894d49cf";
    private static final String IDP_METADATA_URL = "https://login.microsoftonline.com/cd857f0c-ad35-49e9-9555-3e0f108aa92e/federationmetadata/2007-06/federationmetadata.xml?appid=ef61d5fe-a65e-49c8-a676-13db894d49cf";
    private static final String SAML_DESTINATION = "https://login.microsoftonline.com/cd857f0c-ad35-49e9-9555-3e0f108aa92e/saml2";

    public static String generateSAMLRequest(String userName) throws Exception {
        InitializationService.initialize();

        LogoutRequest logoutRequest = buildLogoutRequest(userName);

        // 🔹 Use new marshalling method
        String xmlLogoutRequest = marshallSAMLObject(logoutRequest);

        System.out.println("Generated SAML LogoutRequest XML: \n" + xmlLogoutRequest);

        byte[] compressedSAMLRequest = compress(xmlLogoutRequest);
        return URLEncoder.encode(Base64.getEncoder().encodeToString(compressedSAMLRequest), StandardCharsets.UTF_8);
    }

    private static LogoutRequest buildLogoutRequest(String userName) {
        // Get XML Builder Factory
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        // Build LogoutRequest
        LogoutRequest logoutRequest = ((LogoutRequestBuilder) Objects.requireNonNull(builderFactory.getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME)))
                .buildObject(LogoutRequest.DEFAULT_ELEMENT_NAME);

        logoutRequest.setID("id-" + UUID.randomUUID());
        logoutRequest.setIssueInstant(new DateTime().toDate().toInstant());
        logoutRequest.setDestination(SAML_DESTINATION);
        logoutRequest.setVersion(SAMLVersion.VERSION_20);

        // Add Issuer
        Issuer issuer = ((IssuerBuilder) Objects.requireNonNull(builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME)))
                .buildObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(ENTITY_ID);
        logoutRequest.setIssuer(issuer);

        // Add NameID (User Identifier)
        NameID nameID = ((NameIDBuilder) Objects.requireNonNull(builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME)))
                .buildObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(userName);
        nameID.setFormat(NameID.EMAIL);
        logoutRequest.setNameID(nameID);

        return logoutRequest;
    }

    private static byte[] compress(String data) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        deflaterOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        deflaterOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/saml2/**", "/").permitAll()
                        .requestMatchers("/logout-success").permitAll()
                        .anyRequest().authenticated()
                )
                .saml2Login(saml2 -> saml2
                        .loginPage("/saml2/authenticate/azure")
                        .defaultSuccessUrl("/logged-in", true)
                )
                //.logout(logout -> logout.logoutUrl("/logout")
                //        .logoutSuccessUrl("/logget-out"))
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Define the logout endpoint
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Generate SAML LogoutRequest dynamically
                            String samlRequest = null;
                            try {
                                samlRequest = generateSAMLRequest(authentication.getName());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            String logoutUrl = SAML_DESTINATION
                                    + "?SAMLRequest=" + samlRequest;

                            System.out.println("Redirecting to: " + logoutUrl);
                            response.sendRedirect(logoutUrl);
                        })
                        .invalidateHttpSession(true)  // Invalidate session
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
//                .saml2Logout(saml2 -> saml2
//                        .logoutUrl("/saml2/logout")) // for SLO
        ;


        return http.build();
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation(IDP_METADATA_URL)
                .registrationId("azure")
//                .singleLogoutServiceLocation("https://login.microsoftonline.com/cd857f0c-ad35-49e9-9555-3e0f108aa92e/saml2")
//                .singleLogoutServiceResponseLocation("/logout")
                .entityId(ENTITY_ID)
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
}
