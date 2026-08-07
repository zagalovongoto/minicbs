package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.service.EmailService;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SystemParameterService paramService; // Interroge la table system_parameters

    // Injection de l'URL d'infrastructure depuis le fichier YAML ou l'environnement DevOps
    @Value("${minicbs.app.base-url}")
    private String appBaseUrl;

    @Override
    public void sendActivationEmail(AppUser user, String token) {

        // LECTURE DYNAMIQUE DU PARAMÉTRAGE MÉTIER DEPUIS LA BASE DE DONNÉES
        String mailFrom = paramService.getRequiredString("MAIL_FROM_ADDRESS");
        String bankName = paramService.getRequiredString("BANK_DISPLAY_NAME");

        // Construction dynamique de l'URL souveraine d'activation unique
        String activationUrl = appBaseUrl + "/login/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("[" + bankName + "] Activation de votre console d'habilitation");

        // Corps du mail générique, dynamique et 100% découplé
        String content = "Bonjour " + user.getPrenom() + " " + user.getNom().toUpperCase() + ",\n\n"
                + "Votre profil de gestionnaire de plateforme Core Banking vient d'être créé avec succès.\n"
                + "Pour des raisons de sécurité, votre identifiant d'accès est fixé à : " + user.getUsername() + "\n\n"
                + "Veuillez cliquer sur le lien de sécurité ci-dessous afin de définir votre mot de passe et activer votre compte :\n"
                + activationUrl + "\n\n"
                + "Attention : Ce lien d'activation est unique et expirera automatiquement sous 24 heures.\n\n"
                + "Direction de la Sécurité des Systèmes d'Information\n"
                + bankName + " Central Ledger.";

        message.setText(content);

        // Expédition asynchrone sur le réseau
        mailSender.send(message);
    }
}
