package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.model.AppUser;

public interface EmailService {
    void sendActivationEmail(AppUser user, String token);
}
